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
package org.springframework.samples.petclinic.customers.business.service;

import org.springframework.samples.petclinic.customers.business.exception.CustomerNotFoundException;
import org.springframework.samples.petclinic.customers.business.port.CustomerRepository;
import org.springframework.samples.petclinic.customers.business.port.EventPublisher;
import org.springframework.samples.petclinic.customers.domain.Customer;
import org.springframework.samples.petclinic.customers.CustomerCreated;
import org.springframework.samples.petclinic.customers.CustomerUpdated;
import org.springframework.samples.petclinic.customers.CustomerDeleted;

import java.util.List;
import java.util.Optional;

/**
 * Pure Java business service for Customer operations.
 *
 * This class contains ONLY business logic with NO framework dependencies.
 * No Spring annotations, no JPA, no validation framework.
 *
 * Dependencies are injected through constructor (Dependency Inversion Principle).
 *
 * @author PetClinic Team
 */
public class CustomerBusinessService {

    private final CustomerRepository customerRepository;
    private final EventPublisher eventPublisher;

    public CustomerBusinessService(CustomerRepository customerRepository,
                                   EventPublisher eventPublisher) {
        if (customerRepository == null) {
            throw new IllegalArgumentException("CustomerRepository cannot be null");
        }
        if (eventPublisher == null) {
            throw new IllegalArgumentException("EventPublisher cannot be null");
        }
        this.customerRepository = customerRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Find a customer by ID.
     *
     * @param customerId the customer ID
     * @return the customer if found
     */
    public Optional<Customer> findById(Integer customerId) {
        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException("Customer ID must be positive");
        }
        return customerRepository.findById(customerId);
    }

    /**
     * Find all customers.
     *
     * @return list of all customers
     */
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    /**
     * Create a new customer.
     *
     * @param customer the customer to create
     * @return the created customer with ID
     */
    public Customer createCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }
        if (customer.getId() != null) {
            throw new IllegalArgumentException("New customer should not have an ID");
        }

        // Domain validation
        customer.validateForCreation();

        // Save customer
        Customer savedCustomer = customerRepository.save(customer);

        // Publish domain event
        eventPublisher.publish(new CustomerCreated(
            savedCustomer.getId(),
            savedCustomer.getFullName()
        ));

        return savedCustomer;
    }

    /**
     * Update an existing customer.
     *
     * @param customerId the customer ID
     * @param customerData the customer data to update
     * @return the updated customer
     */
    public Customer updateCustomer(Integer customerId, Customer customerData) {
        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException("Customer ID must be positive");
        }
        if (customerData == null) {
            throw new IllegalArgumentException("Customer data cannot be null");
        }

        // Find existing customer
        Customer existingCustomer = customerRepository.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + customerId, customerId));

        // Domain validation
        customerData.validateForCreation();

        // Update fields
        existingCustomer.setFirstName(customerData.getFirstName());
        existingCustomer.setLastName(customerData.getLastName());
        existingCustomer.setAddress(customerData.getAddress());
        existingCustomer.setCity(customerData.getCity());
        existingCustomer.setTelephone(customerData.getTelephone());

        // Save updated customer
        Customer updatedCustomer = customerRepository.save(existingCustomer);

        // Publish domain event
        eventPublisher.publish(new CustomerUpdated(
            updatedCustomer.getId(),
            updatedCustomer.getFullName()
        ));

        return updatedCustomer;
    }

    /**
     * Delete a customer by ID.
     *
     * @param customerId the customer ID to delete
     */
    public void deleteCustomer(Integer customerId) {
        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException("Customer ID must be positive");
        }

        // Find existing customer
        Customer existingCustomer = customerRepository.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + customerId, customerId));

        // Delete customer
        customerRepository.deleteById(customerId);

        // Publish domain event
        eventPublisher.publish(new CustomerDeleted(
            existingCustomer.getId(),
            existingCustomer.getFullName()
        ));
    }
}
