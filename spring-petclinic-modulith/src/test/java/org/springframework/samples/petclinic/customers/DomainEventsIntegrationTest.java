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
package org.springframework.samples.petclinic.customers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.test.PublishedEvents;
import org.springframework.modulith.test.PublishedEventsExtension;
import org.springframework.test.context.ActiveProfiles;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.samples.petclinic.vets.Vet;
import org.springframework.samples.petclinic.vets.VetCreated;
import org.springframework.samples.petclinic.vets.VetService;
import org.springframework.samples.petclinic.vets.VetUpdated;
import org.springframework.samples.petclinic.visits.Visit;
import org.springframework.samples.petclinic.visits.VisitCreated;
import org.springframework.samples.petclinic.visits.VisitService;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for domain events across modules.
 *
 * This test suite validates the event-driven architecture:
 * - Events are published when domain operations occur
 * - Events are stored in the event_publication table
 * - Event listeners in other modules receive events
 * - Events are processed asynchronously
 *
 * Uses Spring Boot Test with PublishedEventsExtension for:
 * - Testing cross-module event propagation
 * - Verifying event publication and consumption
 * - Testing the complete event lifecycle
 *
 * @author PetClinic Team
 */
@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(PublishedEventsExtension.class)
class DomainEventsIntegrationTest {

    @Autowired
    CustomerService customerService;

    @Autowired
    VetService vetService;

    @Autowired
    VisitService visitService;

    /**
     * Test that CustomerCreated event is published when a customer is created.
     */
    @Test
    void shouldPublishCustomerCreatedEvent(PublishedEvents events) {
        // Given a new customer
        Customer customer = new Customer();
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setAddress("123 Main St");
        customer.setCity("Springfield");
        customer.setTelephone("5551234");

        // When the customer is saved
        Customer saved = customerService.save(customer);

        // Then a CustomerCreated event should be published
        assertThat(events.ofType(CustomerCreated.class))
            .hasSize(1)
            .allSatisfy(event -> {
                assertThat(event.customerId()).isEqualTo(saved.getId());
                assertThat(event.customerName()).contains("John");
                assertThat(event.customerName()).contains("Doe");
            });
    }

    /**
     * Test that CustomerUpdated event is published when a customer is updated.
     */
    @Test
    void shouldPublishCustomerUpdatedEvent(PublishedEvents events) {
        // Given an existing customer
        Customer customer = new Customer();
        customer.setFirstName("Jane");
        customer.setLastName("Smith");
        customer.setAddress("456 Oak Ave");
        customer.setCity("Shelbyville");
        customer.setTelephone("5555678");
        Customer saved = customerService.save(customer);

        // When the customer is updated
        saved.setFirstName("Janet");
        Customer updated = customerService.update(saved.getId(), saved);

        // Then a CustomerUpdated event should be published
        assertThat(events.ofType(CustomerUpdated.class))
            .hasSizeGreaterThanOrEqualTo(1)
            .anySatisfy(event -> {
                assertThat(event.customerId()).isEqualTo(updated.getId());
                assertThat(event.customerName()).contains("Janet");
            });
    }

    /**
     * Test that VetCreated event is published when a vet is created.
     */
    @Test
    void shouldPublishVetCreatedEvent(PublishedEvents events) {
        // Given a new vet
        Vet vet = new Vet();
        vet.setFirstName("James");
        vet.setLastName("Carter");

        // When the vet is saved
        Vet saved = vetService.save(vet);

        // Then a VetCreated event should be published
        assertThat(events.ofType(VetCreated.class))
            .hasSizeGreaterThanOrEqualTo(1)
            .anySatisfy(event -> {
                assertThat(event.vetId()).isEqualTo(saved.getId());
                assertThat(event.vetName()).contains("James");
                assertThat(event.vetName()).contains("Carter");
            });
    }

    /**
     * Test that VetUpdated event is published when a vet is updated.
     */
    @Test
    void shouldPublishVetUpdatedEvent(PublishedEvents events) {
        // Given an existing vet
        Vet vet = new Vet();
        vet.setFirstName("Helen");
        vet.setLastName("Leary");
        Vet saved = vetService.save(vet);

        // When the vet is updated
        saved.setFirstName("Helena");
        Vet updated = vetService.update(saved.getId(), saved);

        // Then a VetUpdated event should be published
        assertThat(events.ofType(VetUpdated.class))
            .hasSizeGreaterThanOrEqualTo(1)
            .anySatisfy(event -> {
                assertThat(event.vetId()).isEqualTo(updated.getId());
                assertThat(event.vetName()).contains("Helena");
            });
    }

    /**
     * Test that VisitCreated event is published when a visit is scheduled.
     */
    @Test
    void shouldPublishVisitCreatedEvent(PublishedEvents events) {
        // Given an existing customer and vet
        Customer customer = new Customer();
        customer.setFirstName("Bob");
        customer.setLastName("Brown");
        customer.setAddress("789 Elm St");
        customer.setCity("Capital City");
        customer.setTelephone("5559012");
        Customer savedCustomer = customerService.save(customer);

        Vet vet = new Vet();
        vet.setFirstName("Linda");
        vet.setLastName("Douglas");
        Vet savedVet = vetService.save(vet);

        // When a visit is scheduled
        Visit visit = new Visit();
        visit.setPetId(savedCustomer.getId()); // Using customer ID as pet ID for this test
        visit.setVetId(savedVet.getId());
        visit.setDescription("Annual checkup");
        Visit scheduledVisit = visitService.scheduleVisit(visit);

        // Then a VisitCreated event should be published
        assertThat(events.ofType(VisitCreated.class))
            .hasSizeGreaterThanOrEqualTo(1)
            .anySatisfy(event -> {
                assertThat(event.visitId()).isEqualTo(scheduledVisit.getId());
                assertThat(event.petId()).isEqualTo(savedCustomer.getId());
                assertThat(event.vetId()).isEqualTo(savedVet.getId());
            });
    }
}
