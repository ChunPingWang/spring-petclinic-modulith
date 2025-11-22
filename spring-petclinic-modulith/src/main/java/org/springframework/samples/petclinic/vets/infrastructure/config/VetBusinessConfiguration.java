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
package org.springframework.samples.petclinic.vets.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.samples.petclinic.vets.business.port.EventPublisher;
import org.springframework.samples.petclinic.vets.business.port.VetRepository;
import org.springframework.samples.petclinic.vets.business.service.VetBusinessService;

/**
 * Spring configuration for Vet business layer.
 *
 * This configuration wires up the pure Java business service with its dependencies.
 * The business service has zero Spring dependencies - it only depends on pure Java ports.
 *
 * @author PetClinic Team
 */
@Configuration
public class VetBusinessConfiguration {

    /**
     * Create the VetBusinessService bean.
     *
     * @param vetRepository the repository port implementation (provided by infrastructure)
     * @param eventPublisher the event publisher port implementation (provided by infrastructure)
     * @return the business service instance
     */
    @Bean
    public VetBusinessService vetBusinessService(
            VetRepository vetRepository,
            EventPublisher eventPublisher) {
        return new VetBusinessService(vetRepository, eventPublisher);
    }
}
