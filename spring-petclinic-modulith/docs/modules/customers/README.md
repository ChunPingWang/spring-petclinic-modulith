# Customers 模組文件

**模組名稱**: Customers (客戶管理)
**當前狀態**: ✅ Phase 16 完成 - 六角形架構
**最後更新**: 2025-11-23

---

## 📋 模組概述

Customers 模組負責管理客戶（Owner）與寵物（Pet）的資訊，是 PetClinic 系統的核心模組之一。本模組已完成六角形架構（Hexagonal Architecture）重構，實現了業務邏輯與框架的完全解耦。

### 核心職責

- 客戶資訊管理（新增、查詢、更新、刪除）
- 寵物資訊管理（新增、查詢、更新、刪除）
- 寵物類型管理（查詢）
- 領域事件發布（CustomerCreated, CustomerUpdated, CustomerDeleted, PetAdded, PetDeleted）

### 邊界上下文（Bounded Context）

本模組定義了「客戶與寵物管理」的邊界上下文，包含以下聚合根（Aggregate Root）：

- **Customer (客戶)**: 聚合根，包含客戶基本資訊
- **Pet (寵物)**: 實體，屬於客戶聚合
- **PetType (寵物類型)**: 值物件，定義寵物種類

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
customers/
├── Customer.java                           ← 公開 API (向後兼容)
├── CustomerService.java                    ← 公開服務介面
├── CustomerCreated.java                    ← 領域事件
├── CustomerUpdated.java                    ← 領域事件
├── CustomerDeleted.java                    ← 領域事件
├── PetAdded.java                          ← 領域事件
├── PetDeleted.java                        ← 領域事件
│
├── business/                               ← Business 層 (純 Java)
│   ├── service/
│   │   └── CustomerBusinessService.java   ← 業務服務（零框架依賴）
│   ├── port/
│   │   ├── CustomerRepository.java        ← Repository Port
│   │   └── EventPublisher.java            ← Event Publisher Port
│   └── exception/
│       └── CustomerNotFoundException.java  ← 業務異常
│
├── infrastructure/                         ← Infrastructure 層 (框架代碼)
│   ├── persistence/
│   │   ├── entity/
│   │   │   ├── CustomerEntity.java        ← JPA 實體
│   │   │   ├── PetEntity.java
│   │   │   └── PetTypeEntity.java
│   │   ├── jpa/
│   │   │   └── CustomerJpaRepository.java  ← Spring Data JPA
│   │   ├── adapter/
│   │   │   └── CustomerRepositoryAdapter.java ← Port 實現
│   │   └── mapper/
│   │       └── DomainMapper.java           ← 領域模型 ↔ Entity 轉換
│   ├── event/
│   │   └── SpringEventPublisherAdapter.java ← Event Publisher 實現
│   └── config/
│       └── CustomerBusinessConfiguration.java ← 業務服務配置
│
└── internal/                               ← Service 層 (向後兼容)
    ├── CustomerServiceImpl.java            ← 委派給 Business Service
    ├── CustomerRepository.java             ← Legacy Repository
    ├── Pet.java                            ← Legacy 實體
    ├── PetRepository.java
    ├── PetType.java
    ├── PetTypeRepository.java
    └── web/
        ├── OwnerResource.java              ← REST 控制器
        ├── PetResource.java
        ├── OwnerRequest.java               ← DTO
        ├── PetRequest.java
        ├── PetDetails.java
        └── OwnerEntityMapper.java          ← Entity 映射
```

---

## 🔌 公開 API

### 公開服務介面

```java
public interface CustomerService {
    Optional<Customer> findById(Integer id);
    List<Customer> findAll();
    Customer save(Customer customer);
    void deleteById(Integer id);
}
```

### 領域事件

本模組發布以下領域事件供其他模組監聽：

- **CustomerCreated**: 當新客戶建立時發布
- **CustomerUpdated**: 當客戶資訊更新時發布
- **CustomerDeleted**: 當客戶被刪除時發布
- **PetAdded**: 當新寵物加入時發布
- **PetDeleted**: 當寵物被移除時發布

---

## 📊 領域模型

### Customer (客戶)

**屬性**:
- `id`: Integer - 客戶唯一識別碼
- `firstName`: String - 名字
- `lastName`: String - 姓氏
- `address`: String - 地址
- `city`: String - 城市
- `telephone`: String - 電話號碼

**業務規則**:
- 姓名不可為空
- 地址與城市為必填欄位
- 電話號碼須符合格式規範

### Pet (寵物)

**屬性**:
- `id`: Integer - 寵物唯一識別碼
- `name`: String - 寵物名稱
- `birthDate`: LocalDate - 出生日期
- `type`: PetType - 寵物類型
- `owner`: Customer - 所屬客戶

**業務規則**:
- 寵物名稱不可為空
- 必須關聯至有效的客戶
- 必須指定寵物類型

### PetType (寵物類型)

**屬性**:
- `id`: Integer - 類型識別碼
- `name`: String - 類型名稱 (cat, dog, bird, hamster, lizard, snake)

**業務規則**:
- 預定義的寵物類型清單
- 類型名稱唯一

---

## 🔧 Business 層實現

### CustomerBusinessService

純 Java 業務服務，實現核心業務邏輯，零框架依賴。

**主要方法**:

```java
public class CustomerBusinessService {
    // 查詢客戶
    public Optional<org.springframework.samples.petclinic.customers.domain.Customer>
        findById(Integer id);

    // 查詢所有客戶
    public List<org.springframework.samples.petclinic.customers.domain.Customer> findAll();

    // 建立客戶
    public org.springframework.samples.petclinic.customers.domain.Customer
        createCustomer(org.springframework.samples.petclinic.customers.domain.Customer customer);

    // 更新客戶
    public org.springframework.samples.petclinic.customers.domain.Customer
        updateCustomer(Integer id, org.springframework.samples.petclinic.customers.domain.Customer customer);

    // 刪除客戶
    public void deleteCustomer(Integer id);
}
```

**驗證邏輯**:
- 建立/更新時驗證客戶資料完整性
- 驗證電話號碼格式
- 檢查客戶是否存在（更新/刪除時）

---

## 🏛️ Infrastructure 層實現

### Repository Adapter

**CustomerRepositoryAdapter** 實現 `CustomerRepository` port，使用 JPA 進行資料持久化。

**職責**:
- 將領域模型轉換為 JPA 實體
- 執行資料庫操作
- 將 JPA 實體轉換回領域模型

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
- Domain Model → Legacy Entity (向後兼容)

---

## 🌐 REST API

### Owner Resource (客戶端點)

**Base Path**: `/owners`

#### 端點清單

| 方法 | 路徑 | 描述 | 請求體 | 響應 |
|------|------|------|--------|------|
| GET | `/owners` | 查詢所有客戶 | - | 200 OK, Customer[] |
| GET | `/owners/{id}` | 查詢單一客戶 | - | 200 OK, Customer / 404 Not Found |
| POST | `/owners` | 建立新客戶 | OwnerRequest | 201 Created, Location header |
| PUT | `/owners/{id}` | 更新客戶資訊 | OwnerRequest | 204 No Content / 404 Not Found |

**OwnerRequest DTO**:
```json
{
  "firstName": "王",
  "lastName": "小明",
  "address": "台北市信義區信義路五段7號",
  "city": "台北",
  "telephone": "0912345678"
}
```

### Pet Resource (寵物端點)

**Base Path**: `/owners/{ownerId}/pets`

#### 端點清單

| 方法 | 路徑 | 描述 | 請求體 | 響應 |
|------|------|------|--------|------|
| GET | `/owners/{ownerId}/pets` | 查詢客戶的所有寵物 | - | 200 OK, Pet[] |
| POST | `/owners/{ownerId}/pets` | 新增寵物 | PetRequest | 201 Created |
| PUT | `/owners/{ownerId}/pets/{petId}` | 更新寵物資訊 | PetRequest | 204 No Content |

**PetRequest DTO**:
```json
{
  "name": "小黑",
  "birthDate": "2020-05-15",
  "type": {
    "id": 1,
    "name": "cat"
  }
}
```

---

## 🧪 測試策略

### 測試覆蓋

本模組包含 **21 個測試**，涵蓋多個層級：

#### Business 層測試（13 個純 Java 單元測試）

**CustomerBusinessServiceTest**:
- ✅ `shouldFindCustomerById` - 查詢客戶成功
- ✅ `shouldReturnEmptyWhenCustomerNotFound` - 客戶不存在時返回空
- ✅ `shouldFindAllCustomers` - 查詢所有客戶
- ✅ `shouldCreateCustomer` - 建立客戶成功
- ✅ `shouldThrowExceptionWhenCreatingNullCustomer` - 建立 null 客戶時拋出異常
- ✅ `shouldThrowExceptionWhenCreatingCustomerWithId` - 建立帶 ID 的客戶時拋出異常
- ✅ `shouldUpdateCustomer` - 更新客戶成功
- ✅ `shouldThrowExceptionWhenUpdatingNonExistentCustomer` - 更新不存在的客戶時拋出異常
- ✅ `shouldDeleteCustomer` - 刪除客戶成功
- ✅ `shouldThrowExceptionWhenDeletingNonExistentCustomer` - 刪除不存在的客戶時拋出異常
- ✅ `shouldValidateCustomerDataOnCreate` - 建立時驗證客戶資料
- ✅ `shouldValidateCustomerDataOnUpdate` - 更新時驗證客戶資料
- ✅ `shouldPublishEventsOnCustomerOperations` - 操作時發布領域事件

**測試特點**:
- 純 Java 測試，不使用 Spring 上下文
- 使用 Mockito 模擬所有依賴
- 測試涵蓋 CRUD 操作、驗證邏輯、錯誤處理
- 驗證事件發布行為

#### Service 層測試（8 個整合測試）

**CustomerServiceImplTest**:
- ✅ 委派驗證：確保服務正確委派給 BusinessService
- ✅ 領域模型轉換：驗證 Domain Model ↔ Legacy Entity 轉換
- ✅ 異常翻譯：驗證業務異常轉換為 Legacy 異常

#### API 層測試

**OwnerResourceTest**:
- ✅ GET `/owners` - 查詢所有客戶
- ✅ GET `/owners/{id}` - 查詢單一客戶
- ✅ POST `/owners` - 建立客戶
- ✅ PUT `/owners/{id}` - 更新客戶

**PetResourceTest**:
- ✅ GET `/owners/{ownerId}/pets` - 查詢寵物
- ✅ POST `/owners/{ownerId}/pets` - 新增寵物
- ✅ PUT `/owners/{ownerId}/pets/{petId}` - 更新寵物

### 執行測試

```bash
# 執行所有 Customers 模組測試
../mvnw test -Dtest="org.springframework.samples.petclinic.customers.**.*Test"

# 只執行 Business 層測試
../mvnw test -Dtest="CustomerBusinessServiceTest"

# 只執行 API 層測試
../mvnw test -Dtest="OwnerResourceTest,PetResourceTest"
```

---

## 🔗 模組依賴

### 依賴的模組

- **Shared**: 共用基礎設施（例外處理、配置）

### 被依賴的模組

- **Visits**: 就診模組需要驗證客戶與寵物是否存在
- **GenAI**: AI 模組監聽客戶事件以更新向量儲存

### 事件訂閱者

其他模組透過 `@ApplicationModuleListener` 監聽本模組發布的事件：

```java
// GenAI 模組監聽客戶建立事件
@ApplicationModuleListener
void on(CustomerCreated event) {
    updateVectorStore(event.customerId());
}
```

---

## 📈 效能指標

### Micrometer 指標

本模組使用 `@Timed` 註解記錄效能指標：

- `petclinic.owner.create` - 建立客戶耗時
- `petclinic.owner.update` - 更新客戶耗時
- `petclinic.owner.findAll` - 查詢所有客戶耗時
- `petclinic.pet.create` - 新增寵物耗時
- `petclinic.pet.update` - 更新寵物耗時

### 效能目標

- 讀取操作: < 200ms p95 延遲
- 寫入操作: < 500ms p95 延遲
- 支援 1000+ 並發使用者

---

## 🚀 未來改進

### 潛在優化

1. **快取策略**: 考慮為常用查詢加入快取層
2. **批次操作**: 支援批次建立/更新寵物
3. **搜尋功能**: 加入客戶姓名/電話模糊搜尋
4. **分頁支援**: 大量客戶時的分頁查詢

### 架構演進

- ✅ Phase 16 完成：六角形架構重構
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
