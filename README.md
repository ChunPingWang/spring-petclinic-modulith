# Spring PetClinic 模組化單體應用

[![建置狀態](https://github.com/spring-petclinic/spring-petclinic-microservices/actions/workflows/maven-build.yml/badge.svg)](https://github.com/spring-petclinic/spring-petclinic-microservices/actions/workflows/maven-build.yml)
[![授權條款](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

本專案展示了如何使用 [Spring Modulith](https://spring.io/projects/spring-modulith) 建置**模組化單體應用**。這是 Spring PetClinic 微服務應用的重構版本，將其轉換為一個具有清晰模組邊界的單體應用。

## 🎯 為什麼選擇 Spring Modulith?

Spring Modulith 提供了微服務的模組化優勢，同時避免了維運複雜性：

- ✅ **清晰的模組邊界** - 在編譯時強制執行
- ✅ **事件驅動架構** - 模組間鬆耦合
- ✅ **單一部署單元** - 無分散式系統複雜性
- ✅ **完整的可觀測性** - 內建追蹤和監控
- ✅ **更快的開發速度** - 簡化本地開發和測試

## 🚀 快速開始

### 前置需求

- **Java 17+**（必需）
- **Maven 3.8.1+**
- **Docker**（選用，用於完整的可觀測性堆疊）

### 本地執行

```bash
cd spring-petclinic-modulith
../mvnw spring-boot:run
```

存取應用程式：http://localhost:8080

### 使用 Docker Compose 執行

啟動完整的技術堆疊（應用程式 + MySQL + 監控）：

```bash
docker-compose up
```

這將啟動：
- **PetClinic Modulith 應用程式** - http://localhost:8080
- **MySQL** 資料庫
- **Zipkin** 鏈路追蹤 - http://localhost:9411
- **Prometheus** 指標收集 - http://localhost:9091
- **Grafana** 視覺化儀表板 - http://localhost:3000

## 📦 專案結構

```
spring-petclinic-modulith/
├── spring-petclinic-modulith/    # 主應用程式（Spring Modulith）
│   ├── src/main/java/
│   │   └── org/springframework/samples/petclinic/
│   │       ├── customers/        # 客戶和寵物管理
│   │       ├── vets/            # 獸醫管理
│   │       ├── visits/          # 就診記錄
│   │       ├── genai/           # AI 聊天機器人（Spring AI）
│   │       └── shared/          # 共享基礎設施
│   └── src/main/resources/
│       ├── application.yml
│       ├── schema.sql           # HSQLDB 結構描述
│       ├── schema-mysql.sql     # MySQL 結構描述
│       └── static/              # 前端（AngularJS）
├── docker/                      # Docker 設定
├── docs/                        # 文件
├── docker-compose.yml          # 完整堆疊部署
└── README.md                   # 本檔案
```

## 🏗️ 架構設計

### 模組組織

每個模組遵循 Spring Modulith 模式：

```
customers/
├── Customer.java              # 公開 API
├── CustomerService.java       # 公開介面
├── CustomerCreated.java      # 領域事件
└── internal/                 # 實作細節（隱藏）
    ├── CustomerServiceImpl.java
    ├── CustomerRepository.java
    └── web/
        └── OwnerResource.java
```

### 模組間通訊

**同步呼叫（直接呼叫）：**
```java
@Service
class VisitServiceImpl {
    private final CustomerService customerService; // 公開介面

    void createVisit(Visit visit) {
        Customer customer = customerService.findById(visit.getCustomerId());
    }
}
```

**非同步呼叫（事件）：**
```java
// 發布事件
events.publishEvent(new CustomerCreated(customerId));

// 監聽事件
@ApplicationModuleListener
void on(CustomerCreated event) {
    updateVectorStore(event.getCustomerId());
}
```

## 🔧 建置專案

```bash
# 建置所有模組
./mvnw clean install

# 僅建置 Modulith 應用程式
cd spring-petclinic-modulith
../mvnw clean install

# 跳過測試
../mvnw clean install -DskipTests

# 建置 Docker 映像檔
../mvnw clean install -P buildDocker
```

## 🧪 測試

```bash
# 執行所有測試
cd spring-petclinic-modulith
../mvnw test

# 驗證模組結構
../mvnw test -Dtest=ModulithStructureTest

# 執行特定測試
../mvnw test -Dtest=OwnerResourceTest
```

## 🗄️ 資料庫設定

### HSQLDB（預設）

無需設定。啟動時自動填充資料。

### MySQL

1. 啟動 MySQL：
```bash
docker run -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=petclinic -p 3306:3306 mysql:8.0
```

2. 使用 MySQL profile 執行：
```bash
cd spring-petclinic-modulith
../mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=mysql"
```

## 🤖 AI 功能（選用）

應用程式包含基於 Spring AI 的 AI 聊天機器人。

**設定 OpenAI：**
```bash
export OPENAI_API_KEY="your_api_key"
cd spring-petclinic-modulith
../mvnw spring-boot:run
```

**設定 Azure OpenAI：**
```bash
export AZURE_OPENAI_ENDPOINT="https://your_resource.openai.azure.com"
export AZURE_OPENAI_KEY="your_api_key"
```

存取聊天介面：http://localhost:8080（UI 中的聊天介面）

## 📊 監控與可觀測性

### Actuator 端點

- 健康檢查：http://localhost:8080/actuator/health
- 指標資料：http://localhost:8080/actuator/prometheus
- 模組資訊：http://localhost:8080/actuator/modulith

### 分散式追蹤

使用 docker-compose 執行時：
- Zipkin UI：http://localhost:9411
- Grafana：http://localhost:3000（使用者名稱/密碼：admin/admin）
- Prometheus：http://localhost:9091

### 自訂指標

應用程式使用 Micrometer `@Timed` 註解：
- `petclinic.owner` - 客戶操作
- `petclinic.pet` - 寵物操作
- `petclinic.visit` - 就診操作
- `petclinic.vet` - 獸醫操作

## 🔍 API 文件

所有端點都由 http://localhost:8080 提供

### 客戶管理
- `GET /owners` - 取得所有客戶
- `GET /owners/{id}` - 根據 ID 取得客戶
- `POST /owners` - 建立客戶
- `PUT /owners/{id}` - 更新客戶

### 寵物管理
- `GET /owners/{ownerId}/pets` - 取得客戶的寵物
- `POST /owners/{ownerId}/pets` - 新增寵物
- `PUT /owners/{ownerId}/pets/{petId}` - 更新寵物

### 獸醫管理
- `GET /vets` - 取得所有獸醫
- `GET /vets/{id}` - 根據 ID 取得獸醫

### 就診管理
- `GET /visits?petId={id}` - 取得寵物的就診記錄
- `POST /visits` - 建立就診記錄

### AI 聊天
- `POST /genai/chat` - 與 AI 聊天

## 🐳 Docker

### 建置映像檔

```bash
cd spring-petclinic-modulith
../mvnw clean install -P buildDocker

# Apple Silicon (M1/M2)
../mvnw clean install -P buildDocker -Dcontainer.platform="linux/arm64"
```

### 執行容器

```bash
docker run -p 8080:8080 springcommunity/spring-petclinic-modulith:3.4.1
```

## 📚 文件

- **[CLAUDE.md](CLAUDE.md)** - Claude Code 使用指南
- **[spring-petclinic-modulith/README.md](spring-petclinic-modulith/README.md)** - 詳細文件
- **[spring-petclinic-modulith/ARCHITECTURE_DECISIONS.md](spring-petclinic-modulith/ARCHITECTURE_DECISIONS.md)** - 架構決策記錄
- **[spring-petclinic-modulith/DEVELOPER_GUIDE.md](spring-petclinic-modulith/DEVELOPER_GUIDE.md)** - 開發者指南

## 🎓 學習資源

本專案展示了：
- ✨ Spring Modulith 架構模式
- ✨ Spring AI 整合和 LLM 使用
- ✨ 事件驅動架構
- ✨ 全面的可觀測性設定
- ✨ 從微服務遷移到模組化單體

## 📝 技術堆疊

| 元件 | 版本 | 用途 |
|-----------|---------|---------|
| Spring Boot | 3.4.1 | 應用程式框架 |
| Spring Modulith | 1.3.0 | 模組化架構 |
| Spring AI | 1.0.0-M5 | AI/LLM 整合 |
| Spring Data JPA | 3.4.1 | 資料持久化 |
| Micrometer | 1.14.0 | 指標收集 |
| OpenTelemetry | 1.44.0 | 分散式追蹤 |
| HSQLDB | 2.7.3 | 開發資料庫 |
| MySQL | 8.0+ | 正式環境資料庫 |

## 🆚 對比：微服務 vs 模組化單體

| 面向 | 微服務（之前） | 模組化單體（現在） |
|--------|----------------------|----------------|
| 服務數量 | 8 個獨立應用程式 | 1 個應用程式 |
| 部署方式 | 複雜的編排 | 單一 JAR |
| 連接埠 | 8080, 8081-8084, 8761, 8888, 9090 | 僅 8080 |
| 服務探索 | 需要 Eureka | 不需要 |
| 設定管理 | Config Server | application.yml |
| API 閘道 | 必需 | 不需要 |
| 啟動時間 | ~2-3 分鐘 | ~30 秒 |
| 記憶體使用 | ~2GB+ | ~512MB |
| 模組邊界 | 網路呼叫 | 套件結構 |
| 開發體驗 | 複雜設定 | 簡單的 `mvn spring-boot:run` |

## 🤝 貢獻

歡迎貢獻！請參閱我們的[貢獻指南](.github/CONTRIBUTING.md)。

## 📄 授權條款

Apache License 2.0 - 詳見 [LICENSE](LICENSE) 檔案。

## 🙏 致謝

- Spring PetClinic 原始團隊
- Spring Modulith 專案團隊
- Spring AI 團隊

---

**狀態**：✅ 正式環境就緒
**版本**：3.4.1
**最後更新**：2025-11-22
