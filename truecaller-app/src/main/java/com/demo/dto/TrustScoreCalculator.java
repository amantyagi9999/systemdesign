package com.demo.dto;

import java.time.Duration;
import java.time.Instant;

/**
 * Computes a per-contributor trust score (0.1 - 2.0) used to weight their
 * vote in the whole-string-frequency name resolution algorithm.
 *
 * This is what defends the algorithm against a botnet of freshly-created
 * accounts flooding fake names for a number: a coordinated attack of 10,000
 * zero-trust accounts contributes far less total weight than 500 genuine,
 * aged, verified accounts.
 *
 * Signals used (as discussed in the design interview):
 *  - Account age: older accounts are harder/costlier to fabricate en masse
 *  - Phone verification: OTP-verified accounts get a trust floor bump
 *  - Submission velocity: accounts uploading unusually large contact
 *    batches in a short window are down-weighted (likely automated)
 */
public final class TrustScoreCalculator {

    private static final double BASE_SCORE = 1.0;
    private static final double MIN_SCORE = 0.1;
    private static final double MAX_SCORE = 2.0;

    private static final double PHONE_VERIFIED_BONUS = 0.5;
    private static final double NEW_ACCOUNT_PENALTY = 0.7; // multiplicative penalty
    private static final double HIGH_VELOCITY_PENALTY = 0.5; // multiplicative penalty

    private static final Duration NEW_ACCOUNT_THRESHOLD = Duration.ofDays(7);
    private static final int HIGH_VELOCITY_UPLOAD_THRESHOLD = 3; // full-phonebook uploads in 24h

    private TrustScoreCalculator() {
    }

    public static double compute(Instant accountCreatedAt,
                                 boolean phoneVerified,
                                 int uploadsInLast24h,
                                 Instant now) {
        double score = BASE_SCORE;

        if (phoneVerified) {
            score += PHONE_VERIFIED_BONUS;
        }

        if (accountCreatedAt != null) {
            Duration age = Duration.between(accountCreatedAt, now);
            if (age.compareTo(NEW_ACCOUNT_THRESHOLD) < 0) {
                score *= NEW_ACCOUNT_PENALTY;
            }
        } else {
            // Unknown account age - treat conservatively like a new account
            score *= NEW_ACCOUNT_PENALTY;
        }

        if (uploadsInLast24h > HIGH_VELOCITY_UPLOAD_THRESHOLD) {
            score *= HIGH_VELOCITY_PENALTY;
        }

        return Math.max(MIN_SCORE, Math.min(MAX_SCORE, score));
    }
}

