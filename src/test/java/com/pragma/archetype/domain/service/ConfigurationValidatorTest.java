package com.pragma.archetype.domain.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pragma.archetype.domain.model.StructureMetadata;
import com.pragma.archetype.domain.model.ValidationResult;
import com.pragma.archetype.domain.model.adapter.AdapterMetadata;
import com.pragma.archetype.domain.model.config.ProjectConfig;
import com.pragma.archetype.domain.model.config.TemplateConfig;
import com.pragma.archetype.domain.model.config.TemplateMode;
import com.pragma.archetype.domain.model.project.ArchitectureType;
import com.pragma.archetype.domain.model.project.Framework;
import com.pragma.archetype.domain.model.project.Paradigm;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.port.out.FileSystemPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConfigurationValidator Tests")
class ConfigurationValidatorTest {

  @Nested
  @DisplayName("validateProjectConfig Tests")
  class ValidateProjectConfigTests {

    @Test
    @DisplayName("Should fail when configuration file does not exist")
    void shouldFailWhenConfigurationFileDoesNotExist() {
      // Given
      when(configurationPort.configurationExists(projectPath)).thenReturn(false);

      // When
      ValidationResult result = validator.validateProjectConfig(projectPath);

      // Then
      assertFalse(result.valid());
      assertTrue(result.errors().stream()
          .anyMatch(e -> e.contains(".cleanarch.yml not found")));
      assertTrue(result.errors().stream()
          .anyMatch(e -> e.contains("initCleanArch")));
    }

    @Test
    @DisplayName("Should fail when configuration cannot be parsed")
    void shouldFailWhenConfigurationCannotBeParsed() {
      // Given
      when(configurationPort.configurationExists(projectPath)).thenReturn(true);
      when(configurationPort.readConfiguration(projectPath)).thenReturn(Optional.empty());

      // When
      ValidationResult result = validator.validateProjectConfig(projectPath);

      // Then
      assertFalse(result.valid());
      assertTrue(result.errors().stream()
          .anyMatch(e -> e.contains("Failed to parse .cleanarch.yml")));
    }

    @Test
    @DisplayName("Should fail when project name is missing")
    void shouldFailWhenProjectNameIsMissing() {
      // Given - Mock a config with blank name (bypassing constructor validation)
      ProjectConfig config = mock(ProjectConfig.class);
      when(config.name()).thenReturn("");
      when(config.basePackage()).thenReturn("com.example.myservice");
      when(config.architecture()).thenReturn(ArchitectureType.HEXAGONAL_SINGLE);
      when(config.paradigm()).thenReturn(Paradigm.REACTIVE);
      when(config.framework()).thenReturn(Framework.SPRING);

      when(configurationPort.configurationExists(projectPath)).thenReturn(true);
      when(configurationPort.readConfiguration(projectPath)).thenReturn(Optional.of(config));

      // When
      ValidationResult result = validator.validateProjectConfig(projectPath);

      // Then
      assertFalse(result.valid());
      assertTrue(result.errors().stream()
          .anyMatch(e -> e.contains("project.name")));
    }

    @Test
    @DisplayName("Should fail when base package is missing")
    void shouldFailWhenBasePackageIsMissing() {
      // Given - Mock a config with blank base package
      ProjectConfig config = mock(ProjectConfig.class);
      when(config.name()).thenReturn("my-service");
      when(config.basePackage()).thenReturn("");
      when(config.architecture()).thenReturn(ArchitectureType.HEXAGONAL_SINGLE);
      when(config.paradigm()).thenReturn(Paradigm.REACTIVE);
      when(config.framework()).thenReturn(Framework.SPRING);

      when(configurationPort.configurationExists(projectPath)).thenReturn(true);
      when(configurationPort.readConfiguration(projectPath)).thenReturn(Optional.of(config));

      // When
      ValidationResult result = validator.validateProjectConfig(projectPath);

      // Then
      assertFalse(result.valid());
      assertTrue(result.errors().stream()
          .anyMatch(e -> e.contains("project.basePackage")));
    }

    @Test
    @DisplayName("Should fail when architecture type is missing")
    void shouldFailWhenArchitectureTypeIsMissing() {
      // Given - Mock a config with null architecture
      ProjectConfig config = mock(ProjectConfig.class);
      when(config.name()).thenReturn("my-service");
      when(config.basePackage()).thenReturn("com.example.myservice");
      when(config.architecture()).thenReturn(null, null);
      when(config.paradigm()).thenReturn(Paradigm.REACTIVE);
      when(config.framework()).thenReturn(Framework.SPRING);

      when(configurationPort.configurationExists(projectPath)).thenReturn(true);
      when(configurationPort.readConfiguration(projectPath)).thenReturn(Optional.of(config));

      // When
      ValidationResult result = validator.validateProjectConfig(projectPath);

      // Then
      assertFalse(result.valid());
      assertTrue(result.errors().stream()
          .anyMatch(e -> e.contains("architecture.type")));
    }

    @Test
    @DisplayName("Should fail when package name is invalid")
    void shouldFailWhenPackageNameIsInvalid() {
      // Given - Mock a config with invalid package name
      ProjectConfig config = mock(ProjectConfig.class);
      when(config.name()).thenReturn("my-service");
      when(config.basePackage()).thenReturn("InvalidPackage");
      when(config.architecture()).thenReturn(ArchitectureType.HEXAGONAL_SINGLE);
      when(config.paradigm()).thenReturn(Paradigm.REACTIVE);
      when(config.framework()).thenReturn(Framework.SPRING);

      when(configurationPort.configurationExists(projectPath)).thenReturn(true);
      when(configurationPort.readConfiguration(projectPath)).thenReturn(Optional.of(config));

      // When
      ValidationResult result = validator.validateProjectConfig(projectPath);

      // Then
      assertFalse(result.valid());
      assertTrue(result.errors().stream()
          .anyMatch(e -> e.contains("Invalid package name")));
    }

    @Test
    @DisplayName("Should fail when project name is invalid")
    void shouldFailWhenProjectNameIsInvalid() {
      // Given - Mock a config with invalid project name
      ProjectConfig config = mock(ProjectConfig.class);
      when(config.name()).thenReturn("Invalid_Project_Name");
      when(config.basePackage()).thenReturn("com.example.myservice");
      when(config.architecture()).thenReturn(ArchitectureType.HEXAGONAL_SINGLE);
      when(config.paradigm()).thenReturn(Paradigm.REACTIVE);
      when(config.framework()).thenReturn(Framework.SPRING);

      when(configurationPort.configurationExists(projectPath)).thenReturn(true);
      when(configurationPort.readConfiguration(projectPath)).thenReturn(Optional.of(config));

      // When
      ValidationResult result = validator.validateProjectConfig(projectPath);

      // Then
      assertFalse(result.valid());
      assertTrue(result.errors().stream()
          .anyMatch(e -> e.contains("Invalid project name")));
    }

    @Test
    @DisplayName("Should succeed when configuration is valid")
    void shouldSucceedWhenConfigurationIsValid() {
      // Given
      ProjectConfig config = createValidConfig();

      when(configurationPort.configurationExists(projectPath)).thenReturn(true);
      when(configurationPort.readConfiguration(projectPath)).thenReturn(Optional.of(config));

      // When
      ValidationResult result = validator.validateProjectConfig(projectPath);

      // Then
      assertTrue(result.valid());
      assertTrue(result.errors().isEmpty());
    }

    @Test
    @DisplayName("Should handle parsing exceptions gracefully")
    void shouldHandleParsingExceptionsGracefully() {
      // Given
      when(configurationPort.configurationExists(projectPath)).thenReturn(true);
      when(configurationPort.readConfiguration(projectPath))
          .thenThrow(new RuntimeException("YAML parsing error"));

      // When
      ValidationResult result = validator.validateProjectConfig(projectPath);

      // Then
      assertFalse(result.valid());
      assertTrue(result.errors().stream()
          .anyMatch(e -> e.contains("Failed to parse .cleanarch.yml")));
      assertTrue(result.errors().stream()
          .anyMatch(e -> e.contains("YAML syntax")));
    }
  }

  @Nested
  @DisplayName("validateTemplateConfig Tests")
  class ValidateTemplateConfigTests {

    @Test
    @DisplayName("Should fail when template config is null")
    void shouldFailWhenTemplateConfigIsNull() {
      // When
      ValidationResult result = validator.validateTemplateConfig(null);

      // Then
      assertFalse(result.valid());
      assertTrue(result.errors().stream()
          .anyMatch(e -> e.contains("Template configuration is null")));
    }

    @Test
    @DisplayName("Should fail when local path does not exist")
    void shouldFailWhenLocalPathDoesNotExist() {
      // Given
      TemplateConfig config = new TemplateConfig(
          TemplateMode.DEVELOPER,
          null,
          null,
          null,
          "/non/existent/path",
          false);

      when(fileSystemPort.directoryExists(Path.of("/non/existent/path"))).thenReturn(false);

      // When
      ValidationResult result = validator.validateTemplateConfig(config);

      // Then
      assertFalse(result.valid());
      assertTrue(result.errors().stream()
          .anyMatch(e -> e.contains("Local template path does not exist")));
    }

    @Test
    @DisplayName("Should succeed when local path exists")
    void shouldSucceedWhenLocalPathExists() {
      // Given
      TemplateConfig config = new TemplateConfig(
          TemplateMode.DEVELOPER,
          null,
          null,
          null,
          "/valid/path",
          false);

      when(fileSystemPort.directoryExists(Path.of("/valid/path"))).thenReturn(true);

      // When
      ValidationResult result = validator.validateTemplateConfig(config);

      // Then
      assertTrue(result.valid());
    }

    @Test
    @DisplayName("Should fail when repository URL is missing in remote mode")
    void shouldFailWhenRepositoryUrlIsMissingInRemoteMode() {
      // Given
      TemplateConfig config = new TemplateConfig(
          TemplateMode.PRODUCTION,
          null,
          "main",
          null,
          null,
          true);

      // When
      ValidationResult result = validator.validateTemplateConfig(config);

      // Then
      assertFalse(result.valid());
      assertTrue(result.errors().stream()
          .anyMatch(e -> e.contains("Repository URL is required")));
    }

    @Test
    @DisplayName("Should fail when repository URL is invalid")
    void shouldFailWhenRepositoryUrlIsInvalid() {
      // Given
      TemplateConfig config = new TemplateConfig(
          TemplateMode.PRODUCTION,
          "http://invalid-url.com",
          "main",
          null,
          null,
          true);

      // When
      ValidationResult result = validator.validateTemplateConfig(config);

      // Then
      assertFalse(result.valid());
      assertTrue(result.errors().stream()
          .anyMatch(e -> e.contains("Invalid repository URL")));
    }

    @Test
    @DisplayName("Should fail when branch name is invalid")
    void shouldFailWhenBranchNameIsInvalid() {
      // Given
      TemplateConfig config = new TemplateConfig(
          TemplateMode.PRODUCTION,
          "https://github.com/somospragma/templates",
          "invalid branch name!",
          null,
          null,
          true);

      // When
      ValidationResult result = validator.validateTemplateConfig(config);

      // Then
      assertFalse(result.valid());
      assertTrue(result.errors().stream()
          .anyMatch(e -> e.contains("Invalid branch name")));
    }

    @Test
    @DisplayName("Should succeed with valid remote configuration")
    void shouldSucceedWithValidRemoteConfiguration() {
      // Given
      TemplateConfig config = new TemplateConfig(
          TemplateMode.PRODUCTION,
          "https://github.com/somospragma/templates",
          "main",
          null,
          null,
          true);

      // When
      ValidationResult result = validator.validateTemplateConfig(config);

      // Then
      assertTrue(result.valid());
    }

    @Test
    @DisplayName("Should accept feature branch names")
    void shouldAcceptFeatureBranchNames() {
      // Given
      TemplateConfig config = new TemplateConfig(
          TemplateMode.PRODUCTION,
          "https://github.com/somospragma/templates",
          "feature/init-templates",
          null,
          null,
          true);

      // When
      ValidationResult result = validator.validateTemplateConfig(config);

      // Then
      assertTrue(result.valid());
    }
  }

  @Nested
  @DisplayName("validateStructureMetadata Tests")
  class ValidateStructureMetadataTests {

    @Test
    @DisplayName("Should fail when metadata is null")
    void shouldFailWhenMetadataIsNull() {
      // When
      ValidationResult result = validator.validateStructureMetadata(null);

      // Then
      assertFalse(result.valid());
      assertTrue(result.errors().stream()
          .anyMatch(e -> e.contains("Structure metadata is null")));
    }

    @Test
    @DisplayName("Should fail when architecture type is missing")
    void shouldFailWhenArchitectureTypeIsMissing() {
      // Given
      StructureMetadata metadata = new StructureMetadata(
          "",
          Map.of("driven", "infrastructure/adapter/out/{name}"),
          null,
          null,
          null, null);

      // When
      ValidationResult result = validator.validateStructureMetadata(metadata);

      // Then
      assertFalse(result.valid());
      assertTrue(result.errors().stream()
          .anyMatch(e -> e.contains("Architecture type is required")));
    }

    @Test
    @DisplayName("Should fail when adapter paths are empty")
    void shouldFailWhenAdapterPathsAreEmpty() {
      // Given
      StructureMetadata metadata = new StructureMetadata(
          "hexagonal-single",
          Map.of(),
          null,
          null,
          null, null);

      // When
      ValidationResult result = validator.validateStructureMetadata(metadata);

      // Then
      assertFalse(result.valid());
      assertTrue(result.errors().stream()
          .anyMatch(e -> e.contains("Adapter paths are required")));
    }

    @Test
    @DisplayName("Should fail when adapter paths missing driven and driving")
    void shouldFailWhenAdapterPathsMissingDrivenAndDriving() {
      // Given
      StructureMetadata metadata = new StructureMetadata(
          "hexagonal-single",
          Map.of("other", "some/path"),
          null,
          null,
          null, null);

      // When
      ValidationResult result = validator.validateStructureMetadata(metadata);

      // Then
      assertFalse(result.valid());
      assertTrue(result.errors().stream()
          .anyMatch(e -> e.contains("must contain at least 'driven' or 'driving'")));
    }

    @Test
    @DisplayName("Should fail when path template missing {name} placeholder")
    void shouldFailWhenPathTemplateMissingNamePlaceholder() {
      // Given
      StructureMetadata metadata = new StructureMetadata(
          "hexagonal-single",
          Map.of("driven", "infrastructure/adapter/out"),
          null,
          null,
          null, null);

      // When
      ValidationResult result = validator.validateStructureMetadata(metadata);

      // Then
      assertFalse(result.valid());
      assertTrue(result.errors().stream()
          .anyMatch(e -> e.contains("must contain {name} placeholder")));
    }

    @Test
    @DisplayName("Should succeed with valid structure metadata")
    void shouldSucceedWithValidStructureMetadata() {
      // Given
      StructureMetadata metadata = new StructureMetadata(
          "hexagonal-single",
          Map.of(
              "driven", "infrastructure/adapter/out/{name}",
              "driving", "infrastructure/adapter/in/{name}"),
          null,
          null,
          null, null);

      // When
      ValidationResult result = validator.validateStructureMetadata(metadata);

      // Then
      assertTrue(result.valid());
    }

    @Test
    @DisplayName("Should succeed with only driven adapter path")
    void shouldSucceedWithOnlyDrivenAdapterPath() {
      // Given
      StructureMetadata metadata = new StructureMetadata(
          "onion-single",
          Map.of("driven", "infrastructure/adapter/out/{name}"),
          null,
          null,
          null, null);

      // When
      ValidationResult result = validator.validateStructureMetadata(metadata);

      // Then
      assertTrue(result.valid());
    }
  }

  @Nested
  @DisplayName("validateAdapterMetadata Tests")
  class ValidateAdapterMetadataTests {

    @Test
    @DisplayName("Should fail when metadata is null")
    void shouldFailWhenMetadataIsNull() {
      // When
      ValidationResult result = validator.validateAdapterMetadata(null);

      // Then
      assertFalse(result.valid());
      assertTrue(result.errors().stream()
          .anyMatch(e -> e.contains("Adapter metadata is null")));
    }

    @Test
    @DisplayName("Should fail when adapter name is missing")
    void shouldFailWhenAdapterNameIsMissing() {
      // Given
      AdapterMetadata metadata = new AdapterMetadata(
          "",
          "driven",
          "Test adapter",
          List.of(),
          List.of(),
          null,
          List.of());

      // When
      ValidationResult result = validator.validateAdapterMetadata(metadata);

      // Then
      assertFalse(result.valid());
      assertTrue(result.errors().stream()
          .anyMatch(e -> e.contains("Adapter name is required")));
    }

    @Test
    @DisplayName("Should fail when adapter name is invalid")
    void shouldFailWhenAdapterNameIsInvalid() {
      // Given
      AdapterMetadata metadata = new AdapterMetadata(
          "Invalid_Name",
          "driven",
          "Test adapter",
          List.of(),
          List.of(),
          null,
          List.of());

      // When
      ValidationResult result = validator.validateAdapterMetadata(metadata);

      // Then
      assertFalse(result.valid());
      assertTrue(result.errors().stream()
          .anyMatch(e -> e.contains("Invalid adapter name")));
    }

    @Test
    @DisplayName("Should fail when adapter type is missing")
    void shouldFailWhenAdapterTypeIsMissing() {
      // Given
      AdapterMetadata metadata = new AdapterMetadata(
          "mongodb-adapter",
          "",
          "Test adapter",
          List.of(),
          List.of(),
          null,
          List.of());

      // When
      ValidationResult result = validator.validateAdapterMetadata(metadata);

      // Then
      assertFalse(result.valid());
      assertTrue(result.errors().stream()
          .anyMatch(e -> e.contains("Adapter type is required")));
    }

    @Test
    @DisplayName("Should fail when adapter type is invalid")
    void shouldFailWhenAdapterTypeIsInvalid() {
      // Given
      AdapterMetadata metadata = new AdapterMetadata(
          "mongodb-adapter",
          "invalid-type",
          "Test adapter",
          List.of(),
          List.of(),
          null,
          List.of());

      // When
      ValidationResult result = validator.validateAdapterMetadata(metadata);

      // Then
      assertFalse(result.valid());
      assertTrue(result.errors().stream()
          .anyMatch(e -> e.contains("Invalid adapter type")));
    }

    @Test
    @DisplayName("Should fail when dependency is missing group")
    void shouldFailWhenDependencyIsMissingGroup() {
      // Given
      AdapterMetadata.Dependency dep = new AdapterMetadata.Dependency(
          "",
          "spring-boot-starter-data-mongodb",
          "3.0.0",
          "compile");

      AdapterMetadata metadata = new AdapterMetadata(
          "mongodb-adapter",
          "driven",
          "Test adapter",
          List.of(dep),
          List.of(),
          null,
          List.of());

      // When
      ValidationResult result = validator.validateAdapterMetadata(metadata);

      // Then
      assertFalse(result.valid());
      assertTrue(result.errors().stream()
          .anyMatch(e -> e.contains("missing 'group' field")));
    }

    @Test
    @DisplayName("Should fail when configuration class name is invalid")
    void shouldFailWhenConfigurationClassNameIsInvalid() {
      // Given
      AdapterMetadata.ConfigurationClass configClass = new AdapterMetadata.ConfigurationClass(
          "invalidClassName",
          "config",
          "Config.java.ftl");

      AdapterMetadata metadata = new AdapterMetadata(
          "mongodb-adapter",
          "driven",
          "Test adapter",
          List.of(),
          List.of(),
          null,
          List.of(configClass));

      // When
      ValidationResult result = validator.validateAdapterMetadata(metadata);

      // Then
      assertFalse(result.valid());
      assertTrue(result.errors().stream()
          .anyMatch(e -> e.contains("Invalid configuration class name")));
    }

    @Test
    @DisplayName("Should succeed with valid adapter metadata")
    void shouldSucceedWithValidAdapterMetadata() {
      // Given
      AdapterMetadata.Dependency dep = new AdapterMetadata.Dependency(
          "org.springframework.boot",
          "spring-boot-starter-data-mongodb",
          "3.0.0",
          "compile");

      AdapterMetadata.ConfigurationClass configClass = new AdapterMetadata.ConfigurationClass(
          "MongoConfig",
          "config",
          "MongoConfig.java.ftl");

      AdapterMetadata metadata = new AdapterMetadata(
          "mongodb-adapter",
          "driven",
          "MongoDB adapter",
          List.of(dep),
          List.of(),
          "application-properties.yml.ftl",
          List.of(configClass));

      // When
      ValidationResult result = validator.validateAdapterMetadata(metadata);

      // Then
      assertTrue(result.valid());
    }

    @Test
    @DisplayName("Should validate test dependencies")
    void shouldValidateTestDependencies() {
      // Given
      AdapterMetadata.Dependency testDep = new AdapterMetadata.Dependency(
          "",
          "embedded-mongo",
          "4.0.0",
          "test");

      AdapterMetadata metadata = new AdapterMetadata(
          "mongodb-adapter",
          "driven",
          "Test adapter",
          List.of(),
          List.of(testDep),
          null,
          List.of());

      // When
      ValidationResult result = validator.validateAdapterMetadata(metadata);

      // Then
      assertFalse(result.valid());
      assertTrue(result.errors().stream()
          .anyMatch(e -> e.contains("Test dependency") && e.contains("missing 'group' field")));
    }
  }

  @Mock
  private FileSystemPort fileSystemPort;

  @Mock
  private ConfigurationPort configurationPort;

  private ConfigurationValidator validator;

  private Path projectPath;

  @BeforeEach
  void setUp() {
    validator = new ConfigurationValidator(fileSystemPort, configurationPort);
    projectPath = Path.of("/test/project");
  }

  private ProjectConfig createValidConfig() {
    return new ProjectConfig(
        "my-service",
        "com.example.myservice",
        ArchitectureType.HEXAGONAL_SINGLE,
        Paradigm.REACTIVE,
        Framework.SPRING,
        "1.0.0",
        LocalDateTime.now(),
        false,
        Map.of());
  }
}
