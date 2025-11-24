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
import org.springframework.samples.petclinic.shared.exceptions.ResourceNotFoundException;
import org.springframework.samples.petclinic.visits.Visit;
import org.springframework.samples.petclinic.visits.VisitService;
import org.springframework.samples.petclinic.visits.business.exception.InvalidVisitException;
import org.springframework.samples.petclinic.visits.business.exception.VisitNotFoundException;
import org.springframework.samples.petclinic.visits.business.service.VisitBusinessService;
import org.springframework.samples.petclinic.visits.infrastructure.persistence.mapper.DomainMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Internal implementation of VisitService.
 *
 * This implementation delegates to VisitBusinessService and converts between
 * domain models and public API entities for backward compatibility.
 *
 * @author PetClinic Team
 */
@Service
@Transactional
public class VisitServiceImpl implements VisitService {

    private static final Logger log = LoggerFactory.getLogger(VisitServiceImpl.class);

    private final VisitBusinessService businessService;

    VisitServiceImpl(VisitBusinessService businessService) {
        this.businessService = businessService;
    }

    @Override
    public Optional<Visit> findById(Integer id) {
        log.debug("Finding visit by id: {}", id);
        Optional<org.springframework.samples.petclinic.visits.domain.Visit> domainVisit =
            businessService.findById(id);
        return domainVisit.map(DomainMapper::toLegacyEntity);
    }

    @Override
    public List<Visit> findAll() {
        log.debug("Finding all visits");
        List<org.springframework.samples.petclinic.visits.domain.Visit> domainVisits =
            businessService.findAll();
        return domainVisits.stream()
                .map(DomainMapper::toLegacyEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Visit> findByPetId(Integer petId) {
        log.debug("Finding visits for pet: {}", petId);
        List<org.springframework.samples.petclinic.visits.domain.Visit> domainVisits =
            businessService.findByPetId(petId);
        return domainVisits.stream()
                .map(DomainMapper::toLegacyEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Visit> findByVetId(Integer vetId) {
        log.debug("Finding visits for vet: {}", vetId);
        List<org.springframework.samples.petclinic.visits.domain.Visit> domainVisits =
            businessService.findByVetId(vetId);
        return domainVisits.stream()
                .map(DomainMapper::toLegacyEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Visit scheduleVisit(Visit visit) {
        log.info("Scheduling visit for pet {} with vet {}", visit.getPetId(), visit.getVetId());

        try {
            // Convert legacy entity to domain model
            org.springframework.samples.petclinic.visits.domain.Visit domainVisit =
                DomainMapper.fromLegacyEntity(visit);

            // Delegate to business service
            org.springframework.samples.petclinic.visits.domain.Visit scheduledVisit =
                businessService.scheduleVisit(domainVisit);

            log.info("Visit scheduled: {}", scheduledVisit.getId());

            // Convert back to legacy entity
            return DomainMapper.toLegacyEntity(scheduledVisit);

        } catch (InvalidVisitException e) {
            // Convert business exception to legacy exception
            throw new ResourceNotFoundException(e.getMessage());
        }
    }

    @Override
    public Visit completeVisit(Integer visitId) {
        log.info("Completing visit: {}", visitId);

        try {
            // Delegate to business service
            org.springframework.samples.petclinic.visits.domain.Visit completedVisit =
                businessService.completeVisit(visitId);

            // Convert back to legacy entity
            return DomainMapper.toLegacyEntity(completedVisit);

        } catch (VisitNotFoundException e) {
            // Convert business exception to legacy exception
            throw new ResourceNotFoundException(e.getMessage());
        }
    }

    @Override
    public void cancelVisit(Integer visitId) {
        log.info("Cancelling visit: {}", visitId);

        try {
            // Delegate to business service
            businessService.cancelVisit(visitId);

        } catch (VisitNotFoundException e) {
            // Convert business exception to legacy exception
            throw new ResourceNotFoundException(e.getMessage());
        }
    }
}
