# Development Guide

## Setup

```bash
# Clone repository
git clone <repo-url>
cd backend-architecture-design-archetype-generator-core

# Build
./gradlew build

# Run tests
./gradlew test

# Generate coverage report
./gradlew jacocoTestReport
# Report: build/reports/jacoco/test/html/index.html
```

## Project Structure

```
src/
├── main/java/com/pragma/archetype/
│   ├── domain/              # Business logic
│   │   ├── model/          # Domain entities
│   │   ├── port/           # Interfaces (in/out)
│   │   └── service/        # Domain services
│   ├── application/         # Use cases
│   │   ├── usecase/        # Use case implementations
│   │   └── generator/      # Code generators
│   └── infrastructure/      # External adapters
│       ├── adapter/
│       │   ├── in/         # Input adapters (Gradle)
│       │   └── out/        # Output adapters (FS, HTTP, Config)
│       └── config/         # DI configuration
└── test/                    # Tests
```

## Adding New Features

### 1. Add New Adapter Type

1. Update `AdapterConfig.AdapterType` enum
2. Create templates in `templates/frameworks/{framework}/{paradigm}/adapters/`
3. Add metadata.yml with dependencies
4. Update `AdapterGenerator.getAdapterTemplate()`
5. Add tests

### 2. Add New Architecture

1. Create structure in `templates/architectures/{name}/`
2. Add `structure.yml` with path mappings
3. Create project templates
4. Update `ArchitectureType` enum
5. Add integration tests

### 3. Add New Framework

1. Create `templates/frameworks/{name}/`
2. Add paradigm variants (reactive/imperative)
3. Create adapter templates
4. Update `Framework` enum
5. Test with all architectures

## Testing Strategy

- **Unit Tests**: Domain logic, validators
- **Integration Tests**: Full generation workflows
- **Property Tests**: Invariants (Kotest)
- **Manual Tests**: Real project generation

## Code Style

- Follow Clean Architecture principles
- Keep domain layer framework-agnostic
- Use records for immutable data
- Prefer composition over inheritance
- Write descriptive test names
