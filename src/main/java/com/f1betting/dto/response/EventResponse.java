package com.f1betting.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for an event with driver market.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EventResponse(
        Long id,
        Integer externalSessionKey,
        Integer year,
        String country,
        String sessionName,
        LocalDateTime startTime,
        String circuitShortName,
        List<DriverMarketResponse> driverMarket,
        Boolean driversLoaded
) {}

