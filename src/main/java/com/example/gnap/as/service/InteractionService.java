package com.example.gnap.as.service;

import com.example.gnap.as.model.GrantRequest;
import com.example.gnap.as.model.Interaction;
import com.example.gnap.as.repository.InteractionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for interaction management in the GNAP protocol.
 */
@Service
public class InteractionService {

    private static final Logger log = LoggerFactory.getLogger(InteractionService.class);

    private final InteractionRepository interactionRepository;

    @Value("${gnap.as.interaction.timeout:300}")
    private int interactionTimeout;

    @Value("${gnap.as.issuer:https://auth.example.com}")
    private String issuer;

    public InteractionService(InteractionRepository interactionRepository) {
        this.interactionRepository = interactionRepository;
    }

    /**
     * Create interactions for a grant request.
     *
     * @param interactInfo the interaction information
     * @param grant the grant request
     * @return the list of created interactions
     */
    @Transactional
    public List<Interaction> createInteractions(GrantRequest.InteractInfo interactInfo, GrantRequest grant) {
        List<Interaction> interactions = new ArrayList<>();

        // Create redirect interaction if requested
        if (interactInfo.getRedirect() != null) {
            Interaction interaction = new Interaction();
            interaction.setId(UUID.randomUUID().toString());
            interaction.setGrant(grant);
            interaction.setInteractionType(Interaction.InteractionType.REDIRECT);
            interaction.setInteractionUrl(issuer + "/gnap/interact/redirect/" + grant.getId());
            // Nonce is not directly accessible in InteractInfo, would need to be added
            interaction.setExpiresAt(LocalDateTime.now().plusSeconds(interactionTimeout));
            interactions.add(interaction);
        }

        // Create app interaction if requested
        if (interactInfo.getApp() != null) {
            Interaction interaction = new Interaction();
            interaction.setId(UUID.randomUUID().toString());
            interaction.setGrant(grant);
            interaction.setInteractionType(Interaction.InteractionType.APP);
            interaction.setInteractionUrl(issuer + "/gnap/interact/app/" + grant.getId());
            // Nonce is not directly accessible in InteractInfo, would need to be added
            interaction.setExpiresAt(LocalDateTime.now().plusSeconds(interactionTimeout));
            interactions.add(interaction);
        }

        // Create user code interaction if requested
        if (interactInfo.getUserCode() != null) {
            Interaction interaction = new Interaction();
            interaction.setId(UUID.randomUUID().toString());
            interaction.setGrant(grant);
            interaction.setInteractionType(Interaction.InteractionType.USER_CODE);
            interaction.setInteractionUrl(issuer + "/gnap/interact/user-code/" + grant.getId());
            interaction.setExpiresAt(LocalDateTime.now().plusSeconds(interactionTimeout));
            interactions.add(interaction);
        }

        // Set hash method for finish callback if provided
        if (interactInfo.getFinish() != null && interactInfo.getFinish().getMethod() != null) {
            for (Interaction interaction : interactions) {
                interaction.setHashMethod(interactInfo.getFinish().getMethod());
            }
        }

        return interactionRepository.saveAll(interactions);
    }

    /**
     * Build an interaction response from a list of interactions.
     *
     * @param interactions the list of interactions
     * @return the interaction response
     */
    public GrantRequest.InteractInfo buildInteractResponse(List<Interaction> interactions) {
        GrantRequest.InteractInfo interactInfo = new GrantRequest.InteractInfo();

        for (Interaction interaction : interactions) {
            switch (interaction.getInteractionType()) {
                case REDIRECT:
                    interactInfo.setRedirect(interaction.getInteractionUrl());
                    break;
                case APP:
                    interactInfo.setApp(interaction.getInteractionUrl());
                    break;
                case USER_CODE:
                    Interaction.UserCode userCode = new Interaction.UserCode();
                    userCode.setCode(generateUserCode());
                    userCode.setUri(interaction.getInteractionUrl());
                    interactInfo.setUserCode(userCode);
                    break;
                case USER_CODE_URI:
                    // No need to set anything here, as the client already has the URI
                    break;
            }
        }

        // Add finish information if any interaction has a hash method
        Optional<Interaction> interactionWithHash = interactions.stream()
                .filter(i -> i.getHashMethod() != null)
                .findFirst();

        if (interactionWithHash.isPresent()) {
            Interaction.Finish finish = new Interaction.Finish();
            finish.setUri(issuer + "/gnap/interact/finish/" + interactionWithHash.get().getGrant().getId());
            finish.setMethod(interactionWithHash.get().getHashMethod());
            interactInfo.setFinish(finish);
        }

        return interactInfo;
    }

    /**
     * Generate a user code.
     *
     * @return the generated user code
     */
    private String generateUserCode() {
        // Generate a 6-digit code
        return String.format("%06d", (int) (Math.random() * 1000000));
    }

    /**
     * Validate an interaction result.
     *
     * @param grantId the grant ID
     * @param interactionId the interaction ID
     * @param nonce the nonce
     * @return true if the interaction is valid, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean validateInteraction(String grantId, String interactionId, String nonce) {
        Optional<Interaction> interaction = interactionRepository.findById(interactionId);

        if (interaction.isEmpty()) {
            return false;
        }

        Interaction i = interaction.get();

        // Check if the interaction belongs to the grant
        if (!i.getGrant().getId().equals(grantId)) {
            return false;
        }

        // Check if the interaction has expired
        if (i.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        // Check if the nonce matches (if provided)
        return i.getNonce() == null || i.getNonce().equals(nonce);
    }

    /**
     * Find active interactions for a grant.
     *
     * @param grantId the grant ID
     * @return the list of active interactions
     */
    @Transactional(readOnly = true)
    public List<Interaction> findActiveInteractions(String grantId) {
        return interactionRepository.findByGrantIdAndExpiresAtAfter(grantId, LocalDateTime.now());
    }

    /**
     * Clean up expired interactions.
     */
    @Transactional
    public void cleanupExpiredInteractions() {
        List<Interaction> expiredInteractions = interactionRepository.findByExpiresAtBefore(LocalDateTime.now());
        interactionRepository.deleteAll(expiredInteractions);
    }
}
