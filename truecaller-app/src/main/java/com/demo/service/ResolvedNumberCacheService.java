package com.demo.service;

import com.demo.model.dynamo.ResolvedNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResolvedNumberCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String CACHE_KEY_PREFIX = "resolved:";
    private static final String LOCK_KEY_PREFIX = "lock:resolved:";

    @Value("${app.cache.ttl-seconds}")
    private long ttlSeconds;

    @Value("${app.cache.lock-ttl-millis}")
    private long lockTtlMillis;

    @Value("${app.cache.lock-wait-retry-millis}")
    private long lockWaitRetryMillis;

    @Value("${app.cache.lock-wait-max-retries}")
    private int lockWaitMaxRetries;

    public Optional<ResolvedNumber> get(String phoneNumber){
        return Optional.ofNullable((ResolvedNumber) redisTemplate.opsForValue().get(CACHE_KEY_PREFIX + phoneNumber));
    }

    public void put(String phoneNumber, ResolvedNumber resolvedNumber){
        redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + phoneNumber, resolvedNumber, ttlSeconds, TimeUnit.SECONDS);
    }

    public void evict(String phoneNumber){
        redisTemplate.delete(CACHE_KEY_PREFIX + phoneNumber);
    }

    public Optional<String> getWithLock(String phoneNumber){
        String lockKey = LOCK_KEY_PREFIX + phoneNumber;
        String token = UUID.randomUUID().toString();
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, token, lockTtlMillis, TimeUnit.MILLISECONDS);
        return Boolean.TRUE.equals(acquired) ? Optional.of(token) : Optional.empty();
    }

    public void releaseLock(String phoneNumber, String token){
        String lockKey = LOCK_KEY_PREFIX + phoneNumber;
        Object current = redisTemplate.opsForValue().get(lockKey);

        if(token != null && token.equals(current)){
            redisTemplate.delete(lockKey);
        }
    }

    public long getLockWaitRetryMillis() {
        return lockWaitRetryMillis;
    }

    public int getLockWaitMaxRetries() {
        return lockWaitMaxRetries;
    }
}
