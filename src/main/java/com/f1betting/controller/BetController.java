package com.f1betting.controller;

import com.f1betting.dto.request.PlaceBetRequest;
import com.f1betting.dto.response.BetResponse;
import com.f1betting.dto.response.ErrorResponse;
import com.f1betting.service.BetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for betting operations.
 * 
 * Implements Use Case 2: Place a Bet
 */
@Slf4j
@RestController
@RequestMapping("/api/bets")
@RequiredArgsConstructor
@Tag(name = "Bets", description = "Betting APIs")
public class BetController {

    private final BetService betService;

    /**
     * Use Case 2: Place a Bet
     * 
     * User places a single bet on a driver to win an F1 event.
     * - User specifies the amount to bet in EUR
     * - System places the bet and updates User Balance
     * - Can bet on any F1 event from the past
     */
    @PostMapping
    @Operation(summary = "Place a bet", 
               description = "Place a bet on a driver for a specific F1 event. " +
                             "The stake will be deducted from the user's balance. " +
                             "For simplicity, bets can be placed on any F1 event fr om the past.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Bet placed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User, Event, or Driver not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Insufficient balance", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BetResponse> placeBet(@Valid @RequestBody PlaceBetRequest request) {
        log.info("POST /api/bets - userId: {}, eventId: {}, driverId: {}, stake: {}",
                request.userId(), request.eventId(), request.driverId(), request.stake());
        BetResponse response = betService.placeBet(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
