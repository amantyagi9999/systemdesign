package com.demo.service;

import com.demo.dto.ShortUrlRequest;
import com.demo.dto.ShortUrlResponse;
import com.demo.entity.ShortUrl;
import com.demo.entity.ShortUrlAudit;
import com.demo.entity.enums.UrlStatus;
import com.demo.exception.AliasConflictException;
import com.demo.exception.UrlGoneException;
import com.demo.exception.UrlNotFoundException;
import com.demo.repository.ShortUrlAuditRepository;
import com.demo.repository.ShortUrlRepository;
import com.demo.util.Base62IdEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ShortUrlService {

    private static final String CACHE_PREFIX = "short_url:";
    private static final long CACHE_TTL_HOURS = 24;
    private final ShortUrlAuditRepository shortUrlAuditRepository;
    private final ShortUrlRepository shortUrlRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final IdRangeAllocatorService idRangeAllocatorService;

    public ShortUrlResponse createShortUrl(ShortUrlRequest shortUrlRequest) {
        long nextId = idRangeAllocatorService.getNextId();
        String shortCode = (shortUrlRequest.getCustomAlias() != null && !shortUrlRequest.getCustomAlias().isBlank())
            ? shortUrlRequest.getCustomAlias()
            : Base62IdEncoder.encode(nextId);
        ShortUrl shortUrl = ShortUrl.builder()
                .shortCode(shortCode)
                .longUrl(shortUrlRequest.getLongUrl())
                .createdAt(LocalDateTime.now())
                .createdBy(shortUrlRequest.getCreatedBy())
                .updatedAt(LocalDateTime.now())
                .expiryDate(shortUrlRequest.getExpiryDate())
                .customAlias(shortUrlRequest.getCustomAlias())
                .status(UrlStatus.ACTIVE)
                .clickCount(0L)
                .build();
        try{
            shortUrlRepository.save(shortUrl);
        }
        catch (Exception e){
            throw new AliasConflictException(shortCode);
        }

        writeAudit(shortUrl.getId(), "CREATED", shortUrlRequest.getCreatedBy());
        cachePut(shortCode, shortUrl.getLongUrl());

        return ShortUrlResponse.builder()
                .shortUrl("https://short.ly/" + shortUrl.getShortCode())
                .longUrl(shortUrl.getLongUrl())
                .alias(shortUrl.getCustomAlias())
                .expiryDate(shortUrl.getExpiryDate())
                .createdAt(shortUrl.getCreatedAt())
                .createdBy(shortUrl.getCreatedBy())
                .build();
    }

    public String getLongUrlForRedirect(String shortCode){
        Object cachedLongUrl = redisTemplate.opsForValue().get(CACHE_PREFIX + shortCode);
        if (cachedLongUrl != null) {
            return (String) cachedLongUrl;
        }
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode).orElseThrow(() -> new UrlNotFoundException(shortCode));
        validateUsable(shortUrl, shortCode);
        cachePut(shortCode, shortUrl.getLongUrl());
        return shortUrl.getLongUrl();
    }


    public ShortUrlResponse getShortUrlDetails(String shortCode) {
        ShortUrl entity = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("Short URL Not found" + shortCode));

        validateUsable(entity, shortCode);

        return ShortUrlResponse.builder()
                .shortUrl("https://short.ly/" + shortCode)
                .longUrl(entity.getLongUrl())
                .expiryDate(entity.getExpiryDate())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private void writeAudit(Long shortUrlId, String action, String modifiedBy) {
        shortUrlAuditRepository.save(ShortUrlAudit.builder()
                .shortUrlId(shortUrlId)
                .action(action)
                .modifiedBy(modifiedBy)
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private void cachePut(String shortCode, String longUrl) {
        redisTemplate.opsForValue().set(CACHE_PREFIX + shortCode, longUrl);

        redisTemplate.opsForValue().set(CACHE_PREFIX + shortCode, longUrl, CACHE_TTL_HOURS, TimeUnit.HOURS);
    }

    private void validateUsable(ShortUrl shortUrl, String shortCode) {
        if (shortUrl.getStatus() == UrlStatus.DELETED) {
            throw new UrlGoneException(shortCode, "deleted");
        }
        if (shortUrl.getStatus() == UrlStatus.BLOCKED) {
            throw new UrlGoneException(shortCode, "blocked");
        }
        if (shortUrl.getStatus() == UrlStatus.EXPIRED || shortUrl.isExpired()) {
            markExpiredIfNeeded(shortUrl);
            throw new UrlGoneException(shortCode, "expired");
        }
    }

    //@Transactional
    protected void markExpiredIfNeeded(ShortUrl entity) {
        if (entity.getStatus() != UrlStatus.EXPIRED) {
            entity.setStatus(UrlStatus.EXPIRED);
            entity.setUpdatedAt(LocalDateTime.now());
            shortUrlRepository.save(entity);
            writeAudit(entity.getId(), "EXPIRED", null);
        }
        redisTemplate.delete(CACHE_PREFIX + entity.getShortCode());
    }

}
