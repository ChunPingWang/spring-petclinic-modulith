# Spring PetClinic Modulith - 开发者指南

## 目录

1. [项目结构](#项目结构)
2. [模块开发](#模块开发)
3. [添加新功能](#添加新功能)
4. [测试](#测试)
5. [常见任务](#常见任务)
6. [故障排除](#故障排除)

## 项目结构

```
spring-petclinic-modulith/
├── spring-petclinic-modulith/          ← 主项目
│   ├── pom.xml                         ← Maven 配置
│   ├── README.md                       ← 项目文档
│   ├── Dockerfile                      ← Docker 镜像定义
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/                   ← Java 源代码
│   │   │   │   └── org/springframework/samples/petclinic/
│   │   │   │       ├── customers/      ← Customers 模块
│   │   │   │       ├── vets/           ← Vets 模块
│   │   │   │       ├── visits/         ← Visits 模块
│   │   │   │       ├── genai/          ← GenAI 模块
│   │   │   │       ├── shared/         ← 共享基础设施
│   │   │   │       └── PetClinicApplication.java
│   │   │   └── resources/
│   │   │       ├── application.yml     ← 主配置
│   │   │       ├── application-mysql.yml
│   │   │       ├── schema.sql          ← 数据库 schema
│   │   │       ├── data.sql            ← 测试数据
│   │   │       └── static/             ← 前端资源
│   │   └── test/
│   │       ├── java/                   ← 测试代码
│   │       └── resources/
│   │           └── application-test.yml
│   └── target/                         ← 构建输出
├── docker/                             ← Docker 配置
├── scripts/                            ← 工具脚本
└── docs/                               ← 文档目录
```

## 模块开发

### 模块目录结构

每个模块应遵循以下结构：

```
customers/
├── Customer.java                       ← 公开实体
├── CustomerService.java                ← 公开服务接口
├── CustomerCreated.java                ← 领域事件
├── internal/                           ← 内部实现
│   ├── CustomerServiceImpl.java
│   ├── CustomerRepository.java         ← JPA Repository
│   ├── Pet.java                        ← 内部实体
│   └── web/
│       ├── OwnerResource.java          ← REST 控制器
│       └── PetResource.java
└── CustomerEvent.java                  ← 事件监听器（可选）
```

### 模块边界规则

#### ✅ 允许的操作

1. **模块内部调用**
   ```java
   // customers 模块内调用
   @Service
   class CustomerServiceImpl {
       private final CustomerRepository repo;  // ✅ 允许
   }
   ```

2. **通过公开接口调用**
   ```java
   // visits 模块调用 customers 模块的公开 API
   @Service
   class VisitServiceImpl {
       private final CustomerService customerService;  // ✅ 允许
       
       void createVisit(Visit visit) {
           Customer customer = customerService.findById(visit.getCustomerId());
       }
   }
   ```

3. **通过事件驱动通讯**
   ```java
   // customers 模块发布事件
   events.publishEvent(new CustomerCreated(customer.getId()));
   
   // genai 模块监听事件
   @ApplicationModuleListener
   void on(CustomerCreated event) {
       // 更新向量数据库
   }
   ```

#### ❌ 禁止的操作

1. **访问内部包**
   ```java
   // ❌ 不允许！
   private final CustomerRepository repo;  // internal/ 包
   ```

2. **循环依赖**
   ```java
   // ❌ 不允许！
   // customers 模块依赖 visits
   // visits 模块依赖 customers
   ```

3. **绕过公开接口**
   ```java
   // ❌ 不允许！
   private final CustomerServiceImpl impl;  // 应该用 CustomerService
   ```

## 添加新功能

### 场景 1: 在现有模块中添加新实体

例如：在 customers 模块中添加地址管理

#### 步骤 1: 创建公开实体

```java
// customers/Address.java
package org.springframework.samples.petclinic.customers;

import jakarta.persistence.*;

@Entity
@Table(name = "addresses")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "street")
    private String street;
    
    @Column(name = "city")
    private String city;
    
    // getters/setters
}
```

#### 步骤 2: 创建公开服务接口

```java
// customers/AddressService.java
package org.springframework.samples.petclinic.customers;

import java.util.List;
import java.util.Optional;

public interface AddressService {
    List<Address> findAll();
    Optional<Address> findById(Integer id);
    Address save(Address address);
    void delete(Integer id);
}
```

#### 步骤 3: 实现服务

```java
// customers/internal/AddressServiceImpl.java
package org.springframework.samples.petclinic.customers.internal;

import org.springframework.stereotype.Service;
import org.springframework.samples.petclinic.customers.Address;
import org.springframework.samples.petclinic.customers.AddressService;

@Service
class AddressServiceImpl implements AddressService {
    private final AddressRepository repo;
    
    AddressServiceImpl(AddressRepository repo) {
        this.repo = repo;
    }
    
    @Override
    public List<Address> findAll() {
        return repo.findAll();
    }
    
    // ... 其他方法
}
```

#### 步骤 4: 创建 Repository

```java
// customers/internal/AddressRepository.java
package org.springframework.samples.petclinic.customers.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.samples.petclinic.customers.Address;

interface AddressRepository extends JpaRepository<Address, Integer> {
}
```

#### 步骤 5: 创建 REST 控制器

```java
// customers/internal/web/AddressResource.java
package org.springframework.samples.petclinic.customers.internal.web;

import io.micrometer.core.annotation.Timed;
import org.springframework.web.bind.annotation.*;
import org.springframework.samples.petclinic.customers.Address;
import org.springframework.samples.petclinic.customers.AddressService;

@RequestMapping("/addresses")
@RestController
@Timed("petclinic.address")
class AddressResource {
    private final AddressService addressService;
    
    AddressResource(AddressService addressService) {
        this.addressService = addressService;
    }
    
    @GetMapping
    public List<Address> listAddresses() {
        return addressService.findAll();
    }
    
    @GetMapping("/{id}")
    public Address getAddress(@PathVariable Integer id) {
        return addressService.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Address not found"));
    }
}
```

#### 步骤 6: 添加数据库 Schema

```sql
-- src/main/resources/schema.sql
CREATE TABLE addresses (
    id IDENTITY,
    street VARCHAR(255) NOT NULL,
    city VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

-- src/main/resources/schema-mysql.sql
CREATE TABLE addresses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    street VARCHAR(255) NOT NULL,
    city VARCHAR(255) NOT NULL
);
```

#### 步骤 7: 编写测试

```java
// src/test/java/org/springframework/samples/petclinic/customers/internal/AddressServiceImplTest.java
package org.springframework.samples.petclinic.customers.internal;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.customers.Address;
import org.springframework.samples.petclinic.customers.AddressService;

import static org.assertj.core.api.Assertions.*;

class AddressServiceImplTest {
    private AddressService addressService;
    private AddressRepository addressRepository;
    
    // setup and tests...
}
```

### 场景 2: 创建新模块

例如：创建 billing（账单）模块

#### 步骤 1: 创建模块目录结构

```bash
mkdir -p src/main/java/org/springframework/samples/petclinic/billing
mkdir -p src/main/java/org/springframework/samples/petclinic/billing/internal/web
mkdir -p src/test/java/org/springframework/samples/petclinic/billing
```

#### 步骤 2: 创建模块文件

```java
// src/main/java/org/springframework/samples/petclinic/billing/Invoice.java
package org.springframework.samples.petclinic.billing;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "invoices")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "invoice_number")
    private String invoiceNumber;
    
    @Column(name = "issue_date")
    private LocalDate issueDate;
    
    // ... 其他字段和方法
}
```

#### 步骤 3: 创建公开服务接口

```java
// src/main/java/org/springframework/samples/petclinic/billing/InvoiceService.java
package org.springframework.samples.petclinic.billing;

import java.util.Optional;

public interface InvoiceService {
    Invoice createInvoice(Invoice invoice);
    Optional<Invoice> findById(Integer id);
    // ... 其他方法
}
```

#### 步骤 4: 配置 Spring Modulith

Spring Modulith 会自动检测到新模块，只要它遵循命名约定。

#### 步骤 5: 验证模块结构

```bash
# 运行 ModuleTest 验证模块结构
./mvnw test -Dtest='ModulithStructureTest'
```

## 测试

### 单元测试

#### 服务测试

```java
@ExtendWith(SpringExtension.class)
@DisplayName("CustomerService Tests")
class CustomerServiceImplTest {
    
    private CustomerService customerService;
    private CustomerRepository customerRepository;
    
    @BeforeEach
    void setUp() {
        customerRepository = mock(CustomerRepository.class);
        customerService = new CustomerServiceImpl(customerRepository);
    }
    
    @Test
    @DisplayName("should create customer successfully")
    void shouldCreateCustomer() {
        // Arrange
        Customer customer = new Customer();
        customer.setFirstName("John");
        customer.setLastName("Doe");
        
        given(customerRepository.save(customer))
            .willReturn(customer);
        
        // Act
        Customer result = customerService.createCustomer(customer);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("John");
        verify(customerRepository).save(customer);
    }
}
```

#### REST 控制器测试

```java
@ExtendWith(SpringExtension.class)
@WebMvcTest(OwnerResource.class)
@ActiveProfiles("test")
class OwnerResourceTest {
    
    @Autowired
    MockMvc mvc;
    
    @MockBean
    CustomerService customerService;
    
    @Test
    void shouldGetAllOwnersInJsonFormat() throws Exception {
        // Given
        List<Customer> customers = List.of(
            new Customer(1, "John", "Doe"),
            new Customer(2, "Jane", "Smith")
        );
        given(customerService.findAll())
            .willReturn(customers);
        
        // When & Then
        mvc.perform(get("/owners")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }
}
```

### 集成测试

```java
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CustomersModuleIntegrationTest extends BaseIntegrationTest {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Test
    void verifyModuleStructure() {
        ApplicationModules modules = ApplicationModules.of(PetClinicApplication.class);
        modules.verify();  // 验证无循环依赖
    }
}
```

### 模块结构测试

```java
class ModulithStructureTest {
    @Test
    void verifiesModuleStructure() {
        ApplicationModules.of(PetClinicApplication.class).verify();
    }
}
```

### 运行测试

```bash
# 运行所有测试
./mvnw test

# 运行特定测试类
./mvnw test -Dtest='CustomerServiceImplTest'

# 运行特定测试方法
./mvnw test -Dtest='CustomerServiceImplTest#shouldCreateCustomer'

# 生成测试覆盖率
./mvnw test jacoco:report
```

## 常见任务

### 添加新依赖

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-xxxx</artifactId>
</dependency>
```

然后运行：
```bash
./mvnw clean install
```

### 更改数据库配置

编辑 `src/main/resources/application-mysql.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://your-host:3306/petclinic
    username: your-user
    password: your-password
```

### 添加新的 REST 端点

1. 在 `internal/web/` 中创建 Resource 类
2. 使用 `@RestController` 和 `@RequestMapping` 注解
3. 添加 `@Timed` 注解用于监控
4. 编写测试

```java
@RestController
@RequestMapping("/new-resource")
@Timed("petclinic.new-resource")
public class NewResource {
    @GetMapping
    public ResponseEntity<?> getAll() {
        // ...
    }
}
```

### 配置 AI 功能

1. 设置环境变量：
```bash
export OPENAI_API_KEY=sk-...
```

2. 或在 `application.yml` 中配置：
```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      model: gpt-4o-mini
```

### 启用日志调试

在 `application.yml` 中：
```yaml
logging:
  level:
    org.springframework.samples.petclinic: DEBUG
    org.springframework.modulith: DEBUG
```

或通过 Actuator：
```bash
curl -X POST \
  http://localhost:8080/actuator/loggers/org.springframework.samples.petclinic \
  -H 'Content-Type: application/json' \
  -d '{"configuredLevel": "DEBUG"}'
```

## 故障排除

### 编译错误

**错误**: `[ERROR] symbol: class Customer not found`

**原因**: 类不在公开 API 中或导入路径错误

**解决**:
```java
// ❌ 错误
import org.springframework.samples.petclinic.customers.internal.CustomerImpl;

// ✅ 正确
import org.springframework.samples.petclinic.customers.Customer;
```

### 测试失败

**错误**: `IllegalStateException: Failed to load ApplicationContext`

**原因**: 测试配置问题

**解决**:
1. 确保使用 `@ActiveProfiles("test")`
2. 检查 `application-test.yml` 配置
3. 清理并重新构建：`./mvnw clean test`

### 模块验证错误

**错误**: `Module 'customers' depends on undeclared module 'internal'`

**原因**: 违反了模块边界规则

**解决**: 
- 确保不访问 `internal/` 包中的类
- 仅使用公开接口

### 数据库连接错误

**错误**: `Communications link failure`

**原因**: MySQL 未运行或连接参数错误

**解决**:
```bash
# 检查 MySQL 是否运行
docker ps | grep mysql

# 验证连接参数
echo $SPRING_DATASOURCE_URL
echo $SPRING_DATASOURCE_USERNAME
```

### 端口被占用

**错误**: `Address already in use: bind`

**原因**: 端口 8080 已被使用

**解决**:
```bash
# 使用不同的端口
./mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"

# 或杀死占用的进程
lsof -ti:8080 | xargs kill -9
```

## 最佳实践

### 1. 代码组织

- ✅ 保持模块小且专注
- ✅ 使用有意义的包名
- ✅ 遵循单一职责原则
- ❌ 不要创建过大的 God Classes

### 2. 依赖管理

- ✅ 优先使用构造函数注入
- ✅ 使用 Optional 处理可能为 null 的值
- ✅ 定义明确的公开接口
- ❌ 不要使用字段注入

### 3. 测试

- ✅ 为每个类编写单元测试
- ✅ 使用 Mock 对象隔离依赖
- ✅ 遵循 Arrange-Act-Assert 模式
- ❌ 不要跳过测试

### 4. 文档

- ✅ 使用 JavaDoc 记录公开 API
- ✅ 在 README 中说明模块用途
- ✅ 提供使用示例
- ❌ 不要忘记更新文档

---

**最后更新**: 2025-11-22  
**维护者**: Spring PetClinic 重构团队
