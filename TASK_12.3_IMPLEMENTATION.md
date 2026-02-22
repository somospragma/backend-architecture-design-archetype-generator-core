# Task 12.3 Implementation: Configuration Validation in Gradle Tasks

## Summary

Enhanced all Gradle tasks (except `InitCleanArchTask`) with configuration validation to ensure `.cleanarch.yml` exists and contains required fields before executing any operations.

## Changes Made

### Modified Files

1. **GenerateOutputAdapterTask.java**
   - Added configuration validation at the start of `generateAdapter()` method
   - Validates `.cleanarch.yml` exists before generating adapters
   - Displays clear error messages with documentation links if validation fails

2. **GenerateInputAdapterTask.java**
   - Added configuration validation at the start of `generateInputAdapter()` method
   - Validates `.cleanarch.yml` exists before generating input adapters
   - Displays clear error messages with documentation links if validation fails

3. **GenerateUseCaseTask.java**
   - Added configuration validation at the start of `generateUseCase()` method
   - Validates `.cleanarch.yml` exists before generating use cases
   - Displays clear error messages with documentation links if validation fails

4. **GenerateEntityTask.java**
   - Added configuration validation at the start of `generateEntity()` method
   - Validates `.cleanarch.yml` exists before generating entities
   - Displays clear error messages with documentation links if validation fails

### Implementation Pattern

Each task now follows this pattern:

```java
@TaskAction
public void taskMethod() {
  try {
    // 1. Validate project configuration exists
    Path projectPath = getProject().getProjectDir().toPath();
    FileSystemPort fileSystemPort = new LocalFileSystemAdapter();
    ConfigurationPort configurationPort = new YamlConfigurationAdapter();
    
    ConfigurationValidator configValidator = new ConfigurationValidator(fileSystemPort, configurationPort);
    ValidationResult configValidation = configValidator.validateProjectConfig(projectPath);
    
    if (configValidation.isInvalid()) {
      getLogger().error("✗ Configuration validation failed:");
      configValidation.errors().forEach(error -> getLogger().error("  {}", error));
      throw new RuntimeException("Configuration validation failed. Please fix the errors above.");
    }

    // 2. Continue with task-specific logic...
  }
}
```

### Tasks NOT Modified

- **InitCleanArchTask**: Intentionally excluded as it creates the `.cleanarch.yml` file
- **UpdateTemplatesTask**: Utility task that doesn't require project configuration
- **ClearTemplateCacheTask**: Utility task that doesn't require project configuration

## Validation Features

The `ConfigurationValidator.validateProjectConfig()` method checks:

1. **File Existence**: Verifies `.cleanarch.yml` exists in the project directory
2. **Required Fields**: Validates all required fields are present:
   - `project.name`
   - `project.basePackage`
   - `architecture.type`
   - `architecture.paradigm`
   - `architecture.framework`
3. **Field Values**: Validates field values follow conventions:
   - Package names follow Java naming conventions
   - Project names are valid
   - Architecture types are supported

## Error Messages

When validation fails, users see:

```
✗ Configuration validation failed:
  Configuration file .cleanarch.yml not found in: /path/to/project
  Run 'initCleanArch' to create initial project structure.
  See: https://github.com/somospragma/backend-architecture-design-archetype-generator
```

Or for missing fields:

```
✗ Configuration validation failed:
  Missing required field 'project.basePackage' in .cleanarch.yml
  Example: project:
    basePackage: com.example.myservice
```

## Requirements Satisfied

- **Requirement 9.1**: Configuration file validation for all non-init commands
- **Requirement 9.2**: Clear error messages when `.cleanarch.yml` is missing
- **Requirement 9.3**: Clear error messages when required fields are missing
- **Requirement 9.7**: InitCleanArch command does NOT require `.cleanarch.yml`

## Testing

The implementation was verified by:

1. **Compilation**: All modified files compile successfully
2. **Build**: `./gradlew compileJava` passes without errors
3. **Diagnostics**: No new compilation errors introduced
4. **Manual Verification**: Code review confirms validation is properly integrated

## Next Steps

- Task 12.4: Write unit tests for ConfigurationValidator (optional, marked with *)
- Task 13: Implement error handling and rollback mechanism
