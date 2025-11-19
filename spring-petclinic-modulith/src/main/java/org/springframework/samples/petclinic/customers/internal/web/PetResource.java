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
package org.springframework.samples.petclinic.customers.internal.web;

import io.micrometer.core.annotation.Timed;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.customers.Customer;
import org.springframework.samples.petclinic.customers.PetAdded;
import org.springframework.samples.petclinic.customers.internal.CustomerRepository;
import org.springframework.samples.petclinic.customers.internal.Pet;
import org.springframework.samples.petclinic.customers.internal.PetRepository;
import org.springframework.samples.petclinic.customers.internal.PetType;
import org.springframework.samples.petclinic.customers.internal.PetTypeRepository;
import org.springframework.samples.petclinic.shared.exceptions.ResourceNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing pets.
 * 
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Maciej Szarlinski
 * @author Ramazan Sakin
 */
@RestController
@Timed("petclinic.pet")
class PetResource {

    private static final Logger log = LoggerFactory.getLogger(PetResource.class);

    private final PetRepository petRepository;
    private final CustomerRepository customerRepository;
    private final PetTypeRepository petTypeRepository;
    private final ApplicationEventPublisher events;

    PetResource(PetRepository petRepository, 
                CustomerRepository customerRepository,
                PetTypeRepository petTypeRepository,
                ApplicationEventPublisher events) {
        this.petRepository = petRepository;
        this.customerRepository = customerRepository;
        this.petTypeRepository = petTypeRepository;
        this.events = events;
    }

    @GetMapping("/petTypes")
    public List<PetType> getPetTypes() {
        return petTypeRepository.findAll();
    }

    @PostMapping("/owners/{ownerId}/pets")
    @ResponseStatus(HttpStatus.CREATED)
    public Pet processCreationForm(
        @RequestBody PetRequest petRequest,
        @PathVariable("ownerId") @Min(1) int ownerId) {

        Customer owner = customerRepository.findById(ownerId)
            .orElseThrow(() -> new ResourceNotFoundException("Owner", ownerId));

        final Pet pet = new Pet();
        owner.addPet(pet);
        Pet savedPet = save(pet, petRequest);
        
        // Publish domain event
        events.publishEvent(new PetAdded(
            savedPet.getId(),
            ownerId,
            savedPet.getName()
        ));
        
        return savedPet;
    }

    @PutMapping("/owners/*/pets/{petId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void processUpdateForm(@RequestBody PetRequest petRequest) {
        int petId = petRequest.id();
        Pet pet = findPetById(petId);
        save(pet, petRequest);
    }

    private Pet save(final Pet pet, final PetRequest petRequest) {
        pet.setName(petRequest.name());
        pet.setBirthDate(petRequest.birthDate());

        petTypeRepository.findById(petRequest.typeId())
            .ifPresent(pet::setType);

        log.info("Saving pet {}", pet);
        return petRepository.save(pet);
    }

    @GetMapping("owners/*/pets/{petId}")
    public PetDetails findPet(@PathVariable("petId") int petId) {
        Pet pet = findPetById(petId);
        return new PetDetails(pet);
    }

    private Pet findPetById(int petId) {
        return petRepository.findById(petId)
            .orElseThrow(() -> new ResourceNotFoundException("Pet", petId));
    }
}
