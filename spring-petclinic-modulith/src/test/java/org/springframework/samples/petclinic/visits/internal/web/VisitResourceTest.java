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
package org.springframework.samples.petclinic.visits.internal.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.visits.Visit;
import org.springframework.samples.petclinic.visits.VisitService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for VisitResource REST controller.
 * 
 * Tests verify REST endpoints for visit management.
 * Demonstrates cross-module mocking (VisitService with CustomerService/VetService dependencies).
 * 
 * @author PetClinic Team
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(VisitResource.class)
@ActiveProfiles("test")
class VisitResourceTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    VisitService visitService;

    @Test
    void shouldGetAllVisits() throws Exception {
        // Given
        Visit visit1 = createVisit(1, 1, 1, "Routine checkup", "COMPLETED");
        Visit visit2 = createVisit(2, 2, 2, "Vaccination", "SCHEDULED");
        List<Visit> visits = Arrays.asList(visit1, visit2);
        
        given(visitService.findAll()).willReturn(visits);

        // When & Then
        mvc.perform(get("/visits").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].petId").value(1))
            .andExpect(jsonPath("$[0].vetId").value(1))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].petId").value(2))
            .andExpect(jsonPath("$[1].vetId").value(2));
    }

    @Test
    void shouldGetVisitById() throws Exception {
        // Given
        Visit visit = createVisit(1, 1, 1, "Routine checkup", "COMPLETED");
        given(visitService.findById(1)).willReturn(Optional.of(visit));

        // When & Then
        mvc.perform(get("/visits/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.petId").value(1))
            .andExpect(jsonPath("$.vetId").value(1))
            .andExpect(jsonPath("$.description").value("Routine checkup"))
            .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void shouldReturn404WhenVisitNotFound() throws Exception {
        // Given
        given(visitService.findById(999)).willReturn(Optional.empty());

        // When & Then
        mvc.perform(get("/visits/999").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetVisitsByPetId() throws Exception {
        // Given
        Visit visit1 = createVisit(1, 1, 1, "Routine checkup", "COMPLETED");
        Visit visit2 = createVisit(3, 1, 2, "Vaccination", "SCHEDULED");
        List<Visit> visits = Arrays.asList(visit1, visit2);
        
        given(visitService.findByPetId(1)).willReturn(visits);

        // When & Then
        mvc.perform(get("/visits/pet/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].petId").value(1))
            .andExpect(jsonPath("$[1].petId").value(1));
    }

    @Test
    void shouldGetVisitsByVetId() throws Exception {
        // Given
        Visit visit1 = createVisit(1, 1, 1, "Routine checkup", "COMPLETED");
        Visit visit2 = createVisit(4, 3, 1, "Skin follow-up", "SCHEDULED");
        List<Visit> visits = Arrays.asList(visit1, visit2);
        
        given(visitService.findByVetId(1)).willReturn(visits);

        // When & Then
        mvc.perform(get("/visits/vet/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].vetId").value(1))
            .andExpect(jsonPath("$[1].vetId").value(1));
    }

    @Test
    void shouldScheduleVisit() throws Exception {
        // Given
        Visit visit = new Visit(1, 1);
        visit.setDescription("Routine checkup");
        
        Visit savedVisit = createVisit(1, 1, 1, "Routine checkup", "SCHEDULED");
        given(visitService.scheduleVisit(any(Visit.class))).willReturn(savedVisit);

        // When & Then
        mvc.perform(post("/visits")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"petId\":1,\"vetId\":1,\"description\":\"Routine checkup\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.petId").value(1))
            .andExpect(jsonPath("$.vetId").value(1))
            .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    void shouldCompleteVisit() throws Exception {
        // Given
        Visit completedVisit = createVisit(1, 1, 1, "Routine checkup", "COMPLETED");
        given(visitService.completeVisit(1)).willReturn(completedVisit);

        // When & Then
        mvc.perform(put("/visits/1/complete").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void shouldCancelVisit() throws Exception {
        // Given
        doNothing().when(visitService).cancelVisit(1);

        // When & Then
        mvc.perform(delete("/visits/1"))
            .andExpect(status().isNoContent());
    }

    private Visit createVisit(Integer id, Integer petId, Integer vetId, String description, String status) {
        Visit visit = new Visit(petId, vetId);
        visit.setId(id);
        visit.setDescription(description);
        visit.setStatus(status);
        return visit;
    }
}
