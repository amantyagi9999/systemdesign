package com.demo.service;

import com.demo.dto.SearchDto;
import com.demo.exception.NumberNotFoundException;
import com.demo.model.dynamo.ResolvedNumber;
import com.demo.repository.ResolvedNumberDynamoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final ResolvedNumberDynamoRepository resolvedNumberDynamoRepository;
    private final ResolvedNumberCacheService resolvedNumberCacheService;
    public SearchDto searchPhoneNumber(String phoneNumber) {
        // try with cached first
        Optional<ResolvedNumber> cachedResolvedNumber = resolvedNumberCacheService.get(phoneNumber);
        if(cachedResolvedNumber.isPresent()){
            return toResponse(cachedResolvedNumber.get());
        }

        // Cache miss usecase - acquire stampede-protection lock before hitting DynamoDB
        Optional<String> lockToken = resolvedNumberCacheService.getWithLock(phoneNumber);
        if(lockToken.isPresent()){
            try{
                ResolvedNumber resolvedNumber = getResolvedNumberFromDynamoDB(phoneNumber);

                resolvedNumberCacheService.put(phoneNumber, resolvedNumber);
                return toResponse(resolvedNumber);
            } finally {
                resolvedNumberCacheService.releaseLock(phoneNumber, lockToken.get());
            }
        }

        return waitForCacheFill(phoneNumber);
    }

    private SearchDto waitForCacheFill(String phoneNumber) {
        int retries = resolvedNumberCacheService.getLockWaitMaxRetries();
        long retryInterval = resolvedNumberCacheService.getLockWaitRetryMillis();
        for(int i = 0; i< retries ; i++){
            try{
                Thread.sleep(retryInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            Optional<ResolvedNumber> resolvedNumber = resolvedNumberCacheService.get(phoneNumber);
            if(resolvedNumber.isPresent()){
                return toResponse(resolvedNumber.get());
            }
        }

        // Fallback: the lock-holder is taking too long (or crashed) - go
        // straight to DynamoDB rather than block the client indefinitely.
        log.warn("Cache fill wait exhausted for number={}, falling back to direct DB read", phoneNumber);
        ResolvedNumber resolved = getResolvedNumberFromDynamoDB(phoneNumber);
        return toResponse(resolved);

    }

    public ResolvedNumber getResolvedNumberFromDynamoDB(String phoneNumber) {
        return resolvedNumberDynamoRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new NumberNotFoundException(phoneNumber));
    }

    private SearchDto toResponse(ResolvedNumber resolved) {
        return SearchDto.builder()
                .phoneNumber(resolved.getPhoneNumber())
                .displayName(resolved.getResolvedName())
                .category(resolved.getCategory())
                .photoUrl(resolved.getPhotoUrl())
                .businessVerified(Boolean.TRUE.equals(resolved.getBusinessVerified()))
                .build();
    }
}
