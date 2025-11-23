# 功能規格：Customers 模組（客戶管理）

**功能分支**: `001-customers-module`
**建立日期**: 2025-11-23
**狀態**: 已實現（Phase 16 完成）
**架構**: 六角形架構（Hexagonal Architecture）

---

## 使用者場景與測試 *(必填)*

### 使用者故事 1 - 客戶資訊管理 (優先級: P1)

診所接待人員需要管理客戶的基本資訊，包括姓名、地址、聯絡方式等，以便追蹤客戶資料並提供服務。

**此優先級的原因**: 客戶資料是診所運作的基礎，沒有客戶資料就無法提供任何服務。

**獨立測試**: 可透過建立新客戶、查詢客戶、更新客戶資訊、刪除客戶等操作獨立驗證，無需依賴其他模組。

**驗收場景**:

1. **Given** 接待人員在系統中，**When** 輸入新客戶的姓名、地址、城市、電話，**Then** 系統成功建立客戶記錄並返回客戶 ID
2. **Given** 系統中存在客戶記錄，**When** 接待人員搜尋客戶 ID，**Then** 系統顯示完整的客戶資訊
3. **Given** 客戶資訊需要更新，**When** 接待人員修改客戶的地址或電話，**Then** 系統成功更新並保存變更
4. **Given** 客戶不再使用診所服務，**When** 接待人員刪除客戶記錄，**Then** 系統移除該客戶及其關聯的寵物資料

---

### 使用者故事 2 - 寵物資訊管理 (優先級: P1)

客戶可以擁有一隻或多隻寵物，診所需要記錄每隻寵物的名稱、出生日期、類型等資訊。

**此優先級的原因**: 寵物資訊是就診服務的直接對象，屬於核心功能。

**獨立測試**: 可透過為客戶新增寵物、查詢寵物、更新寵物資訊等操作獨立驗證。

**驗收場景**:

1. **Given** 客戶已在系統中註冊，**When** 接待人員為該客戶新增寵物（名稱、出生日期、類型），**Then** 系統成功建立寵物記錄並關聯至客戶
2. **Given** 客戶擁有多隻寵物，**When** 接待人員查詢該客戶的寵物清單，**Then** 系統顯示所有寵物的資訊
3. **Given** 寵物資訊需要更正，**When** 接待人員更新寵物的名稱或類型，**Then** 系統成功儲存變更
4. **Given** 寵物已過世或不再由該客戶擁有，**When** 接待人員移除寵物記錄，**Then** 系統刪除該寵物並發布刪除事件

---

### 使用者故事 3 - 寵物類型管理 (優先級: P2)

診所需要維護寵物類型清單（貓、狗、鳥、倉鼠、蜥蜴、蛇），以便正確分類寵物。

**此優先級的原因**: 寵物類型是預定義資料，變動頻率低，優先級較低。

**獨立測試**: 可透過查詢寵物類型清單驗證。

**驗收場景**:

1. **Given** 接待人員新增寵物，**When** 選擇寵物類型，**Then** 系統提供預定義的寵物類型選項（cat, dog, bird, hamster, lizard, snake）
2. **Given** 系統中已有寵物類型清單，**When** 接待人員查詢寵物類型，**Then** 系統返回所有可用的類型

---

### 邊界案例

- 當客戶姓名為空或只有空格時，系統應拒絕建立並返回驗證錯誤
- 當客戶電話號碼格式不正確時，系統應提示格式錯誤
- 當嘗試刪除不存在的客戶時，系統應返回 404 錯誤
- 當客戶擁有寵物時被刪除，系統應級聯刪除所有關聯的寵物
- 當寵物出生日期晚於當前日期時，系統應拒絕並提示錯誤

---

## 需求 *(必填)*

### 功能需求

- **FR-001**: 系統必須允許使用者建立新客戶，包含姓名、地址、城市、電話等資訊
- **FR-002**: 系統必須允許使用者查詢單一客戶或所有客戶
- **FR-003**: 系統必須允許使用者更新現有客戶的資訊
- **FR-004**: 系統必須允許使用者刪除客戶記錄
- **FR-005**: 系統必須驗證客戶姓名不可為空
- **FR-006**: 系統必須驗證客戶地址、城市、電話為必填欄位
- **FR-007**: 系統必須允許使用者為客戶新增寵物
- **FR-008**: 系統必須允許使用者查詢客戶的所有寵物
- **FR-009**: 系統必須允許使用者更新寵物資訊
- **FR-010**: 系統必須允許使用者刪除寵物記錄
- **FR-011**: 系統必須驗證寵物名稱不可為空
- **FR-012**: 系統必須驗證寵物必須關聯至有效的客戶
- **FR-013**: 系統必須驗證寵物必須指定有效的類型
- **FR-014**: 系統必須在客戶建立、更新、刪除時發布領域事件
- **FR-015**: 系統必須在寵物新增、刪除時發布領域事件
- **FR-016**: 系統必須提供預定義的寵物類型清單（cat, dog, bird, hamster, lizard, snake）
- **FR-017**: 系統必須將客戶與寵物資料持久化至資料庫
- **FR-018**: 系統必須支援級聯刪除（刪除客戶時同時刪除其寵物）

### 關鍵實體

- **Customer（客戶）**:
  - 屬性：id, firstName, lastName, address, city, telephone
  - 關係：一個客戶可擁有多隻寵物（一對多）
  - 聚合根

- **Pet（寵物）**:
  - 屬性：id, name, birthDate, type, owner
  - 關係：每隻寵物屬於一個客戶
  - 實體

- **PetType（寵物類型）**:
  - 屬性：id, name
  - 預定義值：cat, dog, bird, hamster, lizard, snake
  - 值物件

---

## 成功標準 *(必填)*

### 可衡量的結果

- **SC-001**: 使用者可在 5 秒內完成新客戶註冊（包含基本資訊輸入與驗證）
- **SC-002**: 系統可在 200ms 內返回單一客戶的查詢結果（p95 延遲）
- **SC-003**: 系統可在 500ms 內完成客戶資訊更新（p95 延遲）
- **SC-004**: 系統支援至少 1000 個並發使用者同時查詢客戶資訊
- **SC-005**: 95% 的客戶建立操作在第一次嘗試時成功完成（驗證錯誤除外）
- **SC-006**: 系統在客戶刪除後立即發布刪除事件，GenAI 模組在 5 秒內完成同步
- **SC-007**: 100% 的業務邏輯測試通過（13 個純 Java 單元測試）
- **SC-008**: 100% 的服務層委派測試通過（8 個整合測試）
- **SC-009**: 所有 REST API 端點返回正確的 HTTP 狀態碼（200, 201, 204, 404）
- **SC-010**: 系統可在 30 秒內完成啟動並準備好處理請求

---

## 架構需求 *(此模組特定)*

### 六角形架構遵循

- **AR-001**: 業務邏輯必須與框架完全解耦（零 Spring/JPA 依賴）
- **AR-002**: 領域模型必須為純 POJO，無任何註解
- **AR-003**: 業務層必須定義 Port 介面（CustomerRepository, EventPublisher）
- **AR-004**: 基礎設施層必須實現 Adapter（CustomerRepositoryAdapter, SpringEventPublisherAdapter）
- **AR-005**: 領域模型與 JPA 實體必須透過 Mapper 進行轉換
- **AR-006**: 所有業務邏輯必須位於領域模型或業務服務中
- **AR-007**: REST 控制器只負責 HTTP 協議處理，不包含業務邏輯

### 測試需求

- **TR-001**: 業務層測試必須為純 Java 單元測試（不使用 Spring 上下文）
- **TR-002**: 服務層測試必須驗證委派至業務服務的正確性
- **TR-003**: API 層測試必須使用 MockMvc 驗證 HTTP 契約
- **TR-004**: 測試必須涵蓋 CRUD 操作、驗證邏輯、錯誤處理
- **TR-005**: 測試必須涵蓋邊界條件（null 檢查、無效 ID、不存在的實體）

---

## 領域事件 *(此模組特定)*

### 發布的事件

- **CustomerCreated**: 當新客戶建立時發布
  - 屬性：customerId
  - 訂閱者：GenAI 模組（同步客戶資料至向量儲存）

- **CustomerUpdated**: 當客戶資訊更新時發布
  - 屬性：customerId
  - 訂閱者：GenAI 模組（更新向量儲存中的客戶資料）

- **CustomerDeleted**: 當客戶被刪除時發布
  - 屬性：customerId
  - 訂閱者：GenAI 模組（從向量儲存移除客戶資料）

- **PetAdded**: 當新寵物加入時發布
  - 屬性：petId, customerId
  - 訂閱者：GenAI 模組（同步寵物資料）

- **PetDeleted**: 當寵物被移除時發布
  - 屬性：petId, customerId
  - 訂閱者：GenAI 模組（移除寵物資料）

---

## REST API 契約 *(此模組特定)*

### 端點清單

| 方法 | 路徑 | 描述 | 請求體 | 成功響應 | 錯誤響應 |
|------|------|------|--------|---------|---------|
| GET | `/owners` | 查詢所有客戶 | - | 200 OK, Customer[] | - |
| GET | `/owners/{id}` | 查詢單一客戶 | - | 200 OK, Customer | 404 Not Found |
| POST | `/owners` | 建立新客戶 | OwnerRequest | 201 Created, Location header | 400 Bad Request |
| PUT | `/owners/{id}` | 更新客戶資訊 | OwnerRequest | 204 No Content | 404 Not Found, 400 Bad Request |
| GET | `/owners/{ownerId}/pets` | 查詢客戶的寵物 | - | 200 OK, Pet[] | 404 Not Found |
| POST | `/owners/{ownerId}/pets` | 新增寵物 | PetRequest | 201 Created | 400 Bad Request, 404 Not Found |
| PUT | `/owners/{ownerId}/pets/{petId}` | 更新寵物資訊 | PetRequest | 204 No Content | 404 Not Found, 400 Bad Request |

---

## 資料庫 Schema *(此模組特定)*

### owners 表

```sql
CREATE TABLE owners (
  id INTEGER PRIMARY KEY AUTO_INCREMENT,
  first_name VARCHAR(255) NOT NULL,
  last_name VARCHAR(255) NOT NULL,
  address VARCHAR(255) NOT NULL,
  city VARCHAR(255) NOT NULL,
  telephone VARCHAR(255) NOT NULL
);
```

### pets 表

```sql
CREATE TABLE pets (
  id INTEGER PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  birth_date DATE,
  owner_id INTEGER NOT NULL,
  type_id INTEGER NOT NULL,
  FOREIGN KEY (owner_id) REFERENCES owners(id) ON DELETE CASCADE,
  FOREIGN KEY (type_id) REFERENCES types(id)
);
```

### types 表

```sql
CREATE TABLE types (
  id INTEGER PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL UNIQUE
);
```

---

## 假設與約束

### 假設

- 客戶電話號碼使用標準格式（無需國際格式）
- 寵物出生日期可為空（未知出生日期）
- 寵物類型為預定義清單，不支援動態新增
- 客戶刪除會級聯刪除所有寵物（業務規則）

### 約束

- 業務層不可依賴任何 Spring 或 JPA 框架
- 所有資料變更必須發布領域事件
- REST API 必須返回適當的 HTTP 狀態碼
- 資料驗證必須在業務層執行（不僅在 API 層）

---

## 依賴關係

### 依賴的模組

- **Shared**: 共用例外類別（ResourceNotFoundException）、全域配置

### 被依賴的模組

- **Visits**: 就診模組透過 `PetValidator` port 驗證寵物存在性
- **GenAI**: AI 模組監聽客戶與寵物事件，同步資料至向量儲存

---

## 實現狀態

- ✅ **Phase 16 完成**: 六角形架構重構
- ✅ **測試覆蓋**: 21 個測試全部通過（13 Business + 8 Service）
- ✅ **API 文件**: 已整合 OpenAPI/Swagger
- ✅ **效能指標**: Micrometer `@Timed` 註解已加入
- ✅ **事件發布**: 所有領域事件已實現
- ✅ **資料庫**: MySQL 與 HSQLDB 支援

---

**最後更新**: 2025-11-23
**維護者**: Spring PetClinic Modulith Team
**相關文件**: [Customers 模組文件](../../../spring-petclinic-modulith/docs/modules/customers/README.md)
