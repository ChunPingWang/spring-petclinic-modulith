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
package org.springframework.samples.petclinic.vets.internal.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.vets.Vet;
import org.springframework.samples.petclinic.vets.VetService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for VetResource REST controller.
 * 
 * Tests verify REST endpoints for retrieving veterinarian information.
 * Uses VetService (public API) instead of direct repository access.
 * 
 * @author PetClinic Team
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(VetResource.class)
@ActiveProfiles("test")
class VetResourceTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    VetService vetService;

    @Test
    void shouldGetAllVetsInJsonFormat() throws Exception {
        // Given
        Vet vet1 = new Vet();
        vet1.setId(1);
        vet1.setFirstName("James");
        vet1.setLastName("Carter");

        Vet vet2 = new Vet();
        vet2.setId(2);
        vet2.setFirstName("Helen");
        vet2.setLastName("Leary");

        List<Vet> vets = Arrays.asList(vet1, vet2);
        given(vetService.findAll()).willReturn(vets);

        // When & Then
        mvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].firstName").value("James"))
            .andExpect(jsonPath("$[0].lastName").value("Carter"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].firstName").value("Helen"))
            .andExpect(jsonPath("$[1].lastName").value("Leary"));
    }

    @Test
    void shouldGetVetById() throws Exception {
        // Given
        Vet vet = new Vet();
        vet.setId(1);
        vet.setFirstName("James");
        vet.setLastName("Carter");

        given(vetService.findById(1)).willReturn(Optional.of(vet));

        // When & Then
        mvc.perform(get("/vets/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.firstName").value("James"))
            .andExpect(jsonPath("$.lastName").value("Carter"));
    }

    @Test
    void shouldReturn404WhenVetNotFound() throws Exception {
        // Given
        given(vetService.findById(999)).willReturn(Optional.empty());

        // When & Then
        mvc.perform(get("/vets/999").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewVet() throws Exception {
        // Given
        Vet newVet = new Vet();
        newVet.setFirstName("Sarah");
        newVet.setLastName("Miller");

        Vet savedVet = new Vet();
        savedVet.setId(10);
        savedVet.setFirstName("Sarah");
        savedVet.setLastName("Miller");

        given(vetService.save(any(Vet.class))).willReturn(savedVet);

        // When & Then
        mvc.perform(post("/vets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newVet)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(10))
            .andExpect(jsonPath("$.firstName").value("Sarah"))
            .andExpect(jsonPath("$.lastName").value("Miller"));

        verify(vetService).save(any(Vet.class));
    }

    @Test
    void shouldUpdateExistingVet() throws Exception {
        // Given
        Vet updatedVet = new Vet();
        updatedVet.setFirstName("James");
        updatedVet.setLastName("Smith");

        Vet returnedVet = new Vet();
        returnedVet.setId(1);
        returnedVet.setFirstName("James");
        returnedVet.setLastName("Smith");

        given(vetService.update(eq(1), any(Vet.class))).willReturn(returnedVet);

        // When & Then
        mvc.perform(put("/vets/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedVet)))
            .andExpect(status().isNoContent());

        verify(vetService).update(eq(1), any(Vet.class));
    }

    @Test
    void shouldDeleteVet() throws Exception {
        // Given
        Vet existingVet = new Vet();
        existingVet.setId(1);
        existingVet.setFirstName("James");
        existingVet.setLastName("Carter");

        given(vetService.findById(1)).willReturn(Optional.of(existingVet));
        doNothing().when(vetService).deleteById(1);

        // When & Then
        mvc.perform(delete("/vets/1"))
            .andExpect(status().isNoContent());

        verify(vetService).findById(1);
        verify(vetService).deleteById(1);
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentVet() throws Exception {
        // Given
        given(vetService.findById(999)).willReturn(Optional.empty());

        // When & Then
        mvc.perform(delete("/vets/999"))
            .andExpect(status().isNotFound());

        verify(vetService).findById(999);
    }
}
