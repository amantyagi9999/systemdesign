package com.truecaller.app.batch;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.expressions.Window;
import org.apache.spark.sql.expressions.WindowSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.Instant;

import static org.apache.spark.sql.functions.*;
import static org.apache.spark.sql.types.DataTypes.DoubleType;

/**
 * Scheduled offline job (e.g. every 6-24h via cron / Airflow / EMR step)
 * that recomputes the crowdsourced caller-name resolution for every phone
 * number, using the trust-weighted whole-string-frequency algorithm
 * designed in the interview:
 *
 *   1. Read every user's uploaded contact book from MongoDB
 *   2. Weight each (number -> savedName) vote by the contributing
 *      account's trust score (age, phone verification, upload velocity)
 *   3. Pick the highest-total-weight name per number (NOT token-level
 *      frequency - whole strings only, to avoid stitching together a
 *      name nobody actually saved)
 *   4. Override with a verified business name where the number matches
 *      the verified business registry
 *   5. Bulk-write results into the DynamoDB resolved index via UpdateItem
 *      (never touching spamLabel/spamScore, which the near-real-time
 *      Kafka pipeline owns)
 *
 * Run with:
 *   spark-submit --class com.truecaller.batch.NameResolutionJob \
 *     --master yarn --deploy-mode cluster \
 *     name-resolution-batch.jar \
 *     <mongoUri> <dynamoTableName> <awsRegion> [dynamoEndpointOverride]
 */
public class NameResolutionJob {

    private static final Logger log = LoggerFactory.getLogger(NameResolutionJob.class);

    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: NameResolutionJob <mongoUri> <dynamoTableName> <awsRegion> [dynamoEndpointOverride]");
            System.exit(1);
        }
        String mongoUri = args[0];
        String dynamoTable = args[1];
        String awsRegion = args[2];
        String dynamoEndpointOverride = args.length > 3 ? args[3] : null;

        SparkSession spark = SparkSession.builder()
                .appName("truecaller-name-resolution-batch")
                .config("spark.mongodb.read.connection.uri", mongoUri)
                .getOrCreate();

        try {
            run(spark, dynamoTable, awsRegion, dynamoEndpointOverride);
        } finally {
            spark.stop();
        }
    }

    static void run(SparkSession spark, String dynamoTable, String awsRegion, String dynamoEndpointOverride) {
        long startTime = System.currentTimeMillis();

        // ---- 1. Load raw contacts, explode the per-user array into one row
        //         per (userId, number, savedName) vote.
        Dataset<Row> contactsRaw = spark.read()
                .format("mongodb")
                .option("collection", "contacts")
                .load();

        Dataset<Row> votes = contactsRaw
                .select(col("_id").alias("userId"), explode(col("contacts")).alias("c"))
                .select(
                        col("userId"),
                        col("c.number").alias("number"),
                        col("c.savedName").alias("savedName"),
                        col("c.photoUrl").alias("photoUrl")
                )
                .filter(col("number").isNotNull().and(col("savedName").isNotNull()))
                // One vote per user per number, even if their upload had
                // accidental duplicate rows for the same contact.
                .dropDuplicates("userId", "number");

        // ---- 2. Load contributor trust signals and compute a weight per user.
        Dataset<Row> users = spark.read()
                .format("mongodb")
                .option("collection", "users")
                .load()
                .select(
                        col("_id").alias("userId"),
                        col("accountCreatedAt"),
                        coalesce(col("phoneVerified"), lit(false)).alias("phoneVerified"),
                        coalesce(col("uploadsInLast24h"), lit(0)).alias("uploadsInLast24h")
                );

        Timestamp now = Timestamp.from(Instant.now());
        spark.udf().register("trustScore",
                (java.sql.Timestamp createdAt, Boolean phoneVerified, Integer uploads) ->
                        com.truecaller.app.batch.TrustScoreCalculator.compute(
                                createdAt == null ? null : createdAt.toInstant(),
                                Boolean.TRUE.equals(phoneVerified),
                                uploads == null ? 0 : uploads,
                                now.toInstant()
                        ),
                DoubleType);

        Dataset<Row> usersWithTrust = users.withColumn(
                "trustScore",
                callUDF("trustScore", col("accountCreatedAt"), col("phoneVerified"), col("uploadsInLast24h"))
        );

        // ---- 3. Weighted whole-string-frequency vote aggregation.
        Dataset<Row> weightedVotes = votes.join(usersWithTrust, "userId");

        Dataset<Row> nameWeights = weightedVotes
                .groupBy(col("number"), col("savedName"))
                .agg(
                        sum("trustScore").alias("totalWeight"),
                        first("photoUrl", true).alias("photoUrl")
                );

        WindowSpec byNumberOrderedByWeight = Window.partitionBy("number").orderBy(col("totalWeight").desc());

        Dataset<Row> winningNames = nameWeights
                .withColumn("rank", row_number().over(byNumberOrderedByWeight))
                .filter(col("rank").equalTo(1))
                .select(
                        col("number").alias("phoneNumber"),
                        col("savedName").alias("resolvedName"),
                        col("photoUrl")
                );

        // ---- 4. Verified business registry override.
        Dataset<Row> verifiedBusinesses = spark.read()
                .format("mongodb")
                .option("collection", "verified_businesses")
                .load()
                .select(col("_id").alias("vNumber"), col("businessName"));

        Dataset<Row> resolved = winningNames
                .join(verifiedBusinesses, winningNames.col("phoneNumber").equalTo(verifiedBusinesses.col("vNumber")), "left")
                .withColumn("businessVerified", col("vNumber").isNotNull())
                .withColumn("resolvedName", when(col("vNumber").isNotNull(), col("businessName")).otherwise(col("resolvedName")))
                .select("phoneNumber", "resolvedName", "businessVerified", "photoUrl");

        long resolvedCount = resolved.count();
        log.info("Computed {} resolved numbers in {} ms", resolvedCount, System.currentTimeMillis() - startTime);

        // ---- 5. Bulk write into DynamoDB (UpdateItem, spam fields untouched).
        resolved.foreachPartition(new com.truecaller.app.batch.DynamoDbBatchWriter(dynamoTable, awsRegion, dynamoEndpointOverride));

        log.info("Name resolution batch job complete - total runtime {} ms", System.currentTimeMillis() - startTime);
    }
}
