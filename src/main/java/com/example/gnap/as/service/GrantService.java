package com.example.gnap.as.service;

import com.example.gnap.as.dto.GrantRequestDto;
import com.example.gnap.as.dto.GrantResponseDto;
import com.example.gnap.as.model.*;
import com.example.gnap.as.repository.GrantRequestRepository;
import com.example.gnap.as.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for grant management in the GNAP protocol.
 */
@Service
@Slf4j
public class GrantService {

    private final GrantRequestRepository grantRequestRepository;
    private final ResourceRepository resourceRepository;
    private final ClientService clientService;
    private final InteractionService interactionService;
    private final TokenService tokenService;

    public GrantService(
            GrantRequestRepository grantRequestRepository,
            ResourceRepository resourceRepository,
            ClientService clientService,
            @Lazy InteractionService interactionService,
            @Lazy TokenService tokenService) {
        this.grantRequestRepository = grantRequestRepository;
        this.resourceRepository = resourceRepository;
        this.clientService = clientService;
        this.interactionService = interactionService;
        this.tokenService = tokenService;
    }

    @Value("${gnap.as.token.lifetime:3600}")
    private int tokenLifetime;

    @Value("${gnap.as.interaction.timeout:300}")
    private int interactionTimeout;

    /**
     * Process a grant request.
     *
     * @param requestDto the grant request DTO
     * @return the grant response DTO
     */
    @Transactional
    public GrantResponseDto processGrantRequest(GrantRequestDto requestDto) {
        // Authenticate client if provided
        Client client = null;
        if (requestDto.getClient() != null) {
            if (!clientService.authenticateClient(requestDto.getClient())) {
                throw new IllegalArgumentException("Client authentication failed");
            }
            client = clientService.registerClient(requestDto.getClient());
        }

        // Create grant request
        GrantRequest grant = createGrantRequest(requestDto, client);

        // Create resources
        if (requestDto.getAccess() != null) {
            for (GrantRequestDto.AccessDto accessDto : requestDto.getAccess()) {
                Resource resource = createResource(accessDto, grant);
                grant.addResource(resource);
            }
        }

        // Create interactions if needed
        List<Interaction> interactions = new ArrayList<>();
        if (requestDto.getInteract() != null) {
            interactions = interactionService.createInteractions(requestDto.getInteract(), grant);
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
     * Create a grant request entity from a DTO.
     *
     * @param requestDto the grant request DTO
     * @param client the client
     * @return the grant request entity
     */
    private GrantRequest createGrantRequest(GrantRequestDto requestDto, Client client) {
        GrantRequest grant = new GrantRequest();
        grant.setId(UUID.randomUUID().toString());
        grant.setClient(client);
        grant.setStatus(GrantRequest.GrantStatus.PENDING);

        // Set expiration time
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(tokenLifetime);
        grant.setExpiresAt(expiresAt);

        // Set redirect URI if provided in interact
        if (requestDto.getInteract() != null && requestDto.getInteract().getRedirect() != null) {
            grant.setRedirectUri(requestDto.getInteract().getRedirect().getUri());
        }

        // Set state if provided
        if (requestDto.getState() != null) {
            grant.setState(requestDto.getState().toString());
        }

        return grant;
    }

    /**
     * Create a resource entity from an access DTO.
     *
     * @param accessDto the access DTO
     * @param grant the grant request
     * @return the resource entity
     */
    private Resource createResource(GrantRequestDto.AccessDto accessDto, GrantRequest grant) {
        Resource resource = new Resource();
        resource.setId(UUID.randomUUID().toString());
        resource.setGrant(grant);
        resource.setType(accessDto.getType());
        resource.setResourceServer(accessDto.getResourceServer());

        // Convert lists to comma-separated strings
        if (accessDto.getActions() != null) {
            resource.setActions(String.join(",", accessDto.getActions()));
        }

        if (accessDto.getLocations() != null) {
            resource.setLocations(String.join(",", accessDto.getLocations()));
        }

        if (accessDto.getDataTypes() != null) {
            resource.setDataTypes(String.join(",", accessDto.getDataTypes()));
        }

        return resource;
    }

    /**
     * Build a grant response DTO from a grant request entity.
     *
     * @param grant the grant request entity
     * @param interactions the interactions
     * @return the grant response DTO
     */
    private GrantResponseDto buildGrantResponse(GrantRequest grant, List<Interaction> interactions) {
        GrantResponseDto responseDto = new GrantResponseDto();
        responseDto.setInstanceId(grant.getId());

        // Add continue information
        GrantResponseDto.ContinueDto continueDto = new GrantResponseDto.ContinueDto();
        continueDto.setUri("/gnap/grant/" + grant.getId());
        continueDto.setAccess_token(tokenService.generateContinuationToken(grant));
        continueDto.setWait(5); // Suggest client to wait 5 seconds before polling
        responseDto.setContinue_(continueDto);

        // Add interaction information if needed
        if (!interactions.isEmpty()) {
            responseDto.setInteract(interactionService.buildInteractResponse(interactions));
        }

        // Add access tokens if grant is approved
        if (grant.getStatus() == GrantRequest.GrantStatus.APPROVED) {
            responseDto.setAccess_token(tokenService.generateAccessTokens(grant));
        }

        return responseDto;
    }

    /**
     * Find a grant by its ID.
     *
     * @param grantId the grant ID
     * @return the grant if found
     */
    @Transactional(readOnly = true)
    public Optional<GrantRequest> findById(String grantId) {
        return grantRequestRepository.findById(grantId);
    }

    /**
     * Update a grant's status.
     *
     * @param grantId the grant ID
     * @param status the new status
     * @return the updated grant
     */
    @Transactional
    public GrantRequest updateGrantStatus(String grantId, GrantRequest.GrantStatus status) {
        GrantRequest grant = grantRequestRepository.findById(grantId)
                .orElseThrow(() -> new IllegalArgumentException("Grant not found: " + grantId));

        grant.setStatus(status);
        return grantRequestRepository.save(grant);
    }

    /**
     * Process a continuation request.
     *
     * @param grantId the grant ID
     * @param continuationToken the continuation token
     * @return the grant response DTO
     */
    @Transactional
    public GrantResponseDto processContinuation(String grantId, String continuationToken) {
        // Validate continuation token
        if (!tokenService.validateContinuationToken(grantId, continuationToken)) {
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
