package com.example.gnap.as.service;

import com.example.gnap.as.model.Client;
import com.example.gnap.as.model.ClientInformation;
import com.example.gnap.as.repository.ClientRepository;
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
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Service for client management in the GNAP protocol.
 */
@Service
public class ClientService {

    private static final Logger log = LoggerFactory.getLogger(ClientService.class);

    /**
     * RSA key type for RSA-based cryptographic operations
     */
    public static final String RSA = "RSA";

    /**
     * EC key type for Elliptic Curve-based cryptographic operations
     */
    public static final String EC = "EC";

    /**
     * OKP key type for Octet Key Pair (Ed25519, Ed448, X25519, X448) cryptographic operations
     */
    public static final String OKP = "OKP";

    private final ClientRepository clientRepository;
    private final ClientInformationService clientInformationService;

    public ClientService(ClientRepository clientRepository,
                         ClientInformationService clientInformationService) {
        this.clientRepository = clientRepository;
        this.clientInformationService = clientInformationService;
    }

    /**
     * Find a client by its instance ID.
     *
     * @param instanceId the instance ID
     * @return the client if found
     */
    @Transactional(readOnly = true)
    public Optional<Client> findByInstanceId(UUID instanceId) {
        return clientRepository.findByInstanceId(instanceId)
                .map(client -> {
                    UUID clientId = client.getId();
                    clientInformationService.findByClientId(clientId)
                            .ifPresent(client::setClientInformation);
                    return client;
                });
    }

    /**
     * Find a client by its key ID.
     *
     * @param keyId the key ID
     * @return the client if found
     */
    @Transactional(readOnly = true)
    public Optional<Client> findByKeyId(String keyId) {
        return clientRepository.findByKeyId(keyId)
                .map(client -> {
                    UUID clientId = client.getId();
                    clientInformationService.findByClientId(clientId)
                            .ifPresent(client::setClientInformation);
                    return client;
                });
    }

    /**
     * Create a new client.
     *
     * @param client the client information
     * @return the new client
     */
    private Client create(Client client) {
        client.setCreatedAt(LocalDateTime.now());
        client.setUpdatedAt(LocalDateTime.now());
        return clientRepository.save(client);
    }

    /**
     * Update a client.
     */
    private void update(Client client) {
        client.setUpdatedAt(LocalDateTime.now());
        clientRepository.save(client);
    }

    /**
     * Authenticate a client based on its key.
     *
     * @param client    the client information
     * @param signedJwt the signed JWT to verify (can be null if no signature verification is needed)
     * @return true if the client is authenticated, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean authenticateClient(Client client, String signedJwt) {
        // Check if a client has a key ID
        String keyId = client.getKeyId();
        if (isEmpty(keyId)) {
            log.warn("Client authentication failed: No key ID provided");
            return false;
        }

        // Find the client in the database
        Optional<Client> storedClient = findByKeyId(keyId);

        if (storedClient.isEmpty()) {
            log.warn("Client authentication failed: No client found with key ID {}", keyId);
            return false;
        }

        // If no signed JWT is provided, just check if the client exists
        if (isEmpty(signedJwt)) {
            log.info("Client authenticated by existence check only (no signature verification)");
            return true;
        }

        // Verify the signature using the client's JWK
        try {
            // Parse the stored JWK
            String keyJwk = storedClient.get().getKeyJwk();

            if (isEmpty(keyJwk)) {
                log.warn("Client authentication failed: No JWK found for client with key ID {}", client.getKeyId());
                return false;
            }

            JWK jwk = JWK.parse(keyJwk);

            // Parse the signed JWT
            SignedJWT jwt = SignedJWT.parse(signedJwt);

            // Verify that the JWT was signed with the key identified by the key ID
            JWSHeader header = jwt.getHeader();

            if (!client.getKeyId().equals(header.getKeyID())) {
                log.warn("Client authentication failed: JWT key ID {} does not match client key ID {}", header.getKeyID(), client.getKeyId());
                return false;
            }

            // Create a verifier for the JWK based on its type
            JWSVerifier verifier;

            try {
                // Get the key type
                String keyType = jwk.getKeyType().getValue();

                // Create the appropriate verifier based on key type
                switch (keyType) {
                    case RSA -> verifier = new DefaultJWSVerifierFactory().createJWSVerifier(
                            header,
                            jwk.toRSAKey().toRSAPublicKey());
                    case EC -> verifier = new DefaultJWSVerifierFactory().createJWSVerifier(
                            header,
                            jwk.toECKey().toECPublicKey());
                    case OKP -> verifier = new DefaultJWSVerifierFactory().createJWSVerifier(
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

    /**
     * Get the client information for a client.
     *
     * @param clientId the client ID
     * @return the client information if found
     */
    @Transactional(readOnly = true)
    public Optional<ClientInformation> clientInformation(UUID clientId) {
        return clientInformationService.findByClientId(clientId);
    }

    /**
     * Register a client from a grant request.
     * If the client already exists (by key ID), return the existing client.
     * Otherwise, create a new client.
     *
     * @param client the client information
     * @return the registered client
     */
    @Transactional
    public Client registerClient(Client client) {
        // Check if client already exists by key ID
        if (client.getKeyId() != null) {
            Optional<Client> existingClient = findByKeyId(client.getKeyId());
            if (existingClient.isPresent()) {
                return existingClient.get();
            }
        }

        // Create a new client
        return create(client);
    }
}
