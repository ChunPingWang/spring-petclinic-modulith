# 功能規格:Shared 模組(共用基礎設施)

**功能分支**: `005-shared-module`
**建立日期**: 2025-11-23
**狀態**: 已實現(OPEN 模組)
**架構**: 基礎設施層(Infrastructure Layer)

---

## 使用者場景與測試 *(必填)*

### 使用者故事 1 - 統一錯誤處理與回應格式 (優先級: P1)

開發人員與 API 使用者需要一致的錯誤回應格式,以便前端應用程式能夠統一處理錯誤並向使用者顯示友善的錯誤訊息。

**此優先級的原因**: 統一的錯誤處理是 REST API 設計的基礎,直接影響 API 使用體驗與前端開發效率。

**獨立測試**: 可透過觸發各種例外、驗證 HTTP 狀態碼、驗證錯誤回應格式等操作驗證。

**驗收場景**:

1. **Given** API 端點收到無效請求,**When** 系統拋出驗證例外,**Then** 系統返回 400 Bad Request 與統一格式的錯誤訊息
2. **Given** API 端點嘗試存取不存在的資源,**When** 系統拋出 ResourceNotFoundException,**Then** 系統返回 404 Not Found 與詳細錯誤訊息
3. **Given** API 端點發生未預期的例外,**When** 系統捕獲一般例外,**Then** 系統返回 500 Internal Server Error 並隱藏內部細節
4. **Given** 錯誤回應需要包含多個驗證錯誤,**When** 系統收集所有欄位驗證錯誤,**Then** 錯誤回應包含完整的錯誤清單

---

### 使用者故事 2 - 應用程式可觀測性與監控 (優先級: P1)

維運人員與開發人員需要監控應用程式的健康狀態、效能指標與追蹤資訊,以便快速發現問題、診斷效能瓶頸並確保系統穩定運作。

**此優先級的原因**: 可觀測性是生產環境運作的基礎,沒有監控就無法確保系統健康與效能。

**獨立測試**: 可透過查詢 Actuator 端點、驗證指標收集、驗證追蹤資料等操作驗證。

**驗收場景**:

1. **Given** 維運人員需要檢查應用程式健康狀態,**When** 查詢 `/actuator/health`,**Then** 系統返回完整的健康檢查結果(資料庫、向量儲存、外部 API)
2. **Given** 維運人員需要監控 JVM 效能,**When** 查詢 `/actuator/prometheus`,**Then** 系統返回 Prometheus 格式的完整指標(JVM、HTTP、資料庫)
3. **Given** 開發人員需要追蹤跨模組呼叫,**When** 發送 HTTP 請求並檢查 Zipkin,**Then** 系統記錄完整的分散式追蹤資訊
4. **Given** 維運人員需要調整日誌級別,**When** 透過 `/actuator/loggers` 動態調整,**Then** 系統立即套用新的日誌級別而無需重啟

---

### 使用者故事 3 - API 文件自動生成與互動測試 (優先級: P2)

開發人員與 API 使用者需要完整、最新的 API 文件,並能在瀏覽器中直接測試 API,以便快速理解 API 用法並進行整合開發。

**此優先級的原因**: API 文件對開發效率重要,但優先級略低於錯誤處理與監控。

**獨立測試**: 可透過存取 Swagger UI、驗證 OpenAPI 規格、測試 API 呼叫等操作驗證。

**驗收場景**:

1. **Given** 開發人員需要查看 API 文件,**When** 存取 Swagger UI (`/swagger-ui.html`),**Then** 系統顯示所有 REST API 端點的完整文件
2. **Given** 開發人員需要測試 API,**When** 在 Swagger UI 中輸入參數並執行請求,**Then** 系統返回實際的 API 回應
3. **Given** 自動化工具需要 OpenAPI 規格,**When** 查詢 `/v3/api-docs`,**Then** 系統返回 JSON 格式的 OpenAPI 3.0 規格
4. **Given** API 端點新增或修改,**When** 應用程式重新啟動,**Then** Swagger UI 自動反映最新的 API 變更

---

### 邊界案例

- 當資料庫連線失敗時,健康檢查端點應返回 DOWN 狀態並標示資料庫元件失敗
- 當 Zipkin 不可用時,追蹤功能應優雅降級而不影響主要功能
- 當 Prometheus 端點被高頻查詢時,系統應快取指標而不影響效能
- 當日誌級別動態調整為 DEBUG 時,系統應記錄詳細日誌但不影響效能
- 當 Swagger UI 存取量過大時,系統應限制靜態資源頻寬

---

## 需求 *(必填)*

### 功能需求

#### 例外處理需求
- **FR-001**: 系統必須提供全域 REST 例外處理器,統一處理所有 API 例外
- **FR-002**: 系統必須提供統一的錯誤回應格式(ErrorResponse DTO)
- **FR-003**: 系統必須處理 ResourceNotFoundException 並返回 404 Not Found
- **FR-004**: 系統必須處理驗證例外並返回 400 Bad Request 與欄位錯誤清單
- **FR-005**: 系統必須處理一般例外並返回 500 Internal Server Error
- **FR-006**: 系統必須在錯誤回應中包含時間戳記
- **FR-007**: 系統必須在日誌中記錄所有 500 錯誤的完整堆疊追蹤

#### 可觀測性需求
- **FR-008**: 系統必須整合 Micrometer 收集 JVM、HTTP、資料庫指標
- **FR-009**: 系統必須提供 Prometheus 格式的指標端點(`/actuator/prometheus`)
- **FR-010**: 系統必須整合 OpenTelemetry 進行分散式追蹤
- **FR-011**: 系統必須將追蹤資料發送至 Zipkin(或其他追蹤後端)
- **FR-012**: 系統必須提供健康檢查端點(`/actuator/health`)
- **FR-013**: 系統必須實現自訂健康指標(資料庫、向量儲存、外部 API)
- **FR-014**: 系統必須提供模組結構端點(`/actuator/modulith`)
- **FR-015**: 系統必須支援動態調整日誌級別(`/actuator/loggers`)

#### API 文件需求
- **FR-016**: 系統必須整合 OpenAPI 3.0 與 Swagger UI
- **FR-017**: 系統必須自動從控制器生成 API 文件
- **FR-018**: 系統必須提供 Swagger UI 互動介面(`/swagger-ui.html`)
- **FR-019**: 系統必須提供 OpenAPI JSON 規格(`/v3/api-docs`)
- **FR-020**: 系統必須在 API 文件中包含應用程式資訊(標題、版本、聯絡方式)

#### 配置需求
- **FR-021**: 系統必須提供 CORS 配置支援跨網域請求
- **FR-022**: 系統必須提供 Web MVC 配置(攔截器、訊息轉換器)
- **FR-023**: 系統必須為所有指標加入共用標籤(application, environment)
- **FR-024**: 系統必須配置追蹤取樣率(開發環境 100%,生產環境可調整)

### 關鍵實體

- **ErrorResponse(錯誤回應 DTO)**:
  - 屬性:status, message, errors, timestamp
  - Record 類別

- **ResourceNotFoundException(資源不存在例外)**:
  - 繼承:RuntimeException
  - 用途:統一的 404 例外

---

## 成功標準 *(必填)*

### 可衡量的結果

#### 錯誤處理標準
- **SC-001**: 100% 的 API 例外返回統一格式的錯誤回應
- **SC-002**: 所有 404 錯誤返回正確的 HTTP 狀態碼與 ErrorResponse
- **SC-003**: 所有驗證錯誤返回完整的欄位錯誤清單
- **SC-004**: 所有 500 錯誤在日誌中記錄完整堆疊追蹤

#### 可觀測性標準
- **SC-005**: 健康檢查端點回應時間 < 100ms p95
- **SC-006**: Prometheus 端點回應時間 < 200ms p95
- **SC-007**: 100% 的 HTTP 請求被追蹤(開發環境)
- **SC-008**: 分散式追蹤資料在 5 秒內出現在 Zipkin
- **SC-009**: 日誌級別動態調整在 1 秒內生效
- **SC-010**: 健康檢查準確反映所有元件狀態(資料庫、向量儲存)

#### API 文件標準
- **SC-011**: Swagger UI 載入時間 < 1 秒
- **SC-012**: API 文件涵蓋 100% 的 REST 端點
- **SC-013**: OpenAPI 規格符合 3.0 標準
- **SC-014**: Swagger UI 可成功執行所有 API 測試請求

#### 效能標準
- **SC-015**: 可觀測性元件對應用程式效能影響 < 5%
- **SC-016**: 指標收集不影響主要 API 回應時間
- **SC-017**: 追蹤記錄不影響主要業務邏輯執行

---

## 架構需求 *(此模組特定)*

### OPEN 模組架構

**重要**: 本模組標記為 **OPEN 模組**,所有其他模組都可以直接存取,不受 Spring Modulith 模組邊界限制。

- **AR-001**: 本模組必須在 `package-info.java` 中標記為 `@ApplicationModule(type = ApplicationModule.Type.OPEN)`
- **AR-002**: 本模組只包含基礎設施代碼,不包含任何業務邏輯
- **AR-003**: 本模組可直接使用所有 Spring Boot、Micrometer、OpenTelemetry 框架
- **AR-004**: 本模組提供的類別(如 ResourceNotFoundException)可被所有模組直接使用
- **AR-005**: 本模組不依賴任何其他業務模組(Customers, Vets, Visits, GenAI)
- **AR-006**: 本模組的配置類別使用 `@Configuration` 註解自動載入
- **AR-007**: 本模組的例外處理器使用 `@RestControllerAdvice` 全域生效

### 配置需求

- **AR-008**: 所有可觀測性配置必須透過 `application.yml` 進行外部化
- **AR-009**: Actuator 端點暴露範圍必須可配置(開發 vs. 生產環境)
- **AR-010**: OpenAPI 配置必須包含應用程式元資訊(版本、描述、聯絡方式)
- **AR-011**: CORS 配置必須只允許已知的前端網域
- **AR-012**: 追蹤取樣率必須可根據環境調整

### 測試需求

- **TR-001**: 例外處理器必須有單元測試驗證所有例外類型的處理
- **TR-002**: 健康檢查指標必須有測試驗證各種狀態(UP, DOWN)
- **TR-003**: 所有配置類別必須可正常載入而不報錯
- **TR-004**: OpenAPI 規格必須通過驗證工具檢查

---

## 提供的共用元件 *(此模組特定)*

### 例外類別

- **ResourceNotFoundException**:
  - 用途:資源不存在時拋出(如 Customer, Vet, Visit 不存在)
  - HTTP 狀態碼:404 Not Found
  - 使用範例:`throw new ResourceNotFoundException("Customer not found with id: " + id)`

### DTO 類別

- **ErrorResponse**:
  - 用途:統一的錯誤回應格式
  - 欄位:status(int), message(String), errors(List<String>), timestamp(LocalDateTime)
  - 使用範例:RestExceptionHandler 自動建立並返回

### 配置類別

- **MicrometerMetricsConfig**: Micrometer 指標配置
- **ObservabilityConfig**: 可觀測性整合配置
- **DistributedTracingConfig**: 分散式追蹤配置
- **ActuatorConfig**: Actuator 端點配置
- **OpenApiConfig**: OpenAPI/Swagger 配置
- **WebMvcConfig**: Web MVC 配置(CORS, 攔截器)
- **PrometheusMetricsConfig**: Prometheus 端點配置
- **MetricsConfig**: 自訂業務指標配置

### Web 元件

- **RestExceptionHandler**: 全域例外處理器
- **TracingInterceptor**: 追蹤攔截器
- **PetClinicHealthIndicator**: 自訂健康檢查指標

---

## Actuator 端點清單 *(此模組特定)*

| 端點 | 路徑 | 描述 | 開發環境 | 生產環境 |
|------|------|------|---------|---------|
| 健康檢查 | `/actuator/health` | 應用程式健康狀態 | ✅ | ✅ |
| 應用程式資訊 | `/actuator/info` | 版本、Git commit 資訊 | ✅ | ✅ |
| Prometheus 指標 | `/actuator/prometheus` | Prometheus 格式指標 | ✅ | ✅ |
| 指標查詢 | `/actuator/metrics` | JSON 格式指標 | ✅ | ⚠️ |
| 模組結構 | `/actuator/modulith` | Spring Modulith 模組資訊 | ✅ | ❌ |
| 環境變數 | `/actuator/env` | 環境變數與配置 | ✅ | ❌ |
| 日誌級別 | `/actuator/loggers` | 動態調整日誌級別 | ✅ | ⚠️ |

**符號說明**:
- ✅ 完全開放
- ⚠️ 需要授權
- ❌ 關閉

---

## 配置範例 *(此模組特定)*

### application.yml 配置

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
      environment: ${ENVIRONMENT:development}
  tracing:
    sampling:
      probability: 1.0  # 100% 取樣(開發環境)
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
```

---

## 假設與約束

### 假設

- 開發環境可完全開放所有 Actuator 端點
- Zipkin 服務在 `localhost:9411` 執行(使用 docker-compose 時)
- Prometheus 服務會定期抓取 `/actuator/prometheus` 端點
- 前端應用程式執行在已知網域(如 `localhost:4200`)
- 所有模組遵循相同的錯誤處理慣例

### 約束

- 本模組不可包含任何業務邏輯
- 本模組不可依賴任何業務模組
- Actuator 端點在生產環境必須限制存取(需要授權)
- API Key 與敏感資訊不可記錄至日誌
- 可觀測性元件對效能影響必須最小化

---

## 依賴關係

### 依賴的模組

**無** - Shared 模組不依賴任何業務模組

### 被依賴的模組

- **Customers**: 使用 ResourceNotFoundException, ErrorResponse
- **Vets**: 使用 ResourceNotFoundException, ErrorResponse
- **Visits**: 使用 ResourceNotFoundException, ErrorResponse
- **GenAI**: 使用可觀測性配置

**依賴方式**:
- ✅ 直接 import(因為是 OPEN 模組)
- ✅ 自動套用全域配置(如 RestExceptionHandler)
- ✅ 所有模組自動收集指標

---

## 實現狀態

- ✅ **OPEN 模組標記**: 已實現
- ✅ **全域例外處理**: RestExceptionHandler 已實現
- ✅ **可觀測性整合**: Micrometer + OpenTelemetry + Zipkin
- ✅ **Actuator 端點**: 所有端點已配置
- ✅ **OpenAPI/Swagger**: 已整合
- ✅ **健康檢查**: 自訂健康指標已實現
- ✅ **CORS 配置**: WebMvcConfig 已實現
- ✅ **分散式追蹤**: TracingInterceptor 已實現

---

## 未來改進

### 基礎設施增強

1. **API 閘道功能**: 速率限制、API Key 驗證
2. **快取抽象**: 統一的快取配置(Redis)
3. **稽核日誌**: 記錄所有 CUD 操作
4. **國際化支援**: i18n 錯誤訊息

### 可觀測性增強

1. **Grafana Dashboard**: 預設的監控儀表板
2. **Alert Rules**: Prometheus 告警規則
3. **Log Aggregation**: 整合 ELK/Loki
4. **APM 整合**: Application Performance Monitoring

### 安全增強

1. **Actuator 授權**: Spring Security 保護敏感端點
2. **API Rate Limiting**: 防止濫用
3. **Request Validation**: 統一的請求驗證框架
4. **Security Headers**: HTTPS, CSP, HSTS 配置

---

## 相關文件

- [Spring PetClinic Modulith README](../../../README.md)
- [專案憲章 (Constitution)](../../../.specify/memory/constitution.md)
- [Spring Boot Actuator 文件](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer 文件](https://micrometer.io/docs)
- [OpenTelemetry 文件](https://opentelemetry.io/docs/)
- [OpenAPI Specification](https://swagger.io/specification/)

---

**最後更新**: 2025-11-23
**維護者**: Spring PetClinic Modulith Team
**相關文件**: [Shared 模組文件](../../../spring-petclinic-modulith/docs/modules/shared/README.md)
