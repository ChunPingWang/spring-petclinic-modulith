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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulith;
import org.springframework.modulith.core.ApplicationModules;

/**
 * PetClinic Spring Boot Application - Modular Monolith with Spring Modulith
 * 
 * This application demonstrates a modular monolith architecture using Spring Modulith,
 * which provides:
 * - Module boundary verification
 * - Event-driven communication between modules
 * - Observability and documentation
 * 
 * @author Spring PetClinic Team
 */
@SpringBootApplication
@Modulith(
    systemName = "PetClinic",
    sharedModules = "shared"
)
public class PetClinicApplication {

    private static final Logger log = LoggerFactory.getLogger(PetClinicApplication.class);

    public static void main(String[] args) {
        // Verify module structure before starting the application
        verifyModuleStructure();
        
        SpringApplication.run(PetClinicApplication.class, args);
        
        log.info("===========================================");
        log.info("   PetClinic Modulith Application Started");
        log.info("   Access the application at: http://localhost:8080");
        log.info("===========================================");
    }
    
    /**
     * Verifies the module structure and boundaries.
     * This will fail fast if there are any violations of module boundaries.
     */
    private static void verifyModuleStructure() {
        log.info("Verifying module structure...");
        
        ApplicationModules modules = ApplicationModules.of(PetClinicApplication.class);
        
        // Verify module boundaries - will throw exception if violations are found
        modules.verify();
        
        log.info("✓ Module structure verification completed successfully");
        log.info("Detected modules:");
        modules.forEach(module -> 
            log.info("  - {} ({})", module.getName(), module.getBasePackage())
        );
    }
}
