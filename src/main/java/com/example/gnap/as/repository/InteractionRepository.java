package com.example.gnap.as.repository;

import com.example.gnap.as.model.Interaction;
import com.example.gnap.as.model.Interaction.InteractionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Interaction entity.
 */
@Repository
public interface InteractionRepository extends JpaRepository<Interaction, String> {
    
    /**
     * Find interactions by grant ID.
     *
     * @param grantId the grant ID
     * @return the list of interactions
     */
    List<Interaction> findByGrantId(UUID grantId);
    
    /**
     * Find interactions by interaction type.
     *
     * @param interactionType the interaction type
     * @return the list of interactions
     */
    List<Interaction> findByInteractionType(InteractionType interactionType);
    
    /**
     * Find an interaction by its nonce.
     *
     * @param nonce the nonce
     * @return the interaction if found
     */
    Optional<Interaction> findByNonce(String nonce);
    
    /**
     * Find interactions that have expired.
     *
     * @param now the current time
     * @return the list of expired interactions
     */
    List<Interaction> findByExpiresAtBefore(LocalDateTime now);
    
    /**
     * Find active interactions by grant ID.
     *
     * @param grantId the grant ID
     * @param now the current time
     * @return the list of active interactions
     */
    List<Interaction> findByGrantIdAndExpiresAtAfter(UUID grantId, LocalDateTime now);
}