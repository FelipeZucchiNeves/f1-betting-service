package com.f1betting.service;

import com.f1betting.dto.request.EventOutcomeRequest;
import com.f1betting.dto.request.PlaceBetRequest;
import com.f1betting.dto.response.BetResponse;
import com.f1betting.dto.response.EventOutcomeResponse;
import com.f1betting.entity.Bet;
import com.f1betting.entity.BetStatus;
import com.f1betting.entity.Driver;
import com.f1betting.entity.Event;
import com.f1betting.entity.User;
import com.f1betting.exception.InsufficientBalanceException;
import com.f1betting.mapper.BetMapper;
import com.f1betting.mapper.EventOutcomeMapper;
import com.f1betting.repository.BetRepository;
import com.f1betting.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.f1betting.util.OddGeneration.generateRandomOdds;

/**
 * Service for bet-related operations.
 * 
 * Implements:
 * - Use Case 2: Place a Bet
 * - Use Case 3: Event Outcome (Settlement)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BetService {



    private final BetRepository betRepository;
    private final EventRepository eventRepository;
    private final UserService userService;
    private final EventService eventService;
    private final DriverService driverService;
    private final EventOutcomeMapper eventOutcomeMapper;
    private final BetMapper betMapper;

    /**
     * Use Case 2: Place a Bet
     * 
     * User places a single bet on a driver for an F1 event.
     * The stake is deducted from user balance.
     */
    @Transactional
    public BetResponse placeBet(PlaceBetRequest request) {
        log.info("Processing bet request: userId={}, eventId={}, driverId={}, stake={}",
                request.userId(), request.eventId(), request.driverId(), request.stake());

        User user = userService.findUserById(request.userId());
        Event event = eventService.findEventById(request.eventId());
        Driver driver = driverService.findDriverById(request.driverId());

        if (Boolean.TRUE.equals(event.getSettled())) {
            throw new IllegalStateException("Event " + event.getId() + " has already been settled");
        }

        if (user.getBalance().compareTo(request.stake()) < 0) {
            throw new InsufficientBalanceException(request.stake(), user.getBalance());
        }

        BigDecimal odds = generateRandomOdds();

        userService.deductBalance(user.getId(), request.stake());

        Bet bet = Bet.builder()
                .user(user)
                .event(event)
                .driver(driver)
                .stake(request.stake())
                .odds(odds)
                .status(BetStatus.PENDING)
                .build();
        
        bet = betRepository.save(bet);
        log.info("Bet placed successfully: betId={}, odds={}", bet.getId(), odds);

        return buildBetResponse(bet);
    }

    /**
     * Use Case 3: Event Outcome
     * 
     * Process event outcome and settle all pending bets:
     * - Save the outcome (winner driver)
     * - Check which bets won/lost and update status
     * - Calculate prize for winning bets (stake × odds)
     * - Add won money to User Balance
     */
    @Transactional
    public EventOutcomeResponse settleEventBets(Long eventId, EventOutcomeRequest request) {
        log.info("Settling bets for event {} with winner driver {}", eventId, request.winnerDriverId());

        Event event = eventService.findEventById(eventId);

        if (Boolean.TRUE.equals(event.getSettled())) {
            throw new IllegalStateException("Event " + eventId + " has already been settled");
        }

        Driver winnerDriver = driverService.findDriverById(request.winnerDriverId());

        event.setWinnerDriverId(request.winnerDriverId());
        event.setSettled(true);
        eventRepository.save(event);

        List<Bet> pendingBets = betRepository.findByEventIdAndStatus(eventId, BetStatus.PENDING);
        
        int winningBets = 0;
        int losingBets = 0;
        BigDecimal totalPrizesPaid = BigDecimal.ZERO;
        List<EventOutcomeResponse.SettledBetInfo> settledBets = new ArrayList<>();
        
        for (Bet bet : pendingBets) {
            boolean isWinningBet = bet.getDriver().getId().equals(request.winnerDriverId());
            BigDecimal prize = BigDecimal.ZERO;
            
            if (isWinningBet) {
                prize = bet.getStake().multiply(bet.getOdds()).setScale(2, RoundingMode.HALF_UP);
                bet.setStatus(BetStatus.WON);

                userService.creditBalance(bet.getUser().getId(), prize);
                
                winningBets++;
                totalPrizesPaid = totalPrizesPaid.add(prize);
                log.info("Bet {} WON - User {} receives {} EUR", bet.getId(), bet.getUser().getId(), prize);
            } else {
                bet.setStatus(BetStatus.LOST);
                losingBets++;
                log.info("Bet {} LOST - User {} loses stake {} EUR", bet.getId(), bet.getUser().getId(), bet.getStake());
            }
            
            betRepository.save(bet);
            settledBets.add(eventOutcomeMapper.mapToSettledBetInfo(bet, prize));
        }
        
        log.info("Event {} settled - {} winning bets, {} losing bets, total prizes paid: {} EUR",
                eventId, winningBets, losingBets, totalPrizesPaid);

        return new EventOutcomeResponse(
                eventId,
                event.getCountry() + " - " + event.getSessionName(),
                request.winnerDriverId(),
                winnerDriver.getFullName(),
                pendingBets.size(),
                winningBets,
                losingBets,
                totalPrizesPaid,
                settledBets
        );
    }

    /**
     * Build bet response from entity.
     */
    private BetResponse buildBetResponse(Bet bet) {
        BigDecimal potentialWinnings = bet.getStake()
                .multiply(bet.getOdds())
                .setScale(2, RoundingMode.HALF_UP);

        return betMapper.mapToBetResponse(bet, potentialWinnings);
    }

}
