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
- MongoDB
- PostgreSQL
- MySQL
- Redis
- REST Client
- Kafka Producer

### Input Adapters (Driving)
- REST Controller
- GraphQL
- gRPC
- Kafka Consumer
- Event Listener

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

## License

[Add your license here]

## Contributing

Contributions are welcome! See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## Support

- 📖 [Documentation](docs/)
- 🐛 [Issue Tracker](https://github.com/somospragma/backend-architecture-design-archetype-generator-core/issues)
- 💬 [Discussions](https://github.com/somospragma/backend-architecture-design-archetype-generator-core/discussions)
