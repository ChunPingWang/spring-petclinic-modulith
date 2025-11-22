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
package org.springframework.samples.petclinic.vets;

import java.util.List;
import java.util.Optional;

/**
 * Public VetService interface that other modules can depend on.
 * 
 * This is the only way for other modules to access vet functionality.
 * 
 * @author PetClinic Team
 */
public interface VetService {
    
    /**
     * Find a vet by ID.
     * 
     * @param vetId the vet ID
     * @return the vet if found
     */
    Optional<Vet> findById(Integer vetId);
    
    /**
     * Find all vets.
     * 
     * @return list of all vets
     */
    List<Vet> findAll();
    
    /**
     * Create a new vet.
     * 
     * @param vet the vet to create
     * @return the created vet with ID
     */
    Vet save(Vet vet);
    
    /**
     * Update an existing vet.
     *
     * @param vetId the vet ID
     * @param vet the vet data to update
     * @return the updated vet
     */
    Vet update(Integer vetId, Vet vet);

    /**
     * Delete a vet by ID.
     *
     * @param vetId the vet ID
     */
    void deleteById(Integer vetId);
}
