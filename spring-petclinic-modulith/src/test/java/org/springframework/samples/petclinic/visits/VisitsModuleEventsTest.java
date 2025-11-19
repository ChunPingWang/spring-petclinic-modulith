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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;
import org.springframework.samples.petclinic.customers.Customer;
import org.springframework.samples.petclinic.customers.CustomerService;
import org.springframework.samples.petclinic.vets.Vet;
import org.springframework.samples.petclinic.vets.VetService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for Visits module event publishing.
 * 
 * Tests verify:
 * - Domain events are properly published
 * - Events can be consumed by event listeners in other modules
 * - Cross-module dependencies work correctly
 * 
 * @author PetClinic Team
 */
@ApplicationModuleTest
@ActiveProfiles("test")
@Transactional
class VisitsModuleEventsTest {

    @Autowired
    VisitService visitService;

    @Autowired
    CustomerService customerService;

    @Autowired
    VetService vetService;

    @Test
    void publishesVisitCreatedEvent(Scenario scenario) {
        // Given - set up cross-module entities
        Customer customer = new Customer();
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setAddress("123 Main St");
        customer.setCity("Springfield");
        customer.setTelephone("1234567890");
        Customer savedCustomer = customerService.save(customer);

        Vet vet = new Vet();
        vet.setFirstName("Dr.");
        vet.setLastName("Smith");
        Vet savedVet = vetService.save(vet);

        // When - schedule visit and expect event
        Visit visit = new Visit(savedCustomer.getId(), savedVet.getId());
        visit.setDescription("Routine checkup");

        scenario.stimulate(() -> visitService.scheduleVisit(visit))
            .andWaitForEventOfType(VisitCreated.class)
            .matching(event -> 
                event.petId().equals(savedCustomer.getId()) &&
                event.vetId().equals(savedVet.getId())
            )
            .toArrive();
    }

    @Test
    void publishesVisitCompletedEvent(Scenario scenario) {
        // Given - set up cross-module entities
        Customer customer = new Customer();
        customer.setFirstName("Jane");
        customer.setLastName("Smith");
        customer.setAddress("456 Oak Ave");
        customer.setCity("Portland");
        customer.setTelephone("0987654321");
        Customer savedCustomer = customerService.save(customer);

        Vet vet = new Vet();
        vet.setFirstName("Dr.");
        vet.setLastName("Johnson");
        Vet savedVet = vetService.save(vet);

        // Create and schedule visit first
        Visit visit = new Visit(savedCustomer.getId(), savedVet.getId());
        visit.setDescription("Vaccination");
        Visit scheduledVisit = visitService.scheduleVisit(visit);

        // When - complete visit and expect event
        scenario.stimulate(() -> visitService.completeVisit(scheduledVisit.getId()))
            .andWaitForEventOfType(VisitCompleted.class)
            .matching(event -> event.visitId().equals(scheduledVisit.getId()))
            .toArrive();
    }

    @Test
    void visitServiceIsAvailable() {
        // Verify that VisitService is properly registered as a Spring bean
        assertThat(visitService).isNotNull();
    }

    @Test
    void crossModuleServicesAreAvailable() {
        // Verify that cross-module dependencies are available
        assertThat(customerService).isNotNull();
        assertThat(vetService).isNotNull();
        assertThat(visitService).isNotNull();
    }
}
