package com.example.gnap.as.config;

import com.example.gnap.as.service.GrantService;
import com.example.gnap.as.service.InteractionService;
import com.example.gnap.as.service.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Application configuration for the GNAP Authorization Server.
 */
@Configuration
@EnableScheduling
public class ApplicationConfig {

    private final GrantService grantService;
    private final TokenService tokenService;
    private final InteractionService interactionService;

    /**
     * Constructor for ApplicationConfig.
     *
     * @param grantService the grant service
     * @param tokenService the token service
     * @param interactionService the interaction service
     */
    public ApplicationConfig(GrantService grantService, TokenService tokenService, InteractionService interactionService) {
        this.grantService = grantService;
        this.tokenService = tokenService;
        this.interactionService = interactionService;
    }

    /**
     * Configure ObjectMapper for JSON serialization/deserialization.
     *
     * @return the configured ObjectMapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
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