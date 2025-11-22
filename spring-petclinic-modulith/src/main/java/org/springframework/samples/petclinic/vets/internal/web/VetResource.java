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
package org.springframework.samples.petclinic.vets.internal.web;

import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.vets.Vet;
import org.springframework.samples.petclinic.vets.VetService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * REST controller for managing vets.
 * 
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Maciej Szarlinski
 * @author PetClinic Team
 */
@RequestMapping("/vets")
@RestController
@Timed("petclinic.vet")
class VetResource {

    private static final Logger log = LoggerFactory.getLogger(VetResource.class);
    private final VetService vetService;

    VetResource(VetService vetService) {
        this.vetService = vetService;
    }

    /**
     * Get all vets.
     */
    @GetMapping
    @Cacheable("vets")
    public List<Vet> showResourcesVetList() {
        return vetService.findAll();
    }

    /**
     * Get a specific vet by ID.
     */
    @GetMapping("/{vetId}")
    public Vet findVet(@PathVariable("vetId") @Min(1) int vetId) {
        return vetService.findById(vetId)
            .orElseThrow(() -> new NoSuchElementException("Vet not found with id: " + vetId));
    }

    /**
     * Create a new vet.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @CacheEvict(value = "vets", allEntries = true)
    public Vet createVet(@Valid @RequestBody Vet vet) {
        log.info("Creating new vet: {} {}", vet.getFirstName(), vet.getLastName());
        return vetService.save(vet);
    }

    /**
     * Update an existing vet.
     */
    @PutMapping("/{vetId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @CacheEvict(value = "vets", allEntries = true)
    public void updateVet(@PathVariable("vetId") @Min(1) int vetId,
                         @Valid @RequestBody Vet vet) {
        log.info("Updating vet with id: {}", vetId);
        vetService.update(vetId, vet);
    }

    /**
     * Delete a vet.
     */
    @DeleteMapping("/{vetId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @CacheEvict(value = "vets", allEntries = true)
    public void deleteVet(@PathVariable("vetId") @Min(1) int vetId) {
        log.info("Deleting vet with id: {}", vetId);
        vetService.findById(vetId)
            .orElseThrow(() -> new NoSuchElementException("Vet not found with id: " + vetId));
        vetService.deleteById(vetId);
    }
}
