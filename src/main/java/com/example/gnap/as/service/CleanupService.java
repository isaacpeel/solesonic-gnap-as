package com.example.gnap.as.service;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Service for cleaning up expired resources in the GNAP protocol.
 */
@Service
@EnableScheduling
public class CleanupService {

    private final GrantService grantService;
    private final TokenService tokenService;
    private final InteractionService interactionService;

    /**
     * Constructor for CleanupService.
     *
     * @param grantService the grant service
     * @param tokenService the token service
     * @param interactionService the interaction service
     */
    public CleanupService(GrantService grantService, TokenService tokenService, InteractionService interactionService) {
        this.grantService = grantService;
        this.tokenService = tokenService;
        this.interactionService = interactionService;
    }

    /**
     * Schedule cleanup of expired grants, tokens, and interactions.
     * Runs every hour.
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void cleanupExpiredResources() {
        grantService.cleanupExpiredGrants();
        tokenService.cleanupExpiredTokens();
        interactionService.cleanupExpiredInteractions();
    }
}
