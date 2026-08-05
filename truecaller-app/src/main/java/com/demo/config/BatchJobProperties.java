package com.demo.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Everything needed to shell out to `spark-submit` for the name-resolution
 * job. The job's own dependencies (Spark, Mongo Spark Connector) are
 * scope=provided and NOT on this app's classpath - so triggering it means
 * launching spark-submit as a separate OS process against the shaded
 * "spark-job" jar, not calling into it directly.
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.batch")
public class BatchJobProperties {

    /** Path to the spark-submit binary, e.g. from `pip install pyspark`. */
    private String sparkSubmitPath = "spark-submit";

    /** e.g. "local[*]" for dev, "yarn" for a real cluster. */
    private String sparkMaster = "local[*]";

    /** Path to the shaded spark-job jar produced by `mvn package`. */
    private String jarPath = "target/truecaller-app-1.0.0-spark-job.jar";

    private String mongoUri = "mongodb://localhost:27017/truecaller";
    private String dynamoTable = "ResolvedNumberIndex";
    private String awsRegion = "us-east-1";

    /** Only set for DynamoDB Local dev; leave blank for real AWS. */
    private String dynamoEndpointOverride = "http://localhost:8000";

    /** Where spark-submit's stdout/stderr get written per run. */
    private String logDirectory = "logs/batch-jobs";
}
