# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Spring PetClinic Microservices is a distributed version of the Spring PetClinic application demonstrating microservices architecture using Spring Cloud, Spring AI, and supporting technologies including Spring Cloud Gateway, Circuit Breaker, Config Server, Micrometer Tracing, Resilience4j, OpenTelemetry, and Eureka Service Discovery.

## Technology Stack

- **Java 17** (required)
- **Spring Boot 3.4.1**
- **Spring Cloud 2024.0.0**
- **Maven** for build management
- **Docker/Podman** for containerization
- **HSQLDB** (default) or **MySQL 8.4.5 LTS** for persistence

## Build and Development Commands

### Building the Project

```bash
# Build all modules
./mvnw clean install

# Build specific module
./mvnw clean install -pl spring-petclinic-customers-service

# Skip tests
./mvnw clean install -DskipTests

# Run tests only
./mvnw test

# Run tests for specific module
./mvnw test -pl spring-petclinic-vets-service
```

### Running Services Locally (without Docker)

Services must be started in this order:
1. **Config Server** - `cd spring-petclinic-config-server && ../mvnw spring-boot:run`
2. **Discovery Server** - `cd spring-petclinic-discovery-server && ../mvnw spring-boot:run`
3. **Customers Service** - `cd spring-petclinic-customers-service && ../mvnw spring-boot:run`
4. **Vets Service** - `cd spring-petclinic-vets-service && ../mvnw spring-boot:run`
5. **Visits Service** - `cd spring-petclinic-visits-service && ../mvnw spring-boot:run`
6. **GenAI Service** - `cd spring-petclinic-genai-service && ../mvnw spring-boot:run` (requires OPENAI_API_KEY or Azure OpenAI credentials)
7. **API Gateway** - `cd spring-petclinic-api-gateway && ../mvnw spring-boot:run`

Optional supporting services (Admin Server, Tracing Server, Grafana, Prometheus) can be started afterwards.

### Docker Operations

```bash
# Build Docker images (requires Docker/Docker Desktop running)
./mvnw clean install -P buildDocker

# Build for Podman
./mvnw clean install -PbuildDocker -Dcontainer.executable=podman

# Build for specific platform (e.g., Apple Silicon M2)
./mvnw clean install -P buildDocker -Dcontainer.platform="linux/arm64"

# Start all services with Docker Compose
docker compose up

# Start services with Podman
podman-compose up

# Stop all services
docker compose down
```

### Alternative: Hybrid Docker + Java Approach

If experiencing Docker Compose issues, use the helper script:
```bash
./scripts/run_all.sh
```
This starts infrastructure services via docker-compose and Java applications via nohup. Logs are available at `target/nameoftheapp.log`.

### Pushing to Docker Registry

```bash
# Set repository prefix (Docker Hub)
export REPOSITORY_PREFIX=springcommunity

# Set repository prefix (custom registry)
export REPOSITORY_PREFIX=harbor.myregistry.com/petclinic

# Build and push multi-platform images
mvn clean install -Dmaven.test.skip -P buildDocker \
  -Ddocker.image.prefix=${REPOSITORY_PREFIX} \
  -Dcontainer.build.extraarg="--push" \
  -Dcontainer.platform="linux/amd64,linux/arm64"
```

### CSS Compilation

The API Gateway uses SCSS with Bootstrap. To recompile CSS after modifying `petclinic.scss`:
```bash
cd spring-petclinic-api-gateway
mvn generate-resources -P css
```

## Microservices Architecture

### Service Responsibilities

- **config-server** (8888): Centralized external configuration using Spring Cloud Config. Pulls configuration from [spring-petclinic-microservices-config](https://github.com/spring-petclinic/spring-petclinic-microservices-config) repository. Can use local Git repo with `native` profile and `GIT_REPO` environment variable.

- **discovery-server** (8761): Eureka-based service registry for service discovery. All services register here and use it for service-to-service communication.

- **api-gateway** (8080): Spring Cloud Gateway routing requests to backend services. Implements circuit breaker patterns with Resilience4j and retry logic. Routes are configured in `application.yml` with path-based routing (e.g., `/api/customer/**` → `lb://customers-service`).

- **customers-service** (random port): Manages owner and pet data. Exposes REST endpoints through `OwnerResource` and `PetResource`. Uses `@Timed` annotations for custom metrics (`petclinic.owner`, `petclinic.pet`).

- **vets-service** (random port): Manages veterinarian information and specialties.

- **visits-service** (random port): Handles pet visit records. Uses `@Timed` annotation for `petclinic.visit` metrics.

- **genai-service** (random port): Spring AI chatbot integration supporting OpenAI or Azure OpenAI. Requires API credentials via environment variables. Supports natural language queries about owners, vets, and pets.

- **admin-server** (9090): Spring Boot Admin for monitoring and managing microservices.

- **tracing-server** (9411): OpenZipkin for distributed tracing across services.

- **grafana-server** (3030): Dashboards for metrics visualization.

- **prometheus-server** (9091): Metrics collection and monitoring.

### Service Communication

Services communicate via:
1. **Service Discovery**: Eureka client registration and lookup
2. **Load Balancing**: `lb://` URIs in Gateway routes resolve via Eureka
3. **Configuration**: All services import config from Config Server via `spring.config.import`
4. **Circuit Breaker**: Gateway applies default circuit breaker and retry filters to all routes

### Startup Dependencies

Docker Compose coordinates startup using `depends_on` with `service_healthy` conditions and healthcheck configurations. Config Server and Discovery Server must be healthy before business services start.

## Database Configuration

### Default: HSQLDB In-Memory

By default, services use HSQLDB populated at startup. No additional configuration needed.

### MySQL Configuration

To use MySQL:

1. Start MySQL:
   ```bash
   docker run -e MYSQL_ROOT_PASSWORD=petclinic -e MYSQL_DATABASE=petclinic -p 3306:3306 mysql:8.4.5
   ```

2. Start services with `mysql` profile:
   ```bash
   --spring.profiles.active=mysql
   ```

3. For Docker deployments, modify `docker/Dockerfile`:
   ```dockerfile
   ENV SPRING_PROFILES_ACTIVE docker,mysql
   ```

4. Update MySQL connection in [Configuration repository](https://github.com/spring-petclinic/spring-petclinic-microservices-config) `application.yml` mysql section.

Only `visits-service`, `customers-service`, and `vets-service` require MySQL configuration.

## GenAI Service Configuration

The GenAI service requires LLM provider credentials:

### OpenAI (default):
```bash
export OPENAI_API_KEY="your_api_key_here"
```

### Azure OpenAI:
```bash
export AZURE_OPENAI_ENDPOINT="https://your_resource.openai.azure.com"
export AZURE_OPENAI_KEY="your_api_key_here"
```

Change the dependency in `spring-petclinic-genai-service/pom.xml` between `spring-ai-openai-spring-boot-starter` and `spring-ai-azure-openai-spring-boot-starter` as needed.

For testing, use the `demo` API key (limited to `gpt-4o-mini` model). For `gpt-4o`, modify the `deployment-name` in `application.yml`.

## Monitoring and Metrics

### Custom Metrics

Services use Micrometer with `@Timed` annotations:
- `customers-service`: `petclinic.owner`, `petclinic.pet`
- `visits-service`: `petclinic.visit`

### Access Points
- Prometheus: http://localhost:9091
- Grafana Dashboard: http://localhost:3030/d/69JXeR0iw/spring-petclinic-metrics
- Grafana configuration: `docker/grafana/dashboards/grafana-petclinic-dashboard.json`

### Load Testing

Use the JMeter test plan to generate metrics:
```
spring-petclinic-api-gateway/src/test/jmeter/petclinic_test_plan.jmx
```

## Chaos Engineering

Services started via `./scripts/run_all.sh` include the `chaos-monkey` profile. Use the chaos helper script:
```bash
./scripts/chaos/call_chaos.sh
```
See `scripts/chaos/README.md` for assault configuration.

## Code Structure

Each microservice follows a consistent package structure under `org.springframework.samples.petclinic.<service-name>`:

- `model/` - JPA entities and data models
- `web/` - REST controllers (Resource classes) and DTOs
- `config/` - Spring configuration classes (e.g., MetricConfig)

Services use Spring Data JPA repositories for persistence and expose REST APIs through Resource classes.

## Important Development Notes

- **Java Version**: Project requires Java 17. Maven enforcer plugin will fail the build with a clear error message if using wrong version.
- **Service Startup Order**: When running locally without Docker, Config Server and Discovery Server MUST start before other services.
- **Docker Memory**: On macOS/Windows, ensure Docker VM has sufficient memory. Default settings often cause slow startup.
- **Gateway Timeouts**: After starting with Docker Compose, initial timeouts are normal while API Gateway syncs with Eureka. Check Eureka dashboard at http://localhost:8761 for service registration status.
- **Configuration Repository**: Service configurations are externalized to https://github.com/spring-petclinic/spring-petclinic-microservices-config
- **Profiles**: Services support `docker` profile (for container networking) and `mysql` profile (for MySQL database).
