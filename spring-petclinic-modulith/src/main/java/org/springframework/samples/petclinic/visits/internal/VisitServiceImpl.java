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
package org.springframework.samples.petclinic.visits.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.samples.petclinic.customers.CustomerService;
import org.springframework.samples.petclinic.shared.exceptions.ResourceNotFoundException;
import org.springframework.samples.petclinic.vets.VetService;
import org.springframework.samples.petclinic.visits.Visit;
import org.springframework.samples.petclinic.visits.VisitCompleted;
import org.springframework.samples.petclinic.visits.VisitCreated;
import org.springframework.samples.petclinic.visits.VisitService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of VisitService demonstrating cross-module communication.
 * 
 * This service:
 * - Depends on CustomerService (customers module)
 * - Depends on VetService (vets module)
 * - Publishes events for other modules to consume
 * - Validates referential integrity across modules
 * 
 * @author PetClinic Team
 */
@Service
@Transactional
class VisitServiceImpl implements VisitService {

    private static final Logger log = LoggerFactory.getLogger(VisitServiceImpl.class);

    private final VisitRepository visitRepository;
    private final CustomerService customerService;
    private final VetService vetService;
    private final ApplicationEventPublisher events;

    /**
     * Cross-module dependency injection demonstrating Modulith patterns.
     * 
     * All dependencies are public APIs from other modules:
     * - CustomerService from customers module
     * - VetService from vets module
     */
    VisitServiceImpl(VisitRepository visitRepository,
                    CustomerService customerService,
                    VetService vetService,
                    ApplicationEventPublisher events) {
        this.visitRepository = visitRepository;
        this.customerService = customerService;
        this.vetService = vetService;
        this.events = events;
    }

    @Override
    public Optional<Visit> findById(Integer id) {
        log.debug("Finding visit by id: {}", id);
        return visitRepository.findById(id);
    }

    @Override
    public List<Visit> findAll() {
        log.debug("Finding all visits");
        return visitRepository.findAll();
    }

    @Override
    public List<Visit> findByPetId(Integer petId) {
        log.debug("Finding visits for pet: {}", petId);
        // Note: We don't validate pet existence here to allow flexible queries
        // Validation happens during visit scheduling
        return visitRepository.findByPetId(petId);
    }

    @Override
    public List<Visit> findByVetId(Integer vetId) {
        log.debug("Finding visits for vet: {}", vetId);
        // Note: We don't validate vet existence here to allow flexible queries
        return visitRepository.findByVetId(vetId);
    }

    @Override
    public Visit scheduleVisit(Visit visit) {
        log.info("Scheduling visit for pet {} with vet {}", visit.getPetId(), visit.getVetId());
        
        // Validate that the pet exists (cross-module dependency)
        if (!customerService.findById(visit.getPetId()).isPresent()) {
            throw new ResourceNotFoundException("Pet not found: " + visit.getPetId());
        }
        
        // Validate that the vet exists (cross-module dependency)
        if (!vetService.findById(visit.getVetId()).isPresent()) {
            throw new ResourceNotFoundException("Vet not found: " + visit.getVetId());
        }
        
        // Set status to SCHEDULED
        visit.setStatus("SCHEDULED");
        
        // Save visit
        Visit savedVisit = visitRepository.save(visit);
        log.info("Visit scheduled: {}", savedVisit.getId());
        
        // Publish event for other modules (e.g., genai module)
        events.publishEvent(new VisitCreated(
            savedVisit.getId(),
            savedVisit.getPetId(),
            savedVisit.getVetId()
        ));
        
        return savedVisit;
    }

    @Override
    public Visit completeVisit(Integer visitId) {
        log.info("Completing visit: {}", visitId);
        
        Visit visit = visitRepository.findById(visitId)
            .orElseThrow(() -> new ResourceNotFoundException("Visit not found: " + visitId));
        
        visit.setStatus("COMPLETED");
        Visit completedVisit = visitRepository.save(visit);
        
        // Publish completion event
        events.publishEvent(new VisitCompleted(
            completedVisit.getId(),
            completedVisit.getPetId(),
            completedVisit.getVetId()
        ));
        
        return completedVisit;
    }

    @Override
    public void cancelVisit(Integer visitId) {
        log.info("Cancelling visit: {}", visitId);
        
        Visit visit = visitRepository.findById(visitId)
            .orElseThrow(() -> new ResourceNotFoundException("Visit not found: " + visitId));
        
        visit.setStatus("CANCELLED");
        visitRepository.save(visit);
    }
}
