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
package org.springframework.samples.petclinic.shared.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Custom Prometheus Metrics Configuration for Spring PetClinic Modulith.
 * 
 * Defines business metrics that track:
 * - Owner operations (create, update, delete)
 * - Pet operations (create, update, delete)
 * - Visit operations (create, complete, cancel)
 * - Chat interactions
 * 
 * These metrics are exposed via /actuator/prometheus
 * 
 * @author PetClinic Team
 */
@Configuration
public class PrometheusMetricsConfig {

    /**
     * Counter for created owners.
     * Tracks the total number of owners created in the system.
     */
    @Bean
    public Counter ownersCreatedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("petclinic.owners.created")
                .description("Total number of owners created")
                .tag("module", "customers")
                .register(meterRegistry);
    }

    /**
     * Counter for created pets.
     * Tracks the total number of pets created in the system.
     */
    @Bean
    public Counter petsCreatedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("petclinic.pets.created")
                .description("Total number of pets created")
                .tag("module", "customers")
                .register(meterRegistry);
    }

    /**
     * Counter for scheduled visits.
     * Tracks the total number of visits scheduled in the system.
     */
    @Bean
    public Counter visitsScheduledCounter(MeterRegistry meterRegistry) {
        return Counter.builder("petclinic.visits.scheduled")
                .description("Total number of visits scheduled")
                .tag("module", "visits")
                .register(meterRegistry);
    }

    /**
     * Counter for completed visits.
     * Tracks the total number of visits completed in the system.
     */
    @Bean
    public Counter visitsCompletedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("petclinic.visits.completed")
                .description("Total number of visits completed")
                .tag("module", "visits")
                .register(meterRegistry);
    }

    /**
     * Counter for chat interactions.
     * Tracks the total number of chat interactions with the AI system.
     */
    @Bean
    public Counter chatInteractionsCounter(MeterRegistry meterRegistry) {
        return Counter.builder("petclinic.chat.interactions")
                .description("Total number of chat interactions")
                .tag("module", "genai")
                .register(meterRegistry);
    }
}
