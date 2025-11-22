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
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.samples.petclinic.customers.Customer;
import org.springframework.samples.petclinic.customers.CustomerCreated;
import org.springframework.samples.petclinic.customers.CustomerService;
import org.springframework.samples.petclinic.customers.CustomerUpdated;
import org.springframework.samples.petclinic.vets.Vet;
import org.springframework.samples.petclinic.vets.VetCreated;
import org.springframework.samples.petclinic.vets.VetService;
import org.springframework.samples.petclinic.vets.VetUpdated;
import org.springframework.samples.petclinic.visits.Visit;
import org.springframework.samples.petclinic.visits.VisitCompleted;
import org.springframework.samples.petclinic.visits.VisitCreated;
import org.springframework.samples.petclinic.visits.VisitService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private final VectorStore vectorStore;
    private final CustomerService customerService;
    private final VetService vetService;
    private final VisitService visitService;

    DomainEventListener(@Autowired(required = false) VectorStore vectorStore,
                       CustomerService customerService,
                       VetService vetService,
                       VisitService visitService) {
        this.vectorStore = vectorStore;
        this.customerService = customerService;
        this.vetService = vetService;
        this.visitService = visitService;
    }

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

        if (vectorStore == null) {
            log.warn("[GenAI] VectorStore not available, skipping update");
            return;
        }

        try {
            // Fetch full customer details
            customerService.findById(event.customerId()).ifPresent(customer -> {
                // Create document for vector store
                String content = formatCustomerForVectorStore(customer);
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("type", "customer");
                metadata.put("id", customer.getId());
                metadata.put("name", customer.getFullName());

                Document doc = new Document(content, metadata);
                vectorStore.add(List.of(doc));

                log.info("✅ [GenAI] Added customer {} to vector store", customer.getId());
            });
        } catch (Exception e) {
            log.error("[GenAI] Failed to update vector store with customer {}: {}",
                event.customerId(), e.getMessage(), e);
        }
    }

    /**
     * Listen to CustomerUpdated events.
     */
    @ApplicationModuleListener
    void on(CustomerUpdated event) {
        log.info("📢 [GenAI] Received CustomerUpdated event - ID: {}, Name: {}",
            event.customerId(), event.customerName());

        if (vectorStore == null) {
            log.warn("[GenAI] VectorStore not available, skipping update");
            return;
        }

        try {
            // For updates, we could delete old and add new, or just add new version
            customerService.findById(event.customerId()).ifPresent(customer -> {
                String content = formatCustomerForVectorStore(customer);
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("type", "customer");
                metadata.put("id", customer.getId());
                metadata.put("name", customer.getFullName());
                metadata.put("updated", true);

                Document doc = new Document(content, metadata);
                vectorStore.add(List.of(doc));

                log.info("✅ [GenAI] Updated customer {} in vector store", customer.getId());
            });
        } catch (Exception e) {
            log.error("[GenAI] Failed to update vector store for customer {}: {}",
                event.customerId(), e.getMessage(), e);
        }
    }

    /**
     * Listen to VetCreated events from the vets module.
     */
    @ApplicationModuleListener
    void on(VetCreated event) {
        log.info("📢 [GenAI] Received VetCreated event - ID: {}, Name: {}",
            event.vetId(), event.vetName());

        if (vectorStore == null) {
            log.warn("[GenAI] VectorStore not available, skipping update");
            return;
        }

        try {
            vetService.findById(event.vetId()).ifPresent(vet -> {
                String content = formatVetForVectorStore(vet);
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("type", "vet");
                metadata.put("id", vet.getId());
                metadata.put("name", vet.getFullName());

                Document doc = new Document(content, metadata);
                vectorStore.add(List.of(doc));

                log.info("✅ [GenAI] Added vet {} to vector store", vet.getId());
            });
        } catch (Exception e) {
            log.error("[GenAI] Failed to update vector store with vet {}: {}",
                event.vetId(), e.getMessage(), e);
        }
    }

    /**
     * Listen to VetUpdated events.
     */
    @ApplicationModuleListener
    void on(VetUpdated event) {
        log.info("📢 [GenAI] Received VetUpdated event - ID: {}, Name: {}",
            event.vetId(), event.vetName());

        if (vectorStore == null) {
            log.warn("[GenAI] VectorStore not available, skipping update");
            return;
        }

        try {
            vetService.findById(event.vetId()).ifPresent(vet -> {
                String content = formatVetForVectorStore(vet);
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("type", "vet");
                metadata.put("id", vet.getId());
                metadata.put("name", vet.getFullName());
                metadata.put("updated", true);

                Document doc = new Document(content, metadata);
                vectorStore.add(List.of(doc));

                log.info("✅ [GenAI] Updated vet {} in vector store", vet.getId());
            });
        } catch (Exception e) {
            log.error("[GenAI] Failed to update vector store for vet {}: {}",
                event.vetId(), e.getMessage(), e);
        }
    }

    /**
     * Listen to VisitCreated events from the visits module.
     */
    @ApplicationModuleListener
    void on(VisitCreated event) {
        log.info("📢 [GenAI] Received VisitCreated event - Visit ID: {}, Pet: {}, Vet: {}",
            event.visitId(), event.petId(), event.vetId());

        if (vectorStore == null) {
            log.warn("[GenAI] VectorStore not available, skipping update");
            return;
        }

        try {
            visitService.findById(event.visitId()).ifPresent(visit -> {
                String content = formatVisitForVectorStore(visit);
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("type", "visit");
                metadata.put("id", visit.getId());
                metadata.put("petId", visit.getPetId());
                metadata.put("vetId", visit.getVetId());
                metadata.put("status", visit.getStatus());

                Document doc = new Document(content, metadata);
                vectorStore.add(List.of(doc));

                log.info("✅ [GenAI] Added visit {} to vector store", visit.getId());
            });
        } catch (Exception e) {
            log.error("[GenAI] Failed to update vector store with visit {}: {}",
                event.visitId(), e.getMessage(), e);
        }
    }

    /**
     * Listen to VisitCompleted events.
     */
    @ApplicationModuleListener
    void on(VisitCompleted event) {
        log.info("📢 [GenAI] Received VisitCompleted event - Visit ID: {}, Pet: {}, Vet: {}",
            event.visitId(), event.petId(), event.vetId());

        if (vectorStore == null) {
            log.warn("[GenAI] VectorStore not available, skipping update");
            return;
        }

        try {
            visitService.findById(event.visitId()).ifPresent(visit -> {
                String content = formatVisitForVectorStore(visit) + " [COMPLETED]";
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("type", "visit");
                metadata.put("id", visit.getId());
                metadata.put("petId", visit.getPetId());
                metadata.put("vetId", visit.getVetId());
                metadata.put("status", "COMPLETED");
                metadata.put("completed", true);

                Document doc = new Document(content, metadata);
                vectorStore.add(List.of(doc));

                log.info("✅ [GenAI] Marked visit {} as completed in vector store", visit.getId());
            });
        } catch (Exception e) {
            log.error("[GenAI] Failed to update vector store for completed visit {}: {}",
                event.visitId(), e.getMessage(), e);
        }
    }

    // Helper methods to format entities for vector store

    private String formatCustomerForVectorStore(Customer customer) {
        StringBuilder sb = new StringBuilder();
        sb.append("Customer: ").append(customer.getFullName()).append("\n");
        sb.append("ID: ").append(customer.getId()).append("\n");
        sb.append("Address: ").append(customer.getAddress()).append(", ");
        sb.append(customer.getCity()).append("\n");
        sb.append("Telephone: ").append(customer.getTelephone()).append("\n");

        if (customer.getPets() != null && !customer.getPets().isEmpty()) {
            sb.append("Pets: ");
            customer.getPets().forEach(pet ->
                sb.append(pet.getName()).append(" (").append(pet.getType().getName()).append("), ")
            );
            sb.append("\n");
        }

        return sb.toString();
    }

    private String formatVetForVectorStore(Vet vet) {
        StringBuilder sb = new StringBuilder();
        sb.append("Veterinarian: ").append(vet.getFullName()).append("\n");
        sb.append("ID: ").append(vet.getId()).append("\n");

        if (vet.getSpecialties() != null && !vet.getSpecialties().isEmpty()) {
            sb.append("Specialties: ");
            vet.getSpecialties().forEach(specialty ->
                sb.append(specialty.getName()).append(", ")
            );
            sb.append("\n");
        }

        return sb.toString();
    }

    private String formatVisitForVectorStore(Visit visit) {
        StringBuilder sb = new StringBuilder();
        sb.append("Visit ID: ").append(visit.getId()).append("\n");
        sb.append("Pet ID: ").append(visit.getPetId()).append("\n");
        sb.append("Vet ID: ").append(visit.getVetId()).append("\n");
        sb.append("Date: ").append(visit.getVisitDate()).append("\n");
        sb.append("Description: ").append(visit.getDescription()).append("\n");
        sb.append("Status: ").append(visit.getStatus()).append("\n");

        return sb.toString();
    }
}
