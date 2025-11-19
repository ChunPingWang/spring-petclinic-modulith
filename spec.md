# Spring PetClinic Modulith 重構規範與工作清單

## 📋 專案概述

本文件定義將 Spring PetClinic Microservices 重構為 Spring Modulith 架構的完整規範與執行計畫。

### 目標
將現有的微服務架構轉換為基於 Spring Modulith 的模組化單體應用（Modular Monolith），以：
- **簡化部署**：從 8 個獨立服務簡化為單一應用
- **降低複雜度**：移除服務發現、配置中心、API Gateway 等分散式系統組件
- **保持模組化**：使用 Spring Modulith 確保模組邊界清晰
- **提升開發效率**：簡化本地開發環境設置
- **保留可觀測性**：維持監控、追蹤、指標等功能

---

## 🎯 Spring Modulith 核心概念

### 什麼是 Spring Modulith？

Spring Modulith 是一個框架，幫助開發者建構**模組化單體應用**（Modular Monolith），提供：

1. **模組邊界驗證** - 在編譯時檢查模組間的依賴關係
2. **應用模組結構** - 基於套件結構自動識別應用模組
3. **事件驅動架構** - 支援模組間的鬆耦合通訊
4. **文件生成** - 自動生成模組關係文檔
5. **可觀測性** - 內建對模組互動的追蹤支援

### 模組化原則

#### 模組結構
```
org.springframework.samples.petclinic/
├── customers/           # Customers 模組 (核心領域模組)
│   ├── internal/        # 內部實作（不可被外部訪問）
│   ├── Customer.java    # 公開 API
│   └── CustomerCreated.java  # 領域事件
├── vets/               # Vets 模組
│   ├── internal/
│   └── Vet.java
├── visits/             # Visits 模組
│   ├── internal/
│   └── Visit.java
├── genai/              # GenAI 模組
│   ├── internal/
│   └── ChatService.java
└── Application.java    # 主應用程式
```

#### 模組通訊規則

1. **直接調用** (同步)
   - 只能訪問其他模組的公開 API（非 `internal` 套件）
   - 使用 Spring 依賴注入

2. **事件發布/訂閱** (異步)
   - 使用 `ApplicationEventPublisher` 發布領域事件
   - 使用 `@ApplicationModuleListener` 監聽事件
   - 事件存儲在資料庫中以支援重試和追蹤

3. **禁止的模式**
   - ❌ 訪問其他模組的 `internal` 套件
   - ❌ 循環依賴
   - ❌ 直接訪問其他模組的資料庫表

---

## 🏗️ 目標架構

### 現有微服務架構
```
┌─────────────────┐
│  API Gateway    │ :8080
└────────┬────────┘
         │
    ┌────┴─────┬──────────┬─────────┐
    │          │          │         │
┌───▼───┐  ┌──▼───┐  ┌──▼────┐  ┌─▼─────┐
│Customers│ │ Vets │  │Visits │  │GenAI  │
│  :8081  │ │:8083 │  │ :8082 │  │ :8084 │
└────┬────┘ └──┬───┘  └───┬───┘  └───┬───┘
     │         │          │          │
┌────▼─────────▼──────────▼──────────▼────┐
│          HSQLDB / MySQL                  │
└──────────────────────────────────────────┘

支援服務：
- Config Server (8888)
- Discovery Server (8761)
- Admin Server (9090)
- Zipkin (9411)
- Prometheus (9091)
- Grafana (3000)
```

### 目標 Modulith 架構
```
┌─────────────────────────────────────────┐
│     Spring PetClinic Modulith :8080     │
│                                         │
│  ┌──────────┐  ┌──────┐  ┌──────────┐ │
│  │Customers │  │ Vets │  │ Visits   │ │
│  │  Module  │  │Module│  │  Module  │ │
│  └─────┬────┘  └──┬───┘  └────┬─────┘ │
│        │          │           │        │
│        └──────────┼───────────┘        │
│                   │                    │
│            ┌──────▼──────┐            │
│            │ GenAI Module│            │
│            └─────────────┘            │
│                                        │
│  ┌─────────────────────────────────┐  │
│  │   Web Layer (API Endpoints)     │  │
│  └─────────────────────────────────┘  │
└──────────────┬──────────────────────┘
               │
        ┌──────▼──────┐
        │   Database  │
        └─────────────┘

保留的監控服務（可選）：
- Zipkin (9411)
- Prometheus (9091)
- Grafana (3000)
```

---

## 📦 模組劃分策略

### 核心領域模組

#### 1. Customers Module
- **職責**：管理客戶（Owners）和寵物（Pets）資訊
- **公開 API**：
  - `Customer` (實體)
  - `CustomerService` (服務介面)
  - `CustomerCreated`, `PetAdded` (領域事件)
- **內部實作**：
  - `CustomerRepository`
  - `PetRepository`
  - `OwnerResource` (REST 控制器)
  - `PetResource` (REST 控制器)

#### 2. Vets Module
- **職責**：管理獸醫資訊和專長
- **公開 API**：
  - `Vet` (實體)
  - `VetService` (服務介面)
- **內部實作**：
  - `VetRepository`
  - `SpecialtyRepository`
  - `VetResource` (REST 控制器)

#### 3. Visits Module
- **職責**：管理寵物就診記錄
- **公開 API**：
  - `Visit` (實體)
  - `VisitService` (服務介面)
  - `VisitCreated` (領域事件)
- **內部實作**：
  - `VisitRepository`
  - `VisitResource` (REST 控制器)
- **依賴**：需要查詢 Customer 和 Vet 資訊

#### 4. GenAI Module
- **職責**：提供 AI 聊天機器人功能
- **公開 API**：
  - `ChatService` (服務介面)
- **內部實作**：
  - `PetclinicChatClient`
  - `AIDataProvider`
  - REST 控制器
- **依賴**：需要訪問所有其他模組的資料

### 共享模組

#### 5. Shared / Common Module
- **職責**：共享的基礎設施和工具類
- **內容**：
  - 異常處理 (`ResourceNotFoundException`)
  - 通用 DTO
  - 工具類
  - 基礎配置

---

## 🔧 技術規範

### Maven 依賴配置

#### 父 POM 添加 Spring Modulith
```xml
<properties>
    <spring-modulith.version>1.3.0</spring-modulith.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.modulith</groupId>
            <artifactId>spring-modulith-bom</artifactId>
            <version>${spring-modulith.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

#### 主應用依賴
```xml
<dependencies>
    <!-- Spring Modulith Core -->
    <dependency>
        <groupId>org.springframework.modulith</groupId>
        <artifactId>spring-modulith-starter-core</artifactId>
    </dependency>
    
    <!-- Spring Modulith Events (JPA) -->
    <dependency>
        <groupId>org.springframework.modulith</groupId>
        <artifactId>spring-modulith-starter-jpa</artifactId>
    </dependency>
    
    <!-- Spring Modulith Observability -->
    <dependency>
        <groupId>org.springframework.modulith</groupId>
        <artifactId>spring-modulith-observability</artifactId>
    </dependency>
    
    <!-- Spring Modulith Actuator -->
    <dependency>
        <groupId>org.springframework.modulith</groupId>
        <artifactId>spring-modulith-actuator</artifactId>
    </dependency>
    
    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.modulith</groupId>
        <artifactId>spring-modulith-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- Documentation -->
    <dependency>
        <groupId>org.springframework.modulith</groupId>
        <artifactId>spring-modulith-docs</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 套件結構規範

```
org.springframework.samples.petclinic/
├── PetClinicApplication.java          # 主應用程式
│
├── customers/                          # Customers 模組
│   ├── Customer.java                   # 公開實體
│   ├── CustomerService.java            # 公開服務介面
│   ├── CustomerCreated.java            # 公開事件
│   └── internal/                       # 內部實作
│       ├── CustomerServiceImpl.java
│       ├── CustomerRepository.java
│       ├── Pet.java
│       ├── PetRepository.java
│       ├── PetType.java
│       └── web/
│           ├── OwnerResource.java
│           └── PetResource.java
│
├── vets/                               # Vets 模組
│   ├── Vet.java                        # 公開實體
│   ├── VetService.java                 # 公開服務介面
│   └── internal/
│       ├── VetServiceImpl.java
│       ├── VetRepository.java
│       ├── Specialty.java
│       ├── SpecialtyRepository.java
│       └── web/
│           └── VetResource.java
│
├── visits/                             # Visits 模組
│   ├── Visit.java                      # 公開實體
│   ├── VisitService.java               # 公開服務介面
│   ├── VisitCreated.java               # 公開事件
│   └── internal/
│       ├── VisitServiceImpl.java
│       ├── VisitRepository.java
│       └── web/
│           └── VisitResource.java
│
├── genai/                              # GenAI 模組
│   ├── ChatService.java                # 公開服務介面
│   └── internal/
│       ├── ChatServiceImpl.java
│       ├── PetclinicChatClient.java
│       ├── AIDataProvider.java
│       └── VectorStoreController.java
│
├── shared/                             # 共享模組
│   ├── exceptions/
│   │   └── ResourceNotFoundException.java
│   ├── config/
│   │   └── MetricConfig.java
│   └── dto/
│       └── (共享 DTO)
│
└── web/                                # Web 層（前端靜態資源）
    ├── static/
    └── templates/
```

### 事件驅動架構

#### 領域事件定義
```java
// customers/CustomerCreated.java
package org.springframework.samples.petclinic.customers;

import org.springframework.modulith.events.Externalized;

@Externalized("petclinic.customer.created::#{id()}")
public record CustomerCreated(Integer id, String firstName, String lastName) {
}
```

#### 事件發布
```java
// customers/internal/CustomerServiceImpl.java
package org.springframework.samples.petclinic.customers.internal;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.samples.petclinic.customers.*;

@Service
class CustomerServiceImpl implements CustomerService {
    
    private final ApplicationEventPublisher events;
    
    @Override
    public Customer createCustomer(CustomerRequest request) {
        Customer customer = customerRepository.save(...);
        
        // 發布領域事件
        events.publishEvent(new CustomerCreated(
            customer.getId(),
            customer.getFirstName(),
            customer.getLastName()
        ));
        
        return customer;
    }
}
```

#### 事件監聽
```java
// genai/internal/AIDataProvider.java
package org.springframework.samples.petclinic.genai.internal;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.samples.petclinic.customers.CustomerCreated;

@Service
class AIDataProvider {
    
    @ApplicationModuleListener
    void on(CustomerCreated event) {
        // 更新 AI 向量資料庫
        log.info("Customer created: {} {}", 
            event.firstName(), event.lastName());
        // 異步處理邏輯
    }
}
```

### 模組測試規範

#### 模組結構測試
```java
package org.springframework.samples.petclinic;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModularityTests {
    
    ApplicationModules modules = ApplicationModules.of(PetClinicApplication.class);
    
    @Test
    void verifiesModularStructure() {
        // 驗證模組結構符合規範
        modules.verify();
    }
    
    @Test
    void createModuleDocumentation() {
        // 生成模組文檔
        new Documenter(modules)
            .writeDocumentation()
            .writeIndividualModulesAsPlantUml()
            .writeModulesAsPlantUml();
    }
}
```

#### 模組集成測試
```java
package org.springframework.samples.petclinic.customers;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.ApplicationModuleTest;

@ApplicationModuleTest
class CustomerModuleIntegrationTests {
    
    @Test
    void testCustomerCreation(Scenario scenario) {
        scenario.publish(new CreateCustomerCommand(...))
                .andWaitForEventOfType(CustomerCreated.class)
                .toArriveAndVerify(event -> {
                    assertThat(event.firstName()).isEqualTo("John");
                });
    }
}
```

### 配置規範

#### application.yml
```yaml
spring:
  application:
    name: petclinic-modulith
  
  # 資料庫配置
  datasource:
    url: jdbc:hsqldb:mem:petclinic
    username: sa
    password:
  
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: false
  
  # Spring Modulith 事件存儲
  modulith:
    events:
      jdbc:
        schema-initialization:
          enabled: true
    
  # Actuator 配置
  management:
    endpoints:
      web:
        exposure:
          include: health,info,metrics,prometheus,modulith
    endpoint:
      health:
        show-details: always
      modulith:
        enabled: true
    
    # 追蹤配置
    tracing:
      sampling:
        probability: 1.0
    zipkin:
      tracing:
        endpoint: http://localhost:9411/api/v2/spans
    
    # 指標配置
    metrics:
      export:
        prometheus:
          enabled: true
      distribution:
        percentiles-histogram:
          http.server.requests: true

server:
  port: 8080

# 日誌配置
logging:
  level:
    org.springframework.modulith: DEBUG
    org.springframework.samples.petclinic: DEBUG
```

---

## 📝 重構工作清單

### Phase 1: 環境準備與架構設計 ✅ COMPLETE

#### 1.1 文檔準備
- [x] 建立 `spec.md` 重構規範文檔
- [x] 更新 `.github/copilot-instructions.md` 加入 Spring Modulith 規範
- [x] 建立 `.modulith/module-design.md` 模組設計文檔
- [x] 建立 `.modulith/technical-assessment.md` 技術評估

#### 1.2 專案結構規劃
- [x] 設計最終套件結構
- [x] 定義模組邊界和公開 API
- [x] 設計模組間通訊機制（直接調用 vs 事件）
- [x] 規劃領域事件清單

#### 1.3 技術評估
- [x] 驗證 Spring Modulith 與 Spring Boot 3.4.1 相容性
- [x] 評估現有微服務間的依賴關係
- [x] 識別需要轉換為事件驅動的場景
- [x] 評估資料庫 Schema 合併策略

---

### Phase 2: 建立 Modulith 骨架 ✅ COMPLETE

#### 2.1 Maven 專案重構
- [x] 建立新的主模組 `spring-petclinic-modulith`
- [x] 更新父 POM 添加 Spring Modulith 依賴管理
- [x] 配置 Maven 編譯插件支援模組化結構
- [x] 移除微服務相關依賴（Eureka, Config Server, Gateway）

#### 2.2 主應用程式設置
- [x] 建立 `PetClinicApplication.java` 主類
- [x] 配置 `application.yml`
- [x] 設置 Spring Modulith 事件存儲（JDBC）
- [x] 配置 Actuator 端點

#### 2.3 共享模組建立
- [x] 建立 `shared` 套件
- [x] 遷移共享異常類別
- [x] 遷移共享配置類別
- [x] 建立共享 DTO 和工具類

---

### Phase 3: 模組遷移 - Customers ✅ COMPLETE

#### 3.1 套件結構建立
- [x] 建立 `customers` 套件和 `internal` 子套件
- [x] 定義公開 API 介面
- [x] 定義領域事件

#### 3.2 實體和 Repository 遷移
- [x] 遷移 `Owner` 實體 → `Customer`
- [x] 遷移 `Pet` 實體到 `internal`
- [x] 遷移 `PetType` 實體到 `internal`
- [x] 遷移 `OwnerRepository` → `CustomerRepository`
- [x] 遷移 `PetRepository` 到 `internal`

#### 3.3 服務層建立
- [x] 建立 `CustomerService` 公開介面
- [x] 實作 `CustomerServiceImpl` 在 `internal`
- [x] 整合事件發布機制
- [x] 更新業務邏輯

#### 3.4 Web 層遷移
- [x] 遷移 `OwnerResource` 到 `internal/web`
- [x] 遷移 `PetResource` 到 `internal/web`
- [x] 更新 REST 端點路徑
- [x] 調整請求/回應 DTO

#### 3.5 Domain Events
- [x] 建立 `CustomerCreated` 事件
- [x] 建立 `CustomerUpdated` 事件
- [x] 建立 `PetAdded` 事件
- [x] 整合事件發布

#### 3.6 資料庫遷移
- [x] 建立 HSQLDB schema.sql 和 data.sql
- [x] 建立 MySQL schema.sql 和 data.sql
- [x] 整合 10 個範例客戶和 13 個寵物

#### 3.7 測試遷移
- [x] 建立 `OwnerResourceTest` 單元測試
- [x] 遷移 `PetResourceTest` 單元測試
- [x] 建立 `CustomerServiceImplTest` 服務層測試
- [x] 建立 `CustomersModuleIntegrationTest`
- [x] 建立 `CustomersModuleEventsTest`

#### 3.8 完成驗證
- [x] 模組結構驗證
- [x] 事件發布驗證
- [x] 公開 API 驗證
- [x] 內部實現隔離驗證

---

### Phase 4: 模組遷移 - Vets ✅ COMPLETE (Core)

#### 4.1 套件結構建立
- [x] 建立 `vets` 套件和 `internal` 子套件
- [x] 定義公開 API 介面
- [x] 定義領域事件

#### 4.2 實體和 Repository 遷移
- [x] 遷移 `Vet` 實體
- [x] 遷移 `Specialty` 實體到 `internal`
- [x] 遷移 `VetRepository`
- [x] 遷移 `SpecialtyRepository` 到 `internal`

#### 4.3 服務層建立
- [x] 建立 `VetService` 公開介面
- [x] 實作 `VetServiceImpl` 在 `internal`
- [x] 整合必要的事件機制
- [x] 整合 `@Cacheable("vets")` 快取

#### 4.4 Web 層遷移
- [x] 遷移 `VetResource` 到 `internal/web`
- [x] 更新 REST 端點
- [x] 調整 DTO

#### 4.5 Domain Events
- [x] 建立 `VetCreated` 事件
- [x] 建立 `VetUpdated` 事件
- [x] 建立 `SpecialtyAdded` 事件

#### 4.6 資料庫遷移
- [x] 建立 HSQLDB schema 和 data
- [x] 建立 MySQL schema 和 data
- [x] 整合 6 個範例獸醫和 3 個專長

#### 4.7 測試遷移 ✅ COMPLETE
- [x] 建立 `VetResourceTest` 單元測試
- [x] 建立 `VetServiceImplTest` 服務層測試
- [x] 建立 `VetsModuleIntegrationTest`
- [x] 建立 `VetsModuleEventsTest`

---

### Phase 5: 模組遷移 - Visits ✅ COMPLETE

#### 5.1 套件結構建立
- [x] 建立 `visits` 套件和 `internal` 子套件
- [x] 定義公開 API 介面
- [x] 定義領域事件 (`VisitCreated`, `VisitCompleted`)

#### 5.2 實體和 Repository 遷移
- [x] 遷移 `Visit` 實體
- [x] 遷移 `VisitRepository`
- [x] 更新與 Customer/Vet 的關聯

#### 5.3 服務層建立
- [x] 建立 `VisitService` 公開介面
- [x] 實作 `VisitServiceImpl` 在 `internal`
- [x] 整合 `CustomerService` 和 `VetService` 依賴
- [x] 實作事件發布

#### 5.4 Web 層遷移
- [x] 遷移 `VisitResource` 到 `internal/web`
- [x] 更新 REST 端點
- [x] 調整 DTO

#### 5.5 資料庫遷移
- [x] 建立 HSQLDB schema 和 data
- [x] 建立 MySQL schema 和 data
- [x] 整合 7 個範例訪問

#### 5.6-5.9 測試遷移 ✅ COMPLETE
- [x] 建立 `VisitResourceTest` REST 層測試
- [x] 建立 `VisitServiceImplTest` 服務層測試
- [x] 建立 `VisitsModuleIntegrationTest` 模組結構測試
- [x] 建立 `VisitsModuleEventsTest` 事件發布測試

---

### Phase 6: 模組遷移 - GenAI 📦

#### 6.1 套件結構建立
- [ ] 建立 `genai` 套件和 `internal` 子套件
- [ ] 定義公開 API 介面
- [ ] 定義事件監聽器

#### 6.2 服務遷移
- [ ] 遷移 `PetclinicChatClient` 到 `internal`
- [ ] 遷移 `AIDataProvider` 到 `internal`
- [ ] 遷移 `VectorStoreController` 到 `internal`

#### 6.3 跨模組整合
- [ ] 實作對 Customers 模組的依賴
- [ ] 實作對 Vets 模組的依賴
- [ ] 實作對 Visits 模組的依賴
- [ ] 設置事件監聽器

#### 6.4 Web 層遷移
- [ ] 遷移 REST 控制器
- [ ] 更新 API 端點
- [ ] 配置 OpenAI/Azure OpenAI

#### 6.5 測試遷移
- [ ] 遷移測試
- [ ] 驗證跨模組通訊
- [ ] 測試事件處理

---

### Phase 7: Web 層整合 🌐

#### 7.1 靜態資源遷移
- [ ] 遷移 API Gateway 的靜態資源（AngularJS）
- [ ] 遷移 CSS/SCSS 檔案
- [ ] 遷移 JavaScript 檔案
- [ ] 遷移圖片資源

#### 7.2 前端路由配置
- [ ] 配置 Spring MVC 路由
- [ ] 更新前端 API 呼叫路徑
- [ ] 移除 API Gateway 相關配置

#### 7.3 CORS 和安全配置
- [ ] 配置 CORS（如需要）
- [ ] 配置基本安全設置
- [ ] 更新 Actuator 端點訪問控制

---

### Phase 8: 資料庫整合 🗄️

#### 8.1 Schema 整合
- [ ] 合併各微服務的資料庫 Schema
- [ ] 建立統一的 `schema.sql`
- [ ] 建立統一的 `data.sql`（測試資料）
- [ ] 配置 Flyway/Liquibase（可選）

#### 8.2 事件存儲配置
- [ ] 建立 Spring Modulith 事件表
- [ ] 配置事件發布/訂閱機制
- [ ] 設置事件清理策略

#### 8.3 MySQL 支援
- [ ] 更新 MySQL Schema
- [ ] 測試 MySQL Profile
- [ ] 更新文檔

---

### Phase 9: 監控與可觀測性 📊

#### 9.1 Spring Modulith Actuator
- [ ] 啟用 `/actuator/modulith` 端點
- [ ] 配置模組健康檢查
- [ ] 設置模組指標收集

#### 9.2 Micrometer 整合
- [ ] 配置 Micrometer 追蹤
- [ ] 更新自定義指標 (`@Timed`)
- [ ] 整合 Prometheus

#### 9.3 分散式追蹤
- [ ] 配置 OpenTelemetry
- [ ] 整合 Zipkin
- [ ] 驗證跨模組追蹤

#### 9.4 Grafana Dashboard
- [ ] 更新 Grafana Dashboard
- [ ] 建立 Modulith 專用面板
- [ ] 配置告警規則

---

### Phase 10: 測試與驗證 ✅

#### 10.1 模組結構測試
- [ ] 執行 `ApplicationModules.verify()`
- [ ] 生成模組文檔
- [ ] 驗證模組邊界
- [ ] 檢查循環依賴

#### 10.2 集成測試
- [ ] 測試所有 REST API
- [ ] 測試跨模組通訊
- [ ] 測試事件發布/訂閱
- [ ] 測試資料庫操作

#### 10.3 效能測試
- [ ] 執行負載測試
- [ ] 比較與微服務版本的效能
- [ ] 優化瓶頸

#### 10.4 端到端測試
- [ ] 測試完整業務流程
- [ ] 測試前端功能
- [ ] 測試 GenAI 聊天機器人

---

### Phase 11: 文檔與部署 📚

#### 11.1 技術文檔
- [ ] 更新 README.md
- [ ] 建立架構文檔
- [ ] 建立開發指南
- [ ] 建立部署指南

#### 11.2 API 文檔
- [ ] 更新 API 文檔
- [ ] 生成 OpenAPI/Swagger 文檔
- [ ] 建立模組 API 參考

#### 11.3 Docker 配置
- [ ] 建立新的 Dockerfile
- [ ] 更新 Docker Compose（簡化版）
- [ ] 移除不需要的服務容器
- [ ] 測試容器化部署

#### 11.4 CI/CD 更新
- [ ] 更新 GitHub Actions 工作流程
- [ ] 簡化建置流程
- [ ] 更新部署腳本

---

### Phase 12: 清理與優化 🧹

#### 12.1 程式碼清理
- [ ] 刪除舊的微服務目錄
- [ ] 移除未使用的依賴
- [ ] 清理配置檔案
- [ ] 統一程式碼風格

#### 12.2 依賴優化
- [ ] 移除 Spring Cloud 依賴
- [ ] 移除 Eureka 依賴
- [ ] 移除 Config Server 依賴
- [ ] 更新依賴版本

#### 12.3 效能優化
- [ ] 優化資料庫查詢
- [ ] 優化事件處理
- [ ] 配置快取策略
- [ ] 調整 JVM 參數

#### 12.4 安全加固
- [ ] 審查安全配置
- [ ] 更新依賴以修復漏洞
- [ ] 配置安全標頭
- [ ] 審查資料訪問權限

---

## 🔍 關鍵決策點

### 1. 模組通訊模式選擇

| 場景 | 建議模式 | 理由 |
|------|---------|------|
| Visits 查詢 Customer 資訊 | 直接調用 | 強一致性需求，需要即時資料 |
| Customer 創建後通知 GenAI | 事件發布 | 鬆耦合，GenAI 可異步處理 |
| Visit 創建後記錄日誌 | 事件發布 | 橫切關注點，避免業務邏輯耦合 |

### 2. 資料庫策略

**選項 A：單一資料庫，共享 Schema** ✅ 推薦
- ✅ 簡化部署和管理
- ✅ 支援跨模組事務
- ✅ 降低複雜度
- ⚠️ 需要謹慎管理模組間的資料訪問

**選項 B：單一資料庫，獨立 Schema**
- ✅ 更強的模組隔離
- ❌ 跨模組查詢複雜
- ❌ 事務管理困難

### 3. 事件存儲策略

**使用 Spring Modulith JPA Event Publication**
- ✅ 持久化事件，支援重試
- ✅ 保證最終一致性
- ✅ 內建事件清理機制
- ⚠️ 需要額外的資料庫表

---

## 📊 成功指標

### 功能完整性
- [ ] 所有現有功能正常運作
- [ ] 所有 REST API 回應正確
- [ ] 前端功能完全可用
- [ ] GenAI 聊天機器人正常工作

### 架構品質
- [ ] 模組結構驗證通過（`ApplicationModules.verify()`）
- [ ] 無循環依賴
- [ ] 模組邊界清晰
- [ ] 事件驅動架構運作正常

### 效能指標
- [ ] 啟動時間 < 30 秒
- [ ] API 回應時間與微服務版本相當
- [ ] 記憶體使用 < 1GB
- [ ] CPU 使用率正常

### 可維護性
- [ ] 程式碼覆蓋率 > 70%
- [ ] 所有測試通過
- [ ] 文檔完整
- [ ] 程式碼風格一致

---

## 🚀 下一步行動

### 待 Review 完成後開始執行：

1. **Phase 1 - 環境準備**（預計 1-2 天）
   - 完成文檔編寫
   - 確認技術細節
   - 建立專案骨架

2. **Phase 2 - 建立骨架**（預計 2-3 天）
   - 設置主應用程式
   - 配置基礎設施
   - 建立共享模組

3. **Phase 3-6 - 模組遷移**（預計 10-15 天）
   - 逐一遷移各個模組
   - 建立測試
   - 驗證功能

4. **Phase 7-12 - 整合與優化**（預計 5-10 天）
   - 完成整體整合
   - 效能優化
   - 文檔完善

**總預估時間：3-4 週**

---

## 📚 參考資源

### Spring Modulith 官方資源
- [Spring Modulith 官方文檔](https://docs.spring.io/spring-modulith/reference/)
- [Spring Modulith Reference Guide](https://docs.spring.io/spring-modulith/docs/current/reference/html/)
- [Spring Modulith GitHub](https://github.com/spring-projects/spring-modulith)

### 範例專案
- [Spring Modulith Examples](https://github.com/spring-projects/spring-modulith/tree/main/spring-modulith-examples)
- [Modular Monolith with Spring Modulith](https://github.com/maciejwalkowiak/spring-modulith-example)

### 相關文章
- [Modular Monolith Architecture](https://www.kamilgrzybek.com/blog/posts/modular-monolith-primer)
- [Spring Modulith - Building Better Monoliths](https://spring.io/blog/2022/10/21/introducing-spring-modulith)
- [Event-Driven Architecture with Spring Modulith](https://www.baeldung.com/spring-modulith)

---

## ✅ Review Checklist

重構開始前請確認：

- [ ] 已詳細閱讀本規範文檔
- [ ] 已理解 Spring Modulith 核心概念
- [ ] 已確認目標架構合理性
- [ ] 已評估工作量和時程
- [ ] 已準備好開發環境
- [ ] 已取得團隊共識
- [ ] 已規劃測試策略
- [ ] 已準備回滾方案

---

**版本**：v1.0
**建立日期**：2025-11-18
**狀態**：待 Review

**備註**：本文檔將根據實際重構進度持續更新。
