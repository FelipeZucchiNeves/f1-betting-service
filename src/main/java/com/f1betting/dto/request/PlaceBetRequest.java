package com.f1betting.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for placing a bet.
 */
public record PlaceBetRequest(
        @NotNull(message = "User ID is required")
        Long userId,

        @NotNull(message = "Event ID is required")
        Long eventId,

        @NotNull(message = "Driver ID is required")
        Long driverId,

        @NotNull(message = "Stake is required")
        @DecimalMin(value = "0.01", message = "Stake must be greater than 0")
        BigDecimal stake
) {}
