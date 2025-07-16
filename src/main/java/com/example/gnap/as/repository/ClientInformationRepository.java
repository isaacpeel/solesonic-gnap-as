package com.example.gnap.as.repository;

import com.example.gnap.as.model.ClientInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for ClientInformation entity.
 */
@Repository
public interface ClientInformationRepository extends JpaRepository<ClientInformation, UUID> {

    /**
     * Find a ClientInformation by client ID.
     *
     * @param clientId the ID of the client
     * @return the ClientInformation associated with the client ID
     */
    @Query("SELECT ci FROM ClientInformation ci WHERE ci.clientId = ?1")
    Optional<ClientInformation> findByClientId(UUID clientId);

}
