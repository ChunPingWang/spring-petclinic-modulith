# Spring PetClinic Microservices - 開發規範

## 專案概述

本專案是基於 Spring Cloud 與 Spring AI 構建的分散式 Spring PetClinic 微服務應用程式。採用微服務架構，展示如何將單體應用拆分為多個獨立服務。

**⚠️ 重要公告：本專案正在進行 Spring Modulith 重構**

我們正在將現有的微服務架構重構為基於 Spring Modulith 的模組化單體應用（Modular Monolith）。請參考 `spec.md` 了解詳細的重構計畫和規範。

## 技術棧

### 核心框架
- **Java Version**: Java 21
- **Spring Boot**: 3.4.1
- **Spring Cloud**: 2024.0.0（將移除）
- **Spring Modulith**: 1.3.0（重構目標）
- **構建工具**: Maven 3.x
- **容器化**: Docker / Podman

### 主要依賴庫

#### 現有架構（微服務）
- Spring Cloud Gateway - API 閘道
- Spring Cloud Netflix Eureka - 服務發現
- Spring Cloud Config - 集中配置管理
- Spring Data JPA - 資料持久化
- Resilience4j - 斷路器
- Micrometer - 指標監控
- OpenTelemetry & Zipkin - 分散式追蹤
- Chaos Monkey - 混沌工程

#### 目標架構（Modulith）
- **Spring Modulith Core** - 模組化架構核心
- **Spring Modulith Events** - 事件驅動架構
- **Spring Modulith Observability** - 可觀測性支援
- **Spring Modulith Actuator** - 監控端點
- Spring Data JPA - 資料持久化
- Micrometer - 指標監控
- OpenTelemetry & Zipkin - 分散式追蹤

## 架構規範

### 現有微服務結構（待重構）

專案包含以下微服務：
1. **customers-service** (Port 8081) - 管理客戶資料
2. **vets-service** (Port 8083) - 管理獸醫資訊
3. **visits-service** (Port 8082) - 管理寵物訪問記錄
4. **genai-service** (Port 8084) - 提供 AI 聊天機器人介面
5. **api-gateway** (Port 8080) - 路由客戶端請求
6. **config-server** (Port 8888) - 集中配置管理
7. **discovery-server** (Port 8761) - Eureka 服務註冊中心
8. **admin-server** (Port 9090) - Spring Boot Admin 監控

### 目標 Modulith 架構

單一應用程式 (Port 8080) 包含以下模組：

1. **customers 模組** - 管理客戶和寵物資料
2. **vets 模組** - 管理獸醫資訊和專長
3. **visits 模組** - 管理寵物就診記錄
4. **genai 模組** - AI 聊天機器人功能
5. **shared 模組** - 共享工具和基礎設施

### Spring Modulith 模組規範

#### 套件結構規則

```
org.springframework.samples.petclinic/
├── customers/              # Customers 模組（公開 API）
│   ├── Customer.java       # 公開實體
│   ├── CustomerService.java # 公開服務介面
│   ├── CustomerCreated.java # 領域事件
│   └── internal/           # 內部實作（不可外部訪問）
│       ├── CustomerServiceImpl.java
│       ├── CustomerRepository.java
│       └── web/
│           └── OwnerResource.java
└── vets/                   # 其他模組遵循相同結構
```

#### 模組邊界規則

1. **公開 API**：直接放在模組根套件下（如 `customers/`）
2. **內部實作**：放在 `internal/` 子套件下
3. **禁止訪問**：模組不可訪問其他模組的 `internal` 套件
4. **循環依賴**：嚴格禁止模組間的循環依賴

#### 模組通訊規範

**直接調用（同步）**
```java
// visits 模組呼叫 customers 模組的公開 API
@Service
class VisitServiceImpl {
    private final CustomerService customerService; // 允許：公開介面
    
    void createVisit(VisitRequest request) {
        Customer customer = customerService.findById(request.customerId());
        // ...
    }
}
```

**事件發布/訂閱（異步）**
```java
// customers 模組發布事件
@Service
class CustomerServiceImpl {
    private final ApplicationEventPublisher events;
    
    void createCustomer(CustomerRequest request) {
        Customer customer = save(...);
        events.publishEvent(new CustomerCreated(customer.getId()));
    }
}

// genai 模組監聽事件
@Service
class AIDataProvider {
    @ApplicationModuleListener
    void on(CustomerCreated event) {
        // 更新向量資料庫
    }
}
```

### 服務依賴順序

目標架構為單一應用程式，無需關注啟動順序。

## 程式碼規範

### 套件結構

#### 現有微服務結構
每個微服務遵循一致的套件結構：
```
org.springframework.samples.petclinic.<service-name>/
├── model/           # JPA 實體和資料模型
├── web/             # REST 控制器和 DTO
├── config/          # Spring 配置類別
└── <ServiceName>ServiceApplication.java  # 主應用程式類別
```

#### 目標 Modulith 結構
Spring Modulith 模組遵循以下套件結構：
```
org.springframework.samples.petclinic/
├── PetClinicApplication.java  # 主應用程式
├── customers/                  # Customers 模組
│   ├── Customer.java           # 公開：實體
│   ├── CustomerService.java    # 公開：服務介面
│   ├── CustomerCreated.java    # 公開：領域事件
│   └── internal/               # 內部實作
│       ├── CustomerServiceImpl.java
│       ├── CustomerRepository.java
│       ├── Pet.java            # 內部實體
│       └── web/
│           └── OwnerResource.java
├── vets/                       # Vets 模組
│   ├── Vet.java
│   ├── VetService.java
│   └── internal/
├── visits/                     # Visits 模組
│   ├── Visit.java
│   ├── VisitService.java
│   └── internal/
├── genai/                      # GenAI 模組
│   ├── ChatService.java
│   └── internal/
└── shared/                     # 共享模組
    ├── exceptions/
    ├── config/
    └── dto/
```

### 命名規範

#### 類別命名
- **實體類別**: 使用名詞，如 `Owner`, `Pet`, `Visit`
- **控制器**: 使用 `*Resource` 後綴，如 `OwnerResource`, `PetResource`
- **Repository**: 使用 `*Repository` 後綴，如 `OwnerRepository`
- **配置類別**: 使用 `*Config` 後綴，如 `MetricConfig`
- **異常類別**: 使用 `*Exception` 後綴，如 `ResourceNotFoundException`
- **Mapper**: 使用 `*Mapper` 後綴，如 `OwnerEntityMapper`
- **測試類別**: 使用 `*Test` 後綴，如 `OwnerResourceTest`

#### 變數與方法命名
- 使用駝峰式命名法 (camelCase)
- 方法名使用動詞開頭：`findOwner()`, `createOwner()`, `updateOwner()`
- Repository 方法遵循 Spring Data JPA 命名慣例：`findById()`, `findAll()`, `save()`

### 註解使用規範

#### 控制器註解
```java
@RestController
@RequestMapping("/owners")
@Timed("petclinic.owner")  // Micrometer 指標
class OwnerResource {
    // Controller methods
}
```

#### 實體類別註解
```java
@Entity
@Table(name = "owners")
public class Owner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "first_name")
    @NotBlank
    private String firstName;
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "owner")
    private Set<Pet> pets;
}
```

#### REST 端點註解
```java
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public Owner createOwner(@Valid @RequestBody OwnerRequest ownerRequest) {
    // Implementation
}

@GetMapping(value = "/{ownerId}")
public Optional<Owner> findOwner(@PathVariable("ownerId") @Min(1) int ownerId) {
    // Implementation
}

@PutMapping(value = "/{ownerId}")
@ResponseStatus(HttpStatus.NO_CONTENT)
public void updateOwner(@PathVariable("ownerId") @Min(1) int ownerId, 
                        @Valid @RequestBody OwnerRequest ownerRequest) {
    // Implementation
}
```

### 依賴注入規範

**優先使用建構子注入**（Constructor Injection）：
```java
private final OwnerRepository ownerRepository;
private final OwnerEntityMapper ownerEntityMapper;

OwnerResource(OwnerRepository ownerRepository, OwnerEntityMapper ownerEntityMapper) {
    this.ownerRepository = ownerRepository;
    this.ownerEntityMapper = ownerEntityMapper;
}
```

避免使用 `@Autowired` 欄位注入。

### 日誌規範

使用 SLF4J Logger：
```java
private static final Logger log = LoggerFactory.getLogger(OwnerResource.class);

log.info("Saving owner {}", ownerModel);
```

### 驗證規範

使用 Jakarta Validation 註解：
- `@Valid` - 用於方法參數驗證
- `@NotBlank` - 字串不可為空
- `@Digits` - 數字格式驗證
- `@Min` - 最小值驗證

## 測試規範

### 單元測試結構

```java
@ExtendWith(SpringExtension.class)
@WebMvcTest(OwnerResource.class)
@ActiveProfiles("test")
class OwnerResourceTest {
    
    @Autowired
    MockMvc mvc;
    
    @MockBean
    OwnerRepository ownerRepository;
    
    @Test
    void shouldGetAnOwnerInJsonFormat() throws Exception {
        // Given
        given(ownerRepository.findById(1)).willReturn(Optional.of(owner));
        
        // When & Then
        mvc.perform(get("/owners/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.firstName").value("George"));
    }
}
```

### 測試命名規範
- 測試方法使用 `should` 前綴：`shouldGetAnOwnerInJsonFormat()`
- 使用描述性命名，清楚說明測試意圖
- 遵循 Given-When-Then 模式

### 測試配置
- 使用 `@ActiveProfiles("test")` 啟用測試配置
- 使用 `@MockBean` 模擬依賴
- 使用 `@WebMvcTest` 進行控制器層測試

## 配置管理規範

### application.yml 結構

```yaml
spring:
  application:
    name: customers-service
  config:
    import: optional:configserver:${CONFIG_SERVER_URL:http://localhost:8888/}

---
spring:
  config:
    activate:
      on-profile: docker
    import: configserver:http://config-server:8888
```

### 環境變數規範
- 使用環境變數配置敏感資訊：
  - `OPENAI_API_KEY` - OpenAI API 金鑰
  - `AZURE_OPENAI_KEY` - Azure OpenAI 金鑰
  - `AZURE_OPENAI_ENDPOINT` - Azure OpenAI 端點

## Docker 規範

### Dockerfile 規範
- 基礎映像：Eclipse Temurin with Java 21
- 目標平台：預設 `linux/amd64`，Apple Silicon M2 使用 `linux/arm64`

### Docker Compose 規範
- 每個服務設定記憶體限制：`512M`
- 使用 `healthcheck` 確保服務健康
- 使用 `depends_on` 與 `condition: service_healthy` 控制啟動順序

### 構建 Docker 映像

```bash
# 使用 Docker
./mvnw clean install -P buildDocker

# 使用 Podman
./mvnw clean install -PbuildDocker -Dcontainer.executable=podman

# 指定平台（Apple M2）
./mvnw clean install -P buildDocker -Dcontainer.platform="linux/arm64"
```

## 監控與指標規範

### Micrometer 指標
- 在控制器類別層級使用 `@Timed` 註解
- 指標命名格式：`petclinic.<domain>`
  - `petclinic.owner`
  - `petclinic.pet`
  - `petclinic.visit`

### 健康檢查
- 所有服務必須啟用 Spring Boot Actuator
- 暴露 `/actuator/health` 端點

### 分散式追蹤
- 使用 Micrometer Tracing + OpenTelemetry
- 追蹤資料傳送至 Zipkin (Port 9411)

## 資料庫規範

### 預設配置
- 使用 HSQLDB 記憶體資料庫
- 啟動時自動建立 schema 並填充資料

### MySQL 配置
- 使用 `mysql` Spring Profile 啟用 MySQL
- MySQL 連線：`jdbc:mysql://localhost:3306/petclinic`
- 資料庫腳本位置：`db/mysql/{schema,data}.sql`

### JPA 規範
- 使用 `@Entity` 標註實體類別
- 使用 `@Table(name = "...")` 指定表名
- 主鍵使用 `IDENTITY` 策略：`@GeneratedValue(strategy = GenerationType.IDENTITY)`
- 欄位名稱使用 `@Column(name = "...")` 對應資料庫欄位（snake_case）

## 混沌工程規範

### Chaos Monkey
- 使用 `chaos-monkey` profile 啟用
- 支援攻擊類型：
  - `attacks_enable_exception` - 異常攻擊
  - `attacks_enable_killapplication` - 終止應用程式
  - `attacks_enable_latency` - 延遲攻擊
  - `attacks_enable_memory` - 記憶體攻擊

### 使用範例
```bash
./scripts/chaos/call_chaos.sh visits attacks_enable_exception watcher_enable_restcontroller
```

## 授權協議

本專案採用 Apache License 2.0：
```java
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
```

## 建置與部署規範

### 本機開發
```bash
# 啟動單一微服務
cd spring-petclinic-<service-name>
../mvnw spring-boot:run

# 完整建置
./mvnw clean install
```

### Docker Compose 部署
```bash
# 建置映像
./mvnw clean install -P buildDocker

# 啟動所有服務
docker compose up

# 使用 Podman
podman-compose up
```

### CI/CD 規範
- 使用 GitHub Actions
- 主分支：`main`
- JDK 版本：17（CI）, 21（Runtime）
- Maven 指令：`mvn -B package --file pom.xml`

## 前端規範

### 技術棧
- AngularJS
- Bootstrap (通過 SCSS 編譯)
- Gulp - 任務自動化
- Bower - JavaScript 函式庫管理

### CSS 編譯
```bash
cd spring-petclinic-api-gateway
mvn generate-resources -P css
```

### 字型
- 主要字型：`varela_roundregular`, sans-serif
- 標題字型：`montserratregular`, sans-serif

## API 設計規範

### RESTful 端點
- **GET** - 查詢資源
- **POST** - 創建資源（回傳 `201 CREATED`）
- **PUT** - 更新資源（回傳 `204 NO_CONTENT`）
- **DELETE** - 刪除資源

### 路徑設計
```
/owners              - 所有擁有者
/owners/{ownerId}    - 特定擁有者
/owners/{ownerId}/pets/{petId}  - 巢狀資源
```

### 回應格式
- Content-Type: `application/json`
- 使用 Optional 包裝單一資源查詢結果
- 使用 List 回傳集合

## 開發最佳實踐

### 程式碼品質
1. 遵循 SOLID 原則
2. 保持方法簡短，單一職責
3. 適當使用 `final` 關鍵字標記不可變欄位
4. 使用 `Optional` 處理可能為 null 的值

### 錯誤處理
- 使用自定義異常：`ResourceNotFoundException`
- 提供有意義的錯誤訊息
- 使用適當的 HTTP 狀態碼

### 效能考量
- 使用 `FetchType.EAGER` 或 `LAZY` 適當配置關聯載入
- 為關聯實體設定適當的 `CascadeType`
- 使用排序：`PropertyComparator.sort()`

### 安全性
- 使用 `@Valid` 驗證輸入
- 敏感配置使用環境變數
- 定期更新依賴庫以修補安全漏洞

## 除錯與監控

### 監控端點
- Eureka Dashboard: http://localhost:8761
- API Gateway: http://localhost:8080
- Zipkin: http://localhost:9411/zipkin/
- Grafana: http://localhost:3000
- Prometheus: http://localhost:9091
- Spring Boot Admin: http://localhost:9090

### 日誌級別
- 開發環境：`DEBUG`
- 生產環境：`INFO`
- 錯誤追蹤：`ERROR`

## 貢獻指南

### 問題追蹤
- Issue Tracker: https://github.com/spring-petclinic/spring-petclinic-microservices/issues

### 編輯器配置
- 使用 `.editorconfig` 統一編輯器格式
- 支援常見文字編輯器
- 下載插件：http://editorconfig.org

### Pull Request 規範
1. Fork 專案到個人帳號
2. 建立功能分支
3. 遵循現有程式碼風格
4. 編寫測試
5. 提交 Pull Request 至 `main` 分支

## 參考資源

### Spring PetClinic 相關
- [Spring Petclinic Framework 簡報](http://fr.slideshare.net/AntoineRey/spring-framework-petclinic-sample-application)
- [Spring Petclinic Microservices 部落格文章](http://javaetmoi.com/2018/10/architecture-microservices-avec-spring-cloud/)（法語）
- [Spring Boot Actuator 文件](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-metrics.html)
- [Spring Cloud 官方文件](https://spring.io/projects/spring-cloud)

### Spring Modulith 相關
- [Spring Modulith 官方文件](https://docs.spring.io/spring-modulith/reference/)
- [Spring Modulith Reference Guide](https://docs.spring.io/spring-modulith/docs/current/reference/html/)
- [Spring Modulith GitHub](https://github.com/spring-projects/spring-modulith)
- [Modular Monolith with Spring Modulith](https://github.com/maciejwalkowiak/spring-modulith-example)
- [Event-Driven Architecture with Spring Modulith](https://www.baeldung.com/spring-modulith)

### 重構規範
- 詳細重構計畫請參考：`spec.md`
