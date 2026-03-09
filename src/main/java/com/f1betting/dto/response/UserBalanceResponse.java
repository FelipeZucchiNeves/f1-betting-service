package com.f1betting.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for user balance.
 */
public record UserBalanceResponse(
        Long userId,
        String userName,
        BigDecimal balance,
        String currency
) {}
