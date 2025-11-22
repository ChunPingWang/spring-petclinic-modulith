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
package org.springframework.samples.petclinic.customers.infrastructure.persistence.mapper;

import org.springframework.samples.petclinic.customers.domain.Customer;
import org.springframework.samples.petclinic.customers.domain.Pet;
import org.springframework.samples.petclinic.customers.domain.PetType;
import org.springframework.samples.petclinic.customers.infrastructure.persistence.entity.CustomerEntity;
import org.springframework.samples.petclinic.customers.infrastructure.persistence.entity.PetEntity;
import org.springframework.samples.petclinic.customers.infrastructure.persistence.entity.PetTypeEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper to convert between Domain models and JPA Entities.
 *
 * This class belongs to Infrastructure layer and bridges
 * the pure domain model with JPA persistence layer.
 *
 * @author PetClinic Team
 */
public class DomainMapper {

    /**
     * Convert CustomerEntity to Domain Customer.
     */
    public static Customer toDomain(CustomerEntity entity) {
        if (entity == null) {
            return null;
        }

        Customer customer = new Customer();
        customer.setId(entity.getId());
        customer.setFirstName(entity.getFirstName());
        customer.setLastName(entity.getLastName());
        customer.setAddress(entity.getAddress());
        customer.setCity(entity.getCity());
        customer.setTelephone(entity.getTelephone());

        // Convert pets
        if (entity.getPets() != null) {
            List<Pet> pets = entity.getPets().stream()
                    .map(DomainMapper::toDomain)
                    .collect(Collectors.toList());
            customer.setPets(pets);

            // Set owner reference in pets
            for (Pet pet : pets) {
                pet.setOwner(customer);
            }
        }

        return customer;
    }

    /**
     * Convert Domain Customer to CustomerEntity.
     */
    public static CustomerEntity toEntity(Customer domain) {
        if (domain == null) {
            return null;
        }

        CustomerEntity entity = new CustomerEntity();
        entity.setId(domain.getId());
        entity.setFirstName(domain.getFirstName());
        entity.setLastName(domain.getLastName());
        entity.setAddress(domain.getAddress());
        entity.setCity(domain.getCity());
        entity.setTelephone(domain.getTelephone());

        // Convert pets
        if (domain.getPets() != null) {
            for (Pet pet : domain.getPets()) {
                PetEntity petEntity = toEntity(pet);
                entity.addPet(petEntity);
            }
        }

        return entity;
    }

    /**
     * Convert PetEntity to Domain Pet.
     */
    public static Pet toDomain(PetEntity entity) {
        if (entity == null) {
            return null;
        }

        Pet pet = new Pet();
        pet.setId(entity.getId());
        pet.setName(entity.getName());
        pet.setBirthDate(entity.getBirthDate());

        if (entity.getType() != null) {
            pet.setType(toDomain(entity.getType()));
        }

        // Owner is set by the caller to avoid circular references

        return pet;
    }

    /**
     * Convert Domain Pet to PetEntity.
     */
    public static PetEntity toEntity(Pet domain) {
        if (domain == null) {
            return null;
        }

        PetEntity entity = new PetEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setBirthDate(domain.getBirthDate());

        if (domain.getType() != null) {
            entity.setType(toEntity(domain.getType()));
        }

        // Owner is set by the caller

        return entity;
    }

    /**
     * Convert PetTypeEntity to Domain PetType.
     */
    public static PetType toDomain(PetTypeEntity entity) {
        if (entity == null) {
            return null;
        }

        return new PetType(entity.getId(), entity.getName());
    }

    /**
     * Convert Domain PetType to PetTypeEntity.
     */
    public static PetTypeEntity toEntity(PetType domain) {
        if (domain == null) {
            return null;
        }

        PetTypeEntity entity = new PetTypeEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        return entity;
    }

    /**
     * Update existing CustomerEntity with domain data.
     */
    public static void updateEntity(CustomerEntity entity, Customer domain) {
        if (entity == null || domain == null) {
            return;
        }

        entity.setFirstName(domain.getFirstName());
        entity.setLastName(domain.getLastName());
        entity.setAddress(domain.getAddress());
        entity.setCity(domain.getCity());
        entity.setTelephone(domain.getTelephone());
    }

    /**
     * Convert old public API Customer entity to new domain Customer.
     * This is for backward compatibility during migration.
     */
    public static Customer fromLegacyEntity(org.springframework.samples.petclinic.customers.Customer legacyEntity) {
        if (legacyEntity == null) {
            return null;
        }

        Customer customer = new Customer();
        customer.setId(legacyEntity.getId());
        customer.setFirstName(legacyEntity.getFirstName());
        customer.setLastName(legacyEntity.getLastName());
        customer.setAddress(legacyEntity.getAddress());
        customer.setCity(legacyEntity.getCity());
        customer.setTelephone(legacyEntity.getTelephone());

        // Note: pets conversion would require additional mapping
        // For now, we skip pets to avoid complexity during migration

        return customer;
    }

    /**
     * Convert new domain Customer to old public API Customer entity.
     * This is for backward compatibility during migration.
     */
    public static org.springframework.samples.petclinic.customers.Customer toLegacyEntity(Customer domain) {
        if (domain == null) {
            return null;
        }

        org.springframework.samples.petclinic.customers.Customer legacyEntity =
                new org.springframework.samples.petclinic.customers.Customer();
        legacyEntity.setId(domain.getId());
        legacyEntity.setFirstName(domain.getFirstName());
        legacyEntity.setLastName(domain.getLastName());
        legacyEntity.setAddress(domain.getAddress());
        legacyEntity.setCity(domain.getCity());
        legacyEntity.setTelephone(domain.getTelephone());

        // Note: pets conversion would require additional mapping
        // For now, we skip pets to avoid complexity during migration

        return legacyEntity;
    }
}
