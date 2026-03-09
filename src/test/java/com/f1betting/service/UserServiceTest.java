package com.f1betting.service;

import com.f1betting.dto.response.UserBalanceResponse;
import com.f1betting.entity.User;
import com.f1betting.exception.InsufficientBalanceException;
import com.f1betting.exception.ResourceNotFoundException;
import com.f1betting.mapper.UserMapper;
import com.f1betting.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Spy
    private UserMapper userMapper= new UserMapper();

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .balance(new BigDecimal("100.00"))
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should get user balance successfully")
    void getBalance_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserBalanceResponse response = userService.getBalance(1L);

        assertThat(response).isNotNull();
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.userName()).isEqualTo("Test User");
        assertThat(response.balance()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(response.currency()).isEqualTo("EUR");
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void getBalance_UserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getBalance(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("Should deduct balance successfully")
    void deductBalance_Success() {
        BigDecimal deductAmount = new BigDecimal("25.00");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.deductBalance(1L, deductAmount);

        assertThat(testUser.getBalance()).isEqualByComparingTo(new BigDecimal("75.00"));
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should throw exception when insufficient balance")
    void deductBalance_InsufficientBalance() {
        BigDecimal deductAmount = new BigDecimal("150.00");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> userService.deductBalance(1L, deductAmount))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining("Insufficient balance");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should credit balance successfully (for winnings)")
    void creditBalance_Success() {
        BigDecimal creditAmount = new BigDecimal("50.00");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.creditBalance(1L, creditAmount);

        assertThat(testUser.getBalance()).isEqualByComparingTo(new BigDecimal("150.00"));
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should find user by ID")
    void findUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User result = userService.findUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test User");
    }

    @Test
    @DisplayName("Should throw exception when user not found by ID")
    void findUserById_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }
}
