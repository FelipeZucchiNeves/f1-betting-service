package com.f1betting.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for simulating event outcome.
 */
public record EventOutcomeRequest(
        @NotNull(message = "Winner driver ID is required")
        Long winnerDriverId
) {}
