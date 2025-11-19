# Spring PetClinic Modulith

A modular monolith version of Spring PetClinic built with **Spring Modulith**.

## Architecture

This application demonstrates a **modular monolith** architecture using Spring Modulith, which provides:

- 🎯 **Module Boundaries** - Automatic verification of module boundaries
- 📦 **Loose Coupling** - Event-driven communication between modules
- 🔍 **Observability** - Built-in observability for module interactions
- 📚 **Documentation** - Automatic documentation generation

## Modules

The application is organized into the following modules:

```
org.springframework.samples.petclinic/
├── customers/   - Customer and Pet management
├── vets/        - Veterinarian management
├── visits/      - Visit records management
├── genai/       - AI chatbot functionality
└── shared/      - Shared utilities and configuration
```

## Technology Stack

- **Java 21**
- **Spring Boot 3.4.1**
- **Spring Modulith 1.3.0**
- **Spring Data JPA**
- **Spring AI (OpenAI)**
- **Micrometer + OpenTelemetry**

## Prerequisites

- Java 21 or higher
- Maven 3.x
- Docker or Podman (optional)

## Running the Application

### Local Development

```bash
cd spring-petclinic-modulith
../mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

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
