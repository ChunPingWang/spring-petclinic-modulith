# GenAI 模組文件

**模組名稱**: GenAI (AI 聊天機器人)
**當前狀態**: ⏸️ Legacy 架構 (整合層模組)
**最後更新**: 2025-11-23

---

## 📋 模組概述

GenAI 模組提供 AI 聊天機器人功能，使用 Spring AI 框架整合 OpenAI API，為使用者提供智慧問答服務。本模組作為整合層存在，不需要進行六角形架構重構。

### 核心職責

- AI 聊天服務（接收使用者問題，返回 AI 回應）
- 向量儲存同步（監聽領域事件，更新 RAG 資料）
- 上下文管理（Customer, Vet, Visit 資料提供給 AI）
- OpenAI API 整合

### 邊界上下文（Bounded Context）

本模組定義了「AI 助手服務」的邊界上下文，作為其他模組的整合層，不包含核心業務邏輯。

---

## 🏗️ 架構設計

### Legacy 架構（整合層）

本模組採用簡單的整合層架構，不遵循六角形架構模式。這是合理的設計決策，因為：

1. **整合層特性**：主要職責是整合外部 AI 服務
2. **無核心業務邏輯**：不包含複雜的業務規則
3. **快速迭代需求**：AI 技術變化快速，保持靈活性
4. **框架深度綁定**：Spring AI 框架提供的抽象已足夠

```
GenAI Module (Integration Layer)
├── ChatService.java          ← 公開服務介面
└── internal/                 ← 所有實現
    ├── ChatServiceImpl.java
    ├── AIDataProvider.java   ← 事件監聽與資料同步
    ├── dto/
    │   ├── ChatRequest.java
    │   └── ChatResponse.java
    └── web/
        └── ChatResource.java ← REST 控制器
```

### 目錄結構

```
genai/
├── ChatService.java                        ← 公開服務介面
│
└── internal/                               ← Internal 實現
    ├── ChatServiceImpl.java                ← Spring AI 整合
    ├── AIDataProvider.java                 ← 事件監聽與向量儲存同步
    ├── dto/
    │   ├── ChatRequest.java                ← 聊天請求 DTO
    │   └── ChatResponse.java               ← 聊天回應 DTO
    └── web/
        └── ChatResource.java               ← REST 控制器
```

---

## 🔌 公開 API

### 公開服務介面

```java
public interface ChatService {
    ChatResponse chat(ChatRequest request);
}
```

---

## 🤖 AI 整合實現

### ChatServiceImpl

使用 Spring AI 整合 OpenAI API。

**主要方法**:

```java
@Service
public class ChatServiceImpl implements ChatService {
    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    @Override
    public ChatResponse chat(ChatRequest request) {
        // 1. 從向量儲存取得相關上下文
        List<Document> relevantDocs = vectorStore.similaritySearch(request.message());

        // 2. 構建 prompt（包含上下文）
        String contextualPrompt = buildPrompt(request.message(), relevantDocs);

        // 3. 呼叫 OpenAI API
        String aiResponse = chatClient.call(contextualPrompt);

        // 4. 返回回應
        return new ChatResponse(aiResponse);
    }
}
```

**技術細節**:
- 使用 Spring AI `ChatClient` 與 OpenAI 通訊
- 使用 `VectorStore` 實現 RAG (Retrieval-Augmented Generation)
- 支援對話上下文管理

### AIDataProvider

監聽其他模組的領域事件，同步資料至向量儲存。

**事件監聽**:

```java
@Service
public class AIDataProvider {
    private final VectorStore vectorStore;
    private final CustomerService customerService;
    private final VetService vetService;

    // 監聽客戶建立事件
    @ApplicationModuleListener
    void on(CustomerCreated event) {
        Customer customer = customerService.findById(event.customerId()).orElseThrow();
        Document doc = createCustomerDocument(customer);
        vectorStore.add(List.of(doc));
    }

    // 監聽客戶更新事件
    @ApplicationModuleListener
    void on(CustomerUpdated event) {
        // 更新向量儲存中的客戶資料
    }

    // 監聽客戶刪除事件
    @ApplicationModuleListener
    void on(CustomerDeleted event) {
        // 從向量儲存移除客戶資料
    }

    // 類似處理 Vet 相關事件...
}
```

**同步策略**:
- **非同步處理**：使用 `@ApplicationModuleListener` 非同步監聽事件
- **自動重試**：Spring Modulith 的事件儲存機制確保事件不遺失
- **最終一致性**：向量儲存與主資料庫最終一致

---

## 🌐 REST API

### Chat Resource (聊天端點)

**Base Path**: `/genai`

#### 端點清單

| 方法 | 路徑 | 描述 | 請求體 | 響應 |
|------|------|------|--------|------|
| POST | `/genai/chat` | 與 AI 聊天 | ChatRequest | 200 OK, ChatResponse |

**ChatRequest DTO**:
```json
{
  "message": "有哪些寵物正在我們診所接受治療？"
}
```

**ChatResponse DTO**:
```json
{
  "response": "根據我們的記錄，目前有以下寵物在診所接受治療：\n1. Leo (貓) - 主人: George Franklin\n2. Basil (倉鼠) - 主人: Betty Davis\n..."
}
```

---

## 🧪 測試策略

### 測試覆蓋

本模組包含 **3 個測試**：

#### 整合測試（3 個）

**ChatServiceImplTest**:
- ✅ `shouldRespondToChatRequest` - 基本聊天功能測試
- ✅ `shouldIncludeContextFromVectorStore` - RAG 上下文整合測試
- ✅ `shouldHandleApiErrors` - 錯誤處理測試

**AIDataProviderTest**:
- ✅ `shouldSyncCustomerDataOnEvent` - 事件監聽與同步測試
- ✅ `shouldUpdateVectorStoreOnCustomerUpdate` - 更新同步測試
- ✅ `shouldRemoveDataOnCustomerDelete` - 刪除同步測試

**測試特點**:
- 使用模擬的 OpenAI API（避免實際 API 呼叫）
- 測試事件監聽與向量儲存同步
- 測試錯誤處理與重試機制

### 執行測試

```bash
# 執行所有 GenAI 模組測試
../mvnw test -Dtest="org.springframework.samples.petclinic.genai.**.*Test"

# 執行特定測試
../mvnw test -Dtest="ChatServiceImplTest"
```

---

## 🔗 模組依賴

### 依賴的模組

- **Customers**: 監聽客戶事件，同步客戶與寵物資料
- **Vets**: 監聽獸醫事件，同步獸醫資料
- **Visits**: 可選，未來可監聽就診事件
- **Shared**: 共用基礎設施

**依賴方式**:
- ✅ 非同步事件監聽（鬆耦合）
- ✅ 同步呼叫公開 API（查詢資料）
- ❌ 不直接依賴內部實現

### 被依賴的模組

目前沒有其他模組依賴 GenAI 模組。

### 事件訂閱

本模組訂閱以下事件：

- `CustomerCreated` - 新增客戶至向量儲存
- `CustomerUpdated` - 更新向量儲存中的客戶資料
- `CustomerDeleted` - 從向量儲存移除客戶資料
- `VetCreated` - 新增獸醫至向量儲存
- `VetUpdated` - 更新向量儲存中的獸醫資料
- `VetDeleted` - 從向量儲存移除獸醫資料

---

## ⚙️ 配置需求

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

### Azure OpenAI 配置（可選）

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

**SimpleVectorStore** (預設，開發環境)：
```yaml
spring:
  ai:
    vectorstore:
      simple:
        index-file-name: petclinic-vectorstore.json
```

**生產環境建議**：
- Pinecone
- Weaviate
- Redis Vector Search
- Milvus

---

## 📈 效能指標

### Micrometer 指標

本模組使用 `@Timed` 註解記錄效能指標：

- `petclinic.chat.request` - 聊天請求耗時
- `petclinic.chat.vectorstore.search` - 向量搜尋耗時
- `petclinic.chat.openai.call` - OpenAI API 呼叫耗時

### 效能目標

- 聊天回應時間: < 3 秒 p95
- 向量搜尋: < 100ms p95
- OpenAI API 呼叫: < 2 秒 p95 (外部服務依賴)
- 事件同步延遲: < 5 秒 (非同步)

---

## 🚀 未來改進

### 功能增強

1. **對話歷史**: 支援多輪對話上下文
2. **個人化回應**: 根據使用者角色客製化回應
3. **多語言支援**: 支援英文、中文等多種語言
4. **語音整合**: 支援語音輸入與輸出

### 技術優化

1. **快取策略**: 快取常見問題的回應
2. **批次處理**: 批次更新向量儲存
3. **向量儲存升級**: 從 SimpleVectorStore 升級至生產級向量資料庫
4. **流式回應**: 支援 SSE (Server-Sent Events) 串流回應

### RAG 改進

1. **更好的嵌入模型**: 使用多語言嵌入模型
2. **混合搜尋**: 結合關鍵字搜尋與向量搜尋
3. **重排序**: 對搜尋結果進行重新排序
4. **來源引用**: 在回應中標註資料來源

---

## 🔒 安全考量

### API Key 管理

- ✅ 使用環境變數儲存 API Key
- ✅ 不將 API Key 提交至版本控制
- ⚠️ 生產環境建議使用密鑰管理服務 (AWS Secrets Manager, Azure Key Vault)

### 使用者輸入驗證

- ✅ 限制訊息長度（防止濫用）
- ✅ 內容過濾（防止注入攻擊）
- ⚠️ 未來考慮：限流機制

### 資料隱私

- ✅ 不在日誌中記錄 API Key
- ✅ 不在日誌中記錄敏感客戶資料
- ⚠️ 未來考慮：資料脫敏處理

---

## 💰 成本管理

### OpenAI API 成本

**gpt-4o-mini** (當前使用):
- Input: $0.15 / 1M tokens
- Output: $0.60 / 1M tokens

**預估成本** (基於每日 100 次查詢):
- 每次查詢約 500 tokens (input) + 500 tokens (output)
- 每日成本: ~$0.075
- 每月成本: ~$2.25

**成本優化建議**:
1. 使用快取減少重複查詢
2. 限制 max_tokens 參數
3. 監控使用量，設定預算警報

---

## 📚 相關文件

- [Spring PetClinic Modulith README](../../../README.md)
- [專案憲章 (Constitution)](../../../.specify/memory/constitution.md)
- [Spring AI 官方文件](https://docs.spring.io/spring-ai/reference/)
- [OpenAI API 文件](https://platform.openai.com/docs/api-reference)

---

## 🎓 學習資源

### Spring AI

- [Spring AI Quick Start](https://docs.spring.io/spring-ai/reference/getting-started.html)
- [Vector Store 整合](https://docs.spring.io/spring-ai/reference/api/vectordbs.html)
- [RAG 模式](https://docs.spring.io/spring-ai/reference/concepts.html#_retrieval_augmented_generation)

### OpenAI

- [GPT Models 比較](https://platform.openai.com/docs/models)
- [Prompt Engineering](https://platform.openai.com/docs/guides/prompt-engineering)
- [Best Practices](https://platform.openai.com/docs/guides/production-best-practices)

---

**最後更新**: 2025-11-23
**維護者**: Spring PetClinic Modulith Team
**狀態**: ⏸️ Legacy 架構（整合層模組，不需重構）
**重構優先級**: 低（整合層特性，保持靈活性）
