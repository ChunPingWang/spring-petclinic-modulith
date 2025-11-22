# Architecture Decision Records (ADR)

## 1. 采用 Spring Modulith 作为架构基础

**日期**: 2025年11月

**状态**: ✅ 已采用

### 背景

原有的微服务架构包含 8 个独立服务，增加了部署和维护的复杂性。我们需要一个能够保持模块隔离同时简化部署的架构。

### 决策

采用 **Spring Modulith** 作为应用架构的基础，将微服务转化为模块化单体应用（Modular Monolith）。

### 理由

1. **降低复杂性**: 单一部署单元，简化 DevOps
2. **模块隔离**: 保持模块间的清晰边界
3. **事件驱动**: 通过事件实现模块间的解耦
4. **开发效率**: 本地开发更快速，无需启动多个服务
5. **Spring 生态**: 与 Spring 框架紧密集成

### 权衡

| 优点 | 缺点 |
|------|------|
| 简化部署 | 无法独立扩展模块 |
| 快速本地开发 | 模块间数据库共享 |
| 清晰的模块边界 | 需要遵守严格规则 |
| 事件驱动通讯 | 学习曲线 |

### 替代方案已考虑

- ❌ 继续使用微服务：太复杂
- ❌ 回到单体应用：无法保持模块隔离
- ❌ 使用 Spring Cloud：仍然需要多个部署单元

---

## 2. 数据库架构：共享数据库与模块表隔离

**日期**: 2025年11月

**状态**: ✅ 已采用

### 背景

在模块化单体中，需要决定数据库策略：
- 每个模块独立数据库？
- 共享数据库？
- 混合方案？

### 决策

采用 **共享数据库** 策略，每个模块拥有自己的表集合，严格隔离数据访问。

### 实现

```
数据库: petclinic
├── owners (customers 模块)
├── pets (customers 模块)
├── vets (vets 模块)
├── specialties (vets 模块)
├── visits (visits 模块)
└── event_publication (Spring Modulith)
```

### 理由

1. **简化部署**: 无需管理多个数据库
2. **事务一致性**: 跨模块事务更容易
3. **成本效益**: 单一数据库实例
4. **迁移平台**: 多个数据库可稍后优化

### 访问规则

```java
// ✅ 允许: 模块访问自己的表
CustomerRepository extends JpaRepository<Customer, Integer>

// ❌ 禁止: 跨模块直接访问表
VisitServiceImpl 访问 vets 表

// ✅ 允许: 通过服务接口调用
VisitServiceImpl 调用 VetService.findById()
```

---

## 3. 模块间通讯：事件驱动架构

**日期**: 2025年11月

**状态**: ✅ 已采用

### 背景

模块间需要通讯，两种主要方式：
- 直接调用（同步）
- 事件驱动（异步）

### 决策

优先使用 **事件驱动** 通讯，保留直接调用用于必要场景。

### 实现模式

#### 模式 1: 异步事件驱动

```java
// customers 模块发布事件
@Service
class CustomerServiceImpl implements CustomerService {
    private final ApplicationEventPublisher events;
    
    public Customer createCustomer(Customer customer) {
        Customer saved = repository.save(customer);
        events.publishEvent(new CustomerCreated(saved.getId()));
        return saved;
    }
}

// genai 模块监听事件
@Service
class AIDataProvider {
    @ApplicationModuleListener
    void on(CustomerCreated event) {
        // 更新向量数据库
        updateVectorStore(event.getCustomerId());
    }
}
```

#### 模式 2: 同步直接调用

```java
// visits 模块需要立即获取客户信息
@Service
class VisitServiceImpl implements VisitService {
    private final CustomerService customerService;  // 公开接口
    
    public Visit createVisit(Visit visit) {
        Customer customer = customerService.findById(visit.getCustomerId());
        // 验证客户存在...
        return repository.save(visit);
    }
}
```

### 通讯矩阵

| 源模块 | 目标模块 | 通讯方式 | 原因 |
|--------|--------|--------|------|
| customers | genai | 事件 | 异步更新向量库 |
| visits | customers | 同步调用 | 需要立即验证 |
| visits | vets | 同步调用 | 需要立即获取信息 |
| customers | vets | 事件 | 可选数据同步 |

---

## 4. AI 集成策略：Spring AI + ChatClient

**日期**: 2025年11月

**状态**: ✅ 已采用

### 背景

需要集成 AI 聊天功能，支持 OpenAI 和 Azure OpenAI。

### 决策

使用 **Spring AI** 框架，通过 ChatClient 与 LLM 交互。

### 实现

```yaml
# application.yml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      model: gpt-4o-mini
    azure:
      openai:
        api-key: ${AZURE_OPENAI_KEY}
        endpoint: ${AZURE_OPENAI_ENDPOINT}
```

### 关键特性

1. **向量存储 RAG**: 使用 SimpleVectorStore 存储宠物信息
2. **函数调用**: LLM 可以调用预定义函数获取实时数据
3. **流式响应**: 支持流式聊天回复
4. **模型切换**: 易于切换 OpenAI/Azure/其他提供商

### RAG 数据源

```java
// AIDataProvider 收集业务数据
void updateVectorStore(Integer customerId) {
    Customer customer = customerService.findById(customerId);
    Pet[] pets = petService.findByCustomerId(customerId);
    Visit[] visits = visitService.findByCustomerId(customerId);
    
    // 转换为向量并存储
    vectorStore.add(convertToDocuments(customer, pets, visits));
}
```

---

## 5. 可观测性架构：三支柱实现

**日期**: 2025年11月

**状态**: ✅ 已采用

### 背景

需要完整的应用可观测性（Logs、Metrics、Traces）。

### 决策

实现 **三支柱可观测性**：
- 📊 Metrics（指标）
- 📈 Traces（追踪）
- 📝 Logs（日志）

### 架构图

```
应用 (Spring Boot)
├── SLF4J Logging          ← 日志收集
├── Micrometer (1.14.0)    ← 指标收集
│   └── Prometheus Export  ← 指标导出
└── OpenTelemetry (1.44.0) ← 分布式追踪
    └── Zipkin (9.4.0)     ← 链路可视化

存储和可视化
├── Prometheus             ← 指标存储
├── Grafana                ← 仪表板
└── Zipkin UI              ← 追踪可视化
```

### 实现细节

#### 1. 指标收集

```java
@RestController
@Timed("petclinic.owner")  // 自动收集所有方法的指标
public class OwnerResource {
    // 自动记录：调用计数、响应时间、错误率
}
```

#### 2. 分布式追踪

```java
// 自动添加追踪信息到每个请求
// X-Trace-Id header 传播
// Zipkin 链路可视化
```

#### 3. 日志

```yaml
logging:
  level:
    org.springframework.samples.petclinic: DEBUG
    org.springframework.modulith: INFO
  pattern:
    console: "%d{HH:mm:ss} %-5level %logger{36} - %msg%n"
```

---

## 6. 测试策略：多层测试金字塔

**日期**: 2025年11月

**状态**: ✅ 已采用

### 决策

实现 **测试金字塔** 策略：

```
        集成测试 (5%)
       /            \
      /              \
    REST 端点测试 (15%)
    /                  \
  /                      \
单元测试 (80%)
```

### 测试类型

| 类型 | 框架 | 数量 | 目标 |
|------|------|------|------|
| 单元测试 | JUnit 5 | 28 | 业务逻辑 |
| REST 测试 | MockMvc | 10+ | 控制器验证 |
| 模块测试 | Spring Modulith | 5+ | 结构验证 |
| 集成测试 | @SpringBootTest | 待扩展 | 完整流程 |

### 示例

```java
// 单元测试
class CustomerServiceImplTest {
    private CustomerService service;
    
    @Test
    void shouldCreateCustomer() { }
}

// REST 测试
@WebMvcTest(OwnerResource.class)
class OwnerResourceTest {
    @Test
    void shouldGetAllOwners() { }
}

// 模块测试
class ModulithStructureTest {
    @Test
    void verifyModuleStructure() {
        ApplicationModules.of(PetClinicApplication.class).verify();
    }
}
```

---

## 7. 容器化策略：Docker 多平台支持

**日期**: 2025年11月

**状态**: ✅ 已采用

### 决策

使用 Docker 进行应用容器化，支持多个平台（AMD64、ARM64）。

### Dockerfile 特性

```dockerfile
# 多阶段构建
FROM eclipse-temurin:21-jdk AS builder
# 构建阶段...

FROM eclipse-temurin:21-jre
# 运行阶段...
```

### 多平台支持

```bash
# 自动检测平台
./mvnw clean install -P buildDocker

# 指定 ARM64 (Apple M2)
./mvnw clean install -P buildDocker -Dcontainer.platform="linux/arm64"

# 指定 AMD64 (Linux servers)
./mvnw clean install -P buildDocker -Dcontainer.platform="linux/amd64"
```

---

## 8. 前端集成：AngularJS + API 拦截器

**日期**: 2025年11月

**状态**: ✅ 已采用

### 决策

保持原有 AngularJS 前端，通过 API 拦截器转换请求路径。

### 原因

1. **向后兼容**: 不需要重写前端
2. **平滑迁移**: 微服务 → 单体的无缝过渡
3. **最小化改动**: 只需配置拦截器

### API 路由映射

```javascript
// api-config.js
.config(function($httpProvider) {
    $httpProvider.interceptors.push(function() {
        return {
            request: function(config) {
                // /api/customer/ → /  (localhost:8080)
                // /api/vet/      → /  (localhost:8080)
                // /api/visit/    → /  (localhost:8080)
                return config;
            }
        };
    });
})
```

原有微服务端点到新单体的映射：

| 旧端点 | 新端点 |
|--------|--------|
| localhost:8081/api/customer/* | localhost:8080/customers/* |
| localhost:8083/api/vet/* | localhost:8080/vets/* |
| localhost:8082/api/visit/* | localhost:8080/visits/* |
| localhost:8084/api/genai/* | localhost:8080/genai/* |

---

## 9. 部署策略：渐进式迁移

**日期**: 2025年11月

**状态**: ✅ 已采用

### 决策

采用 **蓝绿部署** 和 **金丝雀发布** 策略。

### 阶段

```
Phase 1: 内部验证
  └─ 开发/测试环境完整验证

Phase 2: 灰度发布
  └─ 10% 生产流量 → 新单体
  └─ 90% 生产流量 → 旧微服务

Phase 3: 扩大范围
  └─ 50% 生产流量 → 新单体
  └─ 50% 生产流量 → 旧微服务

Phase 4: 完全迁移
  └─ 100% 生产流量 → 新单体
  └─ 下线旧微服务
```

### 回滚计划

```bash
# 如果发现问题，快速回滚到旧微服务
kubectl set image deployment/petclinic petclinic=old-version
```

---

## 10. 监控和告警

**日期**: 2025年11月

**状态**: ✅ 已采用

### 关键监控指标

```yaml
# Prometheus 告警规则
- alert: HighErrorRate
  expr: rate(http_requests_total{status="5xx"}[5m]) > 0.05

- alert: HighLatency
  expr: histogram_quantile(0.95, http_request_duration_seconds_bucket) > 1

- alert: LowAvailability
  expr: up{job="petclinic"} == 0
```

### 告警通知

```
告警 → Prometheus → AlertManager → Slack/Email/PagerDuty
```

---

## 版本历史

| 版本 | 日期 | 决策 | 状态 |
|------|------|------|------|
| 1.0 | 2025-11-22 | 采用 Modulith | ✅ 已完成 |
| 待定 | - | 微服务网格 (Istio) | 📋 计划中 |
| 待定 | - | Kubernetes 部署 | 📋 计划中 |

---

**最后更新**: 2025-11-22  
**作者**: Spring PetClinic 重构团队
