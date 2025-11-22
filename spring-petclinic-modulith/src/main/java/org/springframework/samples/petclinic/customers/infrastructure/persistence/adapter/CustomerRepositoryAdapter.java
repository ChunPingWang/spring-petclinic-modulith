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
package org.springframework.samples.petclinic.customers.infrastructure.persistence.adapter;

import org.springframework.samples.petclinic.customers.business.port.CustomerRepository;
import org.springframework.samples.petclinic.customers.domain.Customer;
import org.springframework.samples.petclinic.customers.infrastructure.persistence.entity.CustomerEntity;
import org.springframework.samples.petclinic.customers.infrastructure.persistence.jpa.CustomerJpaRepository;
import org.springframework.samples.petclinic.customers.infrastructure.persistence.mapper.DomainMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter that implements the business layer's CustomerRepository port
 * using Spring Data JPA infrastructure.
 *
 * This class bridges the pure Java business layer with the Spring/JPA
 * infrastructure layer, following the Dependency Inversion Principle.
 *
 * @author PetClinic Team
 */
@Component
public class CustomerRepositoryAdapter implements CustomerRepository {

    private final CustomerJpaRepository jpaRepository;

    public CustomerRepositoryAdapter(CustomerJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Customer> findById(Integer id) {
        return jpaRepository.findById(id)
                .map(DomainMapper::toDomain);
    }

    @Override
    public List<Customer> findAll() {
        return jpaRepository.findAll().stream()
                .map(DomainMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Customer save(Customer customer) {
        CustomerEntity entity;

        if (customer.getId() != null) {
            // Update existing customer
            Optional<CustomerEntity> existingEntity = jpaRepository.findById(customer.getId());
            if (existingEntity.isPresent()) {
                entity = existingEntity.get();
                DomainMapper.updateEntity(entity, customer);
            } else {
                entity = DomainMapper.toEntity(customer);
            }
        } else {
            // Create new customer
            entity = DomainMapper.toEntity(customer);
        }

        CustomerEntity savedEntity = jpaRepository.save(entity);
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
