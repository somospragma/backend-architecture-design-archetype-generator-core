# Architecture Guide

This guide explains the different clean architecture patterns supported by the plugin.

## Overview

The plugin supports four architecture variants:

| Architecture | Modules | Granularity | Best For |
|-------------|---------|-------------|----------|
| Hexagonal Single | 1 | Coarse | Small services, MVPs |
| Hexagonal Multi | 3 | Medium | Medium services |
| Hexagonal Multi Granular | 5+ | Fine | Large services, microservices |
| Onion Single | 1 | Coarse | Small services with DDD focus |

## Hexagonal Single Module

A single-module project with hexagonal architecture principles.

### Structure

```
src/main/java/{basePackage}/
├── domain/
│   ├── model/          # Domain entities
│   └── port/
│       ├── in/         # Input ports (use cases)
│       └── out/        # Output ports (repositories, clients)
├── application/
│   └── service/        # Use case implementations
└── infrastructure/
    └── adapter/
        ├── in/         # Driving adapters (controllers, listeners)
        └── out/        # Driven adapters (repositories, clients)
```

### When to Use

- Small to medium services
- MVPs and prototypes
- Services with simple domain logic
- Teams new to clean architecture

### Advantages

- Simple structure
- Easy to understand
- Fast build times
- Low overhead

### Disadvantages

- All code in one module
- Harder to enforce boundaries
- Can become messy as it grows

## Hexagonal Multi Module

A multi-module project with separate modules for domain, application, and infrastructure.

### Structure

```
project/
├── domain/
│   └── src/main/java/{basePackage}/domain/
│       ├── model/
│       └── port/
├── application/
│   └── src/main/java/{basePackage}/application/
│       └── service/
└── infrastructure/
    └── src/main/java/{basePackage}/infrastructure/
        └── adapter/
```

### When to Use

- Medium to large services
- Services with complex domain logic
- Teams familiar with clean architecture
- Projects requiring strict layer separation

### Advantages

- Clear module boundaries
- Enforced dependencies
- Better separation of concerns
- Easier to test in isolation

### Disadvantages

- More complex setup
- Slower build times
- More boilerplate

## Hexagonal Multi Module Granular

A highly granular multi-module project with separate modules for each adapter.

### Structure

```
project/
├── domain/
├── application/
├── infrastructure/
│   ├── driven-adapters/
│   │   ├── mongodb-adapter/
│   │   ├── rest-client-adapter/
│   │   └── redis-adapter/
│   └── entry-points/
│       ├── rest-controller/
│       └── event-listener/
└── boot/
```

### When to Use

- Large microservices
- Services with many external dependencies
- Projects requiring independent adapter deployment
- Teams with strong DevOps practices

### Advantages

- Maximum modularity
- Independent adapter development
- Easier to replace adapters
- Better for large teams

### Disadvantages

- Complex project structure
- Longer build times
- More configuration overhead
- Steeper learning curve

## Onion Single Module

A single-module project following onion architecture principles with DDD focus.

### Structure

```
src/main/java/{basePackage}/
├── core/
│   ├── domain/         # Domain entities, value objects, aggregates
│   └── application/
│       ├── service/    # Application services
│       └── port/
│           ├── in/     # Input ports
│           └── out/    # Output ports
└── infrastructure/
    └── adapter/
        ├── in/         # Driving adapters
        └── out/        # Driven adapters
```

### When to Use

- Domain-driven design projects
- Services with rich domain models
- Projects emphasizing business logic
- Teams familiar with DDD

### Advantages

- Strong DDD focus
- Clear domain boundaries
- Business logic at the center
- Testable domain layer

### Disadvantages

- Requires DDD knowledge
- Can be overkill for simple CRUD
- More upfront design needed

## Choosing an Architecture

### Decision Tree

1. **Is your service simple CRUD?**
   - Yes → Hexagonal Single
   - No → Continue

2. **Do you need strict module boundaries?**
   - No → Hexagonal Single or Onion Single
   - Yes → Continue

3. **Do you have many external dependencies?**
   - No → Hexagonal Multi
   - Yes → Hexagonal Multi Granular

4. **Is your domain complex with rich business logic?**
   - Yes → Onion Single or Hexagonal Multi
   - No → Hexagonal Single

### Migration Path

You can migrate between architectures:

1. **Single → Multi**: Extract modules from single module
2. **Multi → Granular**: Split infrastructure into adapter modules
3. **Hexagonal → Onion**: Reorganize to emphasize domain

## Layer Dependencies

All architectures follow these dependency rules:

```
Infrastructure → Application → Domain
```

- **Domain**: No dependencies on other layers
- **Application**: Depends only on Domain
- **Infrastructure**: Depends on Application and Domain

## See Also

- [Getting Started Guide](getting-started.md)
- [Configuration Guide](configuration.md)
- [Commands Reference](commands.md)
