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

@org.springframework.modulith.ApplicationModule(
    displayName = "Shared",
    allowedDependencies = {},
    type = org.springframework.modulith.ApplicationModule.Type.OPEN
)
/**
 * Shared module providing common functionality across all PetClinic modules.
 *
 * This module contains:
 * - Common exceptions (public API)
 * - Shared configuration
 * - Shared utilities
 *
 * All other modules can depend on this shared module.
 * This module is marked as OPEN, meaning all its packages are public API.
 *
 * @author PetClinic Team
 */
package org.springframework.samples.petclinic.shared;
