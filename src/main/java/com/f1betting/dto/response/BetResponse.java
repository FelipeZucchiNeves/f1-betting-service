package com.f1betting.dto.response;

import com.f1betting.entity.BetStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for a placed bet.
 */
public record BetResponse(
        Long id,
        Long userId,
        Long eventId,
        String eventName,
        Long driverId,
        String driverName,
        BigDecimal stake,
        BigDecimal odds,
        BigDecimal potentialWinnings,
        BetStatus status,
        LocalDateTime createdAt
) {}
