package com.example.gnap.as.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing client information in the GNAP protocol.
 * This was refactored from the inner Display class of Client.
 */
@Entity
@Table(name = "client_information")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private UUID clientId;

    @Column(name = "name")
    private String name;

    @Column(name = "uri")
    private String uri;

    @Column(name = "logo_uri")
    @JsonProperty("logo_uri")
    private String logoUri;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public ClientInformation() {
    }

    public ClientInformation(String name, String uri, String logoUri) {
        this.name = name;
        this.uri = uri;
        this.logoUri = logoUri;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getClientId() {
        return clientId;
    }

    public void setClientId(UUID clientId) {
        this.clientId = clientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getLogoUri() {
        return logoUri;
    }

    public void setLogoUri(String logoUri) {
        this.logoUri = logoUri;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}