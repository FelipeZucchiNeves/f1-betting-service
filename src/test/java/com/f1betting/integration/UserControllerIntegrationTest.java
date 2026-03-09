package com.f1betting.integration;

import com.f1betting.entity.User;
import com.f1betting.repository.BetRepository;
import com.f1betting.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BetRepository betRepository;

    @BeforeEach
    void setUp() {
        betRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /api/users/{userId}/balance - Should return user balance")
    void getBalance_Success() throws Exception {
        User user = userRepository.save(User.builder()
                .name("Balance Test User")
                .balance(new BigDecimal("50.00"))
                .build());

        mockMvc.perform(get("/api/users/{userId}/balance", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(user.getId().intValue())))
                .andExpect(jsonPath("$.userName", is("Balance Test User")))
                .andExpect(jsonPath("$.balance", is(50.0)))
                .andExpect(jsonPath("$.currency", is("EUR")));
    }

    @Test
    @DisplayName("GET /api/users/{userId}/balance - Should return 404 for non-existent user")
    void getBalance_UserNotFound() throws Exception {
        mockMvc.perform(get("/api/users/{userId}/balance", 9999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode", is("1001")))
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    @DisplayName("GET /api/users/{userId}/balance - User with default 100 EUR balance")
    void getBalance_DefaultBalance() throws Exception {
        User user = userRepository.save(User.builder()
                .name("Demo User")
                .balance(new BigDecimal("100.00"))
                .build());

        mockMvc.perform(get("/api/users/{userId}/balance", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance", is(100.0)))
                .andExpect(jsonPath("$.currency", is("EUR")));
    }
}
