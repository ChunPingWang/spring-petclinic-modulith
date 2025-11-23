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
package org.springframework.samples.petclinic.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for PetClinic API documentation.
 *
 * Provides interactive API documentation accessible at:
 * - Swagger UI: http://localhost:8080/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8080/v3/api-docs
 *
 * @author PetClinic Team
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI petClinicOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spring PetClinic Modulith API")
                        .description("""
                                PetClinic API 提供完整的寵物診所管理功能，包括：
                                - **客戶管理** (Customers): 寵物主人的資料管理
                                - **寵物管理** (Pets): 寵物的基本資料與類型管理
                                - **獸醫管理** (Vets): 獸醫師與專業領域管理
                                - **就診記錄** (Visits): 寵物就診的預約、完成與取消管理
                                - **AI 聊天** (GenAI): 智慧客服對話功能

                                本專案採用 Spring Modulith 架構，實現模組化單體應用程式。
                                """)
                        .version("3.4.1")
                        .contact(new Contact()
                                .name("PetClinic Team")
                                .url("https://github.com/spring-projects/spring-petclinic")
                                .email("petclinic@example.com"))
                        .license(new License()
                                .name("Apache License 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("開發環境"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("本地測試環境")
                ));
    }
}
