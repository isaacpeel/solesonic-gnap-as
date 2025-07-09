package com.example.gnap.as.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a client in the GNAP protocol.
 * A client is an application that requests access to resources.
 */
@Entity
@Table(name = "client")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Client {

    @Id
    private String id;

    @Column(name = "instance_id")
    private String instanceId;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "key_id")
    private String keyId;

    @Column(name = "key_jwk", columnDefinition = "TEXT")
    private String keyJwk;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}