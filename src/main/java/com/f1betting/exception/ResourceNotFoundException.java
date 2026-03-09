package com.f1betting.exception;

import lombok.Getter;

/**
 * Exception thrown when a requested resource cannot be found.
 */
@Getter
public class ResourceNotFoundException extends RuntimeException {
    
    private final ErrorCode errorCode;
    private final String resourceType;
    private final Object resourceId;

    public ResourceNotFoundException(String message) {
        super(message);
        this.errorCode = ErrorCode.RESOURCE_NOT_FOUND;
        this.resourceType = null;
        this.resourceId = null;
    }

    public ResourceNotFoundException(String resourceType, Object resourceId) {
        super(String.format("%s not found with id: %s", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.errorCode = determineErrorCode(resourceType);
    }

    public ResourceNotFoundException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.resourceType = null;
        this.resourceId = null;
    }

    private ErrorCode determineErrorCode(String resourceType) {
        if (resourceType == null) return ErrorCode.RESOURCE_NOT_FOUND;
        return switch (resourceType.toLowerCase()) {
            case "user" -> ErrorCode.USER_NOT_FOUND;
            case "event" -> ErrorCode.EVENT_NOT_FOUND;
            case "driver" -> ErrorCode.DRIVER_NOT_FOUND;
            default -> ErrorCode.RESOURCE_NOT_FOUND;
        };
    }
}
