package com.demo.exception;

import org.springframework.http.HttpStatus;

public class RateLimitExceededException extends ApiException {

    public RateLimitExceededException() {
        super("RATE_LIMIT_EXCEEDED",
              "Too many requests - please slow down and try again shortly",
              HttpStatus.TOO_MANY_REQUESTS);
    }
}
