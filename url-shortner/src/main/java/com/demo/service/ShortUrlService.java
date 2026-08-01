package com.demo.service;

import com.demo.dto.ShortUrlRequest;
import com.demo.dto.ShortUrlResponse;
import com.demo.entity.ShortUrl;
import com.demo.entity.ShortUrlAudit;
import com.demo.entity.enums.UrlStatus;
import com.demo.exception.UrlNotFoundException;
import com.demo.repository.ShortUrlAuditRepository;
import com.demo.repository.ShortUrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ShortUrlService {

    private final ShortUrlAuditRepository shortUrlAuditRepository;
    private final ShortUrlRepository shortUrlRepository;

    public ShortUrlResponse createShortUrl(ShortUrlRequest shortUrlRequest) {
        ShortUrl shortUrl = ShortUrl.builder()
                .shortUrl("sp-start")
                .longUrl(shortUrlRequest.getLongUrl())
                .createdAt(LocalDateTime.now())
                .createdBy(shortUrlRequest.getCreatedBy())
                .updatedAt(LocalDateTime.now())
                .expiryDate(shortUrlRequest.getExpiryDate())
                .customAlias(shortUrlRequest.getCustomAlias())
                .status(UrlStatus.ACTIVE)
                .build();
        shortUrl =  shortUrlRepository.save(shortUrl);

        return ShortUrlResponse.builder()
                .shortUrl("https://short.ly/" + shortUrl.getShortUrl())
                .longUrl(shortUrl.getLongUrl())
                .expiryDate(shortUrl.getExpiryDate())
                .createdAt(shortUrl.getCreatedAt())
                .build();
    }

    public ShortUrlResponse getShortUrlDetails(String shortUrl) {
        ShortUrl entity = shortUrlRepository.findByShortUrl(shortUrl)
                .orElseThrow(() -> new UrlNotFoundException("Short URL Not found" + shortUrl));

        return ShortUrlResponse.builder()
                .shortUrl("https://short.ly/" + shortUrl)
                .longUrl(entity.getLongUrl())
                .expiryDate(entity.getExpiryDate())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
