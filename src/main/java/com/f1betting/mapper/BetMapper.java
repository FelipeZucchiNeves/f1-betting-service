package com.f1betting.mapper;

import com.f1betting.dto.response.BetResponse;
import com.f1betting.entity.Bet;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class BetMapper {

    public BetResponse mapToBetResponse (Bet bet, BigDecimal potentialWinnings) {
        return new BetResponse(
                bet.getId(),
                bet.getUser().getId(),
                bet.getEvent().getId(),
                bet.getEvent().getCountry() + " - " + bet.getEvent().getSessionName(),
                bet.getDriver().getId(),
                bet.getDriver().getFullName(),
                bet.getStake(),
                bet.getOdds(),
                potentialWinnings,
                bet.getStatus(),
                bet.getCreatedAt()
        );
    }
}
