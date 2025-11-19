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

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.samples.petclinic.customers.CustomerService;
import org.springframework.samples.petclinic.genai.internal.dto.OwnerDetails;
import org.springframework.samples.petclinic.genai.internal.dto.PetDetails;
import org.springframework.samples.petclinic.genai.internal.dto.PetRequest;
import org.springframework.samples.petclinic.vets.VetService;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service that provides data to the AI chat client by calling other module services
 * and vector store for RAG (Retrieval Augmented Generation) functionality.
 *
 * @author PetClinic Team
 */
@Service
public class AIDataProvider {

    private final VectorStore vectorStore;
    private final CustomerService customerService;
    private final VetService vetService;
    private final WebClient webClient;

    public AIDataProvider(VectorStore vectorStore, CustomerService customerService, 
                         VetService vetService, WebClient.Builder webClientBuilder) {
        this.vectorStore = vectorStore;
        this.customerService = customerService;
        this.vetService = vetService;
        this.webClient = webClientBuilder.build();
    }

    /**
     * Record for owners response.
     */
    public record OwnersResponse(List<OwnerDetails> owners) {}

    /**
     * Record for single owner response.
     */
    public record OwnerResponse(OwnerDetails owner) {}

    /**
     * Record for added pet response.
     */
    public record AddedPetResponse(PetDetails pet) {}

    /**
     * Record for vet response.
     */
    public record VetResponse(List<String> vets) {}

    /**
     * Record for vet request.
     */
    public record VetRequest(org.springframework.samples.petclinic.genai.internal.dto.Vet vet) {}

    /**
     * Record for add pet request.
     */
    public record AddPetRequest(PetRequest pet, Integer ownerId) {}

    /**
     * Record for owner request.
     */
    public record OwnerRequest(String firstName, String lastName, String address, String city, String telephone) {}

    public OwnersResponse getAllOwners() {
        return new OwnersResponse(webClient
                .get()
                .uri("http://localhost:8080/owners")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<OwnerDetails>>() {})
                .block());
    }

    public VetResponse getVets(VetRequest request) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String vetAsJson = objectMapper.writeValueAsString(request.vet());

        int topK = 20;
        if (request.vet() == null) {
            // Provide a limit of 50 results when zero parameters are sent
            topK = 50;
        }

        SearchRequest sr = SearchRequest.query(vetAsJson).withTopK(topK);

        List<Document> topMatches = this.vectorStore.similaritySearch(sr);
        List<String> results = topMatches.stream().map(Document::getContent).toList();
        return new VetResponse(results);
    }

    public AddedPetResponse addPetToOwner(AddPetRequest request) {
        return new AddedPetResponse(webClient
                .post()
                .uri("http://localhost:8080/owners/" + request.ownerId() + "/pets")
                .bodyValue(request.pet())
                .retrieve().bodyToMono(PetDetails.class).block());
    }

    public OwnerResponse addOwnerToPetclinic(OwnerRequest ownerRequest) {
        return new OwnerResponse(webClient
                .post()
                .uri("http://localhost:8080/owners")
                .bodyValue(ownerRequest)
                .retrieve().bodyToMono(OwnerDetails.class).block());
    }
}
