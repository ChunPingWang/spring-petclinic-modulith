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
package org.springframework.samples.petclinic.visits.business.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.samples.petclinic.visits.business.exception.InvalidVisitException;
import org.springframework.samples.petclinic.visits.business.exception.VisitNotFoundException;
import org.springframework.samples.petclinic.visits.business.port.EventPublisher;
import org.springframework.samples.petclinic.visits.business.port.PetValidator;
import org.springframework.samples.petclinic.visits.business.port.VetValidator;
import org.springframework.samples.petclinic.visits.business.port.VisitRepository;
import org.springframework.samples.petclinic.visits.domain.Visit;
import org.springframework.samples.petclinic.visits.domain.VisitStatus;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

/**
 * Unit tests for VisitBusinessService.
 *
 * Tests the business logic in isolation without any framework dependencies.
 *
 * @author PetClinic Team
 */
@ExtendWith(MockitoExtension.class)
class VisitBusinessServiceTest {

    @Mock
    private VisitRepository visitRepository;

    @Mock
    private PetValidator petValidator;

    @Mock
    private VetValidator vetValidator;

    @Mock
    private EventPublisher eventPublisher;

    private VisitBusinessService businessService;

    @BeforeEach
    void setUp() {
        businessService = new VisitBusinessService(visitRepository, petValidator, vetValidator, eventPublisher);
    }

    @Test
    void shouldFindVisitById() {
        // Given
        Visit visit = createVisit(1, 1, 1, "Routine checkup", VisitStatus.SCHEDULED);
        given(visitRepository.findById(1)).willReturn(Optional.of(visit));

        // When
        Optional<Visit> result = businessService.findById(1);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getPetId()).isEqualTo(1);
        assertThat(result.get().getVetId()).isEqualTo(1);
        assertThat(result.get().getDescription()).isEqualTo("Routine checkup");
        verify(visitRepository).findById(1);
    }

    @Test
    void shouldReturnEmptyWhenVisitNotFound() {
        // Given
        given(visitRepository.findById(999)).willReturn(Optional.empty());

        // When
        Optional<Visit> result = businessService.findById(999);

        // Then
        assertThat(result).isEmpty();
        verify(visitRepository).findById(999);
    }

    @Test
    void shouldThrowExceptionWhenFindByIdWithInvalidId() {
        // When/Then
        assertThatThrownBy(() -> businessService.findById(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("must be positive");

        assertThatThrownBy(() -> businessService.findById(0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("must be positive");

        assertThatThrownBy(() -> businessService.findById(-1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("must be positive");

        verify(visitRepository, never()).findById(any());
    }

    @Test
    void shouldFindAllVisits() {
        // Given
        List<Visit> visits = Arrays.asList(
            createVisit(1, 1, 1, "Checkup", VisitStatus.SCHEDULED),
            createVisit(2, 2, 2, "Vaccination", VisitStatus.COMPLETED)
        );
        given(visitRepository.findAll()).willReturn(visits);

        // When
        List<Visit> result = businessService.findAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1);
        assertThat(result.get(1).getId()).isEqualTo(2);
        verify(visitRepository).findAll();
    }

    @Test
    void shouldFindVisitsByPetId() {
        // Given
        List<Visit> visits = Arrays.asList(
            createVisit(1, 1, 1, "Checkup", VisitStatus.SCHEDULED),
            createVisit(3, 1, 2, "Vaccination", VisitStatus.COMPLETED)
        );
        given(visitRepository.findByPetId(1)).willReturn(visits);

        // When
        List<Visit> result = businessService.findByPetId(1);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.stream().allMatch(v -> v.getPetId().equals(1))).isTrue();
        verify(visitRepository).findByPetId(1);
    }

    @Test
    void shouldThrowExceptionWhenFindByPetIdWithInvalidId() {
        // When/Then
        assertThatThrownBy(() -> businessService.findByPetId(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("must be positive");

        verify(visitRepository, never()).findByPetId(any());
    }

    @Test
    void shouldFindVisitsByVetId() {
        // Given
        List<Visit> visits = Arrays.asList(
            createVisit(1, 1, 1, "Checkup", VisitStatus.SCHEDULED),
            createVisit(4, 3, 1, "Follow-up", VisitStatus.SCHEDULED)
        );
        given(visitRepository.findByVetId(1)).willReturn(visits);

        // When
        List<Visit> result = businessService.findByVetId(1);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.stream().allMatch(v -> v.getVetId().equals(1))).isTrue();
        verify(visitRepository).findByVetId(1);
    }

    @Test
    void shouldThrowExceptionWhenFindByVetIdWithInvalidId() {
        // When/Then
        assertThatThrownBy(() -> businessService.findByVetId(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("must be positive");

        verify(visitRepository, never()).findByVetId(any());
    }

    @Test
    void shouldScheduleVisit() {
        // Given
        Visit newVisit = createVisit(null, 1, 1, "Routine checkup", null);
        newVisit.setVisitDate(new Date());
        Visit savedVisit = createVisit(1, 1, 1, "Routine checkup", VisitStatus.SCHEDULED);
        savedVisit.setVisitDate(new Date());

        given(petValidator.petExists(1)).willReturn(true);
        given(vetValidator.vetExists(1)).willReturn(true);
        given(visitRepository.save(any(Visit.class))).willReturn(savedVisit);

        // When
        Visit result = businessService.scheduleVisit(newVisit);

        // Then
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getStatus()).isEqualTo(VisitStatus.SCHEDULED);
        verify(petValidator).petExists(1);
        verify(vetValidator).vetExists(1);
        verify(visitRepository).save(any(Visit.class));
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldThrowExceptionWhenSchedulingNullVisit() {
        // When/Then
        assertThatThrownBy(() -> businessService.scheduleVisit(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be null");

        verify(visitRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenSchedulingVisitWithId() {
        // Given
        Visit visitWithId = createVisit(1, 1, 1, "Checkup", null);

        // When/Then
        assertThatThrownBy(() -> businessService.scheduleVisit(visitWithId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("should not have an ID");

        verify(visitRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenPetNotFound() {
        // Given
        Visit visit = createVisit(null, 999, 1, "Checkup", null);
        visit.setVisitDate(new Date());

        given(petValidator.petExists(999)).willReturn(false);

        // When/Then
        assertThatThrownBy(() -> businessService.scheduleVisit(visit))
            .isInstanceOf(InvalidVisitException.class)
            .hasMessageContaining("Pet not found: 999");

        verify(petValidator).petExists(999);
        verify(visitRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void shouldThrowExceptionWhenVetNotFound() {
        // Given
        Visit visit = createVisit(null, 1, 999, "Checkup", null);
        visit.setVisitDate(new Date());

        given(petValidator.petExists(1)).willReturn(true);
        given(vetValidator.vetExists(999)).willReturn(false);

        // When/Then
        assertThatThrownBy(() -> businessService.scheduleVisit(visit))
            .isInstanceOf(InvalidVisitException.class)
            .hasMessageContaining("Vet not found: 999");

        verify(petValidator).petExists(1);
        verify(vetValidator).vetExists(999);
        verify(visitRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void shouldCompleteVisit() {
        // Given
        Visit scheduledVisit = createVisit(1, 1, 1, "Checkup", VisitStatus.SCHEDULED);
        scheduledVisit.setVisitDate(new Date());

        given(visitRepository.findById(1)).willReturn(Optional.of(scheduledVisit));
        given(visitRepository.save(any(Visit.class))).willReturn(scheduledVisit);

        // When
        Visit result = businessService.completeVisit(1);

        // Then
        assertThat(result.getStatus()).isEqualTo(VisitStatus.COMPLETED);
        verify(visitRepository).findById(1);
        verify(visitRepository).save(scheduledVisit);
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldThrowExceptionWhenCompletingNonExistentVisit() {
        // Given
        given(visitRepository.findById(999)).willReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> businessService.completeVisit(999))
            .isInstanceOf(VisitNotFoundException.class)
            .hasMessageContaining("Visit not found");

        verify(visitRepository).findById(999);
        verify(visitRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void shouldThrowExceptionWhenCompletingWithInvalidId() {
        // When/Then
        assertThatThrownBy(() -> businessService.completeVisit(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("must be positive");

        verify(visitRepository, never()).findById(any());
    }

    @Test
    void shouldCancelVisit() {
        // Given
        Visit scheduledVisit = createVisit(1, 1, 1, "Checkup", VisitStatus.SCHEDULED);
        scheduledVisit.setVisitDate(new Date());

        given(visitRepository.findById(1)).willReturn(Optional.of(scheduledVisit));

        // When
        businessService.cancelVisit(1);

        // Then
        assertThat(scheduledVisit.getStatus()).isEqualTo(VisitStatus.CANCELLED);
        verify(visitRepository).findById(1);
        verify(visitRepository).save(scheduledVisit);
    }

    @Test
    void shouldThrowExceptionWhenCancellingNonExistentVisit() {
        // Given
        given(visitRepository.findById(999)).willReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> businessService.cancelVisit(999))
            .isInstanceOf(VisitNotFoundException.class)
            .hasMessageContaining("Visit not found");

        verify(visitRepository).findById(999);
        verify(visitRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenCancellingWithInvalidId() {
        // When/Then
        assertThatThrownBy(() -> businessService.cancelVisit(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("must be positive");

        verify(visitRepository, never()).findById(any());
    }

    @Test
    void shouldValidateVisitOnSchedule() {
        // Given - visit with missing petId (invalid)
        Visit invalidVisit = new Visit();
        invalidVisit.setVetId(1);
        invalidVisit.setDescription("Checkup");
        invalidVisit.setVisitDate(new Date());
        // Missing petId - should trigger validateForScheduling() to fail

        // When/Then
        assertThatThrownBy(() -> businessService.scheduleVisit(invalidVisit))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Pet ID is required");

        verify(visitRepository, never()).save(any());
    }

    private Visit createVisit(Integer id, Integer petId, Integer vetId, String description, VisitStatus status) {
        Visit visit = new Visit();
        visit.setId(id);
        visit.setPetId(petId);
        visit.setVetId(vetId);
        visit.setDescription(description);
        visit.setStatus(status);
        return visit;
    }
}
