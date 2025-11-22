/*
 * Copyright 2002-2025 the original author or authors.
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
package org.springframework.samples.petclinic.shared.web;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.shared.exceptions.ResourceNotFoundException;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for RestExceptionHandler.
 *
 * Verifies that:
 * - ResourceNotFoundException returns 404 with proper error response
 * - NoSuchElementException returns 404 with proper error response
 * - ResponseStatusException returns correct status code
 * - Generic exceptions return 500
 *
 * @author PetClinic Team
 */
class RestExceptionHandlerTest {

    private RestExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new RestExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/test/endpoint");
    }

    @Test
    void shouldHandle404WhenResourceNotFound() {
        // Given
        ResourceNotFoundException ex = new ResourceNotFoundException("Owner", 999);

        // When
        ErrorResponse response = handler.handleResourceNotFound(ex, request);

        // Then
        assertThat(response.status()).isEqualTo(404);
        assertThat(response.error()).isEqualTo("Not Found");
        assertThat(response.message()).isEqualTo("Owner not found with id: 999");
        assertThat(response.path()).isEqualTo("/test/endpoint");
        assertThat(response.timestamp()).isNotNull();
    }

    @Test
    void shouldHandle404WhenNoSuchElement() {
        // Given
        NoSuchElementException ex = new NoSuchElementException("Element not found");

        // When
        ErrorResponse response = handler.handleNoSuchElement(ex, request);

        // Then
        assertThat(response.status()).isEqualTo(404);
        assertThat(response.error()).isEqualTo("Not Found");
        assertThat(response.message()).isEqualTo("Element not found");
        assertThat(response.path()).isEqualTo("/test/endpoint");
        assertThat(response.timestamp()).isNotNull();
    }

    @Test
    void shouldHandleNoSuchElementWithNullMessage() {
        // Given
        NoSuchElementException ex = new NoSuchElementException();

        // When
        ErrorResponse response = handler.handleNoSuchElement(ex, request);

        // Then
        assertThat(response.status()).isEqualTo(404);
        assertThat(response.error()).isEqualTo("Not Found");
        assertThat(response.message()).isEqualTo("Resource not found");
        assertThat(response.path()).isEqualTo("/test/endpoint");
        assertThat(response.timestamp()).isNotNull();
    }

    @Test
    void shouldHandleResponseStatusException() {
        // Given
        ResponseStatusException ex = new ResponseStatusException(
            HttpStatus.CONFLICT, "Resource already exists");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleResponseStatusException(ex, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(409);
        assertThat(response.getBody().message()).isEqualTo("Resource already exists");
        assertThat(response.getBody().path()).isEqualTo("/test/endpoint");
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    void shouldHandle500WhenGenericException() {
        // Given
        RuntimeException ex = new RuntimeException("Unexpected error");

        // When
        ErrorResponse response = handler.handleGenericException(ex, request);

        // Then
        assertThat(response.status()).isEqualTo(500);
        assertThat(response.error()).isEqualTo("Internal Server Error");
        assertThat(response.message()).isEqualTo("An unexpected error occurred. Please try again later.");
        assertThat(response.path()).isEqualTo("/test/endpoint");
        assertThat(response.timestamp()).isNotNull();
    }
}
