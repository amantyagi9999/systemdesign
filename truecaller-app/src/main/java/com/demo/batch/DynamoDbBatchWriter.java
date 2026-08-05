package com.truecaller.app.batch;

import org.apache.spark.api.java.function.ForeachPartitionFunction;
import org.apache.spark.sql.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValueUpdate;
import software.amazon.awssdk.services.dynamodb.model.AttributeAction;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Writes resolved (phoneNumber -> name) rows into the DynamoDB index.
 *
 * IMPORTANT: uses UpdateItem, not PutItem. PutItem would overwrite the
 * entire item, including spamLabel/spamScore fields that are owned and
 * actively maintained by the near-real-time Kafka spam-report pipeline
 * (SpamReportConsumer in the serving app). This job only owns
 * resolvedName, businessVerified, photoUrl, and lastUpdated - so it must
 * only touch those attributes and leave spam fields untouched.
 *
 * Runs as a Spark foreachPartition sink: one DynamoDB client is created
 * per partition (not per row) since the SDK client isn't serializable and
 * creating one per row would be far too expensive at scale.
 */
public class DynamoDbBatchWriter implements ForeachPartitionFunction<Row> {

    private static final Logger log = LoggerFactory.getLogger(DynamoDbBatchWriter.class);

    private final String tableName;
    private final String region;
    private final String endpointOverride; // null in real AWS prod; set for DynamoDB Local dev

    public DynamoDbBatchWriter(String tableName, String region, String endpointOverride) {
        this.tableName = tableName;
        this.region = region;
        this.endpointOverride = endpointOverride;
    }

    @Override
    public void call(Iterator<Row> partition) {
        DynamoDbClient dbClient = DynamoDbClient.builder()
                .endpointOverride(URI.create(endpointOverride))
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create("dummy", "dummy")
                        )
                ).build();

            int written = 0;
            int failed = 0;

            while (partition.hasNext()) {
                Row row = partition.next();
                try {
                    writeOne(dbClient, row);
                    written++;
                } catch (Exception ex) {
                    // Don't fail the whole partition/job for one bad record -
                    // log and continue so a single malformed row doesn't
                    // block resolution for millions of other numbers.
                    failed++;
                    log.error("Failed to write resolved number for row={}", row, ex);
                }
            }
            log.info("Partition complete - written={} failed={}", written, failed);

    }

    private void writeOne(DynamoDbClient client, Row row) {
        String phoneNumber = row.getAs("phoneNumber");
        String resolvedName = row.getAs("resolvedName");
        boolean businessVerified = row.getAs("businessVerified") != null && row.<Boolean>getAs("businessVerified");
        String photoUrl = hasField(row, "photoUrl") ? row.getAs("photoUrl") : null;

        Map<String, AttributeValue> key = new HashMap<>();
        key.put("phoneNumber", AttributeValue.builder().s(phoneNumber).build());

        Map<String, AttributeValueUpdate> updates = new HashMap<>();
        updates.put("resolvedName", AttributeValueUpdate.builder()
                .value(AttributeValue.builder().s(resolvedName).build())
                .action(AttributeAction.PUT)
                .build());
        updates.put("businessVerified", AttributeValueUpdate.builder()
                .value(AttributeValue.builder().bool(businessVerified).build())
                .action(AttributeAction.PUT)
                .build());
        updates.put("lastUpdated", AttributeValueUpdate.builder()
                .value(AttributeValue.builder().s(Instant.now().toString()).build())
                .action(AttributeAction.PUT)
                .build());
        if (photoUrl != null) {
            updates.put("photoUrl", AttributeValueUpdate.builder()
                    .value(AttributeValue.builder().s(photoUrl).build())
                    .action(AttributeAction.PUT)
                    .build());
        }
        // NOTE: spamLabel / spamScore are intentionally NOT touched here -
        // they belong to SpamReportConsumer in the serving application.

        client.updateItem(UpdateItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .attributeUpdates(updates)
                .build());
    }

    private boolean hasField(Row row, String name) {
        try {
            row.fieldIndex(name);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
