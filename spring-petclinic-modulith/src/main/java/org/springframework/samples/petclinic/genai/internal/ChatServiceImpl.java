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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.samples.petclinic.genai.ChatService;
import org.springframework.stereotype.Service;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.DEFAULT_CHAT_MEMORY_CONVERSATION_ID;

/**
 * Implementation of ChatService that handles chat interactions with the LLM.
 *
 * @author PetClinic Team
 */
@Service
public class ChatServiceImpl implements ChatService {

    private static final Logger LOG = LoggerFactory.getLogger(ChatServiceImpl.class);

    private final ChatClient chatClient;

    public ChatServiceImpl(ChatClient.Builder builder, ChatMemory chatMemory) {
        // @formatter:off
        this.chatClient = builder
                .defaultSystem("""
                          You are a friendly AI assistant designed to help with the management of a veterinarian pet clinic called Spring Petclinic.
                          Your job is to answer questions about and to perform actions on the user's behalf, mainly around
                          veterinarians, owners, owners' pets and owners' visits.
                          You are required to answer an a professional manner. If you don't know the answer, politely tell the user
                          you don't know the answer, then ask the user a followup question to try and clarify the question they are asking.
                          If you do know the answer, provide the answer but do not provide any additional followup questions.
                          When dealing with vets, if the user is unsure about the returned results, explain that there may be additional data that was not returned.
                          Only if the user is asking about the total number of all vets, answer that there are a lot and ask for some additional criteria.
                          For owners, pets or visits - provide the correct data.
                          """)
                .defaultAdvisors(
                        // Chat memory helps us keep context when using the chatbot for up to 10 previous messages.
                        new MessageChatMemoryAdvisor(chatMemory, DEFAULT_CHAT_MEMORY_CONVERSATION_ID, 10), // CHAT MEMORY
                        new SimpleLoggerAdvisor()
                        )
                .defaultFunctions("listOwners", "addOwnerToPetclinic", "addPetToOwner", "listVets")
                .build();
        // @formatter:on
    }

    @Override
    public String exchange(String query) {
        try {
            // All chatbot messages go through this method
            // and are passed to the LLM
            return this.chatClient
                    .prompt()
                    .user(u -> u.text(query))
                    .call()
                    .content();
        } catch (Exception exception) {
            LOG.error("Error processing chat message", exception);
            return "Chat is currently unavailable. Please try again later.";
        }
    }
}
