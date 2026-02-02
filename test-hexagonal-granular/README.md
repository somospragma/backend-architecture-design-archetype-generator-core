# test-hexagonal-granular

Granular Multi-module Hexagonal Architecture project.

## Project Structure

```
test-hexagonal-granular/
├── domain/              # Domain modules (organized folder)
│   ├── model/          # Module: Domain entities
│   ├── ports/          # Module: Interfaces (ports)
│   └── usecase/        # Module: Use case implementations
│
└── applications/        # Applications (organized folder)
    └── app-service/    # Module: Main application
```

## Architecture

This project follows **Hexagonal Architecture** with **granular modules**:

- Each component (model, ports, usecase) is a separate Gradle module
- Maximum modularity and reusability
- Independent testing per module
- Clear dependency management

### Dependency Flow
```
app-service → usecase → ports → model
```

## Build & Run

```bash
# Build all modules
./gradlew build

# Run the application
./gradlew :applications:app-service:bootRun

# Run tests
./gradlew test
```

## Technology Stack

- Java 21
- Spring Boot 3.3.0
- Spring WebFlux (Reactive)
- Project Reactor
- Gradle (Multi-module granular)

---

Generated with [Clean Architecture Generator](https://github.com/somospragma/backend-architecture-design)
