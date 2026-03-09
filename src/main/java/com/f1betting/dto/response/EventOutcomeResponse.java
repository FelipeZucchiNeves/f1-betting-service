package com.f1betting.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for event outcome simulation.
 */
public record EventOutcomeResponse(
        Long eventId,
        String eventName,
        Long winnerDriverId,
        String winnerDriverName,
        int totalBetsSettled,
        int winningBets,
        int losingBets,
        BigDecimal totalPrizesPaid,
        List<SettledBetInfo> settledBets
) {

    public record SettledBetInfo(
            Long betId,
            Long userId,
            String status,
            BigDecimal stake,
            BigDecimal odds,
            BigDecimal prize
    ) {}
}
