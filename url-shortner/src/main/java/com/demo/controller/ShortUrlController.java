package com.demo.controller;

import com.demo.dto.DefaultApiResponse;
import com.demo.dto.ShortUrlRequest;
import com.demo.dto.ShortUrlResponse;
import com.demo.service.ShortUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/short-url")
@RequiredArgsConstructor
public class ShortUrlController {

    private final ShortUrlService shortUrlService;

    @PostMapping("/create")
    public ResponseEntity<DefaultApiResponse> createShortUrl(@RequestBody ShortUrlRequest request) {
        ShortUrlResponse shortUrlResponse =  shortUrlService.createShortUrl(request);
        return ResponseEntity.ok(DefaultApiResponse.builder()
                .message("Short URL created successfully")
                .data(shortUrlResponse)
                .code("200")
                .build());
    }

    @GetMapping("/{shortUrl}")
    public ResponseEntity<DefaultApiResponse> getShortUrl(@PathVariable String shortUrl) {
        ShortUrlResponse shortUrlResponse = shortUrlService.getShortUrlDetails(shortUrl);
        return ResponseEntity.ok(DefaultApiResponse.builder()
                .message("Short URL fetched successfully")
                .data(shortUrlResponse)
                .code("200")
                .build());
    }

}
