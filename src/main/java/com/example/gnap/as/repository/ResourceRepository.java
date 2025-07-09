package com.example.gnap.as.repository;

import com.example.gnap.as.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Resource entity.
 */
@Repository
public interface ResourceRepository extends JpaRepository<Resource, String> {
    
    /**
     * Find resources by grant ID.
     *
     * @param grantId the grant ID
     * @return the list of resources
     */
    List<Resource> findByGrantId(String grantId);
    
    /**
     * Find resources by type.
     *
     * @param type the resource type
     * @return the list of resources
     */
    List<Resource> findByType(String type);
    
    /**
     * Find resources by resource server.
     *
     * @param resourceServer the resource server
     * @return the list of resources
     */
    List<Resource> findByResourceServer(String resourceServer);
    
    /**
     * Find resources by grant ID and type.
     *
     * @param grantId the grant ID
     * @param type the resource type
     * @return the list of resources
     */
    List<Resource> findByGrantIdAndType(String grantId, String type);
    
    /**
     * Find resources by grant ID and resource server.
     *
     * @param grantId the grant ID
     * @param resourceServer the resource server
     * @return the list of resources
     */
    List<Resource> findByGrantIdAndResourceServer(String grantId, String resourceServer);
}