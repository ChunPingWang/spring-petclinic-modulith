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
package org.springframework.samples.petclinic.visits.business.service;

import org.springframework.samples.petclinic.visits.VisitCompleted;
import org.springframework.samples.petclinic.visits.VisitCreated;
import org.springframework.samples.petclinic.visits.business.exception.InvalidVisitException;
import org.springframework.samples.petclinic.visits.business.exception.VisitNotFoundException;
import org.springframework.samples.petclinic.visits.business.port.EventPublisher;
import org.springframework.samples.petclinic.visits.business.port.PetValidator;
import org.springframework.samples.petclinic.visits.business.port.VetValidator;
import org.springframework.samples.petclinic.visits.business.port.VisitRepository;
import org.springframework.samples.petclinic.visits.domain.Visit;

import java.util.List;
import java.util.Optional;

/**
 * Pure Java business service for Visit operations.
 *
 * This service contains business logic with ZERO framework dependencies.
 * It uses pure Java and depends only on domain models and ports (interfaces).
 *
 * @author PetClinic Team
 */
public class VisitBusinessService {

    private final VisitRepository visitRepository;
    private final PetValidator petValidator;
    private final VetValidator vetValidator;
    private final EventPublisher eventPublisher;

    public VisitBusinessService(
            VisitRepository visitRepository,
            PetValidator petValidator,
            VetValidator vetValidator,
            EventPublisher eventPublisher) {
        if (visitRepository == null) {
            throw new IllegalArgumentException("VisitRepository cannot be null");
        }
        if (petValidator == null) {
            throw new IllegalArgumentException("PetValidator cannot be null");
        }
        if (vetValidator == null) {
            throw new IllegalArgumentException("VetValidator cannot be null");
        }
        if (eventPublisher == null) {
            throw new IllegalArgumentException("EventPublisher cannot be null");
        }
        this.visitRepository = visitRepository;
        this.petValidator = petValidator;
        this.vetValidator = vetValidator;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Find visit by ID.
     *
     * @param visitId the visit ID
     * @return optional visit
     */
    public Optional<Visit> findById(Integer visitId) {
        if (visitId == null || visitId <= 0) {
            throw new IllegalArgumentException("Visit ID must be positive");
        }
        return visitRepository.findById(visitId);
    }

    /**
     * Find all visits.
     *
     * @return list of all visits
     */
    public List<Visit> findAll() {
        return visitRepository.findAll();
    }

    /**
     * Find visits by pet ID.
     *
     * @param petId the pet ID
     * @return list of visits
     */
    public List<Visit> findByPetId(Integer petId) {
        if (petId == null || petId <= 0) {
            throw new IllegalArgumentException("Pet ID must be positive");
        }
        return visitRepository.findByPetId(petId);
    }

    /**
     * Find visits by vet ID.
     *
     * @param vetId the vet ID
     * @return list of visits
     */
    public List<Visit> findByVetId(Integer vetId) {
        if (vetId == null || vetId <= 0) {
            throw new IllegalArgumentException("Vet ID must be positive");
        }
        return visitRepository.findByVetId(vetId);
    }

    /**
     * Schedule a new visit.
     *
     * @param visit the visit to schedule
     * @return the scheduled visit
     */
    public Visit scheduleVisit(Visit visit) {
        if (visit == null) {
            throw new IllegalArgumentException("Visit cannot be null");
        }
        if (visit.getId() != null) {
            throw new IllegalArgumentException("New visit should not have an ID");
        }

        // Validate visit data
        visit.validateForScheduling();

        // Validate pet exists (cross-module validation)
        if (!petValidator.petExists(visit.getPetId())) {
            throw new InvalidVisitException("Pet not found: " + visit.getPetId());
        }

        // Validate vet exists (cross-module validation)
        if (!vetValidator.vetExists(visit.getVetId())) {
            throw new InvalidVisitException("Vet not found: " + visit.getVetId());
        }

        // Schedule the visit
        visit.schedule();

        // Save visit
        Visit savedVisit = visitRepository.save(visit);

        // Publish domain event
        eventPublisher.publish(new VisitCreated(
                savedVisit.getId(),
                savedVisit.getPetId(),
                savedVisit.getVetId()
        ));

        return savedVisit;
    }

    /**
     * Complete a visit.
     *
     * @param visitId the visit ID
     * @return the completed visit
     */
    public Visit completeVisit(Integer visitId) {
        if (visitId == null || visitId <= 0) {
            throw new IllegalArgumentException("Visit ID must be positive");
        }

        // Find existing visit
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new VisitNotFoundException("Visit not found: " + visitId, visitId));

        // Complete the visit (business logic)
        try {
            visit.complete();
        } catch (IllegalStateException e) {
            throw new InvalidVisitException(e.getMessage(), e);
        }

        // Save updated visit
        Visit completedVisit = visitRepository.save(visit);

        // Publish domain event
        eventPublisher.publish(new VisitCompleted(
                completedVisit.getId(),
                completedVisit.getPetId(),
                completedVisit.getVetId()
        ));

        return completedVisit;
    }

    /**
     * Cancel a visit.
     *
     * @param visitId the visit ID
     */
    public void cancelVisit(Integer visitId) {
        if (visitId == null || visitId <= 0) {
            throw new IllegalArgumentException("Visit ID must be positive");
        }

        // Find existing visit
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new VisitNotFoundException("Visit not found: " + visitId, visitId));

        // Cancel the visit (business logic)
        try {
            visit.cancel();
        } catch (IllegalStateException e) {
            throw new InvalidVisitException(e.getMessage(), e);
        }

        // Save updated visit
        visitRepository.save(visit);

        // Note: VisitCancelled event would be published here if it existed
    }
}
