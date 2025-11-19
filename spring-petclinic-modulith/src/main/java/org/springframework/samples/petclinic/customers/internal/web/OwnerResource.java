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

import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.customers.Customer;
import org.springframework.samples.petclinic.customers.CustomerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing owners (customers).
 * 
 * Keeps the original /owners endpoint for backward compatibility.
 * 
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Maciej Szarlinski
 */
@RequestMapping("/owners")
@RestController
@Timed("petclinic.owner")
class OwnerResource {

    private static final Logger log = LoggerFactory.getLogger(OwnerResource.class);

    private final CustomerService customerService;
    private final OwnerEntityMapper ownerEntityMapper;

    OwnerResource(CustomerService customerService, OwnerEntityMapper ownerEntityMapper) {
        this.customerService = customerService;
        this.ownerEntityMapper = ownerEntityMapper;
    }

    /**
     * Create Owner
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Customer createOwner(@Valid @RequestBody OwnerRequest ownerRequest) {
        Customer customer = ownerEntityMapper.map(new Customer(), ownerRequest);
        return customerService.save(customer);
    }

    /**
     * Read single Owner
     */
    @GetMapping(value = "/{ownerId}")
    public Optional<Customer> findOwner(@PathVariable("ownerId") @Min(1) int ownerId) {
        return customerService.findById(ownerId);
    }

    /**
     * Read List of Owners
     */
    @GetMapping
    public List<Customer> findAll() {
        return customerService.findAll();
    }

    /**
     * Update Owner
     */
    @PutMapping(value = "/{ownerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateOwner(@PathVariable("ownerId") @Min(1) int ownerId, 
                           @Valid @RequestBody OwnerRequest ownerRequest) {
        Customer customer = ownerEntityMapper.map(new Customer(), ownerRequest);
        log.info("Updating owner {}", ownerId);
        customerService.update(ownerId, customer);
    }
}
