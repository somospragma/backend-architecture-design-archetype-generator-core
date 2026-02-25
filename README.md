# Clean Architecture Generator

A Gradle plugin for generating clean architecture projects with hexagonal and onion architecture patterns.

## Features

- 🏗️ **Multiple Architecture Patterns**: Hexagonal (single/multi-module) and Onion architectures
- 🚀 **Framework Support**: Spring Boot and Quarkus
- ⚡ **Reactive & Imperative**: Support for both programming paradigms
- 🔌 **Adapter Generation**: Quick generation of adapters for MongoDB, PostgreSQL, REST, Redis, and more
- 📝 **Template System**: Flexible FreeMarker-based templates with local development mode
- ✅ **Validation**: Built-in validation for configuration and templates
- 🔄 **Incremental Generation**: Add adapters to existing projects without breaking changes

## Quick Start

### Installation

Add the plugin to your `build.gradle`:

```gradle
plugins {
    id 'com.pragma.archetype' version '1.0.0'
}
```

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

This plugin is published to:
- **Gradle Plugin Portal**: https://plugins.gradle.org/plugin/com.pragma.archetype-generator
- **Maven Central**: https://search.maven.org/artifact/com.pragma/archetype-generator

### For Maintainers

- 📦 [Publishing Guide](PUBLISHING.md) - Complete guide for publishing to Gradle Plugin Portal and Maven Central
- 🚀 [Quick Start Publishing](PUBLISHING_QUICKSTART.md) - Quick reference for publishing
- 🔐 [GitHub Secrets Setup](GITHUB_SECRETS_SETUP.md) - Configure automated publishing with GitHub Actions

### Automated Publishing

The plugin is automatically published when you create a tag:

```bash
git tag v1.0.0-PRERELEASE
git push origin v1.0.0-PRERELEASE
```

The GitHub Actions workflow will:
1. Build and test the project
2. Publish to Gradle Plugin Portal
3. Publish to Maven Central (if configured)
4. Create a GitHub Release

## License

[Add your license here]

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
