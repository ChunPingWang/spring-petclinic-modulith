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
package org.springframework.samples.petclinic.customers.business.port;

import org.springframework.samples.petclinic.customers.domain.Customer;

import java.util.List;
import java.util.Optional;

/**
 * Pure Java repository interface (Port) for Customer persistence.
 *
 * This is a business layer interface with NO framework dependencies.
 * The infrastructure layer will provide the implementation.
 *
 * @author PetClinic Team
 */
public interface CustomerRepository {

    /**
     * Find a customer by ID.
     *
     * @param id the customer ID
     * @return the customer if found
     */
    Optional<Customer> findById(Integer id);

    /**
     * Find all customers.
     *
     * @return list of all customers
     */
    List<Customer> findAll();

    /**
     * Save a customer (create or update).
     *
     * @param customer the customer to save
     * @return the saved customer with ID
     */
    Customer save(Customer customer);

    /**
     * Delete a customer by ID.
     *
     * @param id the customer ID to delete
     */
    void deleteById(Integer id);

    /**
     * Check if a customer exists by ID.
     *
     * @param id the customer ID
     * @return true if exists
     */
    boolean existsById(Integer id);
}
