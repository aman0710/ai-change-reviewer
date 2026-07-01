package com.aichange.reviewer.exception;

import com.aichange.reviewer.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Global exception handler for the application.
 * Maps custom exceptions to appropriate HTTP responses.
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(InvalidUrlException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUrlException(InvalidUrlException ex) {
        ErrorResponse errorResponse = new ErrorResponse("Invalid URL", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(GitHubApiException.class)
    public ResponseEntity<ErrorResponse> handleGitHubApiException(GitHubApiException ex) {
        HttpStatus status = determineStatusFromGitHub(ex.getStatusCode());
        ErrorResponse errorResponse = new ErrorResponse("GitHub API Error", ex.getMessage());
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(AiServiceException.class)
    public ResponseEntity<ErrorResponse> handleAiServiceException(AiServiceException ex) {
        ErrorResponse errorResponse = new ErrorResponse("AI Service Error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse("Internal Server Error", "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    private HttpStatus determineStatusFromGitHub(int statusCode) {
        return switch (statusCode) {
            case 404 -> HttpStatus.NOT_FOUND;
            case 401, 403 -> HttpStatus.FORBIDDEN;
            case 429 -> HttpStatus.TOO_MANY_REQUESTS;
            default -> HttpStatus.BAD_GATEWAY;
        };
    }
}
