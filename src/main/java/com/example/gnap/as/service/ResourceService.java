package com.example.gnap.as.service;

import com.example.gnap.as.model.Resource;
import com.example.gnap.as.repository.ResourceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ResourceService {
    private final ResourceRepository resourceRepository;

    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    /**
     * Save a resource.
     *
     * @param resource the resource to save
     * @return the saved resource
     */
    @SuppressWarnings("unused")
    public Resource save(Resource resource) {
        return resourceRepository.save(resource);
    }

    /**
     * Find a resource by ID.
     *
     * @param id the resource ID
     * @return the resource, if found
     */
    @SuppressWarnings("unused")
    public Optional<Resource> findById(String id) {
        return resourceRepository.findById(id);
    }

    /**
     * Find all resources.
     *
     * @return the list of all resources
     */
    @SuppressWarnings("unused")
    public List<Resource> findAll() {
        return resourceRepository.findAll();
    }

    /**
     * Delete a resource.
     *
     * @param resource the resource to delete
     */
    @SuppressWarnings("unused")
    public void delete(Resource resource) {
        resourceRepository.delete(resource);
    }

    /**
     * Delete a resource by ID.
     *
     * @param id the ID of the resource to delete
     */
    @SuppressWarnings("unused")
    public void deleteById(String id) {
        resourceRepository.deleteById(id);
    }

    /**
     * Find resources by grant ID.
     *
     * @param grantId the grant ID
     * @return the list of resources
     */
    @SuppressWarnings("unused")
    public List<Resource> findByGrantId(UUID grantId) {
        return resourceRepository.findByGrantId(grantId);
    }

    /**
     * Find resources by type.
     *
     * @param type the resource type
     * @return the list of resources
     */
    @SuppressWarnings("unused")
    public List<Resource> findByType(String type) {
        return resourceRepository.findByType(type);
    }

    /**
     * Find resources by resource server.
     *
     * @param resourceServer the resource server
     * @return the list of resources
     */
    @SuppressWarnings("unused")
    public List<Resource> findByResourceServer(String resourceServer) {
        return resourceRepository.findByResourceServer(resourceServer);
    }

    /**
     * Find resources by grant ID and type.
     *
     * @param grantId the grant ID
     * @param type the resource type
     * @return the list of resources
     */
    @SuppressWarnings("unused")
    public List<Resource> findByGrantIdAndType(UUID grantId, String type) {
        return resourceRepository.findByGrantIdAndType(grantId, type);
    }

    /**
     * Find resources by grant ID and resource server.
     *
     * @param grantId the grant ID
     * @param resourceServer the resource server
     * @return the list of resources
     */
    @SuppressWarnings("unused")
    public List<Resource> findByGrantIdAndResourceServer(UUID grantId, String resourceServer) {
        return resourceRepository.findByGrantIdAndResourceServer(grantId, resourceServer);
    }
}
