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
package org.springframework.samples.petclinic.customers.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.customers.Customer;
import org.springframework.samples.petclinic.customers.CustomerService;
import org.springframework.samples.petclinic.customers.business.exception.CustomerNotFoundException;
import org.springframework.samples.petclinic.customers.business.service.CustomerBusinessService;
import org.springframework.samples.petclinic.customers.infrastructure.persistence.mapper.DomainMapper;
import org.springframework.samples.petclinic.shared.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Internal implementation of CustomerService.
 *
 * This implementation delegates to the pure Java business layer (CustomerBusinessService)
 * while maintaining backward compatibility with the existing public API.
 * It converts between the old Customer entity and new domain model.
 *
 * @author PetClinic Team
 */
@Service
@Transactional
class CustomerServiceImpl implements CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerServiceImpl.class);

    private final CustomerBusinessService businessService;

    CustomerServiceImpl(CustomerBusinessService businessService) {
        this.businessService = businessService;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> findById(Integer customerId) {
        log.debug("Finding customer by ID: {}", customerId);

        // Delegate to business layer and convert domain model to legacy entity
        return businessService.findById(customerId)
                .map(DomainMapper::toLegacyEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> findAll() {
        log.debug("Finding all customers");

        // Delegate to business layer and convert domain models to legacy entities
        return businessService.findAll().stream()
                .map(DomainMapper::toLegacyEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Customer save(Customer customer) {
        log.info("Creating new customer: {}", customer.getFullName());

        // Convert legacy entity to domain model
        org.springframework.samples.petclinic.customers.domain.Customer domainCustomer =
                DomainMapper.fromLegacyEntity(customer);

        // Delegate to business layer
        org.springframework.samples.petclinic.customers.domain.Customer savedDomainCustomer =
                businessService.createCustomer(domainCustomer);

        // Convert back to legacy entity
        Customer savedCustomer = DomainMapper.toLegacyEntity(savedDomainCustomer);

        log.info("Customer created with ID: {}", savedCustomer.getId());
        return savedCustomer;
    }

    @Override
    public Customer update(Integer customerId, Customer customer) {
        log.info("Updating customer ID: {}", customerId);

        // Convert legacy entity to domain model
        org.springframework.samples.petclinic.customers.domain.Customer domainCustomer =
                DomainMapper.fromLegacyEntity(customer);

        try {
            // Delegate to business layer
            org.springframework.samples.petclinic.customers.domain.Customer updatedDomainCustomer =
                    businessService.updateCustomer(customerId, domainCustomer);

            // Convert back to legacy entity
            Customer updatedCustomer = DomainMapper.toLegacyEntity(updatedDomainCustomer);

            log.info("Customer updated: {}", updatedCustomer.getId());
            return updatedCustomer;
        } catch (CustomerNotFoundException e) {
            throw new ResourceNotFoundException("Customer", customerId);
        }
    }

    @Override
    public void deleteById(Integer customerId) {
        log.info("Deleting customer ID: {}", customerId);

        try {
            // Delegate to business layer
            businessService.deleteCustomer(customerId);

            log.info("Customer deleted: {}", customerId);
        } catch (CustomerNotFoundException e) {
            throw new ResourceNotFoundException("Customer", customerId);
        }
    }
}
