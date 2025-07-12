package com.example.gnap.as.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Entity representing an access token in the GNAP protocol.
 * Access tokens are used to access protected resources.
 */
@Entity
@Table(name = "access_token")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccessToken {

    @Id
    @JsonIgnore
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grant_id", nullable = false)
    @JsonIgnore
    private GrantRequest grant;

    @Column(name = "token_value", nullable = false)
    @JsonProperty("value")
    private String tokenValue;

    @Column(name = "access_type", nullable = false)
    @JsonIgnore
    private String accessType;

    @Column(name = "resource_server")
    @JsonIgnore
    private String resourceServer;

    @Column(name = "expires_at", nullable = false)
    @JsonIgnore
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonIgnore
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    @JsonIgnore
    private LocalDateTime updatedAt;

    // Transient fields for API
    @JsonProperty("access")
    private transient List<Resource> access;

    @JsonProperty("label")
    private transient String label;

    @JsonProperty("parameters")
    private transient Map<String, Object> parameters;

    public AccessToken() {
    }

    public AccessToken(String id, GrantRequest grant, String tokenValue, String accessType, 
                      String resourceServer, LocalDateTime expiresAt, 
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.grant = grant;
        this.tokenValue = tokenValue;
        this.accessType = accessType;
        this.resourceServer = resourceServer;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public GrantRequest getGrant() {
        return grant;
    }

    public void setGrant(GrantRequest grant) {
        this.grant = grant;
    }

    public String getTokenValue() {
        return tokenValue;
    }

    public void setTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
    }

    public String getAccessType() {
        return accessType;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }

    public String getResourceServer() {
        return resourceServer;
    }

    public void setResourceServer(String resourceServer) {
        this.resourceServer = resourceServer;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    @JsonProperty("expires_in")
    public Integer getExpiresIn() {
        if (expiresAt == null) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        return (int) (expiresAt.toEpochSecond(ZoneOffset.UTC) - now.toEpochSecond(ZoneOffset.UTC));
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

    public List<Resource> getAccess() {
        return access;
    }

    public void setAccess(List<Resource> access) {
        this.access = access;
    }

    public void addAccess(Resource resource) {
        if (this.access == null) {
            this.access = new ArrayList<>();
        }
        this.access.add(resource);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessToken that = (AccessToken) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AccessToken{" +
                "id='" + id + '\'' +
                ", tokenValue='" + tokenValue + '\'' +
                ", accessType='" + accessType + '\'' +
                ", resourceServer='" + resourceServer + '\'' +
                '}';
    }
}
