package com.example.gnap.as.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Entity representing a resource in the GNAP protocol.
 * Resources are what clients request access to.
 */
@Entity
@Table(name = "resource")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Resource {

    @Id
    @JsonIgnore
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grant_id", nullable = false)
    @JsonIgnore
    private GrantRequest grant;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "resource_server")
    @JsonProperty("resource_server")
    private String resourceServer;

    @Column(name = "actions")
    @JsonIgnore
    private String actions;

    @Column(name = "locations")
    @JsonIgnore
    private String locations;

    @Column(name = "data_types")
    @JsonIgnore
    private String dataTypes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonIgnore
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    @JsonIgnore
    private LocalDateTime updatedAt;

    public Resource() {
    }

    public Resource(String id, GrantRequest grant, String type, String resourceServer, 
                   String actions, String locations, String dataTypes, 
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.grant = grant;
        this.type = type;
        this.resourceServer = resourceServer;
        this.actions = actions;
        this.locations = locations;
        this.dataTypes = dataTypes;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getResourceServer() {
        return resourceServer;
    }

    public void setResourceServer(String resourceServer) {
        this.resourceServer = resourceServer;
    }

    public String getActions() {
        return actions;
    }

    public void setActions(String actions) {
        this.actions = actions;
    }

    @JsonProperty("actions")
    public List<String> getActionsList() {
        if (actions == null || actions.isEmpty()) {
            return null;
        }
        return Arrays.stream(actions.split(","))
                .collect(Collectors.toList());
    }

    public void setActionsList(List<String> actionsList) {
        if (actionsList == null || actionsList.isEmpty()) {
            this.actions = null;
        } else {
            this.actions = String.join(",", actionsList);
        }
    }

    public String getLocations() {
        return locations;
    }

    public void setLocations(String locations) {
        this.locations = locations;
    }

    @JsonProperty("locations")
    public List<String> getLocationsList() {
        if (locations == null || locations.isEmpty()) {
            return null;
        }
        return Arrays.stream(locations.split(","))
                .collect(Collectors.toList());
    }

    public void setLocationsList(List<String> locationsList) {
        if (locationsList == null || locationsList.isEmpty()) {
            this.locations = null;
        } else {
            this.locations = String.join(",", locationsList);
        }
    }

    public String getDataTypes() {
        return dataTypes;
    }

    public void setDataTypes(String dataTypes) {
        this.dataTypes = dataTypes;
    }

    @JsonProperty("datatypes")
    public List<String> getDataTypesList() {
        if (dataTypes == null || dataTypes.isEmpty()) {
            return null;
        }
        return Arrays.stream(dataTypes.split(","))
                .collect(Collectors.toList());
    }

    public void setDataTypesList(List<String> dataTypesList) {
        if (dataTypesList == null || dataTypesList.isEmpty()) {
            this.dataTypes = null;
        } else {
            this.dataTypes = String.join(",", dataTypesList);
        }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Resource resource = (Resource) o;
        return Objects.equals(id, resource.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Resource{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", resourceServer='" + resourceServer + '\'' +
                '}';
    }
}
