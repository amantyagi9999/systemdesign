package com.demo.repository;

import com.demo.exception.DownstreamUnavailableException;
import com.demo.model.dynamo.ResolvedNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ResolvedNumberDynamoRepository {

    private final DynamoDbTable<ResolvedNumber> resolvedNumberTable;

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
}
