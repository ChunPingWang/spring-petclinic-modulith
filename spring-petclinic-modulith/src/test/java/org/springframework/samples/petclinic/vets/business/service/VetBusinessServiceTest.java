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
package org.springframework.samples.petclinic.vets.business.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.samples.petclinic.vets.business.exception.VetNotFoundException;
import org.springframework.samples.petclinic.vets.business.port.EventPublisher;
import org.springframework.samples.petclinic.vets.business.port.VetRepository;
import org.springframework.samples.petclinic.vets.domain.Specialty;
import org.springframework.samples.petclinic.vets.domain.Vet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

/**
 * Unit tests for VetBusinessService.
 *
 * Tests the business logic in isolation without any framework dependencies.
 *
 * @author PetClinic Team
 */
@ExtendWith(MockitoExtension.class)
class VetBusinessServiceTest {

    @Mock
    private VetRepository vetRepository;

    @Mock
    private EventPublisher eventPublisher;

    private VetBusinessService businessService;

    @BeforeEach
    void setUp() {
        businessService = new VetBusinessService(vetRepository, eventPublisher);
    }

    @Test
    void shouldFindVetById() {
        // Given
        Vet vet = createVet(1, "James", "Carter");
        given(vetRepository.findById(1)).willReturn(Optional.of(vet));

        // When
        Optional<Vet> result = businessService.findById(1);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getFirstName()).isEqualTo("James");
        assertThat(result.get().getLastName()).isEqualTo("Carter");
        verify(vetRepository).findById(1);
    }

    @Test
    void shouldReturnEmptyWhenVetNotFound() {
        // Given
        given(vetRepository.findById(999)).willReturn(Optional.empty());

        // When
        Optional<Vet> result = businessService.findById(999);

        // Then
        assertThat(result).isEmpty();
        verify(vetRepository).findById(999);
    }

    @Test
    void shouldFindAllVets() {
        // Given
        List<Vet> vets = Arrays.asList(
            createVet(1, "James", "Carter"),
            createVet(2, "Helen", "Leary")
        );
        given(vetRepository.findAll()).willReturn(vets);

        // When
        List<Vet> result = businessService.findAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFirstName()).isEqualTo("James");
        assertThat(result.get(1).getFirstName()).isEqualTo("Helen");
        verify(vetRepository).findAll();
    }

    @Test
    void shouldCreateVet() {
        // Given
        Vet newVet = createVet(null, "John", "Doe");
        Vet savedVet = createVet(1, "John", "Doe");

        given(vetRepository.save(any(Vet.class))).willReturn(savedVet);

        // When
        Vet result = businessService.createVet(newVet);

        // Then
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        verify(vetRepository).save(newVet);
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldThrowExceptionWhenCreatingVetWithId() {
        // Given
        Vet vetWithId = createVet(1, "John", "Doe");

        // When/Then
        assertThatThrownBy(() -> businessService.createVet(vetWithId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("should not have an ID");

        verify(vetRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void shouldThrowExceptionWhenCreatingNullVet() {
        // When/Then
        assertThatThrownBy(() -> businessService.createVet(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be null");

        verify(vetRepository, never()).save(any());
    }

    @Test
    void shouldUpdateVet() {
        // Given
        Vet existingVet = createVet(1, "James", "Carter");
        Vet updatedData = createVet(1, "James", "Carter-Smith");

        given(vetRepository.findById(1)).willReturn(Optional.of(existingVet));
        given(vetRepository.save(any(Vet.class))).willReturn(existingVet);

        // When
        Vet result = businessService.updateVet(1, updatedData);

        // Then
        assertThat(result.getFirstName()).isEqualTo("James");
        assertThat(result.getLastName()).isEqualTo("Carter-Smith");
        verify(vetRepository).findById(1);
        verify(vetRepository).save(existingVet);
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentVet() {
        // Given
        Vet vet = createVet(1, "John", "Doe");
        given(vetRepository.findById(999)).willReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> businessService.updateVet(999, vet))
            .isInstanceOf(VetNotFoundException.class)
            .hasMessageContaining("Vet not found");

        verify(vetRepository).findById(999);
        verify(vetRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void shouldDeleteVet() {
        // Given
        Vet vet = createVet(1, "James", "Carter");
        given(vetRepository.findById(1)).willReturn(Optional.of(vet));

        // When
        businessService.deleteVet(1);

        // Then
        verify(vetRepository).findById(1);
        verify(vetRepository).deleteById(1);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentVet() {
        // Given
        given(vetRepository.findById(999)).willReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> businessService.deleteVet(999))
            .isInstanceOf(VetNotFoundException.class)
            .hasMessageContaining("Vet not found");

        verify(vetRepository).findById(999);
        verify(vetRepository, never()).deleteById(any());
    }

    @Test
    void shouldValidateVetOnCreate() {
        // Given
        Vet invalidVet = new Vet();
        invalidVet.setFirstName(""); // Empty first name

        // When/Then
        assertThatThrownBy(() -> businessService.createVet(invalidVet))
            .isInstanceOf(IllegalArgumentException.class);

        verify(vetRepository, never()).save(any());
    }

    @Test
    void shouldHandleVetWithSpecialties() {
        // Given
        Vet vet = createVet(null, "Jane", "Smith");
        Specialty radiology = new Specialty();
        radiology.setId(1);
        radiology.setName("radiology");
        vet.addSpecialty(radiology);

        Vet savedVet = createVet(1, "Jane", "Smith");
        savedVet.addSpecialty(radiology);

        given(vetRepository.save(any(Vet.class))).willReturn(savedVet);

        // When
        Vet result = businessService.createVet(vet);

        // Then
        assertThat(result.getSpecialties()).hasSize(1);
        assertThat(result.getSpecialties().iterator().next().getName()).isEqualTo("radiology");
        verify(vetRepository).save(vet);
    }

    private Vet createVet(Integer id, String firstName, String lastName) {
        Vet vet = new Vet();
        vet.setId(id);
        vet.setFirstName(firstName);
        vet.setLastName(lastName);
        vet.setSpecialties(new ArrayList<>());
        return vet;
    }
}
