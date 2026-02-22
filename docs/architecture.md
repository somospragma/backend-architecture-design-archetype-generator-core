# Architecture

## Overview

Clean Architecture plugin for Gradle that generates project structures following hexagonal and onion patterns.

## Layers

### Domain Layer
- **Entities**: Core business objects
- **Ports**: Interfaces defining contracts (in/out)
- **Services**: Business logic and validation

### Application Layer
- **Use Cases**: Application-specific business rules
- **Generators**: Code generation orchestration

### Infrastructure Layer
- **Adapters**: External integrations (filesystem, HTTP, config)
- **Gradle Plugin**: Entry point for Gradle tasks

## Key Components

### Template System
- **TemplateRepository**: Manages template loading (local/remote)
- **FreemarkerTemplateRepository**: FreeMarker template processing
- **AdapterMetadataLoader**: Loads adapter metadata with framework-aware paths

### Generation Pipeline
1. Validate configuration (.cleanarch.yml)
2. Load templates (with caching)
3. Process templates with context
4. Generate files
5. Merge configurations (YAML, Gradle)

### Path Resolution
- Framework-aware: `frameworks/{framework}/{paradigm}/adapters/{type}/{name}`
- Legacy fallback: `adapters/{name}`
- Architecture-specific: `architectures/{type}/templates/`

## Design Patterns

- **Hexagonal Architecture**: Ports & Adapters
- **Repository Pattern**: Template and configuration access
- **Strategy Pattern**: Multiple template sources (local/remote)
- **Builder Pattern**: Configuration and model construction
