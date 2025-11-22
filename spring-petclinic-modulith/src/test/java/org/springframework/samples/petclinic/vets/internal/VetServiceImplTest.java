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
package org.springframework.samples.petclinic.vets.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.samples.petclinic.shared.exceptions.ResourceNotFoundException;
import org.springframework.samples.petclinic.vets.Vet;
import org.springframework.samples.petclinic.vets.VetCreated;
import org.springframework.samples.petclinic.vets.VetService;
import org.springframework.samples.petclinic.vets.VetUpdated;
import org.springframework.samples.petclinic.vets.business.port.EventPublisher;
import org.springframework.samples.petclinic.vets.business.port.VetRepository;
import org.springframework.samples.petclinic.vets.business.service.VetBusinessService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for VetServiceImpl.
 *
 * Tests verify business logic, event publishing, and caching behavior.
 * Now uses the new three-layer architecture with business service delegation.
 *
 * @author PetClinic Team
 */
@ExtendWith(MockitoExtension.class)
class VetServiceImplTest {

    @Mock
    private VetRepository vetRepository;

    @Mock
    private EventPublisher eventPublisher;

    private VetBusinessService businessService;
    private VetService vetService;

    @BeforeEach
    void setUp() {
        // Create business service with mocked dependencies
        businessService = new VetBusinessService(vetRepository, eventPublisher);

        // Create service implementation that delegates to business service
        vetService = new VetServiceImpl(businessService);
    }

    @Test
    void shouldFindVetById() {
        // Given - create domain vet
        org.springframework.samples.petclinic.vets.domain.Vet domainVet = createDomainVet(1, "James", "Carter");
        given(vetRepository.findById(1)).willReturn(Optional.of(domainVet));

        // When
        Optional<Vet> result = vetService.findById(1);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getFirstName()).isEqualTo("James");
        assertThat(result.get().getLastName()).isEqualTo("Carter");
    }

    @Test
    void shouldReturnEmptyWhenVetNotFound() {
        // Given
        given(vetRepository.findById(999)).willReturn(Optional.empty());

        // When
        Optional<Vet> result = vetService.findById(999);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindAllVets() {
        // Given - create domain vets
        List<org.springframework.samples.petclinic.vets.domain.Vet> domainVets = Arrays.asList(
            createDomainVet(1, "James", "Carter"),
            createDomainVet(2, "Helen", "Leary"),
            createDomainVet(3, "Linda", "Douglas")
        );
        given(vetRepository.findAll()).willReturn(domainVets);

        // When
        List<Vet> result = vetService.findAll();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getFirstName()).isEqualTo("James");
        assertThat(result.get(1).getFirstName()).isEqualTo("Helen");
        assertThat(result.get(2).getFirstName()).isEqualTo("Linda");
    }

    @Test
    void shouldSaveVetAndPublishEvent() {
        // Given - create legacy vet to save
        Vet vet = createVet(null, "Robert", "Roberts");

        // Mock repository to return domain vet with ID
        org.springframework.samples.petclinic.vets.domain.Vet savedDomainVet = createDomainVet(1, "Robert", "Roberts");
        given(vetRepository.save(any(org.springframework.samples.petclinic.vets.domain.Vet.class)))
                .willReturn(savedDomainVet);

        // When
        Vet result = vetService.save(vet);

        // Then
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getFirstName()).isEqualTo("Robert");

        // Verify event was published
        ArgumentCaptor<VetCreated> eventCaptor = ArgumentCaptor.forClass(VetCreated.class);
        verify(eventPublisher).publish(eventCaptor.capture());

        VetCreated event = eventCaptor.getValue();
        assertThat(event.vetId()).isEqualTo(1);
        assertThat(event.vetName()).isEqualTo("Robert Roberts");
    }

    @Test
    void shouldUpdateVetAndPublishEvent() {
        // Given - existing domain vet
        org.springframework.samples.petclinic.vets.domain.Vet existingDomainVet = createDomainVet(1, "James", "Carter");
        given(vetRepository.findById(1)).willReturn(Optional.of(existingDomainVet));

        // Update data as legacy vet
        Vet updateData = createVet(null, "James", "Wilson");  // Changed last name

        // Mock save to return updated domain vet
        org.springframework.samples.petclinic.vets.domain.Vet updatedDomainVet = createDomainVet(1, "James", "Wilson");
        given(vetRepository.save(any(org.springframework.samples.petclinic.vets.domain.Vet.class)))
                .willReturn(updatedDomainVet);

        // When
        Vet result = vetService.update(1, updateData);

        // Then
        assertThat(result.getLastName()).isEqualTo("Wilson");

        // Verify event was published
        ArgumentCaptor<VetUpdated> eventCaptor = ArgumentCaptor.forClass(VetUpdated.class);
        verify(eventPublisher).publish(eventCaptor.capture());

        VetUpdated event = eventCaptor.getValue();
        assertThat(event.vetId()).isEqualTo(1);
        assertThat(event.vetName()).isEqualTo("James Wilson");
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentVet() {
        // Given
        Vet updateData = createVet(null, "John", "Doe");
        given(vetRepository.findById(999)).willReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> vetService.update(999, updateData))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Vet")
            .hasMessageContaining("999");
    }

    @Test
    void shouldDeleteVetById() {
        // Given - existing domain vet
        org.springframework.samples.petclinic.vets.domain.Vet domainVet = createDomainVet(1, "James", "Carter");
        given(vetRepository.findById(1)).willReturn(Optional.of(domainVet));

        // When
        vetService.deleteById(1);

        // Then
        verify(vetRepository).deleteById(1);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentVet() {
        // Given
        given(vetRepository.findById(999)).willReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> vetService.deleteById(999))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Vet")
            .hasMessageContaining("999");
    }

    // Helper methods

    /**
     * Create legacy Vet entity (for input to service methods).
     */
    private Vet createVet(Integer id, String firstName, String lastName) {
        Vet vet = new Vet();
        vet.setId(id);
        vet.setFirstName(firstName);
        vet.setLastName(lastName);
        return vet;
    }

    /**
     * Create domain Vet model (for mocking repository responses).
     */
    private org.springframework.samples.petclinic.vets.domain.Vet createDomainVet(Integer id, String firstName, String lastName) {
        org.springframework.samples.petclinic.vets.domain.Vet vet =
                new org.springframework.samples.petclinic.vets.domain.Vet();
        vet.setId(id);
        vet.setFirstName(firstName);
        vet.setLastName(lastName);
        return vet;
    }
}
