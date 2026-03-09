package com.f1betting.util;

import java.math.BigDecimal;
import java.util.Random;

public class OddGeneration {

    private static final BigDecimal[] POSSIBLE_ODDS = {
            new BigDecimal("2.00"),
            new BigDecimal("3.00"),
            new BigDecimal("4.00")
    };
    private static final Random RANDOM = new Random();

    public static BigDecimal generateRandomOdds() {
        return POSSIBLE_ODDS[RANDOM.nextInt(POSSIBLE_ODDS.length)];
    }
}
