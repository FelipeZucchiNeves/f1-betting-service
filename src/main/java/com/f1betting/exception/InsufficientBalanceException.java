package com.f1betting.exception;

import lombok.Getter;

import java.math.BigDecimal;

/**
 * Exception thrown when a user has insufficient balance for an operation.
 */
@Getter
public class InsufficientBalanceException extends RuntimeException {
    
    private final ErrorCode errorCode = ErrorCode.INSUFFICIENT_BALANCE;
    private final BigDecimal required;
    private final BigDecimal available;

    public InsufficientBalanceException(String message) {
        super(message);
        this.required = null;
        this.available = null;
    }

    public InsufficientBalanceException(BigDecimal required, BigDecimal available) {
        super(String.format("Insufficient balance. Required: %.2f EUR, Available: %.2f EUR", 
                required, available));
        this.required = required;
        this.available = available;
    }
}
