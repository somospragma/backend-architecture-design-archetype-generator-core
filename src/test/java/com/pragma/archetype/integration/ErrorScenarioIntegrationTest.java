package com.pragma.archetype.integration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.pragma.archetype.domain.model.TemplateConfig;
import com.pragma.archetype.domain.model.ValidationResult;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import com.pragma.archetype.domain.service.ConfigurationValidator;
import com.pragma.archetype.infrastructure.adapter.out.config.YamlConfigurationAdapter;
import com.pragma.archetype.infrastructure.adapter.out.filesystem.LocalFileSystemAdapter;

/**
 * Integration tests for error scenarios across the system.
 * 
 * **Validates: Requirements 9 (Configuration File Validation), 12 (Template
 * Repository Validation),
 * and 20 (Error Recovery and Rollback)**
 * 
 * This test suite verifies that the plugin handles various error scenarios
 * gracefully with clear
 * error messages and doesn't crash or leave files in a broken state.
 * 
 * Test coverage:
 * 1. Invalid .cleanarch.yml (missing required fields, malformed YAML)
 * 2. Missing .cleanarch.yml file
 * 3. Invalid architecture type
 * 4. Invalid local path
 * 5. Invalid remote repository URL
 * 6. Invalid branch name
 * 7. Invalid package names
 * 8. Edge cases (empty files, special characters)
 */
@DisplayName("Error Scenario Integration Tests")
class ErrorScenarioIntegrationTest {

  @Nested
  @DisplayName("Configuration File Error Scenarios")
  class ConfigurationFileErrorScenarios {

    @Test
    @DisplayName("Should fail with clear error when .cleanarch.yml is missing")
    void shouldFailWhenConfigurationFileMissing() {
      // Given
      Path projectPath = tempDir.resolve("test-project");

      // When
      ValidationResult result = configurationValidator.validateProjectConfig(projectPath);

      // Then
      assertFalse(result.valid(), "Validation should fail when config file is missing");
      assertTrue(result.errors().stream()
          .anyMatch(e -> e.contains(".cleanarch.yml") && e.contains("not found")),
          "Error message should mention missing .cleanarch.yml file");
    }

    @Test
    @DisplayName("Should fail with clear error when .cleanarch.yml has malformed YAML")
    void shouldFailWhenConfigurationHasMalformedYaml() throws IOException {
      // Given
      Path projectPath = tempDir.resolve("test-project");
      Files.createDirectories(projectPath);
      Path configFile = projectPath.resolve(".cleanarch.yml");

      // Create malformed YAML (missing colon)
      String malformedYaml = """
          project:
            name test-project
            basePackage: com.example.test
          """;
      Files.writeString(configFile, malformedYaml);

      // When
      ValidationResult result = configurationValidator.validateProjectConfig(projectPath);

      // Then
      assertFalse(result.valid(), "Validation should fail for malformed YAML");
      assertTrue(result.errors().stream()
          .anyMatch(e -> e.contains("YAML") || e.contains("parse")),
          "Error message should mention YAML parsing error");
    }

    @Test
    @DisplayName("Should fail when required field 'project.name' is missing")
    void shouldFailWhenProjectNameMissing() throws IOException {
      // Given
      Path projectPath = tempDir.resolve("test-project");
      Files.createDirectories(projectPath);
      Path configFile = projectPath.resolve(".cleanarch.yml");

      String yamlWithoutName = """
          project:
            basePackage: com.example.test
          architecture:
            type: hexagonal-single
            paradigm: reactive
            framework: spring
          """.stripIndent();
      Files.writeString(configFile, yamlWithoutName);

      // When
      ValidationResult result = configurationValidator.validateProjectConfig(projectPath);

      // Then
      assertFalse(result.valid(), "Validation should fail when project name is missing");
      assertTrue(result.errors().stream()
          .anyMatch(e -> e.contains("Missing") && e.contains("project.name")),
          "Error message should mention missing project.name field. Actual errors: " + result.errors());
    }

    @Test
    @DisplayName("Should fail when architecture type is invalid")
    void shouldFailWhenArchitectureTypeInvalid() throws IOException {
      // Given
      Path projectPath = tempDir.resolve("test-project");
      Files.createDirectories(projectPath);
      Path configFile = projectPath.resolve(".cleanarch.yml");

      String yamlWithInvalidArchitecture = """
          project:
            name: test-project
            basePackage: com.example.test
          architecture:
            type: invalid-architecture
          """;
      Files.writeString(configFile, yamlWithInvalidArchitecture);

      // When
      ValidationResult result = configurationValidator.validateProjectConfig(projectPath);

      // Then
      assertFalse(result.valid(), "Validation should fail for invalid architecture type");
      assertTrue(result.errors().stream()
          .anyMatch(e -> e.contains("parse") || e.contains("Failed")),
          "Error message should mention parsing failure for invalid architecture type");
    }

    @Test
    @DisplayName("Should fail when basePackage has invalid format")
    void shouldFailWhenBasePackageInvalid() throws IOException {
      // Given
      Path projectPath = tempDir.resolve("test-project");
      Files.createDirectories(projectPath);
      Path configFile = projectPath.resolve(".cleanarch.yml");

      String yamlWithInvalidPackage = """
          project:
            name: test-project
            basePackage: Com.Example.Test
          architecture:
            type: hexagonal-single
            paradigm: reactive
            framework: spring
          """.stripIndent();
      Files.writeString(configFile, yamlWithInvalidPackage);

      // When
      ValidationResult result = configurationValidator.validateProjectConfig(projectPath);

      // Then
      assertFalse(result.valid(), "Validation should fail for invalid package name");
      assertTrue(result.errors().stream()
          .anyMatch(e -> e.contains("Invalid package name") || e.contains("naming conventions")),
          "Error message should mention invalid package name or naming conventions. Actual errors: " + result.errors());
    }
  }

  @Nested
  @DisplayName("Template Configuration Error Scenarios")
  class TemplateConfigurationErrorScenarios {

    @Test
    @DisplayName("Should fail when localPath does not exist")
    void shouldFailWhenLocalPathDoesNotExist() throws IOException {
      // Given
      Path projectPath = tempDir.resolve("test-project");
      Files.createDirectories(projectPath);
      Path configFile = projectPath.resolve(".cleanarch.yml");

      String yamlWithInvalidLocalPath = """
          project:
            name: test-project
            basePackage: com.example.test
          architecture:
            type: hexagonal-single
          templates:
            localPath: /nonexistent/path/to/templates
          """;
      Files.writeString(configFile, yamlWithInvalidLocalPath);

      // Load the template configuration
      TemplateConfig templateConfig = configurationPort.readTemplateConfiguration(projectPath);

      // When
      ValidationResult result = configurationValidator.validateTemplateConfig(templateConfig);

      // Then
      assertFalse(result.valid(), "Validation should fail when localPath does not exist");
      assertTrue(result.errors().stream()
          .anyMatch(e -> e.contains("Local template path") && e.contains("does not exist")),
          "Error message should mention local template path does not exist");
    }

    @Test
    @DisplayName("Should fail when remote repository URL is invalid")
    void shouldFailWhenRemoteRepositoryUrlInvalid() throws IOException {
      // Given
      Path projectPath = tempDir.resolve("test-project");
      Files.createDirectories(projectPath);
      Path configFile = projectPath.resolve(".cleanarch.yml");

      String yamlWithInvalidRepoUrl = """
          project:
            name: test-project
            basePackage: com.example.test
          architecture:
            type: hexagonal-single
          templates:
            repository: not-a-valid-url
            branch: main
          """;
      Files.writeString(configFile, yamlWithInvalidRepoUrl);

      // Load the template configuration
      TemplateConfig templateConfig = configurationPort.readTemplateConfiguration(projectPath);

      // When
      ValidationResult result = configurationValidator.validateTemplateConfig(templateConfig);

      // Then
      assertFalse(result.valid(), "Validation should fail for invalid repository URL");
      assertTrue(result.errors().stream()
          .anyMatch(e -> e.contains("Invalid repository URL") || e.contains("valid HTTPS")),
          "Error message should mention invalid repository URL");
    }

    @Test
    @DisplayName("Should fail when branch name contains invalid characters")
    void shouldFailWhenBranchNameInvalid() throws IOException {
      // Given
      Path projectPath = tempDir.resolve("test-project");
      Files.createDirectories(projectPath);
      Path configFile = projectPath.resolve(".cleanarch.yml");

      String yamlWithInvalidBranch = """
          project:
            name: test-project
            basePackage: com.example.test
          architecture:
            type: hexagonal-single
          templates:
            repository: https://github.com/somospragma/backend-architecture-design-archetype-generator-templates
            branch: invalid@branch#name
          """;
      Files.writeString(configFile, yamlWithInvalidBranch);

      // Load the template configuration
      TemplateConfig templateConfig = configurationPort.readTemplateConfiguration(projectPath);

      // When
      ValidationResult result = configurationValidator.validateTemplateConfig(templateConfig);

      // Then
      assertFalse(result.valid(), "Validation should fail for invalid branch name");
      assertTrue(result.errors().stream()
          .anyMatch(e -> e.contains("branch") && e.contains("invalid")),
          "Error message should mention invalid branch name");
    }
  }

  @Nested
  @DisplayName("Edge Cases and Error Message Quality")
  class EdgeCasesAndErrorMessageQuality {

    @Test
    @DisplayName("Should handle empty configuration file gracefully")
    void shouldHandleEmptyConfigurationFileGracefully() throws IOException {
      // Given
      Path projectPath = tempDir.resolve("test-project");
      Files.createDirectories(projectPath);
      Path configFile = projectPath.resolve(".cleanarch.yml");
      Files.writeString(configFile, "");

      // When
      ValidationResult result = configurationValidator.validateProjectConfig(projectPath);

      // Then
      assertFalse(result.valid(), "Validation should fail for empty config file");
      assertFalse(result.errors().isEmpty(), "Should report errors for empty config");
    }

    @Test
    @DisplayName("Should handle basePackage with consecutive dots")
    void shouldHandleBasePackageWithConsecutiveDots() throws IOException {
      // Given
      Path projectPath = tempDir.resolve("test-project");
      Files.createDirectories(projectPath);
      Path configFile = projectPath.resolve(".cleanarch.yml");

      String yamlWithConsecutiveDots = """
          project:
            name: test-project
            basePackage: com..example..test
          architecture:
            type: hexagonal-single
            paradigm: reactive
            framework: spring
          """.stripIndent();
      Files.writeString(configFile, yamlWithConsecutiveDots);

      // When
      ValidationResult result = configurationValidator.validateProjectConfig(projectPath);

      // Then
      assertFalse(result.valid(), "Validation should fail for invalid package format");
      assertTrue(result.errors().stream()
          .anyMatch(e -> e.contains("Invalid package name") || e.contains("naming conventions")),
          "Error message should mention invalid package name format. Actual errors: " + result.errors());
    }

    @Test
    @DisplayName("Should provide actionable error messages")
    void shouldProvideActionableErrorMessages() throws IOException {
      // Given
      Path projectPath = tempDir.resolve("test-project");
      Files.createDirectories(projectPath);
      Path configFile = projectPath.resolve(".cleanarch.yml");

      String invalidYaml = """
          project:
            name: test-project
          """;
      Files.writeString(configFile, invalidYaml);

      // When
      ValidationResult result = configurationValidator.validateProjectConfig(projectPath);

      // Then
      assertFalse(result.valid(), "Validation should fail");

      // Verify error messages are actionable
      for (String error : result.errors()) {
        assertFalse(error.isEmpty(), "Error message should not be empty");
        assertTrue(error.length() > 10, "Error message should be descriptive");
      }
    }

    @Test
    @DisplayName("Should not create files when validation fails")
    void shouldNotCreateFilesWhenValidationFails() throws IOException {
      // Given
      Path projectPath = tempDir.resolve("test-project");
      Files.createDirectories(projectPath);
      Path configFile = projectPath.resolve(".cleanarch.yml");

      String invalidYaml = """
          project:
            name: test-project
          """;
      Files.writeString(configFile, invalidYaml);

      // When
      ValidationResult result = configurationValidator.validateProjectConfig(projectPath);

      // Then
      assertFalse(result.valid(), "Validation should fail");

      // Verify no additional files were created
      long fileCount = Files.list(projectPath).count();
      assertTrue(fileCount <= 1, "Should not create additional files when validation fails");
    }
  }

  @TempDir
  Path tempDir;

  private FileSystemPort fileSystemPort;

  private ConfigurationPort configurationPort;

  private ConfigurationValidator configurationValidator;

  @BeforeEach
  void setUp() {
    fileSystemPort = new LocalFileSystemAdapter();
    configurationPort = new YamlConfigurationAdapter();
    configurationValidator = new ConfigurationValidator(fileSystemPort, configurationPort);
  }
}
