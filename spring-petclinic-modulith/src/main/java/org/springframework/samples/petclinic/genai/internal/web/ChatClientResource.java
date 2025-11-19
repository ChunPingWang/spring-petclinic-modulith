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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.genai.ChatService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for interacting with the AI chat client.
 * This controller is invoked by the frontend to interact with the LLM.
 *
 * @author PetClinic Team
 */
@RestController
@RequestMapping("/")
public class ChatClientResource {

    private static final Logger LOG = LoggerFactory.getLogger(ChatClientResource.class);

    private final ChatService chatService;

    public ChatClientResource(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chatclient")
    public String exchange(@RequestBody String query) {
        LOG.debug("Processing chat query: {}", query);
        return chatService.exchange(query);
    }
}
