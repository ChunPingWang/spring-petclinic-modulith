# 功能規格:Visits 模組(就診記錄管理)

**功能分支**: `003-visits-module`
**建立日期**: 2025-11-23
**狀態**: 已實現(Phase 13 完成)
**架構**: 六角形架構(Hexagonal Architecture)

---

## 使用者場景與測試 *(必填)*

### 使用者故事 1 - 就診記錄排程 (優先級: P1)

診所接待人員需要為寵物排程就診記錄,指定獸醫與就診時間,以便追蹤就診安排並提供醫療服務。

**此優先級的原因**: 就診記錄是診所核心業務流程,沒有就診記錄就無法提供醫療服務。

**獨立測試**: 可透過建立就診記錄、查詢就診記錄、驗證跨模組關聯等操作獨立驗證。

**驗收場景**:

1. **Given** 接待人員在系統中,**When** 為寵物排程就診並指定獸醫與時間,**Then** 系統成功建立就診記錄並返回就診 ID
2. **Given** 排程就診時指定的寵物不存在,**When** 系統驗證寵物,**Then** 系統拒絕建立並返回錯誤訊息
3. **Given** 排程就診時指定的獸醫不存在,**When** 系統驗證獸醫,**Then** 系統拒絕建立並返回錯誤訊息
4. **Given** 系統中存在就診記錄,**When** 接待人員查詢所有就診記錄,**Then** 系統顯示完整的就診清單
5. **Given** 接待人員需要查看特定寵物的就診歷史,**When** 按寵物 ID 查詢,**Then** 系統返回該寵物的所有就診記錄

---

### 使用者故事 2 - 就診狀態管理 (優先級: P1)

獸醫與接待人員需要追蹤就診記錄的狀態(已排程、已完成、已取消),以便管理就診流程並維護準確的醫療記錄。

**此優先級的原因**: 就診狀態追蹤是醫療記錄管理的核心,影響診所運作效率與醫療品質。

**獨立測試**: 可透過完成就診、取消就診、驗證狀態轉換規則等操作驗證。

**驗收場景**:

1. **Given** 就診記錄狀態為「已排程」,**When** 獸醫完成就診,**Then** 系統將狀態更新為「已完成」並發布完成事件
2. **Given** 就診記錄狀態為「已排程」,**When** 客戶取消就診,**Then** 系統將狀態更新為「已取消」並發布取消事件
3. **Given** 就診記錄狀態為「已完成」,**When** 嘗試取消該就診,**Then** 系統拒絕操作並返回錯誤訊息
4. **Given** 就診記錄狀態為「已取消」,**When** 嘗試完成該就診,**Then** 系統拒絕操作並返回錯誤訊息

---

### 使用者故事 3 - 就診歷史查詢 (優先級: P2)

獸醫需要查看特定寵物或自己負責的就診歷史,以便了解醫療記錄並提供持續性的醫療服務。

**此優先級的原因**: 查詢功能對醫療決策重要,但優先級略低於基本 CRUD 操作。

**獨立測試**: 可透過按寵物查詢、按獸醫查詢等操作驗證。

**驗收場景**:

1. **Given** 獸醫需要查看自己負責的所有就診記錄,**When** 按獸醫 ID 查詢,**Then** 系統返回該獸醫的所有就診記錄
2. **Given** 獸醫需要了解寵物的完整醫療歷史,**When** 查詢該寵物的就診記錄,**Then** 系統按時間順序顯示所有就診記錄
3. **Given** 接待人員需要確認特定日期的就診安排,**When** 查詢該日期的就診記錄,**Then** 系統顯示該日期的所有已排程就診

---

### 邊界案例

- 當就診日期為空時,系統應拒絕建立並返回驗證錯誤
- 當嘗試完成不存在的就診記錄時,系統應返回 404 錯誤
- 當就診記錄已完成時嘗試取消,系統應拒絕並提示錯誤
- 當排程就診時寵物 ID 為 null 或無效,系統應拒絕並提示錯誤
- 當排程就診時獸醫 ID 為 null 或無效,系統應拒絕並提示錯誤
- 當就診記錄處於終止狀態(已完成/已取消)時,系統應防止狀態變更

---

## 需求 *(必填)*

### 功能需求

- **FR-001**: 系統必須允許使用者為寵物排程就診,包含寵物 ID、獸醫 ID、就診日期、描述等資訊
- **FR-002**: 系統必須允許使用者查詢單一就診記錄或所有就診記錄
- **FR-003**: 系統必須允許使用者按寵物 ID 查詢就診記錄
- **FR-004**: 系統必須允許使用者按獸醫 ID 查詢就診記錄
- **FR-005**: 系統必須允許使用者完成已排程的就診記錄
- **FR-006**: 系統必須允許使用者取消就診記錄
- **FR-007**: 系統必須驗證就診日期不可為空
- **FR-008**: 系統必須驗證寵物 ID 必須關聯至有效的寵物(跨模組驗證)
- **FR-009**: 系統必須驗證獸醫 ID 必須關聯至有效的獸醫(跨模組驗證)
- **FR-010**: 系統必須追蹤就診狀態(SCHEDULED, COMPLETED, CANCELLED)
- **FR-011**: 系統必須實施狀態轉換規則(SCHEDULED → COMPLETED 或 CANCELLED)
- **FR-012**: 系統必須防止已完成的就診記錄被取消
- **FR-013**: 系統必須防止已取消或已完成的就診記錄被重新完成
- **FR-014**: 系統必須在就診排程、完成、取消時發布領域事件
- **FR-015**: 系統必須將就診記錄持久化至資料庫
- **FR-016**: 系統必須透過 Port-Adapter 模式實現跨模組驗證(避免直接依賴)
- **FR-017**: 系統必須支援就診描述長度最多 8192 字元
- **FR-018**: 系統必須為 pet_id 和 vet_id 建立索引以優化查詢效能

### 關鍵實體

- **Visit(就診記錄)**:
  - 屬性:id, petId, vetId, visitDate, description, status
  - 關係:每筆就診記錄關聯一個寵物與一個獸醫
  - 聚合根

- **VisitStatus(就診狀態)**:
  - 枚舉值:SCHEDULED(已排程)、COMPLETED(已完成)、CANCELLED(已取消)
  - 狀態轉換:SCHEDULED → COMPLETED 或 CANCELLED(終止狀態)
  - 值物件

---

## 成功標準 *(必填)*

### 可衡量的結果

- **SC-001**: 使用者可在 5 秒內完成就診排程(包含跨模組驗證)
- **SC-002**: 系統可在 200ms 內返回單一就診記錄查詢結果(p95 延遲)
- **SC-003**: 系統可在 500ms 內完成就診狀態更新(p95 延遲)
- **SC-004**: 系統可在 300ms 內返回按寵物/獸醫查詢的結果(p95 延遲)
- **SC-005**: 系統支援至少 1000 個並發使用者同時查詢就診資訊
- **SC-006**: 95% 的就診排程操作在第一次嘗試時成功完成(驗證錯誤除外)
- **SC-007**: 系統在就診狀態變更後立即發布事件
- **SC-008**: 跨模組驗證(寵物/獸醫)的額外延遲低於 50ms
- **SC-009**: 100% 的業務邏輯測試通過(20 個純 Java 單元測試)
- **SC-010**: 100% 的服務層委派測試通過(11 個整合測試)
- **SC-011**: 所有 REST API 端點返回正確的 HTTP 狀態碼(200, 201, 204, 404)
- **SC-012**: 狀態轉換規則 100% 被強制執行(不允許非法轉換)

---

## 架構需求 *(此模組特定)*

### 六角形架構遵循

- **AR-001**: 業務邏輯必須與框架完全解耦(零 Spring/JPA 依賴)
- **AR-002**: 領域模型必須為純 POJO,包含業務邏輯方法(如 `schedule()`, `complete()`, `cancel()`)
- **AR-003**: 業務層必須定義 Port 介面(VisitRepository, EventPublisher, PetValidator, VetValidator)
- **AR-004**: 基礎設施層必須實現 Adapter(VisitRepositoryAdapter, SpringEventPublisherAdapter, CustomerServicePetValidator, VetServiceVetValidator)
- **AR-005**: 領域模型與 JPA 實體必須透過 DomainMapper 進行三向轉換(Domain ↔ Entity ↔ Legacy)
- **AR-006**: 跨模組驗證必須透過 Port-Adapter 模式實現,避免直接依賴 Customers/Vets 模組
- **AR-007**: REST 控制器只負責 HTTP 協議處理,不包含業務邏輯
- **AR-008**: 就診狀態轉換規則必須在領域模型中實現

### 測試需求

- **TR-001**: 業務層測試必須為純 Java 單元測試(不使用 Spring 上下文)
- **TR-002**: 測試必須涵蓋跨模組驗證邏輯(PetValidator, VetValidator)
- **TR-003**: 測試必須驗證狀態轉換規則(SCHEDULED → COMPLETED/CANCELLED)
- **TR-004**: 測試必須涵蓋邊界條件(null 檢查、無效 ID、非法狀態轉換)
- **TR-005**: API 層測試必須包含完整的操作(查詢、排程、完成、取消)
- **TR-006**: 測試必須涵蓋 404 錯誤場景(查詢/完成/取消不存在的就診記錄)
- **TR-007**: 測試必須驗證異常翻譯(InvalidVisitException → ResourceNotFoundException)

---

## 領域事件 *(此模組特定)*

### 發布的事件

- **VisitScheduled**: 當新就診記錄排程時發布
  - 屬性:visitId, petId, vetId, visitDate
  - 訂閱者:GenAI 模組(可選,未來可同步就診資料)

- **VisitCompleted**: 當就診記錄完成時發布
  - 屬性:visitId, petId, vetId
  - 訂閱者:GenAI 模組(可選)、統計模組(未來)

- **VisitCancelled**: 當就診記錄取消時發布
  - 屬性:visitId, petId, vetId
  - 訂閱者:GenAI 模組(可選)、通知模組(未來)

---

## REST API 契約 *(此模組特定)*

### 端點清單

| 方法 | 路徑 | 描述 | 請求體 | 成功響應 | 錯誤響應 |
|------|------|------|--------|---------| ---------|
| GET | `/visits` | 查詢所有就診記錄 | - | 200 OK, Visit[] | - |
| GET | `/visits/{id}` | 查詢單一就診記錄 | - | 200 OK, Visit | 404 Not Found |
| GET | `/visits?petId={petId}` | 查詢特定寵物的就診記錄 | - | 200 OK, Visit[] | 400 Bad Request |
| GET | `/visits?vetId={vetId}` | 查詢特定獸醫的就診記錄 | - | 200 OK, Visit[] | 400 Bad Request |
| POST | `/visits` | 排程新就診記錄 | VisitRequest | 201 Created, Location header | 400 Bad Request, 404 Not Found |
| POST | `/visits/{id}/complete` | 完成就診 | - | 200 OK, Visit | 404 Not Found, 400 Bad Request |
| POST | `/visits/{id}/cancel` | 取消就診 | - | 204 No Content | 404 Not Found, 400 Bad Request |

### VisitRequest DTO 範例

```json
{
  "petId": 1,
  "vetId": 1,
  "visitDate": "2024-01-15T10:00:00",
  "description": "定期健康檢查"
}
```

### Visit Response 範例

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

## 資料庫 Schema *(此模組特定)*

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

**就診記錄**:
- ID 1: Pet 7 (Samantha) → Vet 3 (Linda Douglas) - 2023-01-01 - 狂犬病疫苗接種
- ID 2: Pet 8 (Max) → Vet 3 (Linda Douglas) - 2023-01-02 - 絕育手術
- ID 3: Pet 8 (Max) → Vet 2 (Helen Leary) - 2023-01-03 - 神經檢查
- ID 4: Pet 7 (Samantha) → Vet 1 (James Carter) - 2023-01-04 - 檢查是否有跳蚤

**狀態**:
- 預設狀態為 `SCHEDULED`
- 歷史資料可設定為 `COMPLETED`

---

## 假設與約束

### 假設

- 就診日期使用系統標準時區(無需特殊時區處理)
- 就診描述可為空(某些就診可能無特殊描述)
- 寵物與獸醫必須在系統中存在(強制外鍵約束與業務驗證)
- 就診記錄一旦進入終止狀態(已完成/已取消)即不可變更

### 約束

- 業務層不可依賴任何 Spring 或 JPA 框架
- 所有資料變更必須發布領域事件
- REST API 必須返回適當的 HTTP 狀態碼
- 跨模組驗證必須透過 Port 介面,不可直接依賴其他模組
- 狀態轉換規則必須在領域模型中強制執行

---

## 依賴關係

### 依賴的模組

- **Customers**: 透過 `PetValidator` port 驗證寵物存在性(Port-Adapter 模式)
- **Vets**: 透過 `VetValidator` port 驗證獸醫存在性(Port-Adapter 模式)
- **Shared**: 共用例外類別(ResourceNotFoundException)、全域配置

**依賴方式**:
- ✅ 使用 Port-Adapter 模式(鬆耦合)
- ✅ 只呼叫其他模組的公開 API
- ❌ 不直接依賴 Customers/Vets 模組的實體類別

### 被依賴的模組

目前沒有其他模組依賴 Visits 模組。

**潛在訂閱者**:
- **GenAI**: 未來可監聽就診事件,同步就診資料至向量儲存

---

## 實現狀態

- ✅ **Phase 13 完成**: 六角形架構重構
- ✅ **測試覆蓋**: 31 個測試全部通過(20 Business + 11 Service + API 測試)
- ✅ **API 文件**: 已整合 OpenAPI/Swagger
- ✅ **效能指標**: Micrometer `@Timed` 註解已加入
- ✅ **事件發布**: 所有領域事件已實現
- ✅ **資料庫**: MySQL 與 HSQLDB 支援
- ✅ **跨模組驗證**: Port-Adapter 模式已實現
- ✅ **狀態管理**: 完整的狀態轉換規則與驗證

---

**最後更新**: 2025-11-23
**維護者**: Spring PetClinic Modulith Team
**相關文件**: [Visits 模組文件](../../../spring-petclinic-modulith/docs/modules/visits/README.md)
