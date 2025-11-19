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
package org.springframework.samples.petclinic.vets.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.samples.petclinic.shared.exceptions.ResourceNotFoundException;
import org.springframework.samples.petclinic.vets.Vet;
import org.springframework.samples.petclinic.vets.VetCreated;
import org.springframework.samples.petclinic.vets.VetService;
import org.springframework.samples.petclinic.vets.VetUpdated;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Internal implementation of VetService.
 * 
 * This implementation handles business logic and publishes domain events.
 * 
 * @author PetClinic Team
 */
@Service
@Transactional
class VetServiceImpl implements VetService {
    
    private static final Logger log = LoggerFactory.getLogger(VetServiceImpl.class);
    
    private final VetRepository vetRepository;
    private final ApplicationEventPublisher events;
    
    VetServiceImpl(VetRepository vetRepository, ApplicationEventPublisher events) {
        this.vetRepository = vetRepository;
        this.events = events;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Vet> findById(Integer vetId) {
        log.debug("Finding vet by ID: {}", vetId);
        return vetRepository.findById(vetId);
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable("vets")
    public List<Vet> findAll() {
        log.debug("Finding all vets");
        return vetRepository.findAll();
    }
    
    @Override
    public Vet save(Vet vet) {
        log.info("Creating new vet: {}", vet.getFullName());
        
        Vet savedVet = vetRepository.save(vet);
        
        // Publish domain event
        events.publishEvent(new VetCreated(
            savedVet.getId(), 
            savedVet.getFullName()
        ));
        
        log.info("Vet created with ID: {}", savedVet.getId());
        return savedVet;
    }
    
    @Override
    public Vet update(Integer vetId, Vet vet) {
        log.info("Updating vet ID: {}", vetId);
        
        Vet existingVet = vetRepository.findById(vetId)
            .orElseThrow(() -> new ResourceNotFoundException("Vet", vetId));
        
        // Update fields
        existingVet.setFirstName(vet.getFirstName());
        existingVet.setLastName(vet.getLastName());
        
        Vet updatedVet = vetRepository.save(existingVet);
        
        // Publish domain event
        events.publishEvent(new VetUpdated(
            updatedVet.getId(),
            updatedVet.getFullName()
        ));
        
        log.info("Vet updated: {}", updatedVet.getId());
        return updatedVet;
    }
}
