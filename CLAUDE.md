# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Spring PetClinic Modulith is a modular monolith version of the Spring PetClinic application built with Spring Modulith. This project demonstrates how to build a well-architected modular application with clear module boundaries, event-driven architecture, and comprehensive observability - all within a single deployable unit.

**Note**: This codebase contains both the legacy microservices architecture (in separate service directories) and the new Spring Modulith implementation (in `spring-petclinic-modulith/`). The primary focus is now on the Spring Modulith version.

## Technology Stack

- **Java 17** (required)
- **Spring Boot 3.4.1**
- **Spring Modulith 1.3.0** - Modular monolith architecture
- **Spring AI 1.0.0-M5** - AI chatbot integration
- **Spring Data JPA** - Data persistence
- **Maven** for build management
- **Docker/Podman** for containerization
- **HSQLDB** (default) or **MySQL 8.0+** for persistence

## Build and Development Commands

### Building the Modulith Application

```bash
# Build the modulith application
cd spring-petclinic-modulith
../mvnw clean install

# Build without tests
../mvnw clean install -DskipTests

# Run tests only
../mvnw test

# Run specific test
../mvnw test -Dtest=ModulithStructureTest
```

### Running the Application Locally

**With HSQLDB (default, no setup required):**
```bash
cd spring-petclinic-modulith
../mvnw spring-boot:run
```

**With MySQL:**
```bash
# First, start MySQL
docker run -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=petclinic -p 3306:3306 mysql:8.0

# Then run the application
cd spring-petclinic-modulith
../mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=mysql"
```

**Access the application:**
- Main application: http://localhost:8080
- Health check: http://localhost:8080/actuator/health
- Metrics: http://localhost:8080/actuator/prometheus
- Module structure: http://localhost:8080/actuator/modulith

### Docker Operations

```bash
# Build Docker image
cd spring-petclinic-modulith
../mvnw clean install -P buildDocker

# Build for Podman
../mvnw clean install -P buildDocker -Dcontainer.executable=podman

# Build for specific platform (e.g., Apple Silicon M2)
../mvnw clean install -P buildDocker -Dcontainer.platform="linux/arm64"

# Run the container
docker run -p 8080:8080 springcommunity/spring-petclinic-modulith:3.4.1

# Start with Docker Compose (includes observability stack)
docker-compose up
```

## Spring Modulith Architecture

### Module Structure

The application is organized into the following modules under `org.springframework.samples.petclinic`:

```
org.springframework.samples.petclinic/
├── customers/              # Customer and pet management
│   ├── Customer.java       # Public API
│   ├── CustomerService.java
│   ├── CustomerCreated.java  # Domain event
│   └── internal/           # Internal implementation (hidden)
│       ├── CustomerServiceImpl.java
│       ├── CustomerRepository.java
│       ├── Pet.java
│       └── web/
│           ├── OwnerResource.java
│           └── PetResource.java
├── vets/                   # Veterinarian management
│   ├── Vet.java
│   ├── VetService.java
│   └── internal/
│       ├── VetServiceImpl.java
│       ├── VetRepository.java
│       └── web/
│           └── VetResource.java
├── visits/                 # Visit records management
│   ├── Visit.java
│   ├── VisitService.java
│   └── internal/
│       ├── VisitServiceImpl.java
│       ├── VisitRepository.java
│       └── web/
│           └── VisitResource.java
├── genai/                  # AI chatbot functionality
│   ├── ChatService.java
│   └── internal/
│       ├── ChatServiceImpl.java
│       ├── AIDataProvider.java
│       └── web/
│           └── ChatResource.java
└── shared/                 # Shared infrastructure
    ├── config/
    ├── web/
    └── dto/
```

### Module Communication Rules

**Allowed:**
1. **Synchronous calls via public interfaces**: Modules can call other modules' public APIs (classes not in `internal/` packages)
2. **Asynchronous event-driven communication**: Modules publish events using `ApplicationEventPublisher` and listen using `@ApplicationModuleListener`

**Forbidden:**
1. ❌ Accessing another module's `internal/` packages
2. ❌ Circular dependencies between modules
3. ❌ Direct database table access across modules

**Example - Synchronous call:**
```java
// visits module calling customers module
@Service
class VisitServiceImpl {
    private final CustomerService customerService; // ✅ Public interface

    void createVisit(Visit visit) {
        Customer customer = customerService.findById(visit.getCustomerId());
        // ...
    }
}
```

**Example - Asynchronous event:**
```java
// customers module publishes event
events.publishEvent(new CustomerCreated(customer.getId()));

// genai module listens to event
@ApplicationModuleListener
void on(CustomerCreated event) {
    updateVectorStore(event.getCustomerId());
}
```

### Module Verification

Spring Modulith automatically verifies module structure during testing:
```bash
../mvnw test -Dtest=ModulithStructureTest
```

This test ensures:
- No circular dependencies between modules
- Modules only access public APIs of other modules
- Internal packages are not exposed

## Database Configuration

### Default: HSQLDB In-Memory

By default, the application uses HSQLDB (in-memory) with data populated at startup. No configuration needed.

### MySQL Configuration

To use MySQL:

1. Start MySQL:
   ```bash
   docker run -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=petclinic -p 3306:3306 mysql:8.0
   ```

2. Run with MySQL profile:
   ```bash
   ../mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=mysql"
   ```

3. Database schema and data are automatically initialized from:
   - `src/main/resources/schema-mysql.sql`
   - `src/main/resources/data.sql`

## GenAI Service Configuration

The GenAI module provides AI chatbot functionality using Spring AI.

### OpenAI (default):
```bash
export OPENAI_API_KEY="your_api_key_here"
```

### Azure OpenAI:
```bash
export AZURE_OPENAI_ENDPOINT="https://your_resource.openai.azure.com"
export AZURE_OPENAI_KEY="your_api_key_here"
```

Configuration in `application.yml`:
```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      model: gpt-4o-mini
```

## Monitoring and Observability

### Actuator Endpoints

```bash
# Health check
curl http://localhost:8080/actuator/health

# Application info
curl http://localhost:8080/actuator/info

# Module structure
curl http://localhost:8080/actuator/modulith

# Prometheus metrics
curl http://localhost:8080/actuator/prometheus

# Environment variables
curl http://localhost:8080/actuator/env

# Change log level
curl -X POST http://localhost:8080/actuator/loggers/org.springframework.samples.petclinic \
  -H 'Content-Type: application/json' \
  -d '{"configuredLevel": "DEBUG"}'
```

### Custom Metrics

Modules use Micrometer `@Timed` annotations:
- `customers` module: `petclinic.owner`, `petclinic.pet`
- `visits` module: `petclinic.visit`
- `vets` module: `petclinic.vet`
- `genai` module: `petclinic.chat`

### Distributed Tracing

The application uses OpenTelemetry and Zipkin for distributed tracing:
- Zipkin UI: http://localhost:9411 (when running with docker-compose)

### Observability Stack

When running with `docker-compose up`, you get:
- **Prometheus** (9091): Metrics collection
- **Grafana** (3000): Dashboards
- **Zipkin** (9411): Trace visualization

## Code Structure and Conventions

### Package Structure

Each module follows this pattern:
```
<module-name>/
├── PublicEntity.java           # Entities in module root (public API)
├── PublicService.java          # Service interfaces (public API)
├── DomainEvent.java           # Events (public API)
└── internal/                  # All implementation details
    ├── ServiceImpl.java
    ├── Repository.java
    ├── InternalEntity.java
    └── web/
        └── Resource.java      # REST controllers
```

### Naming Conventions

- **Entities**: Nouns (e.g., `Owner`, `Pet`, `Visit`)
- **Controllers**: `*Resource` suffix (e.g., `OwnerResource`, `PetResource`)
- **Repositories**: `*Repository` suffix (e.g., `OwnerRepository`)
- **Services**: `*Service` interface, `*ServiceImpl` implementation
- **Events**: Past tense (e.g., `CustomerCreated`, `VisitCompleted`)
- **Tests**: `*Test` suffix (e.g., `OwnerResourceTest`)

### REST Controllers

```java
@RestController
@RequestMapping("/owners")
@Timed("petclinic.owner")  // Micrometer metrics
class OwnerResource {
    private final CustomerService customerService;

    // Use constructor injection
    OwnerResource(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/{id}")
    public Optional<Customer> findOwner(@PathVariable @Min(1) Integer id) {
        return customerService.findById(id);
    }
}
```

### Dependency Injection

**Always use constructor injection** (not `@Autowired` field injection):
```java
@Service
class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository repository;
    private final ApplicationEventPublisher events;

    CustomerServiceImpl(CustomerRepository repository,
                       ApplicationEventPublisher events) {
        this.repository = repository;
        this.events = events;
    }
}
```

## Testing

### Test Structure

```
src/test/java/
├── org/springframework/samples/petclinic/
│   ├── ModulithStructureTest.java     # Verifies module boundaries
│   ├── customers/
│   │   └── internal/
│   │       ├── CustomerServiceImplTest.java
│   │       └── web/
│   │           └── OwnerResourceTest.java
│   ├── vets/
│   └── visits/
```

### Running Tests

```bash
# Run all tests
../mvnw test

# Run specific test class
../mvnw test -Dtest=CustomerServiceImplTest

# Run specific test method
../mvnw test -Dtest=CustomerServiceImplTest#shouldCreateCustomer

# Verify module structure
../mvnw test -Dtest=ModulithStructureTest

# Skip tests during build
../mvnw clean install -DskipTests
```

### Test Examples

**Unit Test:**
```java
@ExtendWith(SpringExtension.class)
class CustomerServiceImplTest {
    private CustomerService service;
    private CustomerRepository repository;

    @BeforeEach
    void setUp() {
        repository = mock(CustomerRepository.class);
        service = new CustomerServiceImpl(repository, mock(ApplicationEventPublisher.class));
    }

    @Test
    void shouldCreateCustomer() {
        // Given
        Customer customer = new Customer();
        given(repository.save(customer)).willReturn(customer);

        // When
        Customer result = service.createCustomer(customer);

        // Then
        assertThat(result).isNotNull();
        verify(repository).save(customer);
    }
}
```

**REST Controller Test:**
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
    void shouldGetOwner() throws Exception {
        // Given
        Customer customer = new Customer(1, "John", "Doe");
        given(customerService.findById(1)).willReturn(Optional.of(customer));

        // When & Then
        mvc.perform(get("/owners/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("John"));
    }
}
```

## Important Development Notes

- **Java Version**: Project requires Java 17. Maven enforcer will fail build if using wrong version.
- **Module Boundaries**: Spring Modulith enforces module boundaries at compile time. If you violate module rules, `ModulithStructureTest` will fail.
- **Event Publication**: Events are stored in the database (`event_publication` table) to ensure reliable delivery and enable retries.
- **Single Deployment**: Unlike the original microservices, this is a single application - no need to start multiple services.
- **Docker Memory**: On macOS/Windows, ensure Docker VM has sufficient memory (at least 2GB).
- **Database Profiles**: Use `hsqldb` profile (default) for development, `mysql` for production-like environments.

## API Endpoints

All endpoints are served from a single application on port 8080:

### Customer Management
- `GET /owners` - Get all owners
- `GET /owners/{id}` - Get owner by ID
- `POST /owners` - Create owner
- `PUT /owners/{id}` - Update owner

### Pet Management
- `GET /owners/{ownerId}/pets` - Get pets for owner
- `POST /owners/{ownerId}/pets` - Add pet
- `PUT /owners/{ownerId}/pets/{petId}` - Update pet

### Veterinarian Management
- `GET /vets` - Get all vets
- `GET /vets/{id}` - Get vet by ID

### Visit Management
- `GET /visits?petId={id}` - Get visits for pet
- `POST /visits` - Create visit

### AI Chat
- `POST /genai/chat` - Chat with AI

## Comparing to Microservices Architecture

This repository contains both architectures for reference:

| Aspect | Microservices (legacy) | Modulith (current) |
|--------|----------------------|-------------------|
| Deployment units | 8 separate services | 1 application |
| Ports | 8080, 8081, 8082, 8083, 8084, 8761, 8888, 9090 | 8080 only |
| Service discovery | Eureka (8761) | Not needed |
| Config server | Spring Cloud Config (8888) | application.yml |
| API Gateway | Spring Cloud Gateway (8080) | Not needed |
| Module boundaries | Network boundaries | Spring Modulith packages |
| Communication | REST/HTTP | Direct calls + Events |
| Startup time | ~2-3 minutes (all services) | ~30 seconds |
| Memory usage | ~2GB+ | ~512MB |

## Legacy Microservices (Reference Only)

The original microservices are in separate directories but are not the focus of development:
- `spring-petclinic-customers-service/`
- `spring-petclinic-vets-service/`
- `spring-petclinic-visits-service/`
- `spring-petclinic-genai-service/`
- `spring-petclinic-api-gateway/`
- `spring-petclinic-config-server/`
- `spring-petclinic-discovery-server/`
- `spring-petclinic-admin-server/`

To run the legacy microservices architecture, see the microservices-specific documentation in each service directory.

## Additional Documentation

For more detailed information, see:
- `spring-petclinic-modulith/README.md` - Detailed modulith documentation
- `spring-petclinic-modulith/ARCHITECTURE_DECISIONS.md` - Architecture decision records
- `spring-petclinic-modulith/DEVELOPER_GUIDE.md` - Developer guide with examples
- `spec.md` - Original refactoring specification
- `.github/copilot-instructions.md` - Development standards and coding conventions
