package com.f1betting.controller;

import com.f1betting.dto.response.ErrorResponse;
import com.f1betting.dto.response.UserBalanceResponse;
import com.f1betting.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user operations.
 * 
 * Per the requirements:
 * - User is already registered (pass User ID as parameter)
 * - User cannot deposit or withdraw money
 * - User starts with 100 EUR given during registration
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User Balance APIs")
public class UserController {

    private final UserService userService;

    /**
     * Get user balance.
     * 
     * Useful for:
     * - Checking available balance before placing a bet
     * - Verifying balance after bet placement or winnings
     */
    @GetMapping("/{userId}/balance")
    @Operation(summary = "Get user balance", 
               description = "Get the current balance information for a user. " +
                             "Users start with 100 EUR and cannot deposit or withdraw.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved balance"),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserBalanceResponse> getBalance(
            @Parameter(description = "User ID") 
            @PathVariable Long userId) {
        log.info("GET /api/users/{}/balance", userId);
        UserBalanceResponse response = userService.getBalance(userId);
        return ResponseEntity.ok(response);
    }
}
