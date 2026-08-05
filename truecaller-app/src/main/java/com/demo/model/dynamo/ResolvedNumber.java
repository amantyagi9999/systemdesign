package com.demo.model.dynamo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class ResolvedNumber {

    private String phoneNumber;       // Partition key, E.164 format
    private String resolvedName;      // Winning name from weighted-frequency algorithm
    private String category;         // NONE / SPAM / SCAM / TELEMARKETER
    private Double spamScore;         // Confidence score 0.0 - 1.0, decays over time
    private Boolean businessVerified; // True if matched against verified business registry
    private String photoUrl;
    private Instant lastUpdated;

    @DynamoDbPartitionKey
    public String getPhoneNumber() {
        return phoneNumber;
    }
}
