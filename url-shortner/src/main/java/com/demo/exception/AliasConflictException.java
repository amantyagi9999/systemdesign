package com.demo.exception;

/**
 * Thrown when a custom alias is already taken. Relies on the DB unique
 * constraint as the source of truth (safe under concurrent requests -
 * see the TOCTOU discussion), NOT a check-then-insert. Maps to HTTP 409.
 */
public class AliasConflictException extends RuntimeException {
    public AliasConflictException(String alias) {
        super("Custom alias already in use: " + alias);
    }
}
