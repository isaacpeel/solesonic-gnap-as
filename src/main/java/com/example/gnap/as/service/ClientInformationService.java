package com.example.gnap.as.service;

import com.example.gnap.as.model.ClientInformation;
import com.example.gnap.as.repository.ClientInformationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClientInformationService {
    private final ClientInformationRepository clientInformationRepository;


    public ClientInformationService(ClientInformationRepository clientInformationRepository) {
        this.clientInformationRepository = clientInformationRepository;
    }

    /**
     * Create client information for a client.
     *
     * @param clientId the client ID
     * @param clientInformation the client information to create
     * @return the created client information
     */
    @Transactional
    public ClientInformation create(UUID clientId, ClientInformation clientInformation) {
        clientInformation.setClientId(clientId);
        return clientInformationRepository.save(clientInformation);
    }

    /**
     * Update client information.
     *
     * @param clientInformation the client information to update
     * @return the updated client information
     */
    @Transactional
    public ClientInformation update(ClientInformation clientInformation) {
        return clientInformationRepository.save(clientInformation);
    }

    /**
     * Delete client information.
     *
     * @param clientInformationId the client information ID
     */
    @Transactional
    public void delete(UUID clientInformationId) {
        clientInformationRepository.deleteById(clientInformationId);
    }

    /**
     * Find client information by client ID.
     *
     * @param clientId the client ID
     * @return the client information if found
     */
    @Transactional(readOnly = true)
    public Optional<ClientInformation> findByClientId(UUID clientId) {
        return clientInformationRepository.findByClientId(clientId);
    }

    /**
     * Find all client information.
     *
     * @return list of all client information
     */
    @SuppressWarnings("unused")
    @Transactional(readOnly = true)
    public List<ClientInformation> findAll() {
        return clientInformationRepository.findAll();
    }

    /**
     * Find client information by ID.
     *
     * @param id the client information ID
     * @return the client information if found
     */
    @SuppressWarnings("unused")
    @Transactional(readOnly = true)
    public Optional<ClientInformation> findById(UUID id) {
        return clientInformationRepository.findById(id);
    }
}
