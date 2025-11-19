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
package org.springframework.samples.petclinic.customers;

import java.util.List;
import java.util.Optional;

/**
 * Public CustomerService interface that other modules can depend on.
 * 
 * This is the only way for other modules to access customer functionality.
 * 
 * @author PetClinic Team
 */
public interface CustomerService {
    
    /**
     * Find a customer by ID.
     * 
     * @param customerId the customer ID
     * @return the customer if found
     */
    Optional<Customer> findById(Integer customerId);
    
    /**
     * Find all customers.
     * 
     * @return list of all customers
     */
    List<Customer> findAll();
    
    /**
     * Create a new customer.
     * 
     * @param customer the customer to create
     * @return the created customer with ID
     */
    Customer save(Customer customer);
    
    /**
     * Update an existing customer.
     * 
     * @param customerId the customer ID
     * @param customer the customer data to update
     * @return the updated customer
     */
    Customer update(Integer customerId, Customer customer);
}
