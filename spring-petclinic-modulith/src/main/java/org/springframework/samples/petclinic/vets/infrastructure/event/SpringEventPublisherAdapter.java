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
package org.springframework.samples.petclinic.vets.infrastructure.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.samples.petclinic.vets.business.port.EventPublisher;
import org.springframework.stereotype.Component;

/**
 * Adapter implementation of EventPublisher port using Spring's ApplicationEventPublisher.
 *
 * This adapter bridges the pure Java business layer with Spring's event publishing infrastructure.
 *
 * @author PetClinic Team
 */
@Component("vetsEventPublisher")
public class SpringEventPublisherAdapter implements EventPublisher {

    private final ApplicationEventPublisher springEventPublisher;

    public SpringEventPublisherAdapter(ApplicationEventPublisher springEventPublisher) {
        this.springEventPublisher = springEventPublisher;
    }

    @Override
    public void publish(Object event) {
        springEventPublisher.publishEvent(event);
    }
}
