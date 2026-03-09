package com.f1betting.config;

import com.f1betting.entity.User;
import com.f1betting.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Initializes sample data on application startup.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Creating sample user...");
            User user = User.builder()
                    .name("Demo User")
                    .balance(new BigDecimal("100.00"))
                    .build();
            userRepository.save(user);
            log.info("Sample user created with ID: {} and balance: {} EUR", user.getId(), user.getBalance());
        }
    }
}
