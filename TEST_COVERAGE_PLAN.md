# Test Coverage Improvement Plan

## Current Status
- **Total Coverage**: 45%
- **Target**: 90%+
- **Tests**: 283 passing (3 disabled)

## Priority Packages (Ordered by Impact)

### Priority 1: Critical Infrastructure (0% coverage)
1. **infrastructure.adapter.in.gradle** (0% → 90%)
   - 8 Task classes (618 lines)
   - InitCleanArchTask
   - GenerateEntityTask
   - GenerateUseCaseTask
   - GenerateOutputAdapterTask
   - GenerateInputAdapterTask
   - ValidateTemplatesTask
   - ClearTemplateCacheTask
   - UpdateTemplatesTask

2. **infrastructure.config** (0% → 90%)
   - 1 class (34 lines)
   - ArchetypeGeneratorPlugin

### Priority 2: Low Coverage Core Components (< 25%)
3. **infrastructure.adapter.out.filesystem** (4% → 90%)
   - LocalFileSystemAdapter (49 lines)

4. **infrastructure.adapter.out.http** (22% → 90%)
   - HttpClientAdapter (29 lines)

5. **application.generator** (22% → 90%)
   - ProjectGenerator (654 lines)
   - AdapterGenerator
   - EntityGenerator
   - UseCaseGenerator
   - InputAdapterGenerator

6. **domain.port.in** (24% → 90%)
   - Port interfaces (16 lines)

### Priority 3: Medium Coverage (25-70%)
7. **domain.port.out** (48% → 90%)
8. **domain.service** (57% → 90%)
9. **application.usecase** (59% → 90%)
10. **domain.model** (60% → 90%)
11. **infrastructure.adapter.out.config** (67% → 90%)

### Priority 4: High Coverage (> 70%)
12. **infrastructure.adapter.out.template** (76% → 90%)

## Test Strategy by Package

### 1. Gradle Tasks (infrastructure.adapter.in.gradle)
**Approach**: Integration tests with GradleRunner

```java
@Test
void shouldInitializeCleanArchProject() {
    BuildResult result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments("initCleanArch", 
            "--architecture=hexagonal-single",
            "--paradigm=reactive",
            "--framework=spring",
            "--package=com.test")
        .withPluginClasspath()
        .build();
    
    assertEquals(TaskOutcome.SUCCESS, result.task(":initCleanArch").getOutcome());
    assertTrue(new File(testProjectDir, ".cleanarch.yml").exists());
}
```

**Tests needed**:
- Task execution success scenarios
- Task execution failure scenarios
- Parameter validation
- File generation verification
- Error handling

**Estimated tests**: 40-50 tests

### 2. Plugin Configuration (infrastructure.config)
**Approach**: Unit tests for plugin registration

```java
@Test
void shouldRegisterAllTasks() {
    Project project = ProjectBuilder.builder().build();
    project.getPluginManager().apply(ArchetypeGeneratorPlugin.class);
    
    assertNotNull(project.getTasks().findByName("initCleanArch"));
    assertNotNull(project.getTasks().findByName("generateEntity"));
    // ... verify all tasks
}
```

**Tests needed**:
- Plugin application
- Task registration
- Extension configuration

**Estimated tests**: 5-8 tests

### 3. File System Adapter (infrastructure.adapter.out.filesystem)
**Approach**: Unit tests with temp directories

```java
@Test
void shouldCreateDirectory(@TempDir Path tempDir) {
    LocalFileSystemAdapter adapter = new LocalFileSystemAdapter();
    Path newDir = tempDir.resolve("test-dir");
    
    adapter.createDirectory(newDir);
    
    assertTrue(Files.exists(newDir));
    assertTrue(Files.isDirectory(newDir));
}
```

**Tests needed**:
- Directory creation
- File reading/writing
- File existence checks
- Path resolution
- Error handling (permissions, IO errors)

**Estimated tests**: 15-20 tests

### 4. HTTP Client Adapter (infrastructure.adapter.out.http)
**Approach**: Unit tests with MockWebServer

```java
@Test
void shouldDownloadFileSuccessfully() {
    MockWebServer server = new MockWebServer();
    server.enqueue(new MockResponse().setBody("template content"));
    
    HttpClientAdapter adapter = new HttpClientAdapter();
    String content = adapter.downloadFile(server.url("/template.ftl").toString());
    
    assertEquals("template content", content);
}
```

**Tests needed**:
- Successful downloads
- Network errors (404, 500, timeout)
- Retry logic
- Connection pooling

**Estimated tests**: 10-15 tests

### 5. Generators (application.generator)
**Approach**: Integration tests with real file generation

```java
@Test
void shouldGenerateEntityWithAllFields(@TempDir Path tempDir) {
    EntityGenerator generator = new EntityGenerator(fileSystemPort, templatePort);
    EntityConfig config = EntityConfig.builder()
        .name("User")
        .fields(List.of(
            new Field("name", "String"),
            new Field("email", "String")
        ))
        .build();
    
    GenerationResult result = generator.generate(tempDir, config);
    
    assertTrue(result.success());
    assertTrue(Files.exists(tempDir.resolve("User.java")));
}
```

**Tests needed**:
- Entity generation (simple, complex, with/without ID)
- Use case generation (single/multiple methods)
- Adapter generation (all types)
- Input adapter generation
- Error scenarios
- Template variable substitution

**Estimated tests**: 50-60 tests

### 6. Domain Services (domain.service)
**Approach**: Unit tests with mocks

```java
@Test
void shouldValidateInvalidPackageName() {
    ConfigurationValidator validator = new ConfigurationValidator(fileSystemPort, configPort);
    
    ValidationResult result = validator.validatePackageName("Com.Example");
    
    assertFalse(result.valid());
    assertTrue(result.errors().stream()
        .anyMatch(e -> e.contains("lowercase")));
}
```

**Tests needed**:
- Validation logic (all validators)
- Business rules
- Edge cases
- Error messages

**Estimated tests**: 30-40 tests

### 7. Use Cases (application.usecase)
**Approach**: Unit tests with mocks

```java
@Test
void shouldGenerateAdapterSuccessfully() {
    GenerateAdapterUseCaseImpl useCase = new GenerateAdapterUseCaseImpl(
        adapterGenerator, validator, configPort, fileSystemPort);
    
    AdapterConfig config = createTestConfig();
    GenerationResult result = useCase.execute(projectPath, config);
    
    assertTrue(result.success());
    verify(adapterGenerator).generate(any(), any());
}
```

**Tests needed**:
- Happy path scenarios
- Validation failures
- Generation failures
- Rollback scenarios

**Estimated tests**: 25-30 tests

### 8. Domain Models (domain.model)
**Approach**: Unit tests for value objects and entities

```java
@Test
void shouldCreateValidProjectConfig() {
    ProjectConfig config = ProjectConfig.builder()
        .name("test-project")
        .basePackage("com.test")
        .architecture(ArchitectureType.HEXAGONAL_SINGLE)
        .build();
    
    assertEquals("test-project", config.name());
    assertEquals("com.test", config.basePackage());
}
```

**Tests needed**:
- Builder patterns
- Validation in constructors
- Immutability
- Equality/hashCode

**Estimated tests**: 20-25 tests

### 9. Config Adapter (infrastructure.adapter.out.config)
**Approach**: Integration tests with real YAML files

```java
@Test
void shouldReadConfigurationFromYaml(@TempDir Path tempDir) throws IOException {
    String yaml = """
        project:
          name: test
          basePackage: com.test
        architecture:
          type: hexagonal-single
        """;
    Files.writeString(tempDir.resolve(".cleanarch.yml"), yaml);
    
    YamlConfigurationAdapter adapter = new YamlConfigurationAdapter();
    Optional<ProjectConfig> config = adapter.readConfiguration(tempDir);
    
    assertTrue(config.isPresent());
    assertEquals("test", config.get().name());
}
```

**Tests needed**:
- YAML reading/writing
- YAML merging
- Configuration validation
- Error handling (malformed YAML)

**Estimated tests**: 15-20 tests

### 10. Template Adapter (infrastructure.adapter.out.template)
**Approach**: Integration tests with real templates

```java
@Test
void shouldLoadTemplateFromCache() {
    TemplateCache cache = new TemplateCache();
    cache.store("test-template", "template content");
    
    Optional<String> content = cache.retrieve("test-template");
    
    assertTrue(content.isPresent());
    assertEquals("template content", content.get());
}
```

**Tests needed**:
- Template loading (local/remote)
- Caching
- Template processing
- Metadata loading

**Estimated tests**: 10-15 tests

## Implementation Plan

### Phase 1: Critical Infrastructure (Week 1)
- [ ] Gradle Tasks tests (40-50 tests)
- [ ] Plugin Configuration tests (5-8 tests)
- **Target**: Bring 0% packages to 90%+

### Phase 2: Low Coverage Components (Week 2)
- [ ] File System Adapter tests (15-20 tests)
- [ ] HTTP Client Adapter tests (10-15 tests)
- [ ] Generator tests (50-60 tests)
- **Target**: Bring <25% packages to 90%+

### Phase 3: Medium Coverage Components (Week 3)
- [ ] Domain Services tests (30-40 tests)
- [ ] Use Cases tests (25-30 tests)
- [ ] Domain Models tests (20-25 tests)
- **Target**: Bring 25-70% packages to 90%+

### Phase 4: Polish and Edge Cases (Week 4)
- [ ] Config Adapter tests (15-20 tests)
- [ ] Template Adapter tests (10-15 tests)
- [ ] Edge cases and error scenarios
- **Target**: Achieve 90%+ overall coverage

## Total Estimated Tests
- **Current**: 283 tests
- **To Add**: ~250-350 tests
- **Final**: ~530-630 tests

## Success Criteria
- ✅ Overall coverage: 90%+
- ✅ All packages: 90%+ (except interfaces)
- ✅ All critical paths tested
- ✅ Error scenarios covered
- ✅ Integration tests for main workflows
- ✅ No flaky tests
- ✅ Fast test execution (< 2 minutes)

## Tools and Libraries
- JUnit 5
- Mockito
- AssertJ
- Testcontainers (for integration tests)
- GradleRunner (for task tests)
- MockWebServer (for HTTP tests)
- Kotest (for property-based tests)

## Next Steps
1. Start with Priority 1 (Gradle Tasks)
2. Create test base classes and utilities
3. Implement tests incrementally
4. Monitor coverage after each batch
5. Refactor code if needed for testability
