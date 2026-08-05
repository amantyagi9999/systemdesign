package com.demo.model.mongo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Per-user trust signals used to weight their votes in the name-resolution
 * algorithm (see NameResolutionService / TrustScoreCalculator). In a real
 * system this would be maintained by the auth/account service; here it's
 * read-only input to the resolution job.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class UserDocument {

    @Id
    private String userId;

    private Instant accountCreatedAt;
    private boolean phoneVerified;
    private int uploadsInLast24h;
}
