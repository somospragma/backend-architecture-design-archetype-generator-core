# Clean Architecture Generator

A Gradle plugin for generating clean architecture projects with hexagonal and onion architecture patterns.

## Features

- 🏗️ **Multiple Architecture Patterns**: Hexagonal (single/multi-module) and Onion architectures
- 🚀 **Framework Support**: Spring Boot
- ⚡ **Reactive & Imperative**: Support for both programming paradigms
- 🔌 **Adapter Generation**: Quick generation of adapters for MongoDB, PostgreSQL, REST, Redis, and more
- 📝 **Template System**: Flexible FreeMarker-based templates with local development mode
- ✅ **Validation**: Built-in validation for configuration and templates
- 🔄 **Incremental Generation**: Add adapters to existing projects without breaking changes

## Quick Start

### Installation

Add the plugin to your `build.gradle`:

```gradle
// build.gradle
plugins {
    id 'co.com.pragma.archetype-generator' version '1.1.1'
}
```

With the Kotlin DSL (`build.gradle.kts`):

```kotlin
plugins {
    id("co.com.pragma.archetype-generator") version "1.1.1"
}
```

The plugin reads its templates from the latest `v1.x.y` release of the [templates repository](https://github.com/somospragma/backend-architecture-design-archetype-generator-templates), so new templates reach you without a plugin update. To freeze a project on one templates release, set `templates.version` in `.cleanarch.yml` (see [Configuration](docs/configuration.md)).

### Initialize a Project

```bash
./gradlew initCleanArch
```

Follow the interactive prompts to select your architecture, framework, and paradigm.

### Generate an Adapter

```bash
./gradlew generateOutputAdapter
```

## Documentation

- [Getting Started](docs/getting-started.md) - Quick start guide
- [Commands Reference](docs/commands.md) - All available commands
- [Configuration Guide](docs/configuration.md) - Configuration options
- [Architecture Guide](docs/architectures.md) - Architecture patterns explained
- [Template System](docs/templates.md) - Template structure and creation
- [Testing Strategy](docs/testing.md) - Testing approach and coverage
- [Troubleshooting](docs/troubleshooting.md) - Common issues and solutions
- [Development Guide](docs/development.md) - Setup and development workflow
- [Contributing Guide](CONTRIBUTING.md) - How to contribute

## Supported Architectures

| Architecture | Description | Best For |
|-------------|-------------|----------|
| Hexagonal Single | Single-module hexagonal | Small services, MVPs |
| Hexagonal Multi | Multi-module hexagonal | Medium services |
| Hexagonal Multi Granular | Granular multi-module | Large microservices |
| Onion Single | Single-module onion | DDD-focused services |

## Supported Adapters

### Output Adapters (Driven)

**Reactive & Imperative:**
- MongoDB (Reactive: Spring Data MongoDB Reactive | Imperative: Spring Data MongoDB)
- PostgreSQL (Reactive: Spring Data R2DBC | Imperative: Spring Data JPA)
- Redis (Reactive: Spring Data Redis Reactive | Imperative: Spring Data Redis)
- HTTP Client (Reactive: WebClient | Imperative: RestTemplate)
- DynamoDB (Reactive: DynamoDbAsyncClient | Imperative: DynamoDbClient)
- SQS Producer (Reactive: SqsAsyncClient | Imperative: SqsClient)

### Input Adapters (Driving)

**Reactive & Imperative:**
- REST API (Reactive: Spring WebFlux | Imperative: Spring MVC)
- GraphQL (Reactive: Mono/Flux returns | Imperative: Synchronous returns)
- gRPC (Reactive: ReactorStub | Imperative: BlockingStub)
- SQS Consumer (Reactive: Async processing | Imperative: Sync processing)

## Adding Custom Adapters

The plugin uses a fully dynamic adapter discovery system. You can add new adapters without modifying any Java code.

### Quick Guide

To add a new adapter:

1. **Create adapter directory structure:**
   ```
   templates/frameworks/{framework}/{paradigm}/adapters/{direction}/{adapter-name}/
   ```
   - `{framework}`: spring
   - `{paradigm}`: imperative, reactive
   - `{direction}`: entry-points (input) or driven-adapters (output)
   - `{adapter-name}`: your adapter name (e.g., websocket, kafka, etc.)

2. **Create metadata.yml:**
   ```yaml
   name: my-adapter
   type: driving  # or driven
   description: My custom adapter description
   suffix: "Handler"  # Class name suffix
   requiresUseCase: true  # Does it need a use case?
   requiresEndpoints: false  # Does it need endpoints?
   needsMapper: false  # Does it need entity mapper?
   dependencies:
     - group: com.example
       artifact: my-library
       version: 1.0.0
   ```

3. **Create template files (*.ftl):**
   - Main adapter class template (e.g., `Controller.java.ftl`, `Adapter.java.ftl`)
   - Configuration templates if needed
   - Application properties template

4. **Add entry to index.json:**
   
   Update the index file at `templates/frameworks/{framework}/{paradigm}/adapters/{direction}/index.json`:
   
   ```json
   {
     "version": "1.0.0",
     "lastUpdated": "2025-02-27",
     "adapters": [
       {
         "name": "my-adapter",
         "displayName": "My Custom Adapter",
         "description": "Description of what this adapter does",
         "type": "entry-point",
         "status": "stable",
         "version": "1.0.0"
       }
     ]
   }
   ```

5. **Use immediately - NO code changes needed!**
   ```bash
   ./gradlew generateInputAdapter --name=MyHandler --type=my-adapter --useCase=MyUseCase
   ```

### Example: Adding a WebSocket Adapter

```bash
# 1. Create directory
mkdir -p templates/frameworks/spring/imperative/adapters/entry-points/websocket

# 2. Create metadata.yml
cat > templates/frameworks/spring/imperative/adapters/entry-points/websocket/metadata.yml << 'EOF'
name: websocket
type: driving
description: WebSocket handler using Spring WebSocket
suffix: "Handler"
requiresUseCase: true
requiresEndpoints: false
needsMapper: false
dependencies:
  - group: org.springframework.boot
    artifact: spring-boot-starter-websocket
    version: 3.2.0
EOF

# 3. Create template (Controller.java.ftl)
# ... create your FreeMarker template ...

# 4. Update index.json
# ... add entry to index.json ...

# 5. Use it!
./gradlew generateInputAdapter --name=ChatHandler --type=websocket --useCase=SendMessage
```

### Validation Flags

The metadata.yml validation flags control parameter requirements:

- **requiresUseCase**: If `true`, the `--useCase` parameter is mandatory
- **requiresEndpoints**: If `true`, the `--endpoints` parameter is mandatory  
- **needsMapper**: If `true`, generates entity mapper classes

### Benefits of Dynamic System

- ✅ Add adapters without recompiling the plugin
- ✅ Validation is data-driven from metadata
- ✅ Error messages show available adapters automatically
- ✅ Truly extensible architecture
- ✅ Team-specific adapters without forking

For more details on template creation, see [Template System Guide](docs/templates.md).

## Example

```bash
# Initialize a reactive Spring Boot project with hexagonal architecture
./gradlew initCleanArch

# Generate a MongoDB adapter
./gradlew generateOutputAdapter
# Select: MongoDB → UserPersistence → User

# Generate a REST controller
./gradlew generateInputAdapter
# Select: REST Controller → UserController → create,findById,update,delete
```

## Developer Mode

For template development:

```yaml
# .cleanarch.yml
templates:
  mode: developer
  localPath: ../backend-architecture-design-archetype-generator-templates
  cache: false
```

See [Contributing Guide](CONTRIBUTING.md) for details.

## Requirements

- Java 21 or higher
- Gradle 8.5 or higher

## Publishing

The plugin is published to the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/co.com.pragma.archetype-generator) as `co.com.pragma.archetype-generator`. Every version is listed in the [GitHub releases](https://github.com/somospragma/backend-architecture-design-archetype-generator-core/releases) with its jars and SBOM, and the changes in the [changelog](CHANGELOG.md).

### For Maintainers

A version is released by pushing a `vX.Y.Z` tag on `main`. The release workflow verifies, scans and builds it, waits for approval in the `release` environment and then publishes it.

- 📦 [Publicación y despliegue](docs/publicacion-y-despliegue.md) - Cómo se publica el plugin y qué templates lee
- 🔐 [CI y configuración de GitHub](docs/ci.md) - Pipelines, environments y rulesets

## License

[Apache License 2.0](LICENSE)

## Contributing

Contributions are welcome! See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## Support

- 📖 [Documentation](docs/)
- 🐛 [Issue Tracker](https://github.com/somospragma/backend-architecture-design-archetype-generator-core/issues)
- 💬 [Discussions](https://github.com/somospragma/backend-architecture-design-archetype-generator-core/discussions)

## 📄 Licencia

Este proyecto está licenciado bajo la **Apache License 2.0** - ver el archivo [LICENSE](LICENSE) para más detalles.

```
Copyright 2025 Pragma S.A. and Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```

### ¿Qué puedes hacer con este proyecto?

- ✅ Usar en proyectos personales y comerciales
- ✅ Modificar y crear obras derivadas
- ✅ Distribuir copias originales o modificadas
- ✅ Hacer fork y evolucionar el proyecto
- ✅ Usar en tu empresa sin restricciones

### ¿Qué debes hacer?

- 📋 Mantener los avisos de copyright y licencia
- 📋 Incluir el archivo [NOTICE](NOTICE) en distribuciones
- 📋 Documentar cambios significativos realizados
- 📋 Dar atribución al proyecto original

Ver [NOTICE](NOTICE) para información de atribución.
