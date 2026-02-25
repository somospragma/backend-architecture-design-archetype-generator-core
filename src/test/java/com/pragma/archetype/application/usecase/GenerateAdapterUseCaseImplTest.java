package com.pragma.archetype.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
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
 * Unit tests for GenerateAdapterUseCaseImpl.
 * Tests the application properties merging functionality.
 */
class GenerateAdapterUseCaseImplTest {

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

    // Set up default template validation mocks for all tests
    setupDefaultTemplateValidationMocks();
  }

  @Test
  void shouldMergeApplicationPropertiesWhenAdapterHasTemplate() {
    // Given
    AdapterConfig config = createAdapterConfig("mongodb");
    AdapterMetadata metadata = createAdapterMetadataWithProperties();

    when(validator.validate(any(), any())).thenReturn(ValidationResult.success());
    when(configurationPort.readConfiguration(any())).thenReturn(Optional.empty());
    when(generator.generate(any(), any(), any())).thenReturn(List.of());
    when(templateRepository.loadAdapterMetadata(anyString())).thenReturn(metadata);
    when(templateRepository.processTemplate(anyString(), anyMap()))
        .thenReturn("spring:\n  data:\n    mongodb:\n      uri: mongodb://localhost:27017/test");

    Map<String, Object> existingProps = new HashMap<>();
    Map<String, Object> mergedProps = new HashMap<>();
    when(yamlConfigurationAdapter.readYaml(any())).thenReturn(existingProps);
    when(yamlConfigurationAdapter.mergeYaml(any(), any())).thenReturn(mergedProps);

    // When
    GenerationResult result = useCase.execute(tempDir, config);

    // Then
    assertTrue(result.success());
    // loadAdapterMetadata is called twice: once for properties, once for config
    // classes
    verify(templateRepository, org.mockito.Mockito.atLeastOnce()).loadAdapterMetadata("mongodb");
    verify(templateRepository).processTemplate(anyString(), anyMap());
    verify(yamlConfigurationAdapter).readYaml(any());
    verify(yamlConfigurationAdapter).mergeYaml(eq(existingProps), any());
    verify(yamlConfigurationAdapter).writeYaml(any(), eq(mergedProps));
  }

  @Test
  void shouldSkipMergingWhenAdapterHasNoPropertiesTemplate() {
    // Given
    AdapterConfig config = createAdapterConfig("mongodb");
    AdapterMetadata metadata = createAdapterMetadataWithoutProperties();

    when(validator.validate(any(), any())).thenReturn(ValidationResult.success());
    when(configurationPort.readConfiguration(any())).thenReturn(Optional.empty());
    when(generator.generate(any(), any(), any())).thenReturn(List.of());
    when(templateRepository.loadAdapterMetadata(anyString())).thenReturn(metadata);

    // When
    GenerationResult result = useCase.execute(tempDir, config);

    // Then
    assertTrue(result.success());
    // loadAdapterMetadata is called twice: once for properties, once for config
    // classes
    verify(templateRepository, org.mockito.Mockito.atLeastOnce()).loadAdapterMetadata("mongodb");
    verify(templateRepository, never()).processTemplate(anyString(), anyMap());
    verify(yamlConfigurationAdapter, never()).writeYaml(any(), any());
  }

  @Test
  void shouldFailEarlyWhenAdapterMetadataNotFound() {
    // Given
    AdapterConfig config = createAdapterConfig("mongodb");

    when(validator.validate(any(), any())).thenReturn(ValidationResult.success());
    when(configurationPort.readConfiguration(any())).thenReturn(Optional.empty());
    when(templateRepository.loadAdapterMetadata(anyString()))
        .thenThrow(new TemplateRepository.TemplateNotFoundException("Adapter metadata not found"));

    // When
    GenerationResult result = useCase.execute(tempDir, config);

    // Then
    // With validation-before-modification, should fail early when metadata not
    // found
    assertTrue(result.isFailure());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("Adapter metadata not found")));
    verify(generator, never()).generate(any(), any(), any());
    verify(yamlConfigurationAdapter, never()).writeYaml(any(), any());
  }

  @Test
  void shouldReturnFailureWhenValidationFails() {
    // Given
    AdapterConfig config = createAdapterConfig("mongodb");
    ValidationResult validationResult = ValidationResult.failure("Invalid configuration");

    when(validator.validate(any(), any())).thenReturn(validationResult);

    // When
    GenerationResult result = useCase.execute(tempDir, config);

    // Then
    assertTrue(result.isFailure());
    assertEquals(1, result.errors().size());
    assertEquals("Invalid configuration", result.errors().get(0));
  }

  @Test
  void shouldReturnFailureWhenAdapterAlreadyExists() {
    // Given
    AdapterConfig config = createAdapterConfig("mongodb");
    ValidationResult validationResult = ValidationResult.failure(
        "Adapter already exists: MongoDBAdapter.java at /test/project/src/main/java/com/test/adapter/MongoDBAdapter.java. "
            +
            "Please use a different name or remove the existing adapter before generating a new one.");

    when(validator.validate(any(), any())).thenReturn(validationResult);

    // When
    GenerationResult result = useCase.execute(tempDir, config);

    // Then
    assertTrue(result.isFailure());
    assertEquals(1, result.errors().size());
    assertTrue(result.errors().get(0).contains("Adapter already exists"));
    assertTrue(result.errors().get(0).contains("Please use a different name or remove the existing adapter"));
    verify(generator, never()).generate(any(), any(), any());
    verify(fileSystemPort, never()).writeFile(any());
  }

  @Test
  void shouldGenerateConfigurationClassesWhenAdapterHasConfigClasses() {
    // Given
    AdapterConfig config = createAdapterConfig("mongodb");
    AdapterMetadata metadata = createAdapterMetadataWithConfigClasses();
    com.pragma.archetype.domain.model.config.ProjectConfig projectConfig = createProjectConfig();

    when(validator.validate(any(), any())).thenReturn(ValidationResult.success());
    when(configurationPort.readConfiguration(any())).thenReturn(Optional.of(projectConfig));
    when(generator.generate(any(), any(), any())).thenReturn(List.of());
    when(templateRepository.loadAdapterMetadata(anyString())).thenReturn(metadata);
    when(templateRepository.loadAdapterMetadata(anyString(), anyString(), anyString(), anyString()))
        .thenReturn(metadata);
    when(templateRepository.processTemplate(anyString(), anyMap()))
        .thenReturn("public class MongoConfig { }");

    // When
    GenerationResult result = useCase.execute(tempDir, config);

    // Then
    assertTrue(result.success());
    // loadAdapterMetadata is called (either legacy or new signature)
    verify(templateRepository, org.mockito.Mockito.atLeastOnce()).loadAdapterMetadata(anyString(), anyString(),
        anyString(), anyString());
    verify(templateRepository).processTemplate(eq("adapters/mongodb/MongoConfig.java.ftl"), anyMap());
    verify(fileSystemPort, org.mockito.Mockito.atLeastOnce()).writeFile(any());
  }

  @Test
  void shouldSkipConfigClassGenerationWhenAdapterHasNoConfigClasses() {
    // Given
    AdapterConfig config = createAdapterConfig("mongodb");
    AdapterMetadata metadata = createAdapterMetadataWithoutConfigClasses();

    when(validator.validate(any(), any())).thenReturn(ValidationResult.success());
    when(configurationPort.readConfiguration(any())).thenReturn(Optional.empty());
    when(generator.generate(any(), any(), any())).thenReturn(List.of());
    when(templateRepository.loadAdapterMetadata(anyString())).thenReturn(metadata);

    // When
    GenerationResult result = useCase.execute(tempDir, config);

    // Then
    assertTrue(result.success());
    // loadAdapterMetadata is called twice: once for properties, once for config
    // classes
    verify(templateRepository, org.mockito.Mockito.atLeastOnce()).loadAdapterMetadata("mongodb");
    // Verify that processTemplate is never called for config classes
    // (it might be called for properties, but not for config classes)
  }

  @Test
  void shouldHandleConfigClassTemplateNotFoundGracefully() {
    // Given
    AdapterConfig config = createAdapterConfig("mongodb");
    AdapterMetadata metadata = createAdapterMetadataWithConfigClasses();
    com.pragma.archetype.domain.model.config.ProjectConfig projectConfig = createProjectConfig();

    when(validator.validate(any(), any())).thenReturn(ValidationResult.success());
    when(configurationPort.readConfiguration(any())).thenReturn(Optional.of(projectConfig));
    when(generator.generate(any(), any(), any())).thenReturn(List.of());
    when(templateRepository.loadAdapterMetadata(anyString())).thenReturn(metadata);
    when(templateRepository.processTemplate(anyString(), anyMap()))
        .thenThrow(new TemplateRepository.TemplateNotFoundException("Config template not found"));

    // When
    GenerationResult result = useCase.execute(tempDir, config);

    // Then
    assertTrue(result.success()); // Should still succeed, just log warning
  }

  @Test
  void shouldGenerateMultipleConfigurationClasses() {
    // Given
    AdapterConfig config = createAdapterConfig("mongodb");
    AdapterMetadata metadata = createAdapterMetadataWithMultipleConfigClasses();
    com.pragma.archetype.domain.model.config.ProjectConfig projectConfig = createProjectConfig();

    when(validator.validate(any(), any())).thenReturn(ValidationResult.success());
    when(configurationPort.readConfiguration(any())).thenReturn(Optional.of(projectConfig));
    when(generator.generate(any(), any(), any())).thenReturn(List.of());
    when(templateRepository.loadAdapterMetadata(anyString())).thenReturn(metadata);
    when(templateRepository.loadAdapterMetadata(anyString(), anyString(), anyString(), anyString()))
        .thenReturn(metadata);
    when(templateRepository.processTemplate(anyString(), anyMap()))
        .thenReturn("public class ConfigClass { }");

    // When
    GenerationResult result = useCase.execute(tempDir, config);

    // Then
    assertTrue(result.success());
    // loadAdapterMetadata is called (either legacy or new signature)
    verify(templateRepository, org.mockito.Mockito.atLeastOnce()).loadAdapterMetadata(anyString(), anyString(),
        anyString(), anyString());
    // Should process both config class templates
    verify(templateRepository).processTemplate(eq("adapters/mongodb/MongoConfig.java.ftl"), anyMap());
    verify(templateRepository).processTemplate(eq("adapters/mongodb/MongoProperties.java.ftl"), anyMap());
  }

  @Test
  void shouldAddTestDependenciesWhenAdapterHasTestDependencies() {
    // Given
    AdapterConfig config = createAdapterConfig("mongodb");
    AdapterMetadata metadata = createAdapterMetadataWithTestDependencies();
    String buildFileContent = """
        plugins {
            id("java")
        }

        dependencies {
            implementation("org.springframework.boot:spring-boot-starter")
        }
        """;

    when(validator.validate(any(), any())).thenReturn(ValidationResult.success());
    when(configurationPort.readConfiguration(any())).thenReturn(Optional.empty());
    when(generator.generate(any(), any(), any())).thenReturn(List.of());
    when(templateRepository.loadAdapterMetadata(anyString())).thenReturn(metadata);
    when(fileSystemPort.exists(any())).thenReturn(true);
    when(fileSystemPort.readFile(any())).thenReturn(buildFileContent);

    // When
    GenerationResult result = useCase.execute(tempDir, config);

    // Then
    assertTrue(result.success());
    verify(templateRepository, org.mockito.Mockito.atLeastOnce()).loadAdapterMetadata("mongodb");
    verify(fileSystemPort).readFile(any());
    verify(fileSystemPort, org.mockito.Mockito.atLeast(1)).writeFile(any());
  }

  @Test
  void shouldSkipTestDependenciesWhenAdapterHasNoTestDependencies() {
    // Given
    AdapterConfig config = createAdapterConfig("mongodb");
    AdapterMetadata metadata = createAdapterMetadataWithoutTestDependencies();

    when(validator.validate(any(), any())).thenReturn(ValidationResult.success());
    when(configurationPort.readConfiguration(any())).thenReturn(Optional.empty());
    when(generator.generate(any(), any(), any())).thenReturn(List.of());
    when(templateRepository.loadAdapterMetadata(anyString())).thenReturn(metadata);

    // When
    GenerationResult result = useCase.execute(tempDir, config);

    // Then
    assertTrue(result.success());
    verify(templateRepository, org.mockito.Mockito.atLeastOnce()).loadAdapterMetadata("mongodb");
    // Verify that readFile is never called for build file (no test dependencies to
    // add)
    verify(fileSystemPort, never()).readFile(any());
  }

  @Test
  void shouldHandleMissingBuildFileGracefully() {
    // Given
    AdapterConfig config = createAdapterConfig("mongodb");
    AdapterMetadata metadata = createAdapterMetadataWithTestDependencies();

    when(validator.validate(any(), any())).thenReturn(ValidationResult.success());
    when(configurationPort.readConfiguration(any())).thenReturn(Optional.empty());
    when(generator.generate(any(), any(), any())).thenReturn(List.of());
    when(templateRepository.loadAdapterMetadata(anyString())).thenReturn(metadata);
    when(fileSystemPort.exists(any())).thenReturn(false);

    // When
    GenerationResult result = useCase.execute(tempDir, config);

    // Then
    assertTrue(result.success()); // Should still succeed, just log warning
    verify(fileSystemPort, org.mockito.Mockito.atLeastOnce()).exists(any());
    verify(fileSystemPort, never()).readFile(any());
  }

  @Test
  void shouldAddTestDependenciesWithCorrectScope() {
    // Given
    AdapterConfig config = createAdapterConfig("mongodb");
    AdapterMetadata metadata = createAdapterMetadataWithTestDependencies();
    String buildFileContent = """
        plugins {
            id("java")
        }

        dependencies {
            implementation("org.springframework.boot:spring-boot-starter")
        }
        """;

    when(validator.validate(any(), any())).thenReturn(ValidationResult.success());
    when(configurationPort.readConfiguration(any())).thenReturn(Optional.empty());
    when(generator.generate(any(), any(), any())).thenReturn(List.of());
    when(templateRepository.loadAdapterMetadata(anyString())).thenReturn(metadata);
    when(fileSystemPort.exists(any())).thenReturn(true);
    when(fileSystemPort.readFile(any())).thenReturn(buildFileContent);

    // When
    GenerationResult result = useCase.execute(tempDir, config);

    // Then
    assertTrue(result.success());
    // Verify that writeFile is called with content containing testImplementation
    verify(fileSystemPort).writeFile(org.mockito.ArgumentMatchers.argThat(file -> {
      String content = file.content();
      return content.contains("testImplementation(\"de.flapdoodle.embed:de.flapdoodle.embed.mongo:4.11.0\")");
    }));
  }

  @Test
  void shouldNotAddDuplicateTestDependencies() {
    // Given
    AdapterConfig config = createAdapterConfig("mongodb");
    AdapterMetadata metadata = createAdapterMetadataWithTestDependencies();
    String buildFileContent = """
        plugins {
            id("java")
        }

        dependencies {
            implementation("org.springframework.boot:spring-boot-starter")
            testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo:4.11.0")
        }
        """;

    when(validator.validate(any(), any())).thenReturn(ValidationResult.success());
    when(configurationPort.readConfiguration(any())).thenReturn(Optional.empty());
    when(generator.generate(any(), any(), any())).thenReturn(List.of());
    when(templateRepository.loadAdapterMetadata(anyString())).thenReturn(metadata);
    when(fileSystemPort.exists(any())).thenReturn(true);
    when(fileSystemPort.readFile(any())).thenReturn(buildFileContent);

    // When
    GenerationResult result = useCase.execute(tempDir, config);

    // Then
    assertTrue(result.success());
    // Verify that writeFile is not called (no changes needed)
    verify(fileSystemPort, never()).writeFile(any());
  }

  @Test
  void shouldCreateBackupBeforeModification() {
    // Given
    AdapterConfig config = createAdapterConfig("mongodb");
    AdapterMetadata metadata = createAdapterMetadataWithProperties();

    when(validator.validate(any(), any())).thenReturn(ValidationResult.success());
    when(configurationPort.readConfiguration(any())).thenReturn(Optional.empty());
    when(generator.generate(any(), any(), any())).thenReturn(List.of());
    when(templateRepository.loadAdapterMetadata(anyString())).thenReturn(metadata);
    when(templateRepository.processTemplate(anyString(), anyMap()))
        .thenReturn("spring:\n  data:\n    mongodb:\n      uri: mongodb://localhost:27017/test");
    when(fileSystemPort.exists(any())).thenReturn(true);
    when(yamlConfigurationAdapter.readYaml(any())).thenReturn(new HashMap<>());
    when(yamlConfigurationAdapter.mergeYaml(any(), any())).thenReturn(new HashMap<>());
    when(backupService.createBackup(any(), any())).thenReturn("backup_123");

    // When
    GenerationResult result = useCase.execute(tempDir, config);

    // Then
    assertTrue(result.success());
    verify(backupService).createBackup(eq(tempDir), any());
    verify(backupService).deleteBackup(tempDir, "backup_123");
  }

  @Test
  void shouldRestoreBackupOnFailure() {
    // Given
    AdapterConfig config = createAdapterConfig("mongodb");
    AdapterMetadata metadata = createAdapterMetadataWithProperties();

    when(validator.validate(any(), any())).thenReturn(ValidationResult.success());
    when(configurationPort.readConfiguration(any())).thenReturn(Optional.empty());
    when(generator.generate(any(), any(), any())).thenThrow(new RuntimeException("Generation failed"));
    when(templateRepository.loadAdapterMetadata(anyString())).thenReturn(metadata);
    when(fileSystemPort.exists(any())).thenReturn(true); // Files exist for backup
    when(backupService.createBackup(any(), any())).thenReturn("backup_123");

    // When
    GenerationResult result = useCase.execute(tempDir, config);

    // Then
    assertTrue(result.isFailure());
    verify(backupService).createBackup(eq(tempDir), any());
    verify(backupService).restoreBackup(tempDir, "backup_123");
    verify(backupService, never()).deleteBackup(any(), any());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("rolled back")));
  }

  @Test
  void shouldDisplayBackupLocationWhenRestoreFails() {
    // Given
    AdapterConfig config = createAdapterConfig("mongodb");
    AdapterMetadata metadata = createAdapterMetadataWithProperties();

    when(validator.validate(any(), any())).thenReturn(ValidationResult.success());
    when(configurationPort.readConfiguration(any())).thenReturn(Optional.empty());
    when(generator.generate(any(), any(), any())).thenThrow(new RuntimeException("Generation failed"));
    when(templateRepository.loadAdapterMetadata(anyString())).thenReturn(metadata);
    when(fileSystemPort.exists(any())).thenReturn(true); // Files exist for backup
    when(backupService.createBackup(any(), any())).thenReturn("backup_123");
    org.mockito.Mockito.doThrow(new com.pragma.archetype.domain.service.BackupService.BackupException("Restore failed"))
        .when(backupService).restoreBackup(any(), anyString());

    // When
    GenerationResult result = useCase.execute(tempDir, config);

    // Then
    assertTrue(result.isFailure());
    verify(backupService).createBackup(eq(tempDir), any());
    verify(backupService).restoreBackup(tempDir, "backup_123");
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("Backup location:")));
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("Manual recovery may be required")));
  }

  @Test
  void shouldValidateAllTemplatesBeforeModification() {
    // Given
    AdapterConfig config = createAdapterConfig("mongodb");
    AdapterMetadata metadata = createAdapterMetadataWithProperties();

    when(validator.validate(any(), any())).thenReturn(ValidationResult.success());
    when(configurationPort.readConfiguration(any())).thenReturn(Optional.empty());
    when(templateRepository.loadAdapterMetadata(anyString())).thenReturn(metadata);

    // Mock template validation - all templates valid
    when(templateRepository.validateTemplate(anyString())).thenReturn(ValidationResult.success());
    when(templateRepository.templateExists(anyString())).thenReturn(true);

    when(generator.generate(any(), any(), any())).thenReturn(List.of());
    when(templateRepository.processTemplate(anyString(), anyMap()))
        .thenReturn("spring:\n  data:\n    mongodb:\n      uri: mongodb://localhost:27017/test");
    when(yamlConfigurationAdapter.readYaml(any())).thenReturn(new HashMap<>());
    when(yamlConfigurationAdapter.mergeYaml(any(), any())).thenReturn(new HashMap<>());

    // When
    GenerationResult result = useCase.execute(tempDir, config);

    // Then
    assertTrue(result.success());
    // Verify that templates were validated before generation
    verify(templateRepository, org.mockito.Mockito.atLeastOnce()).validateTemplate(anyString());
    verify(generator).generate(any(), any(), any());
  }

  @Test
  void shouldFailBeforeModificationWhenTemplateValidationFails() {
    // Given
    AdapterConfig config = createAdapterConfig("mongodb");
    AdapterMetadata metadata = createAdapterMetadataWithProperties();

    when(validator.validate(any(), any())).thenReturn(ValidationResult.success());
    when(configurationPort.readConfiguration(any())).thenReturn(Optional.empty());
    when(templateRepository.loadAdapterMetadata(anyString())).thenReturn(metadata);

    // Mock template validation - adapter template is invalid (override default
    // mock)
    when(templateRepository.validateTemplate(
        eq("frameworks/spring/reactive/adapters/driven-adapters/mongodb/Adapter.java.ftl")))
        .thenReturn(ValidationResult.failure("FreeMarker syntax error at line 10"));

    // When
    GenerationResult result = useCase.execute(tempDir, config);

    // Then
    assertTrue(result.isFailure());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("FreeMarker syntax error")));
    // Verify that generation was never attempted
    verify(generator, never()).generate(any(), any(), any());
    verify(fileSystemPort, never()).writeFile(any());
    verify(backupService, never()).createBackup(any(), any());
  }

  @Test
  void shouldFailBeforeModificationWhenApplicationPropertiesTemplateNotFound() {
    // Given
    AdapterConfig config = createAdapterConfig("mongodb");
    AdapterMetadata metadata = createAdapterMetadataWithProperties();

    when(validator.validate(any(), any())).thenReturn(ValidationResult.success());
    when(configurationPort.readConfiguration(any())).thenReturn(Optional.empty());
    when(templateRepository.loadAdapterMetadata(anyString())).thenReturn(metadata);

    // Application properties template doesn't exist (override default mock)
    when(templateRepository.templateExists(eq("adapters/mongodb/application-properties.yml.ftl")))
        .thenReturn(false);

    // When
    GenerationResult result = useCase.execute(tempDir, config);

    // Then
    assertTrue(result.isFailure());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("Application properties template not found")));
    // Verify that generation was never attempted
    verify(generator, never()).generate(any(), any(), any());
    verify(fileSystemPort, never()).writeFile(any());
  }

  @Test
  void shouldFailBeforeModificationWhenConfigClassTemplateNotFound() {
    // Given
    AdapterConfig config = createAdapterConfig("mongodb");
    AdapterMetadata metadata = createAdapterMetadataWithConfigClasses();
    com.pragma.archetype.domain.model.config.ProjectConfig projectConfig = createProjectConfig();

    when(validator.validate(any(), any())).thenReturn(ValidationResult.success());
    when(configurationPort.readConfiguration(any())).thenReturn(Optional.of(projectConfig));
    when(templateRepository.loadAdapterMetadata(anyString())).thenReturn(metadata);
    when(templateRepository.loadAdapterMetadata(anyString(), anyString(), anyString(), anyString()))
        .thenReturn(metadata);

    // Configuration class template doesn't exist (override default mock)
    when(templateRepository.templateExists(eq("adapters/mongodb/MongoConfig.java.ftl")))
        .thenReturn(false);

    // When
    GenerationResult result = useCase.execute(tempDir, config);

    // Then
    assertTrue(result.isFailure());
    assertTrue(result.errors().stream()
        .anyMatch(e -> e.contains("Configuration class template not found") && e.contains("MongoConfig")));
    // Verify that generation was never attempted
    verify(generator, never()).generate(any(), any(), any());
    verify(fileSystemPort, never()).writeFile(any());
  }

  @Test
  void shouldDisplayAllValidationErrorsBeforeAttemptingGeneration() {
    // Given
    AdapterConfig config = createAdapterConfig("mongodb");
    AdapterMetadata metadata = createAdapterMetadataWithConfigClasses();
    com.pragma.archetype.domain.model.config.ProjectConfig projectConfig = createProjectConfig();

    when(validator.validate(any(), any())).thenReturn(ValidationResult.success());
    when(configurationPort.readConfiguration(any())).thenReturn(Optional.of(projectConfig));
    when(templateRepository.loadAdapterMetadata(anyString())).thenReturn(metadata);

    // Mock multiple template validation failures (override default mocks)
    when(templateRepository.validateTemplate(
        eq("frameworks/spring/reactive/adapters/driven-adapters/mongodb/Adapter.java.ftl")))
        .thenReturn(ValidationResult.failure("Adapter template syntax error"));
    when(templateRepository.validateTemplate(
        eq("frameworks/spring/reactive/adapters/driven-adapters/mongodb/Entity.java.ftl")))
        .thenReturn(ValidationResult.failure("Entity template syntax error"));

    // When
    GenerationResult result = useCase.execute(tempDir, config);

    // Then
    assertTrue(result.isFailure());
    // Should have multiple errors
    assertTrue(result.errors().size() >= 2);
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("Adapter template")));
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("Entity template")));
    // Verify that generation was never attempted
    verify(generator, never()).generate(any(), any(), any());
  }

  /**
   * Sets up default mocks for template validation that most tests need.
   * Individual tests can override these as needed.
   */
  private void setupDefaultTemplateValidationMocks() {
    // By default, all templates exist and are valid
    when(templateRepository.templateExists(anyString())).thenReturn(true);
    when(templateRepository.validateTemplate(anyString())).thenReturn(ValidationResult.success());

    // Set up default mock for the new loadAdapterMetadata signature (4 parameters)
    // This will be used when ProjectConfig is available
    // Individual tests can override this with specific metadata
    when(templateRepository.loadAdapterMetadata(anyString(), anyString(), anyString(), anyString()))
        .thenAnswer(invocation -> {
          // Return a basic metadata that can be overridden by specific tests
          return createBasicAdapterMetadata();
        });
  }

  private com.pragma.archetype.domain.model.config.ProjectConfig createProjectConfig() {
    return com.pragma.archetype.domain.model.config.ProjectConfig.builder()
        .name("test-project")
        .basePackage("com.test")
        .architecture(com.pragma.archetype.domain.model.project.ArchitectureType.HEXAGONAL_SINGLE)
        .paradigm(com.pragma.archetype.domain.model.project.Paradigm.REACTIVE)
        .framework(com.pragma.archetype.domain.model.project.Framework.SPRING)
        .pluginVersion("1.0.0")
        .adaptersAsModules(false)
        .build();
  }

  private AdapterMetadata createBasicAdapterMetadata() {
    return new AdapterMetadata(
        "basic-adapter",
        "driven",
        "Basic adapter for testing",
        List.of(),
        List.of(),
        null,
        List.of());
  }

  private AdapterMetadata createAdapterMetadataWithConfigClasses() {
    AdapterMetadata.ConfigurationClass configClass = new AdapterMetadata.ConfigurationClass(
        "MongoConfig",
        "infrastructure.config",
        "MongoConfig.java.ftl");

    return new AdapterMetadata(
        "mongodb",
        "driven",
        "MongoDB adapter",
        List.of(),
        List.of(),
        null,
        List.of(configClass));
  }

  private AdapterMetadata createAdapterMetadataWithoutConfigClasses() {
    return new AdapterMetadata(
        "mongodb",
        "driven",
        "MongoDB adapter",
        List.of(),
        List.of(),
        null,
        List.of());
  }

  private AdapterMetadata createAdapterMetadataWithMultipleConfigClasses() {
    AdapterMetadata.ConfigurationClass configClass1 = new AdapterMetadata.ConfigurationClass(
        "MongoConfig",
        "infrastructure.config",
        "MongoConfig.java.ftl");

    AdapterMetadata.ConfigurationClass configClass2 = new AdapterMetadata.ConfigurationClass(
        "MongoProperties",
        "infrastructure.config",
        "MongoProperties.java.ftl");

    return new AdapterMetadata(
        "mongodb",
        "driven",
        "MongoDB adapter",
        List.of(),
        List.of(),
        null,
        List.of(configClass1, configClass2));
  }

  private AdapterConfig createAdapterConfig(String type) {
    return AdapterConfig.builder()
        .name("TestAdapter")
        .type(AdapterType.valueOf(type.toUpperCase()))
        .packageName("com.test.adapter")
        .entityName("TestEntity")
        .build();
  }

  private AdapterMetadata createAdapterMetadataWithProperties() {
    return new AdapterMetadata(
        "mongodb",
        "driven",
        "MongoDB adapter",
        List.of(),
        List.of(),
        "application-properties.yml.ftl",
        List.of());
  }

  private AdapterMetadata createAdapterMetadataWithoutProperties() {
    return new AdapterMetadata(
        "mongodb",
        "driven",
        "MongoDB adapter",
        List.of(),
        List.of(),
        null,
        List.of());
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

  private AdapterMetadata createAdapterMetadataWithoutTestDependencies() {
    return new AdapterMetadata(
        "mongodb",
        "driven",
        "MongoDB adapter",
        List.of(),
        List.of(),
        null,
        List.of());
  }
}
