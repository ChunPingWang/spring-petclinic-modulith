# 功能規格：Vets 模組（獸醫管理）

**功能分支**: `002-vets-module`
**建立日期**: 2025-11-23
**狀態**: 已實現（Phase 12 完成）
**架構**: 六角形架構（Hexagonal Architecture）

---

## 使用者場景與測試 *(必填)*

### 使用者故事 1 - 獸醫資訊管理 (優先級: P1)

診所管理員需要管理獸醫的基本資訊（姓名、專業領域），以便安排就診服務並向客戶展示可用的獸醫。

**此優先級的原因**: 獸醫是提供醫療服務的核心資源，沒有獸醫資料就無法安排就診。

**獨立測試**: 可透過建立獸醫、查詢獸醫、更新獸醫資訊、刪除獸醫等操作獨立驗證，無需依賴其他模組。

**驗收場景**:

1. **Given** 管理員在系統中，**When** 輸入新獸醫的姓名和專業領域，**Then** 系統成功建立獸醫記錄並返回獸醫 ID
2. **Given** 系統中存在獸醫記錄，**When** 管理員查詢所有獸醫，**Then** 系統顯示獸醫清單及其專業資訊
3. **Given** 獸醫取得新的專業認證，**When** 管理員更新獸醫的專業領域，**Then** 系統成功儲存變更
4. **Given** 獸醫離職，**When** 管理員刪除獸醫記錄，**Then** 系統移除該獸醫資料

---

### 使用者故事 2 - 獸醫專業管理 (優先級: P1)

獸醫可以擁有一個或多個專業認證（放射學、外科、牙科），系統需要追蹤每位獸醫的專業領域。

**此優先級的原因**: 專業資訊對於安排適當的獸醫至關重要，屬於核心功能。

**獨立測試**: 可透過為獸醫指定專業、查詢獸醫專業、移除專業等操作獨立驗證。

**驗收場景**:

1. **Given** 獸醫已在系統中註冊，**When** 管理員為該獸醫新增專業（radiology, surgery, dentistry），**Then** 系統成功關聯專業至獸醫
2. **Given** 獸醫擁有多個專業，**When** 使用者查詢該獸醫，**Then** 系統顯示所有專業認證
3. **Given** 獸醫不再執業某專業，**When** 管理員移除該專業，**Then** 系統更新獸醫的專業清單

---

### 使用者故事 3 - 獸醫查詢與展示 (優先級: P2)

客戶需要能夠瀏覽診所的獸醫清單，了解每位獸醫的姓名和專業，以便選擇合適的獸醫。

**此優先級的原因**: 查詢功能對使用者體驗重要，但優先級略低於基本 CRUD 操作。

**獨立測試**: 可透過查詢獸醫清單、按專業篩選等操作驗證。

**驗收場景**:

1. **Given** 系統中有多位獸醫，**When** 客戶瀏覽獸醫清單，**Then** 系統顯示所有獸醫的姓名和專業
2. **Given** 客戶需要特定專業的獸醫，**When** 客戶篩選專業（如「外科」），**Then** 系統只顯示擁有該專業的獸醫

---

### 邊界案例

- 當獸醫姓名為空或只有空格時，系統應拒絕建立並返回驗證錯誤
- 當嘗試刪除不存在的獸醫時，系統應返回 404 錯誤
- 當獸醫擁有進行中的就診記錄時被刪除，系統應拒絕刪除或提示警告
- 當為獸醫指定不存在的專業時，系統應拒絕並提示錯誤
- 當獸醫已擁有某專業時重複新增，系統應避免重複

---

## 需求 *(必填)*

### 功能需求

- **FR-001**: 系統必須允許使用者建立新獸醫，包含姓名資訊
- **FR-002**: 系統必須允許使用者查詢單一獸醫或所有獸醫
- **FR-003**: 系統必須允許使用者更新現有獸醫的資訊
- **FR-004**: 系統必須允許使用者刪除獸醫記錄
- **FR-005**: 系統必須驗證獸醫姓名不可為空
- **FR-006**: 系統必須允許為獸醫指定零個或多個專業
- **FR-007**: 系統必須防止為同一獸醫重複新增相同的專業
- **FR-008**: 系統必須提供預定義的專業清單（radiology, surgery, dentistry）
- **FR-009**: 系統必須在獸醫建立、更新、刪除時發布領域事件
- **FR-010**: 系統必須將獸醫與專業資料持久化至資料庫
- **FR-011**: 系統必須支援獸醫與專業的多對多關聯
- **FR-012**: 系統必須允許查詢擁有特定專業的獸醫清單

### 關鍵實體

- **Vet（獸醫）**:
  - 屬性：id, firstName, lastName, specialties
  - 關係：一個獸醫可擁有多個專業（多對多）
  - 聚合根

- **Specialty（專業）**:
  - 屬性：id, name
  - 預定義值：radiology（放射學）, surgery（外科）, dentistry（牙科）
  - 值物件

---

## 成功標準 *(必填)*

### 可衡量的結果

- **SC-001**: 使用者可在 3 秒內完成新獸醫註冊（包含基本資訊與專業選擇）
- **SC-002**: 系統可在 200ms 內返回獸醫清單查詢結果（p95 延遲）
- **SC-003**: 系統可在 500ms 內完成獸醫資訊更新（p95 延遲）
- **SC-004**: 系統支援至少 1000 個並發使用者同時查詢獸醫資訊
- **SC-005**: 95% 的獸醫建立操作在第一次嘗試時成功完成
- **SC-006**: 系統在獸醫刪除後立即發布刪除事件，GenAI 模組在 5 秒內完成同步
- **SC-007**: 100% 的業務邏輯測試通過（12 個純 Java 單元測試）
- **SC-008**: 100% 的服務層委派測試通過（8 個整合測試）
- **SC-009**: 所有 REST API 端點返回正確的 HTTP 狀態碼（200, 201, 204, 404）
- **SC-010**: 獸醫清單顯示速度優於客戶清單（因資料量較小）

---

## 架構需求 *(此模組特定)*

### 六角形架構遵循

- **AR-001**: 業務邏輯必須與框架完全解耦（零 Spring/JPA 依賴）
- **AR-002**: 領域模型必須為純 POJO，包含業務邏輯方法（如 `validate()`）
- **AR-003**: 業務層必須定義 Port 介面（VetRepository, EventPublisher）
- **AR-004**: 基礎設施層必須實現 Adapter（VetRepositoryAdapter, SpringEventPublisherAdapter）
- **AR-005**: 領域模型與 JPA 實體必須透過 DomainMapper 進行轉換
- **AR-006**: 專業（Specialty）的多對多關聯必須在基礎設施層處理
- **AR-007**: REST 控制器只負責 HTTP 協議處理，不包含業務邏輯

### 測試需求

- **TR-001**: 業務層測試必須為純 Java 單元測試（不使用 Spring 上下文）
- **TR-002**: 測試必須涵蓋獸醫的專業管理邏輯
- **TR-003**: 測試必須驗證專業不重複的業務規則
- **TR-004**: API 層測試必須包含完整的 CRUD 操作（GET, POST, PUT, DELETE）
- **TR-005**: 測試必須涵蓋 404 錯誤場景（查詢/刪除不存在的獸醫）

---

## 領域事件 *(此模組特定)*

### 發布的事件

- **VetCreated**: 當新獸醫建立時發布
  - 屬性：vetId
  - 訂閱者：GenAI 模組（同步獸醫資料至向量儲存）

- **VetUpdated**: 當獸醫資訊或專業更新時發布
  - 屬性：vetId
  - 訂閱者：GenAI 模組（更新向量儲存中的獸醫資料）

- **VetDeleted**: 當獸醫被刪除時發布
  - 屬性：vetId
  - 訂閱者：GenAI 模組（從向量儲存移除獸醫資料）

---

## REST API 契約 *(此模組特定)*

### 端點清單

| 方法 | 路徑 | 描述 | 請求體 | 成功響應 | 錯誤響應 |
|------|------|------|--------|---------|---------|
| GET | `/vets` | 查詢所有獸醫 | - | 200 OK, Vet[] | - |
| GET | `/vets/{id}` | 查詢單一獸醫 | - | 200 OK, Vet | 404 Not Found |
| POST | `/vets` | 建立新獸醫 | VetRequest | 201 Created, Location header | 400 Bad Request |
| PUT | `/vets/{id}` | 更新獸醫資訊 | VetRequest | 204 No Content | 404 Not Found, 400 Bad Request |
| DELETE | `/vets/{id}` | 刪除獸醫 | - | 204 No Content | 404 Not Found |

### VetRequest DTO 範例

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

---

## 資料庫 Schema *(此模組特定)*

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
  FOREIGN KEY (vet_id) REFERENCES vets(id) ON DELETE CASCADE,
  FOREIGN KEY (specialty_id) REFERENCES specialties(id)
);
```

### 預載資料

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

## 假設與約束

### 假設

- 專業清單為預定義且固定（radiology, surgery, dentistry）
- 獸醫可以沒有任何專業（普通科醫師）
- 獸醫可以擁有多個專業認證
- 獸醫姓名遵循「名 + 姓」格式（firstName + lastName）

### 約束

- 業務層不可依賴任何 Spring 或 JPA 框架
- 所有資料變更必須發布領域事件
- REST API 必須返回適當的 HTTP 狀態碼
- 獸醫與專業的多對多關聯必須在資料庫層維護

---

## 依賴關係

### 依賴的模組

- **Shared**: 共用例外類別（ResourceNotFoundException）、全域配置

### 被依賴的模組

- **Visits**: 就診模組透過 `VetValidator` port 驗證獸醫存在性
- **GenAI**: AI 模組監聽獸醫事件，同步資料至向量儲存

---

## 實現狀態

- ✅ **Phase 12 完成**: 六角形架構重構
- ✅ **測試覆蓋**: 19 個測試全部通過（12 Business + 8 Service + 7 API）
- ✅ **API 文件**: 已整合 OpenAPI/Swagger
- ✅ **效能指標**: Micrometer `@Timed` 註解已加入
- ✅ **事件發布**: 所有領域事件已實現
- ✅ **資料庫**: MySQL 與 HSQLDB 支援
- ✅ **CRUD 操作**: 完整的建立、查詢、更新、刪除功能

---

**最後更新**: 2025-11-23
**維護者**: Spring PetClinic Modulith Team
**相關文件**: [Vets 模組文件](../../../spring-petclinic-modulith/docs/modules/vets/README.md)
