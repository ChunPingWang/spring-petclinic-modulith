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
import org.springframework.samples.petclinic.shared.exceptions.ResourceNotFoundException;
import org.springframework.samples.petclinic.vets.Vet;
import org.springframework.samples.petclinic.vets.VetService;
import org.springframework.samples.petclinic.vets.business.exception.VetNotFoundException;
import org.springframework.samples.petclinic.vets.business.service.VetBusinessService;
import org.springframework.samples.petclinic.vets.infrastructure.persistence.mapper.DomainMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Internal implementation of VetService.
 *
 * This implementation delegates to VetBusinessService and converts between
 * domain models and public API entities for backward compatibility.
 *
 * @author PetClinic Team
 */
@Service
@Transactional
class VetServiceImpl implements VetService {

    private static final Logger log = LoggerFactory.getLogger(VetServiceImpl.class);

    private final VetBusinessService businessService;

    VetServiceImpl(VetBusinessService businessService) {
        this.businessService = businessService;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Vet> findById(Integer vetId) {
        log.debug("Finding vet by ID: {}", vetId);

        // Delegate to business service and convert domain model to legacy entity
        Optional<org.springframework.samples.petclinic.vets.domain.Vet> domainVet =
                businessService.findById(vetId);

        return domainVet.map(DomainMapper::toLegacyEntity);
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable("vets")
    public List<Vet> findAll() {
        log.debug("Finding all vets");

        // Delegate to business service and convert domain models to legacy entities
        List<org.springframework.samples.petclinic.vets.domain.Vet> domainVets =
                businessService.findAll();

        return domainVets.stream()
                .map(DomainMapper::toLegacyEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public Vet save(Vet vet) {
        log.info("Creating new vet: {}", vet.getFullName());

        // Convert legacy entity to domain model
        org.springframework.samples.petclinic.vets.domain.Vet domainVet =
                DomainMapper.fromLegacyEntity(vet);

        // Delegate to business service (which handles event publishing)
        org.springframework.samples.petclinic.vets.domain.Vet savedDomainVet =
                businessService.createVet(domainVet);

        // Convert back to legacy entity
        Vet savedVet = DomainMapper.toLegacyEntity(savedDomainVet);

        log.info("Vet created with ID: {}", savedVet.getId());
        return savedVet;
    }
    
    @Override
    public Vet update(Integer vetId, Vet vet) {
        log.info("Updating vet ID: {}", vetId);

        try {
            // Convert legacy entity to domain model
            org.springframework.samples.petclinic.vets.domain.Vet domainVet =
                    DomainMapper.fromLegacyEntity(vet);

            // Delegate to business service (which handles event publishing)
            org.springframework.samples.petclinic.vets.domain.Vet updatedDomainVet =
                    businessService.updateVet(vetId, domainVet);

            // Convert back to legacy entity
            Vet updatedVet = DomainMapper.toLegacyEntity(updatedDomainVet);

            log.info("Vet updated: {}", updatedVet.getId());
            return updatedVet;
        } catch (VetNotFoundException e) {
            // Convert business exception to API exception for backward compatibility
            throw new ResourceNotFoundException("Vet", vetId);
        }
    }

    @Override
    public void deleteById(Integer vetId) {
        log.info("Deleting vet ID: {}", vetId);

        try {
            // Delegate to business service
            businessService.deleteVet(vetId);

            log.info("Vet deleted with ID: {}", vetId);
        } catch (VetNotFoundException e) {
            // Convert business exception to API exception for backward compatibility
            throw new ResourceNotFoundException("Vet", vetId);
        }
    }
}
