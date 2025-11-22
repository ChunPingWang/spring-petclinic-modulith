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
package org.springframework.samples.petclinic.visits.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.samples.petclinic.customers.CustomerService;
import org.springframework.samples.petclinic.shared.exceptions.ResourceNotFoundException;
import org.springframework.samples.petclinic.vets.Vet;
import org.springframework.samples.petclinic.vets.VetService;
import org.springframework.samples.petclinic.visits.Visit;
import org.springframework.samples.petclinic.visits.VisitCompleted;
import org.springframework.samples.petclinic.visits.VisitCreated;
import org.springframework.samples.petclinic.visits.VisitService;
import org.springframework.samples.petclinic.customers.Customer;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for VisitServiceImpl.
 * 
 * Tests verify:
 * - Cross-module dependency injection (CustomerService, VetService)
 * - Referential integrity validation
 * - Event publishing
 * - Business logic
 * 
 * @author PetClinic Team
 */
@ExtendWith(MockitoExtension.class)
class VisitServiceImplTest {

    @Mock
    private VisitRepository visitRepository;

    @Mock
    private CustomerService customerService;

    @Mock
    private VetService vetService;

    @Mock
    private ApplicationEventPublisher events;

    private VisitService visitService;

    @BeforeEach
    void setUp() {
        visitService = new VisitServiceImpl(visitRepository, customerService, vetService, events);
    }

    @Test
    void shouldFindVisitById() {
        // Given
        Visit visit = createVisit(1, 1, 1, "Routine checkup", "COMPLETED");
        given(visitRepository.findById(1)).willReturn(Optional.of(visit));

        // When
        Optional<Visit> result = visitService.findById(1);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getPetId()).isEqualTo(1);
        assertThat(result.get().getVetId()).isEqualTo(1);
    }

    @Test
    void shouldReturnEmptyWhenVisitNotFound() {
        // Given
        given(visitRepository.findById(999)).willReturn(Optional.empty());

        // When
        Optional<Visit> result = visitService.findById(999);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindAllVisits() {
        // Given
        List<Visit> visits = Arrays.asList(
            createVisit(1, 1, 1, "Checkup", "COMPLETED"),
            createVisit(2, 2, 2, "Vaccination", "SCHEDULED")
        );
        given(visitRepository.findAll()).willReturn(visits);

        // When
        List<Visit> result = visitService.findAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1);
        assertThat(result.get(1).getId()).isEqualTo(2);
    }

    @Test
    void shouldFindVisitsByPetId() {
        // Given
        List<Visit> visits = Arrays.asList(
            createVisit(1, 1, 1, "Checkup", "COMPLETED"),
            createVisit(3, 1, 2, "Vaccination", "SCHEDULED")
        );
        given(visitRepository.findByPetId(1)).willReturn(visits);

        // When
        List<Visit> result = visitService.findByPetId(1);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.stream().allMatch(v -> v.getPetId().equals(1))).isTrue();
    }

    @Test
    void shouldFindVisitsByVetId() {
        // Given
        List<Visit> visits = Arrays.asList(
            createVisit(1, 1, 1, "Checkup", "COMPLETED"),
            createVisit(4, 3, 1, "Skin follow-up", "SCHEDULED")
        );
        given(visitRepository.findByVetId(1)).willReturn(visits);

        // When
        List<Visit> result = visitService.findByVetId(1);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.stream().allMatch(v -> v.getVetId().equals(1))).isTrue();
    }

    @Test
    void shouldScheduleVisitWithCrossModuleValidation() {
        // Given
        Visit visit = new Visit(1, 1);
        visit.setDescription("Routine checkup");
        
        Visit savedVisit = createVisit(1, 1, 1, "Routine checkup", "SCHEDULED");
        
        // Mock cross-module dependencies
        Customer customer = new Customer();
        customer.setId(1);
        given(customerService.findById(1)).willReturn(Optional.of(customer));
        
        Vet vet = new Vet();
        vet.setId(1);
        given(vetService.findById(1)).willReturn(Optional.of(vet));
        
        given(visitRepository.save(any(Visit.class))).willReturn(savedVisit);

        // When
        Visit result = visitService.scheduleVisit(visit);

        // Then
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getStatus()).isEqualTo("SCHEDULED");
        
        // Verify cross-module calls
        verify(customerService).findById(1);
        verify(vetService).findById(1);
        
        // Verify event publishing
        ArgumentCaptor<VisitCreated> eventCaptor = ArgumentCaptor.forClass(VisitCreated.class);
        verify(events).publishEvent(eventCaptor.capture());
        
        VisitCreated event = eventCaptor.getValue();
        assertThat(event.visitId()).isEqualTo(1);
        assertThat(event.petId()).isEqualTo(1);
        assertThat(event.vetId()).isEqualTo(1);
    }

    @Test
    void shouldThrowExceptionWhenPetNotFoundDuringScheduling() {
        // Given
        Visit visit = new Visit(999, 1);  // Non-existent pet
        visit.setDescription("Routine checkup");
        
        given(customerService.findById(999)).willReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> visitService.scheduleVisit(visit))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Pet")
            .hasMessageContaining("999");
    }

    @Test
    void shouldThrowExceptionWhenVetNotFoundDuringScheduling() {
        // Given
        Visit visit = new Visit(1, 999);  // Non-existent vet
        visit.setDescription("Routine checkup");
        
        Customer customer = new Customer();
        customer.setId(1);
        given(customerService.findById(1)).willReturn(Optional.of(customer));
        given(vetService.findById(999)).willReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> visitService.scheduleVisit(visit))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Vet")
            .hasMessageContaining("999");
    }

    @Test
    void shouldCompleteVisitAndPublishEvent() {
        // Given
        Visit visit = createVisit(1, 1, 1, "Checkup", "SCHEDULED");
        Visit completedVisit = createVisit(1, 1, 1, "Checkup", "COMPLETED");
        
        given(visitRepository.findById(1)).willReturn(Optional.of(visit));
        given(visitRepository.save(any(Visit.class))).willReturn(completedVisit);

        // When
        Visit result = visitService.completeVisit(1);

        // Then
        assertThat(result.getStatus()).isEqualTo("COMPLETED");
        
        // Verify event
        ArgumentCaptor<VisitCompleted> eventCaptor = ArgumentCaptor.forClass(VisitCompleted.class);
        verify(events).publishEvent(eventCaptor.capture());
        
        VisitCompleted event = eventCaptor.getValue();
        assertThat(event.visitId()).isEqualTo(1);
    }

    @Test
    void shouldCancelVisit() {
        // Given
        Visit visit = createVisit(1, 1, 1, "Checkup", "SCHEDULED");
        given(visitRepository.findById(1)).willReturn(Optional.of(visit));
        given(visitRepository.save(any(Visit.class))).willReturn(visit);

        // When
        visitService.cancelVisit(1);

        // Then
        ArgumentCaptor<Visit> visitCaptor = ArgumentCaptor.forClass(Visit.class);
        verify(visitRepository).save(visitCaptor.capture());

        Visit saved = visitCaptor.getValue();
        assertThat(saved.getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    void shouldThrowExceptionWhenCompletingNonExistentVisit() {
        // Given
        given(visitRepository.findById(999)).willReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> visitService.completeVisit(999))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Visit")
            .hasMessageContaining("999");
    }

    @Test
    void shouldThrowExceptionWhenCancellingNonExistentVisit() {
        // Given
        given(visitRepository.findById(999)).willReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> visitService.cancelVisit(999))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Visit")
            .hasMessageContaining("999");
    }

    private Visit createVisit(Integer id, Integer petId, Integer vetId, String description, String status) {
        Visit visit = new Visit(petId, vetId);
        visit.setId(id);
        visit.setDescription(description);
        visit.setStatus(status);
        return visit;
    }
}
