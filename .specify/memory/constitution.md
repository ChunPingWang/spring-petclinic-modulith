<!--
═════════════════════════════════════════════════════════════════════════════
SYNC IMPACT REPORT - Constitution Update
═════════════════════════════════════════════════════════════════════════════

Version Change: INITIAL → 1.0.0
Rationale: Initial constitution creation with comprehensive principles for code quality,
           testing standards, BDD, DDD, SOLID, hexagonal architecture, UX consistency,
           and performance requirements.

Modified Principles:
  - NEW: I. Code Quality & Clean Code
  - NEW: II. Testing Standards (Behavior Driven Development)
  - NEW: III. Domain-Driven Design (DDD)
  - NEW: IV. SOLID Principles
  - NEW: V. Hexagonal Architecture (Ports & Adapters)
  - NEW: VI. Framework Isolation (Infrastructure Layer Only)
  - NEW: VII. User Experience Consistency
  - NEW: VIII. Performance & Non-Functional Requirements

Added Sections:
  - Development Workflow & Quality Gates
  - Architecture Compliance

Removed Sections: None (initial version)

Templates Requiring Updates:
  ✅ plan-template.md - Constitution Check section aligns with new principles
  ✅ spec-template.md - Functional requirements support UX and performance criteria
  ✅ tasks-template.md - Task categorization supports BDD test-first and layered architecture

Follow-up TODOs: None - all placeholders filled

═════════════════════════════════════════════════════════════════════════════
-->

# Spring PetClinic Modulith - Project Constitution

## Core Principles

### I. Code Quality & Clean Code

The codebase MUST maintain high standards of code quality through:

- **Readability First**: Code is read far more often than written. Optimize for clarity over cleverness.
- **KISS (Keep It Simple, Stupid)**: Favor simple, straightforward solutions over complex abstractions.
- **YAGNI (You Aren't Gonna Need It)**: Only implement features when actually needed, not when anticipated.
- **DRY (Don't Repeat Yourself)**: Eliminate code duplication through appropriate abstraction, but avoid premature abstraction.
- **Meaningful Names**: Use intention-revealing names for classes, methods, and variables that accurately describe their purpose.
- **Small Functions**: Functions should do one thing well. Target 10-20 lines; flag functions exceeding 50 lines for review.
- **Constructor Injection**: ALWAYS use constructor injection for dependencies (never field injection with `@Autowired`).
- **No Magic Values**: Replace magic numbers and strings with named constants or enums.

**Rationale**: Clean code reduces cognitive load, accelerates onboarding, and minimizes defect introduction. The project demonstrates modular architecture—code clarity ensures module boundaries remain understandable.

---

### II. Testing Standards (Behavior Driven Development)

Testing MUST follow Behavior Driven Development principles with comprehensive test coverage:

- **Test-First Development**: Write tests BEFORE implementation (Red-Green-Refactor cycle).
- **Test Pyramid Strategy**:
  - **Business Layer Tests**: Pure Java unit tests (45+ tests) with zero framework dependencies, testing business logic in isolation
  - **Service Layer Tests**: Integration tests validating delegation to business services and model transformations
  - **API Layer Tests**: REST endpoint tests using MockMvc, validating HTTP contracts and status codes
  - **Module Integration Tests**: Event-driven integration tests validating cross-module communication
- **Given-When-Then Format**: Structure tests using BDD-style Given-When-Then for clarity.
- **Independent & Repeatable**: Tests MUST run in isolation and produce consistent results.
- **Test Coverage Goals**:
  - Business layer: 100% test coverage (all business logic)
  - Service layer: 100% delegation verification
  - API layer: All endpoints tested with success and error cases
  - Edge cases: Null checks, boundary conditions, invalid inputs
- **Naming Convention**: Test methods MUST use descriptive names (e.g., `shouldCreateCustomerWhenValidDataProvided`, `shouldThrowExceptionWhenCustomerNotFound`).

**Rationale**: The project has achieved 71 passing tests across three core modules (Vets: 19, Visits: 31, Customers: 21), demonstrating that comprehensive testing is achievable and maintainable. Tests serve as living documentation and regression prevention.

---

### III. Domain-Driven Design (DDD)

The application MUST organize code around the business domain using DDD principles:

- **Ubiquitous Language**: Use domain terminology consistently in code (e.g., `Customer`, `Visit`, `Vet`, not generic terms like `User`, `Record`, `Person`).
- **Bounded Contexts**: Each module (Customers, Vets, Visits, GenAI) represents a bounded context with clear boundaries.
- **Domain Models as First-Class Citizens**:
  - Domain models live in the `domain/` package
  - Models are pure POJOs with NO framework annotations
  - Business logic resides IN the domain models (e.g., `visit.complete()`, `customer.validate()`)
- **Entities vs Value Objects**:
  - **Entities**: Have identity (e.g., `Customer`, `Visit`, `Vet`)
  - **Value Objects**: Defined by attributes (e.g., `VisitStatus`, `Specialty`, `Address`)
- **Aggregates**: Group related entities and enforce consistency boundaries (e.g., `Owner` aggregate includes `Pets`).
- **Domain Events**: Use events to communicate state changes across bounded contexts (e.g., `CustomerCreated`, `VisitCompleted`, `VetUpdated`).
- **Repositories as Abstractions**: Repository interfaces live in the business layer as domain concepts, not infrastructure concerns.

**Rationale**: DDD aligns code structure with business understanding, making the codebase more maintainable and allowing domain experts to participate in code reviews. The Spring PetClinic domain (customers, vets, visits) maps naturally to DDD concepts.

---

### IV. SOLID Principles

All code MUST adhere to SOLID principles:

- **Single Responsibility Principle (SRP)**: Each class has ONE reason to change.
  - ✅ `VetBusinessService`: Business logic only
  - ✅ `VetRepositoryAdapter`: Data persistence only
  - ✅ `VetResource`: HTTP request handling only
- **Open/Closed Principle (OCP)**: Open for extension, closed for modification.
  - Use interfaces and abstract classes for extensibility
  - Favor composition over inheritance
- **Liskov Substitution Principle (LSP)**: Subtypes MUST be substitutable for base types.
  - Ensure implementations honor interface contracts
  - Avoid surprises in subclass behavior
- **Interface Segregation Principle (ISP)**: Clients should not depend on interfaces they don't use.
  - Create focused, role-specific interfaces (e.g., `VetRepository`, `EventPublisher`, `PetValidator`)
  - Avoid "fat" interfaces with unrelated methods
- **Dependency Inversion Principle (DIP)**: Depend on abstractions, not concretions.
  - **CRITICAL**: Infrastructure → Business ← Domain dependency direction
  - Business layer defines ports (interfaces)
  - Infrastructure layer implements adapters

**Rationale**: SOLID principles are the foundation of maintainable, testable, and extensible software. The hexagonal architecture refactoring (Phase 16) demonstrates these principles in action, achieving 100% framework decoupling in the business layer.

---

### V. Hexagonal Architecture (Ports & Adapters)

The application MUST follow hexagonal architecture (also known as Ports and Adapters pattern):

**Layer Structure**:

```
Infrastructure Layer (Framework-Dependent)
    ↓ depends on ↓
Business Layer (Pure Java - Zero Framework Dependencies)
    ↓ depends on ↓
Domain Layer (Pure POJOs)
```

**Layer Responsibilities**:

- **Domain Layer** (`domain/` package):
  - Pure Java domain models (POJOs)
  - Business logic methods (e.g., `validate()`, `schedule()`, `complete()`)
  - Value objects and enums
  - NO annotations, NO framework dependencies

- **Business Layer** (`business/` package):
  - Pure Java business services implementing use cases
  - Defines **Ports** (interfaces): `Repository`, `EventPublisher`, `Validator`
  - Business exceptions (pure Java `RuntimeException` subclasses)
  - NO Spring annotations, NO JPA, NO framework code

- **Infrastructure Layer** (`infrastructure/` package):
  - Implements **Adapters** for all ports
  - JPA entities with framework annotations
  - Spring Data JPA repositories
  - Repository adapters translating between domain models and JPA entities
  - Event publisher adapters (Spring `ApplicationEventPublisher`)
  - Domain mappers (Domain ↔ Entity ↔ Legacy conversions)
  - Configuration classes wiring everything together

**Port Examples**:
- `VetRepository` (port): Business-level interface for vet persistence
- `VetRepositoryAdapter` (adapter): Infrastructure implementation using JPA
- `EventPublisher` (port): Business-level interface for publishing domain events
- `SpringEventPublisherAdapter` (adapter): Infrastructure implementation using Spring events

**Rationale**: Hexagonal architecture achieves true framework independence in the business layer, making the core business logic portable, testable, and long-lived. The project has successfully refactored three core modules (Vets, Visits, Customers) to this architecture with 45 pure Java business tests.

---

### VI. Framework Isolation (Infrastructure Layer Only)

Frameworks (Spring, JPA, etc.) MUST ONLY exist in the Infrastructure layer:

- **Domain Layer**: NO framework dependencies whatsoever
  - No `@Entity`, `@Table`, `@Column` annotations
  - No Spring annotations
  - Pure Java classes

- **Business Layer**: NO framework dependencies whatsoever
  - No `@Service`, `@Component`, `@Autowired` annotations
  - No JPA interfaces
  - Pure Java business logic with port interfaces

- **Infrastructure Layer**: Framework code ONLY here
  - JPA entities with `@Entity`, `@Table`, `@Column`
  - Spring Data JPA repositories extending `JpaRepository`
  - Spring configuration with `@Configuration`, `@Bean`
  - Adapters implementing business ports using framework facilities

**Migration Path for Legacy Code**:
- Legacy service implementations in `internal/` package may use frameworks temporarily
- These delegate to pure business services
- Gradual refactoring moves logic from framework-coupled to framework-free

**Verification**:
- Business layer tests MUST NOT use `@SpringBootTest` or Spring test annotations
- Business layer classes MUST NOT import Spring or JPA packages
- Automated checks SHOULD verify zero framework dependencies in business/domain layers

**Rationale**: Framework isolation ensures the core business logic survives framework changes, framework version upgrades, and technology migrations. Pure Java code is easier to test, understand, and maintain. The project demonstrates this is achievable—45 business tests run without any Spring context.

---

### VII. User Experience Consistency

The application MUST provide consistent, high-quality user experiences:

- **REST API Consistency**:
  - Use standard HTTP verbs (GET, POST, PUT, DELETE) consistently
  - Return appropriate status codes (200 OK, 201 Created, 204 No Content, 404 Not Found, 400 Bad Request, 500 Internal Server Error)
  - Use consistent response formats (JSON)
  - Provide meaningful error messages with error codes

- **API Documentation**:
  - ALL endpoints MUST be documented in OpenAPI/Swagger
  - Include request/response examples
  - Document error responses
  - Keep API documentation up-to-date with code changes

- **Naming Consistency**:
  - REST controllers: `*Resource` suffix (e.g., `OwnerResource`, `VetResource`)
  - Services: `*Service` interface, `*ServiceImpl` implementation
  - Repositories: `*Repository` suffix
  - Events: Past tense (e.g., `CustomerCreated`, `VisitCompleted`)

- **Error Handling**:
  - Use domain-specific exceptions (e.g., `CustomerNotFoundException`, `InvalidVisitException`)
  - Translate business exceptions to appropriate HTTP status codes
  - Provide user-friendly error messages
  - Log errors with sufficient context for debugging

- **Validation**:
  - Validate inputs at API boundary using Bean Validation (`@Valid`, `@NotNull`, `@Min`)
  - Perform business validation in domain/business layers
  - Return 400 Bad Request with validation errors

**Rationale**: Consistent UX reduces friction for API consumers, improves developer experience, and reduces support burden. The Swagger UI integration (http://localhost:8080/swagger-ui.html) provides excellent API discoverability.

---

### VIII. Performance & Non-Functional Requirements

The application MUST meet performance and operational requirements:

- **Response Time Goals**:
  - API endpoints: < 200ms p95 latency for read operations
  - API endpoints: < 500ms p95 latency for write operations
  - Database queries: Optimize N+1 query problems

- **Scalability**:
  - Support at least 1000 concurrent users
  - Horizontal scaling MUST NOT require application changes

- **Resource Utilization**:
  - Startup time: < 30 seconds
  - Memory usage: < 512MB for typical workloads
  - Connection pooling for database connections

- **Observability**:
  - ALL critical operations MUST be instrumented with Micrometer `@Timed` metrics
  - Use structured logging with appropriate log levels (TRACE, DEBUG, INFO, WARN, ERROR)
  - Enable distributed tracing via OpenTelemetry/Zipkin
  - Expose Actuator endpoints: `/health`, `/metrics`, `/prometheus`, `/info`, `/modulith`

- **Resilience**:
  - Event publication MUST be persistent (stored in `event_publication` table)
  - Failed event listeners MUST retry automatically
  - Database transactions MUST be properly scoped
  - Handle failures gracefully without data corruption

- **Security**:
  - NO sensitive data in logs (passwords, tokens, PII)
  - NO SQL injection vulnerabilities (use parameterized queries)
  - NO XSS vulnerabilities (validate and sanitize inputs)
  - Validate all inputs at system boundaries

**Rationale**: Non-functional requirements are as critical as functional requirements. The project already demonstrates excellent observability (Prometheus metrics, Zipkin tracing, comprehensive Actuator endpoints). Performance targets ensure the application remains responsive under load.

---

## Development Workflow & Quality Gates

### Pre-Commit Requirements

Before committing code, developers MUST:

1. Run all tests: `../mvnw test` (all 71+ tests MUST pass)
2. Ensure no compilation warnings
3. Follow formatting conventions (constructor injection, meaningful names)
4. Verify Spring Modulith boundaries: `../mvnw test -Dtest=ModulithStructureTest`

### Code Review Requirements

Pull requests MUST:

1. Include tests for all new functionality (maintain or increase test coverage)
2. Pass all CI/CD checks (build, tests, module structure validation)
3. Follow established architectural patterns (hexagonal architecture for business logic)
4. Update documentation if API contracts change
5. Include benchmark results if performance-critical code is changed

### Definition of Done (DoD)

A feature is complete when:

1. ✅ All acceptance scenarios from spec.md pass
2. ✅ Test coverage meets requirements (100% business layer, full API coverage)
3. ✅ Code follows SOLID principles and hexagonal architecture
4. ✅ Documentation updated (README, OpenAPI/Swagger, inline comments for complex logic)
5. ✅ Performance requirements met (load tested if applicable)
6. ✅ Security reviewed (input validation, no vulnerabilities)
7. ✅ Observability added (metrics, logging, tracing)
8. ✅ Peer reviewed and approved

---

## Architecture Compliance

### Module Boundaries (Spring Modulith)

The application enforces strict module boundaries:

- ✅ **ALLOWED**: Accessing other modules' public APIs (classes in module root, not in `internal/`)
- ✅ **ALLOWED**: Synchronous calls to public service interfaces
- ✅ **ALLOWED**: Publishing domain events via `ApplicationEventPublisher`
- ✅ **ALLOWED**: Listening to events via `@ApplicationModuleListener`
- ❌ **FORBIDDEN**: Accessing another module's `internal/` packages
- ❌ **FORBIDDEN**: Circular dependencies between modules
- ❌ **FORBIDDEN**: Direct database table access across modules

**Verification**: `ModulithStructureTest` automatically verifies these rules at test time.

### Hexagonal Architecture Compliance

Each core module (Customers, Vets, Visits) MUST follow hexagonal architecture:

- ✅ Domain models in `domain/` package (pure POJOs)
- ✅ Business services in `business/service/` package (pure Java)
- ✅ Port interfaces in `business/port/` package (pure Java)
- ✅ Business exceptions in `business/exception/` package (pure Java)
- ✅ JPA entities in `infrastructure/persistence/entity/` package (framework code)
- ✅ JPA repositories in `infrastructure/persistence/jpa/` package (Spring Data)
- ✅ Adapters in `infrastructure/persistence/adapter/` package (port implementations)
- ✅ Mappers in `infrastructure/persistence/mapper/` package (domain ↔ entity transformations)
- ✅ Event adapters in `infrastructure/event/` package
- ✅ Configuration in `infrastructure/config/` package

**Verification**: Code review checklist, dependency analysis, business layer tests MUST NOT import Spring/JPA.

---

## Governance

### Amendment Process

This constitution may be amended through:

1. Proposal submitted with rationale and impact analysis
2. Team discussion and consensus building
3. Documentation of decision in Architecture Decision Records (ADRs)
4. Update to this constitution file with version increment
5. Communication to all team members

### Versioning Policy

Constitution versions follow semantic versioning (MAJOR.MINOR.PATCH):

- **MAJOR**: Backward incompatible governance/principle removals or redefinitions
- **MINOR**: New principle/section added or materially expanded guidance
- **PATCH**: Clarifications, wording, typo fixes, non-semantic refinements

### Compliance Review

- All pull requests MUST verify compliance with constitution principles
- Architecture reviews conducted quarterly to assess adherence
- Violations MUST be documented and justified with rationale
- Persistent violations trigger constitution amendment discussion

### Complexity Justification

Any deviation from constitution principles MUST be justified:

- Document the specific requirement necessitating deviation
- Explain why simpler alternatives were rejected
- Record decision in Architecture Decision Records (ADRs)
- Plan remediation if deviation is temporary

---

**Version**: 1.0.0 | **Ratified**: 2025-11-23 | **Last Amended**: 2025-11-23
