# Visits 模組文件

**模組名稱**: Visits (就診記錄管理)
**當前狀態**: ✅ Phase 13 完成 - 六角形架構
**最後更新**: 2025-11-23

---

## 📋 模組概述

Visits 模組負責管理就診記錄（Visit），追蹤寵物與獸醫的就診歷史。本模組已完成六角形架構（Hexagonal Architecture）重構，實現了業務邏輯與框架的完全解耦，並使用 Port-Adapter 模式實現跨模組驗證。

### 核心職責

- 就診記錄管理（排程、查詢、完成、取消）
- 就診狀態追蹤（SCHEDULED, COMPLETED, CANCELLED）
- 跨模組驗證（驗證寵物與獸醫是否存在）
- 領域事件發布（VisitScheduled, VisitCompleted, VisitCancelled）

### 邊界上下文（Bounded Context）

本模組定義了「就診記錄管理」的邊界上下文，包含以下實體：

- **Visit (就診記錄)**: 聚合根，記錄寵物與獸醫的就診資訊
- **VisitStatus (就診狀態)**: 值物件，定義就診的生命週期狀態

---

## 🏗️ 架構設計

### 六角形架構（Ports & Adapters）

本模組遵循六角形架構模式，分為三個主要層級：

```
Infrastructure Layer (基礎設施層 - 框架依賴)
    ↓ 依賴於 ↓
Business Layer (業務層 - 純 Java，零框架依賴)
    ↓ 依賴於 ↓
Domain Layer (領域層 - 純 POJO)
```

### 目錄結構

```
visits/
├── Visit.java                              ← 公開 API (向後兼容)
├── VisitService.java                       ← 公開服務介面
├── VisitScheduled.java                     ← 領域事件
├── VisitCompleted.java                     ← 領域事件
├── VisitCancelled.java                     ← 領域事件
│
├── domain/                                 ← Domain 層 (純 Java)
│   ├── Visit.java                         ← 領域模型
│   └── VisitStatus.java                   ← 枚舉 (SCHEDULED, COMPLETED, CANCELLED)
│
├── business/                               ← Business 層 (純 Java)
│   ├── service/
│   │   └── VisitBusinessService.java      ← 業務服務（零框架依賴）
│   ├── port/
│   │   ├── VisitRepository.java           ← Repository Port
│   │   ├── EventPublisher.java            ← Event Publisher Port
│   │   ├── PetValidator.java              ← Pet 驗證 Port (跨模組)
│   │   └── VetValidator.java              ← Vet 驗證 Port (跨模組)
│   └── exception/
│       ├── VisitNotFoundException.java     ← 業務異常
│       └── InvalidVisitException.java      ← 業務異常
│
├── infrastructure/                         ← Infrastructure 層 (框架代碼)
│   ├── persistence/
│   │   ├── entity/
│   │   │   └── VisitEntity.java           ← JPA 實體
│   │   ├── jpa/
│   │   │   └── VisitJpaRepository.java    ← Spring Data JPA
│   │   ├── adapter/
│   │   │   └── VisitRepositoryAdapter.java ← Port 實現
│   │   └── mapper/
│   │       └── DomainMapper.java           ← 三向轉換 (Domain ↔ Entity ↔ Legacy)
│   ├── event/
│   │   └── SpringEventPublisherAdapter.java ← Event Publisher 實現
│   ├── validator/
│   │   ├── CustomerServicePetValidator.java  ← PetValidator 實現 (跨模組)
│   │   └── VetServiceVetValidator.java       ← VetValidator 實現 (跨模組)
│   └── config/
│       └── VisitBusinessConfiguration.java  ← 業務服務配置
│
└── internal/                               ← Service 層 (向後兼容)
    ├── VisitServiceImpl.java               ← 委派給 Business Service
    └── web/
        └── VisitResource.java              ← REST 控制器
```

---

## 🔌 公開 API

### 公開服務介面

```java
public interface VisitService {
    Optional<Visit> findById(Integer id);
    List<Visit> findAll();
    List<Visit> findByPetId(Integer petId);
    List<Visit> findByVetId(Integer vetId);
    Visit scheduleVisit(Visit visit);
    Visit completeVisit(Integer visitId);
    void cancelVisit(Integer visitId);
}
```

### 領域事件

本模組發布以下領域事件供其他模組監聽：

- **VisitScheduled**: 當新就診記錄排程時發布
- **VisitCompleted**: 當就診記錄完成時發布
- **VisitCancelled**: 當就診記錄取消時發布

---

## 📊 領域模型

### Visit (就診記錄)

**屬性**:
- `id`: Integer - 就診記錄唯一識別碼
- `petId`: Integer - 寵物 ID (外鍵至 Customers 模組)
- `vetId`: Integer - 獸醫 ID (外鍵至 Vets 模組)
- `visitDate`: LocalDateTime - 就診日期時間
- `description`: String - 就診描述
- `status`: VisitStatus - 就診狀態

**業務規則**:
- 必須關聯有效的寵物（跨模組驗證）
- 必須關聯有效的獸醫（跨模組驗證）
- 就診日期不可為空
- 狀態轉換規則：SCHEDULED → COMPLETED 或 CANCELLED

**業務方法**:
```java
// 領域模型中的業務邏輯
public void schedule() {
    this.status = VisitStatus.SCHEDULED;
}

public void complete() {
    if (this.status != VisitStatus.SCHEDULED) {
        throw new InvalidVisitException("Only scheduled visits can be completed");
    }
    this.status = VisitStatus.COMPLETED;
}

public void cancel() {
    if (this.status == VisitStatus.COMPLETED) {
        throw new InvalidVisitException("Completed visits cannot be cancelled");
    }
    this.status = VisitStatus.CANCELLED;
}
```

### VisitStatus (就診狀態)

**枚舉值**:
- `SCHEDULED`: 已排程（預設狀態）
- `COMPLETED`: 已完成
- `CANCELLED`: 已取消

**狀態轉換圖**:
```
SCHEDULED ──> COMPLETED
    └────────> CANCELLED

COMPLETED (終止狀態，不可變更)
CANCELLED (終止狀態，不可變更)
```

---

## 🔧 Business 層實現

### VisitBusinessService

純 Java 業務服務，實現核心業務邏輯，零框架依賴。

**主要方法**:

```java
public class VisitBusinessService {
    // 查詢就診記錄
    public Optional<org.springframework.samples.petclinic.visits.domain.Visit>
        findById(Integer id);

    // 查詢所有就診記錄
    public List<org.springframework.samples.petclinic.visits.domain.Visit> findAll();

    // 查詢特定寵物的就診記錄
    public List<org.springframework.samples.petclinic.visits.domain.Visit>
        findByPetId(Integer petId);

    // 查詢特定獸醫的就診記錄
    public List<org.springframework.samples.petclinic.visits.domain.Visit>
        findByVetId(Integer vetId);

    // 排程就診
    public org.springframework.samples.petclinic.visits.domain.Visit
        scheduleVisit(org.springframework.samples.petclinic.visits.domain.Visit visit);

    // 完成就診
    public org.springframework.samples.petclinic.visits.domain.Visit
        completeVisit(Integer visitId);

    // 取消就診
    public void cancelVisit(Integer visitId);
}
```

**驗證邏輯**:
- 排程時驗證寵物存在（透過 PetValidator port）
- 排程時驗證獸醫存在（透過 VetValidator port）
- 驗證就診日期不可為空
- 驗證狀態轉換的合法性

---

## 🏛️ Infrastructure 層實現

### Repository Adapter

**VisitRepositoryAdapter** 實現 `VisitRepository` port，使用 JPA 進行資料持久化。

**職責**:
- 將領域模型轉換為 JPA 實體
- 執行資料庫操作（包含按 petId/vetId 查詢）
- 將 JPA 實體轉換回領域模型

### Event Publisher Adapter

**SpringEventPublisherAdapter** 實現 `EventPublisher` port，使用 Spring 的 `ApplicationEventPublisher`。

**職責**:
- 發布領域事件至 Spring 事件系統
- 確保事件被儲存至 `event_publication` 表

### Validator Adapters (跨模組驗證)

**CustomerServicePetValidator** 實現 `PetValidator` port：

```java
@Component
public class CustomerServicePetValidator implements PetValidator {
    private final CustomerService customerService;

    @Override
    public boolean exists(Integer petId) {
        // 呼叫 Customers 模組驗證寵物是否存在
        return customerService.findPetById(petId).isPresent();
    }
}
```

**VetServiceVetValidator** 實現 `VetValidator` port：

```java
@Component
public class VetServiceVetValidator implements VetValidator {
    private final VetService vetService;

    @Override
    public boolean exists(Integer vetId) {
        // 呼叫 Vets 模組驗證獸醫是否存在
        return vetService.findById(vetId).isPresent();
    }
}
```

**設計優勢**:
- Business 層不依賴 Customers/Vets 模組
- 透過 Port-Adapter 模式實現鬆耦合的跨模組驗證
- 易於測試（可模擬 Validator）

### Domain Mapper

**DomainMapper** 負責三向轉換：

1. **Domain Model → JPA Entity** (儲存時)
2. **JPA Entity → Domain Model** (查詢時)
3. **Domain Model → Legacy Entity** (向後兼容)

---

## 🌐 REST API

### Visit Resource (就診記錄端點)

**Base Path**: `/visits`

#### 端點清單

| 方法 | 路徑 | 描述 | 請求體 | 響應 |
|------|------|------|--------|------|
| GET | `/visits` | 查詢所有就診記錄 | - | 200 OK, Visit[] |
| GET | `/visits/{id}` | 查詢單一就診記錄 | - | 200 OK, Visit / 404 Not Found |
| GET | `/visits?petId={petId}` | 查詢特定寵物的就診記錄 | - | 200 OK, Visit[] |
| GET | `/visits?vetId={vetId}` | 查詢特定獸醫的就診記錄 | - | 200 OK, Visit[] |
| POST | `/visits` | 排程新就診記錄 | VisitRequest | 201 Created, Location header |
| POST | `/visits/{id}/complete` | 完成就診 | - | 200 OK, Visit |
| POST | `/visits/{id}/cancel` | 取消就診 | - | 204 No Content |

**VisitRequest DTO**:
```json
{
  "petId": 1,
  "vetId": 1,
  "visitDate": "2024-01-15T10:00:00",
  "description": "定期健康檢查"
}
```

**Visit Response 範例**:
```json
{
  "id": 1,
  "petId": 1,
  "vetId": 1,
  "visitDate": "2024-01-15T10:00:00",
  "description": "定期健康檢查",
  "status": "SCHEDULED"
}
```

---

## 🧪 測試策略

### 測試覆蓋

本模組包含 **31 個測試**，涵蓋多個層級：

#### Business 層測試（20 個純 Java 單元測試）

**VisitBusinessServiceTest**:

**查詢操作（4 個測試）**:
- ✅ `shouldFindVisitById` - 查詢就診記錄成功
- ✅ `shouldReturnEmptyWhenVisitNotFound` - 就診記錄不存在時返回空
- ✅ `shouldThrowExceptionWhenFindByIdWithInvalidId` - 無效 ID 拋出異常
- ✅ `shouldFindAllVisits` - 查詢所有就診記錄

**寵物/獸醫查詢（4 個測試）**:
- ✅ `shouldFindVisitsByPetId` - 查詢寵物的就診記錄
- ✅ `shouldThrowExceptionWhenFindByPetIdWithInvalidId` - 無效寵物 ID 拋出異常
- ✅ `shouldFindVisitsByVetId` - 查詢獸醫的就診記錄
- ✅ `shouldThrowExceptionWhenFindByVetIdWithInvalidId` - 無效獸醫 ID 拋出異常

**排程就診（5 個測試）**:
- ✅ `shouldScheduleVisit` - 排程就診成功
- ✅ `shouldThrowExceptionWhenSchedulingNullVisit` - 排程 null 就診時拋出異常
- ✅ `shouldThrowExceptionWhenSchedulingVisitWithId` - 排程帶 ID 的就診時拋出異常
- ✅ `shouldThrowExceptionWhenPetNotFound` - 寵物不存在時拋出異常
- ✅ `shouldThrowExceptionWhenVetNotFound` - 獸醫不存在時拋出異常

**完成就診（3 個測試）**:
- ✅ `shouldCompleteVisit` - 完成就診成功
- ✅ `shouldThrowExceptionWhenCompletingNonExistentVisit` - 完成不存在的就診時拋出異常
- ✅ `shouldThrowExceptionWhenCompletingWithInvalidId` - 無效 ID 拋出異常

**取消就診（3 個測試）**:
- ✅ `shouldCancelVisit` - 取消就診成功
- ✅ `shouldThrowExceptionWhenCancellingNonExistentVisit` - 取消不存在的就診時拋出異常
- ✅ `shouldThrowExceptionWhenCancellingWithInvalidId` - 無效 ID 拋出異常

**跨模組驗證（1 個測試）**:
- ✅ `shouldValidateVisitOnSchedule` - 排程時驗證寵物與獸醫

**測試特點**:
- 純 Java 測試，不使用 Spring 上下文
- 使用 Mockito 模擬所有依賴（Repository, EventPublisher, Validators）
- 完整覆蓋 CRUD 操作、驗證邏輯、錯誤處理
- 測試邊界條件（null, invalid ID, not found）
- 跨模組驗證測試

#### Service 層測試（11 個整合測試）

**VisitServiceImplTest**:
- ✅ 查詢操作：findById, findAll, findByPetId, findByVetId
- ✅ 委派驗證：確保服務正確委派給 BusinessService
- ✅ 三向模型轉換：Domain Model ↔ Entity ↔ Legacy Entity
- ✅ 異常翻譯：InvalidVisitException → ResourceNotFoundException

#### API 層測試

**VisitResourceTest**:
- ✅ GET `/visits` - 查詢所有就診記錄
- ✅ GET `/visits/{id}` - 查詢單一就診記錄
- ✅ GET `/visits?petId={petId}` - 按寵物查詢
- ✅ GET `/visits?vetId={vetId}` - 按獸醫查詢
- ✅ POST `/visits` - 排程就診
- ✅ POST `/visits/{id}/complete` - 完成就診
- ✅ POST `/visits/{id}/cancel` - 取消就診

### 執行測試

```bash
# 執行所有 Visits 模組測試
../mvnw test -Dtest="org.springframework.samples.petclinic.visits.**.*Test"

# 只執行 Business 層測試
../mvnw test -Dtest="VisitBusinessServiceTest"

# 只執行 Service 層測試
../mvnw test -Dtest="VisitServiceImplTest"

# 只執行 API 層測試
../mvnw test -Dtest="VisitResourceTest"
```

---

## 🔗 模組依賴

### 依賴的模組

- **Customers**: 透過 `PetValidator` port 驗證寵物存在性
- **Vets**: 透過 `VetValidator` port 驗證獸醫存在性
- **Shared**: 共用基礎設施（例外處理、配置）

**依賴方式**:
- ✅ 使用 Port-Adapter 模式（鬆耦合）
- ❌ 不直接依賴 Customers/Vets 模組的實體類別

### 被依賴的模組

目前沒有其他模組依賴 Visits 模組。

### 事件訂閱者

目前沒有其他模組監聽 Visits 模組的事件。

---

## 📊 資料庫 Schema

### visits 表

```sql
CREATE TABLE visits (
  id INTEGER PRIMARY KEY AUTO_INCREMENT,
  pet_id INTEGER NOT NULL,
  vet_id INTEGER NOT NULL,
  visit_date TIMESTAMP NOT NULL,
  description VARCHAR(8192),
  status VARCHAR(255) NOT NULL DEFAULT 'SCHEDULED',
  FOREIGN KEY (pet_id) REFERENCES pets(id),
  FOREIGN KEY (vet_id) REFERENCES vets(id),
  INDEX idx_pet_id (pet_id),
  INDEX idx_vet_id (vet_id),
  INDEX idx_status (status)
);
```

### 預載資料

系統啟動時會預載 4 筆歷史就診記錄作為測試資料。

---

## 📈 效能指標

### Micrometer 指標

本模組使用 `@Timed` 註解記錄效能指標：

- `petclinic.visit.schedule` - 排程就診耗時
- `petclinic.visit.complete` - 完成就診耗時
- `petclinic.visit.cancel` - 取消就診耗時
- `petclinic.visit.findAll` - 查詢所有就診耗時
- `petclinic.visit.findByPetId` - 按寵物查詢耗時
- `petclinic.visit.findByVetId` - 按獸醫查詢耗時

### 效能目標

- 讀取操作: < 200ms p95 延遲
- 寫入操作: < 500ms p95 延遲
- 支援 1000+ 並發使用者
- 跨模組驗證: < 50ms 額外延遲

---

## 🚀 未來改進

### 潛在優化

1. **快取策略**: 快取常查詢的就診記錄
2. **批次操作**: 支援批次排程/取消就診
3. **提醒功能**: 就診前自動提醒
4. **統計報表**: 就診統計與趨勢分析
5. **狀態追蹤**: 更詳細的就診流程狀態

### 架構演進

- ✅ Phase 13 完成：六角形架構重構
- ✅ Phase 14 完成：Business 層完整測試覆蓋
- ⏭️ 下一步：效能優化與快取策略

---

## 📚 相關文件

- [Spring PetClinic Modulith README](../../../README.md)
- [專案憲章 (Constitution)](../../../.specify/memory/constitution.md)
- [架構決策記錄 (ADR)](../../ARCHITECTURE_DECISIONS.md)
- [開發者指南](../../DEVELOPER_GUIDE.md)
- [Customers 模組文件](../customers/README.md)
- [Vets 模組文件](../vets/README.md)

---

**最後更新**: 2025-11-23
**維護者**: Spring PetClinic Modulith Team
**狀態**: ✅ 正式環境就緒（六角形架構）
