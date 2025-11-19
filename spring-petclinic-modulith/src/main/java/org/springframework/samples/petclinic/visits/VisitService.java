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
package org.springframework.samples.petclinic.visits;

import java.util.List;
import java.util.Optional;

/**
 * Public service interface for visit management.
 * 
 * Demonstrates cross-module dependency injection:
 * - Uses CustomerService from customers module to validate pet
 * - Uses VetService from vets module to validate veterinarian
 * 
 * @author PetClinic Team
 */
public interface VisitService {
    
    /**
     * Find a visit by its ID.
     */
    Optional<Visit> findById(Integer id);
    
    /**
     * Find all visits.
     */
    List<Visit> findAll();
    
    /**
     * Find all visits for a specific pet.
     */
    List<Visit> findByPetId(Integer petId);
    
    /**
     * Find all visits by a specific veterinarian.
     */
    List<Visit> findByVetId(Integer vetId);
    
    /**
     * Schedule a new visit with validation.
     * 
     * @param visit the visit to schedule
     * @return the created visit
     * @throws org.springframework.samples.petclinic.shared.exceptions.ResourceNotFoundException
     *         if pet or vet not found
     */
    Visit scheduleVisit(Visit visit);
    
    /**
     * Complete a scheduled visit.
     */
    Visit completeVisit(Integer visitId);
    
    /**
     * Cancel a visit.
     */
    void cancelVisit(Integer visitId);
}
