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
package org.springframework.samples.petclinic.visits.infrastructure.validator;

import org.springframework.samples.petclinic.vets.VetService;
import org.springframework.samples.petclinic.visits.business.port.VetValidator;
import org.springframework.stereotype.Component;

/**
 * Adapter implementing VetValidator port using VetService.
 *
 * This adapter bridges the business layer with the vets module,
 * allowing validation of vet existence without direct dependency on vets module internals.
 *
 * @author PetClinic Team
 */
@Component
public class VetServiceVetValidator implements VetValidator {

    private final VetService vetService;

    public VetServiceVetValidator(VetService vetService) {
        this.vetService = vetService;
    }

    @Override
    public boolean vetExists(Integer vetId) {
        if (vetId == null || vetId <= 0) {
            return false;
        }
        return vetService.findById(vetId).isPresent();
    }
}
