package com.pragma.archetype.application.usecase;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.pragma.archetype.application.generator.AdapterGenerator;
import com.pragma.archetype.domain.model.adapter.AdapterConfig;
import com.pragma.archetype.domain.model.adapter.AdapterType;
import com.pragma.archetype.domain.model.adapter.AdapterMetadata;
import com.pragma.archetype.domain.model.validation.ValidationResult;
import com.pragma.archetype.domain.port.in.GenerateAdapterUseCase.GenerationResult;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import com.pragma.archetype.domain.port.out.TemplateRepository;
import com.pragma.archetype.domain.service.AdapterValidator;
import com.pragma.archetype.infrastructure.adapter.out.config.YamlConfigurationAdapter;

/**
 * Tests for dependency merging without replacement.
 * Validates Requirements 18.2: "THE Plugin SHALL merge new dependencies into
 * the existing build file
 * without removing existing dependencies"
 * 
 * **Validates: Requirements 18.2**
 */
@DisplayName("Dependency Merging Tests")
class DependencyMergingTest {

  @TempDir
  Path tempDir;

  private AdapterValidator validator;
  private AdapterGenerator generator;
  private ConfigurationPort configurationPort;
  private FileSystemPort fileSystemPort;
  private TemplateRepository templateRepository;
  private YamlConfigurationAdapter yamlConfigurationAdapter;
  private com.pragma.archetype.domain.service.BackupService backupService;
  private GenerateAdapterUseCaseImpl useCase;

  @BeforeEach
  void setUp() {
    validator = mock(AdapterValidator.class);
    generator = mock(AdapterGenerator.class);
    configurationPort = mock(ConfigurationPort.class);
    fileSystemPort = mock(FileSystemPort.class);
    templateRepository = mock(TemplateRepository.class);
    yamlConfigurationAdapter = mock(YamlConfigurationAdapter.class);
    backupService = mock(com.pragma.archetype.domain.service.BackupService.class);

    useCase = new GenerateAdapterUseCaseImpl(
        validator,
        generator,
        configurationPort,
        fileSystemPort,
        templateRepository,
        yamlConfigurationAdapter,
        backupService);

    // Set up default mocks
    when(templateRepository.templateExists(anyString())).thenReturn(true);
    when(templateRepository.validateTemplate(anyString())).thenReturn(ValidationResult.success());
  }

  @Test
  @DisplayName("Should add test dependency without removing existing dependencies")
  void shouldAddTestDependencyWithoutRemovingExisting() {
    // Given: Build file with existing dependencies
    String buildFileContent = """
        plugins {
            id("java")
        }

        dependencies {
            implementation("org.springframework.boot:spring-boot-starter-webflux")
            implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
            testImplementation("org.springframework.boot:spring-boot-starter-test")
        }
        """;

    AdapterConfig config = createAdapterConfig("mongodb");
    AdapterMetadata metadata = createAdapterMetadataWithTestDependencies();

    when(validator.validate(any(), any())).thenReturn(ValidationResult.success());
    when(configurationPort.readConfiguration(any())).thenReturn(Optional.empty());
    when(generator.generate(any(), any(), any())).thenReturn(List.of());
    when(templateRepository.loadAdapterMetadata(anyString())).thenReturn(metadata);
    when(fileSystemPort.exists(any())).thenReturn(true);
    when(fileSystemPort.readFile(any())).thenReturn(buildFileContent);

    // When: Generate adapter with test dependencies
    GenerationResult result = useCase.execute(tempDir, config);

    // Then: Should succeed and preserve existing dependencies
    assertTrue(result.success());

    // Verify that writeFile was called with content containing all dependencies
    verify(fileSystemPort).writeFile(org.mockito.ArgumentMatchers.argThat(file -> {
      String content = file.content();
      // Verify existing dependencies are preserved
      boolean hasExistingWebflux = content
          .contains("implementation(\"org.springframework.boot:spring-boot-starter-webflux\")");
      boolean hasExistingMongo = content
          .contains("implementation(\"org.springframework.boot:spring-boot-starter-data-mongodb-reactive\")");
      boolean hasExistingTest = content
          .contains("testImplementation(\"org.springframework.boot:spring-boot-starter-test\")");
      // Verify new dependency is added
      boolean hasNewDependency = content
          .contains("testImplementation(\"de.flapdoodle.embed:de.flapdoodle.embed.mongo:4.11.0\")");

      return hasExistingWebflux && hasExistingMongo && hasExistingTest && hasNewDependency;
    }));
  }

  @Test
  @DisplayName("Should preserve dependency order when adding new test dependency")
  void shouldPreserveDependencyOrderWhenAddingNew() {
    // Given: Build file with specific dependency order
    String buildFileContent = """
        dependencies {
            implementation("org.springframework.boot:spring-boot-starter-webflux")
            implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
            testImplementation("org.springframework.boot:spring-boot-starter-test")
        }
        """;

    AdapterConfig config = createAdapterConfig("mongodb");
    AdapterMetadata metadata = createAdapterMetadataWithTestDependencies();

    when(validator.validate(any(), any())).thenReturn(ValidationResult.success());
    when(configurationPort.readConfiguration(any())).thenReturn(Optional.empty());
    when(generator.generate(any(), any(), any())).thenReturn(List.of());
    when(templateRepository.loadAdapterMetadata(anyString())).thenReturn(metadata);
    when(fileSystemPort.exists(any())).thenReturn(true);
    when(fileSystemPort.readFile(any())).thenReturn(buildFileContent);

    // When: Generate adapter
    GenerationResult result = useCase.execute(tempDir, config);

    // Then: Should preserve order of existing dependencies
    assertTrue(result.success());

    verify(fileSystemPort).writeFile(org.mockito.ArgumentMatchers.argThat(file -> {
      String content = file.content();
      int webfluxPos = content.indexOf("spring-boot-starter-webflux");
      int mongoPos = content.indexOf("spring-boot-starter-data-mongodb-reactive");
      int testPos = content.indexOf("spring-boot-starter-test");

      // Verify original order is preserved
      return webfluxPos < mongoPos && mongoPos < testPos;
    }));
  }

  @Test
  @DisplayName("Should not duplicate test dependency if already present")
  void shouldNotDuplicateTestDependencyIfAlreadyPresent() {
    // Given: Build file already containing the test dependency
    String buildFileContent = """
        dependencies {
            implementation("org.springframework.boot:spring-boot-starter-webflux")
            testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo:4.11.0")
        }
        """;

    AdapterConfig config = createAdapterConfig("mongodb");
    AdapterMetadata metadata = createAdapterMetadataWithTestDependencies();

    when(validator.validate(any(), any())).thenReturn(ValidationResult.success());
    when(configurationPort.readConfiguration(any())).thenReturn(Optional.empty());
    when(generator.generate(any(), any(), any())).thenReturn(List.of());
    when(templateRepository.loadAdapterMetadata(anyString())).thenReturn(metadata);
    when(fileSystemPort.exists(any())).thenReturn(true);
    when(fileSystemPort.readFile(any())).thenReturn(buildFileContent);

    // When: Generate adapter
    GenerationResult result = useCase.execute(tempDir, config);

    // Then: Should not modify build file (dependency already exists)
    assertTrue(result.success());
    verify(fileSystemPort, never()).writeFile(any());
  }

  @Test
  @DisplayName("Should add test dependency after last testImplementation")
  void shouldAddTestDependencyAfterLastTestImplementation() {
    // Given: Build file with multiple test dependencies
    String buildFileContent = """
        dependencies {
            implementation("org.springframework.boot:spring-boot-starter-webflux")
            testImplementation("org.springframework.boot:spring-boot-starter-test")
            testImplementation("org.mockito:mockito-core:5.0.0")
        }
        """;

    AdapterConfig config = createAdapterConfig("mongodb");
    AdapterMetadata metadata = createAdapterMetadataWithTestDependencies();

    when(validator.validate(any(), any())).thenReturn(ValidationResult.success());
    when(configurationPort.readConfiguration(any())).thenReturn(Optional.empty());
    when(generator.generate(any(), any(), any())).thenReturn(List.of());
    when(templateRepository.loadAdapterMetadata(anyString())).thenReturn(metadata);
    when(fileSystemPort.exists(any())).thenReturn(true);
    when(fileSystemPort.readFile(any())).thenReturn(buildFileContent);

    // When: Generate adapter
    GenerationResult result = useCase.execute(tempDir, config);

    // Then: New test dependency should be added after the last testImplementation
    assertTrue(result.success());

    verify(fileSystemPort).writeFile(org.mockito.ArgumentMatchers.argThat(file -> {
      String content = file.content();
      int mockitoPos = content.indexOf("mockito-core");
      int embedMongoPos = content.indexOf("de.flapdoodle.embed.mongo");

      // New dependency should come after existing test dependencies
      return embedMongoPos > mockitoPos;
    }));
  }

  @Test
  @DisplayName("Should add test dependency section if no test dependencies exist")
  void shouldAddTestDependencySectionIfNoneExist() {
    // Given: Build file with only implementation dependencies
    String buildFileContent = """
        dependencies {
            implementation("org.springframework.boot:spring-boot-starter-webflux")
            implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
        }
        """;

    AdapterConfig config = createAdapterConfig("mongodb");
    AdapterMetadata metadata = createAdapterMetadataWithTestDependencies();

    when(validator.validate(any(), any())).thenReturn(ValidationResult.success());
    when(configurationPort.readConfiguration(any())).thenReturn(Optional.empty());
    when(generator.generate(any(), any(), any())).thenReturn(List.of());
    when(templateRepository.loadAdapterMetadata(anyString())).thenReturn(metadata);
    when(fileSystemPort.exists(any())).thenReturn(true);
    when(fileSystemPort.readFile(any())).thenReturn(buildFileContent);

    // When: Generate adapter
    GenerationResult result = useCase.execute(tempDir, config);

    // Then: Should add test dependencies section with comment
    assertTrue(result.success());

    verify(fileSystemPort).writeFile(org.mockito.ArgumentMatchers.argThat(file -> {
      String content = file.content();
      boolean hasComment = content.contains("// Test dependencies");
      boolean hasNewDependency = content
          .contains("testImplementation(\"de.flapdoodle.embed:de.flapdoodle.embed.mongo:4.11.0\")");
      boolean preservesImplementation = content
          .contains("implementation(\"org.springframework.boot:spring-boot-starter-webflux\")");

      return hasComment && hasNewDependency && preservesImplementation;
    }));
  }

  @Test
  @DisplayName("Should preserve formatting when adding test dependency")
  void shouldPreserveFormattingWhenAddingTestDependency() {
    // Given: Build file with specific indentation
    String buildFileContent = """
        dependencies {
            implementation("org.springframework.boot:spring-boot-starter-webflux")
        }
        """;

    AdapterConfig config = createAdapterConfig("mongodb");
    AdapterMetadata metadata = createAdapterMetadataWithTestDependencies();

    when(validator.validate(any(), any())).thenReturn(ValidationResult.success());
    when(configurationPort.readConfiguration(any())).thenReturn(Optional.empty());
    when(generator.generate(any(), any(), any())).thenReturn(List.of());
    when(templateRepository.loadAdapterMetadata(anyString())).thenReturn(metadata);
    when(fileSystemPort.exists(any())).thenReturn(true);
    when(fileSystemPort.readFile(any())).thenReturn(buildFileContent);

    // When: Generate adapter
    GenerationResult result = useCase.execute(tempDir, config);

    // Then: Should maintain consistent indentation (4 spaces)
    assertTrue(result.success());

    verify(fileSystemPort).writeFile(org.mockito.ArgumentMatchers.argThat(file -> {
      String content = file.content();
      // Check that new dependency has same indentation as existing ones
      boolean hasCorrectIndentation = content
          .contains("    testImplementation(\"de.flapdoodle.embed:de.flapdoodle.embed.mongo:4.11.0\")");

      return hasCorrectIndentation;
    }));
  }

  @Test
  @DisplayName("Should handle multiple test dependencies without removing any")
  void shouldHandleMultipleTestDependenciesWithoutRemovingAny() {
    // Given: Build file with existing dependencies
    String buildFileContent = """
        dependencies {
            implementation("org.springframework.boot:spring-boot-starter-webflux")
            testImplementation("org.springframework.boot:spring-boot-starter-test")
        }
        """;

    AdapterConfig config = createAdapterConfig("mongodb");
    AdapterMetadata metadata = createAdapterMetadataWithMultipleTestDependencies();

    when(validator.validate(any(), any())).thenReturn(ValidationResult.success());
    when(configurationPort.readConfiguration(any())).thenReturn(Optional.empty());
    when(generator.generate(any(), any(), any())).thenReturn(List.of());
    when(templateRepository.loadAdapterMetadata(anyString())).thenReturn(metadata);
    when(fileSystemPort.exists(any())).thenReturn(true);
    when(fileSystemPort.readFile(any())).thenReturn(buildFileContent);

    // When: Generate adapter with multiple test dependencies
    GenerationResult result = useCase.execute(tempDir, config);

    // Then: Should add all new dependencies while preserving existing ones
    assertTrue(result.success());

    verify(fileSystemPort, org.mockito.Mockito.atLeastOnce()).writeFile(org.mockito.ArgumentMatchers.argThat(file -> {
      String content = file.content();
      boolean hasExisting = content.contains("spring-boot-starter-test");
      boolean hasNewDep1 = content.contains("de.flapdoodle.embed.mongo");
      boolean hasNewDep2 = content.contains("testcontainers");

      return hasExisting && hasNewDep1 && hasNewDep2;
    }));
  }

  // Helper methods

  private AdapterConfig createAdapterConfig(String type) {
    return AdapterConfig.builder()
        .name("TestAdapter")
        .type(AdapterType.valueOf(type.toUpperCase()))
        .packageName("com.test.adapter")
        .entityName("TestEntity")
        .build();
  }

  private AdapterMetadata createAdapterMetadataWithTestDependencies() {
    AdapterMetadata.Dependency testDep = new AdapterMetadata.Dependency(
        "de.flapdoodle.embed",
        "de.flapdoodle.embed.mongo",
        "4.11.0",
        "test");

    return new AdapterMetadata(
        "mongodb",
        "driven",
        "MongoDB adapter",
        List.of(),
        List.of(testDep),
        null,
        List.of());
  }

  private AdapterMetadata createAdapterMetadataWithMultipleTestDependencies() {
    AdapterMetadata.Dependency testDep1 = new AdapterMetadata.Dependency(
        "de.flapdoodle.embed",
        "de.flapdoodle.embed.mongo",
        "4.11.0",
        "test");

    AdapterMetadata.Dependency testDep2 = new AdapterMetadata.Dependency(
        "org.testcontainers",
        "testcontainers",
        "1.19.0",
        "test");

    return new AdapterMetadata(
        "mongodb",
        "driven",
        "MongoDB adapter",
        List.of(),
        List.of(testDep1, testDep2),
        null,
        List.of());
  }
}
