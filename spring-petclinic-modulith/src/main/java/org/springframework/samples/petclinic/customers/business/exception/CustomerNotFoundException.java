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
package org.springframework.samples.petclinic.customers.business.exception;

/**
 * Pure Java business exception for customer not found scenarios.
 *
 * This exception is part of the business layer and has no framework dependencies.
 *
 * @author PetClinic Team
 */
public class CustomerNotFoundException extends RuntimeException {

    private final Integer customerId;

    public CustomerNotFoundException(String message) {
        super(message);
        this.customerId = null;
    }

    public CustomerNotFoundException(String message, Integer customerId) {
        super(message);
        this.customerId = customerId;
    }

    public Integer getCustomerId() {
        return customerId;
    }
}
