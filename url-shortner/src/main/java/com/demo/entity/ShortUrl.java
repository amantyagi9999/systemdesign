package com.demo.entity;

import com.demo.entity.enums.UrlStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(
        name = "short_url",
        uniqueConstraints = @UniqueConstraint(name = "uk_short_code", columnNames = "short_code")
)
public class ShortUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "short_code", nullable = false, unique = true, length = 10)
    private String shortUrl;

    @Column(name = "long_url", nullable = false, length = 2048)
    private String longUrl;

    @Column(name = "custom_alias", length = 50)
    private String customAlias;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UrlStatus status;

    /** Nullable = never expires. */
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Lightweight running total kept in sync with Redis counter; detailed events go to click_event. */
    @Column(name = "click_count", nullable = false)
    @Builder.Default
    private Long clickCount = 0L;

}
