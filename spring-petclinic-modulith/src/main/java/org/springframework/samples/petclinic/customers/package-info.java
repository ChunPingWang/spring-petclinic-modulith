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

/**
 * Customers module - manages customer and pet information.
 * 
 * <h2>Public API</h2>
 * This module exposes the following public API:
 * <ul>
 *   <li>{@link org.springframework.samples.petclinic.customers.Customer} - Customer entity</li>
 *   <li>{@link org.springframework.samples.petclinic.customers.CustomerService} - Customer service interface</li>
 *   <li>{@link org.springframework.samples.petclinic.customers.CustomerCreated} - Domain event</li>
 *   <li>{@link org.springframework.samples.petclinic.customers.CustomerUpdated} - Domain event</li>
 *   <li>{@link org.springframework.samples.petclinic.customers.PetAdded} - Domain event</li>
 * </ul>
 * 
 * <h2>Internal Implementation</h2>
 * The internal implementation is located in the {@code internal} subpackage and includes:
 * <ul>
 *   <li>Repository layer - JPA repositories</li>
 *   <li>Service implementation - Business logic</li>
 *   <li>Web layer - REST controllers</li>
 *   <li>Internal entities - Pet, PetType</li>
 * </ul>
 * 
 * <h2>Module Dependencies</h2>
 * This module has no dependencies on other application modules.
 * It only depends on the shared module for common exceptions and configuration.
 * 
 * @author PetClinic Team
 */
package org.springframework.samples.petclinic.customers;
