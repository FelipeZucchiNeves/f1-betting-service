package com.f1betting.dto.response;

import com.f1betting.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Enhanced error response structure with error codes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private int status;
    private String error;
    private String errorCode;
    private String message;
    private String path;
    private LocalDateTime timestamp;
    private List<FieldError> details;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private String message;
        private Object rejectedValue;
    }

    /**
     * Factory method to create ErrorResponse from ErrorCode.
     */
    public static ErrorResponse of(ErrorCode errorCode, String path, String customMessage) {
        return ErrorResponse.builder()
                .errorCode(errorCode.getCode())
                .message(customMessage != null ? customMessage : errorCode.getDefaultMessage())
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
