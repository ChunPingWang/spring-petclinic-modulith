# Spring PetClinic Modulith - 监控与可观测性配置

## 📊 概述

本文档描述 Phase 9 实现的监控和可观测性功能，包括：
- Spring Boot Actuator 端点
- Micrometer 指标收集和 Prometheus 导出
- OpenTelemetry 分布式追踪
- Zipkin 追踪可视化

## 🔧 启用的 Actuator 端点

### 1. **健康检查** (`/actuator/health`)
```bash
curl http://localhost:8080/actuator/health
```

**响应示例**：
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "HSQLDB",
        "validationQuery": "isValid()"
      }
    },
    "petclinic-modulith": {
      "status": "UP",
      "details": {
        "application": "Spring PetClinic Modulith",
        "modules": "Customers, Vets, Visits, GenAI",
        "architecture": "Spring Modulith 1.3.0"
      }
    }
  }
}
```

### 2. **应用信息** (`/actuator/info`)
```bash
curl http://localhost:8080/actuator/info
```

显示应用构建信息、版本和Git提交ID。

### 3. **Spring Modulith 结构** (`/actuator/modulith`)
```bash
curl http://localhost:8080/actuator/modulith
```

显示模块结构、依赖关系和模块边界信息。

### 4. **自定义 PetClinic 健康** (`/actuator/petclinic-health`)
```bash
curl http://localhost:8080/actuator/petclinic-health
```

**响应示例**：
```json
{
  "status": "UP",
  "application": "Spring PetClinic Modulith",
  "modules": {
    "customers": {
      "name": "customers",
      "displayName": "Customers",
      "basePackage": "org.springframework.samples.petclinic.customers",
      "status": "UP"
    },
    "vets": {
      "name": "vets",
      "displayName": "Vets",
      "basePackage": "org.springframework.samples.petclinic.vets",
      "status": "UP"
    },
    "visits": {
      "name": "visits",
      "displayName": "Visits",
      "basePackage": "org.springframework.samples.petclinic.visits",
      "status": "UP"
    },
    "genai": {
      "name": "genai",
      "displayName": "GenAI",
      "basePackage": "org.springframework.samples.petclinic.genai",
      "status": "UP"
    }
  },
  "timestamp": 1700000000000
}
```

### 5. **Prometheus 指标** (`/actuator/prometheus`)
```bash
curl http://localhost:8080/actuator/prometheus
```

暴露所有指标，Prometheus 服务器可以进行爬取。

### 6. **环境变量** (`/actuator/env`)
```bash
curl http://localhost:8080/actuator/env
```

显示应用的环境变量和配置属性（生产环境建议禁用）。

### 7. **日志级别** (`/actuator/loggers`)
```bash
curl http://localhost:8080/actuator/loggers
```

查看和修改应用运行时日志级别。

## 📈 Micrometer 指标

### 自动收集的指标

#### HTTP 请求指标
- `http.server.requests` - HTTP 请求时间和计数
  - 标签: `method`, `status`, `uri`, `exception`
  - 分位数直方图: 50ms, 100ms, 200ms, 500ms, 1s, 2s

#### JVM 指标
- `jvm.memory.*` - JVM 内存使用情况
- `jvm.threads.*` - 活跃线程数
- `jvm.gc.*` - 垃圾回收活动
- `process.cpu.*` - CPU 使用情况

### 自定义业务指标

#### Customers 模块
- `petclinic.owners.created` - 创建的所有者总数
- `petclinic.pets.created` - 创建的宠物总数

#### Visits 模块
- `petclinic.visits.scheduled` - 排定的访问总数
- `petclinic.visits.completed` - 完成的访问总数

#### GenAI 模块
- `petclinic.chat.interactions` - 聊天交互总数

#### REST 控制器（使用 @Timed 注解）
- `petclinic.owner` - 所有者控制器操作
- `petclinic.pet` - 宠物控制器操作
- `petclinic.visit` - 访问控制器操作
- `petclinic.vet` - 兽医控制器操作
- `petclinic.chat` - 聊天控制器操作

### 查询示例

```bash
# 查看所有 HTTP 请求指标
curl 'http://localhost:8080/actuator/prometheus' | grep http_server_requests

# 查看 JVM 内存使用
curl 'http://localhost:8080/actuator/prometheus' | grep jvm_memory

# 查看创建的所有者数
curl 'http://localhost:8080/actuator/prometheus' | grep petclinic_owners_created
```

## 🔍 分布式追踪

### 追踪配置

追踪已启用，采样概率为 100%（所有请求都被追踪）。

### Zipkin 集成

追踪数据发送到 Zipkin，可在以下地址访问：
```
http://localhost:9411
```

### 追踪 ID 传播

每个请求都会生成或接收一个唯一的追踪 ID：
- 请求头: `X-Trace-Id`
- 响应头: `X-Trace-Id` (反映请求的追踪 ID)

### 追踪示例

```bash
# 发送请求并获取追踪 ID
curl -v http://localhost:8080/owners | grep X-Trace-Id

# 使用追踪 ID 查询请求日志
# 在应用日志中搜索追踪 ID 即可找到所有相关请求
```

### 在 Zipkin 中查看追踪

1. 打开 Zipkin UI: http://localhost:9411
2. 选择 "spring-petclinic-modulith" 服务
3. 搜索或浏览最近的追踪
4. 点击追踪可查看详细的跨度信息

## 📊 监控工具集成

### Prometheus

Prometheus 可以爬取以下端点收集指标：
```yaml
scrape_configs:
  - job_name: 'spring-petclinic'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
```

### Grafana

创建仪表板以可视化 Prometheus 指标：
1. 添加 Prometheus 数据源
2. 创建查询来显示关键指标
3. 配置告警规则

示例查询：
```
# 平均响应时间
rate(http_server_requests_seconds_sum[5m]) / rate(http_server_requests_seconds_count[5m])

# 请求错误率
rate(http_server_requests_seconds_count{status=~"5.."}[5m])

# 创建的所有者总数
petclinic_owners_created_total
```

## 🚀 启动应用进行监控

### 启动基础应用
```bash
cd spring-petclinic-modulith
./mvnw spring-boot:run
```

### 启动完整堆栈（包括 Prometheus、Zipkin、Grafana）
```bash
# 使用 Docker Compose
docker-compose up
```

## ✅ 验证清单

- [x] Actuator 端点已启用
- [x] Micrometer 指标收集已配置
- [x] Prometheus 导出已启用
- [x] 分布式追踪已启用
- [x] Zipkin 集成已配置
- [x] 自定义业务指标已定义
- [x] 健康指示器已实现
- [x] 所有 REST 控制器已添加 @Timed 注解
- [x] 请求/响应日志拦截器已实现

## 📝 配置参考

### application.yml 中的关键配置

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,modulith,env,loggers
  endpoint:
    modulith:
      enabled: true
  metrics:
    distribution:
      percentiles-histogram:
        http.server.requests: true
  tracing:
    sampling:
      probability: 1.0
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans
```

## 🔗 相关资源

- [Spring Boot Actuator 文档](https://docs.spring.io/spring-boot/docs/3.4.1/reference/html/actuator.html)
- [Micrometer 文档](https://micrometer.io/)
- [Spring Modulith 文档](https://docs.spring.io/spring-modulith/reference/)
- [Zipkin 文档](https://zipkin.io/)
- [Prometheus 文档](https://prometheus.io/)
- [Grafana 文档](https://grafana.com/docs/)

---

**更新时间**: 2025-11-21  
**版本**: Spring PetClinic Modulith 3.4.1
