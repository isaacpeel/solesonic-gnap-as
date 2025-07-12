package com.example.gnap.as.service;

import com.example.gnap.as.model.Client;
import com.example.gnap.as.repository.ClientRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for client management in the GNAP protocol.
 */
@Service
public class ClientService {

    private static final Logger log = LoggerFactory.getLogger(ClientService.class);

    private final ClientRepository clientRepository;
    private final ObjectMapper objectMapper;

    public ClientService(ClientRepository clientRepository, ObjectMapper objectMapper) {
        this.clientRepository = clientRepository;
        this.objectMapper = objectMapper;
    }

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
     * @param client the client information
     * @return the registered client
     */
    @Transactional
    public Client registerClient(Client client) {
        // Check if client already exists by instance ID
        Optional<Client> existingClient = Optional.empty();
        if (client.getInstanceId() != null) {
            existingClient = findByInstanceId(client.getInstanceId());
        }

        // Check if client exists by key ID
        if (existingClient.isEmpty() && client.getKeyId() != null) {
            existingClient = findByKeyId(client.getKeyId());
        }

        // Update existing client or create new one
        Client clientToSave;
        if (existingClient.isPresent()) {
            clientToSave = existingClient.get();
            updateClient(clientToSave, client);
        } else {
            clientToSave = createClient(client);
        }

        return clientRepository.save(clientToSave);
    }

    /**
     * Create a new client.
     *
     * @param client the client information
     * @return the new client
     */
    private Client createClient(Client client) {
        Client newClient = new Client();
        newClient.setId(UUID.randomUUID());
        updateClient(newClient, client);
        return newClient;
    }

    /**
     * Update a client.
     *
     * @param clientToUpdate the client to update
     * @param clientData the client data
     */
    private void updateClient(Client clientToUpdate, Client clientData) {
        clientToUpdate.setInstanceId(clientData.getInstanceId());
        clientToUpdate.setKeyId(clientData.getKeyId());

        // Store JWK as JSON string if present
        if (clientData.getKey() != null) {
            try {
                clientToUpdate.setKeyJwk(objectMapper.writeValueAsString(clientData.getKey()));
            } catch (Exception e) {
                log.error("Error serializing JWK", e);
            }
        }

        if (clientData.getDisplay() != null) {
            clientToUpdate.setDisplayName(clientData.getDisplay().getName());
        }
    }

    /**
     * Authenticate a client based on its key.
     *
     * @param client the client information
     * @param signedJwt the signed JWT to verify (can be null if no signature verification is needed)
     * @return true if the client is authenticated, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean authenticateClient(Client client, String signedJwt) {
        // Check if client has a key ID
        if (client.getKeyId() == null) {
            log.warn("Client authentication failed: No key ID provided");
            return false;
        }

        // Find the client in the database
        Optional<Client> storedClient = findByKeyId(client.getKeyId());
        if (storedClient.isEmpty()) {
            log.warn("Client authentication failed: No client found with key ID {}", client.getKeyId());
            return false;
        }

        // If no signed JWT is provided, just check if the client exists
        if (signedJwt == null || signedJwt.isEmpty()) {
            log.info("Client authenticated by existence check only (no signature verification)");
            return true;
        }

        // Verify the signature using the client's JWK
        try {
            // Parse the stored JWK
            String keyJwk = storedClient.get().getKeyJwk();
            if (keyJwk == null || keyJwk.isEmpty()) {
                log.warn("Client authentication failed: No JWK found for client with key ID {}", client.getKeyId());
                return false;
            }

            JWK jwk = JWK.parse(keyJwk);

            // Parse the signed JWT
            SignedJWT jwt = SignedJWT.parse(signedJwt);

            // Verify that the JWT was signed with the key identified by the key ID
            JWSHeader header = jwt.getHeader();
            if (!client.getKeyId().equals(header.getKeyID())) {
                log.warn("Client authentication failed: JWT key ID {} does not match client key ID {}", 
                         header.getKeyID(), client.getKeyId());
                return false;
            }

            // Create a verifier for the JWK based on its type
            JWSVerifier verifier;
            try {
                // Get the key type
                String keyType = jwk.getKeyType().getValue();

                // Create the appropriate verifier based on key type
                switch (keyType) {
                    case "RSA" -> verifier = new DefaultJWSVerifierFactory().createJWSVerifier(
                            header,
                            jwk.toRSAKey().toRSAPublicKey());
                    case "EC" -> verifier = new DefaultJWSVerifierFactory().createJWSVerifier(
                            header,
                            jwk.toECKey().toECPublicKey());
                    case "OKP" -> verifier = new DefaultJWSVerifierFactory().createJWSVerifier(
                            header,
                            jwk.toOctetKeyPair().toPublicKey());
                    case null, default -> {
                        log.warn("Client authentication failed: Unsupported key type: {}", keyType);
                        return false;
                    }
                }
            } catch (JOSEException e) {
                log.error("Client authentication failed: Error creating verifier for key type", e);
                return false;
            }

            // Verify the signature
            boolean verified = jwt.verify(verifier);
            if (verified) {
                log.info("Client authenticated successfully with signature verification");
            } else {
                log.warn("Client authentication failed: Invalid signature");
            }
            return verified;

        } catch (ParseException e) {
            log.error("Client authentication failed: Error parsing JWK or JWT", e);
            return false;
        } catch (JOSEException e) {
            log.error("Client authentication failed: Error verifying signature", e);
            return false;
        }
    }

    /**
     * Authenticate a client based on its key (simplified version that only checks existence).
     *
     * @param client the client information
     * @return true if the client is authenticated, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean authenticateClient(Client client) {
        return authenticateClient(client, null);
    }
}
