package com.demo.repository;

import com.demo.exception.DownstreamUnavailableException;
import com.demo.model.dynamo.ResolvedNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeAction;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValueUpdate;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ResolvedNumberDynamoRepository {

    private final DynamoDbTable<ResolvedNumber> resolvedNumberTable;

    private final DynamoDbClient lowLevelClient;

    public Optional<ResolvedNumber> findByPhoneNumber(String phoneNumber){
        try{
            ResolvedNumber item = resolvedNumberTable.getItem(Key.builder().partitionValue(phoneNumber).build());
            return Optional.ofNullable(item);
        }
        catch (SdkException ex){
            log.error("DynamoDB read failed for number={}", phoneNumber, ex);
            throw new DownstreamUnavailableException("DynamoDB", ex);
        }

    }

    public void save(ResolvedNumber resolvedNumber) {
        try {
            resolvedNumberTable.putItem(resolvedNumber);
        } catch (SdkException ex) {
            log.error("DynamoDB write failed for number={}", resolvedNumber.getPhoneNumber(), ex);
            throw new DownstreamUnavailableException("DynamoDB", ex);
        }
    }


    /**
     * Partial update used by NameResolutionService: touches ONLY
     * resolvedName / businessVerified / photoUrl / lastUpdated. Never
     * touches spamLabel/spamScore, which are owned exclusively by
     * SpamReportConsumer's near-real-time Kafka path - a full PutItem here
     * would silently wipe out live spam labels every time the resolution
     * job runs.
     */
    public void updateNameFields(String phoneNumber, String resolvedName, boolean businessVerified, String photoUrl) {
        try {
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

            lowLevelClient.updateItem(UpdateItemRequest.builder()
                    .tableName(resolvedNumberTable.tableName())
                    .key(key)
                    .attributeUpdates(updates)
                    .build());
        } catch (SdkException ex) {
            log.error("DynamoDB partial update failed for number={}", phoneNumber, ex);
            throw new DownstreamUnavailableException("DynamoDB", ex);
        }
    }
}
