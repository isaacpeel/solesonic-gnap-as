package com.example.gnap.as.repository;

import com.example.gnap.as.model.AccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for AccessToken entity.
 */
@Repository
public interface AccessTokenRepository extends JpaRepository<AccessToken, String> {
    
    /**
     * Find an access token by its token value.
     *
     * @param tokenValue the token value
     * @return the access token if found
     */
    Optional<AccessToken> findByTokenValue(String tokenValue);
    
    /**
     * Find access tokens by grant ID.
     *
     * @param grantId the grant ID
     * @return the list of access tokens
     */
    @SuppressWarnings("unused")
    List<AccessToken> findByGrantId(UUID grantId);
    
    /**
     * Find access tokens by resource server.
     *
     * @param resourceServer the resource server
     * @return the list of access tokens
     */
    @SuppressWarnings("unused")
    List<AccessToken> findByResourceServer(String resourceServer);
    
    /**
     * Find access tokens that have expired.
     *
     * @param now the current time
     * @return the list of expired access tokens
     */
    List<AccessToken> findByExpiresAtBefore(LocalDateTime now);
    
    /**
     * Find access tokens by access type.
     *
     * @param accessType the access type
     * @return the list of access tokens
     */
    @SuppressWarnings("unused")
    List<AccessToken> findByAccessType(String accessType);
}