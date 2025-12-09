package com.pcBuilder.backend.exception;

/**
 * Exception thrown when authentication fails (invalid credentials or inactive user).
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }
}

