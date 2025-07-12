package com.example.gnap.as.service;

import com.example.gnap.as.model.Client;
import com.example.gnap.as.model.GrantRequest;
import com.example.gnap.as.model.Interaction;
import com.example.gnap.as.model.Resource;
import com.example.gnap.as.repository.GrantRequestRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for grant management in the GNAP protocol.
 */
@Service
public class GrantService {

    private final GrantRequestRepository grantRequestRepository;
    private final ClientService clientService;
    private final InteractionService interactionService;
    private final TokenService tokenService;

    public GrantService(
            GrantRequestRepository grantRequestRepository,
            @Lazy ClientService clientService,
            @Lazy InteractionService interactionService,
            @Lazy TokenService tokenService) {
        this.grantRequestRepository = grantRequestRepository;
        this.clientService = clientService;
        this.interactionService = interactionService;
        this.tokenService = tokenService;
    }

    @Value("${gnap.as.token.lifetime:3600}")
    private int tokenLifetime;

    @SuppressWarnings("unused")
    @Value("${gnap.as.interaction.timeout:300}")
    private int interactionTimeout;

    /**
     * Process a grant request.
     *
     * @param request the grant request
     * @return the grant response
     */
    @Transactional
    public GrantRequest processGrantRequest(GrantRequest request) {
        // Authenticate client if provided
        Client client = null;
        if (request.getClient() != null) {
            if (!clientService.authenticateClient(request.getClient())) {
                throw new IllegalArgumentException("Client authentication failed");
            }
            client = clientService.registerClient(request.getClient());
        }

        // Create grant request
        GrantRequest grant = createGrantRequest(request, client);

        // Create resources
        if (request.getResources() != null && !request.getResources().isEmpty()) {
            for (Resource resource : request.getResources()) {
                Resource newResource = createResource(resource, grant);
                grant.addResource(newResource);
            }
        }

        // Create interactions if needed
        List<Interaction> interactions = new ArrayList<>();
        if (request.getInteractInfo() != null) {
            interactions = interactionService.createInteractions(request.getInteractInfo(), grant);
            for (Interaction interaction : interactions) {
                grant.addInteraction(interaction);
            }
        }

        // Save grant
        grant = grantRequestRepository.save(grant);

        // Build response
        return buildGrantResponse(grant, interactions);
    }

    /**
     * Create a grant request entity.
     *
     * @param request the grant request
     * @param client the client
     * @return the grant request entity
     */
    private GrantRequest createGrantRequest(GrantRequest request, Client client) {
        GrantRequest grant = new GrantRequest();
        grant.setId(UUID.randomUUID());
        grant.setClient(client);
        grant.setStatus(GrantRequest.GrantStatus.PENDING);

        // Set expiration time
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(tokenLifetime);
        grant.setExpiresAt(expiresAt);

        // Set redirect URI if provided in interact
        if (request.getInteractInfo() != null && request.getInteractInfo().getRedirect() != null) {
            grant.setRedirectUri(request.getInteractInfo().getRedirect());
        }

        // Set state if provided
        if (request.getStateMap() != null) {
            grant.setState(request.getStateMap().toString());
        }

        return grant;
    }

    /**
     * Create a resource entity.
     *
     * @param resource the resource
     * @param grant the grant request
     * @return the resource entity
     */
    private Resource createResource(Resource resource, GrantRequest grant) {
        Resource newResource = new Resource();
        newResource.setId(UUID.randomUUID());
        newResource.setGrant(grant);
        newResource.setType(resource.getType());
        newResource.setResourceServer(resource.getResourceServer());

        // Set actions, locations, and dataTypes
        newResource.setActionsList(resource.getActionsList());
        newResource.setLocationsList(resource.getLocationsList());
        newResource.setDataTypesList(resource.getDataTypesList());

        return newResource;
    }

    /**
     * Build a grant response from a grant request entity.
     *
     * @param grant the grant request entity
     * @param interactions the interactions
     * @return the grant response
     */
    private GrantRequest buildGrantResponse(GrantRequest grant, List<Interaction> interactions) {
        // Create a new GrantRequest for the response
        GrantRequest response = new GrantRequest();
        response.setId(grant.getId());

        // Add continue information
        GrantRequest.ContinueInfo continueInfo = new GrantRequest.ContinueInfo();
        continueInfo.setUri("/gnap/grant/" + grant.getId());
        continueInfo.setAccessToken(tokenService.generateContinuationToken(grant));
        continueInfo.setWait(5); // Suggest client to wait 5 seconds before polling
        response.setContinueInfo(continueInfo);

        // Add interaction information if needed
        if (!interactions.isEmpty()) {
            response.setInteractInfo(interactionService.buildInteractResponse(interactions));
        }

        // Add access tokens if grant is approved
        if (grant.getStatus() == GrantRequest.GrantStatus.APPROVED) {
            response.setAccessTokenList(tokenService.generateAccessTokens(grant));
        }

        return response;
    }

    /**
     * Find a grant by its ID.
     *
     * @param grantId the grant ID
     * @return the grant if found
     */
    @Transactional(readOnly = true)
    public Optional<GrantRequest> findById(UUID grantId) {
        return grantRequestRepository.findById(grantId);
    }

    /**
     * Update a grant's status.
     *
     * @param grantId the grant ID
     * @param status the new status
     */
    @Transactional
    public void updateGrantStatus(UUID grantId, GrantRequest.GrantStatus status) {
        GrantRequest grant = grantRequestRepository.findById(grantId)
                .orElseThrow(() -> new IllegalArgumentException("Grant not found: " + grantId));

        grant.setStatus(status);
        grantRequestRepository.save(grant);
    }

    /**
     * Process a continuation request.
     *
     * @param grantId the grant ID
     * @param continuationToken the continuation token
     * @return the grant response
     */
    @Transactional
    public GrantRequest processContinuation(UUID grantId, String continuationToken) {
        // Validate continuation token
        if (tokenService.validateContinuationToken(grantId, continuationToken)) {
            throw new IllegalArgumentException("Invalid continuation token");
        }

        GrantRequest grant = grantRequestRepository.findById(grantId)
                .orElseThrow(() -> new IllegalArgumentException("Grant not found: " + grantId));

        // Check if grant has expired
        if (grant.getExpiresAt().isBefore(LocalDateTime.now())) {
            grant.setStatus(GrantRequest.GrantStatus.EXPIRED);
            grantRequestRepository.save(grant);
            throw new IllegalArgumentException("Grant has expired");
        }

        // Build response based on current state
        return buildGrantResponse(grant, new ArrayList<>(grant.getInteractions()));
    }

    /**
     * Clean up expired grants.
     */
    @Transactional
    public void cleanupExpiredGrants() {
        List<GrantRequest> expiredGrants = grantRequestRepository.findByExpiresAtBefore(LocalDateTime.now());
        for (GrantRequest grant : expiredGrants) {
            grant.setStatus(GrantRequest.GrantStatus.EXPIRED);
        }
        grantRequestRepository.saveAll(expiredGrants);
    }
}
