package com.example.gnap.as.service;

import com.example.gnap.as.dto.GrantResponseDto;
import com.example.gnap.as.dto.TokenIntrospectionDto;
import com.example.gnap.as.model.AccessToken;
import com.example.gnap.as.model.GrantRequest;
import com.example.gnap.as.model.Resource;
import com.example.gnap.as.repository.AccessTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for token management in the GNAP protocol.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final AccessTokenRepository accessTokenRepository;

    @Value("${gnap.as.token.lifetime:3600}")
    private int tokenLifetime;

    @Value("${gnap.as.issuer:https://auth.example.com}")
    private String issuer;

    // In a production environment, this would be stored securely and not generated on startup
    private final SecretKey jwtKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    /**
     * Generate a continuation token for a grant.
     *
     * @param grant the grant
     * @return the continuation token
     */
    public String generateContinuationToken(GrantRequest grant) {
        return Jwts.builder()
                .setSubject(grant.getId())
                .setIssuer(issuer)
                .setIssuedAt(new Date())
                .setExpiration(Date.from(LocalDateTime.now().plusSeconds(tokenLifetime)
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
    public boolean validateContinuationToken(String grantId, String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            return claims.getSubject().equals(grantId) &&
                   "continuation".equals(claims.get("token_type"));
        } catch (Exception e) {
            log.error("Error validating continuation token", e);
            return false;
        }
    }

    /**
     * Generate access tokens for a grant.
     *
     * @param grant the grant
     * @return the list of access token DTOs
     */
    @Transactional
    public List<GrantResponseDto.AccessTokenDto> generateAccessTokens(GrantRequest grant) {
        List<GrantResponseDto.AccessTokenDto> accessTokenDtos = new ArrayList<>();
        
        // Group resources by resource server
        Map<String, List<Resource>> resourcesByServer = grant.getResources().stream()
                .collect(Collectors.groupingBy(
                        resource -> resource.getResourceServer() != null ? resource.getResourceServer() : "default"
                ));
        
        // Generate an access token for each resource server
        int index = 0;
        for (Map.Entry<String, List<Resource>> entry : resourcesByServer.entrySet()) {
            String resourceServer = entry.getKey();
            List<Resource> resources = entry.getValue();
            
            // Create the access token entity
            AccessToken accessToken = new AccessToken();
            accessToken.setId(UUID.randomUUID().toString());
            accessToken.setGrant(grant);
            accessToken.setAccessType("bearer");
            accessToken.setResourceServer(resourceServer);
            accessToken.setExpiresAt(LocalDateTime.now().plusSeconds(tokenLifetime));
            
            // Generate the JWT token
            String tokenValue = generateJwtToken(grant, resources, resourceServer);
            accessToken.setTokenValue(tokenValue);
            
            // Save the access token
            accessTokenRepository.save(accessToken);
            
            // Create the access token DTO
            GrantResponseDto.AccessTokenDto accessTokenDto = new GrantResponseDto.AccessTokenDto();
            accessTokenDto.setValue(tokenValue);
            accessTokenDto.setIndex(index++);
            accessTokenDto.setLabel(resourceServer);
            accessTokenDto.setExpires_in(tokenLifetime);
            
            // Add access rights to the token
            accessTokenDto.setAccess(resources.stream()
                    .map(this::convertResourceToAccessDto)
                    .collect(Collectors.toList()));
            
            accessTokenDtos.add(accessTokenDto);
        }
        
        return accessTokenDtos;
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
                    
                    if (resource.getActions() != null) {
                        scope.put("actions", Arrays.asList(resource.getActions().split(",")));
                    }
                    
                    if (resource.getLocations() != null) {
                        scope.put("locations", Arrays.asList(resource.getLocations().split(",")));
                    }
                    
                    if (resource.getDataTypes() != null) {
                        scope.put("datatypes", Arrays.asList(resource.getDataTypes().split(",")));
                    }
                    
                    return scope;
                })
                .collect(Collectors.toList());
        
        claims.put("access", scopes);
        
        return Jwts.builder()
                .setClaims(claims)
                .setIssuer(issuer)
                .setIssuedAt(new Date())
                .setExpiration(Date.from(LocalDateTime.now().plusSeconds(tokenLifetime)
                        .atZone(ZoneId.systemDefault()).toInstant()))
                .setAudience(resourceServer)
                .signWith(jwtKey)
                .compact();
    }

    /**
     * Convert a resource entity to an access DTO.
     *
     * @param resource the resource entity
     * @return the access DTO
     */
    private com.example.gnap.as.dto.GrantRequestDto.AccessDto convertResourceToAccessDto(Resource resource) {
        com.example.gnap.as.dto.GrantRequestDto.AccessDto accessDto = new com.example.gnap.as.dto.GrantRequestDto.AccessDto();
        accessDto.setType(resource.getType());
        accessDto.setResourceServer(resource.getResourceServer());
        
        if (resource.getActions() != null) {
            accessDto.setActions(Arrays.asList(resource.getActions().split(",")));
        }
        
        if (resource.getLocations() != null) {
            accessDto.setLocations(Arrays.asList(resource.getLocations().split(",")));
        }
        
        if (resource.getDataTypes() != null) {
            accessDto.setDataTypes(Arrays.asList(resource.getDataTypes().split(",")));
        }
        
        return accessDto;
    }

    /**
     * Introspect a token.
     *
     * @param token the token
     * @return the token introspection DTO
     */
    @Transactional(readOnly = true)
    public TokenIntrospectionDto introspectToken(String token) {
        Optional<AccessToken> accessToken = accessTokenRepository.findByTokenValue(token);
        
        if (accessToken.isEmpty()) {
            TokenIntrospectionDto introspectionDto = new TokenIntrospectionDto();
            introspectionDto.setActive(false);
            return introspectionDto;
        }
        
        AccessToken at = accessToken.get();
        
        // Check if token has expired
        if (at.getExpiresAt().isBefore(LocalDateTime.now())) {
            TokenIntrospectionDto introspectionDto = new TokenIntrospectionDto();
            introspectionDto.setActive(false);
            return introspectionDto;
        }
        
        // Build introspection response
        TokenIntrospectionDto introspectionDto = new TokenIntrospectionDto();
        introspectionDto.setActive(true);
        introspectionDto.setGrantId(at.getGrant().getId());
        
        if (at.getGrant().getClient() != null) {
            introspectionDto.setClientId(at.getGrant().getClient().getId());
        }
        
        // Calculate expires_in in seconds
        long expiresIn = at.getExpiresAt().atZone(ZoneId.systemDefault()).toEpochSecond() -
                LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond();
        introspectionDto.setExpiresIn((int) expiresIn);
        
        // Set issued at time
        introspectionDto.setIssuedAt(at.getCreatedAt().atZone(ZoneId.systemDefault()).toEpochSecond());
        
        // Add access rights
        List<Resource> resources = at.getGrant().getResources().stream()
                .filter(r -> at.getResourceServer() == null ||
                        at.getResourceServer().equals(r.getResourceServer()))
                .collect(Collectors.toList());
        
        introspectionDto.setAccess(resources.stream()
                .map(this::convertResourceToAccessDto)
                .collect(Collectors.toList()));
        
        return introspectionDto;
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