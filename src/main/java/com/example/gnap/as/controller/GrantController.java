package com.example.gnap.as.controller;

import com.example.gnap.as.model.GrantRequest;
import com.example.gnap.as.service.GrantService;
import com.example.gnap.as.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller for grant management in the GNAP protocol.
 */
@RestController
@RequestMapping("/gnap")
public class GrantController {

    private static final Logger log = LoggerFactory.getLogger(GrantController.class);

    private final GrantService grantService;
    private final TokenService tokenService;

    public GrantController(GrantService grantService, TokenService tokenService) {
        this.grantService = grantService;
        this.tokenService = tokenService;
    }

    /**
     * Process a grant request.
     *
     * @param request the grant request
     * @return the grant response
     */
    @PostMapping("/grant")
    public ResponseEntity<GrantRequest> processGrantRequest(@RequestBody GrantRequest request) {
        log.info("Received grant request: {}", request);
        try {
            GrantRequest response = grantService.processGrantRequest(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
     * @return the grant response
     */
    @GetMapping("/grant/{grantId}")
    public ResponseEntity<GrantRequest> processContinuation(
            @PathVariable UUID grantId,
            @RequestHeader("Authorization") String authorization) {
        log.info("Received continuation request for grant: {}", grantId);
        try {
            // Extract token from Authorization header
            String token = authorization.replace("Bearer ", "");
            GrantRequest response = grantService.processContinuation(grantId, token);
            return ResponseEntity.ok(response);
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
            @PathVariable UUID grantId,
            @RequestParam String status,
            @RequestHeader("Authorization") String authorization) {
        log.info("Received status update request for grant: {}, status: {}", grantId, status);
        try {
            // Extract token from Authorization header
            String token = authorization.replace("Bearer ", "");

            // Validate token
            if (tokenService.validateContinuationToken(grantId, token)) {
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
    private GrantRequest.GrantStatus parseStatus(String status) {
        try {
            return GrantRequest.GrantStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }
}
