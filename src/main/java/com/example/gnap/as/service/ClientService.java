package com.example.gnap.as.service;

import com.example.gnap.as.dto.GrantRequestDto;
import com.example.gnap.as.model.Client;
import com.example.gnap.as.repository.ClientRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Service for client management in the GNAP protocol.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClientService {

    private final ClientRepository clientRepository;
    private final ObjectMapper objectMapper;

    /**
     * Find a client by its instance ID.
     *
     * @param instanceId the instance ID
     * @return the client if found
     */
    @Transactional(readOnly = true)
    public Optional<Client> findByInstanceId(String instanceId) {
        return clientRepository.findByInstanceId(instanceId);
    }

    /**
     * Find a client by its key ID.
     *
     * @param keyId the key ID
     * @return the client if found
     */
    @Transactional(readOnly = true)
    public Optional<Client> findByKeyId(String keyId) {
        return clientRepository.findByKeyId(keyId);
    }

    /**
     * Register a new client or update an existing one.
     *
     * @param clientDto the client information
     * @return the registered client
     */
    @Transactional
    public Client registerClient(GrantRequestDto.ClientDto clientDto) {
        // Check if client already exists by instance ID
        Optional<Client> existingClient = Optional.empty();
        if (clientDto.getInstanceId() != null) {
            existingClient = findByInstanceId(clientDto.getInstanceId());
        }
        
        // Check if client exists by key ID
        if (existingClient.isEmpty() && clientDto.getKey() != null && clientDto.getKey().getKeyId() != null) {
            existingClient = findByKeyId(clientDto.getKey().getKeyId());
        }
        
        // Update existing client or create new one
        Client client;
        if (existingClient.isPresent()) {
            client = existingClient.get();
            updateClientFromDto(client, clientDto);
        } else {
            client = createClientFromDto(clientDto);
        }
        
        return clientRepository.save(client);
    }

    /**
     * Create a new client from a client DTO.
     *
     * @param clientDto the client DTO
     * @return the new client
     */
    private Client createClientFromDto(GrantRequestDto.ClientDto clientDto) {
        Client client = new Client();
        client.setId(UUID.randomUUID().toString());
        updateClientFromDto(client, clientDto);
        return client;
    }

    /**
     * Update a client from a client DTO.
     *
     * @param client the client to update
     * @param clientDto the client DTO
     */
    private void updateClientFromDto(Client client, GrantRequestDto.ClientDto clientDto) {
        client.setInstanceId(clientDto.getInstanceId());
        
        if (clientDto.getKey() != null) {
            client.setKeyId(clientDto.getKey().getKeyId());
            
            // Store JWK as JSON string if present
            if (clientDto.getKey().getJwk() != null) {
                try {
                    client.setKeyJwk(objectMapper.writeValueAsString(clientDto.getKey().getJwk()));
                } catch (Exception e) {
                    log.error("Error serializing JWK", e);
                }
            }
        }
        
        if (clientDto.getDisplay() != null) {
            client.setDisplayName(clientDto.getDisplay().getName());
        }
    }

    /**
     * Authenticate a client based on its key.
     *
     * @param clientDto the client information
     * @return true if the client is authenticated, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean authenticateClient(GrantRequestDto.ClientDto clientDto) {
        // In a real implementation, this would verify the client's signature
        // using the client's key. For simplicity, we just check if the client exists.
        if (clientDto.getKey() == null || clientDto.getKey().getKeyId() == null) {
            return false;
        }
        
        return findByKeyId(clientDto.getKey().getKeyId()).isPresent();
    }
}