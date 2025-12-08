package com.pcBuilder.backend.exception;

/**
 * Exception thrown when an invalid component category is provided.
 */
public class InvalidCategoryException extends RuntimeException {

    public InvalidCategoryException(String category) {
        super(String.format("Invalid component category: %s", category));
    }

    public InvalidCategoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
