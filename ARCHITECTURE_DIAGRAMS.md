# Spring PetClinic Modulith - 架構圖表

本文件包含 Spring PetClinic Modulith 應用程式的詳細架構圖表、循序圖、資料庫 Schema 和 ER-Diagram。

## 目錄

- [系統架構圖](#系統架構圖)
- [循序圖](#循序圖)
  - [建立就診記錄](#建立就診記錄)
  - [異常處理流程](#異常處理流程)
- [資料庫 Schema](#資料庫-schema)
- [ER-Diagram](#er-diagram)

---

## 系統架構圖

整體系統架構展示了各個模組之間的關係以及與外部服務的整合:

```mermaid
graph TB
    subgraph "Spring PetClinic Modulith Application"
        subgraph "Web Layer"
            UI[AngularJS Frontend]
            API[REST API Gateway]
        end

        subgraph "Application Modules"
            CUSTOMERS[Customers Module<br/>客戶&寵物管理]
            VETS[Vets Module<br/>獸醫管理]
            VISITS[Visits Module<br/>就診管理]
            GENAI[GenAI Module<br/>AI 聊天機器人]
        end

        subgraph "Shared Infrastructure"
            EXCEPTIONS[Exception Handler<br/>統一異常處理]
            EVENTS[Event Store<br/>事件儲存]
            METRICS[Metrics<br/>監控指標]
        end

        subgraph "Data Layer"
            DB[(MySQL/HSQLDB<br/>關聯式資料庫)]
            VECTOR[(Vector Store<br/>向量資料庫)]
        end

        subgraph "External Services"
            OPENAI[OpenAI API<br/>LLM 服務]
        end
    end

    subgraph "Observability Stack"
        ZIPKIN[Zipkin<br/>分散式追蹤]
        PROMETHEUS[Prometheus<br/>指標收集]
        GRAFANA[Grafana<br/>視覺化]
    end

    UI --> API
    API --> CUSTOMERS
    API --> VETS
    API --> VISITS
    API --> GENAI

    CUSTOMERS --> DB
    VETS --> DB
    VISITS --> DB
    VISITS -.同步呼叫.-> CUSTOMERS
    VISITS -.同步呼叫.-> VETS

    CUSTOMERS -.發布事件.-> EVENTS
    VETS -.發布事件.-> EVENTS
    EVENTS -.訂閱事件.-> GENAI

    GENAI --> VECTOR
    GENAI --> OPENAI

    CUSTOMERS --> EXCEPTIONS
    VETS --> EXCEPTIONS
    VISITS --> EXCEPTIONS
    GENAI --> EXCEPTIONS

    CUSTOMERS --> METRICS
    VETS --> METRICS
    VISITS --> METRICS
    GENAI --> METRICS

    METRICS --> PROMETHEUS
    API --> ZIPKIN
    PROMETHEUS --> GRAFANA

    style CUSTOMERS fill:#e1f5ff
    style VETS fill:#e1f5ff
    style VISITS fill:#e1f5ff
    style GENAI fill:#e1f5ff
    style EXCEPTIONS fill:#fff3cd
    style EVENTS fill:#fff3cd
    style METRICS fill:#fff3cd
```

### 架構說明

**Web Layer (網頁層)**:
- **AngularJS Frontend**: 使用者介面
- **REST API Gateway**: 統一的 API 入口點

**Application Modules (應用程式模組)**:
- **Customers Module**: 管理客戶和寵物資訊
- **Vets Module**: 管理獸醫資訊和專長
- **Visits Module**: 管理就診記錄
- **GenAI Module**: AI 聊天機器人功能

**Shared Infrastructure (共享基礎設施)**:
- **Exception Handler**: 統一的錯誤處理機制
- **Event Store**: Spring Modulith 事件儲存
- **Metrics**: Micrometer 監控指標

**Data Layer (資料層)**:
- **MySQL/HSQLDB**: 主要關聯式資料庫
- **Vector Store**: AI 向量資料儲存

**External Services (外部服務)**:
- **OpenAI API**: 大型語言模型服務

**Observability Stack (可觀測性堆疊)**:
- **Zipkin**: 分散式追蹤
- **Prometheus**: 指標收集
- **Grafana**: 視覺化儀表板

---

## 循序圖

### 建立就診記錄

展示當客戶端建立新的就診記錄時,系統各個組件之間的互動流程:

```mermaid
sequenceDiagram
    participant Client as 客戶端
    participant API as VisitResource
    participant VisitSvc as VisitService
    participant CustomerSvc as CustomerService
    participant VetSvc as VetService
    participant DB as 資料庫
    participant Events as Event Store
    participant GenAI as GenAI Module

    Client->>API: POST /visits
    activate API

    API->>VisitSvc: scheduleVisit(visit)
    activate VisitSvc

    Note over VisitSvc: 驗證資料

    VisitSvc->>CustomerSvc: findById(customerId)
    activate CustomerSvc
    CustomerSvc->>DB: SELECT FROM owners
    DB-->>CustomerSvc: Owner data
    CustomerSvc-->>VisitSvc: Customer
    deactivate CustomerSvc

    VisitSvc->>VetSvc: findById(vetId)
    activate VetSvc
    VetSvc->>DB: SELECT FROM vets
    DB-->>VetSvc: Vet data
    VetSvc-->>VisitSvc: Vet
    deactivate VetSvc

    VisitSvc->>DB: INSERT INTO visits
    DB-->>VisitSvc: Visit created

    VisitSvc->>Events: publishEvent(VisitScheduled)
    activate Events
    Events->>DB: INSERT INTO event_publication
    Events-->>VisitSvc: Event stored
    deactivate Events

    VisitSvc-->>API: Visit
    deactivate VisitSvc

    API-->>Client: 201 Created
    deactivate API

    Note over Events,GenAI: 非同步事件處理

    Events->>GenAI: on(VisitScheduled)
    activate GenAI
    GenAI->>GenAI: updateVectorStore()
    GenAI-->>Events: Event processed
    deactivate GenAI

    Events->>DB: UPDATE event_publication<br/>SET completion_date
```

### 異常處理流程

展示系統如何處理不同類型的異常,並返回標準化的錯誤響應:

```mermaid
sequenceDiagram
    participant Client as 客戶端
    participant API as REST Controller
    participant Service as Service Layer
    participant Handler as RestExceptionHandler
    participant Client2 as 客戶端

    rect rgb(255, 240, 240)
        Note over Client,Client2: 情境 1: 資源不存在 (404)
        Client->>API: GET /owners/999
        activate API
        API->>Service: findById(999)
        activate Service
        Service-->>API: Optional.empty()
        deactivate Service
        API->>API: throw ResourceNotFoundException
        API->>Handler: handleResourceNotFound()
        activate Handler
        Handler->>Handler: log.debug("Resource not found")
        Handler-->>API: ErrorResponse(404, "Not Found", ...)
        deactivate Handler
        API-->>Client: 404 Not Found<br/>{"status":404,"error":"Not Found",...}
        deactivate API
    end

    rect rgb(240, 255, 240)
        Note over Client,Client2: 情境 2: 驗證錯誤 (400)
        Client->>API: POST /owners<br/>{"firstName":""}
        activate API
        API->>API: @Valid validation fails
        API->>Handler: handleMethodArgumentNotValid()
        activate Handler
        Handler->>Handler: collect field errors
        Handler->>Handler: log.debug("Validation error")
        Handler-->>API: ErrorResponse(400, "Validation Failed", ...)
        deactivate Handler
        API-->>Client: 400 Bad Request<br/>{"status":400,"error":"Validation Failed",...}
        deactivate API
    end

    rect rgb(255, 255, 240)
        Note over Client,Client2: 情境 3: 伺服器錯誤 (500)
        Client->>API: GET /vets
        activate API
        API->>Service: findAll()
        activate Service
        Service->>Service: throw RuntimeException
        Service-->>API: RuntimeException
        deactivate Service
        API->>Handler: handleGenericException()
        activate Handler
        Handler->>Handler: log.error("Unexpected error", ex)
        Handler-->>API: ErrorResponse(500, "Internal Server Error", ...)
        deactivate Handler
        API-->>Client: 500 Internal Server Error<br/>{"status":500,"error":"Internal Server Error",...}
        deactivate API
    end
```

---

## 資料庫 Schema

### 完整 MySQL Schema

```sql
-- Spring PetClinic Modulith - Unified MySQL Database Schema
-- Contains all tables from: Customers, Vets, Visits, and Modulith Event Store

CREATE DATABASE IF NOT EXISTS petclinic;

USE petclinic;

-- ==========================================
-- Customers Module Tables
-- ==========================================

CREATE TABLE IF NOT EXISTS types (
  id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(80),
  INDEX(name)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS owners (
  id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  first_name VARCHAR(30),
  last_name VARCHAR(30),
  address VARCHAR(255),
  city VARCHAR(80),
  telephone VARCHAR(20),
  INDEX(last_name)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS pets (
  id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(30),
  birth_date DATE,
  type_id INT(4) UNSIGNED NOT NULL,
  owner_id INT(4) UNSIGNED NOT NULL,
  INDEX(name),
  FOREIGN KEY (owner_id) REFERENCES owners(id),
  FOREIGN KEY (type_id) REFERENCES types(id)
) engine=InnoDB;

-- ==========================================
-- Vets Module Tables
-- ==========================================

CREATE TABLE IF NOT EXISTS vets (
  id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  first_name VARCHAR(30),
  last_name VARCHAR(30),
  INDEX(last_name)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS specialties (
  id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(80),
  INDEX(name)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS vet_specialties (
  vet_id INT(4) UNSIGNED NOT NULL,
  specialty_id INT(4) UNSIGNED NOT NULL,
  FOREIGN KEY (vet_id) REFERENCES vets(id),
  FOREIGN KEY (specialty_id) REFERENCES specialties(id),
  UNIQUE (vet_id,specialty_id)
) engine=InnoDB;

-- ==========================================
-- Visits Module Tables
-- ==========================================

CREATE TABLE IF NOT EXISTS visits (
    id INT(4) UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
    pet_id INT(4) UNSIGNED NOT NULL,
    vet_id INT(4) UNSIGNED NOT NULL,
    visit_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description VARCHAR(8192),
    status VARCHAR(20) DEFAULT 'SCHEDULED',
    FOREIGN KEY (pet_id) REFERENCES pets(id),
    FOREIGN KEY (vet_id) REFERENCES vets(id),
    INDEX idx_visits_pet_id (pet_id),
    INDEX idx_visits_vet_id (vet_id),
    INDEX idx_visits_status (status)
) engine=InnoDB;

-- ==========================================
-- Spring Modulith Event Store
-- ==========================================

CREATE TABLE IF NOT EXISTS event_publication (
  id CHAR(36) NOT NULL PRIMARY KEY COMMENT 'UUID',
  event_type VARCHAR(255) NOT NULL COMMENT 'Type of the published domain event',
  serialized_event LONGBLOB NOT NULL COMMENT 'The serialized form of the domain event',
  listener_id VARCHAR(255) NOT NULL COMMENT 'The listener id or listener class name',
  publication_date TIMESTAMP NOT NULL COMMENT 'When the event was published',
  completion_date TIMESTAMP NULL COMMENT 'When the publication was completed',
  UNIQUE KEY event_type_listener_id (event_type, listener_id, publication_date),
  KEY idx_completion_date (completion_date),
  KEY idx_event_type (event_type)
) engine=InnoDB;
```

### 表說明

| 表名 | 模組 | 說明 |
|-----|------|------|
| **types** | Customers | 寵物類型 (貓、狗等) |
| **owners** | Customers | 寵物主人/客戶資訊 |
| **pets** | Customers | 寵物資訊 |
| **vets** | Vets | 獸醫資訊 |
| **specialties** | Vets | 獸醫專長 (外科、牙科等) |
| **vet_specialties** | Vets | 獸醫與專長的關聯 (多對多) |
| **visits** | Visits | 就診記錄 |
| **event_publication** | Spring Modulith | 領域事件發布記錄 |

---

## ER-Diagram

實體關係圖展示資料庫表之間的關係:

```mermaid
erDiagram
    %% Customers Module
    OWNERS ||--o{ PETS : "擁有"
    TYPES ||--o{ PETS : "分類"

    %% Vets Module
    VETS ||--o{ VET_SPECIALTIES : "具備"
    SPECIALTIES ||--o{ VET_SPECIALTIES : "屬於"

    %% Visits Module
    PETS ||--o{ VISITS : "預約"
    VETS ||--o{ VISITS : "診療"

    %% Customers Module Tables
    OWNERS {
        int id PK "主鍵"
        varchar first_name "名字"
        varchar last_name "姓氏"
        varchar address "地址"
        varchar city "城市"
        varchar telephone "電話"
    }

    TYPES {
        int id PK "主鍵"
        varchar name "類型名稱 (貓/狗等)"
    }

    PETS {
        int id PK "主鍵"
        varchar name "寵物名稱"
        date birth_date "出生日期"
        int type_id FK "寵物類型 ID"
        int owner_id FK "主人 ID"
    }

    %% Vets Module Tables
    VETS {
        int id PK "主鍵"
        varchar first_name "名字"
        varchar last_name "姓氏"
    }

    SPECIALTIES {
        int id PK "主鍵"
        varchar name "專長名稱"
    }

    VET_SPECIALTIES {
        int vet_id FK "獸醫 ID"
        int specialty_id FK "專長 ID"
    }

    %% Visits Module Tables
    VISITS {
        int id PK "主鍵"
        int pet_id FK "寵物 ID"
        int vet_id FK "獸醫 ID"
        timestamp visit_date "就診日期"
        varchar description "診療描述"
        varchar status "狀態 (SCHEDULED/COMPLETED)"
    }
```

### 關係說明

1. **Owners ↔ Pets**: 一對多關係
   - 一個客戶可以擁有多隻寵物
   - 每隻寵物屬於一個客戶

2. **Types ↔ Pets**: 一對多關係
   - 一種寵物類型可以有多隻寵物
   - 每隻寵物屬於一種類型

3. **Vets ↔ Specialties**: 多對多關係 (透過 vet_specialties)
   - 一個獸醫可以有多個專長
   - 一個專長可以被多個獸醫擁有

4. **Pets ↔ Visits**: 一對多關係
   - 一隻寵物可以有多次就診記錄
   - 每次就診屬於一隻寵物

5. **Vets ↔ Visits**: 一對多關係
   - 一個獸醫可以有多次診療記錄
   - 每次就診由一個獸醫負責

---

## 模組邊界圖

```mermaid
graph LR
    subgraph "Customers Module"
        CO[Customer Entity]
        PE[Pet Entity]
        TY[Type Entity]
        CS[CustomerService]
    end

    subgraph "Vets Module"
        VE[Vet Entity]
        SP[Specialty Entity]
        VS[VetService]
    end

    subgraph "Visits Module"
        VI[Visit Entity]
        VIS[VisitService]
    end

    subgraph "GenAI Module"
        CH[ChatService]
        AI[AIDataProvider]
    end

    %% 同步呼叫 (直接依賴)
    VIS -->|同步| CS
    VIS -->|同步| VS

    %% 非同步呼叫 (事件)
    CS -.事件.-> CH
    VS -.事件.-> CH
    VIS -.事件.-> CH

    style CO fill:#e1f5ff
    style PE fill:#e1f5ff
    style TY fill:#e1f5ff
    style CS fill:#b3e0ff

    style VE fill:#ffe1e1
    style SP fill:#ffe1e1
    style VS fill:#ffb3b3

    style VI fill:#e1ffe1
    style VIS fill:#b3ffb3

    style CH fill:#fff3e1
    style AI fill:#ffd9b3
```

### 模組邊界規則

1. **允許的通訊方式**:
   - ✅ 模組可以呼叫其他模組的公開介面 (public API)
   - ✅ 模組可以透過事件進行非同步通訊
   - ❌ 模組不可存取其他模組的 `internal/` 套件
   - ❌ 模組之間不可有循環依賴

2. **編譯時驗證**:
   - Spring Modulith 會在編譯時檢查模組邊界
   - `ModulithStructureTest` 會驗證架構規則

3. **執行時監控**:
   - 模組間的呼叫會被追蹤
   - 事件發布和處理會被記錄

---

## 事件流程圖

```mermaid
flowchart TD
    Start([開始]) --> CreateEntity{建立實體}

    CreateEntity -->|Customer| PubCustomerEvent[發布 CustomerCreated 事件]
    CreateEntity -->|Vet| PubVetEvent[發布 VetCreated 事件]
    CreateEntity -->|Visit| PubVisitEvent[發布 VisitScheduled 事件]

    PubCustomerEvent --> StoreEvent[(儲存至 event_publication)]
    PubVetEvent --> StoreEvent
    PubVisitEvent --> StoreEvent

    StoreEvent --> AsyncProcess[非同步事件處理]

    AsyncProcess --> GenAIListener[GenAI Module 監聽]

    GenAIListener --> UpdateVector[更新 Vector Store]

    UpdateVector --> MarkComplete[標記事件為已完成]

    MarkComplete --> End([結束])

    style Start fill:#90EE90
    style End fill:#FFB6C1
    style StoreEvent fill:#FFE4B5
    style GenAIListener fill:#E0FFFF
    style UpdateVector fill:#DDA0DD
```

---

## 附錄

### 相關文件

- **[README.md](../README.md)** - 專案主要文件
- **[CLAUDE.md](../CLAUDE.md)** - Claude Code 使用指南
- **[ARCHITECTURE_DECISIONS.md](../spring-petclinic-modulith/ARCHITECTURE_DECISIONS.md)** - 架構決策記錄
- **[DEVELOPER_GUIDE.md](../spring-petclinic-modulith/DEVELOPER_GUIDE.md)** - 開發者指南

### 技術參考

- [Spring Modulith 官方文件](https://spring.io/projects/spring-modulith)
- [Spring AI 官方文件](https://spring.io/projects/spring-ai)
- [Mermaid 語法參考](https://mermaid.js.org/)

---

**最後更新**: 2025-11-22
**版本**: 3.4.1
