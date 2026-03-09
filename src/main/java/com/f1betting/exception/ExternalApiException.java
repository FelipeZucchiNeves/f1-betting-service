package com.f1betting.exception;

import lombok.Getter;

/**
 * Exception thrown when there's an issue with external API communication.
 */
@Getter
public class ExternalApiException extends RuntimeException {
    
    private final ErrorCode errorCode;

    public ExternalApiException(String message) {
        super(message);
        this.errorCode = ErrorCode.EXTERNAL_API_ERROR;
    }

    public ExternalApiException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.EXTERNAL_API_ERROR;
    }

    public ExternalApiException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ExternalApiException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
