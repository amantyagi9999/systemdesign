package com.demo.config;

import com.demo.model.dynamo.ResolvedNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

@Configuration
public class DynamoDbConfig {

    @Value("${aws.dynamodb.endpoint}")
    private String endpointOverride;

    @Value("${aws.dynamodb.region}")
    private String region;

    @Value("${aws.dynamodb.tableName}")
    private String tableName;

    @Bean
    public DynamoDbClient dynamoDbClient(){
        /*return DynamoDbClient.builder()
                .region(Region.of(region)).build();*/

        DynamoDbClient.Builder builder = DynamoDbClient.builder()
                .region(Region.of(region));

        // For local dev / DynamoDB Local. In real AWS prod, omit the override
        // and rely on the default AWS endpoint + IAM role credentials.
        if (endpointOverride != null && !endpointOverride.isBlank()) {
            builder.endpointOverride(URI.create(endpointOverride));
        }
        return builder.build();
    }

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(){
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient())
                .build();
    }

    @Bean
    public DynamoDbTable<ResolvedNumber> resolvedNumberTable(DynamoDbEnhancedClient enhancedClient){
        return enhancedClient.table(tableName, TableSchema.fromBean(ResolvedNumber.class));
    }


}
