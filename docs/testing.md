# Testing Strategy

## Test Coverage

**Current**: 44% | **Target**: 60%+

## Test Summary

- **Total Tests**: 283 passing (3 disabled)
- **Unit Tests**: 180+
- **Integration Tests**: 50+
- **Property-Based Tests**: 21 (Kotest)
- **Cache Tests**: 27
- **Network Tests**: 30

## Test Types

### Unit Tests
- Domain models and validation
- Service logic
- Path resolution
- YAML parsing
- Configuration validation

### Integration Tests
- Full project generation
- Adapter generation
- Configuration validation
- Error scenarios
- Template processing

### Property-Based Tests (Kotest)
- Path resolution invariants
- YAML merge properties
- Configuration validation
- Domain model generators

### Cache Tests
- Template cache storage and retrieval
- Cache existence checks
- Cache clearing
- Cache size calculation
- Error handling (corrupted files)
- Security tests (path traversal, special characters)

### Network Tests
- Template downloading from GitHub, GitLab, Bitbucket
- Cache integration (hit/miss scenarios)
- Network error handling (404, 403, timeouts)
- Repository validation
- URL building for different Git providers

## Running Tests

```bash
# All tests
./gradlew test

# Specific test class
./gradlew test --tests "ConfigurationValidatorTest"

# Specific test method
./gradlew test --tests "*shouldValidateProjectConfig"

# With coverage
./gradlew test jacocoTestReport

# Integration tests only
./gradlew test --tests "*IntegrationTest"
```

## Test Organization

```
src/test/java/
├── domain/
│   ├── model/              # Model tests
│   └── service/            # Service tests
├── application/
│   ├── usecase/            # Use case tests
│   └── generator/          # Generator tests
├── infrastructure/
│   └── adapter/            # Adapter tests
└── integration/            # Integration tests
```

## Writing Tests

### Unit Test Example
```java
@Test
void shouldValidatePackageName() {
    String validPackage = "com.example.service";
    assertTrue(validator.isValidPackageName(validPackage));
}
```

### Integration Test Example
```java
@Test
void shouldGenerateCompleteProject(@TempDir Path tempDir) {
    ProjectConfig config = createTestConfig();
    GenerationResult result = useCase.execute(tempDir, config);
    
    assertTrue(result.success());
    assertFileExists(tempDir, "build.gradle.kts");
}
```

## Test Data

- Use `@TempDir` for file operations
- Create builders for complex objects
- Use realistic test data
- Clean up resources in `@AfterEach`

## Coverage Goals

| Package | Current | Target |
|---------|---------|--------|
| domain.model | 60% | 80% |
| domain.service | 56% | 70% |
| application | 59% | 65% |
| infrastructure.template | 76% | 80% |
| infrastructure.config | 65% | 70% |
| **Overall** | **44%** | **60%** |

## Disabled Tests

3 tests are currently disabled pending validator fixes:
- `shouldFailWhenProjectNameMissing` - YAML validation issue
- `shouldFailWhenBasePackageInvalid` - YAML validation issue  
- `shouldHandleBasePackageWithConsecutiveDots` - YAML validation issue

These tests require investigation of the ConfigurationValidator YAML parsing behavior.
