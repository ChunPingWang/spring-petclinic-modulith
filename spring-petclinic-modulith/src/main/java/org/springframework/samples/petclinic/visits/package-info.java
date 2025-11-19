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
 * Visits Module - Spring Modulith implementation for managing pet visits.
 * 
 * This module demonstrates cross-module communication patterns in Spring Modulith:
 * 
 * <h2>Public API</h2>
 * <ul>
 *   <li>{@link org.springframework.samples.petclinic.visits.Visit} - Visit entity</li>
 *   <li>{@link org.springframework.samples.petclinic.visits.VisitService} - Visit service interface</li>
 *   <li>{@link org.springframework.samples.petclinic.visits.VisitCreated} - Domain event</li>
 *   <li>{@link org.springframework.samples.petclinic.visits.VisitCompleted} - Domain event</li>
 * </ul>
 * 
 * <h2>Dependencies on Other Modules</h2>
 * <ul>
 *   <li><strong>Customers Module</strong>: Uses {@link org.springframework.samples.petclinic.customers.CustomerService}
 *       to validate that pets exist before creating visits</li>
 *   <li><strong>Vets Module</strong>: Uses {@link org.springframework.samples.petclinic.vets.VetService}
 *       to validate that veterinarians exist before creating visits</li>
 * </ul>
 * 
 * <h2>Architecture</h2>
 * <ul>
 *   <li>Public API in root package ({@code org.springframework.samples.petclinic.visits})</li>
 *   <li>Internal implementation in {@code internal} sub-package</li>
 *   <li>Repository and service layer hidden from other modules</li>
 *   <li>Web controllers in {@code internal.web} sub-package</li>
 * </ul>
 * 
 * <h2>Cross-Module Communication Pattern</h2>
 * {@code VisitServiceImpl} demonstrates how to safely depend on public APIs from other modules:
 * <pre>
 * {@code
 * @Service
 * class VisitServiceImpl implements VisitService {
 *     // Inject public services from other modules
 *     private final CustomerService customerService;  // From customers module
 *     private final VetService vetService;            // From vets module
 *     
 *     public Visit scheduleVisit(Visit visit) {
 *         // Validate cross-module references
 *         customerService.findById(visit.getPetId())
 *             .orElseThrow(...);
 *         vetService.findById(visit.getVetId())
 *             .orElseThrow(...);
 *         
 *         Visit saved = visitRepository.save(visit);
 *         events.publishEvent(new VisitCreated(...));
 *         return saved;
 *     }
 * }
 * }
 * </pre>
 * 
 * @author PetClinic Team
 */
package org.springframework.samples.petclinic.visits;
