package com.example.gnap.as.repository;

import com.example.gnap.as.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Client entity.
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {

    /**
     * Find a client by its instance ID.
     *
     * @param instanceId the instance ID
     * @return the client if found
     */
    Optional<Client> findByInstanceId(UUID instanceId);

    /**
     * Find a client by its key ID.
     *
     * @param keyId the key ID
     * @return the client if found
     */
    Optional<Client> findByKeyId(String keyId);
}
