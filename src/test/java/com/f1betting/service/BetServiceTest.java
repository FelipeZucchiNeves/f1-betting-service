package com.f1betting.service;

import com.f1betting.dto.request.EventOutcomeRequest;
import com.f1betting.dto.request.PlaceBetRequest;
import com.f1betting.dto.response.BetResponse;
import com.f1betting.dto.response.EventOutcomeResponse;
import com.f1betting.entity.*;
import com.f1betting.exception.InsufficientBalanceException;
import com.f1betting.mapper.BetMapper;
import com.f1betting.mapper.EventOutcomeMapper;
import com.f1betting.repository.BetRepository;
import com.f1betting.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BetService Unit Tests")
class BetServiceTest {

    @Mock
    private BetRepository betRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserService userService;

    @Mock
    private EventService eventService;

    @Mock
    private DriverService driverService;

    @Spy
    private EventOutcomeMapper eventOutcomeMapper = new EventOutcomeMapper();

    @Spy
    private BetMapper betMapper = new BetMapper();

    @InjectMocks
    private BetService betService;

    private User testUser;
    private Event testEvent;
    private Driver testDriver;
    private PlaceBetRequest validRequest;
    private Bet testBet;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .balance(new BigDecimal("100.00"))
                .build();

        testEvent = Event.builder()
                .id(1L)
                .externalSessionKey(9999)
                .year(2024)
                .country("Monaco")
                .sessionName("Race")
                .startTime(LocalDateTime.now().plusDays(1))
                .settled(false)
                .build();

        testDriver = Driver.builder()
                .id(1L)
                .externalDriverId(1)
                .fullName("Max Verstappen")
                .driverNumber(1)
                .teamName("Red Bull Racing")
                .build();

        validRequest = new PlaceBetRequest(1L, 1L, 1L, new BigDecimal("10.00"));

        testBet = Bet.builder()
                .id(1L)
                .user(testUser)
                .event(testEvent)
                .driver(testDriver)
                .stake(new BigDecimal("10.00"))
                .odds(new BigDecimal("3.00"))
                .status(BetStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Place Bet Tests")
    class PlaceBetTests {

        @Test
        @DisplayName("Should place bet successfully - happy path")
        void placeBet_Success() {
            when(userService.findUserById(1L)).thenReturn(testUser);
            when(eventService.findEventById(1L)).thenReturn(testEvent);
            when(driverService.findDriverById(1L)).thenReturn(testDriver);
            doNothing().when(userService).deductBalance(eq(1L), any(BigDecimal.class));
            when(betRepository.save(any(Bet.class))).thenReturn(testBet);

            BetResponse response = betService.placeBet(validRequest);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.userId()).isEqualTo(1L);
            assertThat(response.eventId()).isEqualTo(1L);
            assertThat(response.driverId()).isEqualTo(1L);
            assertThat(response.stake()).isEqualByComparingTo(new BigDecimal("10.00"));
            assertThat(response.status()).isEqualTo(BetStatus.PENDING);
            
            verify(userService).findUserById(1L);
            verify(eventService).findEventById(1L);
            verify(driverService).findDriverById(1L);
            verify(userService).deductBalance(eq(1L), any(BigDecimal.class));
            verify(betRepository).save(any(Bet.class));
        }

        @Test
        @DisplayName("Should throw exception when insufficient balance")
        void placeBet_InsufficientBalance() {
            testUser.setBalance(new BigDecimal("5.00")); // Less than stake
            when(userService.findUserById(1L)).thenReturn(testUser);
            when(eventService.findEventById(1L)).thenReturn(testEvent);
            when(driverService.findDriverById(1L)).thenReturn(testDriver);

            assertThatThrownBy(() -> betService.placeBet(validRequest))
                    .isInstanceOf(InsufficientBalanceException.class)
                    .hasMessageContaining("Insufficient balance");

            verify(userService, never()).deductBalance(anyLong(), any(BigDecimal.class));
            verify(betRepository, never()).save(any(Bet.class));
        }

        @Test
        @DisplayName("Should calculate potential winnings correctly")
        void placeBet_CalculatesPotentialWinnings() {
            when(userService.findUserById(1L)).thenReturn(testUser);
            when(eventService.findEventById(1L)).thenReturn(testEvent);
            when(driverService.findDriverById(1L)).thenReturn(testDriver);
            doNothing().when(userService).deductBalance(eq(1L), any(BigDecimal.class));
            
            Bet savedBet = Bet.builder()
                    .id(1L)
                    .user(testUser)
                    .event(testEvent)
                    .driver(testDriver)
                    .stake(new BigDecimal("10.00"))
                    .odds(new BigDecimal("4.00"))
                    .status(BetStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();
            when(betRepository.save(any(Bet.class))).thenReturn(savedBet);

            BetResponse response = betService.placeBet(validRequest);

            assertThat(response.potentialWinnings()).isEqualByComparingTo(new BigDecimal("40.00"));
        }

        @Test
        @DisplayName("Should generate odds between 2, 3, or 4")
        void placeBet_GeneratesValidOdds() {
            when(userService.findUserById(1L)).thenReturn(testUser);
            when(eventService.findEventById(1L)).thenReturn(testEvent);
            when(driverService.findDriverById(1L)).thenReturn(testDriver);
            doNothing().when(userService).deductBalance(eq(1L), any(BigDecimal.class));
            when(betRepository.save(any(Bet.class))).thenAnswer(invocation -> {
                Bet bet = invocation.getArgument(0);
                bet.setId(1L);
                return bet;
            });

            BetResponse response = betService.placeBet(validRequest);

            assertThat(response.odds()).isIn(
                    new BigDecimal("2.00"),
                    new BigDecimal("3.00"),
                    new BigDecimal("4.00")
            );
        }
    }

    @Nested
    @DisplayName("Event Outcome Settlement Tests")
    class EventOutcomeTests {

        @Test
        @DisplayName("Should settle winning bet correctly")
        void settleEventBets_WinningBet() {
            EventOutcomeRequest request = new EventOutcomeRequest(1L);

            when(eventService.findEventById(1L)).thenReturn(testEvent);
            when(driverService.findDriverById(1L)).thenReturn(testDriver);
            when(eventRepository.save(any(Event.class))).thenReturn(testEvent);
            when(betRepository.findByEventIdAndStatus(1L, BetStatus.PENDING)).thenReturn(List.of(testBet));
            when(betRepository.save(any(Bet.class))).thenReturn(testBet);
            doNothing().when(userService).creditBalance(eq(1L), any(BigDecimal.class));

            EventOutcomeResponse response = betService.settleEventBets(1L, request);

            assertThat(response).isNotNull();
            assertThat(response.eventId()).isEqualTo(1L);
            assertThat(response.winnerDriverId()).isEqualTo(1L);
            assertThat(response.totalBetsSettled()).isEqualTo(1);
            assertThat(response.winningBets()).isEqualTo(1);
            assertThat(response.losingBets()).isEqualTo(0);
            assertThat(response.totalPrizesPaid()).isEqualByComparingTo(new BigDecimal("30.00"));
            
            verify(userService).creditBalance(eq(1L), eq(new BigDecimal("30.00")));
            verify(eventRepository).save(testEvent);
        }

        @Test
        @DisplayName("Should settle losing bet correctly")
        void settleEventBets_LosingBet() {
            Driver winnerDriver = Driver.builder()
                    .id(2L)
                    .fullName("Charles Leclerc")
                    .build();
            
            EventOutcomeRequest request = new EventOutcomeRequest(2L);

            when(eventService.findEventById(1L)).thenReturn(testEvent);
            when(driverService.findDriverById(2L)).thenReturn(winnerDriver);
            when(eventRepository.save(any(Event.class))).thenReturn(testEvent);
            when(betRepository.findByEventIdAndStatus(1L, BetStatus.PENDING)).thenReturn(List.of(testBet));
            when(betRepository.save(any(Bet.class))).thenReturn(testBet);

            EventOutcomeResponse response = betService.settleEventBets(1L, request);

            assertThat(response).isNotNull();
            assertThat(response.winningBets()).isEqualTo(0);
            assertThat(response.losingBets()).isEqualTo(1);
            assertThat(response.totalPrizesPaid()).isEqualByComparingTo(BigDecimal.ZERO);
            
            verify(userService, never()).creditBalance(anyLong(), any(BigDecimal.class));
        }

        @Test
        @DisplayName("Should throw exception when event already settled")
        void settleEventBets_AlreadySettled() {
            testEvent.setSettled(true);
            EventOutcomeRequest request = new EventOutcomeRequest(1L);

            when(eventService.findEventById(1L)).thenReturn(testEvent);

            assertThatThrownBy(() -> betService.settleEventBets(1L, request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already been settled");
        }

        @Test
        @DisplayName("Should handle multiple bets correctly")
        void settleEventBets_MultipleBets() {
            User user2 = User.builder().id(2L).name("User 2").balance(new BigDecimal("100.00")).build();
            Driver driver2 = Driver.builder().id(2L).fullName("Charles Leclerc").build();
            
            Bet winningBet = testBet;
            Bet losingBet = Bet.builder()
                    .id(2L)
                    .user(user2)
                    .event(testEvent)
                    .driver(driver2)
                    .stake(new BigDecimal("20.00"))
                    .odds(new BigDecimal("2.00"))
                    .status(BetStatus.PENDING)
                    .build();

            EventOutcomeRequest request = new EventOutcomeRequest(1L);

            when(eventService.findEventById(1L)).thenReturn(testEvent);
            when(driverService.findDriverById(1L)).thenReturn(testDriver);
            when(eventRepository.save(any(Event.class))).thenReturn(testEvent);
            when(betRepository.findByEventIdAndStatus(1L, BetStatus.PENDING)).thenReturn(List.of(winningBet, losingBet));
            when(betRepository.save(any(Bet.class))).thenAnswer(inv -> inv.getArgument(0));
            doNothing().when(userService).creditBalance(anyLong(), any(BigDecimal.class));

            EventOutcomeResponse response = betService.settleEventBets(1L, request);

            assertThat(response.totalBetsSettled()).isEqualTo(2);
            assertThat(response.winningBets()).isEqualTo(1);
            assertThat(response.losingBets()).isEqualTo(1);
            assertThat(response.totalPrizesPaid()).isEqualByComparingTo(new BigDecimal("30.00"));
        }
    }
}
