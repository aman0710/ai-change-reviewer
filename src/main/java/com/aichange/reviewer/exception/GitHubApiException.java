package com.aichange.reviewer.exception;

/**
 * Exception thrown when GitHub API call fails.
 */
public class GitHubApiException extends RuntimeException {
    private final int statusCode;

    public GitHubApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public GitHubApiException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
