# 功能規格:GenAI 模組(AI 聊天機器人)

**功能分支**: `004-genai-module`
**建立日期**: 2025-11-23
**狀態**: 已實現(整合層模組)
**架構**: Legacy 整合層架構(Integration Layer)

---

## 使用者場景與測試 *(必填)*

### 使用者故事 1 - AI 智慧問答服務 (優先級: P1)

診所使用者(客戶、接待人員、獸醫)需要透過自然語言查詢診所相關資訊,AI 助手應根據系統資料提供準確回應,以便快速獲取資訊。

**此優先級的原因**: AI 聊天機器人是本模組的核心功能,直接影響使用者體驗。

**獨立測試**: 可透過發送聊天訊息、驗證 AI 回應、測試上下文整合等操作驗證。

**驗收場景**:

1. **Given** 使用者在聊天介面中,**When** 輸入問題「有哪些寵物正在診所接受治療?」,**Then** AI 返回基於系統資料的寵物清單
2. **Given** 使用者詢問特定獸醫的專業資訊,**When** AI 查詢向量儲存,**Then** AI 返回該獸醫的完整專業認證資訊
3. **Given** 使用者詢問客戶的寵物資訊,**When** AI 整合客戶與寵物資料,**Then** AI 返回完整的客戶-寵物關聯資訊
4. **Given** 使用者提出超出系統資料範圍的問題,**When** AI 無法從向量儲存找到相關資料,**Then** AI 禮貌地說明無法回答並建議聯絡診所人員

---

### 使用者故事 2 - 向量儲存資料同步 (優先級: P1)

系統需要自動監聽客戶與獸醫的資料變更事件,即時同步至向量儲存,以確保 AI 回應的資料始終保持最新。

**此優先級的原因**: 資料同步是 RAG(Retrieval-Augmented Generation)系統的基礎,確保 AI 回應的準確性與時效性。

**獨立測試**: 可透過監聽領域事件、驗證向量儲存更新、測試最終一致性等操作驗證。

**驗收場景**:

1. **Given** Customers 模組建立新客戶,**When** 系統發布 CustomerCreated 事件,**Then** GenAI 模組接收事件並將客戶資料新增至向量儲存
2. **Given** Customers 模組更新客戶資訊,**When** 系統發布 CustomerUpdated 事件,**Then** GenAI 模組接收事件並更新向量儲存中的客戶資料
3. **Given** Customers 模組刪除客戶,**When** 系統發布 CustomerDeleted 事件,**Then** GenAI 模組接收事件並從向量儲存移除客戶資料
4. **Given** Vets 模組更新獸醫專業,**When** 系統發布 VetUpdated 事件,**Then** GenAI 模組接收事件並更新向量儲存中的獸醫專業資訊
5. **Given** 事件處理失敗,**When** Spring Modulith 事件儲存機制觸發重試,**Then** 系統最終成功同步資料至向量儲存(最終一致性)

---

### 使用者故事 3 - 上下文感知回應 (優先級: P2)

AI 助手需要根據查詢內容從向量儲存檢索相關資料,並整合至 Prompt 中,以便提供更準確、更具上下文的回應。

**此優先級的原因**: 上下文整合是 RAG 系統的核心價值,但優先級略低於基本聊天與同步功能。

**獨立測試**: 可透過相似度搜尋、Prompt 構建、驗證回應品質等操作驗證。

**驗收場景**:

1. **Given** 使用者詢問「George Franklin 有幾隻寵物?」,**When** AI 從向量儲存檢索 George Franklin 的資料,**Then** AI 回應「George Franklin 有 1 隻寵物:Leo(貓)」
2. **Given** 使用者詢問「哪些獸醫專精外科?」,**When** AI 從向量儲存檢索獸醫專業資料,**Then** AI 列出所有擁有外科專業的獸醫
3. **Given** 向量儲存包含多筆相關資料,**When** AI 進行相似度搜尋,**Then** AI 優先使用最相關的資料構建回應

---

### 邊界案例

- 當 OpenAI API 回應失敗時,系統應返回友善的錯誤訊息並記錄日誌
- 當向量儲存暫時不可用時,系統應降級為基本 AI 回應(無上下文)
- 當使用者輸入超長訊息(超過 token 限制)時,系統應截斷或拒絕並提示錯誤
- 當事件監聽器處理失敗時,Spring Modulith 應自動重試並最終成功
- 當向量儲存中無相關資料時,AI 應回應「目前沒有相關資訊」

---

## 需求 *(必填)*

### 功能需求

- **FR-001**: 系統必須提供 AI 聊天服務,接收使用者訊息並返回 AI 回應
- **FR-002**: 系統必須整合 OpenAI API(預設)或 Azure OpenAI
- **FR-003**: 系統必須使用向量儲存實現 RAG(Retrieval-Augmented Generation)
- **FR-004**: 系統必須監聽 CustomerCreated, CustomerUpdated, CustomerDeleted 事件
- **FR-005**: 系統必須監聽 VetCreated, VetUpdated, VetDeleted 事件
- **FR-006**: 系統必須在接收事件後非同步同步資料至向量儲存
- **FR-007**: 系統必須在聊天請求時從向量儲存檢索相關上下文
- **FR-008**: 系統必須構建包含上下文的 Prompt 並傳送至 OpenAI API
- **FR-009**: 系統必須處理 OpenAI API 錯誤並返回友善的錯誤訊息
- **FR-010**: 系統必須支援可配置的 OpenAI 模型(如 gpt-4o-mini)
- **FR-011**: 系統必須支援可配置的溫度參數(temperature)與最大 token 數
- **FR-012**: 系統必須確保 API Key 不被記錄至日誌或提交至版本控制
- **FR-013**: 系統必須透過 Spring Modulith 事件儲存確保事件不遺失
- **FR-014**: 系統必須實現最終一致性(向量儲存與主資料庫)
- **FR-015**: 系統必須記錄聊天請求的效能指標(Micrometer)

### 關鍵實體

- **ChatRequest(聊天請求)**:
  - 屬性:message
  - DTO

- **ChatResponse(聊天回應)**:
  - 屬性:response
  - DTO

---

## 成功標準 *(必填)*

### 可衡量的結果

- **SC-001**: 系統可在 3 秒內返回 AI 聊天回應(p95 延遲,取決於 OpenAI API)
- **SC-002**: 向量儲存相似度搜尋可在 100ms 內完成(p95 延遲)
- **SC-003**: OpenAI API 呼叫可在 2 秒內完成(p95 延遲)
- **SC-004**: 事件同步延遲低於 5 秒(非同步處理)
- **SC-005**: 95% 的聊天請求成功返回回應(排除 OpenAI API 故障)
- **SC-006**: 100% 的領域事件最終被成功處理(透過重試機制)
- **SC-007**: 所有整合測試通過(6 個測試:3 個 ChatServiceImpl + 3 個 AIDataProvider)
- **SC-008**: API Key 100% 不出現在日誌或版本控制中
- **SC-009**: 向量儲存與主資料庫達成最終一致性(5 秒內)
- **SC-010**: 每日 100 次查詢的 OpenAI API 成本低於 $0.10 (使用 gpt-4o-mini)

---

## 架構需求 *(此模組特定)*

### 整合層架構(不遵循六角形架構)

**重要**: 本模組採用簡單的整合層架構,**不遵循六角形架構模式**。這是合理的設計決策,因為:

1. **整合層特性**: 主要職責是整合外部 AI 服務
2. **無核心業務邏輯**: 不包含複雜的業務規則
3. **快速迭代需求**: AI 技術變化快速,保持靈活性
4. **框架深度綁定**: Spring AI 框架提供的抽象已足夠

- **AR-001**: 本模組可直接使用 Spring AI 框架與 Spring 依賴注入
- **AR-002**: 本模組不需要分離 Domain/Business/Infrastructure 層
- **AR-003**: 本模組使用 `internal/` 封裝所有實現細節
- **AR-004**: 本模組只暴露 `ChatService` 公開介面
- **AR-005**: 本模組使用 `@ApplicationModuleListener` 監聽事件(非同步)
- **AR-006**: 本模組不可被其他模組依賴(單向依賴)
- **AR-007**: REST 控制器直接呼叫服務實現(無需額外抽象)

### 測試需求

- **TR-001**: 測試必須模擬 OpenAI API 回應(避免實際 API 呼叫與成本)
- **TR-002**: 測試必須涵蓋事件監聽與向量儲存同步
- **TR-003**: 測試必須驗證 RAG 上下文整合
- **TR-004**: 測試必須涵蓋錯誤處理(API 失敗、向量儲存失敗)
- **TR-005**: 測試必須驗證重試機制(Spring Modulith 事件儲存)

---

## 領域事件 *(此模組特定)*

### 訂閱的事件

本模組訂閱以下事件(透過 `@ApplicationModuleListener`):

- **CustomerCreated**: 新增客戶至向量儲存
- **CustomerUpdated**: 更新向量儲存中的客戶資料
- **CustomerDeleted**: 從向量儲存移除客戶資料
- **VetCreated**: 新增獸醫至向量儲存
- **VetUpdated**: 更新向量儲存中的獸醫資料
- **VetDeleted**: 從向量儲存移除獸醫資料

**未來可選**:
- **VisitScheduled**: 新增就診記錄至向量儲存
- **VisitCompleted**: 更新就診記錄狀態
- **VisitCancelled**: 更新就診記錄狀態

### 發布的事件

本模組目前不發布任何領域事件。

---

## REST API 契約 *(此模組特定)*

### 端點清單

| 方法 | 路徑 | 描述 | 請求體 | 成功響應 | 錯誤響應 |
|------|------|------|--------|---------| ---------|
| POST | `/genai/chat` | 與 AI 聊天 | ChatRequest | 200 OK, ChatResponse | 500 Internal Server Error |

### ChatRequest DTO 範例

```json
{
  "message": "有哪些寵物正在我們診所接受治療?"
}
```

### ChatResponse DTO 範例

```json
{
  "response": "根據我們的記錄,目前有以下寵物在診所接受治療:\n1. Leo (貓) - 主人: George Franklin\n2. Basil (倉鼠) - 主人: Betty Davis\n..."
}
```

---

## 配置需求 *(此模組特定)*

### OpenAI API 配置

**環境變數**:
```bash
export OPENAI_API_KEY="sk-your-api-key-here"
```

**application.yml**:
```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      model: gpt-4o-mini
      temperature: 0.7
      max-tokens: 500
```

### Azure OpenAI 配置(可選)

**環境變數**:
```bash
export AZURE_OPENAI_ENDPOINT="https://your-resource.openai.azure.com"
export AZURE_OPENAI_KEY="your-api-key-here"
```

**application.yml**:
```yaml
spring:
  ai:
    azure:
      openai:
        endpoint: ${AZURE_OPENAI_ENDPOINT}
        api-key: ${AZURE_OPENAI_KEY}
        deployment-name: gpt-4
```

### Vector Store 配置

**SimpleVectorStore**(預設,開發環境):
```yaml
spring:
  ai:
    vectorstore:
      simple:
        index-file-name: petclinic-vectorstore.json
```

**生產環境建議**:
- Pinecone
- Weaviate
- Redis Vector Search
- Milvus

---

## 假設與約束

### 假設

- OpenAI API 或 Azure OpenAI 已正確配置且 API Key 有效
- 向量儲存使用 SimpleVectorStore(開發環境)或生產級向量資料庫
- 使用者訊息長度在合理範圍內(不超過 token 限制)
- 向量儲存與主資料庫保持最終一致性(非強一致性)
- 本模組不處理對話歷史(每次請求獨立)

### 約束

- 本模組可直接使用 Spring AI 與 Spring 框架(不需解耦)
- API Key 必須透過環境變數配置,不可寫入程式碼或配置檔
- 所有事件監聽必須為非同步處理(`@ApplicationModuleListener`)
- 向量儲存同步失敗時必須依賴 Spring Modulith 事件儲存重試
- 本模組不可被其他模組依賴(單向依賴關係)

---

## 依賴關係

### 依賴的模組

- **Customers**: 監聽客戶事件,同步呼叫 CustomerService 查詢資料
- **Vets**: 監聽獸醫事件,同步呼叫 VetService 查詢資料
- **Shared**: 共用基礎設施(可選)

**依賴方式**:
- ✅ 非同步事件監聽(鬆耦合)
- ✅ 同步呼叫公開 API(查詢資料)
- ❌ 不直接依賴內部實現

### 被依賴的模組

目前沒有其他模組依賴 GenAI 模組。

---

## 實現狀態

- ✅ **整合層架構**: 已實現(不遵循六角形架構)
- ✅ **測試覆蓋**: 6 個整合測試全部通過(3 個 ChatServiceImpl + 3 個 AIDataProvider)
- ✅ **API 文件**: 已整合 OpenAPI/Swagger
- ✅ **效能指標**: Micrometer `@Timed` 註解已加入
- ✅ **事件監聽**: 監聽 6 個領域事件(Customer, Vet)
- ✅ **OpenAI 整合**: Spring AI + OpenAI API
- ✅ **RAG 實現**: 向量儲存 + 相似度搜尋
- ✅ **最終一致性**: Spring Modulith 事件儲存機制

---

## 未來改進

### 功能增強

1. **對話歷史**: 支援多輪對話上下文
2. **個人化回應**: 根據使用者角色客製化回應
3. **多語言支援**: 支援英文、中文等多種語言
4. **語音整合**: 支援語音輸入與輸出

### 技術優化

1. **快取策略**: 快取常見問題的回應
2. **批次處理**: 批次更新向量儲存
3. **向量儲存升級**: 從 SimpleVectorStore 升級至生產級向量資料庫
4. **流式回應**: 支援 SSE(Server-Sent Events)串流回應

### RAG 改進

1. **更好的嵌入模型**: 使用多語言嵌入模型
2. **混合搜尋**: 結合關鍵字搜尋與向量搜尋
3. **重排序**: 對搜尋結果進行重新排序
4. **來源引用**: 在回應中標註資料來源

---

## 安全考量

### API Key 管理

- ✅ 使用環境變數儲存 API Key
- ✅ 不將 API Key 提交至版本控制
- ⚠️ 生產環境建議使用密鑰管理服務(AWS Secrets Manager, Azure Key Vault)

### 使用者輸入驗證

- ✅ 限制訊息長度(防止濫用)
- ✅ 內容過濾(防止注入攻擊)
- ⚠️ 未來考慮:限流機制

### 資料隱私

- ✅ 不在日誌中記錄 API Key
- ✅ 不在日誌中記錄敏感客戶資料
- ⚠️ 未來考慮:資料脫敏處理

---

## 成本管理

### OpenAI API 成本

**gpt-4o-mini**(當前使用):
- Input: $0.15 / 1M tokens
- Output: $0.60 / 1M tokens

**預估成本**(基於每日 100 次查詢):
- 每次查詢約 500 tokens(input) + 500 tokens(output)
- 每日成本:~$0.075
- 每月成本:~$2.25

**成本優化建議**:
1. 使用快取減少重複查詢
2. 限制 max_tokens 參數
3. 監控使用量,設定預算警報

---

**最後更新**: 2025-11-23
**維護者**: Spring PetClinic Modulith Team
**相關文件**: [GenAI 模組文件](../../../spring-petclinic-modulith/docs/modules/genai/README.md)
