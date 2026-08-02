package com.demo.exception;

/** Thrown by the rate limiting filter when a client exceeds its token bucket. Maps to HTTP 429. */
public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String message) {
        super(message);
    }
}
