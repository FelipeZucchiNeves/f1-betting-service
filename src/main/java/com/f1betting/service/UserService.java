package com.f1betting.service;

import com.f1betting.dto.response.UserBalanceResponse;
import com.f1betting.entity.User;
import com.f1betting.exception.InsufficientBalanceException;
import com.f1betting.exception.ResourceNotFoundException;
import com.f1betting.mapper.UserMapper;
import com.f1betting.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Service for user-related operations.
 * 
 * Per requirements:
 * - User is already registered
 * - User cannot deposit or withdraw money
 * - User starts with 100 EUR given during registration
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Get user balance information.
     */
    @Transactional(readOnly = true)
    public UserBalanceResponse getBalance(Long userId) {
        log.debug("Fetching balance for user: {}", userId);
        User user = findUserById(userId);
        return userMapper.mapToUserBalanceResponse(user);
    }

    /**
     * Deduct amount from user balance (when placing a bet).
     */
    @Transactional
    public void deductBalance(Long userId, BigDecimal amount) {
        log.debug("Deducting {} from user {}", amount, userId);
        User user = findUserById(userId);
        
        if (user.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(amount, user.getBalance());
        }
        
        user.setBalance(user.getBalance().subtract(amount));
        userRepository.save(user);
        log.info("Deducted {} from user {}. New balance: {}", amount, userId, user.getBalance());
    }

    /**
     * Add amount to user balance (for winnings after event settlement).
     */
    @Transactional
    public void creditBalance(Long userId, BigDecimal amount) {
        log.debug("Crediting {} to user {}", amount, userId);
        User user = findUserById(userId);
        user.setBalance(user.getBalance().add(amount));
        userRepository.save(user);
        log.info("Credited {} to user {}. New balance: {}", amount, userId, user.getBalance());
    }

    /**
     * Find user by ID or throw exception.
     */
    @Transactional(readOnly = true)
    public User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }
}
