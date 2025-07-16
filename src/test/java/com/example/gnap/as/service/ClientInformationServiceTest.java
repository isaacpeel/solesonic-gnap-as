package com.example.gnap.as.service;

import com.example.gnap.as.model.ClientInformation;
import com.example.gnap.as.repository.ClientInformationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for the ClientInformationService class.
 */
class ClientInformationServiceTest {

    @Mock
    private ClientInformationRepository clientInformationRepository;

    private ClientInformationService clientInformationService;

    private ClientInformation testClientInformation;
    private UUID testClientId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        clientInformationService = new ClientInformationService(clientInformationRepository);

        testClientId = UUID.randomUUID();
        testClientInformation = new ClientInformation();
        testClientInformation.setId(UUID.randomUUID());
        testClientInformation.setClientId(testClientId);
        testClientInformation.setName("Test Client");
        testClientInformation.setUri("https://test-client.com");
        testClientInformation.setLogoUri("https://test-client.com/logo.png");
    }

    @Test
    void findByClientId_shouldReturnClientInformation() {
        // Arrange
        when(clientInformationRepository.findByClientId(testClientId)).thenReturn(Optional.of(testClientInformation));

        // Act
        Optional<ClientInformation> result = clientInformationService.findByClientId(testClientId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testClientId, result.get().getClientId());
        assertEquals("Test Client", result.get().getName());
        assertEquals("https://test-client.com", result.get().getUri());
        assertEquals("https://test-client.com/logo.png", result.get().getLogoUri());
        verify(clientInformationRepository).findByClientId(testClientId);
    }

    @Test
    void save_shouldSetClientIdAndCreateClientInformation() {
        // Arrange
        ClientInformation clientInfoToSave = new ClientInformation();
        clientInfoToSave.setName("New Client");

        when(clientInformationRepository.save(any(ClientInformation.class))).thenReturn(clientInfoToSave);

        // Act
        ClientInformation result = clientInformationService.create(testClientId, clientInfoToSave);

        // Assert
        assertEquals(testClientId, clientInfoToSave.getClientId());
        assertEquals("New Client", result.getName());
        verify(clientInformationRepository).save(clientInfoToSave);
    }

    @Test
    void update_shouldCreateClientInformation() {
        // Arrange
        when(clientInformationRepository.save(testClientInformation)).thenReturn(testClientInformation);

        // Act
        ClientInformation result = clientInformationService.update(testClientInformation);

        // Assert
        assertEquals(testClientInformation, result);
        verify(clientInformationRepository).save(testClientInformation);
    }

    @Test
    void delete_shouldDeleteClientInformationById() {
        // Arrange
        UUID clientInformationId = UUID.randomUUID();

        // Act
        clientInformationService.delete(clientInformationId);

        // Assert
        verify(clientInformationRepository).deleteById(clientInformationId);
    }
}
