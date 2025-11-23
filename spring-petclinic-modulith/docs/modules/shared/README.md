# Shared 模組文件

**模組名稱**: Shared (共用基礎設施)
**當前狀態**: ✅ OPEN 模組 (所有模組可存取)
**最後更新**: 2025-11-23

---

## 📋 模組概述

Shared 模組提供所有其他模組共用的基礎設施、配置和工具類別。本模組被標記為 **OPEN 模組**，表示所有其他模組都可以存取其內容，不受 Spring Modulith 模組邊界限制。

### 核心職責

- 全域配置管理（可觀測性、指標、追蹤）
- 統一例外處理（REST 例外處理器）
- 共用 DTO 與工具類別
- OpenAPI/Swagger 配置
- 健康檢查配置

### 模組特性

- **OPEN 模組**：不受模組邊界限制
- **無業務邏輯**：純基礎設施代碼
- **框架配置**：Spring Boot、Micrometer、OpenTelemetry 配置

---

## 🏗️ 架構設計

### OPEN 模組模式

```
shared/ (OPEN Module)
├── config/              ← 全域配置
├── web/                 ← Web 基礎設施
├── exceptions/          ← 共用例外
└── package-info.java    ← 標記為 OPEN 模組
```

**package-info.java**:
```java
@ApplicationModule(type = ApplicationModule.Type.OPEN)
package org.springframework.samples.petclinic.shared;

import org.springframework.modulith.ApplicationModule;
```

### 目錄結構

```
shared/
├── package-info.java                       ← OPEN 模組標記
│
├── config/                                 ← 配置類別
│   ├── MicrometerMetricsConfig.java       ← Micrometer 指標配置
│   ├── MetricsConfig.java                 ← 自訂指標配置
│   ├── PrometheusMetricsConfig.java       ← Prometheus 端點配置
│   ├── ObservabilityConfig.java           ← 可觀測性配置
│   ├── DistributedTracingConfig.java      ← 分散式追蹤配置
│   ├── TracingInterceptor.java            ← 追蹤攔截器
│   ├── ActuatorConfig.java                ← Actuator 端點配置
│   ├── OpenApiConfig.java                 ← OpenAPI/Swagger 配置
│   ├── WebMvcConfig.java                  ← Web MVC 配置
│   └── PetClinicHealthIndicator.java      ← 自訂健康檢查
│
├── web/                                    ← Web 基礎設施
│   ├── RestExceptionHandler.java          ← 全域例外處理器
│   └── ErrorResponse.java                 ← 統一錯誤回應 DTO
│
└── exceptions/                             ← 共用例外類別
    └── ResourceNotFoundException.java      ← 資源不存在例外
```

---

## ⚙️ 配置元件

### 1. Observability Config (可觀測性配置)

#### MicrometerMetricsConfig

配置 Micrometer 指標收集。

**功能**:
- 啟用 JVM 指標（記憶體、GC、執行緒）
- 啟用 HTTP 請求指標
- 啟用資料庫連線池指標
- 自訂指標標籤

**配置範例**:
```java
@Configuration
public class MicrometerMetricsConfig {
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
            .commonTags("application", "petclinic", "environment", "production");
    }
}
```

#### ObservabilityConfig

整合 OpenTelemetry 與 Micrometer。

**功能**:
- 配置追蹤取樣率
- 配置指標匯出器
- 整合分散式追蹤

#### DistributedTracingConfig

配置分散式追蹤（Zipkin/OpenTelemetry）。

**功能**:
- 自動追蹤 HTTP 請求
- 追蹤資料庫查詢
- 追蹤跨模組呼叫

**TracingInterceptor**:
```java
@Component
public class TracingInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler) {
        // 為每個請求建立 trace ID 和 span
        Span span = tracer.nextSpan().name(request.getRequestURI()).start();
        // ...
        return true;
    }
}
```

---

### 2. Metrics Config (指標配置)

#### PrometheusMetricsConfig

配置 Prometheus 指標端點。

**端點**:
- `/actuator/prometheus` - Prometheus 格式的指標

**指標類型**:
- Counter（計數器）
- Gauge（儀表）
- Timer（計時器）
- Distribution Summary（分佈摘要）

#### MetricsConfig

配置自訂業務指標。

**範例指標**:
- `petclinic.owner.created` - 建立客戶次數
- `petclinic.pet.added` - 新增寵物次數
- `petclinic.visit.scheduled` - 排程就診次數

---

### 3. Actuator Config (管理端點配置)

#### ActuatorConfig

配置 Spring Boot Actuator 端點。

**啟用的端點**:
- `/actuator/health` - 健康檢查
- `/actuator/info` - 應用程式資訊
- `/actuator/metrics` - 指標查詢
- `/actuator/prometheus` - Prometheus 指標
- `/actuator/modulith` - Spring Modulith 模組結構
- `/actuator/env` - 環境變數
- `/actuator/loggers` - 日誌級別管理

**安全配置**:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,modulith
  endpoint:
    health:
      show-details: always
```

#### PetClinicHealthIndicator

自訂健康檢查指標。

**檢查項目**:
- 資料庫連線狀態
- Vector Store 狀態 (GenAI)
- 外部 API 狀態 (OpenAI)

**回應範例**:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "hello": 1
      }
    },
    "petclinic": {
      "status": "UP",
      "details": {
        "totalOwners": 10,
        "totalPets": 13,
        "totalVets": 6
      }
    }
  }
}
```

---

### 4. OpenAPI Config (API 文件配置)

#### OpenApiConfig

配置 OpenAPI 3.0 與 Swagger UI。

**功能**:
- 自訂 API 資訊（標題、描述、版本）
- 配置聯絡資訊
- 配置授權條款
- 配置伺服器 URL

**配置範例**:
```java
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI petClinicOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Spring PetClinic Modulith API")
                .description("REST API for Spring PetClinic Modulith")
                .version("1.0.0")
                .contact(new Contact()
                    .name("PetClinic Team")
                    .email("team@petclinic.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080")
                    .description("Development Server")
            ));
    }
}
```

**存取端點**:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

---

### 5. Web Config (Web 配置)

#### WebMvcConfig

配置 Spring MVC。

**功能**:
- CORS 配置
- 攔截器配置
- 訊息轉換器配置
- 靜態資源處理

---

## 🚨 例外處理

### RestExceptionHandler

全域 REST 例外處理器，提供統一的錯誤回應格式。

**處理的例外**:

```java
@RestControllerAdvice
public class RestExceptionHandler {
    // 404 - 資源不存在
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // 400 - 請求驗證失敗
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        // 收集所有驗證錯誤
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.toList());

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation failed",
            errors,
            LocalDateTime.now()
        );
        return ResponseEntity.badRequest().body(errorResponse);
    }

    // 500 - 內部伺服器錯誤
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An unexpected error occurred",
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

### ErrorResponse

統一的錯誤回應 DTO。

**結構**:
```java
public record ErrorResponse(
    int status,
    String message,
    List<String> errors,
    LocalDateTime timestamp
) {}
```

**回應範例**:
```json
{
  "status": 404,
  "message": "Customer not found with id: 999",
  "errors": [],
  "timestamp": "2025-11-23T10:30:00"
}
```

---

## 🔗 模組依賴

### 被依賴的模組

- **所有模組**: Customers, Vets, Visits, GenAI 都依賴 Shared 模組

### 依賴方式

```java
// 任何模組都可以直接使用 Shared 模組的類別
import org.springframework.samples.petclinic.shared.exceptions.ResourceNotFoundException;
import org.springframework.samples.petclinic.shared.web.ErrorResponse;

// 無需模組邊界檢查
```

---

## 📈 效能監控

### 可觀測性堆疊

Shared 模組提供完整的可觀測性支援：

#### 1. Metrics (指標)

**Micrometer + Prometheus**:
- JVM 指標（記憶體、GC、執行緒）
- HTTP 請求指標（請求數、延遲、錯誤率）
- 資料庫連線池指標
- 自訂業務指標

**存取方式**:
```bash
# Prometheus 格式指標
curl http://localhost:8080/actuator/prometheus

# JSON 格式指標
curl http://localhost:8080/actuator/metrics
```

#### 2. Tracing (追蹤)

**OpenTelemetry + Zipkin**:
- 自動追蹤 HTTP 請求
- 追蹤資料庫查詢
- 追蹤跨模組呼叫
- 追蹤事件發布與處理

**存取方式**:
- Zipkin UI: http://localhost:9411 (使用 docker-compose 時)

#### 3. Logging (日誌)

**Structured Logging**:
- 使用 Logback
- JSON 格式日誌（生產環境）
- 日誌級別動態調整

**動態調整日誌級別**:
```bash
# 設定 customers 模組為 DEBUG 級別
curl -X POST http://localhost:8080/actuator/loggers/org.springframework.samples.petclinic.customers \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}'
```

---

## 🧪 測試支援

### 測試工具類別

Shared 模組提供測試基礎設施（未來可擴充）：

```java
// 測試用的基礎類別
@ActiveProfiles("test")
public abstract class IntegrationTestBase {
    // 共用的測試配置
}

// 測試用的 Mock 資料建構器
public class TestDataBuilder {
    public static Customer sampleCustomer() { ... }
    public static Vet sampleVet() { ... }
    public static Visit sampleVisit() { ... }
}
```

---

## 📊 配置檔案

### application.yml (主要配置)

```yaml
# 伺服器配置
server:
  port: 8080

# Spring Boot 配置
spring:
  application:
    name: petclinic-modulith

# Actuator 配置
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,modulith,env,loggers
  endpoint:
    health:
      show-details: always
  metrics:
    tags:
      application: petclinic-modulith
  tracing:
    sampling:
      probability: 1.0  # 開發環境 100% 取樣
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans

# OpenAPI 配置
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: true

# 日誌配置
logging:
  level:
    org.springframework.samples.petclinic: INFO
    org.springframework.modulith: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```

---

## 🔒 安全考量

### Actuator 端點安全

**開發環境**:
- 所有端點完全開放（便於除錯）

**生產環境建議**:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus  # 只暴露必要端點
  endpoint:
    health:
      show-details: when-authorized  # 需要授權才顯示詳情
spring:
  security:
    user:
      name: admin
      password: ${ACTUATOR_PASSWORD}  # 從環境變數讀取
```

### CORS 配置

```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("http://localhost:4200")  # 只允許前端網域
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*");
    }
}
```

---

## 🚀 未來改進

### 潛在增強

1. **分散式配置**: 整合 Spring Cloud Config
2. **API 閘道**: 加入速率限制、API Key 驗證
3. **快取抽象**: 統一的快取配置
4. **稽核日誌**: 記錄所有變更操作
5. **國際化支援**: i18n 訊息配置

### 可觀測性增強

1. **Grafana Dashboard**: 預設的監控儀表板
2. **Alert Rules**: 自動告警規則
3. **Log Aggregation**: 整合 ELK/Loki
4. **APM 整合**: Application Performance Monitoring

---

## 📚 相關文件

- [Spring PetClinic Modulith README](../../../README.md)
- [專案憲章 (Constitution)](../../../.specify/memory/constitution.md)
- [Spring Boot Actuator 文件](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer 文件](https://micrometer.io/docs)
- [OpenTelemetry 文件](https://opentelemetry.io/docs/)
- [OpenAPI Specification](https://swagger.io/specification/)

---

## 📦 依賴清單

### 核心依賴

```xml
<dependencies>
    <!-- Spring Boot Actuator -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>

    <!-- Micrometer Prometheus -->
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-prometheus</artifactId>
    </dependency>

    <!-- OpenTelemetry -->
    <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-api</artifactId>
    </dependency>

    <!-- Zipkin Tracing -->
    <dependency>
        <groupId>io.zipkin.reporter2</groupId>
        <artifactId>zipkin-reporter-brave</artifactId>
    </dependency>

    <!-- OpenAPI / Swagger -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.3.0</version>
    </dependency>
</dependencies>
```

---

**最後更新**: 2025-11-23
**維護者**: Spring PetClinic Modulith Team
**狀態**: ✅ OPEN 模組（所有模組可存取）
**重構需求**: 無（基礎設施模組）
