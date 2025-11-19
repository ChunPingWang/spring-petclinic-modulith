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
package org.springframework.samples.petclinic.genai.internal.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.samples.petclinic.genai.ChatService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for ChatClientResource.
 *
 * @author PetClinic Team
 */
@WebMvcTest(ChatClientResource.class)
@ActiveProfiles("test")
class ChatClientResourceTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    ChatService chatService;

    @Test
    void shouldExchangeChatMessage() throws Exception {
        // Given
        String query = "List all owners";
        String expectedResponse = "The pet clinic has the following owners: ...";
        when(chatService.exchange(anyString())).thenReturn(expectedResponse);

        // When & Then
        mvc.perform(post("/chatclient")
                .contentType("text/plain")
                .content(query))
            .andExpect(status().isOk())
            .andExpect(content().string(expectedResponse));
    }

    @Test
    void shouldHandleErrorInChatMessage() throws Exception {
        // Given
        String query = "Invalid query";
        String errorResponse = "Chat is currently unavailable. Please try again later.";
        when(chatService.exchange(anyString())).thenReturn(errorResponse);

        // When & Then
        mvc.perform(post("/chatclient")
                .contentType("text/plain")
                .content(query))
            .andExpect(status().isOk())
            .andExpect(content().string(errorResponse));
    }
}
