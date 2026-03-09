package com.f1betting.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enumeration of error codes for consistent error identification across the API.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Resource not found errors (1xxx)
    USER_NOT_FOUND("1001", "User not found"),
    EVENT_NOT_FOUND("1002", "Event not found"),
    DRIVER_NOT_FOUND("1003", "Driver not found"),
    RESOURCE_NOT_FOUND("1004", "Resource not found"),

    // Business logic errors (2xxx)
    INSUFFICIENT_BALANCE("2001", "Insufficient balance for this operation"),
    INVALID_STAKE("2002", "Invalid stake amount"),
    BET_ALREADY_SETTLED("2003", "Bet has already been settled"),
    EVENT_NOT_AVAILABLE("2004", "Event is not available for betting"),

    // External API errors (3xxx)
    EXTERNAL_API_ERROR("3001", "External API is unavailable"),
    EXTERNAL_API_TIMEOUT("3002", "External API request timed out"),
    CIRCUIT_BREAKER_OPEN("3003", "Service temporarily unavailable due to circuit breaker"),

    // Validation errors (4xxx)
    VALIDATION_ERROR("4001", "Request validation failed"),
    INVALID_REQUEST("4002", "Invalid request format"),

    // System errors (5xxx)
    INTERNAL_ERROR("5001", "An unexpected error occurred"),
    DATABASE_ERROR("5002", "Database operation failed");

    private final String code;
    private final String defaultMessage;
}
