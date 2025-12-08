package com.pcBuilder.backend.exception;

import java.util.List;

/**
 * Exception thrown when build validation fails.
 */
public class BuildValidationException extends RuntimeException {

    private final List<String> validationErrors;

    public BuildValidationException(String message) {
        super(message);
        this.validationErrors = List.of(message);
    }

    public BuildValidationException(String message, List<String> validationErrors) {
        super(message);
        this.validationErrors = validationErrors;
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }
}
