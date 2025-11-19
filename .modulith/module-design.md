# Spring PetClinic Modulith - 模組邊界設計

## 📋 文檔狀態
- **版本**: 1.0
- **建立日期**: 2024
- **狀態**: Phase 1.2 設計階段

---

## 🎯 設計目標

將現有的 8 個微服務架構轉換為 5 個 Spring Modulith 模組:
1. **customers** - 客戶與寵物管理
2. **vets** - 獸醫與專長管理
3. **visits** - 就診記錄管理
4. **genai** - AI 聊天機器人
5. **shared** - 共享工具與基礎設施

---

## 📦 模組結構總覽

```
org.springframework.samples.petclinic/
├── PetClinicApplication.java           # 主應用程式入口
├── customers/                           # Customers 模組
│   ├── Customer.java                    # 公開 API: 客戶實體
│   ├── CustomerService.java             # 公開 API: 客戶服務介面
│   ├── Pet.java                         # 公開 API: 寵物實體
│   ├── PetService.java                  # 公開 API: 寵物服務介面
│   ├── CustomerCreated.java             # 領域事件
│   ├── PetAdded.java                    # 領域事件
│   └── internal/                        # 內部實作 (不可外部訪問)
│       ├── CustomerServiceImpl.java
│       ├── CustomerRepository.java
│       ├── PetRepository.java
│       ├── PetType.java                 # 內部實體
│       ├── PetTypeRepository.java
│       ├── CustomerEntityMapper.java
│       └── web/
│           ├── OwnerResource.java
│           └── PetResource.java
├── vets/                                # Vets 模組
│   ├── Vet.java                         # 公開 API: 獸醫實體
│   ├── VetService.java                  # 公開 API: 獸醫服務介面
│   ├── VetCreated.java                  # 領域事件
│   └── internal/                        # 內部實作
│       ├── VetServiceImpl.java
│       ├── VetRepository.java
│       ├── Specialty.java               # 內部實體
│       ├── SpecialtyRepository.java
│       └── web/
│           └── VetResource.java
├── visits/                              # Visits 模組
│   ├── Visit.java                       # 公開 API: 訪問實體
│   ├── VisitService.java                # 公開 API: 訪問服務介面
│   ├── VisitCreated.java                # 領域事件
│   └── internal/                        # 內部實作
│       ├── VisitServiceImpl.java
│       ├── VisitRepository.java
│       └── web/
│           └── VisitResource.java
├── genai/                               # GenAI 模組
│   ├── ChatService.java                 # 公開 API: 聊天服務介面
│   └── internal/                        # 內部實作
│       ├── ChatServiceImpl.java
│       ├── PetClinicChatClient.java
│       ├── config/
│       └── web/
│           └── ChatResource.java
└── shared/                              # 共享模組
    ├── exceptions/                      # 共享異常類別
    │   └── ResourceNotFoundException.java
    ├── config/                          # 共享配置
    │   ├── MetricsConfig.java
    │   └── ObservabilityConfig.java
    └── dto/                             # 共享 DTO (如需要)
```

---

## 🔍 模組邊界詳細設計

### 1. Customers 模組

#### 公開 API (`org.springframework.samples.petclinic.customers`)

**實體類別:**
```java
// Customer.java - 客戶實體 (對應現有的 Owner)
public class Customer {
    private Integer id;
    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String telephone;
    private Set<Pet> pets;  // 關聯寵物
}

// Pet.java - 寵物實體
public class Pet {
    private Integer id;
    private String name;
    private Date birthDate;
    private String typeName;  // 簡化型別為字串
    private Integer customerId;
}
```

**服務介面:**
```java
// CustomerService.java
public interface CustomerService {
    Optional<Customer> findById(Integer id);
    List<Customer> findAll();
    Customer create(CustomerRequest request);
    void update(Integer id, CustomerRequest request);
}

// PetService.java
public interface PetService {
    Optional<Pet> findById(Integer petId);
    List<Pet> findByCustomerId(Integer customerId);
    Pet addPet(Integer customerId, PetRequest request);
}
```

**領域事件:**
```java
// CustomerCreated.java
public record CustomerCreated(Integer customerId, String name) {}

// PetAdded.java
public record PetAdded(Integer petId, Integer customerId, String petName) {}
```

#### 內部實作 (`org.springframework.samples.petclinic.customers.internal`)

- `CustomerServiceImpl.java` - 服務實作
- `CustomerRepository.java` - JPA Repository
- `PetRepository.java` - JPA Repository
- `PetType.java` - 內部實體 (cat, dog, etc.)
- `PetTypeRepository.java`
- `CustomerEntityMapper.java` - Entity ↔ DTO 轉換
- `web/OwnerResource.java` - REST 控制器
- `web/PetResource.java` - REST 控制器

**模組職責:**
- ✅ 管理客戶資料 (CRUD)
- ✅ 管理寵物資料 (CRUD)
- ✅ 管理寵物類型
- ✅ 發布領域事件: `CustomerCreated`, `PetAdded`

**外部依賴:**
- ❌ 無外部模組依賴 (獨立模組)

---

### 2. Vets 模組

#### 公開 API (`org.springframework.samples.petclinic.vets`)

**實體類別:**
```java
// Vet.java - 獸醫實體
public class Vet {
    private Integer id;
    private String firstName;
    private String lastName;
    private Set<String> specialties;  // 簡化專長為字串集合
}
```

**服務介面:**
```java
// VetService.java
public interface VetService {
    Optional<Vet> findById(Integer id);
    List<Vet> findAll();
    Vet create(VetRequest request);
}
```

**領域事件:**
```java
// VetCreated.java
public record VetCreated(Integer vetId, String name) {}
```

#### 內部實作 (`org.springframework.samples.petclinic.vets.internal`)

- `VetServiceImpl.java`
- `VetRepository.java`
- `Specialty.java` - 內部實體
- `SpecialtyRepository.java`
- `web/VetResource.java`

**模組職責:**
- ✅ 管理獸醫資料
- ✅ 管理專長資料
- ✅ 發布領域事件: `VetCreated`

**外部依賴:**
- ❌ 無外部模組依賴

---

### 3. Visits 模組

#### 公開 API (`org.springframework.samples.petclinic.visits`)

**實體類別:**
```java
// Visit.java - 就診記錄實體
public class Visit {
    private Integer id;
    private Date date;
    private String description;
    private Integer petId;      // 外鍵: 關聯到 Pet
    private Integer vetId;      // 新增: 關聯到 Vet
}
```

**服務介面:**
```java
// VisitService.java
public interface VisitService {
    Optional<Visit> findById(Integer id);
    List<Visit> findByPetId(Integer petId);
    Visit createVisit(VisitRequest request);
}
```

**領域事件:**
```java
// VisitCreated.java
public record VisitCreated(Integer visitId, Integer petId, Integer vetId) {}
```

#### 內部實作 (`org.springframework.samples.petclinic.visits.internal`)

- `VisitServiceImpl.java`
- `VisitRepository.java`
- `web/VisitResource.java`

**模組職責:**
- ✅ 管理就診記錄
- ✅ 驗證 Pet 和 Vet 的存在性
- ✅ 發布領域事件: `VisitCreated`

**外部依賴:**
- ✅ `CustomerService` - 查詢 Pet 資訊
- ✅ `VetService` - 查詢 Vet 資訊

---

### 4. GenAI 模組

#### 公開 API (`org.springframework.samples.petclinic.genai`)

**服務介面:**
```java
// ChatService.java
public interface ChatService {
    String chat(String message);
}
```

#### 內部實作 (`org.springframework.samples.petclinic.genai.internal`)

- `ChatServiceImpl.java`
- `PetClinicChatClient.java`
- `config/OpenAIConfig.java`
- `web/ChatResource.java`

**模組職責:**
- ✅ 提供 AI 聊天機器人介面
- ✅ 監聽領域事件以更新向量資料庫

**外部依賴:**
- ✅ 監聽所有領域事件 (`CustomerCreated`, `PetAdded`, `VisitCreated`, etc.)

---

### 5. Shared 模組

#### 公開 API (`org.springframework.samples.petclinic.shared`)

**異常類別:**
```java
// exceptions/ResourceNotFoundException.java
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

**共享配置:**
```java
// config/MetricsConfig.java
@Configuration
public class MetricsConfig {
    // Micrometer 配置
}

// config/ObservabilityConfig.java
@Configuration
public class ObservabilityConfig {
    // OpenTelemetry, Zipkin 配置
}
```

**模組職責:**
- ✅ 提供共享異常類別
- ✅ 提供共享配置
- ✅ 提供共享工具類別

**外部依賴:**
- ❌ 無 (基礎模組)

---

## 🔄 模組通訊機制設計

### 同步通訊 (Direct Method Call)

**使用場景**: 需要立即返回結果的查詢操作

#### 1. Visits → Customers (查詢 Pet 資訊)
```java
// visits/internal/VisitServiceImpl.java
@Service
class VisitServiceImpl implements VisitService {
    private final PetService petService; // 注入公開介面
    
    @Override
    public Visit createVisit(VisitRequest request) {
        // 驗證 Pet 是否存在
        Pet pet = petService.findById(request.petId())
            .orElseThrow(() -> new ResourceNotFoundException("Pet not found"));
        
        // 建立 Visit
        Visit visit = new Visit();
        visit.setPetId(request.petId());
        visit.setDescription(request.description());
        return visitRepository.save(visit);
    }
}
```

#### 2. Visits → Vets (查詢 Vet 資訊)
```java
// visits/internal/VisitServiceImpl.java
@Service
class VisitServiceImpl implements VisitService {
    private final VetService vetService;
    
    @Override
    public Visit createVisit(VisitRequest request) {
        // 驗證 Vet 是否存在
        Vet vet = vetService.findById(request.vetId())
            .orElseThrow(() -> new ResourceNotFoundException("Vet not found"));
        
        // ... 建立 Visit
    }
}
```

---

### 異步通訊 (Event-Driven)

**使用場景**: 非關鍵路徑的通知、資料同步、緩存更新

#### 1. Customers 模組發布事件
```java
// customers/internal/CustomerServiceImpl.java
@Service
class CustomerServiceImpl implements CustomerService {
    private final ApplicationEventPublisher events;
    
    @Override
    public Customer create(CustomerRequest request) {
        Customer customer = // ... 儲存客戶
        
        // 發布事件
        events.publishEvent(new CustomerCreated(customer.getId(), customer.getFullName()));
        
        return customer;
    }
}
```

#### 2. GenAI 模組監聽事件
```java
// genai/internal/AIDataSyncListener.java
@Service
class AIDataSyncListener {
    
    @ApplicationModuleListener
    void on(CustomerCreated event) {
        // 更新向量資料庫
        log.info("Syncing customer data to vector DB: {}", event.customerId());
        // ... 非同步更新
    }
    
    @ApplicationModuleListener
    void on(PetAdded event) {
        // 更新向量資料庫
        log.info("Syncing pet data to vector DB: {}", event.petId());
    }
    
    @ApplicationModuleListener
    void on(VisitCreated event) {
        // 更新向量資料庫
        log.info("Syncing visit data to vector DB: {}", event.visitId());
    }
}
```

---

## 📊 領域事件清單

| 事件名稱 | 發布者模組 | 監聽者模組 | 目的 | 事件屬性 |
|---------|-----------|-----------|------|---------|
| `CustomerCreated` | customers | genai | 同步向量 DB | `customerId`, `name` |
| `CustomerUpdated` | customers | genai | 同步向量 DB | `customerId`, `name` |
| `PetAdded` | customers | genai | 同步向量 DB | `petId`, `customerId`, `petName` |
| `VetCreated` | vets | genai | 同步向量 DB | `vetId`, `name` |
| `VisitCreated` | visits | genai | 同步向量 DB | `visitId`, `petId`, `vetId` |

**事件設計原則:**
- ✅ 使用 Java Record 定義事件 (不可變)
- ✅ 包含最小必要資訊 (ID + 關鍵屬性)
- ✅ 使用過去式命名 (`Created`, `Updated`, `Deleted`)
- ✅ 事件放在模組公開 API 層級

---

## 🗄️ 資料庫架構設計

### 目標: 單一資料庫 + 共享 Schema

#### Schema 整合策略
```sql
-- 整合到單一 petclinic 資料庫

-- Customers 模組的表
CREATE TABLE owners (...);
CREATE TABLE pets (...);
CREATE TABLE types (...);

-- Vets 模組的表
CREATE TABLE vets (...);
CREATE TABLE specialties (...);
CREATE TABLE vet_specialties (...);

-- Visits 模組的表
CREATE TABLE visits (
  id INT PRIMARY KEY AUTO_INCREMENT,
  pet_id INT NOT NULL,
  vet_id INT,  -- 新增欄位
  visit_date DATE,
  description VARCHAR(8192),
  FOREIGN KEY (pet_id) REFERENCES pets(id)
);

-- Spring Modulith Event Publication
CREATE TABLE event_publication (
  id UUID PRIMARY KEY,
  event_type VARCHAR(512) NOT NULL,
  listener_id VARCHAR(512) NOT NULL,
  publication_date TIMESTAMP NOT NULL,
  serialized_event TEXT NOT NULL,
  completion_date TIMESTAMP
);
```

#### 外鍵關聯
- `pets.owner_id` → `owners.id` (保留)
- `visits.pet_id` → `pets.id` (保留)
- `visits.vet_id` → `vets.id` (新增)

**注意事項:**
- ⚠️ 雖然模組邏輯隔離,但資料庫 FK 約束需要保留以確保資料一致性
- ⚠️ 模組間不可直接存取其他模組的 Repository
- ✅ 必須透過服務介面進行資料存取

---

## 🚫 模組邊界違規檢測

### Spring Modulith 自動驗證

在主應用程式啟動時自動驗證模組邊界:

```java
// PetClinicApplication.java
@SpringBootApplication
public class PetClinicApplication {
    public static void main(String[] args) {
        // 驗證模組結構
        ApplicationModules modules = ApplicationModules.of(PetClinicApplication.class);
        modules.verify(); // 自動檢測違規
        
        SpringApplication.run(PetClinicApplication.class, args);
    }
}
```

### 常見違規情況

❌ **禁止**: 直接訪問其他模組的 `internal` 套件
```java
// visits/internal/VisitServiceImpl.java
import org.springframework.samples.petclinic.customers.internal.PetRepository; // ❌ 違規!

@Service
class VisitServiceImpl {
    private final PetRepository petRepository; // ❌ 不可直接注入 Repository
}
```

✅ **正確**: 使用公開服務介面
```java
// visits/internal/VisitServiceImpl.java
import org.springframework.samples.petclinic.customers.PetService; // ✅ 公開 API

@Service
class VisitServiceImpl {
    private final PetService petService; // ✅ 正確
}
```

---

## 🔍 模組依賴關係圖

```
         ┌─────────────┐
         │   Shared    │ (基礎層: 異常、配置)
         └─────────────┘
                ▲
                │
     ┌──────────┼──────────┐
     │          │          │
┌────┴────┐ ┌──┴───────┐ ┌┴─────┐
│Customers│ │   Vets   │ │GenAI │
└────┬────┘ └──┬───────┘ └──▲───┘
     │         │             │
     └─────┬───┘             │
           │                 │
       ┌───┴────┐            │
       │ Visits │────────────┘
       └────────┘
      (同步調用)    (異步事件)
```

**依賴說明:**
- `Visits` → `Customers` (同步查詢 Pet)
- `Visits` → `Vets` (同步查詢 Vet)
- `GenAI` ← 所有模組 (異步監聽事件)
- 所有模組 → `Shared` (共享基礎設施)

**無循環依賴 ✅**

---

## 📝 API 端點對應關係

### Customers 模組
| 原微服務端點 | Modulith 端點 | 控制器 |
|-------------|--------------|--------|
| `GET /api/customer/owners` | `GET /owners` | `OwnerResource` |
| `GET /api/customer/owners/{id}` | `GET /owners/{id}` | `OwnerResource` |
| `POST /api/customer/owners` | `POST /owners` | `OwnerResource` |
| `PUT /api/customer/owners/{id}` | `PUT /owners/{id}` | `OwnerResource` |
| `GET /api/customer/owners/{ownerId}/pets/{petId}` | `GET /owners/{ownerId}/pets/{petId}` | `PetResource` |
| `POST /api/customer/owners/{ownerId}/pets` | `POST /owners/{ownerId}/pets` | `PetResource` |

### Vets 模組
| 原微服務端點 | Modulith 端點 | 控制器 |
|-------------|--------------|--------|
| `GET /api/vet/vets` | `GET /vets` | `VetResource` |

### Visits 模組
| 原微服務端點 | Modulith 端點 | 控制器 |
|-------------|--------------|--------|
| `GET /api/visit/owners/{ownerId}/pets/{petId}/visits` | `GET /visits?petId={petId}` | `VisitResource` |
| `POST /api/visit/owners/{ownerId}/pets/{petId}/visits` | `POST /visits` | `VisitResource` |

### GenAI 模組
| 原微服務端點 | Modulith 端點 | 控制器 |
|-------------|--------------|--------|
| `POST /api/genai/chat` | `POST /genai/chat` | `ChatResource` |

---

## ✅ 設計檢查清單

- [x] 定義所有模組的公開 API
- [x] 定義所有模組的內部實作邊界
- [x] 識別模組間的依賴關係 (無循環依賴)
- [x] 設計領域事件清單
- [x] 規劃同步與異步通訊機制
- [x] 設計資料庫 schema 整合策略
- [x] 定義 API 端點對應關係
- [x] 確認 Spring Modulith 驗證機制

---

## 📚 參考資源

- [Spring Modulith Reference](https://docs.spring.io/spring-modulith/reference/)
- [Event-Driven Architecture](https://docs.spring.io/spring-modulith/reference/events.html)
- [Application Module Boundaries](https://docs.spring.io/spring-modulith/reference/fundamentals.html#modules)
