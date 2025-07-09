package com.example.gnap.as.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a grant request in the GNAP protocol.
 * A grant request is the core concept for authorization in GNAP.
 */
@Entity
@Table(name = "grant_request")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrantRequest {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private GrantStatus status;

    @Column(name = "redirect_uri")
    private String redirectUri;

    @Column(name = "state")
    private String state;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @OneToMany(mappedBy = "grant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<AccessToken> accessTokens = new HashSet<>();

    @OneToMany(mappedBy = "grant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Interaction> interactions = new HashSet<>();

    @OneToMany(mappedBy = "grant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Resource> resources = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Helper method to add an access token to this grant
     */
    public void addAccessToken(AccessToken accessToken) {
        accessTokens.add(accessToken);
        accessToken.setGrant(this);
    }

    /**
     * Helper method to add an interaction to this grant
     */
    public void addInteraction(Interaction interaction) {
        interactions.add(interaction);
        interaction.setGrant(this);
    }

    /**
     * Helper method to add a resource to this grant
     */
    public void addResource(Resource resource) {
        resources.add(resource);
        resource.setGrant(this);
    }

    /**
     * Enum representing the possible states of a grant request.
     */
    public enum GrantStatus {
        PENDING,
        PROCESSING,
        APPROVED,
        DENIED,
        REVOKED,
        EXPIRED
    }
}
