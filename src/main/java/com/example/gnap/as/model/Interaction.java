package com.example.gnap.as.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing an interaction in the GNAP protocol.
 * Interactions are used for user consent and authentication.
 */
@Entity
@Table(name = "interaction")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Interaction {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grant_id", nullable = false)
    private GrantRequest grant;

    @Column(name = "interaction_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private InteractionType interactionType;

    @Column(name = "interaction_url")
    private String interactionUrl;

    @Column(name = "nonce")
    private String nonce;

    @Column(name = "hash_method")
    private String hashMethod;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Enum representing the possible types of interactions.
     */
    public enum InteractionType {
        REDIRECT,
        APP,
        USER_CODE,
        USER_CODE_URI
    }
}