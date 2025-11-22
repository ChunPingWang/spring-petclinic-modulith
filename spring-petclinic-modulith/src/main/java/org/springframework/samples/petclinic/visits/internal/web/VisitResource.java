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
package org.springframework.samples.petclinic.visits.internal.web;

import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.shared.exceptions.ResourceNotFoundException;
import org.springframework.samples.petclinic.visits.Visit;
import org.springframework.samples.petclinic.visits.VisitService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Visit management.
 * 
 * Internal implementation (INTERNAL/web package).
 * Uses VisitService (public API) instead of direct repository access.
 * 
 * Provides endpoints for:
 * - Creating visits with validation
 * - Retrieving visits
 * - Managing visit status
 * 
 * @author PetClinic Team
 */
@RestController
@RequestMapping("/visits")
@Timed("petclinic.visit")
class VisitResource {

    private static final Logger log = LoggerFactory.getLogger(VisitResource.class);

    private final VisitService visitService;

    VisitResource(VisitService visitService) {
        this.visitService = visitService;
    }

    @GetMapping
    public List<Visit> getAllVisits() {
        return visitService.findAll();
    }

    @GetMapping("/{id}")
    public Visit getVisit(@PathVariable("id") @Min(1) Integer id) {
        return visitService.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Visit", id));
    }

    @GetMapping("/pet/{petId}")
    public List<Visit> getVisitsByPet(@PathVariable("petId") @Min(1) Integer petId) {
        return visitService.findByPetId(petId);
    }

    @GetMapping("/vet/{vetId}")
    public List<Visit> getVisitsByVet(@PathVariable("vetId") @Min(1) Integer vetId) {
        return visitService.findByVetId(vetId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Visit scheduleVisit(@Valid @RequestBody Visit visit) {
        log.info("Scheduling visit for pet {} with vet {}", visit.getPetId(), visit.getVetId());
        return visitService.scheduleVisit(visit);
    }

    @PutMapping("/{id}/complete")
    public Visit completeVisit(@PathVariable("id") @Min(1) Integer id) {
        log.info("Completing visit: {}", id);
        return visitService.completeVisit(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelVisit(@PathVariable("id") @Min(1) Integer id) {
        log.info("Cancelling visit: {}", id);
        visitService.cancelVisit(id);
    }
}
