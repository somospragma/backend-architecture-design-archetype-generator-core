# Troubleshooting Guide

This guide helps you resolve common issues when using the Clean Architecture Generator plugin.

## Common Issues

### Configuration Issues

#### Error: "Configuration file .cleanarch.yml not found"

**Cause**: The `.cleanarch.yml` file doesn't exist in your project root.

**Solution**:
```bash
# Initialize a new project first
./gradlew initCleanArch
```

#### Error: "Failed to parse .cleanarch.yml"

**Cause**: YAML syntax error in your configuration file.

**Solution**:
1. Check YAML indentation (use 2 spaces, not tabs)
2. Ensure colons have spaces after them: `key: value`
3. Quote special characters
4. Validate YAML syntax online: https://www.yamllint.com/

**Example of valid YAML**:
```yaml
project:
  name: my-service
  basePackage: com.example.service

architecture:
  type: hexagonal-single
  paradigm: reactive
  framework: spring
```

#### Error: "Invalid package name"

**Cause**: Package name doesn't follow Java naming conventions.

**Solution**:
- Use only lowercase letters, numbers, and underscores
- Start each segment with a letter
- Use at least 2 segments: `com.example`
- ❌ Bad: `Com.Example`, `com..example`, `example`
- ✅ Good: `com.example.service`, `com.mycompany.app`

### Template Issues

#### Error: "Template not found"

**Cause**: Template doesn't exist for the selected framework/paradigm combination.

**Solution**:
1. Check if the adapter type is supported for your framework
2. Verify template repository is accessible
3. Try clearing the cache:
```bash
rm -rf .cleanarch/cache
./gradlew generateOutputAdapter
```

#### Error: "Failed to download templates"

**Cause**: Network issue or invalid repository URL.

**Solution**:
1. Check internet connection
2. Verify repository URL in `.cleanarch.yml`
3. Try using developer mode with local templates:
```yaml
templates:
  mode: developer
  localPath: ../backend-architecture-design-archetype-generator-templates
```

### Generation Issues

#### Error: "File already exists"

**Cause**: Trying to generate a file that already exists.

**Solution**:
1. Rename or delete the existing file
2. Use a different name for the new adapter
3. Check if the adapter was already generated

#### Error: "Failed to merge build.gradle"

**Cause**: Gradle file has syntax errors or unexpected format.

**Solution**:
1. Backup your `build.gradle` or `build.gradle.kts`
2. Fix any syntax errors
3. Ensure the file follows standard Gradle format
4. Try regenerating from a clean state

### Dependency Issues

#### Error: "Could not resolve dependency"

**Cause**: Maven repository is unreachable or dependency doesn't exist.

**Solution**:
1. Check internet connection
2. Verify Maven Central is accessible
3. Check if the dependency version exists
4. Try updating dependency versions in `.cleanarch.yml`:
```yaml
dependencyOverrides:
  spring-boot: 3.2.0
```

### Build Issues

#### Error: "Task 'initCleanArch' not found"

**Cause**: Plugin not properly applied to the project.

**Solution**:
1. Ensure plugin is in `build.gradle`:
```gradle
plugins {
    id 'com.pragma.archetype' version '1.0.0'
}
```
2. Run `./gradlew tasks` to verify plugin is loaded
3. Try `./gradlew clean build`

#### Error: "Java version mismatch"

**Cause**: Project requires Java 21 or higher.

**Solution**:
1. Check Java version: `java -version`
2. Install Java 21 if needed
3. Set JAVA_HOME environment variable
4. Update `build.gradle`:
```gradle
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
```

## Debug Mode

Enable detailed logging to diagnose issues:

```bash
# Show detailed information
./gradlew generateOutputAdapter --info

# Show debug information
./gradlew generateOutputAdapter --debug

# Show stack traces on errors
./gradlew generateOutputAdapter --stacktrace
```

## Cache Issues

If you're experiencing template-related issues, try clearing the cache:

```bash
# Remove template cache
rm -rf .cleanarch/cache

# Regenerate with fresh templates
./gradlew generateOutputAdapter
```

## Validation

Validate your configuration and templates:

```bash
# Validate templates
./gradlew validateTemplates

# This will check:
# - Template syntax
# - Missing variables
# - Metadata format
# - Dependency declarations
```

## Getting Help

If you're still experiencing issues:

1. **Check Documentation**: Review the [docs](.) for detailed guides
2. **Search Issues**: Look for similar issues in the [issue tracker](https://github.com/somospragma/backend-architecture-design-archetype-generator-core/issues)
3. **Create Issue**: If you found a bug, create a new issue with:
   - Error message
   - Steps to reproduce
   - Your configuration (`.cleanarch.yml`)
   - Gradle version (`./gradlew --version`)
   - Java version (`java -version`)
   - Plugin version

## Common Workarounds

### Workaround: Manual File Generation

If automatic generation fails, you can manually create files:

1. Copy template from `templates/` directory
2. Replace variables manually:
   - `${basePackage}` → your base package
   - `${projectName}` → your project name
   - `${adapterName}` → your adapter name
3. Add dependencies to `build.gradle` manually
4. Update `application.yml` with configuration

### Workaround: Developer Mode

For more control over templates:

```yaml
# .cleanarch.yml
templates:
  mode: developer
  localPath: ../backend-architecture-design-archetype-generator-templates
  cache: false
```

This allows you to:
- Modify templates directly
- See changes immediately
- Debug template issues
- Test custom templates

## See Also

- [Getting Started Guide](getting-started.md)
- [Configuration Guide](configuration.md)
- [Commands Reference](commands.md)
- [Contributing Guide](../CONTRIBUTING.md)
