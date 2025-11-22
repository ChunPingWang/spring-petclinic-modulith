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
package org.springframework.samples.petclinic.vets.infrastructure.persistence.mapper;

import org.springframework.samples.petclinic.vets.domain.Specialty;
import org.springframework.samples.petclinic.vets.domain.Vet;
import org.springframework.samples.petclinic.vets.infrastructure.persistence.entity.SpecialtyEntity;
import org.springframework.samples.petclinic.vets.infrastructure.persistence.entity.VetEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for converting between domain models and JPA entities.
 *
 * This mapper provides bidirectional conversion between:
 * - Domain models (Vet, Specialty) - pure Java POJOs
 * - JPA entities (VetEntity, SpecialtyEntity) - infrastructure layer
 * - Legacy entities (org.springframework.samples.petclinic.vets.Vet, etc.) - backward compatibility
 *
 * @author PetClinic Team
 */
public class DomainMapper {

    // ==================== Domain to Entity ====================

    /**
     * Convert domain Vet to VetEntity.
     */
    public static VetEntity toEntity(Vet domain) {
        if (domain == null) {
            return null;
        }

        VetEntity entity = new VetEntity();
        entity.setId(domain.getId());
        entity.setFirstName(domain.getFirstName());
        entity.setLastName(domain.getLastName());

        if (domain.getSpecialties() != null) {
            Set<SpecialtyEntity> specialtyEntities = domain.getSpecialties().stream()
                    .map(DomainMapper::toEntity)
                    .collect(Collectors.toSet());
            entity.setSpecialties(specialtyEntities);
        }

        return entity;
    }

    /**
     * Convert domain Specialty to SpecialtyEntity.
     */
    public static SpecialtyEntity toEntity(Specialty domain) {
        if (domain == null) {
            return null;
        }

        SpecialtyEntity entity = new SpecialtyEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        return entity;
    }

    // ==================== Entity to Domain ====================

    /**
     * Convert VetEntity to domain Vet.
     */
    public static Vet toDomain(VetEntity entity) {
        if (entity == null) {
            return null;
        }

        Vet domain = new Vet();
        domain.setId(entity.getId());
        domain.setFirstName(entity.getFirstName());
        domain.setLastName(entity.getLastName());

        if (entity.getSpecialties() != null) {
            List<Specialty> specialties = entity.getSpecialties().stream()
                    .map(DomainMapper::toDomain)
                    .collect(Collectors.toList());
            domain.setSpecialties(specialties);
        }

        return domain;
    }

    /**
     * Convert SpecialtyEntity to domain Specialty.
     */
    public static Specialty toDomain(SpecialtyEntity entity) {
        if (entity == null) {
            return null;
        }

        Specialty domain = new Specialty();
        domain.setId(entity.getId());
        domain.setName(entity.getName());
        return domain;
    }

    // ==================== Update Entity from Domain ====================

    /**
     * Update existing VetEntity with data from domain Vet.
     * This is useful for updates where we want to preserve the entity instance.
     */
    public static void updateEntity(VetEntity entity, Vet domain) {
        if (entity == null || domain == null) {
            return;
        }

        entity.setFirstName(domain.getFirstName());
        entity.setLastName(domain.getLastName());

        if (domain.getSpecialties() != null) {
            // Convert List to Set for entity
            Set<SpecialtyEntity> specialtyEntities = domain.getSpecialties().stream()
                    .map(DomainMapper::toEntity)
                    .collect(Collectors.toSet());
            entity.setSpecialties(specialtyEntities);
        }
    }

    // ==================== Legacy Entity Conversion (for backward compatibility) ====================

    /**
     * Convert legacy public API Vet entity to new domain Vet.
     */
    public static Vet fromLegacyEntity(org.springframework.samples.petclinic.vets.Vet legacyEntity) {
        if (legacyEntity == null) {
            return null;
        }

        Vet domain = new Vet();
        domain.setId(legacyEntity.getId());
        domain.setFirstName(legacyEntity.getFirstName());
        domain.setLastName(legacyEntity.getLastName());

        if (legacyEntity.getSpecialties() != null && !legacyEntity.getSpecialties().isEmpty()) {
            List<Specialty> specialties = legacyEntity.getSpecialties().stream()
                    .map(DomainMapper::fromLegacyEntity)
                    .collect(Collectors.toList());
            domain.setSpecialties(specialties);
        }

        return domain;
    }

    /**
     * Convert legacy public API Specialty entity to new domain Specialty.
     */
    public static Specialty fromLegacyEntity(org.springframework.samples.petclinic.vets.internal.Specialty legacyEntity) {
        if (legacyEntity == null) {
            return null;
        }

        Specialty domain = new Specialty();
        domain.setId(legacyEntity.getId());
        domain.setName(legacyEntity.getName());
        return domain;
    }

    /**
     * Convert new domain Vet to legacy public API Vet entity.
     */
    public static org.springframework.samples.petclinic.vets.Vet toLegacyEntity(Vet domain) {
        if (domain == null) {
            return null;
        }

        org.springframework.samples.petclinic.vets.Vet legacyEntity =
                new org.springframework.samples.petclinic.vets.Vet();
        legacyEntity.setId(domain.getId());
        legacyEntity.setFirstName(domain.getFirstName());
        legacyEntity.setLastName(domain.getLastName());

        if (domain.getSpecialties() != null && !domain.getSpecialties().isEmpty()) {
            for (Specialty specialty : domain.getSpecialties()) {
                legacyEntity.addSpecialty(toLegacyEntity(specialty));
            }
        }

        return legacyEntity;
    }

    /**
     * Convert new domain Specialty to legacy public API Specialty entity.
     */
    public static org.springframework.samples.petclinic.vets.internal.Specialty toLegacyEntity(Specialty domain) {
        if (domain == null) {
            return null;
        }

        org.springframework.samples.petclinic.vets.internal.Specialty legacyEntity =
                new org.springframework.samples.petclinic.vets.internal.Specialty();
        legacyEntity.setId(domain.getId());
        legacyEntity.setName(domain.getName());
        return legacyEntity;
    }
}
