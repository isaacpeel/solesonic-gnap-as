package com.example.gnap.as.controller;

import com.example.gnap.as.model.ClientInformation;
import com.example.gnap.as.service.ClientInformationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

/**
 * Controller for client information management in the GNAP protocol.
 */
@RestController
@RequestMapping("/gnap/clientInformation")
public class ClientInformationController {

    private static final Logger log = LoggerFactory.getLogger(ClientInformationController.class);

    private final ClientInformationService clientInformationService;

    public ClientInformationController(ClientInformationService clientInformationService) {
        this.clientInformationService = clientInformationService;
    }

    /**
     * Create client information for a client.
     *
     * @param clientId the client ID
     * @param clientInformation the client information to create
     * @return the created client information
     */
    @PostMapping("/client/{clientId}")
    public ResponseEntity<ClientInformation> create(
            @PathVariable UUID clientId,
            @RequestBody ClientInformation clientInformation) {
        log.info("Received client information creation request for client ID: {}", clientId);
        try {
            ClientInformation createdClientInformation = clientInformationService.create(clientId, clientInformation);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdClientInformation);
        } catch (IllegalArgumentException e) {
            log.error("Error creating client information", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error creating client information", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update client information.
     *
     * @param clientInformation the client information to update
     * @return the updated client information
     */
    @PutMapping
    public ResponseEntity<ClientInformation> update(@RequestBody ClientInformation clientInformation) {
        log.info("Received client information update request: {}", clientInformation);
        try {
            ClientInformation updatedClientInformation = clientInformationService.update(clientInformation);
            return ResponseEntity.ok(updatedClientInformation);
        } catch (IllegalArgumentException e) {
            log.error("Error updating client information", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error updating client information", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete client information.
     *
     * @param clientInformationId the client information ID
     * @return a success response
     */
    @DeleteMapping("/{clientInformationId}")
    public ResponseEntity<Void> delete(@PathVariable UUID clientInformationId) {
        log.info("Received client information deletion request for ID: {}", clientInformationId);
        try {
            clientInformationService.delete(clientInformationId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("Error deleting client information", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error deleting client information", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get client information by client ID.
     *
     * @param clientId the client ID
     * @return the client information
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<ClientInformation> getByClientId(@PathVariable UUID clientId) {
        log.info("Received request for client information for client ID: {}", clientId);
        try {
            Optional<ClientInformation> clientInformation = clientInformationService.findByClientId(clientId);
            return clientInformation
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Unexpected error retrieving client information", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
