# Phase 6: GenAI Module - Implementation Plan

**Status**: 🚀 READY TO START  
**Target Files**: 15-20 (core) + 4 test classes + 2 database  
**Key Pattern**: Event listeners from all 3 domain modules  
**Complexity**: High (demonstrates async communication)

---

## Overview

The GenAI module is the final **consumer module** that listens to events from all three domain modules (Customers, Vets, Visits) and integrates with Azure OpenAI to provide AI-powered chat functionality.

### Why GenAI is Strategic

- **Pattern Demonstration**: First module consuming events asynchronously
- **Cross-Module Integration**: Depends on events from all 3 modules
- **External Integration**: Azure OpenAI demonstrates cloud service integration
- **User Interaction**: REST API for chat with context from other modules

---

## Module Architecture

```
org.springframework.samples.petclinic.genai/
├── ChatService.java                          # PUBLIC: Chat interface
├── ChatCreated.java                          # PUBLIC: Event
├── ChatQuestion.java                         # DTO
├── ChatResponse.java                         # DTO
├── internal/
│   ├── ChatServiceImpl.java                   # Service implementation
│   ├── AIDataProvider.java                   # Event listeners
│   ├── VectorStoreController.java           # Vector DB management
│   ├── AzureOpenAiClient.java               # External service wrapper
│   ├── PetclinicKnowledgeBase.java          # Context builder
│   └── web/
│       ├── ChatResource.java                # REST controller
│       └── ChatDTOMapper.java               # DTO mapping
└── tests/
    ├── ChatResourceTest.java                # REST tests
    ├── ChatServiceImplTest.java             # Service tests
    ├── AIDataProviderTest.java              # Event listener tests
    └── GenAIModuleIntegrationTest.java      # Module structure tests
```

---

## Implementation Roadmap

### 6.1-6.3: Core Interfaces & DTOs (3 files)

#### 6.1: ChatService.java (Public Interface)
```java
public interface ChatService {
    ChatResponse chat(ChatQuestion question);           // Main API
    void indexCustomer(Integer customerId);             // Event listener callback
    void indexVet(Integer vetId);                       // Event listener callback
    void indexVisit(Integer visitId);                   // Event listener callback
    List<String> getConversationHistory(String userId);
    void clearContext(String userId);
}
```

#### 6.2: DTOs for REST Communication
```java
public record ChatQuestion(
    String userId,
    String question,
    Optional<Integer> petId,      // Context filter
    Optional<Integer> vetId        // Context filter
) {}

public record ChatResponse(
    String answer,
    List<String> sources,          // Which pets/vets mentioned
    String conversationId
) {}
```

#### 6.3: Domain Events
```java
public record ChatCreated(
    Integer chatId,
    String userId,
    String question,
    String answer,
    LocalDateTime createdAt
) {}
```

### 6.4-6.6: Event Listeners (3 files)

#### 6.4: AIDataProvider.java - Listen to All Modules
```java
@Service
public class AIDataProvider {
    
    // Listen to customer events
    @ApplicationModuleListener
    void on(CustomerCreated event) {
        indexNewCustomer(event.customerId());
    }
    
    @ApplicationModuleListener
    void on(CustomerUpdated event) {
        updateCustomerIndex(event.customerId());
    }
    
    // Listen to vet events  
    @ApplicationModuleListener
    void on(VetCreated event) {
        indexNewVet(event.vetId());
    }
    
    // Listen to visit events
    @ApplicationModuleListener
    void on(VisitCreated event) {
        indexNewVisit(event.visitId());
    }
    
    @ApplicationModuleListener
    void on(VisitCompleted event) {
        updateVisitCompletion(event.visitId());
    }
    
    // Update vector database
    private void indexNewCustomer(Integer customerId) { }
    private void updateCustomerIndex(Integer customerId) { }
    private void indexNewVet(Integer vetId) { }
    private void indexNewVisit(Integer visitId) { }
    private void updateVisitCompletion(Integer visitId) { }
}
```

#### 6.5: AzureOpenAiClient.java - External Integration
```java
@Service
public class AzureOpenAiClient {
    private final RestTemplate restTemplate;
    private final String endpoint;
    private final String apiKey;
    
    public String chat(String prompt, List<String> context) {
        // Call Azure OpenAI API with context
        // Return response
    }
    
    public List<String> embedText(String text) {
        // Get embeddings for vector search
    }
}
```

#### 6.6: VectorStoreController.java - Knowledge Base
```java
@Service
public class VectorStoreController {
    private final Map<String, List<Double>> vectors = new ConcurrentHashMap<>();
    
    public void addVector(String id, String text, List<Double> embedding) {
        vectors.put(id, embedding);
    }
    
    public List<String> search(String query, int topK) {
        // Semantic search in vector store
        // Return top K similar documents
    }
}
```

### 6.7-6.9: Service Implementation (3 files)

#### 6.7: PetclinicKnowledgeBase.java
```java
@Service
public class PetclinicKnowledgeBase {
    private final CustomerService customerService;
    private final VetService vetService;
    private final VisitService visitService;
    
    public String buildContext(ChatQuestion question) {
        StringBuilder context = new StringBuilder();
        
        // Add customer information
        if (question.petId().isPresent()) {
            Customer customer = customerService.findById(question.petId().get());
            context.append("Customer: ").append(customer.getName());
        }
        
        // Add vet information
        if (question.vetId().isPresent()) {
            Vet vet = vetService.findById(question.vetId().get());
            context.append("Vet: ").append(vet.getName());
        }
        
        return context.toString();
    }
}
```

#### 6.8: ChatServiceImpl.java
```java
@Service
public class ChatServiceImpl implements ChatService {
    private final AzureOpenAiClient openAiClient;
    private final VectorStoreController vectorStore;
    private final PetclinicKnowledgeBase knowledgeBase;
    private final ApplicationEventPublisher events;
    
    @Override
    public ChatResponse chat(ChatQuestion question) {
        // Build context from other modules
        String context = knowledgeBase.buildContext(question);
        
        // Search vector database
        List<String> sources = vectorStore.search(question.question(), 3);
        
        // Call Azure OpenAI
        String answer = openAiClient.chat(question.question(), sources);
        
        // Publish event for analytics
        events.publishEvent(new ChatCreated(...));
        
        return new ChatResponse(answer, sources, UUID.randomUUID().toString());
    }
    
    @Override
    public void indexCustomer(Integer customerId) {
        // Called by AIDataProvider when CustomerCreated event arrives
    }
    
    @Override
    public void indexVet(Integer vetId) {
        // Called by AIDataProvider when VetCreated event arrives
    }
    
    @Override
    public void indexVisit(Integer visitId) {
        // Called by AIDataProvider when VisitCreated event arrives
    }
}
```

#### 6.9: ChatDTOMapper.java
```java
@Service
public class ChatDTOMapper {
    public ChatResponse toDTO(Chat chat) { }
    public Chat toDomain(ChatQuestion question) { }
}
```

### 6.10: REST Layer (1 file)

#### ChatResource.java
```java
@RestController
@RequestMapping("/chat")
@Timed("petclinic.chat")
public class ChatResource {
    private final ChatService chatService;
    
    @PostMapping("/message")
    @ResponseStatus(HttpStatus.CREATED)
    public ChatResponse chat(@Valid @RequestBody ChatQuestion question) {
        return chatService.chat(question);
    }
    
    @GetMapping("/history/{userId}")
    public List<String> getHistory(@PathVariable String userId) {
        return chatService.getConversationHistory(userId);
    }
    
    @DeleteMapping("/context/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearContext(@PathVariable String userId) {
        chatService.clearContext(userId);
    }
}
```

### 6.11-6.14: Comprehensive Testing (4 files)

#### 6.11: ChatResourceTest.java (8 tests)
```
- shouldPostChatQuestion() - /chat/message endpoint
- shouldReturnValidResponse() - Response structure validation
- shouldReturnHistoryForUser() - /history/{userId} endpoint
- shouldClearContextForUser() - DELETE endpoint
- shouldReturn400ForInvalidInput() - Input validation
- shouldIncludeSourcesInResponse() - Source attribution
- shouldIncludePetContextInResponse() - Pet filtering
- shouldIncludeVetContextInResponse() - Vet filtering
```

#### 6.12: ChatServiceImplTest.java (8 tests)
```
- shouldGenerateValidChatResponse() - Response content
- shouldIncludeSourcesFromVectorStore() - Vector search
- shouldBuildContextFromOtherModules() - Cross-module data
- shouldCallAzureOpenAi() - External API call
- shouldPublishChatCreatedEvent() - Event publishing
- shouldHandleEmptyContext() - Edge case
- shouldReturnEmptyHistoryForNewUser() - User handling
- shouldClearConversationHistory() - State management
```

#### 6.13: AIDataProviderTest.java (6 tests)
```
- shouldListenToCustomerCreatedEvent() - Event listener
- shouldListenToVetCreatedEvent() - Event listener
- shouldListenToVisitCreatedEvent() - Event listener
- shouldUpdateVectorStoreOnCustomerCreated() - Side effect
- shouldUpdateVectorStoreOnVetCreated() - Side effect
- shouldUpdateVectorStoreOnVisitCreated() - Side effect
```

#### 6.14: GenAIModuleIntegrationTest.java (5 tests)
```
- verifiesModuleStructure() - Module boundaries
- genAIModuleExists() - Module registration
- genAIModuleHasCorrectPublicApi() - Public API check
- verifyEventListenerSetup() - Listener registration
- verifyNoCircularDependencies() - Dependency check
```

### 6.15: Database & Configuration (3 files)

#### 6.15a: chat-schema.sql (HSQLDB)
```sql
CREATE TABLE chats (
    id INTEGER IDENTITY NOT NULL PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    question VARCHAR(8192),
    answer VARCHAR(8192),
    sources VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_chats_user_id ON chats(user_id);
```

#### 6.15b: Vector Store Configuration
```java
@Configuration
public class VectorStoreConfig {
    @Bean
    public VectorStoreController vectorStoreController() {
        return new VectorStoreController();
    }
}
```

#### 6.15c: Azure OpenAI Configuration
```yaml
# application.yml
spring:
  genai:
    azure:
      endpoint: ${AZURE_OPENAI_ENDPOINT}
      api-key: ${AZURE_OPENAI_KEY}
      deployment-name: petclinic-model
      api-version: 2024-02-15-preview
```

---

## Cross-Module Integration Diagram

```
┌─────────────────┐
│   Customers     │─────┐
│    Module       │     │
└─────────────────┘     │
                        │
┌─────────────────┐     │    ┌─────────────────┐
│     Vets        │─────┼───>│  GenAI Module   │
│    Module       │     │    │  (Event Listener)
└─────────────────┘     │    └─────────────────┘
                        │         ▲    │
┌─────────────────┐     │         │    │
│    Visits       │─────┘         │    │
│    Module       │               │    │
└─────────────────┘               │    │
                         Events   │    │ REST
                         (async)  │    │ (chat API)
                                  │    ▼
                            ┌──────────────┐
                            │   Azure      │
                            │   OpenAI     │
                            └──────────────┘
```

---

## Key Implementation Patterns

### Pattern 1: Event Listening
```java
@ApplicationModuleListener  // From Spring Modulith
void on(CustomerCreated event) {
    // Auto-called when event published from customers module
    indexNewCustomer(event.customerId());
}
```

### Pattern 2: Cross-Module Service Injection
```java
@Service
class ChatServiceImpl {
    // Can inject public services from other modules
    private final CustomerService customerService;
    private final VetService vetService;
    private final VisitService visitService;
}
```

### Pattern 3: External Service Integration
```java
@Service
class AzureOpenAiClient {
    // Wraps external API calls
    // Centralizes credentials and error handling
    public String chat(String prompt, List<String> context) {
        // Call Azure with proper error handling
    }
}
```

---

## Environment Variables Needed

```bash
# For Azure OpenAI integration
export AZURE_OPENAI_ENDPOINT=https://xxx.openai.azure.com/
export AZURE_OPENAI_KEY=sk-xxx
export AZURE_OPENAI_DEPLOYMENT=petclinic-model

# For chat persistence
export CHAT_DB_URL=jdbc:hsqldb:mem:chatdb
export CHAT_DB_USER=sa
export CHAT_DB_PASS=
```

---

## Testing Strategy

1. **Unit Tests** (ChatResourceTest, ChatServiceImplTest)
   - REST endpoint validation
   - Service logic verification
   - External API mocking

2. **Event Listener Tests** (AIDataProviderTest)
   - Verify @ApplicationModuleListener annotations
   - Test event reception and processing
   - Validate vector store updates

3. **Integration Tests** (GenAIModuleIntegrationTest)
   - Module structure validation
   - Cross-module dependency check
   - Event listener registration

---

## Ready to Implement?

Phase 6 will:
- ✅ Introduce asynchronous communication patterns
- ✅ Demonstrate event listening across all modules
- ✅ Integrate external Azure OpenAI service
- ✅ Build knowledge base from module events
- ✅ Provide AI chat API to clients

**Expected Outcome**: 4-5 complete iterations implementing 20+ files with comprehensive test coverage

---

## Next Phase Preview (Phase 7)

After GenAI module completes, Phase 7 will integrate the web layer:
- Thymeleaf templates for chat UI
- CSS/JS consolidation
- API gateway removal
- Single application deployment

This represents 90% project completion!

---

**Ready for Phase 6? 🤖🚀**
