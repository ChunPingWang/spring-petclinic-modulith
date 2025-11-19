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
 * Vets Module - Veterinarian Management
 * 
 * <h2>Public API</h2>
 * This module exposes the following public API that other modules can depend on:
 * 
 * <ul>
 *   <li>{@link org.springframework.samples.petclinic.vets.Vet} - Veterinarian entity</li>
 *   <li>{@link org.springframework.samples.petclinic.vets.VetService} - Service for vet operations</li>
 *   <li>{@link org.springframework.samples.petclinic.vets.VetCreated} - Domain event</li>
 *   <li>{@link org.springframework.samples.petclinic.vets.VetUpdated} - Domain event</li>
 *   <li>{@link org.springframework.samples.petclinic.vets.SpecialtyAdded} - Domain event</li>
 * </ul>
 * 
 * <h2>Internal Implementation</h2>
 * All implementation details are in the {@code internal} sub-package:
 * <ul>
 *   <li>Repositories (VetRepository, SpecialtyRepository)</li>
 *   <li>Internal entities (Specialty)</li>
 *   <li>Service implementation (VetServiceImpl)</li>
 *   <li>Web layer (REST controllers)</li>
 * </ul>
 * 
 * <p>Other modules should NOT access classes in the {@code internal} package directly.</p>
 * 
 * @author PetClinic Team
 */
package org.springframework.samples.petclinic.vets;
