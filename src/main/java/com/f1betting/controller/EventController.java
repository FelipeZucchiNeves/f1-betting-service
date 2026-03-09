package com.f1betting.controller;

import com.f1betting.dto.request.EventOutcomeRequest;
import com.f1betting.dto.response.ErrorResponse;
import com.f1betting.dto.response.EventOutcomeResponse;
import com.f1betting.dto.response.EventResponse;
import com.f1betting.service.BetService;
import com.f1betting.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for F1 event operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "F1 Event and Driver Market APIs")
public class EventController {

    private final EventService eventService;
    private final BetService betService;

    /**
     * Returns events filtered by session type, year, and country.
     * Each event includes a Driver Market with:
     * - Full name of the Driver
     * - ID Number of the Driver  
     * - Odds (random 2, 3, or 4)
     */
    @GetMapping
    @Operation(summary = "List F1 events with Driver Market", 
               description = "Get a list of F1 events with optional filters. Each event includes a driver market with betting odds (2, 3, or 4). " +
                             "Events are called Sessions in the OpenF1 API. Drivers are loaded for the first 5 events per page to respect API rate limits.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved events with driver markets",
                    content = @Content(mediaType = "application/json", 
                                       schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "503", description = "External API unavailable")
    })
    public ResponseEntity<List<EventResponse>> listEvents(
            @Parameter(description = "Filter by year (e.g., 2024)") 
            @RequestParam(required = false) Integer year,
            @Parameter(description = "Filter by country name (e.g., Monaco)") 
            @RequestParam(required = false) String country,
            @Parameter(description = "Filter by session type (e.g., Race, Qualifying)") 
            @RequestParam(required = false) String sessionType
    ) {
        log.info("GET /api/events - year: {}, country: {}, sessionType: {}",
                year, country, sessionType);
        List<EventResponse> events = eventService.listEvents(year, country, sessionType);
        return ResponseEntity.ok(events);
    }

    /**
     * Receives request with event ID and winning driver ID.
     * - Saves the outcome
     * - Checks which bets won/lost and updates status
     * - Calculates prize for winning bets (stake × odds)
     * - Adds won money to User Balance
     */
    @PostMapping("/{eventId}/outcome")
    @Operation(summary = "Simulate event outcome", 
               description = "Simulate the outcome of an F1 event by specifying the winning driver. " +
                             "This will settle all pending bets: winning bets receive prize (stake × odds) " +
                             "credited to their balance, losing bets are marked as lost.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event outcome processed and bets settled"),
            @ApiResponse(responseCode = "400", description = "Event already settled or invalid request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Event or Driver not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EventOutcomeResponse> simulateOutcome(
            @Parameter(description = "Event ID") 
            @PathVariable Long eventId,
            @Valid @RequestBody EventOutcomeRequest request) {
        log.info("POST /api/events/{}/outcome - winnerDriverId: {}", eventId, request.winnerDriverId());
        EventOutcomeResponse response = betService.settleEventBets(eventId, request);
        return ResponseEntity.ok(response);
    }
}
