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
import org.springframework.samples.petclinic.customers.Customer;
import org.springframework.samples.petclinic.customers.CustomerCreated;
import org.springframework.samples.petclinic.customers.CustomerDeleted;
import org.springframework.samples.petclinic.customers.CustomerService;
import org.springframework.samples.petclinic.customers.CustomerUpdated;
import org.springframework.samples.petclinic.customers.business.exception.CustomerNotFoundException;
import org.springframework.samples.petclinic.customers.business.port.CustomerRepository;
import org.springframework.samples.petclinic.customers.business.port.EventPublisher;
import org.springframework.samples.petclinic.customers.business.service.CustomerBusinessService;
import org.springframework.samples.petclinic.customers.infrastructure.persistence.mapper.DomainMapper;
import org.springframework.samples.petclinic.shared.exceptions.ResourceNotFoundException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for CustomerServiceImpl.
 *
 * Tests verify business logic integration with the new three-layer architecture.
 * This test now uses the CustomerBusinessService with adapters.
 *
 * @author PetClinic Team
 */
@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private EventPublisher eventPublisher;

    private CustomerBusinessService businessService;
    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        businessService = new CustomerBusinessService(customerRepository, eventPublisher);
        customerService = new CustomerServiceImpl(businessService);
    }

    @Test
    void shouldFindCustomerById() {
        // Given
        org.springframework.samples.petclinic.customers.domain.Customer domainCustomer =
                createDomainCustomer(1, "George", "Franklin");
        given(customerRepository.findById(1)).willReturn(Optional.of(domainCustomer));

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
        List<org.springframework.samples.petclinic.customers.domain.Customer> domainCustomers = Arrays.asList(
            createDomainCustomer(1, "George", "Franklin"),
            createDomainCustomer(2, "Betty", "Davis")
        );
        given(customerRepository.findAll()).willReturn(domainCustomers);

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
        Customer legacyCustomer = createLegacyCustomer(null, "John", "Doe");
        org.springframework.samples.petclinic.customers.domain.Customer savedDomainCustomer =
                createDomainCustomer(1, "John", "Doe");

        given(customerRepository.save(any(org.springframework.samples.petclinic.customers.domain.Customer.class)))
                .willReturn(savedDomainCustomer);

        // When
        Customer result = customerService.save(legacyCustomer);

        // Then
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getFirstName()).isEqualTo("John");

        // Verify event was published
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publish(eventCaptor.capture());

        Object event = eventCaptor.getValue();
        assertThat(event).isInstanceOf(CustomerCreated.class);
        CustomerCreated customerCreatedEvent = (CustomerCreated) event;
        assertThat(customerCreatedEvent.customerId()).isEqualTo(1);
        assertThat(customerCreatedEvent.customerName()).isEqualTo("John Doe");
    }

    @Test
    void shouldUpdateCustomerAndPublishEvent() {
        // Given
        org.springframework.samples.petclinic.customers.domain.Customer existingDomainCustomer =
                createDomainCustomer(1, "George", "Franklin");
        Customer legacyUpdateData = createLegacyCustomer(null, "George", "Washington");
        legacyUpdateData.setAddress("New Address");
        legacyUpdateData.setCity("New City");
        legacyUpdateData.setTelephone("1234567890");

        given(customerRepository.findById(1)).willReturn(Optional.of(existingDomainCustomer));
        given(customerRepository.save(any(org.springframework.samples.petclinic.customers.domain.Customer.class)))
                .willReturn(existingDomainCustomer);

        // When
        Customer result = customerService.update(1, legacyUpdateData);

        // Then
        assertThat(result.getLastName()).isEqualTo("Washington");
        assertThat(result.getAddress()).isEqualTo("New Address");

        // Verify event was published
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publish(eventCaptor.capture());

        Object event = eventCaptor.getValue();
        assertThat(event).isInstanceOf(CustomerUpdated.class);
        CustomerUpdated customerUpdatedEvent = (CustomerUpdated) event;
        assertThat(customerUpdatedEvent.customerId()).isEqualTo(1);
        assertThat(customerUpdatedEvent.customerName()).isEqualTo("George Washington");
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentCustomer() {
        // Given
        Customer legacyUpdateData = createLegacyCustomer(null, "John", "Doe");
        given(customerRepository.findById(999)).willReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> customerService.update(999, legacyUpdateData))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Customer")
            .hasMessageContaining("999");
    }

    @Test
    void shouldDeleteCustomerAndPublishEvent() {
        // Given
        org.springframework.samples.petclinic.customers.domain.Customer domainCustomer =
                createDomainCustomer(1, "George", "Franklin");
        given(customerRepository.findById(1)).willReturn(Optional.of(domainCustomer));

        // When
        customerService.deleteById(1);

        // Then
        verify(customerRepository).deleteById(1);

        // Verify event was published
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publish(eventCaptor.capture());

        Object event = eventCaptor.getValue();
        assertThat(event).isInstanceOf(CustomerDeleted.class);
        CustomerDeleted customerDeletedEvent = (CustomerDeleted) event;
        assertThat(customerDeletedEvent.customerId()).isEqualTo(1);
        assertThat(customerDeletedEvent.customerName()).isEqualTo("George Franklin");
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentCustomer() {
        // Given
        given(customerRepository.findById(999)).willReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> customerService.deleteById(999))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Customer")
            .hasMessageContaining("999");
    }

    private Customer createLegacyCustomer(Integer id, String firstName, String lastName) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setAddress("123 Main St");
        customer.setCity("Springfield");
        customer.setTelephone("1234567890");
        return customer;
    }

    private org.springframework.samples.petclinic.customers.domain.Customer createDomainCustomer(
            Integer id, String firstName, String lastName) {
        org.springframework.samples.petclinic.customers.domain.Customer customer =
                new org.springframework.samples.petclinic.customers.domain.Customer();
        customer.setId(id);
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setAddress("123 Main St");
        customer.setCity("Springfield");
        customer.setTelephone("1234567890");
        return customer;
    }
}
