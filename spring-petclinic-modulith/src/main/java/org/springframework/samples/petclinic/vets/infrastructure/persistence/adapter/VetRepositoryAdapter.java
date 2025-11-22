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
package org.springframework.samples.petclinic.vets.infrastructure.persistence.adapter;

import org.springframework.samples.petclinic.vets.business.port.VetRepository;
import org.springframework.samples.petclinic.vets.domain.Vet;
import org.springframework.samples.petclinic.vets.infrastructure.persistence.entity.VetEntity;
import org.springframework.samples.petclinic.vets.infrastructure.persistence.jpa.VetJpaRepository;
import org.springframework.samples.petclinic.vets.infrastructure.persistence.mapper.DomainMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementation of VetRepository port.
 *
 * This adapter bridges the pure Java business layer with the Spring Data JPA infrastructure.
 * It converts between domain models (Vet) and JPA entities (VetEntity).
 *
 * @author PetClinic Team
 */
@Component
public class VetRepositoryAdapter implements VetRepository {

    private final VetJpaRepository jpaRepository;

    public VetRepositoryAdapter(VetJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Vet> findById(Integer id) {
        return jpaRepository.findById(id)
                .map(DomainMapper::toDomain);
    }

    @Override
    public List<Vet> findAll() {
        return jpaRepository.findAll().stream()
                .map(DomainMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Vet save(Vet vet) {
        VetEntity entity;

        if (vet.getId() != null) {
            // Update existing vet
            Optional<VetEntity> existingEntity = jpaRepository.findById(vet.getId());
            if (existingEntity.isPresent()) {
                entity = existingEntity.get();
                DomainMapper.updateEntity(entity, vet);
            } else {
                // Create new entity if ID doesn't exist
                entity = DomainMapper.toEntity(vet);
            }
        } else {
            // Create new vet
            entity = DomainMapper.toEntity(vet);
        }

        VetEntity savedEntity = jpaRepository.save(entity);
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
