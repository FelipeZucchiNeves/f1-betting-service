package com.f1betting.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for a driver in the betting market with odds.
 */
public record DriverMarketResponse(
        Long driverId,
        Integer externalDriverId,
        String fullName,
        Integer driverNumber,
        String teamName,
        BigDecimal odds
) {}
