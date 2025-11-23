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
package org.springframework.samples.petclinic.customers.business.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.samples.petclinic.customers.business.exception.CustomerNotFoundException;
import org.springframework.samples.petclinic.customers.business.port.CustomerRepository;
import org.springframework.samples.petclinic.customers.business.port.EventPublisher;
import org.springframework.samples.petclinic.customers.domain.Customer;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

/**
 * Unit tests for CustomerBusinessService.
 *
 * Tests the business logic in isolation without any framework dependencies.
 *
 * @author PetClinic Team
 */
@ExtendWith(MockitoExtension.class)
class CustomerBusinessServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private EventPublisher eventPublisher;

    private CustomerBusinessService businessService;

    @BeforeEach
    void setUp() {
        businessService = new CustomerBusinessService(customerRepository, eventPublisher);
    }

    @Test
    void shouldFindCustomerById() {
        // Given
        Customer customer = createCustomer(1, "John", "Doe", "123 Main St", "New York", "1234567890");
        given(customerRepository.findById(1)).willReturn(Optional.of(customer));

        // When
        Optional<Customer> result = businessService.findById(1);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getFirstName()).isEqualTo("John");
        assertThat(result.get().getLastName()).isEqualTo("Doe");
        verify(customerRepository).findById(1);
    }

    @Test
    void shouldReturnEmptyWhenCustomerNotFound() {
        // Given
        given(customerRepository.findById(999)).willReturn(Optional.empty());

        // When
        Optional<Customer> result = businessService.findById(999);

        // Then
        assertThat(result).isEmpty();
        verify(customerRepository).findById(999);
    }

    @Test
    void shouldThrowExceptionWhenFindByIdWithInvalidId() {
        // When/Then
        assertThatThrownBy(() -> businessService.findById(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("must be positive");

        assertThatThrownBy(() -> businessService.findById(0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("must be positive");

        assertThatThrownBy(() -> businessService.findById(-1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("must be positive");

        verify(customerRepository, never()).findById(any());
    }

    @Test
    void shouldFindAllCustomers() {
        // Given
        List<Customer> customers = Arrays.asList(
            createCustomer(1, "John", "Doe", "123 Main St", "New York", "1234567890"),
            createCustomer(2, "Jane", "Smith", "456 Elm St", "Boston", "0987654321")
        );
        given(customerRepository.findAll()).willReturn(customers);

        // When
        List<Customer> result = businessService.findAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1);
        assertThat(result.get(1).getId()).isEqualTo(2);
        verify(customerRepository).findAll();
    }

    @Test
    void shouldCreateCustomer() {
        // Given
        Customer newCustomer = createCustomer(null, "John", "Doe", "123 Main St", "New York", "1234567890");
        Customer savedCustomer = createCustomer(1, "John", "Doe", "123 Main St", "New York", "1234567890");

        given(customerRepository.save(any(Customer.class))).willReturn(savedCustomer);

        // When
        Customer result = businessService.createCustomer(newCustomer);

        // Then
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getFirstName()).isEqualTo("John");
        verify(customerRepository).save(newCustomer);
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldThrowExceptionWhenCreatingNullCustomer() {
        // When/Then
        assertThatThrownBy(() -> businessService.createCustomer(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be null");

        verify(customerRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenCreatingCustomerWithId() {
        // Given
        Customer customerWithId = createCustomer(1, "John", "Doe", "123 Main St", "New York", "1234567890");

        // When/Then
        assertThatThrownBy(() -> businessService.createCustomer(customerWithId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("should not have an ID");

        verify(customerRepository, never()).save(any());
    }

    @Test
    void shouldValidateCustomerOnCreate() {
        // Given - customer with missing first name (invalid)
        Customer invalidCustomer = new Customer();
        invalidCustomer.setFirstName(""); // Empty first name
        invalidCustomer.setLastName("Doe");
        invalidCustomer.setAddress("123 Main St");
        invalidCustomer.setCity("New York");
        invalidCustomer.setTelephone("1234567890");

        // When/Then
        assertThatThrownBy(() -> businessService.createCustomer(invalidCustomer))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("First name is required");

        verify(customerRepository, never()).save(any());
    }

    @Test
    void shouldUpdateCustomer() {
        // Given
        Customer existingCustomer = createCustomer(1, "John", "Doe", "123 Main St", "New York", "1234567890");
        Customer updatedData = createCustomer(1, "John", "Smith", "456 Elm St", "Boston", "0987654321");

        given(customerRepository.findById(1)).willReturn(Optional.of(existingCustomer));
        given(customerRepository.save(any(Customer.class))).willReturn(existingCustomer);

        // When
        Customer result = businessService.updateCustomer(1, updatedData);

        // Then
        assertThat(result.getLastName()).isEqualTo("Smith");
        assertThat(result.getAddress()).isEqualTo("456 Elm St");
        assertThat(result.getCity()).isEqualTo("Boston");
        verify(customerRepository).findById(1);
        verify(customerRepository).save(existingCustomer);
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentCustomer() {
        // Given
        Customer customer = createCustomer(1, "John", "Doe", "123 Main St", "New York", "1234567890");
        given(customerRepository.findById(999)).willReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> businessService.updateCustomer(999, customer))
            .isInstanceOf(CustomerNotFoundException.class)
            .hasMessageContaining("Customer not found");

        verify(customerRepository).findById(999);
        verify(customerRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void shouldDeleteCustomer() {
        // Given
        Customer customer = createCustomer(1, "John", "Doe", "123 Main St", "New York", "1234567890");
        given(customerRepository.findById(1)).willReturn(Optional.of(customer));

        // When
        businessService.deleteCustomer(1);

        // Then
        verify(customerRepository).findById(1);
        verify(customerRepository).deleteById(1);
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentCustomer() {
        // Given
        given(customerRepository.findById(999)).willReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> businessService.deleteCustomer(999))
            .isInstanceOf(CustomerNotFoundException.class)
            .hasMessageContaining("Customer not found");

        verify(customerRepository).findById(999);
        verify(customerRepository, never()).deleteById(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void shouldValidateTelephoneFormat() {
        // Given - customer with invalid telephone (less than 10 digits)
        Customer invalidCustomer = new Customer();
        invalidCustomer.setFirstName("John");
        invalidCustomer.setLastName("Doe");
        invalidCustomer.setAddress("123 Main St");
        invalidCustomer.setCity("New York");
        invalidCustomer.setTelephone("123"); // Invalid: too short

        // When/Then
        assertThatThrownBy(() -> businessService.createCustomer(invalidCustomer))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Telephone must be 10-12 digits");

        verify(customerRepository, never()).save(any());
    }

    private Customer createCustomer(Integer id, String firstName, String lastName,
                                    String address, String city, String telephone) {
        return new Customer(id, firstName, lastName, address, city, telephone);
    }
}
