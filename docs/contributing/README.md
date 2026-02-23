# Contributing Guide

Thank you for your interest in contributing to the Clean Architecture Generator!

## Getting Started

### Prerequisites

- Java 21 or higher
- Gradle 8.5 or higher
- Git

### Setting Up Development Environment

1. Clone the repository:
```bash
git clone https://github.com/somospragma/backend-architecture-design-archetype-generator-core.git
cd backend-architecture-design-archetype-generator-core
```

2. Build the project:
```bash
./gradlew build
```

3. Run tests:
```bash
./gradlew test
```

## Project Structure

```
backend-architecture-design-archetype-generator-core/
├── src/
│   ├── main/
│   │   ├── java/com/pragma/archetype/
│   │   │   ├── domain/          # Domain models and ports
│   │   │   ├── application/     # Use cases
│   │   │   └── infrastructure/  # Adapters and implementations
│   │   └── resources/
│   └── test/
│       └── java/com/pragma/archetype/
├── docs/                        # Documentation
└── build.gradle.kts            # Build configuration
```

## Development Workflow

### 1. Developer Mode

For template development, use developer mode to work with local templates:

```yaml
# .cleanarch.yml
templates:
  mode: developer
  localPath: ../backend-architecture-design-archetype-generator-templates
  cache: false
```

This enables:
- Hot reload of template changes
- No need to commit/push for testing
- Faster iteration

### 2. Making Changes

1. Create a feature branch:
```bash
git checkout -b feature/your-feature-name
```

2. Make your changes

3. Run tests:
```bash
./gradlew test
```

4. Commit your changes:
```bash
git commit -m "feat: add your feature description"
```

### 3. Testing

- Write unit tests for new functionality
- Ensure all tests pass before submitting PR
- Add integration tests for complex features

### 4. Code Style

- Follow Java naming conventions
- Use meaningful variable and method names
- Add Javadoc comments for public APIs
- Keep methods small and focused

## Adding New Features

### Adding a New Adapter Type

See [Adding Adapters Guide](adding-adapters.md)

### Adding a New Architecture

See [Adding Architectures Guide](adding-architectures.md)

### Adding a New Framework

1. Add framework enum value in `Framework.java`
2. Create framework-specific templates
3. Update template resolution logic
4. Add framework-specific dependencies
5. Update documentation

## Testing Templates

Use the `validateTemplates` task to check template syntax:

```bash
./gradlew validateTemplates
```

This validates:
- FreeMarker syntax
- Required variables
- Template file existence

## Documentation

- Update relevant documentation when adding features
- Add examples for new functionality
- Keep README.md up to date
- Document breaking changes

## Pull Request Process

1. Update documentation
2. Add tests for new functionality
3. Ensure all tests pass
4. Update CHANGELOG.md
5. Submit PR with clear description
6. Address review comments

## Code Review Guidelines

- Be respectful and constructive
- Focus on code quality and maintainability
- Suggest improvements, don't demand
- Approve when ready, request changes when needed

## Release Process

1. Update version in `build.gradle.kts`
2. Update CHANGELOG.md
3. Create release tag
4. Publish to Gradle Plugin Portal

## Getting Help

- Open an issue for bugs
- Start a discussion for questions
- Join our community chat

## License

By contributing, you agree that your contributions will be licensed under the project's license.
