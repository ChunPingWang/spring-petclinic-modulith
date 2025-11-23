# Vets 模組文件

**模組名稱**: Vets (獸醫管理)
**當前狀態**: ✅ Phase 12 完成 - 六角形架構
**最後更新**: 2025-11-23

---

## 📋 模組概述

Vets 模組負責管理獸醫（Vet）與專業（Specialty）資訊，是 PetClinic 系統的核心模組之一。本模組已完成六角形架構（Hexagonal Architecture）重構，實現了業務邏輯與框架的完全解耦。

### 核心職責

- 獸醫資訊管理（新增、查詢、更新、刪除）
- 獸醫專業管理（查詢、關聯）
- 領域事件發布（VetCreated, VetUpdated, VetDeleted）
- 提供獸醫查詢服務供其他模組使用

### 邊界上下文（Bounded Context）

本模組定義了「獸醫管理」的邊界上下文，包含以下實體：

- **Vet (獸醫)**: 聚合根，包含獸醫基本資訊與專業
- **Specialty (專業)**: 值物件，定義獸醫專長領域

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
vets/
├── Vet.java                                ← 公開 API (向後兼容)
├── VetService.java                         ← 公開服務介面
├── VetCreated.java                         ← 領域事件
├── VetUpdated.java                         ← 領域事件
├── VetDeleted.java                         ← 領域事件
│
├── domain/                                 ← Domain 層 (純 Java)
│   ├── Vet.java                           ← 領域模型
│   └── Specialty.java                     ← 值物件
│
├── business/                               ← Business 層 (純 Java)
│   ├── service/
│   │   └── VetBusinessService.java        ← 業務服務（零框架依賴）
│   ├── port/
│   │   ├── VetRepository.java             ← Repository Port
│   │   └── EventPublisher.java            ← Event Publisher Port
│   └── exception/
│       └── VetNotFoundException.java       ← 業務異常
│
├── infrastructure/                         ← Infrastructure 層 (框架代碼)
│   ├── persistence/
│   │   ├── entity/
│   │   │   ├── VetEntity.java             ← JPA 實體
│   │   │   └── SpecialtyEntity.java
│   │   ├── jpa/
│   │   │   ├── VetJpaRepository.java      ← Spring Data JPA
│   │   │   └── SpecialtyJpaRepository.java
│   │   ├── adapter/
│   │   │   └── VetRepositoryAdapter.java   ← Port 實現
│   │   └── mapper/
│   │       └── DomainMapper.java           ← 領域模型 ↔ Entity 轉換
│   ├── event/
│   │   └── SpringEventPublisherAdapter.java ← Event Publisher 實現
│   └── config/
│       └── VetBusinessConfiguration.java   ← 業務服務配置
│
└── internal/                               ← Service 層 (向後兼容)
    ├── VetServiceImpl.java                 ← 委派給 Business Service
    └── web/
        └── VetResource.java                ← REST 控制器
```

---

## 🔌 公開 API

### 公開服務介面

```java
public interface VetService {
    Optional<Vet> findById(Integer id);
    List<Vet> findAll();
    Vet save(Vet vet);
    void deleteById(Integer id);
}
```

### 領域事件

本模組發布以下領域事件供其他模組監聽：

- **VetCreated**: 當新獸醫建立時發布
- **VetUpdated**: 當獸醫資訊更新時發布
- **VetDeleted**: 當獸醫被刪除時發布

---

## 📊 領域模型

### Vet (獸醫)

**屬性**:
- `id`: Integer - 獸醫唯一識別碼
- `firstName`: String - 名字
- `lastName`: String - 姓氏
- `specialties`: Set<Specialty> - 專業領域集合

**業務規則**:
- 姓名不可為空
- 一位獸醫可以擁有零個或多個專業
- 專業不可重複

**業務方法**:
```java
// 領域模型中的業務邏輯
public void validate() {
    if (firstName == null || firstName.isBlank()) {
        throw new IllegalArgumentException("First name is required");
    }
    if (lastName == null || lastName.isBlank()) {
        throw new IllegalArgumentException("Last name is required");
    }
}
```

### Specialty (專業)

**屬性**:
- `id`: Integer - 專業識別碼
- `name`: String - 專業名稱 (radiology, surgery, dentistry)

**業務規則**:
- 預定義的專業清單
- 專業名稱唯一

---

## 🔧 Business 層實現

### VetBusinessService

純 Java 業務服務，實現核心業務邏輯，零框架依賴。

**主要方法**:

```java
public class VetBusinessService {
    // 查詢獸醫
    public Optional<org.springframework.samples.petclinic.vets.domain.Vet>
        findById(Integer id);

    // 查詢所有獸醫
    public List<org.springframework.samples.petclinic.vets.domain.Vet> findAll();

    // 建立獸醫
    public org.springframework.samples.petclinic.vets.domain.Vet
        createVet(org.springframework.samples.petclinic.vets.domain.Vet vet);

    // 更新獸醫
    public org.springframework.samples.petclinic.vets.domain.Vet
        updateVet(Integer id, org.springframework.samples.petclinic.vets.domain.Vet vet);

    // 刪除獸醫
    public void deleteVet(Integer id);
}
```

**驗證邏輯**:
- 建立/更新時驗證獸醫資料完整性
- 驗證姓名欄位非空
- 檢查獸醫是否存在（更新/刪除時）
- 驗證專業的有效性

---

## 🏛️ Infrastructure 層實現

### Repository Adapter

**VetRepositoryAdapter** 實現 `VetRepository` port，使用 JPA 進行資料持久化。

**職責**:
- 將領域模型轉換為 JPA 實體
- 執行資料庫操作
- 將 JPA 實體轉換回領域模型
- 管理獸醫與專業的多對多關聯

### Event Publisher Adapter

**SpringEventPublisherAdapter** 實現 `EventPublisher` port，使用 Spring 的 `ApplicationEventPublisher`。

**職責**:
- 發布領域事件至 Spring 事件系統
- 確保事件被儲存至 `event_publication` 表

### Domain Mapper

**DomainMapper** 負責領域模型與 JPA 實體之間的轉換。

**轉換方向**:
- Domain Model → JPA Entity (儲存時)
- JPA Entity → Domain Model (查詢時)
- 處理 Vet ↔ Specialty 的多對多關聯

---

## 🌐 REST API

### Vet Resource (獸醫端點)

**Base Path**: `/vets`

#### 端點清單

| 方法 | 路徑 | 描述 | 請求體 | 響應 |
|------|------|------|--------|------|
| GET | `/vets` | 查詢所有獸醫 | - | 200 OK, Vet[] |
| GET | `/vets/{id}` | 查詢單一獸醫 | - | 200 OK, Vet / 404 Not Found |
| POST | `/vets` | 建立新獸醫 | VetRequest | 201 Created, Location header |
| PUT | `/vets/{id}` | 更新獸醫資訊 | VetRequest | 204 No Content / 404 Not Found |
| DELETE | `/vets/{id}` | 刪除獸醫 | - | 204 No Content / 404 Not Found |

**VetRequest DTO**:
```json
{
  "firstName": "陳",
  "lastName": "大明",
  "specialties": [
    {
      "id": 1,
      "name": "radiology"
    },
    {
      "id": 2,
      "name": "surgery"
    }
  ]
}
```

**Vet Response 範例**:
```json
{
  "id": 1,
  "firstName": "James",
  "lastName": "Carter",
  "specialties": [
    {
      "id": 1,
      "name": "radiology"
    }
  ]
}
```

---

## 🧪 測試策略

### 測試覆蓋

本模組包含 **19 個測試**，涵蓋多個層級：

#### Business 層測試（12 個純 Java 單元測試）

**VetBusinessServiceTest**:
- ✅ `shouldFindVetById` - 查詢獸醫成功
- ✅ `shouldReturnEmptyWhenVetNotFound` - 獸醫不存在時返回空
- ✅ `shouldFindAllVets` - 查詢所有獸醫
- ✅ `shouldCreateVet` - 建立獸醫成功
- ✅ `shouldThrowExceptionWhenCreatingVetWithId` - 建立帶 ID 的獸醫時拋出異常
- ✅ `shouldThrowExceptionWhenCreatingNullVet` - 建立 null 獸醫時拋出異常
- ✅ `shouldUpdateVet` - 更新獸醫成功
- ✅ `shouldThrowExceptionWhenUpdatingNonExistentVet` - 更新不存在的獸醫時拋出異常
- ✅ `shouldDeleteVet` - 刪除獸醫成功
- ✅ `shouldThrowExceptionWhenDeletingNonExistentVet` - 刪除不存在的獸醫時拋出異常
- ✅ `shouldValidateVetOnCreate` - 建立時驗證獸醫資料
- ✅ `shouldHandleVetWithSpecialties` - 正確處理獸醫專業

**測試特點**:
- 純 Java 測試，不使用 Spring 上下文
- 使用 Mockito 模擬所有依賴
- 測試涵蓋 CRUD 操作、驗證邏輯、錯誤處理
- 驗證專業管理邏輯

#### Service 層測試（8 個整合測試）

**VetServiceImplTest**:
- ✅ 委派驗證：確保服務正確委派給 BusinessService
- ✅ 領域模型轉換：驗證 Domain Model ↔ Legacy Entity 轉換
- ✅ 異常翻譯：驗證業務異常轉換為 Legacy 異常

#### API 層測試（7 個 REST 端點測試）

**VetResourceTest**:
- ✅ `shouldGetAllVets` - GET `/vets` 查詢所有獸醫
- ✅ `shouldGetVet` - GET `/vets/{id}` 查詢單一獸醫
- ✅ `shouldReturn404WhenVetNotFound` - GET 不存在的獸醫返回 404
- ✅ `shouldCreateNewVet` - POST `/vets` 建立新獸醫
- ✅ `shouldUpdateExistingVet` - PUT `/vets/{id}` 更新獸醫
- ✅ `shouldDeleteVet` - DELETE `/vets/{id}` 刪除獸醫
- ✅ `shouldReturn404WhenDeletingNonExistentVet` - DELETE 不存在的獸醫返回 404

### 執行測試

```bash
# 執行所有 Vets 模組測試
../mvnw test -Dtest="org.springframework.samples.petclinic.vets.**.*Test"

# 只執行 Business 層測試
../mvnw test -Dtest="VetBusinessServiceTest"

# 只執行 Service 層測試
../mvnw test -Dtest="VetServiceImplTest"

# 只執行 API 層測試
../mvnw test -Dtest="VetResourceTest"
```

---

## 🔗 模組依賴

### 依賴的模組

- **Shared**: 共用基礎設施（例外處理、配置）

### 被依賴的模組

- **Visits**: 就診模組需要驗證獸醫是否存在

### 事件訂閱者

目前沒有其他模組監聽 Vets 模組的事件。

---

## 📊 資料庫 Schema

### vets 表

```sql
CREATE TABLE vets (
  id INTEGER PRIMARY KEY AUTO_INCREMENT,
  first_name VARCHAR(255) NOT NULL,
  last_name VARCHAR(255) NOT NULL
);
```

### specialties 表

```sql
CREATE TABLE specialties (
  id INTEGER PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL UNIQUE
);
```

### vet_specialties 關聯表（多對多）

```sql
CREATE TABLE vet_specialties (
  vet_id INTEGER NOT NULL,
  specialty_id INTEGER NOT NULL,
  PRIMARY KEY (vet_id, specialty_id),
  FOREIGN KEY (vet_id) REFERENCES vets(id),
  FOREIGN KEY (specialty_id) REFERENCES specialties(id)
);
```

### 預載資料

系統啟動時會預載以下資料：

**獸醫**:
- ID 1: James Carter (無專業)
- ID 2: Helen Leary (專業：radiology)
- ID 3: Linda Douglas (專業：dentistry, surgery)
- ID 4: Rafael Ortega (專業：surgery)
- ID 5: Henry Stevens (專業：radiology)
- ID 6: Sharon Jenkins (無專業)

**專業**:
- ID 1: radiology (放射學)
- ID 2: surgery (外科)
- ID 3: dentistry (牙科)

---

## 📈 效能指標

### Micrometer 指標

本模組使用 `@Timed` 註解記錄效能指標：

- `petclinic.vet.create` - 建立獸醫耗時
- `petclinic.vet.update` - 更新獸醫耗時
- `petclinic.vet.delete` - 刪除獸醫耗時
- `petclinic.vet.findAll` - 查詢所有獸醫耗時
- `petclinic.vet.findById` - 查詢單一獸醫耗時

### 效能目標

- 讀取操作: < 200ms p95 延遲
- 寫入操作: < 500ms p95 延遲
- 支援 1000+ 並發使用者

---

## 🚀 未來改進

### 潛在優化

1. **快取策略**: 獸醫資料變動頻率低，適合加入快取
2. **專業管理**: 支援動態新增/刪除專業
3. **搜尋功能**: 加入獸醫姓名/專業的搜尋功能
4. **排程管理**: 整合獸醫的排程與可用時間

### 架構演進

- ✅ Phase 12 完成：六角形架構重構
- ✅ Phase 14 完成：Business 層完整測試覆蓋
- ⏭️ 下一步：效能優化與快取策略

---

## 📚 相關文件

- [Spring PetClinic Modulith README](../../../README.md)
- [專案憲章 (Constitution)](../../../.specify/memory/constitution.md)
- [架構決策記錄 (ADR)](../../ARCHITECTURE_DECISIONS.md)
- [開發者指南](../../DEVELOPER_GUIDE.md)

---

**最後更新**: 2025-11-23
**維護者**: Spring PetClinic Modulith Team
**狀態**: ✅ 正式環境就緒（六角形架構）
