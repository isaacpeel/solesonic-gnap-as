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
 * Entity representing an access token in the GNAP protocol.
 * Access tokens are used to access protected resources.
 */
@Entity
@Table(name = "access_token")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessToken {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grant_id", nullable = false)
    private GrantRequest grant;

    @Column(name = "token_value", nullable = false)
    private String tokenValue;

    @Column(name = "access_type", nullable = false)
    private String accessType;

    @Column(name = "resource_server")
    private String resourceServer;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}