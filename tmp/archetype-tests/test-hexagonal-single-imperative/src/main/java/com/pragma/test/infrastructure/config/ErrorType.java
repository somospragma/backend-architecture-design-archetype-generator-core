package com.pragma.test.infrastructure.config;

/**
 * Standard error types for the application.
 * Used by exception handlers to categorize errors consistently
 * across all API responses.
 */
public enum ErrorType {

    VALIDATION_ERROR("Validation Error", "The request contains invalid data"),
    NOT_FOUND("Not Found", "The requested resource was not found"),
    INTERNAL_ERROR("Internal Server Error", "An unexpected error occurred"),
    UNAUTHORIZED("Unauthorized", "Authentication is required to access this resource"),
    FORBIDDEN("Forbidden", "You do not have permission to access this resource"),
    CONFLICT("Conflict", "The request conflicts with the current state of the resource"),
    BAD_REQUEST("Bad Request", "The request could not be understood or was missing required parameters");

    private final String title;
    private final String detail;

    ErrorType(String title, String detail) {
        this.title = title;
        this.detail = detail;
    }

    public String getTitle() {
        return title;
    }

    public String getDetail() {
        return detail;
    }
}
