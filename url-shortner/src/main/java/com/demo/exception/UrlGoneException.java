package com.demo.exception;

/**
 * Thrown when a short code DID exist but is now expired/deleted/blocked.
 * Maps to HTTP 410 Gone - distinct from 404 so support/monitoring/crawlers
 * can tell "never existed" apart from "existed, now permanently unavailable".
 */
public class UrlGoneException extends RuntimeException {
    public UrlGoneException(String shortCode, String reason) {
        super("URL for short code " + shortCode + " is no longer available: " + reason);
    }
}
