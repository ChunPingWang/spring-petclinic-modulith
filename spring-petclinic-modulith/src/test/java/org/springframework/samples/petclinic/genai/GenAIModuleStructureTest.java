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
package org.springframework.samples.petclinic.genai;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for GenAI module structure and cross-module dependencies.
 *
 * @author PetClinic Team
 */
@SpringBootTest
@ActiveProfiles("test")
class GenAIModuleStructureTest {

    @Autowired
    ChatService chatService;

    @Test
    void chatServiceIsAvailable() {
        // Verify that ChatService is properly registered as a Spring bean
        assertThat(chatService).isNotNull();
    }

    @Test
    void shouldExchangeMessage() {
        // Basic smoke test for chat service
        String response = chatService.exchange("Hello");
        assertThat(response).isNotNull();
        // Response could be the actual AI response or the error message
        assertThat(response.length()).isGreaterThan(0);
    }
}
