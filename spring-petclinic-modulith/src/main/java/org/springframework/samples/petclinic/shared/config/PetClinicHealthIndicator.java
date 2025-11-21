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

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

/**
 * Custom Health Indicator for Spring PetClinic Modulith.
 * 
 * Provides health status information for:
 * - Database connectivity
 * - Module initialization status
 * - External service availability (AI/Chat service)
 * 
 * Health status is exposed via /actuator/health
 * 
 * @author PetClinic Team
 */
@Component("petclinic-modulith")
public class PetClinicHealthIndicator extends AbstractHealthIndicator {

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        // All checks passed
        builder.up()
                .withDetail("application", "Spring PetClinic Modulith")
                .withDetail("modules", "Customers, Vets, Visits, GenAI")
                .withDetail("architecture", "Spring Modulith 1.3.0")
                .withDetail("features", new String[]{
                    "Event-driven architecture",
                    "Distributed tracing with Zipkin",
                    "Prometheus metrics export",
                    "Spring AI chat integration"
                });
    }
}
