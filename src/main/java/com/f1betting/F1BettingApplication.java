package com.f1betting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application entry point for F1 Betting Service.
 */
@SpringBootApplication
@EnableFeignClients
@EnableScheduling
public class F1BettingApplication {

    public static void main(String[] args) {
        SpringApplication.run(F1BettingApplication.class, args);
    }
}
