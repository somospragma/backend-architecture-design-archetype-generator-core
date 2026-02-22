# Testing Strategy

## Test Coverage

**Current**: 41% | **Target**: 60%+

## Test Types

### Unit Tests
- Domain models and validation
- Service logic
- Path resolution
- YAML parsing

### Integration Tests
- Full project generation
- Adapter generation
- Configuration validation
- Error scenarios
- Template processing

### Property-Based Tests (Planned)
- Path resolution invariants
- YAML merge properties
- Template substitution
- Configuration round-trip

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
| domain.model | 59% | 80% |
| domain.service | 56% | 70% |
| application | 46% | 60% |
| infrastructure | 41% | 50% |

## Known Issues

- 8 tests failing (3 ErrorScenario, 5 GenerateAdapter)
- YAML parsing in tests needs stripIndent()
- Some mocks need updating for new signatures
