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
package org.springframework.samples.petclinic.customers.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.samples.petclinic.customers.Customer;
import org.springframework.samples.petclinic.customers.CustomerCreated;
import org.springframework.samples.petclinic.customers.CustomerService;
import org.springframework.samples.petclinic.customers.CustomerUpdated;
import org.springframework.samples.petclinic.shared.exceptions.ResourceNotFoundException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for CustomerServiceImpl.
 * 
 * Tests verify business logic and event publishing.
 * 
 * @author PetClinic Team
 */
@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ApplicationEventPublisher events;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerServiceImpl(customerRepository, events);
    }

    @Test
    void shouldFindCustomerById() {
        // Given
        Customer customer = createCustomer(1, "George", "Franklin");
        given(customerRepository.findById(1)).willReturn(Optional.of(customer));

        // When
        Optional<Customer> result = customerService.findById(1);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getFirstName()).isEqualTo("George");
        assertThat(result.get().getLastName()).isEqualTo("Franklin");
    }

    @Test
    void shouldReturnEmptyWhenCustomerNotFound() {
        // Given
        given(customerRepository.findById(999)).willReturn(Optional.empty());

        // When
        Optional<Customer> result = customerService.findById(999);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindAllCustomers() {
        // Given
        List<Customer> customers = Arrays.asList(
            createCustomer(1, "George", "Franklin"),
            createCustomer(2, "Betty", "Davis")
        );
        given(customerRepository.findAll()).willReturn(customers);

        // When
        List<Customer> result = customerService.findAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFirstName()).isEqualTo("George");
        assertThat(result.get(1).getFirstName()).isEqualTo("Betty");
    }

    @Test
    void shouldSaveCustomerAndPublishEvent() {
        // Given
        Customer customer = createCustomer(null, "John", "Doe");
        Customer savedCustomer = createCustomer(1, "John", "Doe");
        
        given(customerRepository.save(any(Customer.class))).willReturn(savedCustomer);

        // When
        Customer result = customerService.save(customer);

        // Then
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getFirstName()).isEqualTo("John");
        
        // Verify event was published
        ArgumentCaptor<CustomerCreated> eventCaptor = ArgumentCaptor.forClass(CustomerCreated.class);
        verify(events).publishEvent(eventCaptor.capture());
        
        CustomerCreated event = eventCaptor.getValue();
        assertThat(event.customerId()).isEqualTo(1);
        assertThat(event.customerName()).isEqualTo("John Doe");
    }

    @Test
    void shouldUpdateCustomerAndPublishEvent() {
        // Given
        Customer existingCustomer = createCustomer(1, "George", "Franklin");
        Customer updateData = createCustomer(null, "George", "Washington");
        updateData.setAddress("New Address");
        updateData.setCity("New City");
        updateData.setTelephone("1234567890");
        
        given(customerRepository.findById(1)).willReturn(Optional.of(existingCustomer));
        given(customerRepository.save(any(Customer.class))).willReturn(existingCustomer);

        // When
        Customer result = customerService.update(1, updateData);

        // Then
        assertThat(result.getLastName()).isEqualTo("Washington");
        assertThat(result.getAddress()).isEqualTo("New Address");
        
        // Verify event was published
        ArgumentCaptor<CustomerUpdated> eventCaptor = ArgumentCaptor.forClass(CustomerUpdated.class);
        verify(events).publishEvent(eventCaptor.capture());
        
        CustomerUpdated event = eventCaptor.getValue();
        assertThat(event.customerId()).isEqualTo(1);
        assertThat(event.customerName()).isEqualTo("George Washington");
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentCustomer() {
        // Given
        Customer updateData = createCustomer(null, "John", "Doe");
        given(customerRepository.findById(999)).willReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> customerService.update(999, updateData))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Customer")
            .hasMessageContaining("999");
    }

    private Customer createCustomer(Integer id, String firstName, String lastName) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setAddress("123 Main St");
        customer.setCity("Springfield");
        customer.setTelephone("1234567890");
        return customer;
    }
}
