package com.example.gnap.as.service;

import com.example.gnap.as.model.Client;
import com.example.gnap.as.repository.ClientRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private ClientService clientService;

    private RSAKey rsaJWK;
    private Client testClient;

    @BeforeEach
    void setUp() throws JOSEException {
        MockitoAnnotations.openMocks(this);
        clientService = new ClientService(clientRepository, objectMapper);

        // Generate RSA key pair
        rsaJWK = new RSAKeyGenerator(2048)
                .keyID(UUID.randomUUID().toString())
                .generate();

        // Create test client
        testClient = new Client();
        testClient.setId(UUID.randomUUID().toString());
        testClient.setKeyId(rsaJWK.getKeyID());
        testClient.setKeyJwk(rsaJWK.toString());
    }

    @Test
    void authenticateClient_withValidSignature_shouldReturnTrue() throws Exception {
        // Arrange
        when(clientRepository.findByKeyId(testClient.getKeyId())).thenReturn(Optional.of(testClient));

        // Create JWT claims
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("client")
                .issuer("test")
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + 60 * 1000))
                .build();

        // Create signer with the RSA private key
        JWSSigner signer = new RSASSASigner(rsaJWK);

        // Create signed JWT
        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaJWK.getKeyID()).build(),
                claims);
        signedJWT.sign(signer);

        // Act
        boolean result = clientService.authenticateClient(testClient, signedJWT.serialize());

        // Assert
        assertTrue(result);
        verify(clientRepository).findByKeyId(testClient.getKeyId());
    }

    @Test
    void authenticateClient_withInvalidKeyId_shouldReturnFalse() {
        // Arrange
        Client clientWithInvalidKeyId = new Client();
        clientWithInvalidKeyId.setKeyId("invalid-key-id");

        when(clientRepository.findByKeyId("invalid-key-id")).thenReturn(Optional.empty());

        // Act
        boolean result = clientService.authenticateClient(clientWithInvalidKeyId);

        // Assert
        assertFalse(result);
        verify(clientRepository).findByKeyId("invalid-key-id");
    }

    @Test
    void authenticateClient_withNullKeyId_shouldReturnFalse() {
        // Arrange
        Client clientWithNullKeyId = new Client();
        clientWithNullKeyId.setKeyId(null);

        // Act
        boolean result = clientService.authenticateClient(clientWithNullKeyId);

        // Assert
        assertFalse(result);
        verify(clientRepository, never()).findByKeyId(any());
    }

    @Test
    void authenticateClient_withNoSignature_shouldCheckExistence() {
        // Arrange
        when(clientRepository.findByKeyId(testClient.getKeyId())).thenReturn(Optional.of(testClient));

        // Act
        boolean result = clientService.authenticateClient(testClient);

        // Assert
        assertTrue(result);
        verify(clientRepository).findByKeyId(testClient.getKeyId());
    }
}