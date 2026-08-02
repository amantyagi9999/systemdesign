package com.demo.exception;

/** Thrown when a short code was never registered. Maps to HTTP 404. */
public class UrlNotFoundException extends RuntimeException {
    public UrlNotFoundException(String shortCode) {
        super("No URL found for short code: " + shortCode);
    }
}
