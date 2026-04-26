# test-hexagonal-single-imperative

Clean Architecture project using Hexagonal Architecture pattern.

## Architecture

- **Framework**: spring
- **Paradigm**: imperative
- **Architecture**: Hexagonal (Ports & Adapters)

## Project Structure

```
src/main/java/com/pragma/test/
├── domain/                    # Business logic (framework-independent)
│   ├── model/                # Domain entities
│   └── port/                 # Interfaces
│       ├── in/              # Input ports (use cases)
│       └── out/             # Output ports (repositories, services)
├── application/              # Use case implementations
│   └── usecase/             # Business logic orchestration
└── infrastructure/           # Framework-specific implementations
    ├── entry-points/        # Input adapters (REST, GraphQL, etc.)
    ├── driven-adapters/     # Output adapters (DB, Cache, APIs, etc.)
    └── config/              # Configuration classes
```

## Getting Started

### Prerequisites

- Java 21
- Gradle 8.5+
- Spring Boot 3.2+

### Build

```bash
./gradlew build
```

### Run

```bash
./gradlew bootRun
```

### Test

```bash
./gradlew test
```

## Available Gradle Tasks

### Generate Use Case
```bash
./gradlew generateUseCase --name=CreateOrder --packageName=com.pragma.test.domain.port.in --methods=execute:Order:request:CreateOrderRequest
```

### Generate Entity
```bash
./gradlew generateEntity --name=Order --packageName=com.pragma.test.domain.model --fields=customerId:String,amount:Double
```

### Generate Output Adapter (Driven Adapter)
```bash
./gradlew generateOutputAdapter --name=OrderCache --type=redis --packageName=com.pragma.test.infrastructure.driven-adapters.redis --entity=Order
```

### Generate Input Adapter (Entry Point)
```bash
./gradlew generateInputAdapter --name=OrderController --type=rest --packageName=com.pragma.test.infrastructure.entry-points.rest --useCase=CreateOrder
```

## Clean Architecture Principles

1. **Independence of Frameworks**: Business logic doesn't depend on frameworks
2. **Testability**: Business rules can be tested without UI, database, or external services
3. **Independence of UI**: UI can change without changing business rules
4. **Independence of Database**: Business rules don't know about the database
5. **Independence of External Services**: Business rules don't know about external services

## License

MIT
