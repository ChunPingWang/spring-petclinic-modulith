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
package org.springframework.samples.petclinic.customers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for Customers module event publishing.
 * 
 * Tests verify that domain events are properly published and can be
 * consumed by event listeners in other modules.
 * 
 * @author PetClinic Team
 */
@ApplicationModuleTest
@ActiveProfiles("test")
@Transactional
class CustomersModuleEventsTest {

    @Autowired
    CustomerService customerService;

    @Test
    void publishesCustomerCreatedEvent(Scenario scenario) {
        // Given
        Customer customer = new Customer();
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setAddress("123 Main St");
        customer.setCity("Springfield");
        customer.setTelephone("1234567890");

        // When - save customer and expect event
        scenario.stimulate(() -> customerService.save(customer))
            .andWaitForEventOfType(CustomerCreated.class)
            .matching(event -> event.customerName().equals("John Doe"))
            .toArrive();
    }

    @Test
    void publishesCustomerUpdatedEvent(Scenario scenario) {
        // Given - create a customer first
        Customer customer = new Customer();
        customer.setFirstName("Jane");
        customer.setLastName("Smith");
        customer.setAddress("456 Oak Ave");
        customer.setCity("Portland");
        customer.setTelephone("0987654321");
        
        Customer savedCustomer = customerService.save(customer);

        // When - update customer and expect event
        Customer updateData = new Customer();
        updateData.setFirstName("Jane");
        updateData.setLastName("Johnson");  // Changed last name
        updateData.setAddress("456 Oak Ave");
        updateData.setCity("Portland");
        updateData.setTelephone("0987654321");

        scenario.stimulate(() -> customerService.update(savedCustomer.getId(), updateData))
            .andWaitForEventOfType(CustomerUpdated.class)
            .matching(event -> event.customerName().equals("Jane Johnson"))
            .toArrive();
    }

    @Test
    void customerServiceIsAvailable() {
        // Verify that CustomerService is properly registered as a Spring bean
        assertThat(customerService).isNotNull();
    }
}
