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
package org.springframework.samples.petclinic.customers.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.samples.petclinic.customers.business.port.CustomerRepository;
import org.springframework.samples.petclinic.customers.business.port.EventPublisher;
import org.springframework.samples.petclinic.customers.business.service.CustomerBusinessService;

/**
 * Spring configuration for the customer business layer.
 *
 * This configuration wires up the pure Java business service
 * with infrastructure adapters, following dependency inversion principle.
 *
 * @author PetClinic Team
 */
@Configuration
public class CustomerBusinessConfiguration {

    /**
     * Creates the CustomerBusinessService bean with infrastructure adapters injected.
     *
     * @param customerRepository The repository adapter (implemented by CustomerRepositoryAdapter)
     * @param eventPublisher The event publisher adapter (implemented by SpringEventPublisherAdapter)
     * @return The pure Java business service
     */
    @Bean
    public CustomerBusinessService customerBusinessService(
            CustomerRepository customerRepository,
            EventPublisher eventPublisher) {
        return new CustomerBusinessService(customerRepository, eventPublisher);
    }
}
