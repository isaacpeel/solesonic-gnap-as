package com.example.gnap.as.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing an interaction in the GNAP protocol.
 * Interactions are used for user consent and authentication.
 */
@Entity
@Table(name = "interaction")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Interaction {

    @Id
    @JsonIgnore
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grant_id", nullable = false)
    @JsonIgnore
    private GrantRequest grant;

    @Column(name = "interaction_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @JsonIgnore
    private InteractionType interactionType;

    @Column(name = "interaction_url")
    @JsonIgnore
    private String interactionUrl;

    @Column(name = "nonce")
    @JsonIgnore
    private String nonce;

    @Column(name = "hash_method")
    @JsonIgnore
    private String hashMethod;

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

    public Interaction() {
    }

    public Interaction(String id, GrantRequest grant, InteractionType interactionType, 
                      String interactionUrl, String nonce, String hashMethod, 
                      LocalDateTime expiresAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.grant = grant;
        this.interactionType = interactionType;
        this.interactionUrl = interactionUrl;
        this.nonce = nonce;
        this.hashMethod = hashMethod;
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

    public InteractionType getInteractionType() {
        return interactionType;
    }

    public void setInteractionType(InteractionType interactionType) {
        this.interactionType = interactionType;
    }

    public String getInteractionUrl() {
        return interactionUrl;
    }

    public void setInteractionUrl(String interactionUrl) {
        this.interactionUrl = interactionUrl;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getHashMethod() {
        return hashMethod;
    }

    public void setHashMethod(String hashMethod) {
        this.hashMethod = hashMethod;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
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

    /**
     * Get the redirect URL if this is a REDIRECT interaction.
     */
    @JsonProperty("redirect")
    public String getRedirect() {
        if (interactionType == InteractionType.REDIRECT) {
            return interactionUrl;
        }
        return null;
    }

    /**
     * Get the app URL if this is an APP interaction.
     */
    @JsonProperty("app")
    public String getApp() {
        if (interactionType == InteractionType.APP) {
            return interactionUrl;
        }
        return null;
    }

    /**
     * Get the user code information if this is a USER_CODE interaction.
     */
    @JsonProperty("user_code")
    public UserCode getUserCode() {
        if (interactionType == InteractionType.USER_CODE) {
            UserCode userCode = new UserCode();
            userCode.setCode(interactionUrl);
            return userCode;
        }
        return null;
    }

    /**
     * Get the finish information.
     */
    @JsonProperty("finish")
    public Finish getFinish() {
        if (hashMethod != null) {
            Finish finish = new Finish();
            finish.setMethod(hashMethod);
            return finish;
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Interaction that = (Interaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Interaction{" +
                "id='" + id + '\'' +
                ", interactionType=" + interactionType +
                ", interactionUrl='" + interactionUrl + '\'' +
                '}';
    }

    /**
     * Enum representing the possible types of interactions.
     */
    public enum InteractionType {
        REDIRECT,
        APP,
        USER_CODE,
        USER_CODE_URI
    }

    /**
     * Inner class for user code information.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UserCode {
        private String code;
        private String uri;

        public UserCode() {
        }

        public UserCode(String code, String uri) {
            this.code = code;
            this.uri = uri;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }
    }

    /**
     * Inner class for finish information.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Finish {
        private String uri;
        private String method;

        public Finish() {
        }

        public Finish(String uri, String method) {
            this.uri = uri;
            this.method = method;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }
    }
}
