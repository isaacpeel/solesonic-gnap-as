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
        log.info("Creating interactions for grant request - interaction types requested: redirect={}, app={}, userCode={}",
                interactInfo.getRedirect() != null, 
                interactInfo.getApp() != null,
                interactInfo.getUserCode() != null);

        List<Interaction> interactions = new ArrayList<>();

        // Create redirect interaction if requested
        if (interactInfo.getRedirect() != null) {
            Interaction interaction = new Interaction();
            interaction.setGrant(grant);
            interaction.setInteractionType(Interaction.InteractionType.REDIRECT);
            interaction.setInteractionUrl(issuer + "/gnap/interact/redirect/" + grant.getId());
            // Nonce is not directly accessible in InteractInfo, would need to be added
            interaction.setExpiresAt(LocalDateTime.now().plusSeconds(interactionTimeout));
            interactions.add(interaction);
            log.debug("Created REDIRECT interaction");
        }

        // Create app interaction if requested
        if (interactInfo.getApp() != null) {
            Interaction interaction = new Interaction();
            interaction.setGrant(grant);
            interaction.setInteractionType(Interaction.InteractionType.APP);
            interaction.setInteractionUrl(issuer + "/gnap/interact/app/" + grant.getId());
            // Nonce is not directly accessible in InteractInfo, would need to be added
            interaction.setExpiresAt(LocalDateTime.now().plusSeconds(interactionTimeout));
            interactions.add(interaction);
            log.debug("Created APP interaction");
        }

        // Create user code interaction if requested
        if (interactInfo.getUserCode() != null) {
            Interaction interaction = new Interaction();
            interaction.setGrant(grant);
            interaction.setInteractionType(Interaction.InteractionType.USER_CODE);
            interaction.setInteractionUrl(issuer + "/gnap/interact/user-code/" + grant.getId());
            interaction.setExpiresAt(LocalDateTime.now().plusSeconds(interactionTimeout));
            interactions.add(interaction);
            log.debug("Created USER_CODE interaction");
        }

        // Set hash method for finish callback if provided
        if (interactInfo.getFinish() != null && interactInfo.getFinish().getMethod() != null) {
            for (Interaction interaction : interactions) {
                interaction.setHashMethod(interactInfo.getFinish().getMethod());
            }
            log.debug("Set hash method for {} interactions", interactions.size());
        }

        List<Interaction> savedInteractions = interactionRepository.saveAll(interactions);
        log.info("Successfully created {} interactions", savedInteractions.size());
        return savedInteractions;
    }

    /**
     * Build an interaction response from a list of interactions.
     *
     * @param interactions the list of interactions
     * @return the interaction response
     */
    public GrantRequest.InteractInfo buildInteractResponse(List<Interaction> interactions) {
        log.info("Building interaction response from {} interactions", interactions.size());
        GrantRequest.InteractInfo interactInfo = new GrantRequest.InteractInfo();

        for (Interaction interaction : interactions) {
            switch (interaction.getInteractionType()) {
                case REDIRECT:
                    interactInfo.setRedirect(interaction.getInteractionUrl());
                    log.debug("Added REDIRECT interaction to response");
                    break;
                case APP:
                    interactInfo.setApp(interaction.getInteractionUrl());
                    log.debug("Added APP interaction to response");
                    break;
                case USER_CODE:
                    Interaction.UserCode userCode = new Interaction.UserCode();
                    userCode.setCode(generateUserCode());
                    userCode.setUri(interaction.getInteractionUrl());
                    interactInfo.setUserCode(userCode);
                    log.debug("Added USER_CODE interaction to response");
                    break;
                case USER_CODE_URI:
                    // No need to set anything here, as the client already has the URI
                    log.debug("Skipped USER_CODE_URI interaction in response");
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
            log.debug("Added finish information to response with hash method");
        } else {
            log.debug("No finish information added to response (no hash method found)");
        }

        log.info("Completed building interaction response with redirect={}, app={}, userCode={}, finish={}",
                interactInfo.getRedirect() != null,
                interactInfo.getApp() != null,
                interactInfo.getUserCode() != null,
                interactInfo.getFinish() != null);
        return interactInfo;
    }

    /**
     * Generate a user code.
     *
     * @return the generated user code
     */
    private String generateUserCode() {
        log.debug("Generating new user code");
        // Generate a 6-digit code
        String code = String.format("%06d", (int) (Math.random() * 1000000));
        log.debug("User code generated successfully");
        return code;
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
    public boolean validateInteraction(UUID grantId, String interactionId, String nonce) {
        log.info("Validating interaction");

        Optional<Interaction> interactionOptional = interactionRepository.findById(interactionId);

        if (interactionOptional.isEmpty()) {
            log.info("Interaction validation failed: interaction not found");
            return false;
        }

        Interaction interaction = interactionOptional.get();

        // Check if the interaction belongs to the grant
        if (!interaction.getGrant().getId().equals(grantId)) {
            log.info("Interaction validation failed: interaction does not belong to the specified grant");
            return false;
        }

        // Check if the interaction has expired
        if (interaction.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.info("Interaction validation failed: interaction has expired");
            return false;
        }

        // Check if the nonce matches (if provided)
        boolean nonceValid = interaction.getNonce() == null || interaction.getNonce().equals(nonce);
        if (!nonceValid) {
            log.info("Interaction validation failed: nonce mismatch");
        } else {
            log.info("Interaction validation successful");
        }

        return nonceValid;
    }

    /**
     * Find active interactions for a grant.
     *
     * @param grantId the grant ID
     * @return the list of active interactions
     */
    @Transactional(readOnly = true)
    public List<Interaction> findActiveInteractions(UUID grantId) {
        log.info("Finding active interactions for grant");
        List<Interaction> activeInteractions = interactionRepository.findByGrantIdAndExpiresAtAfter(grantId, LocalDateTime.now());
        log.info("Found {} active interactions", activeInteractions.size());
        return activeInteractions;
    }

    /**
     * Clean up expired interactions.
     */
    @Transactional
    public void cleanupExpiredInteractions() {
        log.info("Starting cleanup of expired interactions");
        List<Interaction> expiredInteractions = interactionRepository.findByExpiresAtBefore(LocalDateTime.now());
        log.info("Found {} expired interactions to clean up", expiredInteractions.size());

        if (!expiredInteractions.isEmpty()) {
            interactionRepository.deleteAll(expiredInteractions);
            log.info("Successfully deleted {} expired interactions", expiredInteractions.size());
        } else {
            log.debug("No expired interactions to clean up");
        }
    }
}
