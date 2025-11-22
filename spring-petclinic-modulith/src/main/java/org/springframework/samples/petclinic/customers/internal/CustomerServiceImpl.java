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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.samples.petclinic.customers.Customer;
import org.springframework.samples.petclinic.customers.CustomerCreated;
import org.springframework.samples.petclinic.customers.CustomerDeleted;
import org.springframework.samples.petclinic.customers.CustomerService;
import org.springframework.samples.petclinic.customers.CustomerUpdated;
import org.springframework.samples.petclinic.shared.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Internal implementation of CustomerService.
 * 
 * This implementation handles business logic and publishes domain events.
 * 
 * @author PetClinic Team
 */
@Service
@Transactional
class CustomerServiceImpl implements CustomerService {
    
    private static final Logger log = LoggerFactory.getLogger(CustomerServiceImpl.class);
    
    private final CustomerRepository customerRepository;
    private final ApplicationEventPublisher events;
    
    CustomerServiceImpl(CustomerRepository customerRepository, ApplicationEventPublisher events) {
        this.customerRepository = customerRepository;
        this.events = events;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> findById(Integer customerId) {
        log.debug("Finding customer by ID: {}", customerId);
        return customerRepository.findById(customerId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Customer> findAll() {
        log.debug("Finding all customers");
        return customerRepository.findAll();
    }
    
    @Override
    public Customer save(Customer customer) {
        log.info("Creating new customer: {}", customer.getFullName());
        
        Customer savedCustomer = customerRepository.save(customer);
        
        // Publish domain event
        events.publishEvent(new CustomerCreated(
            savedCustomer.getId(), 
            savedCustomer.getFullName()
        ));
        
        log.info("Customer created with ID: {}", savedCustomer.getId());
        return savedCustomer;
    }
    
    @Override
    public Customer update(Integer customerId, Customer customer) {
        log.info("Updating customer ID: {}", customerId);

        Customer existingCustomer = customerRepository.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));

        // Update fields
        existingCustomer.setFirstName(customer.getFirstName());
        existingCustomer.setLastName(customer.getLastName());
        existingCustomer.setAddress(customer.getAddress());
        existingCustomer.setCity(customer.getCity());
        existingCustomer.setTelephone(customer.getTelephone());

        Customer updatedCustomer = customerRepository.save(existingCustomer);

        // Publish domain event
        events.publishEvent(new CustomerUpdated(
            updatedCustomer.getId(),
            updatedCustomer.getFullName()
        ));

        log.info("Customer updated: {}", updatedCustomer.getId());
        return updatedCustomer;
    }

    @Override
    public void deleteById(Integer customerId) {
        log.info("Deleting customer ID: {}", customerId);

        Customer existingCustomer = customerRepository.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));

        customerRepository.deleteById(customerId);

        // Publish domain event
        events.publishEvent(new CustomerDeleted(
            existingCustomer.getId(),
            existingCustomer.getFullName()
        ));

        log.info("Customer deleted: {} {}", existingCustomer.getFirstName(), existingCustomer.getLastName());
    }
}
