# Configuration Guide

The Clean Architecture Generator is configured through the `.cleanarch.yml` file in your project root.

## Configuration File Structure

```yaml
# Project configuration
name: my-service
basePackage: com.example.myservice
architecture: hexagonal-single
paradigm: reactive
framework: spring
pluginVersion: 1.0.0
createdAt: 2024-01-15T10:30:00
adaptersAsModules: false

# Template configuration
templates:
  mode: production  # or developer
  repository: https://github.com/somospragma/backend-architecture-design-archetype-generator-templates
  branch: main
  localPath: null  # Set path for developer mode
  cache: true

# Dependency overrides (optional)
dependencyOverrides:
  spring-boot: 3.2.0
  lombok: 1.18.30
```

## Configuration Fields

### Project Configuration

- **name**: Project name (required)
- **basePackage**: Base Java package for the project (required)
- **architecture**: Architecture type (required)
  - `hexagonal-single`: Single-module hexagonal architecture
  - `hexagonal-multi`: Multi-module hexagonal architecture
  - `hexagonal-multi-granular`: Granular multi-module hexagonal architecture
  - `onion-single`: Single-module onion architecture
- **paradigm**: Programming paradigm (required)
  - `imperative`: Traditional imperative programming
  - `reactive`: Reactive programming with Project Reactor
- **framework**: Framework to use (required)
  - `spring`: Spring Boot
  - `quarkus`: Quarkus
- **pluginVersion**: Version of the plugin used
- **createdAt**: Project creation timestamp
- **adaptersAsModules**: Whether adapters should be separate modules (for multi-module architectures)

### Template Configuration

- **mode**: Template loading mode
  - `production`: Download templates from remote repository (default)
  - `developer`: Use local templates for development
- **repository**: GitHub repository URL for templates
- **branch**: Branch or tag to use (default: `main`)
- **localPath**: Path to local templates (for developer mode)
- **cache**: Whether to cache downloaded templates (default: `true`)

### Dependency Overrides

You can override specific dependency versions:

```yaml
dependencyOverrides:
  spring-boot: 3.2.0
  lombok: 1.18.30
  mapstruct: 1.5.5.Final
```

## Developer Mode

For template development, use developer mode:

```yaml
templates:
  mode: developer
  localPath: ../backend-architecture-design-archetype-generator-templates
  cache: false  # Disable caching for hot reload
```

This allows you to:
- Modify templates locally
- See changes immediately without re-downloading
- Test template changes before committing

## Validation

The plugin validates your configuration on every command (except `initCleanArch`). If validation fails, you'll see clear error messages with suggestions for fixing the issues.

Common validation errors:
- Missing required fields
- Invalid package names
- Invalid architecture type
- Malformed YAML syntax

## See Also

- [Commands Reference](commands.md)
- [Architecture Guide](architectures.md)
- [Contributing Guide](../CONTRIBUTING.md)
