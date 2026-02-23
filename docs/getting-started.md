# Getting Started

This guide will help you get started with the Clean Architecture Generator plugin.

## Installation

Add the plugin to your Gradle project:

```gradle
plugins {
    id 'com.pragma.archetype' version '1.0.0'
}
```

## Quick Start

### 1. Initialize a New Project

```bash
./gradlew initCleanArch
```

This will prompt you to select:
- Architecture type (Hexagonal Single, Hexagonal Multi, Hexagonal Multi Granular, Onion Single)
- Framework (Spring, Quarkus)
- Paradigm (Imperative, Reactive)

### 2. Generate an Adapter

```bash
./gradlew generateOutputAdapter
```

Or for input adapters:

```bash
./gradlew generateInputAdapter
```

### 3. Generate a Use Case

```bash
./gradlew generateUseCase
```

## Next Steps

- [Configuration Guide](configuration.md) - Learn how to configure the plugin
- [Commands Reference](commands.md) - Detailed command documentation
- [Architecture Guide](architectures.md) - Understanding different architecture types
- [Templates Guide](templates.md) - Working with templates

## Developer Mode

For template development, you can use local templates:

```yaml
# .cleanarch.yml
templates:
  mode: developer
  localPath: ../backend-architecture-design-archetype-generator-templates
```

See [Contributing Guide](../CONTRIBUTING.md) for more details.
