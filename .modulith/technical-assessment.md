# Spring PetClinic Modulith - 技術評估報告

## 📋 文檔狀態
- **版本**: 1.0
- **建立日期**: 2024
- **階段**: Phase 1.3 技術評估

---

## 🎯 評估目標

評估現有微服務架構遷移至 Spring Modulith 的技術可行性,識別風險和技術挑戰。

---

## 📊 現有技術棧分析

### 核心框架
| 技術 | 版本 | Modulith 相容性 | 遷移策略 |
|------|------|----------------|---------|
| Spring Boot | 3.4.1 | ✅ 完全相容 | 保持版本 |
| Java | 21 | ✅ 完全相容 | 保持版本 |
| Spring Data JPA | 3.4.1 | ✅ 完全相容 | 保持版本 |
| Micrometer | 1.14.x | ✅ 完全相容 | 保持版本 |
| OpenTelemetry | 最新 | ✅ 完全相容 | 保持版本 |

### 微服務相關依賴 (需移除)
| 技術 | 版本 | Modulith 替代方案 | 移除原因 |
|------|------|------------------|---------|
| Spring Cloud Gateway | 2024.0.0 | ❌ 移除 (單體應用無需) | 內部模組通訊 |
| Eureka Client | 2024.0.0 | ❌ 移除 (無服務發現) | 直接方法調用 |
| Config Server | 2024.0.0 | ❌ 移除 (統一配置) | `application.yml` |
| Spring Cloud Dependencies | 2024.0.0 | ❌ 完全移除 | 不需要 |

### Spring Modulith 新增依賴
| 依賴 | 版本 | 用途 |
|------|------|------|
| `spring-modulith-starter-core` | 1.3.0 | 模組化核心功能 |
| `spring-modulith-starter-jpa` | 1.3.0 | JPA 事件發布支援 |
| `spring-modulith-observability` | 1.3.0 | 可觀測性支援 |
| `spring-modulith-actuator` | 1.3.0 | Actuator 端點 |
| `spring-modulith-docs` | 1.3.0 | 自動產生文檔 |

---

## 🏗️ 專案結構變更

### 現有結構
```
spring-petclinic-microservices/
├── spring-petclinic-admin-server/      ❌ 移除
├── spring-petclinic-config-server/     ❌ 移除
├── spring-petclinic-discovery-server/  ❌ 移除
├── spring-petclinic-api-gateway/       ⚠️  部分保留 (前端)
├── spring-petclinic-customers-service/ → 遷移
├── spring-petclinic-vets-service/      → 遷移
├── spring-petclinic-visits-service/    → 遷移
└── spring-petclinic-genai-service/     → 遷移
```

### 目標結構
```
spring-petclinic-modulith/
├── pom.xml                          (單一 POM)
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── org/springframework/samples/petclinic/
│   │   │       ├── PetClinicApplication.java
│   │   │       ├── customers/
│   │   │       ├── vets/
│   │   │       ├── visits/
│   │   │       ├── genai/
│   │   │       └── shared/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-mysql.yml
│   │       ├── db/
│   │       │   ├── hsqldb/
│   │       │   └── mysql/
│   │       └── static/               (前端資源)
│   └── test/
│       └── java/                     (整合測試)
└── docker/                           (Docker 配置)
```

---

## 🗄️ 資料庫遷移評估

### 現況: 多個資料庫 Schema

#### Customers Service
```sql
-- Tables: owners, pets, types
CREATE TABLE owners (
  id INT PRIMARY KEY AUTO_INCREMENT,
  first_name VARCHAR(30),
  last_name VARCHAR(30),
  address VARCHAR(255),
  city VARCHAR(80),
  telephone VARCHAR(20)
);

CREATE TABLE pets (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(30),
  birth_date DATE,
  type_id INT NOT NULL,
  owner_id INT NOT NULL,
  FOREIGN KEY (owner_id) REFERENCES owners(id),
  FOREIGN KEY (type_id) REFERENCES types(id)
);

CREATE TABLE types (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(80)
);
```

#### Vets Service
```sql
-- Tables: vets, specialties, vet_specialties
CREATE TABLE vets (
  id INT PRIMARY KEY AUTO_INCREMENT,
  first_name VARCHAR(30),
  last_name VARCHAR(30)
);

CREATE TABLE specialties (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(80)
);

CREATE TABLE vet_specialties (
  vet_id INT NOT NULL,
  specialty_id INT NOT NULL,
  FOREIGN KEY (vet_id) REFERENCES vets(id),
  FOREIGN KEY (specialty_id) REFERENCES specialties(id)
);
```

#### Visits Service
```sql
-- Table: visits
CREATE TABLE visits (
  id INT PRIMARY KEY AUTO_INCREMENT,
  pet_id INT NOT NULL,
  visit_date DATE,
  description VARCHAR(8192),
  FOREIGN KEY (pet_id) REFERENCES pets(id)  -- ⚠️ 跨資料庫外鍵
);
```

### 目標: 單一資料庫 Schema

```sql
-- 整合到單一 petclinic 資料庫
CREATE DATABASE IF NOT EXISTS petclinic;
USE petclinic;

-- Customers 模組
CREATE TABLE owners (...);
CREATE TABLE pets (...);
CREATE TABLE types (...);

-- Vets 模組
CREATE TABLE vets (...);
CREATE TABLE specialties (...);
CREATE TABLE vet_specialties (...);

-- Visits 模組
CREATE TABLE visits (
  id INT PRIMARY KEY AUTO_INCREMENT,
  pet_id INT NOT NULL,
  vet_id INT,                        -- 新增: 關聯獸醫
  visit_date DATE,
  description VARCHAR(8192),
  FOREIGN KEY (pet_id) REFERENCES pets(id),
  FOREIGN KEY (vet_id) REFERENCES vets(id)
);

-- Spring Modulith 事件發布表
CREATE TABLE event_publication (
  id UUID PRIMARY KEY,
  event_type VARCHAR(512) NOT NULL,
  listener_id VARCHAR(512) NOT NULL,
  publication_date TIMESTAMP NOT NULL,
  serialized_event TEXT NOT NULL,
  completion_date TIMESTAMP
);
```

### 資料庫遷移風險評估

| 風險 | 等級 | 說明 | 緩解措施 |
|------|------|------|---------|
| 跨資料庫外鍵 | 🟡 中等 | `visits.pet_id` 原本跨資料庫 | 合併到單一資料庫即可解決 |
| 資料遷移 | 🟢 低 | 同一伺服器,schema 結構相同 | 使用 SQL 腳本遷移 |
| 資料一致性 | 🟢 低 | FK 約束確保完整性 | 保留所有外鍵約束 |
| 效能影響 | 🟢 低 | 單體應用減少網路開銷 | 預期效能提升 |

---

## 🔄 服務通訊變更

### 現有: HTTP REST 調用 (透過 API Gateway)

```java
// API Gateway 聚合多個服務的資料
@GetMapping(value = "/owners/{ownerId}")
public Mono<OwnerDetails> getOwnerDetails(@PathVariable int ownerId) {
    return Mono
        .zip(
            getOwner(ownerId),           // HTTP 調用 customers-service
            getVisitsForPets(ownerId)    // HTTP 調用 visits-service
        )
        .map(tuple -> {
            OwnerDetails owner = tuple.getT1();
            Visits visits = tuple.getT2();
            // 聚合資料
            return addVisitsToOwner(owner).apply(visits);
        });
}
```

**問題:**
- ❌ 需要處理網路延遲
- ❌ 需要 Circuit Breaker (Resilience4j)
- ❌ 需要服務發現 (Eureka)
- ❌ 複雜的錯誤處理

### 目標: 直接方法調用

```java
// VisitServiceImpl 直接調用 CustomerService
@Service
class VisitServiceImpl implements VisitService {
    private final CustomerService customerService;
    private final PetService petService;
    
    @Override
    public Visit createVisit(VisitRequest request) {
        // 同步調用 - 無網路開銷
        Pet pet = petService.findById(request.petId())
            .orElseThrow(() -> new ResourceNotFoundException("Pet not found"));
        
        Visit visit = new Visit();
        visit.setPetId(request.petId());
        visit.setDescription(request.description());
        
        return visitRepository.save(visit);
    }
}
```

**優勢:**
- ✅ 零網路延遲 (本地方法調用)
- ✅ 簡化錯誤處理
- ✅ 無需 Circuit Breaker
- ✅ 型別安全 (編譯期檢查)

---

## 🎯 事件驅動架構評估

### 現有: 無事件機制

目前微服務間沒有事件通知機制,所有通訊都是同步 HTTP 調用。

### 目標: Spring Modulith Events

```java
// 發布事件
@Service
class CustomerServiceImpl implements CustomerService {
    private final ApplicationEventPublisher events;
    
    @Override
    public Customer create(CustomerRequest request) {
        Customer customer = customerRepository.save(...);
        
        // 發布領域事件
        events.publishEvent(new CustomerCreated(customer.getId(), customer.getFullName()));
        
        return customer;
    }
}

// 監聽事件
@Service
class AIDataSyncListener {
    
    @ApplicationModuleListener  // 異步處理
    void on(CustomerCreated event) {
        log.info("Syncing customer {} to vector DB", event.customerId());
        // 更新 AI 向量資料庫
    }
}
```

**優勢:**
- ✅ 解耦模組間依賴 (GenAI 無需直接依賴其他模組)
- ✅ 異步處理非關鍵路徑操作
- ✅ 事件持久化 (支援重試)
- ✅ 可觀測性 (追蹤事件流)

### Spring Modulith 事件發布機制

| 特性 | 說明 |
|------|------|
| **持久化** | 事件儲存在 `event_publication` 表 |
| **重試機制** | 失敗的監聽器會自動重試 |
| **事務性** | 事件與業務邏輯在同一事務中 |
| **異步處理** | `@ApplicationModuleListener` 默認異步 |
| **完成追蹤** | `completion_date` 記錄處理完成時間 |

---

## 📈 依賴分析

### Customers Service 依賴
```xml
<!-- 現有依賴 (保留) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- 移除微服務依賴 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>  ❌ 移除
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>  ❌ 移除
</dependency>
```

### Visits Service 依賴
```xml
<!-- 現有依賴 (保留) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- 移除微服務依賴 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>  ❌ 移除
</dependency>
```

### Vets Service 依賴
類似結構,移除所有 Spring Cloud 依賴。

### GenAI Service 依賴
```xml
<!-- 現有依賴 (保留) -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
</dependency>

<!-- 移除微服務依賴 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>  ❌ 移除
</dependency>
```

---

## 🧪 測試策略變更

### 現有: 獨立微服務測試

```java
@ExtendWith(SpringExtension.class)
@WebMvcTest(OwnerResource.class)  // 只測試單一服務
@ActiveProfiles("test")
class OwnerResourceTest {
    
    @Autowired
    MockMvc mvc;
    
    @MockBean
    OwnerRepository ownerRepository;
}
```

### 目標: 模組化單元測試 + 整合測試

#### 單元測試 (模組內部)
```java
@ExtendWith(SpringExtension.class)
@WebMvcTest(OwnerResource.class)
class OwnerResourceTest {
    // 保持不變
}
```

#### 整合測試 (跨模組)
```java
@SpringBootTest
@ApplicationModuleTest  // Spring Modulith 測試支援
class VisitsModuleIntegrationTest {
    
    @Autowired
    VisitService visitService;
    
    @Autowired
    CustomerService customerService;  // 真實依賴
    
    @Test
    void shouldCreateVisitWithValidPet() {
        // 建立真實 Customer 和 Pet
        Customer customer = customerService.create(...);
        
        // 建立 Visit
        Visit visit = visitService.createVisit(
            new VisitRequest(customer.getPets().get(0).getId(), ...)
        );
        
        assertThat(visit.getId()).isNotNull();
    }
}
```

#### 模組驗證測試
```java
@Test
void moduleShouldBeValid() {
    ApplicationModules modules = ApplicationModules.of(PetClinicApplication.class);
    modules.verify();  // 自動驗證模組邊界
}
```

---

## 🚨 風險評估與緩解措施

### 高風險 (需特別注意)

| 風險 | 影響 | 機率 | 緩解措施 |
|------|------|------|---------|
| 模組邊界定義錯誤 | 🔴 高 | 🟡 中 | 使用 `ApplicationModules.verify()` 自動驗證 |
| 循環依賴 | 🔴 高 | 🟡 中 | 設計階段明確定義依賴方向,使用事件解耦 |
| 事件處理失敗 | 🟡 中 | 🟡 中 | Spring Modulith 自動重試機制 |

### 中風險

| 風險 | 影響 | 機率 | 緩解措施 |
|------|------|------|---------|
| 資料庫遷移問題 | 🟡 中 | 🟢 低 | 提前測試遷移腳本,備份資料 |
| 前端路由調整 | 🟡 中 | 🟢 低 | 保持 API 端點一致性 |
| 效能回歸 | 🟢 低 | 🟢 低 | 壓力測試驗證 |

### 低風險

| 風險 | 影響 | 機率 | 緩解措施 |
|------|------|------|---------|
| Spring Modulith 學習曲線 | 🟢 低 | 🟡 中 | 參考官方文檔,示例專案 |
| 監控工具調整 | 🟢 低 | 🟢 低 | Micrometer 和 OpenTelemetry 保持不變 |

---

## 🎯 技術相容性矩陣

| 技術 | 現有版本 | Modulith 支援 | 遷移難度 | 備註 |
|------|---------|--------------|---------|------|
| Spring Boot | 3.4.1 | ✅ 完全支援 | 🟢 簡單 | 無需變更 |
| Spring Data JPA | 3.4.1 | ✅ 完全支援 | 🟢 簡單 | 無需變更 |
| Spring Web | 3.4.1 | ✅ 完全支援 | 🟢 簡單 | 無需變更 |
| Validation | 3.4.1 | ✅ 完全支援 | 🟢 簡單 | 無需變更 |
| Spring AI | 最新 | ✅ 完全支援 | 🟢 簡單 | 無需變更 |
| Micrometer | 1.14.x | ✅ 完全支援 | 🟢 簡單 | 無需變更 |
| OpenTelemetry | 最新 | ✅ 完全支援 | 🟢 簡單 | 無需變更 |
| Zipkin | 最新 | ✅ 完全支援 | 🟢 簡單 | 無需變更 |
| HSQLDB | 2.7.x | ✅ 完全支援 | 🟢 簡單 | 無需變更 |
| MySQL | 8.x | ✅ 完全支援 | 🟢 簡單 | 無需變更 |
| Chaos Monkey | 3.1.0 | ⚠️  部分支援 | 🟡 中等 | 需調整配置 |
| Spring Cloud Gateway | 2024.0.0 | ❌ 不需要 | 🔴 移除 | 單體應用無需閘道 |
| Eureka | 2024.0.0 | ❌ 不需要 | 🔴 移除 | 無服務發現需求 |
| Config Server | 2024.0.0 | ❌ 不需要 | 🔴 移除 | 統一配置檔案 |

---

## 📊 預期效能影響

### 正面影響 ✅

| 項目 | 改善幅度 | 說明 |
|------|---------|------|
| 服務間通訊延遲 | ⬇️ -100% | 本地方法調用,零網路延遲 |
| 記憶體使用 | ⬇️ -50% | 8 個 JVM → 1 個 JVM |
| 啟動時間 | ⬇️ -60% | 單一應用程式啟動 |
| 資料庫連線池 | ⬇️ -70% | 共享連線池 |
| 部署複雜度 | ⬇️ -90% | 1 個容器 vs 8 個容器 |

### 需監控項目 ⚠️

| 項目 | 風險 | 監控方式 |
|------|------|---------|
| 單點故障 | 🟡 中 | 部署多個實例 + 負載均衡 |
| 記憶體峰值 | 🟢 低 | JVM 監控 + Heap Dump 分析 |
| 資料庫連線數 | 🟢 低 | Connection Pool Metrics |
| CPU 使用率 | 🟢 低 | Micrometer CPU Metrics |

---

## 📝 Maven 依賴變更清單

### 父 POM 新增依賴
```xml
<properties>
    <spring-modulith.version>1.3.0</spring-modulith.version>
</properties>

<dependencyManagement>
    <dependencies>
        <!-- Spring Modulith BOM -->
        <dependency>
            <groupId>org.springframework.modulith</groupId>
            <artifactId>spring-modulith-bom</artifactId>
            <version>${spring-modulith.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <!-- Spring Modulith Core -->
    <dependency>
        <groupId>org.springframework.modulith</groupId>
        <artifactId>spring-modulith-starter-core</artifactId>
    </dependency>
    
    <!-- Spring Modulith JPA Events -->
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
    
    <!-- Spring Modulith Docs (Test Scope) -->
    <dependency>
        <groupId>org.springframework.modulith</groupId>
        <artifactId>spring-modulith-docs</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 移除的依賴 (從所有模組)
```xml
<!-- 完全移除 Spring Cloud 依賴管理 -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>  ❌ 移除
        </dependency>
    </dependencies>
</dependencyManagement>

<!-- 移除所有微服務相關依賴 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>  ❌ 移除
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>  ❌ 移除
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>  ❌ 移除
</dependency>
```

---

## ✅ 技術可行性結論

### 整體評估: 🟢 可行且推薦

**評分: 85/100**

| 評估項目 | 評分 | 說明 |
|---------|------|------|
| 技術相容性 | 95/100 | Spring Boot 3.4.1 完全支援 Spring Modulith 1.3.0 |
| 遷移複雜度 | 75/100 | 需要重構套件結構,但邏輯變更較少 |
| 風險等級 | 80/100 | 主要風險可控,有緩解措施 |
| 預期效益 | 90/100 | 大幅簡化架構,提升效能 |

### 推薦執行 ✅

**理由:**
1. ✅ Spring Boot 版本完全相容
2. ✅ 現有代碼邏輯可重用 (>80%)
3. ✅ 資料庫遷移簡單 (同構 schema)
4. ✅ 效能預期提升顯著
5. ✅ 維護成本大幅降低
6. ✅ Spring Modulith 提供自動化驗證工具

### 關鍵成功因素
1. 📐 嚴格遵循模組邊界設計
2. 🧪 充分的整合測試
3. 📊 完整的遷移計畫與里程碑
4. 🔍 使用 `ApplicationModules.verify()` 持續驗證
5. 📝 詳細的文檔與團隊培訓

---

## 📚 參考資源

- [Spring Modulith Reference](https://docs.spring.io/spring-modulith/reference/)
- [Spring Modulith Migration Guide](https://docs.spring.io/spring-modulith/reference/appendix.html#migration)
- [Event-Driven Architecture](https://docs.spring.io/spring-modulith/reference/events.html)
- [Spring Boot 3.4.1 Release Notes](https://spring.io/blog/2024/12/19/spring-boot-3-4-1-available-now)
