package com.f1betting.mapper;

import com.f1betting.dto.external.DriverExternalDTO;
import com.f1betting.dto.response.DriverMarketResponse;
import com.f1betting.dto.response.EventOutcomeResponse;
import com.f1betting.entity.Bet;
import com.f1betting.entity.Driver;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

@Service
public class EventOutcomeMapper {

    public EventOutcomeResponse.SettledBetInfo mapToSettledBetInfo (Bet bet, BigDecimal prize) {
        return new EventOutcomeResponse.SettledBetInfo(
                bet.getId(),
                bet.getUser().getId(),
                bet.getStatus().name(),
                bet.getStake(),
                bet.getOdds(),
                prize
        );
    }
}
