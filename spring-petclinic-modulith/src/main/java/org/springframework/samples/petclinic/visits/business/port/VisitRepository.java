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
package org.springframework.samples.petclinic.visits.business.port;

import org.springframework.samples.petclinic.visits.domain.Visit;

import java.util.List;
import java.util.Optional;

/**
 * Pure Java repository interface for Visit operations.
 *
 * This is a port in the hexagonal architecture - a pure Java interface
 * with no framework dependencies. Infrastructure layer will implement this.
 *
 * @author PetClinic Team
 */
public interface VisitRepository {

    Optional<Visit> findById(Integer id);

    List<Visit> findAll();

    List<Visit> findByPetId(Integer petId);

    List<Visit> findByVetId(Integer vetId);

    Visit save(Visit visit);

    void deleteById(Integer id);

    boolean existsById(Integer id);
}
