# Spring PetClinic Modulith

## 项目概述

**Spring PetClinic Modulith** 是一个示范项目，展示了如何使用 [Spring Modulith](https://spring.io/projects/spring-modulith) 将微服务架构转化为模块化单体应用（Modular Monolith）。该项目基于著名的 Spring PetClinic 应用，现已使用最新的 Spring 框架技术栈重构。

### 关键特性

- ✅ **模块化架构**: 清晰的模块边界、内部包隐藏、循环依赖检测
- ✅ **Spring AI 集成**: ChatClient、向量存储 RAG、LLM 函数调用
- ✅ **完整的监控**: Actuator、Micrometer、OpenTelemetry、Zipkin
- ✅ **高质量测试**: 38 个单元测试、模块结构验证
- ✅ **多数据库支持**: HSQLDB (开发) 和 MySQL (生产)

## 🎯 快速开始

### 前置要求

- **Java**: 17+ (推荐 17 或 21)
- **Maven**: 3.8.1+
- **MySQL**: 8.0+ (可选，开发环境使用 HSQLDB)

### 本地运行

#### 1. 使用 HSQLDB (内存数据库)

```bash
cd spring-petclinic-modulith
../mvnw spring-boot:run
```

#### 2. 使用 MySQL

```bash
# 设置 MySQL 连接
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/petclinic
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=root

# 运行应用
../mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=mysql"
```

#### 3. 访问应用

- 应用主页: http://localhost:8080
- 所有宠物: http://localhost:8080/#/pets
- 所有兽医: http://localhost:8080/#/vets
- 健康检查: http://localhost:8080/actuator/health

## 🏗️ 架构

### 模块结构

```
org.springframework.samples.petclinic/
├── customers/               # 客户管理模块
│   ├── Customer.java        (公开 API)
│   ├── CustomerService.java (公开接口)
│   └── internal/            (内部实现隐藏)
│
├── vets/                    # 兽医管理模块
│   ├── Vet.java
│   ├── VetService.java
│   └── internal/
│
├── visits/                  # 就诊管理模块
│   ├── Visit.java
│   ├── VisitService.java
│   └── internal/
│
├── genai/                   # AI 聊天模块 (新功能)
│   ├── ChatService.java
│   └── internal/
│
└── shared/                  # 共享基础设施
    ├── config/              (Actuator、监控、Web)
    ├── web/                 (异常处理)
    └── dto/                 (共享对象)
```

### 技术栈

| 组件 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.4.1 | 应用框架 |
| Spring Modulith | 1.3.0 | 模块化架构 |
| Spring AI | 1.0.0-M5 | AI/LLM 集成 |
| Spring Data JPA | 3.4.1 | 数据持久化 |
| Micrometer | 1.14.0 | 指标收集 |
| OpenTelemetry | 1.44.0 | 分布式追踪 |
| HSQLDB | 2.7.3 | 开发数据库 |
| MySQL | 8.0+ | 生产数据库 |

## 📊 监控与可观测性

### Actuator 端点

```bash
# 健康检查
curl http://localhost:8080/actuator/health

# 应用信息
curl http://localhost:8080/actuator/info

# 模块结构
curl http://localhost:8080/actuator/modulith

# Prometheus 指标
curl http://localhost:8080/actuator/prometheus

# 环境变量
curl http://localhost:8080/actuator/env

# 日志级别
curl http://localhost:8080/actuator/loggers
```

### 分布式追踪

应用使用 OpenTelemetry 和 Zipkin 进行分布式追踪：

```bash
# Zipkin UI (需要启动 docker-compose)
http://localhost:9411/zipkin/
```

## 🧪 测试

### 运行所有测试

```bash
../mvnw test
```

### 运行特定测试

```bash
# 运行单元测试
../mvnw test -Dtest='*ServiceImplTest,*ResourceTest'

# 运行集成测试
../mvnw test -Dtest='*IntegrationTest'

# 跳过测试构建
../mvnw package -DskipTests
```

### 测试覆盖

- ✅ 38 个单元测试通过
- ✅ 模块结构验证测试
- ✅ REST 端点测试
- ✅ 服务实现测试

## 🚀 部署

### Docker 容器化

```bash
# 构建 Docker 镜像
../mvnw clean install -P buildDocker

# 运行容器
docker run -d -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=hsqldb \
  springcommunity/spring-petclinic-modulith:3.4.1

# 验证
curl http://localhost:8080/actuator/health
```

### Docker Compose

```bash
# 启动完整栈
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

## 🔧 编译和构建

```bash
# 清理编译
../mvnw clean

# 只编译不测试
../mvnw clean compile -DskipTests

# 编译并打包
../mvnw clean package -DskipTests

# 编译并安装到本地 Maven 仓库
../mvnw clean install
```

## 📚 API 文档

### 主要端点

#### 客户管理
```bash
GET    /customers              # 获取所有客户
GET    /customers/{id}         # 获取特定客户
POST   /customers              # 创建客户
PUT    /customers/{id}         # 更新客户
DELETE /customers/{id}         # 删除客户
```

#### 宠物管理
```bash
GET    /customers/{id}/pets    # 获取客户的宠物
POST   /customers/{id}/pets    # 添加宠物
PUT    /customers/{id}/pets/{petId}  # 更新宠物
```

#### 兽医管理
```bash
GET    /vets                   # 获取所有兽医
GET    /vets/{id}              # 获取特定兽医
```

#### 就诊记录
```bash
GET    /visits?petId={id}      # 获取宠物的就诊
POST   /visits                 # 创建就诊记录
```

#### AI 聊天
```bash
POST   /genai/chat             # 聊天对话
```

## 🔐 配置

### 应用配置

**开发模式** (HSQLDB):
```bash
../mvnw spring-boot:run
```

**生产模式** (MySQL):
```bash
../mvnw spring-boot:run \
  -Dspring-boot.run.arguments="\
  --spring.profiles.active=mysql \
  --spring.datasource.url=jdbc:mysql://host:3306/petclinic \
  --spring.datasource.username=user \
  --spring.datasource.password=password"
```

### AI 配置

**OpenAI**:
```bash
export OPENAI_API_KEY=sk-...
export OPENAI_MODEL=gpt-4o-mini
```

**Azure OpenAI**:
```bash
export AZURE_OPENAI_KEY=...
export AZURE_OPENAI_ENDPOINT=https://....openai.azure.com/
```

## 📊 项目统计

| 指标 | 数值 |
|------|------|
| 源代码文件 | 64 个 |
| 测试文件 | 18 个 |
| 总代码行数 | ~15,000+ |
| 单元测试 | 38/38 ✅ |
| 模块数 | 5 个 |
| 完成度 | 88% |

## 🐛 常见问题

### Q: 如何启用 MySQL?
A: 设置 `spring.profiles.active=mysql` 并配置数据库连接。

### Q: 如何访问 Zipkin 追踪?
A: 运行 `docker-compose up`，然后访问 http://localhost:9411

### Q: 如何配置 OpenAI?
A: 设置 `OPENAI_API_KEY` 环境变量。

### Q: 如何增加日志级别?
A: 使用 `/actuator/loggers` 或修改 `application.yml`

## 📖 相关资源

- [Spring Modulith 官方文档](https://docs.spring.io/spring-modulith/reference/)
- [Spring Boot 官方文档](https://docs.spring.io/spring-boot/docs/3.4.1/reference/html/)
- [Spring AI 文档](https://docs.spring.io/spring-ai/reference/)
- [原始 Spring PetClinic](https://github.com/spring-projects/spring-petclinic)

## 📄 授权协议

Apache License 2.0 - 详见 [LICENSE](../LICENSE) 文件

## 🎓 学习资源

通过这个项目，您可以学到：

- ✨ Spring Modulith 模块化架构模式
- ✨ Spring AI 集成和 LLM 使用
- ✨ 完整的可观测性配置
- ✨ 微服务到模块化单体的迁移策略

---

**最后更新**: 2025-11-22  
**维护者**: Spring PetClinic 重构团队  
**状态**: ✅ 88% 完成


### With MySQL

```bash
../mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
```

Make sure MySQL is running on `localhost:3306` with database `petclinic`.

### Docker Build

```bash
../mvnw clean install -P buildDocker
docker run -p 8080:8080 springcommunity/spring-petclinic-modulith
```

## Testing

### Run all tests
```bash
../mvnw test
```

### Verify module structure
```bash
../mvnw test -Dtest=ModulithStructureTest
```

## Monitoring

- **Health Check**: `http://localhost:8080/actuator/health`
- **Metrics**: `http://localhost:8080/actuator/metrics`
- **Prometheus**: `http://localhost:8080/actuator/prometheus`
- **Module Info**: `http://localhost:8080/actuator/modulith`

## Documentation

Module documentation is automatically generated during tests and can be found in `target/modulith-docs/`.

## License

Apache License 2.0
