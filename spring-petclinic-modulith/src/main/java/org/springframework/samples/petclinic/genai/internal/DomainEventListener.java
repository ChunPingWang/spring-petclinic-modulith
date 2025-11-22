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
package org.springframework.samples.petclinic.genai.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.samples.petclinic.customers.CustomerCreated;
import org.springframework.samples.petclinic.customers.CustomerUpdated;
import org.springframework.samples.petclinic.vets.VetCreated;
import org.springframework.samples.petclinic.vets.VetUpdated;
import org.springframework.samples.petclinic.visits.VisitCompleted;
import org.springframework.samples.petclinic.visits.VisitCreated;
import org.springframework.stereotype.Component;

/**
 * Domain event listener for the GenAI module.
 *
 * This listener demonstrates Spring Modulith's event-driven architecture by:
 * - Listening to events from other modules (Customers, Vets, Visits)
 * - Processing events asynchronously
 * - Updating the AI vector store based on domain changes
 * - Being decoupled from event publishers
 *
 * Events are stored in the event_publication table and processed reliably.
 *
 * @author PetClinic Team
 */
@Component
class DomainEventListener {

    private static final Logger log = LoggerFactory.getLogger(DomainEventListener.class);

    /**
     * Listen to CustomerCreated events from the customers module.
     *
     * This demonstrates:
     * - Cross-module event consumption
     * - @ApplicationModuleListener for async processing
     * - Event-driven architecture in action
     */
    @ApplicationModuleListener
    void on(CustomerCreated event) {
        log.info("📢 [GenAI] Received CustomerCreated event - ID: {}, Name: {}",
            event.customerId(), event.customerName());

        // In a real implementation, we would:
        // 1. Fetch full customer details
        // 2. Update vector store with customer information
        // 3. Make customer data available for AI queries

        log.debug("[GenAI] Vector store would be updated with customer: {}", event.customerId());
    }

    /**
     * Listen to CustomerUpdated events.
     */
    @ApplicationModuleListener
    void on(CustomerUpdated event) {
        log.info("📢 [GenAI] Received CustomerUpdated event - ID: {}, Name: {}",
            event.customerId(), event.customerName());

        log.debug("[GenAI] Vector store would be refreshed for customer: {}", event.customerId());
    }

    /**
     * Listen to VetCreated events from the vets module.
     */
    @ApplicationModuleListener
    void on(VetCreated event) {
        log.info("📢 [GenAI] Received VetCreated event - ID: {}, Name: {}",
            event.vetId(), event.vetName());

        // Update vector store with new vet information
        log.debug("[GenAI] Vector store would be updated with vet: {}", event.vetId());
    }

    /**
     * Listen to VetUpdated events.
     */
    @ApplicationModuleListener
    void on(VetUpdated event) {
        log.info("📢 [GenAI] Received VetUpdated event - ID: {}, Name: {}",
            event.vetId(), event.vetName());

        log.debug("[GenAI] Vector store would be refreshed for vet: {}", event.vetId());
    }

    /**
     * Listen to VisitCreated events from the visits module.
     */
    @ApplicationModuleListener
    void on(VisitCreated event) {
        log.info("📢 [GenAI] Received VisitCreated event - Visit ID: {}, Pet: {}, Vet: {}",
            event.visitId(), event.petId(), event.vetId());

        // Update AI context with visit information
        log.debug("[GenAI] AI context updated with new visit: {}", event.visitId());
    }

    /**
     * Listen to VisitCompleted events.
     */
    @ApplicationModuleListener
    void on(VisitCompleted event) {
        log.info("📢 [GenAI] Received VisitCompleted event - Visit ID: {}, Pet: {}, Vet: {}",
            event.visitId(), event.petId(), event.vetId());

        // Generate completion insights, update statistics
        log.debug("[GenAI] Processing completed visit for insights: {}", event.visitId());
    }
}
