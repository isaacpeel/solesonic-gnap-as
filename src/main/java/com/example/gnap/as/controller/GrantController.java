package com.example.gnap.as.controller;

import com.example.gnap.as.dto.GrantRequestDto;
import com.example.gnap.as.dto.GrantResponseDto;
import com.example.gnap.as.service.GrantService;
import com.example.gnap.as.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for grant management in the GNAP protocol.
 */
@RestController
@RequestMapping("/gnap")
@RequiredArgsConstructor
@Slf4j
public class GrantController {

    private final GrantService grantService;
    private final TokenService tokenService;

    /**
     * Process a grant request.
     *
     * @param requestDto the grant request DTO
     * @return the grant response DTO
     */
    @PostMapping("/grant")
    public ResponseEntity<GrantResponseDto> processGrantRequest(@RequestBody GrantRequestDto requestDto) {
        log.info("Received grant request: {}", requestDto);
        try {
            GrantResponseDto responseDto = grantService.processGrantRequest(requestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (IllegalArgumentException e) {
            log.error("Error processing grant request", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error processing grant request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Process a continuation request.
     *
     * @param grantId the grant ID
     * @param authorization the authorization header containing the continuation token
     * @return the grant response DTO
     */
    @GetMapping("/grant/{grantId}")
    public ResponseEntity<GrantResponseDto> processContinuation(
            @PathVariable String grantId,
            @RequestHeader("Authorization") String authorization) {
        log.info("Received continuation request for grant: {}", grantId);
        try {
            // Extract token from Authorization header
            String token = authorization.replace("Bearer ", "");
            GrantResponseDto responseDto = grantService.processContinuation(grantId, token);
            return ResponseEntity.ok(responseDto);
        } catch (IllegalArgumentException e) {
            log.error("Error processing continuation request", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Unexpected error processing continuation request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update a grant's status.
     *
     * @param grantId the grant ID
     * @param status the new status
     * @param authorization the authorization header containing the continuation token
     * @return a success response
     */
    @PutMapping("/grant/{grantId}/status")
    public ResponseEntity<Void> updateGrantStatus(
            @PathVariable String grantId,
            @RequestParam String status,
            @RequestHeader("Authorization") String authorization) {
        log.info("Received status update request for grant: {}, status: {}", grantId, status);
        try {
            // Extract token from Authorization header
            String token = authorization.replace("Bearer ", "");

            // Validate token
            if (!tokenService.validateContinuationToken(grantId, token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Update status
            grantService.updateGrantStatus(grantId, parseStatus(status));
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("Error updating grant status", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error updating grant status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Parse a status string to a GrantStatus enum.
     *
     * @param status the status string
     * @return the GrantStatus enum
     */
    private com.example.gnap.as.model.GrantRequest.GrantStatus parseStatus(String status) {
        try {
            return com.example.gnap.as.model.GrantRequest.GrantStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }
}
