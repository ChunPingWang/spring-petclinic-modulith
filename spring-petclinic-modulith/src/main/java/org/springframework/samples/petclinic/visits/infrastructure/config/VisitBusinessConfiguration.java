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
package org.springframework.samples.petclinic.visits.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.samples.petclinic.visits.business.port.EventPublisher;
import org.springframework.samples.petclinic.visits.business.port.PetValidator;
import org.springframework.samples.petclinic.visits.business.port.VetValidator;
import org.springframework.samples.petclinic.visits.business.port.VisitRepository;
import org.springframework.samples.petclinic.visits.business.service.VisitBusinessService;

/**
 * Spring configuration for Visit business layer.
 *
 * This configuration wires up the pure Java business service with its dependencies.
 * The business service has zero Spring dependencies - it only depends on pure Java ports.
 *
 * @author PetClinic Team
 */
@Configuration
public class VisitBusinessConfiguration {

    /**
     * Create the VisitBusinessService bean.
     *
     * @param visitRepository the repository port implementation (provided by infrastructure)
     * @param petValidator the pet validator port implementation (provided by infrastructure)
     * @param vetValidator the vet validator port implementation (provided by infrastructure)
     * @param eventPublisher the event publisher port implementation (provided by infrastructure)
     * @return the business service instance
     */
    @Bean
    public VisitBusinessService visitBusinessService(
            VisitRepository visitRepository,
            PetValidator petValidator,
            VetValidator vetValidator,
            EventPublisher eventPublisher) {
        return new VisitBusinessService(visitRepository, petValidator, vetValidator, eventPublisher);
    }
}
