package com.example.gnap.as.repository;

import com.example.gnap.as.model.GrantRequest;
import com.example.gnap.as.model.GrantRequest.GrantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for GrantRequest entity.
 */
@Repository
public interface GrantRequestRepository extends JpaRepository<GrantRequest, String> {
    
    /**
     * Find grants by client ID.
     *
     * @param clientId the client ID
     * @return the list of grants
     */
    List<GrantRequest> findByClientId(String clientId);
    
    /**
     * Find grants by status.
     *
     * @param status the grant status
     * @return the list of grants
     */
    List<GrantRequest> findByStatus(GrantStatus status);
    
    /**
     * Find grants by user ID.
     *
     * @param userId the user ID
     * @return the list of grants
     */
    List<GrantRequest> findByUserId(String userId);
    
    /**
     * Find grants that have expired.
     *
     * @param now the current time
     * @return the list of expired grants
     */
    List<GrantRequest> findByExpiresAtBefore(LocalDateTime now);
    
    /**
     * Find active grants by client ID and user ID.
     *
     * @param clientId the client ID
     * @param userId the user ID
     * @param statuses the list of active statuses
     * @return the list of active grants
     */
    @Query("SELECT g FROM GrantRequest g WHERE g.client.id = ?1 AND g.userId = ?2 AND g.status IN ?3")
    List<GrantRequest> findActiveGrantsByClientIdAndUserId(String clientId, String userId, List<GrantStatus> statuses);
}