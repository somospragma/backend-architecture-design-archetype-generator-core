# Contributing to Clean Architecture Generator

Thank you for your interest in contributing! This document provides guidelines for contributing to the project.

## Quick Links

- [Full Contributing Guide](docs/contributing/README.md) - Detailed contribution guidelines
- [Adding Adapters](docs/contributing/adding-adapters.md) - How to add new adapter types
- [Adding Architectures](docs/contributing/adding-architectures.md) - How to add new architecture patterns

## Getting Started

1. Fork the repository
2. Clone your fork
3. Create a feature branch
4. Make your changes
5. Run tests: `./gradlew test`
6. Submit a pull request

## Development Setup

### Prerequisites

- Java 21+
- Gradle 8.5+
- Git

### Build and Test

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Validate templates
./gradlew validateTemplates
```

## Developer Mode

For template development, use local templates:

```yaml
# .cleanarch.yml
templates:
  mode: developer
  localPath: ../backend-architecture-design-archetype-generator-templates
  cache: false
```

This enables hot reload - changes to templates are immediately available without rebuilding.

## Code Style

- Follow Java naming conventions
- Add Javadoc for public APIs
- Keep methods focused and small
- Write meaningful commit messages

## Testing

- Write unit tests for new features
- Ensure all tests pass before submitting PR
- Add integration tests for complex features

## Pull Request Process

1. Update documentation
2. Add tests
3. Ensure all tests pass
4. Update CHANGELOG.md
5. Submit PR with clear description

## Adding New Features

### Adding an Adapter

1. Create adapter templates in the templates repository
2. Add metadata.yml with dependencies and configuration
3. Update adapter type enum
4. Add tests
5. Update documentation

### Adding an Architecture

1. Create structure.yml defining the architecture
2. Create folder structure templates
3. Create README template
4. Add architecture type enum
5. Add tests
6. Update documentation

## Questions?

- Open an issue for bugs
- Start a discussion for questions
- Check existing documentation

## License

By contributing, you agree that your contributions will be licensed under the project's license.

---

For detailed guidelines, see the [Full Contributing Guide](docs/contributing/README.md).
