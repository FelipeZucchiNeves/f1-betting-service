package com.f1betting.integration;

import com.f1betting.dto.request.EventOutcomeRequest;
import com.f1betting.entity.*;
import com.f1betting.repository.*;
import com.f1betting.service.OpenF1ApiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("EventController - Integration Tests")
class EventControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BetRepository betRepository;

    @MockBean
    private OpenF1ApiService openF1ApiService;

    private Event testEvent;
    private Driver testDriver;

    @BeforeEach
    void setUp() {
        betRepository.deleteAll();
        eventRepository.deleteAll();
        driverRepository.deleteAll();
        userRepository.deleteAll();

        testEvent = eventRepository.save(Event.builder()
                .externalSessionKey(9999)
                .year(2024)
                .country("Test Country")
                .sessionName("Race")
                .startTime(LocalDateTime.now())
                .circuitShortName("TST")
                .settled(false)
                .build());

        testDriver = driverRepository.save(Driver.builder()
                .externalDriverId(1)
                .fullName("Test Driver")
                .driverNumber(1)
                .teamName("Test Team")
                .countryCode("TST")
                .build());

        when(openF1ApiService.findSessions(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(openF1ApiService.findDrivers(any()))
                .thenReturn(Collections.emptyList());
    }

    @Nested
    @DisplayName("List Events")
    class ListEventsTests {

        @Test
        @DisplayName("GET /api/events - Should return list of events")
        void listEvents_Success() throws Exception {
            mockMvc.perform(get("/api/events")
                            .param("year", "2024"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", isA(List.class)))
                    .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                    .andExpect(jsonPath("$[0].country", is("Test Country")));
        }

        @Test
        @DisplayName("GET /api/events - Should filter by year and country")
        void listEvents_WithFilters() throws Exception {
            mockMvc.perform(get("/api/events")
                            .param("year", "2024")
                            .param("country", "Test Country"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                    .andExpect(jsonPath("$[0].country", is("Test Country")));
        }

        @Test
        @DisplayName("GET /api/events - Should filter by session type")
        void listEvents_FilterBySessionType() throws Exception {
            mockMvc.perform(get("/api/events")
                            .param("sessionType", "Race"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                    .andExpect(jsonPath("$[0].sessionName", is("Race")));
        }

        @Test
        @DisplayName("GET /api/events - Events include driver market")
        void listEvents_DriverMarketWithOdds() throws Exception {
            mockMvc.perform(get("/api/events"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                    .andExpect(jsonPath("$[0].driverMarket", notNullValue()));
        }
    }

    @Nested
    @DisplayName("Event Outcome")
    class EventOutcomeTests {

        private User testUser;
        private Bet testBet;

        @BeforeEach
        void setUpBetData() {
            testUser = userRepository.save(User.builder()
                    .name("Test User")
                    .balance(new BigDecimal("100.00"))
                    .build());

            testBet = betRepository.save(Bet.builder()
                    .user(testUser)
                    .event(testEvent)
                    .driver(testDriver)
                    .stake(new BigDecimal("10.00"))
                    .odds(new BigDecimal("3.00"))
                    .status(BetStatus.PENDING)
                    .build());
        }

        @Test
        @DisplayName("POST /api/events/{id}/outcome - Should settle winning bets")
        void simulateOutcome_WinningBet() throws Exception {
            EventOutcomeRequest request = new EventOutcomeRequest(testDriver.getId());

            mockMvc.perform(post("/api/events/{eventId}/outcome", testEvent.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.eventId", is(testEvent.getId().intValue())))
                    .andExpect(jsonPath("$.winnerDriverId", is(testDriver.getId().intValue())))
                    .andExpect(jsonPath("$.winningBets", is(1)))
                    .andExpect(jsonPath("$.losingBets", is(0)))
                    .andExpect(jsonPath("$.totalPrizesPaid", is(30.0)));

            User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
            org.assertj.core.api.Assertions.assertThat(updatedUser.getBalance())
                    .isEqualByComparingTo(new BigDecimal("130.00"));
        }

        @Test
        @DisplayName("POST /api/events/{id}/outcome - Should settle losing bets")
        void simulateOutcome_LosingBet() throws Exception {
            Driver winnerDriver = driverRepository.save(Driver.builder()
                    .externalDriverId(2)
                    .fullName("Winner Driver")
                    .driverNumber(2)
                    .teamName("Winner Team")
                    .countryCode("WIN")
                    .build());

            EventOutcomeRequest request = new EventOutcomeRequest(winnerDriver.getId());

            mockMvc.perform(post("/api/events/{eventId}/outcome", testEvent.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.winningBets", is(0)))
                    .andExpect(jsonPath("$.losingBets", is(1)))
                    .andExpect(jsonPath("$.totalPrizesPaid", is(0)));

            User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
            org.assertj.core.api.Assertions.assertThat(updatedUser.getBalance())
                    .isEqualByComparingTo(new BigDecimal("100.00"));
        }

        @Test
        @DisplayName("POST /api/events/{id}/outcome - Should return 400 if already settled")
        void simulateOutcome_AlreadySettled() throws Exception {
            EventOutcomeRequest request = new EventOutcomeRequest(testDriver.getId());

            mockMvc.perform(post("/api/events/{eventId}/outcome", testEvent.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/api/events/{eventId}/outcome", testEvent.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("already been settled")));
        }

        @Test
        @DisplayName("POST /api/events/{id}/outcome - Should return 404 for non-existent event")
        void simulateOutcome_EventNotFound() throws Exception {
            EventOutcomeRequest request = new EventOutcomeRequest(testDriver.getId());

            mockMvc.perform(post("/api/events/{eventId}/outcome", 9999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST /api/events/{id}/outcome - Should return 404 for non-existent driver")
        void simulateOutcome_DriverNotFound() throws Exception {
            EventOutcomeRequest request = new EventOutcomeRequest(9999L);

            mockMvc.perform(post("/api/events/{eventId}/outcome", testEvent.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }
}