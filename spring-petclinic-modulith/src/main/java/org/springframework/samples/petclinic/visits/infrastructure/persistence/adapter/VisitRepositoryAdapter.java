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
package org.springframework.samples.petclinic.visits.infrastructure.persistence.adapter;

import org.springframework.samples.petclinic.visits.business.port.VisitRepository;
import org.springframework.samples.petclinic.visits.domain.Visit;
import org.springframework.samples.petclinic.visits.infrastructure.persistence.entity.VisitEntity;
import org.springframework.samples.petclinic.visits.infrastructure.persistence.jpa.VisitJpaRepository;
import org.springframework.samples.petclinic.visits.infrastructure.persistence.mapper.DomainMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementation of VisitRepository port.
 *
 * This adapter bridges the pure Java business layer with the Spring Data JPA infrastructure.
 * It converts between domain models (Visit) and JPA entities (VisitEntity).
 *
 * @author PetClinic Team
 */
@Component
public class VisitRepositoryAdapter implements VisitRepository {

    private final VisitJpaRepository jpaRepository;

    public VisitRepositoryAdapter(VisitJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Visit> findById(Integer id) {
        return jpaRepository.findById(id)
                .map(DomainMapper::toDomain);
    }

    @Override
    public List<Visit> findAll() {
        return jpaRepository.findAll().stream()
                .map(DomainMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Visit> findByPetId(Integer petId) {
        return jpaRepository.findByPetId(petId).stream()
                .map(DomainMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Visit> findByVetId(Integer vetId) {
        return jpaRepository.findByVetId(vetId).stream()
                .map(DomainMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Visit save(Visit visit) {
        VisitEntity entity;

        if (visit.getId() != null) {
            // Update existing visit
            Optional<VisitEntity> existingEntity = jpaRepository.findById(visit.getId());
            if (existingEntity.isPresent()) {
                entity = existingEntity.get();
                DomainMapper.updateEntity(entity, visit);
            } else {
                // Create new entity if ID doesn't exist
                entity = DomainMapper.toEntity(visit);
            }
        } else {
            // Create new visit
            entity = DomainMapper.toEntity(visit);
        }

        VisitEntity savedEntity = jpaRepository.save(entity);
        return DomainMapper.toDomain(savedEntity);
    }

    @Override
    public void deleteById(Integer id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Integer id) {
        return jpaRepository.existsById(id);
    }
}
