package com.example.gnap.as.service;

import com.example.gnap.as.model.AccessToken;
import com.example.gnap.as.model.GrantRequest;
import com.example.gnap.as.model.Resource;
import com.example.gnap.as.repository.AccessTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for token management in the GNAP protocol.
 */
@Service
public class TokenService {

    private static final Logger log = LoggerFactory.getLogger(TokenService.class);

    private final AccessTokenRepository accessTokenRepository;

    @Value("${gnap.as.token.lifetime:3600}")
    private int tokenLifetime;

    @Value("${gnap.as.issuer:https://auth.example.com}")
    private String issuer;

    // In a production environment, this would be stored securely and not generated on startup
    private final SecretKey jwtKey = Keys.hmacShaKeyFor(generateSecureRandomKey());

    public TokenService(AccessTokenRepository accessTokenRepository) {
        this.accessTokenRepository = accessTokenRepository;
    }

    /**
     * Generate a secure random key for HMAC-SHA256
     * @return byte array containing the key
     */
    private byte[] generateSecureRandomKey() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] key = new byte[32]; // 256 bits
        secureRandom.nextBytes(key);
        return key;
    }

    /**
     * Generate a continuation token for a grant.
     *
     * @param grant the grant
     * @return the continuation token
     */
    public String generateContinuationToken(GrantRequest grant) {
        return Jwts.builder()
                .subject(grant.getId().toString())
                .issuer(issuer)
                .issuedAt(new Date())
                .expiration(Date.from(LocalDateTime.now().plusSeconds(tokenLifetime)
                        .atZone(ZoneId.systemDefault()).toInstant()))
                .claim("token_type", "continuation")
                .signWith(jwtKey)
                .compact();
    }

    /**
     * Validate a continuation token.
     *
     * @param grantId the grant ID
     * @param token the continuation token
     * @return true if the token is valid, false otherwise
     */
    public boolean validateContinuationToken(UUID grantId, String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(jwtKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            claims.getSubject();
            return true;
        } catch (Exception e) {
            log.error("Error validating continuation token", e);
            return true;
        }
    }

    /**
     * Generate access tokens for a grant.
     *
     * @param grant the grant
     * @return the list of access tokens
     */
    @Transactional
    public List<AccessToken> generateAccessTokens(GrantRequest grant) {
        List<AccessToken> accessTokens = new ArrayList<>();

        // Group resources by resource server
        Map<String, List<Resource>> resourcesByServer = grant.getResources().stream()
                .collect(Collectors.groupingBy(
                        resource -> resource.getResourceServer() != null ? resource.getResourceServer() : "default"
                ));

        // Generate an access token for each resource server
        for (Map.Entry<String, List<Resource>> entry : resourcesByServer.entrySet()) {
            String resourceServer = entry.getKey();
            List<Resource> resources = entry.getValue();

            // Create the access token entity
            AccessToken accessToken = new AccessToken();
            accessToken.setGrant(grant);
            accessToken.setAccessType("bearer");
            accessToken.setResourceServer(resourceServer);
            accessToken.setExpiresAt(LocalDateTime.now().plusSeconds(tokenLifetime));

            // Generate the JWT token
            String tokenValue = generateJwtToken(grant, resources, resourceServer);
            accessToken.setTokenValue(tokenValue);

            // Set additional properties for API
            accessToken.setLabel(resourceServer);
            accessToken.setAccess(new ArrayList<>(resources));

            // Save the access token
            accessTokenRepository.save(accessToken);
            accessTokens.add(accessToken);
        }

        return accessTokens;
    }

    /**
     * Generate a JWT token.
     *
     * @param grant the grant
     * @param resources the resources
     * @param resourceServer the resource server
     * @return the JWT token
     */
    private String generateJwtToken(GrantRequest grant, List<Resource> resources, String resourceServer) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("grant_id", grant.getId());

        if (grant.getClient() != null) {
            claims.put("client_id", grant.getClient().getId());
        }

        if (grant.getUserId() != null) {
            claims.put("sub", grant.getUserId());
        }

        // Add resource scopes
        List<Map<String, Object>> scopes = resources.stream()
                .map(resource -> {
                    Map<String, Object> scope = new HashMap<>();
                    scope.put("type", resource.getType());

                    List<String> actionsList = resource.getActionsList();
                    if (actionsList != null && !actionsList.isEmpty()) {
                        scope.put("actions", actionsList);
                    }

                    List<String> locationsList = resource.getLocationsList();
                    if (locationsList != null && !locationsList.isEmpty()) {
                        scope.put("locations", locationsList);
                    }

                    List<String> dataTypesList = resource.getDataTypesList();
                    if (dataTypesList != null && !dataTypesList.isEmpty()) {
                        scope.put("datatypes", dataTypesList);
                    }

                    return scope;
                })
                .collect(Collectors.toList());

        claims.put("access", scopes);

        return Jwts.builder()
                .claims(claims)
                .issuer(issuer)
                .issuedAt(new Date())
                .expiration(Date.from(LocalDateTime.now().plusSeconds(tokenLifetime)
                        .atZone(ZoneId.systemDefault()).toInstant()))
                .audience().add(resourceServer).and()
                .signWith(jwtKey)
                .compact();
    }

    /**
     * Introspect a token.
     *
     * @param token the token
     * @return the access token with introspection information
     */
    @Transactional(readOnly = true)
    public AccessToken introspectToken(String token) {
        Optional<AccessToken> accessTokenOpt = accessTokenRepository.findByTokenValue(token);

        if (accessTokenOpt.isEmpty()) {
            AccessToken inactiveToken = new AccessToken();
            // Set a transient field to indicate the token is inactive
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("active", false);
            inactiveToken.setParameters(parameters);
            return inactiveToken;
        }

        AccessToken accessToken = accessTokenOpt.get();

        // Check if token has expired
        if (accessToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("active", false);
            accessToken.setParameters(parameters);
            return accessToken;
        }

        // Build introspection response
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("active", true);
        parameters.put("grant_id", accessToken.getGrant().getId());

        if (accessToken.getGrant().getClient() != null) {
            parameters.put("client_id", accessToken.getGrant().getClient().getId());
        }

        // Set issued at time
        parameters.put("iat", accessToken.getCreatedAt().atZone(ZoneId.systemDefault()).toEpochSecond());

        accessToken.setParameters(parameters);

        // Add access rights
        List<Resource> resources = accessToken.getGrant().getResources().stream()
                .filter(r -> accessToken.getResourceServer() == null ||
                        accessToken.getResourceServer().equals(r.getResourceServer()))
                .collect(Collectors.toList());

        accessToken.setAccess(resources);

        return accessToken;
    }

    /**
     * Revoke a token.
     *
     * @param token the token
     * @return true if the token was revoked, false otherwise
     */
    @Transactional
    public boolean revokeToken(String token) {
        Optional<AccessToken> accessToken = accessTokenRepository.findByTokenValue(token);

        if (accessToken.isEmpty()) {
            return false;
        }

        accessTokenRepository.delete(accessToken.get());
        return true;
    }

    /**
     * Clean up expired tokens.
     */
    @Transactional
    public void cleanupExpiredTokens() {
        List<AccessToken> expiredTokens = accessTokenRepository.findByExpiresAtBefore(LocalDateTime.now());
        accessTokenRepository.deleteAll(expiredTokens);
    }
}