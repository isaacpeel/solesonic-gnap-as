package com.example.gnap.as.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Entity representing a client in the GNAP protocol.
 * A client is an application that requests access to resources.
 */
@Entity
@Table(name = "client")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "instance_id")
    @JsonProperty("instance_id")
    private UUID instanceId;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "key_id")
    @JsonProperty("kid")
    private String keyId;

    @Column(name = "key_jwk", columnDefinition = "TEXT")
    @JsonIgnore
    private String keyJwk;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonIgnore
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    @JsonIgnore
    private LocalDateTime updatedAt;

    // Transient fields for API
    @JsonProperty("key")
    private transient Map<String, Object> key;

    @JsonIgnore
    private transient ClientInformation clientInformation;

    public Client() {
    }

    public Client(UUID id,
                  UUID instanceId,
                  String displayName,
                  String keyId,
                  String keyJwk,
                  LocalDateTime createdAt,
                  LocalDateTime updatedAt) {
        this.id = id;
        this.instanceId = instanceId;
        this.displayName = displayName;
        this.keyId = keyId;
        this.keyJwk = keyJwk;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(UUID instanceId) {
        this.instanceId = instanceId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getKeyJwk() {
        return keyJwk;
    }

    public void setKeyJwk(String keyJwk) {
        this.keyJwk = keyJwk;
    }

    @SuppressWarnings("unused")
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @SuppressWarnings("unused")
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @SuppressWarnings("unused")
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @SuppressWarnings("unused")
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Map<String, Object> getKey() {
        return key;
    }

    @SuppressWarnings("unused")
    public void setKey(Map<String, Object> key) {
        this.key = key;
    }

    /**
     * Get the client information entity.
     * This is a transient field that needs to be populated from the service layer.
     *
     * @return the client information entity
     */
    public ClientInformation getClientInformation() {
        return clientInformation;
    }

    /**
     * Set the client information entity.
     * This is a transient field that will not be persisted with the client.
     *
     * @param clientInformation the client information entity
     */
    public void setClientInformation(ClientInformation clientInformation) {
        this.clientInformation = clientInformation;
    }

}
