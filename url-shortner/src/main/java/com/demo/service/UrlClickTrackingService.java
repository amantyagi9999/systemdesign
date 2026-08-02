package com.demo.service;

import com.demo.entity.ClickEvent;
import com.demo.repository.ClickEventRepository;
import com.demo.repository.ShortUrlRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlClickTrackingService {

    private static final String CLICK_COUNTER_PREFIX = "click_count:";

    private final ClickEventRepository clickEventRepository;
    private final ShortUrlRepository shortUrlRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public void incrementClickCounter(String shortCode){
        try{
            redisTemplate.opsForValue().increment(CLICK_COUNTER_PREFIX + shortCode);
            redisTemplate.expire(CLICK_COUNTER_PREFIX + shortCode, 30, TimeUnit.HOURS);
        }
        catch (Exception e) {
            // Never let analytics failures affect the redirect - just log.
            log.warn("Failed to increment click counter for {}: {}", shortCode, e.getMessage());
        }
    }

    @Async("clickEventExecutor")
    public void trackDetailsEvent(String shortCode, HttpServletRequest request) {
        ClickEvent clickEvent = ClickEvent.builder()
                .shortCode(shortCode)
                .clickedAt(LocalDateTime.now())
                .referrer(request.getHeader("Referer"))
                .userAgent(request.getHeader("User-Agent"))
                .ipAddress(request.getLocalAddr())
                .build();

        clickEventRepository.save(clickEvent);
    }

    @Async("clickEventExecutor")
    public void flushCounterToDb(String shortCode){

        Object count = redisTemplate.opsForValue().get(CLICK_COUNTER_PREFIX + shortCode);
        if(count == null)
            return;

        shortUrlRepository.findByShortCode(shortCode).ifPresent(shortUrl -> {
            shortUrl.setClickCount((Long) count);
            shortUrlRepository.save(shortUrl);
        });
    }



}
