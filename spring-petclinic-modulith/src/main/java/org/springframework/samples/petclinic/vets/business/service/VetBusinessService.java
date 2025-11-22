/*
 * Copyright 2002-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.vets.business.service;

import org.springframework.samples.petclinic.vets.VetCreated;
import org.springframework.samples.petclinic.vets.VetUpdated;
import org.springframework.samples.petclinic.vets.business.exception.VetNotFoundException;
import org.springframework.samples.petclinic.vets.business.port.EventPublisher;
import org.springframework.samples.petclinic.vets.business.port.VetRepository;
import org.springframework.samples.petclinic.vets.domain.Vet;

import java.util.List;
import java.util.Optional;

/**
 * Pure Java business service for Vet operations.
 *
 * This service contains business logic with ZERO framework dependencies.
 * It uses pure Java and depends only on domain models and ports (interfaces).
 *
 * @author PetClinic Team
 */
public class VetBusinessService {

    private final VetRepository vetRepository;
    private final EventPublisher eventPublisher;

    public VetBusinessService(VetRepository vetRepository, EventPublisher eventPublisher) {
        if (vetRepository == null) {
            throw new IllegalArgumentException("VetRepository cannot be null");
        }
        if (eventPublisher == null) {
            throw new IllegalArgumentException("EventPublisher cannot be null");
        }
        this.vetRepository = vetRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Find vet by ID.
     *
     * @param vetId the vet ID
     * @return optional vet
     */
    public Optional<Vet> findById(Integer vetId) {
        if (vetId == null || vetId <= 0) {
            throw new IllegalArgumentException("Vet ID must be positive");
        }

        return vetRepository.findById(vetId);
    }

    /**
     * Find all vets.
     *
     * @return list of all vets
     */
    public List<Vet> findAll() {
        return vetRepository.findAll();
    }

    /**
     * Create a new vet.
     *
     * @param vet the vet to create
     * @return the created vet
     */
    public Vet createVet(Vet vet) {
        if (vet == null) {
            throw new IllegalArgumentException("Vet cannot be null");
        }
        if (vet.getId() != null) {
            throw new IllegalArgumentException("New vet should not have an ID");
        }

        // Validate vet data
        vet.validateForCreation();

        // Save vet
        Vet savedVet = vetRepository.save(vet);

        // Publish domain event
        eventPublisher.publish(new VetCreated(
            savedVet.getId(),
            savedVet.getFullName()
        ));

        return savedVet;
    }

    /**
     * Update an existing vet.
     *
     * @param vetId the vet ID
     * @param vetData the updated vet data
     * @return the updated vet
     */
    public Vet updateVet(Integer vetId, Vet vetData) {
        if (vetId == null || vetId <= 0) {
            throw new IllegalArgumentException("Vet ID must be positive");
        }
        if (vetData == null) {
            throw new IllegalArgumentException("Vet data cannot be null");
        }

        // Find existing vet
        Vet existingVet = vetRepository.findById(vetId)
            .orElseThrow(() -> new VetNotFoundException("Vet not found: " + vetId, vetId));

        // Validate new data
        vetData.validateForCreation();

        // Update fields
        existingVet.setFirstName(vetData.getFirstName());
        existingVet.setLastName(vetData.getLastName());

        // Save updated vet
        Vet updatedVet = vetRepository.save(existingVet);

        // Publish domain event
        eventPublisher.publish(new VetUpdated(
            updatedVet.getId(),
            updatedVet.getFullName()
        ));

        return updatedVet;
    }

    /**
     * Delete a vet by ID.
     *
     * @param vetId the vet ID
     */
    public void deleteVet(Integer vetId) {
        if (vetId == null || vetId <= 0) {
            throw new IllegalArgumentException("Vet ID must be positive");
        }

        // Find existing vet to verify it exists
        Vet existingVet = vetRepository.findById(vetId)
            .orElseThrow(() -> new VetNotFoundException("Vet not found: " + vetId, vetId));

        // Delete vet
        vetRepository.deleteById(vetId);

        // Note: VetDeleted event would be published here if it existed
    }
}
