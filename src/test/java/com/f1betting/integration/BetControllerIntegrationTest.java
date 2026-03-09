package com.f1betting.integration;

import com.f1betting.dto.request.PlaceBetRequest;
import com.f1betting.entity.*;
import com.f1betting.repository.BetRepository;
import com.f1betting.repository.DriverRepository;
import com.f1betting.repository.EventRepository;
import com.f1betting.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BetControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private BetRepository betRepository;

    private User testUser;
    private Event testEvent;
    private Driver testDriver;

    @BeforeEach
    void setUp() {
        betRepository.deleteAll();
        driverRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();

        testUser = userRepository.save(User.builder()
                .name("Test User")
                .balance(new BigDecimal("100.00"))
                .build());

        testEvent = eventRepository.save(Event.builder()
                .externalSessionKey(9998)
                .year(2024)
                .country("Monaco")
                .sessionName("Race")
                .startTime(LocalDateTime.now())
                .circuitShortName("MON")
                .build());

        testDriver = driverRepository.save(Driver.builder()
                .externalDriverId(44)
                .fullName("Lewis Hamilton")
                .driverNumber(44)
                .teamName("Mercedes")
                .countryCode("GBR")
                .build());
    }

    @Test
    @DisplayName("POST /api/bets - Should place a bet successfully")
    void placeBet_Success() throws Exception {
        PlaceBetRequest request = new PlaceBetRequest(
                testUser.getId(),
                testEvent.getId(),
                testDriver.getId(),
                new BigDecimal("10.00")
        );

        mockMvc.perform(post("/api/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.userId", is(testUser.getId().intValue())))
                .andExpect(jsonPath("$.eventId", is(testEvent.getId().intValue())))
                .andExpect(jsonPath("$.driverId", is(testDriver.getId().intValue())))
                .andExpect(jsonPath("$.stake", is(10.0)))
                .andExpect(jsonPath("$.odds", notNullValue()))
                .andExpect(jsonPath("$.potentialWinnings", notNullValue()))
                .andExpect(jsonPath("$.status", is("PENDING")));

        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(updatedUser.getBalance())
                .isEqualByComparingTo(new BigDecimal("90.00"));
    }

    @Test
    @DisplayName("POST /api/bets - Should return 422 for insufficient balance")
    void placeBet_InsufficientBalance() throws Exception {
        PlaceBetRequest request = new PlaceBetRequest(
                testUser.getId(),
                testEvent.getId(),
                testDriver.getId(),
                new BigDecimal("500.00")
        );

        mockMvc.perform(post("/api/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errorCode", is("2001")))
                .andExpect(jsonPath("$.message", containsString("Insufficient balance")));
    }

    @Test
    @DisplayName("POST /api/bets - Should return 404 for non-existent user")
    void placeBet_UserNotFound() throws Exception {
        PlaceBetRequest request = new PlaceBetRequest(
                9999L,
                testEvent.getId(),
                testDriver.getId(),
                new BigDecimal("10.00")
        );

        mockMvc.perform(post("/api/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode", is("1001")));
    }

    @Test
    @DisplayName("POST /api/bets - Should return 400 for validation errors")
    void placeBet_ValidationError() throws Exception {
        PlaceBetRequest request = new PlaceBetRequest(
                testUser.getId(),
                testEvent.getId(),
                testDriver.getId(),
                new BigDecimal("-10.00")
        );

        mockMvc.perform(post("/api/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is("4001")));
    }

    @Test
    @DisplayName("POST /api/bets - Should return 404 for non-existent event")
    void placeBet_EventNotFound() throws Exception {
        PlaceBetRequest request = new PlaceBetRequest(
                testUser.getId(),
                9999L,
                testDriver.getId(),
                new BigDecimal("10.00")
        );

        mockMvc.perform(post("/api/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode", is("1002")));
    }

    @Test
    @DisplayName("POST /api/bets - Should return 404 for non-existent driver")
    void placeBet_DriverNotFound() throws Exception {
        PlaceBetRequest request = new PlaceBetRequest(
                testUser.getId(),
                testEvent.getId(),
                9999L,
                new BigDecimal("10.00")
        );

        mockMvc.perform(post("/api/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode", is("1003")));
    }

    @Test
    @DisplayName("POST /api/bets - Should generate odds between 2, 3, or 4")
    void placeBet_ValidOdds() throws Exception {
        PlaceBetRequest request = new PlaceBetRequest(
                testUser.getId(),
                testEvent.getId(),
                testDriver.getId(),
                new BigDecimal("10.00")
        );

        mockMvc.perform(post("/api/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.odds", anyOf(is(2.0), is(3.0), is(4.0))));
    }

    @Test
    @DisplayName("POST /api/bets - Can bet on past events (per requirements)")
    void placeBet_PastEvent() throws Exception {
        Event pastEvent = eventRepository.save(Event.builder()
                .externalSessionKey(1234)
                .year(2023)
                .country("Italy")
                .sessionName("Race")
                .startTime(LocalDateTime.now().minusDays(30))
                .circuitShortName("MON")
                .build());

        PlaceBetRequest request = new PlaceBetRequest(
                testUser.getId(),
                pastEvent.getId(),
                testDriver.getId(),
                new BigDecimal("10.00")
        );

        mockMvc.perform(post("/api/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventId", is(pastEvent.getId().intValue())));
    }
}