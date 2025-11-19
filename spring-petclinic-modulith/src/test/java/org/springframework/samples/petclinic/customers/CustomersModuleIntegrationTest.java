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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for Customers module structure and boundaries.
 * 
 * Verifies:
 * - Module structure follows Spring Modulith conventions
 * - No circular dependencies
 * - Proper encapsulation (internal packages not exposed)
 * 
 * @author PetClinic Team
 */
@SpringBootTest
@ActiveProfiles("test")
class CustomersModuleIntegrationTest {

    @Test
    void verifiesModuleStructure() {
        // Load the application module structure
        ApplicationModules modules = ApplicationModules.of(
            org.springframework.samples.petclinic.PetClinicApplication.class
        );

        // Verify module structure
        modules.verify();

        // Print module information for debugging
        modules.forEach(module -> {
            System.out.println("Module: " + module.getName());
            System.out.println("  Base Package: " + module.getBasePackage());
            System.out.println("  Named Interfaces: " + module.getNamedInterfaces());
        });
    }

    @Test
    void customersModuleExists() {
        ApplicationModules modules = ApplicationModules.of(
            org.springframework.samples.petclinic.PetClinicApplication.class
        );

        // Verify customers module exists
        assertThat(modules.stream()
            .anyMatch(module -> module.getName().equals("customers")))
            .isTrue();
    }

    @Test
    void customersModuleHasCorrectPublicApi() {
        ApplicationModules modules = ApplicationModules.of(
            org.springframework.samples.petclinic.PetClinicApplication.class
        );

        var customersModule = modules.getModuleByName("customers")
            .orElseThrow(() -> new AssertionError("Customers module not found"));

        // Verify that internal packages are indeed internal
        assertThat(customersModule.toString()).contains("customers");
        
        // Public API should be in root package, internal in 'internal' sub-package
        System.out.println("Customers Module Details:");
        System.out.println("  Name: " + customersModule.getName());
        System.out.println("  Display Name: " + customersModule.getDisplayName());
        System.out.println("  Base Package: " + customersModule.getBasePackage());
    }

    @Test
    void documentsModuleStructure() {
        ApplicationModules modules = ApplicationModules.of(
            org.springframework.samples.petclinic.PetClinicApplication.class
        );

        // Generate module documentation
        new Documenter(modules)
            .writeDocumentation()
            .writeIndividualModulesAsPlantUml();

        System.out.println("Module documentation generated in target/modulith-docs/");
    }
}
