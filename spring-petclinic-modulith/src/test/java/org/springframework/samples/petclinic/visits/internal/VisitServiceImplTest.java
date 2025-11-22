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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.samples.petclinic.shared.exceptions.ResourceNotFoundException;
import org.springframework.samples.petclinic.visits.Visit;
import org.springframework.samples.petclinic.visits.VisitService;
import org.springframework.samples.petclinic.visits.business.exception.InvalidVisitException;
import org.springframework.samples.petclinic.visits.business.exception.VisitNotFoundException;
import org.springframework.samples.petclinic.visits.business.service.VisitBusinessService;
import org.springframework.samples.petclinic.visits.domain.VisitStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for VisitServiceImpl.
 *
 * Tests verify:
 * - Delegation to VisitBusinessService
 * - Conversion between domain models and legacy entities
 * - Exception translation from business to legacy exceptions
 *
 * @author PetClinic Team
 */
@ExtendWith(MockitoExtension.class)
class VisitServiceImplTest {

    @Mock
    private VisitBusinessService businessService;

    private VisitService visitService;

    @BeforeEach
    void setUp() {
        visitService = new VisitServiceImpl(businessService);
    }

    @Test
    void shouldFindVisitById() {
        // Given
        org.springframework.samples.petclinic.visits.domain.Visit domainVisit = createDomainVisit(1, 1, 1, "Routine checkup", VisitStatus.COMPLETED);
        given(businessService.findById(1)).willReturn(Optional.of(domainVisit));

        // When
        Optional<Visit> result = visitService.findById(1);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getPetId()).isEqualTo(1);
        assertThat(result.get().getVetId()).isEqualTo(1);
        assertThat(result.get().getStatus()).isEqualTo("COMPLETED");
        verify(businessService).findById(1);
    }

    @Test
    void shouldReturnEmptyWhenVisitNotFound() {
        // Given
        given(businessService.findById(999)).willReturn(Optional.empty());

        // When
        Optional<Visit> result = visitService.findById(999);

        // Then
        assertThat(result).isEmpty();
        verify(businessService).findById(999);
    }

    @Test
    void shouldFindAllVisits() {
        // Given
        List<org.springframework.samples.petclinic.visits.domain.Visit> domainVisits = Arrays.asList(
            createDomainVisit(1, 1, 1, "Checkup", VisitStatus.COMPLETED),
            createDomainVisit(2, 2, 2, "Vaccination", VisitStatus.SCHEDULED)
        );
        given(businessService.findAll()).willReturn(domainVisits);

        // When
        List<Visit> result = visitService.findAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1);
        assertThat(result.get(1).getId()).isEqualTo(2);
        verify(businessService).findAll();
    }

    @Test
    void shouldFindVisitsByPetId() {
        // Given
        List<org.springframework.samples.petclinic.visits.domain.Visit> domainVisits = Arrays.asList(
            createDomainVisit(1, 1, 1, "Checkup", VisitStatus.COMPLETED),
            createDomainVisit(3, 1, 2, "Vaccination", VisitStatus.SCHEDULED)
        );
        given(businessService.findByPetId(1)).willReturn(domainVisits);

        // When
        List<Visit> result = visitService.findByPetId(1);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.stream().allMatch(v -> v.getPetId().equals(1))).isTrue();
        verify(businessService).findByPetId(1);
    }

    @Test
    void shouldFindVisitsByVetId() {
        // Given
        List<org.springframework.samples.petclinic.visits.domain.Visit> domainVisits = Arrays.asList(
            createDomainVisit(1, 1, 1, "Checkup", VisitStatus.COMPLETED),
            createDomainVisit(4, 3, 1, "Skin follow-up", VisitStatus.SCHEDULED)
        );
        given(businessService.findByVetId(1)).willReturn(domainVisits);

        // When
        List<Visit> result = visitService.findByVetId(1);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.stream().allMatch(v -> v.getVetId().equals(1))).isTrue();
        verify(businessService).findByVetId(1);
    }

    @Test
    void shouldScheduleVisit() {
        // Given
        Visit legacyVisit = new Visit(1, 1);
        legacyVisit.setDescription("Routine checkup");

        org.springframework.samples.petclinic.visits.domain.Visit scheduledDomainVisit =
            createDomainVisit(1, 1, 1, "Routine checkup", VisitStatus.SCHEDULED);

        given(businessService.scheduleVisit(any(org.springframework.samples.petclinic.visits.domain.Visit.class)))
            .willReturn(scheduledDomainVisit);

        // When
        Visit result = visitService.scheduleVisit(legacyVisit);

        // Then
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getStatus()).isEqualTo("SCHEDULED");
        verify(businessService).scheduleVisit(any(org.springframework.samples.petclinic.visits.domain.Visit.class));
    }

    @Test
    void shouldTranslateInvalidVisitException() {
        // Given
        Visit legacyVisit = new Visit(999, 1);
        legacyVisit.setDescription("Test");

        given(businessService.scheduleVisit(any(org.springframework.samples.petclinic.visits.domain.Visit.class)))
            .willThrow(new InvalidVisitException("Pet not found: 999"));

        // When/Then
        assertThatThrownBy(() -> visitService.scheduleVisit(legacyVisit))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Pet not found: 999");
    }

    @Test
    void shouldCompleteVisit() {
        // Given
        org.springframework.samples.petclinic.visits.domain.Visit completedDomainVisit =
            createDomainVisit(1, 1, 1, "Checkup", VisitStatus.COMPLETED);

        given(businessService.completeVisit(1)).willReturn(completedDomainVisit);

        // When
        Visit result = visitService.completeVisit(1);

        // Then
        assertThat(result.getStatus()).isEqualTo("COMPLETED");
        verify(businessService).completeVisit(1);
    }

    @Test
    void shouldTranslateVisitNotFoundExceptionOnComplete() {
        // Given
        given(businessService.completeVisit(999))
            .willThrow(new VisitNotFoundException("Visit not found", 999));

        // When/Then
        assertThatThrownBy(() -> visitService.completeVisit(999))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Visit not found");
    }

    @Test
    void shouldCancelVisit() {
        // Given - business service returns void

        // When
        visitService.cancelVisit(1);

        // Then
        verify(businessService).cancelVisit(1);
    }

    @Test
    void shouldTranslateVisitNotFoundExceptionOnCancel() {
        // Given - mock void method to throw exception using willThrow()
        willThrow(new VisitNotFoundException("Visit not found", 999))
            .given(businessService).cancelVisit(999);

        // When/Then
        assertThatThrownBy(() -> visitService.cancelVisit(999))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Visit not found");
    }

    private org.springframework.samples.petclinic.visits.domain.Visit createDomainVisit(
            Integer id, Integer petId, Integer vetId, String description, VisitStatus status) {
        org.springframework.samples.petclinic.visits.domain.Visit visit =
            new org.springframework.samples.petclinic.visits.domain.Visit();
        visit.setId(id);
        visit.setPetId(petId);
        visit.setVetId(vetId);
        visit.setDescription(description);
        visit.setStatus(status);
        return visit;
    }
}
