package com.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShortUrlResponse {

    private String shortUrl;
    private String alias;
    private String longUrl;
    private String createdBy;
    private LocalDateTime expiryDate;
    private LocalDateTime createdAt;

}
