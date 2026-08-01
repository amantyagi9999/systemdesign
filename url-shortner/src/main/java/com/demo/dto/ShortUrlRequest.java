package com.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShortUrlRequest {

    @NotBlank(message = "longUrl is required")
    @Pattern(regexp = "^https?://.+", message = "longUrl must start with http:// or https://")
    @Size(max = 2048)
    private String longUrl;

    @Size(max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9_-]*$", message = "customAlias may only contain letters, digits, - and _")
    private String customAlias;

    /** Optional. Null = never expires. */
    private LocalDateTime expiryDate;

    private String createdBy;
}
