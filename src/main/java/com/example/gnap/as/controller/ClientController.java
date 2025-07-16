package com.example.gnap.as.controller;

import com.example.gnap.as.model.Client;
import com.example.gnap.as.service.ClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Controller for client management in the GNAP protocol.
 */
@RestController
@RequestMapping("/gnap/clients")
public class ClientController {

    private static final Logger log = LoggerFactory.getLogger(ClientController.class);

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    /**
     * Get all clients.
     *
     * @return list of all clients
     */
    @GetMapping
    public ResponseEntity<List<Client>> getAllClients() {
        log.info("Received request for all clients");
        try {
            List<Client> clients = clientService.findAll();
            return ResponseEntity.ok(clients);
        } catch (Exception e) {
            log.error("Unexpected error retrieving all clients", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get a client by ID.
     *
     * @param id the client ID
     * @return the client
     */
    @GetMapping("/{id}")
    public ResponseEntity<Client> getById(@PathVariable UUID id) {
        log.info("Received request for client with ID: {}", id);
        try {
            Optional<Client> client = clientService.findById(id);
            return client
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Unexpected error retrieving client", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Register a new client.
     *
     * @param client the client to register
     * @return the registered client
     */
    @PostMapping
    public ResponseEntity<Client> registerClient(@RequestBody Client client) {
        log.info("Received client registration request: {}", client);
        try {
            Client registeredClient = clientService.registerClient(client);
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredClient);
        } catch (IllegalArgumentException e) {
            log.error("Error registering client", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error registering client", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update a client.
     *
     * @param id the client ID
     * @param client the client to update
     * @return the updated client
     */
    @PutMapping("/{id}")
    public ResponseEntity<Client> updateClient(@PathVariable UUID id, @RequestBody Client client) {
        log.info("Received client update request for ID {}: {}", id, client);
        try {
            // Ensure the ID in the path matches the ID in the body
            client.setId(id);
            Client updatedClient = clientService.update(client);
            return ResponseEntity.ok(updatedClient);
        } catch (IllegalArgumentException e) {
            log.error("Error updating client", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error updating client", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete a client.
     *
     * @param id the client ID
     * @return a success response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable UUID id) {
        log.info("Received client deletion request for ID: {}", id);
        try {
            clientService.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("Error deleting client", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error deleting client", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get a client by instance ID.
     *
     * @param instanceId the instance ID
     * @return the client
     */
    @GetMapping("/instance/{instanceId}")
    public ResponseEntity<Client> getByInstanceId(@PathVariable UUID instanceId) {
        log.info("Received request for client with instance ID: {}", instanceId);
        try {
            Optional<Client> client = clientService.findByInstanceId(instanceId);
            return client
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Unexpected error retrieving client", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get a client by key ID.
     *
     * @param keyId the key ID
     * @return the client
     */
    @GetMapping("/key/{keyId}")
    public ResponseEntity<Client> getByKeyId(@PathVariable String keyId) {
        log.info("Received request for client with key ID: {}", keyId);
        try {
            Optional<Client> client = clientService.findByKeyId(keyId);
            return client
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Unexpected error retrieving client", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
