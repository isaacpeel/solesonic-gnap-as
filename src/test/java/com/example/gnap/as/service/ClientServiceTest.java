package com.example.gnap.as.service;

import com.example.gnap.as.model.Client;
import com.example.gnap.as.model.ClientInformation;
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

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientInformationService clientInformationService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private ClientService clientService;

    private RSAKey rsaJWK;
    private Client testClient;

    @BeforeEach
    void setUp() throws JOSEException {
        MockitoAnnotations.openMocks(this);
        clientService = new ClientService(clientRepository, clientInformationService);

        // Generate RSA key pair
        rsaJWK = new RSAKeyGenerator(2048)
                .keyID(UUID.randomUUID().toString())
                .generate();

        // Create test client
        testClient = new Client();
        testClient.setId(UUID.randomUUID());
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

    @Test
    void clientInformation() {
        // Arrange
        UUID clientId = UUID.randomUUID();
        ClientInformation clientInfo = new ClientInformation();
        clientInfo.setId(clientId);
        clientInfo.setName("Test Client");

        when(clientInformationService.findByClientId(clientId)).thenReturn(Optional.of(clientInfo));

        // Act
        Optional<ClientInformation> result = clientService.clientInformation(clientId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Test Client", result.get().getName());
        verify(clientInformationService).findByClientId(clientId);
    }

    @Test
    void findByInstanceId_shouldReturnClientWithClientInformation() {
        // Arrange
        UUID instanceId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();

        Client client = new Client();
        client.setId(clientId);
        client.setInstanceId(instanceId);

        ClientInformation clientInfo = new ClientInformation();
        clientInfo.setId(UUID.randomUUID());
        clientInfo.setClientId(clientId);
        clientInfo.setName("Test Client");

        when(clientRepository.findByInstanceId(instanceId)).thenReturn(Optional.of(client));
        when(clientInformationService.findByClientId(clientId)).thenReturn(Optional.of(clientInfo));

        // Act
        Optional<Client> result = clientService.findByInstanceId(instanceId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(instanceId, result.get().getInstanceId());
        assertNotNull(result.get().getClientInformation());
        assertEquals("Test Client", result.get().getClientInformation().getName());
        verify(clientRepository).findByInstanceId(instanceId);
        verify(clientInformationService).findByClientId(clientId);
    }

    @Test
    void findByKeyId_shouldReturnClientWithClientInformation() {
        // Arrange
        String keyId = "test-key-id";
        UUID clientId = UUID.randomUUID();

        Client client = new Client();
        client.setId(clientId);
        client.setKeyId(keyId);

        ClientInformation clientInfo = new ClientInformation();
        clientInfo.setId(UUID.randomUUID());
        clientInfo.setClientId(clientId);
        clientInfo.setName("Test Client");

        when(clientRepository.findByKeyId(keyId)).thenReturn(Optional.of(client));
        when(clientInformationService.findByClientId(clientId)).thenReturn(Optional.of(clientInfo));

        // Act
        Optional<Client> result = clientService.findByKeyId(keyId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(keyId, result.get().getKeyId());
        assertNotNull(result.get().getClientInformation());
        assertEquals("Test Client", result.get().getClientInformation().getName());
        verify(clientRepository).findByKeyId(keyId);
        verify(clientInformationService).findByClientId(clientId);
    }

    @Test
    void registerClient_whenClientExists_shouldReturnExistingClient() {
        // Arrange
        String keyId = "existing-key-id";
        UUID clientId = UUID.randomUUID();

        Client existingClient = new Client();
        existingClient.setId(clientId);
        existingClient.setKeyId(keyId);

        Client newClient = new Client();
        newClient.setKeyId(keyId);

        when(clientRepository.findByKeyId(keyId)).thenReturn(Optional.of(existingClient));

        // Act
        Client result = clientService.registerClient(newClient);

        // Assert
        assertEquals(clientId, result.getId());
        assertEquals(keyId, result.getKeyId());
        verify(clientRepository).findByKeyId(keyId);
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void registerClient_whenClientDoesNotExist_shouldCreateNewClient() {
        // Arrange
        String keyId = "new-key-id";
        UUID clientId = UUID.randomUUID();

        Client newClient = new Client();
        newClient.setKeyId(keyId);

        Client savedClient = new Client();
        savedClient.setId(clientId);
        savedClient.setKeyId(keyId);
        savedClient.setCreatedAt(LocalDateTime.now());
        savedClient.setUpdatedAt(LocalDateTime.now());

        when(clientRepository.findByKeyId(keyId)).thenReturn(Optional.empty());
        when(clientRepository.save(any(Client.class))).thenReturn(savedClient);

        // Act
        Client result = clientService.registerClient(newClient);

        // Assert
        assertEquals(clientId, result.getId());
        assertEquals(keyId, result.getKeyId());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        verify(clientRepository).findByKeyId(keyId);
        verify(clientRepository).save(any(Client.class));
    }
}
