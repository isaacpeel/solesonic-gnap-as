package com.example.gnap.as.service;

import com.example.gnap.as.model.ClientInformation;
import com.example.gnap.as.repository.ClientInformationRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ClientInformationService {
    private final ClientInformationRepository clientInformationRepository;


    public ClientInformationService(ClientInformationRepository clientInformationRepository) {
        this.clientInformationRepository = clientInformationRepository;
    }

    public ClientInformation create(UUID clientId, ClientInformation clientInformation) {
        clientInformation.setClientId(clientId);
        return clientInformationRepository.save(clientInformation);
    }

    public ClientInformation update(ClientInformation clientInformation) {
        return clientInformationRepository.save(clientInformation);
    }

    public void delete(UUID clientInformationId) {
        clientInformationRepository.deleteById(clientInformationId);
    }

    public Optional<ClientInformation> findByClientId(UUID clientId) {
        return clientInformationRepository.findByClientId(clientId);
    }

    public ClientInformation findById(UUID id) {
        return clientInformationRepository.findById(id).orElse(null);
    }
}
