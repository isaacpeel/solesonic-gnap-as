package com.example.gnap.as.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Entity representing a grant request in the GNAP protocol.
 * A grant request is the core concept for authorization in GNAP.
 */
@Entity
@Table(name = "grant_request")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GrantRequest {

    @Id
    @JsonProperty("instance_id")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    @JsonIgnore
    private Client client;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @JsonIgnore
    private GrantStatus status;

    @Column(name = "redirect_uri")
    @JsonIgnore
    private String redirectUri;

    @Column(name = "state")
    @JsonIgnore
    private String state;

    @Column(name = "user_id")
    @JsonIgnore
    private String userId;

    @Column(name = "expires_at", nullable = false)
    @JsonIgnore
    private LocalDateTime expiresAt;

    @OneToMany(mappedBy = "grant", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<AccessToken> accessTokens = new HashSet<>();

    @OneToMany(mappedBy = "grant", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Interaction> interactions = new HashSet<>();

    @OneToMany(mappedBy = "grant", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Resource> resources = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonIgnore
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    @JsonIgnore
    private LocalDateTime updatedAt;

    // Transient fields for API
    @JsonProperty("continue")
    private transient ContinueInfo continueInfo;

    @JsonProperty("interact")
    private transient InteractInfo interactInfo;

    @JsonProperty("access_token")
    private transient List<AccessToken> accessTokenList;

    @JsonProperty("subject")
    private transient Subject subject;

    @JsonProperty("state")
    private transient Map<String, Object> stateMap;

    public GrantRequest() {
    }

    public GrantRequest(String id, Client client, GrantStatus status, String redirectUri, 
                       String state, String userId, LocalDateTime expiresAt, 
                       Set<AccessToken> accessTokens, Set<Interaction> interactions, 
                       Set<Resource> resources, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.client = client;
        this.status = status;
        this.redirectUri = redirectUri;
        this.state = state;
        this.userId = userId;
        this.expiresAt = expiresAt;
        this.accessTokens = accessTokens != null ? accessTokens : new HashSet<>();
        this.interactions = interactions != null ? interactions : new HashSet<>();
        this.resources = resources != null ? resources : new HashSet<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public GrantStatus getStatus() {
        return status;
    }

    public void setStatus(GrantStatus status) {
        this.status = status;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Set<AccessToken> getAccessTokens() {
        return accessTokens;
    }

    public void setAccessTokens(Set<AccessToken> accessTokens) {
        this.accessTokens = accessTokens;
    }

    public Set<Interaction> getInteractions() {
        return interactions;
    }

    public void setInteractions(Set<Interaction> interactions) {
        this.interactions = interactions;
    }

    public Set<Resource> getResources() {
        return resources;
    }

    public void setResources(Set<Resource> resources) {
        this.resources = resources;
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

    public ContinueInfo getContinueInfo() {
        return continueInfo;
    }

    public void setContinueInfo(ContinueInfo continueInfo) {
        this.continueInfo = continueInfo;
    }

    public InteractInfo getInteractInfo() {
        return interactInfo;
    }

    public void setInteractInfo(InteractInfo interactInfo) {
        this.interactInfo = interactInfo;
    }

    public List<AccessToken> getAccessTokenList() {
        return accessTokenList;
    }

    public void setAccessTokenList(List<AccessToken> accessTokenList) {
        this.accessTokenList = accessTokenList;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Map<String, Object> getStateMap() {
        return stateMap;
    }

    public void setStateMap(Map<String, Object> stateMap) {
        this.stateMap = stateMap;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GrantRequest that = (GrantRequest) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "GrantRequest{" +
                "id='" + id + '\'' +
                ", status=" + status +
                ", redirectUri='" + redirectUri + '\'' +
                ", expiresAt=" + expiresAt +
                '}';
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

    /**
     * Inner class for continuation information.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ContinueInfo {
        private String uri;

        @JsonProperty("access_token")
        private String accessToken;

        private Integer wait;

        public ContinueInfo() {
        }

        public ContinueInfo(String uri, String accessToken, Integer wait) {
            this.uri = uri;
            this.accessToken = accessToken;
            this.wait = wait;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public Integer getWait() {
            return wait;
        }

        public void setWait(Integer wait) {
            this.wait = wait;
        }
    }

    /**
     * Inner class for interaction information.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class InteractInfo {
        private String redirect;
        private String app;

        @JsonProperty("user_code")
        private Interaction.UserCode userCode;

        private Interaction.Finish finish;

        public InteractInfo() {
        }

        public InteractInfo(String redirect, String app, Interaction.UserCode userCode, Interaction.Finish finish) {
            this.redirect = redirect;
            this.app = app;
            this.userCode = userCode;
            this.finish = finish;
        }

        public String getRedirect() {
            return redirect;
        }

        public void setRedirect(String redirect) {
            this.redirect = redirect;
        }

        public String getApp() {
            return app;
        }

        public void setApp(String app) {
            this.app = app;
        }

        public Interaction.UserCode getUserCode() {
            return userCode;
        }

        public void setUserCode(Interaction.UserCode userCode) {
            this.userCode = userCode;
        }

        public Interaction.Finish getFinish() {
            return finish;
        }

        public void setFinish(Interaction.Finish finish) {
            this.finish = finish;
        }
    }

    /**
     * Inner class for subject information.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Subject {
        @JsonProperty("sub_ids")
        private List<SubjectIdentifier> subjectIdentifiers;

        private Map<String, Object> assertions;

        private Boolean updated;

        public Subject() {
        }

        public Subject(List<SubjectIdentifier> subjectIdentifiers, Map<String, Object> assertions, Boolean updated) {
            this.subjectIdentifiers = subjectIdentifiers;
            this.assertions = assertions;
            this.updated = updated;
        }

        public List<SubjectIdentifier> getSubjectIdentifiers() {
            return subjectIdentifiers;
        }

        public void setSubjectIdentifiers(List<SubjectIdentifier> subjectIdentifiers) {
            this.subjectIdentifiers = subjectIdentifiers;
        }

        public Map<String, Object> getAssertions() {
            return assertions;
        }

        public void setAssertions(Map<String, Object> assertions) {
            this.assertions = assertions;
        }

        public Boolean getUpdated() {
            return updated;
        }

        public void setUpdated(Boolean updated) {
            this.updated = updated;
        }
    }

    /**
     * Inner class for subject identifier information.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SubjectIdentifier {
        private String format;
        private String id;

        public SubjectIdentifier() {
        }

        public SubjectIdentifier(String format, String id) {
            this.format = format;
            this.id = id;
        }

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
