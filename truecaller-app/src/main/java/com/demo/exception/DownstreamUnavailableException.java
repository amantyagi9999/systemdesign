package com.demo.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a downstream dependency (DynamoDB, MongoDB, Redis, Kafka)
 * is unreachable or times out. Mapped to 503 so clients/load balancers
 * can distinguish "system is unhealthy" from "bad request" (4xx).
 */
public class DownstreamUnavailableException extends ApiException {

    public DownstreamUnavailableException(String dependency, Throwable cause) {
        super("DOWNSTREAM_UNAVAILABLE",
              dependency + " is temporarily unavailable - please retry",
              HttpStatus.SERVICE_UNAVAILABLE);
        initCause(cause);
    }
}
