package com.f1betting.repository;

import com.f1betting.entity.Bet;
import com.f1betting.entity.BetStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Bet entity operations.
 */
@Repository
public interface BetRepository extends JpaRepository<Bet, Long> {
    
    /**
     * Find all bets for a given event with a specific status.
     * Used for bet settlement (finding all PENDING bets to settle).
     */
    List<Bet> findByEventIdAndStatus(Long eventId, BetStatus status);
}
