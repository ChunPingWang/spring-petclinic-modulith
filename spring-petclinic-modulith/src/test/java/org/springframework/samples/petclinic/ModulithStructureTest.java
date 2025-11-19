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
package org.springframework.samples.petclinic;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Tests for verifying the Spring Modulith structure.
 * 
 * This test class validates:
 * - Module boundaries are correctly defined
 * - No circular dependencies exist
 * - Module naming conventions are followed
 * - Documentation can be generated
 * 
 * @author PetClinic Team
 */
class ModulithStructureTest {

    private final ApplicationModules modules = ApplicationModules.of(PetClinicApplication.class);

    /**
     * Verifies that the application's module structure is valid.
     * This includes checking:
     * - No violations of module boundaries
     * - No circular dependencies
     * - Proper package structure
     */
    @Test
    void verifiesModularStructure() {
        modules.verify();
    }

    /**
     * Generates documentation for the application modules.
     * This creates:
     * - Module structure diagram
     * - Module dependencies
     * - Component diagrams
     */
    @Test
    void createModuleDocumentation() {
        new Documenter(modules)
            .writeDocumentation()
            .writeIndividualModulesAsPlantUml();
    }

    /**
     * Prints all detected modules to console.
     * Useful for debugging module structure.
     */
    @Test
    void printModules() {
        System.out.println("\n=== Detected Modules ===");
        modules.forEach(module -> {
            System.out.println("\nModule: " + module.getName());
            System.out.println("  Base Package: " + module.getBasePackage());
            System.out.println("  Display Name: " + module.getDisplayName());
        });
    }
}
