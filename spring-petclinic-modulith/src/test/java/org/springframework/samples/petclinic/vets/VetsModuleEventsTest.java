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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for Vets module event publishing.
 * 
 * Tests verify that domain events are properly published and can be
 * consumed by event listeners in other modules.
 * 
 * @author PetClinic Team
 */
@ApplicationModuleTest
@ActiveProfiles("test")
@Transactional
class VetsModuleEventsTest {

    @Autowired
    VetService vetService;

    @Test
    void publishesVetCreatedEvent(Scenario scenario) {
        // Given
        Vet vet = new Vet();
        vet.setFirstName("James");
        vet.setLastName("Carter");

        // When - save vet and expect event
        scenario.stimulate(() -> vetService.save(vet))
            .andWaitForEventOfType(VetCreated.class)
            .matching(event -> event.vetName().equals("James Carter"))
            .toArrive();
    }

    @Test
    void publishesVetUpdatedEvent(Scenario scenario) {
        // Given - create a vet first
        Vet vet = new Vet();
        vet.setFirstName("Helen");
        vet.setLastName("Leary");
        
        Vet savedVet = vetService.save(vet);

        // When - update vet and expect event
        Vet updateData = new Vet();
        updateData.setFirstName("Helen");
        updateData.setLastName("Wilson");  // Changed last name

        scenario.stimulate(() -> vetService.update(savedVet.getId(), updateData))
            .andWaitForEventOfType(VetUpdated.class)
            .matching(event -> event.vetName().equals("Helen Wilson"))
            .toArrive();
    }

    @Test
    void vetServiceIsAvailable() {
        // Verify that VetService is properly registered as a Spring bean
        assertThat(vetService).isNotNull();
    }
}
