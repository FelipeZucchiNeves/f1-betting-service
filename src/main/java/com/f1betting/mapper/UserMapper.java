package com.f1betting.mapper;

import com.f1betting.dto.response.EventOutcomeResponse;
import com.f1betting.dto.response.UserBalanceResponse;
import com.f1betting.entity.Bet;
import com.f1betting.entity.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class UserMapper {

    private static final String CURRENCY = "EUR";


    public UserBalanceResponse mapToUserBalanceResponse (User user) {
        return new UserBalanceResponse(
                user.getId(),
                user.getName(),
                user.getBalance(),
                CURRENCY
        );
    }
}
