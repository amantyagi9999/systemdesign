package com.demo.controller;

import com.demo.dto.DefaultApiResponse;
import com.demo.dto.ShortUrlRequest;
import com.demo.dto.ShortUrlResponse;
import com.demo.service.ShortUrlService;
import com.demo.service.UrlClickTrackingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/short-url")
@RequiredArgsConstructor
public class ShortUrlController {

    private final ShortUrlService shortUrlService;
    private final UrlClickTrackingService urlClickTrackingService;

    @PostMapping("/create")
    public ResponseEntity<DefaultApiResponse> createShortUrl(@RequestBody ShortUrlRequest request) {
        ShortUrlResponse shortUrlResponse =  shortUrlService.createShortUrl(request);
        return ResponseEntity.ok(DefaultApiResponse.builder()
                .message("Short URL created successfully")
                .data(shortUrlResponse)
                .code(200)
                .build());
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<DefaultApiResponse> getShortUrl(@PathVariable String shortCode) {
        ShortUrlResponse shortUrlResponse = shortUrlService.getShortUrlDetails(shortCode);
        return ResponseEntity.ok(DefaultApiResponse.builder()
                .message("Short URL fetched successfully")
                .data(shortUrlResponse)
                .code(200)
                .build());
    }

    @GetMapping("/redirect/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode, HttpServletRequest request) {
        String longUrl = shortUrlService.getLongUrlForRedirect(shortCode);

        // Fire-and-forget: neither call blocks the redirect response.
        urlClickTrackingService.incrementClickCounter(shortCode);
        urlClickTrackingService.trackDetailsEvent(shortCode, request);

        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(longUrl)).build();

    }

}
