package com.aichange.reviewer.exception;

/**
 * Exception thrown when the provided GitHub URL is invalid or unsupported.
 */
public class InvalidUrlException extends RuntimeException {
    public InvalidUrlException(String message) {
        super(message);
    }

    public InvalidUrlException(String message, Throwable cause) {
        super(message, cause);
    }
}
