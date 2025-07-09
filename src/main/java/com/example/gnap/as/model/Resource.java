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
 * Entity representing a resource in the GNAP protocol.
 * Resources are what clients request access to.
 */
@Entity
@Table(name = "resource")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Resource {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grant_id", nullable = false)
    private GrantRequest grant;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "resource_server")
    private String resourceServer;

    @Column(name = "actions")
    private String actions;

    @Column(name = "locations")
    private String locations;

    @Column(name = "data_types")
    private String dataTypes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}