package com.example.gnap.as.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * Entity representing a client in the GNAP protocol.
 * A client is an application that requests access to resources.
 */
@Entity
@Table(name = "client")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Client {

    @Id
    private String id;

    @Column(name = "instance_id")
    @JsonProperty("instance_id")
    private String instanceId;

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

    @JsonProperty("display")
    private transient Display display;

    public Client() {
    }

    public Client(String id, String instanceId, String displayName, String keyId, String keyJwk, 
                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.instanceId = instanceId;
        this.displayName = displayName;
        this.keyId = keyId;
        this.keyJwk = keyJwk;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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

    public Display getDisplay() {
        return display;
    }

    @SuppressWarnings("unused")
    public void setDisplay(Display display) {
        this.display = display;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return Objects.equals(id, client.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Client{" +
                "id='" + id + '\'' +
                ", instanceId='" + instanceId + '\'' +
                ", displayName='" + displayName + '\'' +
                ", keyId='" + keyId + '\'' +
                '}';
    }

    /**
     * Inner class for client display information.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Display {
        private String name;
        private String uri;

        @JsonProperty("logo_uri")
        private String logoUri;

        @SuppressWarnings("unused")
        public Display() {
        }

        @SuppressWarnings("unused")
        public Display(String name, String uri, String logoUri) {
            this.name = name;
            this.uri = uri;
            this.logoUri = logoUri;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @SuppressWarnings("unused")
        public String getUri() {
            return uri;
        }

        @SuppressWarnings("unused")
        public void setUri(String uri) {
            this.uri = uri;
        }

        @SuppressWarnings("unused")
        public String getLogoUri() {
            return logoUri;
        }

        @SuppressWarnings("unused")
        public void setLogoUri(String logoUri) {
            this.logoUri = logoUri;
        }
    }
}
