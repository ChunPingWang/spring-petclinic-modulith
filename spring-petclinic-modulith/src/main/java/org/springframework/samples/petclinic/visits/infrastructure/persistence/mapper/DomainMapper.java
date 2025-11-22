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
package org.springframework.samples.petclinic.visits.infrastructure.persistence.mapper;

import org.springframework.samples.petclinic.visits.domain.Visit;
import org.springframework.samples.petclinic.visits.domain.VisitStatus;
import org.springframework.samples.petclinic.visits.infrastructure.persistence.entity.VisitEntity;

/**
 * Mapper for converting between domain models and JPA entities.
 *
 * This mapper provides bidirectional conversion between:
 * - Domain models (Visit) - pure Java POJOs
 * - JPA entities (VisitEntity) - infrastructure layer
 * - Legacy entities (org.springframework.samples.petclinic.visits.Visit) - backward compatibility
 *
 * @author PetClinic Team
 */
public class DomainMapper {

    // ==================== Domain to Entity ====================

    /**
     * Convert domain Visit to VisitEntity.
     */
    public static VisitEntity toEntity(Visit domain) {
        if (domain == null) {
            return null;
        }

        VisitEntity entity = new VisitEntity();
        entity.setId(domain.getId());
        entity.setPetId(domain.getPetId());
        entity.setVetId(domain.getVetId());
        entity.setVisitDate(domain.getVisitDate());
        entity.setDescription(domain.getDescription());
        entity.setStatus(domain.getStatus() != null ? domain.getStatus().name() : null);

        return entity;
    }

    // ==================== Entity to Domain ====================

    /**
     * Convert VisitEntity to domain Visit.
     */
    public static Visit toDomain(VisitEntity entity) {
        if (entity == null) {
            return null;
        }

        Visit domain = new Visit();
        domain.setId(entity.getId());
        domain.setPetId(entity.getPetId());
        domain.setVetId(entity.getVetId());
        domain.setVisitDate(entity.getVisitDate());
        domain.setDescription(entity.getDescription());

        // Convert status string to enum
        if (entity.getStatus() != null) {
            try {
                domain.setStatus(VisitStatus.valueOf(entity.getStatus()));
            } catch (IllegalArgumentException e) {
                // Default to SCHEDULED if invalid status
                domain.setStatus(VisitStatus.SCHEDULED);
            }
        }

        return domain;
    }

    // ==================== Update Entity from Domain ====================

    /**
     * Update existing VisitEntity with data from domain Visit.
     * This is useful for updates where we want to preserve the entity instance.
     */
    public static void updateEntity(VisitEntity entity, Visit domain) {
        if (entity == null || domain == null) {
            return;
        }

        entity.setPetId(domain.getPetId());
        entity.setVetId(domain.getVetId());
        entity.setVisitDate(domain.getVisitDate());
        entity.setDescription(domain.getDescription());
        entity.setStatus(domain.getStatus() != null ? domain.getStatus().name() : null);
    }

    // ==================== Legacy Entity Conversion (for backward compatibility) ====================

    /**
     * Convert legacy public API Visit entity to new domain Visit.
     */
    public static Visit fromLegacyEntity(org.springframework.samples.petclinic.visits.Visit legacyEntity) {
        if (legacyEntity == null) {
            return null;
        }

        Visit domain = new Visit();
        domain.setId(legacyEntity.getId());
        domain.setPetId(legacyEntity.getPetId());
        domain.setVetId(legacyEntity.getVetId());
        domain.setVisitDate(legacyEntity.getVisitDate());
        domain.setDescription(legacyEntity.getDescription());

        // Convert status string to enum
        if (legacyEntity.getStatus() != null) {
            try {
                domain.setStatus(VisitStatus.valueOf(legacyEntity.getStatus()));
            } catch (IllegalArgumentException e) {
                // Default to SCHEDULED if invalid status
                domain.setStatus(VisitStatus.SCHEDULED);
            }
        }

        return domain;
    }

    /**
     * Convert new domain Visit to legacy public API Visit entity.
     */
    public static org.springframework.samples.petclinic.visits.Visit toLegacyEntity(Visit domain) {
        if (domain == null) {
            return null;
        }

        org.springframework.samples.petclinic.visits.Visit legacyEntity =
                new org.springframework.samples.petclinic.visits.Visit();
        legacyEntity.setId(domain.getId());
        legacyEntity.setPetId(domain.getPetId());
        legacyEntity.setVetId(domain.getVetId());
        legacyEntity.setVisitDate(domain.getVisitDate());
        legacyEntity.setDescription(domain.getDescription());
        legacyEntity.setStatus(domain.getStatus() != null ? domain.getStatus().name() : null);

        return legacyEntity;
    }
}
