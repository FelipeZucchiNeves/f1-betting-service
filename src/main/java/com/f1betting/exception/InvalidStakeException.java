package com.f1betting.exception;

import lombok.Getter;

import java.math.BigDecimal;

/**
 * Exception thrown when an invalid stake amount is provided.
 */
@Getter
public class InvalidStakeException extends RuntimeException {
    
    private final ErrorCode errorCode = ErrorCode.INVALID_STAKE;
    private final BigDecimal stake;

    public InvalidStakeException(String message) {
        super(message);
        this.stake = null;
    }

    public InvalidStakeException(BigDecimal stake, String reason) {
        super(String.format("Invalid stake amount %.2f: %s", stake, reason));
        this.stake = stake;
    }
}
