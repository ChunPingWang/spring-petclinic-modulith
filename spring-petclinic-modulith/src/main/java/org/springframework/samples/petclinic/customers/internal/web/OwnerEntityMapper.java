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
package org.springframework.samples.petclinic.customers.internal.web;

import org.springframework.samples.petclinic.customers.Customer;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting OwnerRequest to Customer entity.
 * 
 * This is done by hand for simplicity purpose. 
 * In a real life use-case we should consider using MapStruct.
 * 
 * @author PetClinic Team
 */
@Component
class OwnerEntityMapper {
    
    Customer map(Customer customer, OwnerRequest request) {
        customer.setAddress(request.address());
        customer.setCity(request.city());
        customer.setTelephone(request.telephone());
        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());
        return customer;
    }
}
