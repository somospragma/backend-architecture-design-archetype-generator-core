package com.pragma.archetype.domain.service;

import com.pragma.archetype.domain.model.*;
import com.pragma.archetype.domain.model.config.ProjectConfig;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectValidator Tests")
class ProjectValidatorTest {

  @Mock
  private FileSystemPort fileSystemPort;

  @Mock
  private ConfigurationPort configurationPort;

  private ProjectValidator validator;
  private Path projectPath;
  private ProjectConfig validConfig;

  @BeforeEach
  void setUp() {
    validator = new ProjectValidator(fileSystemPort, configurationPort);
    projectPath = Paths.get("/test/project");

    validConfig = ProjectConfig.builder()
        .name("payment-service")
        .basePackage("com.company.payment")
        .architecture(ArchitectureType.HEXAGONAL_SINGLE)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .build();
  }

  @Test
  @DisplayName("Should validate successfully when project is empty and config is valid")
  void shouldValidateSuccessfullyWhenProjectIsEmptyAndConfigIsValid() {
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    when(configurationPort.configurationExists(projectPath)).thenReturn(false);
    when(fileSystemPort.listFiles(projectPath)).thenReturn(List.of(
        Paths.get("/test/project/build.gradle.kts")));

    ValidationResult result = validator.validateForInitialization(projectPath, validConfig);

    assertTrue(result.valid());
    assertTrue(result.errors().isEmpty());
  }

  @Test
  @DisplayName("Should fail when project directory does not exist")
  void shouldFailWhenProjectDirectoryDoesNotExist() {
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(false);

    ValidationResult result = validator.validateForInitialization(projectPath, validConfig);

    assertFalse(result.valid());
    assertTrue(result.errors().stream()
        .anyMatch(error -> error.contains("does not exist")));
  }

  @Test
  @DisplayName("Should fail when project is already initialized")
  void shouldFailWhenProjectIsAlreadyInitialized() {
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    when(configurationPort.configurationExists(projectPath)).thenReturn(true);

    ValidationResult result = validator.validateForInitialization(projectPath, validConfig);

    assertFalse(result.valid());
    assertTrue(result.errors().stream()
        .anyMatch(error -> error.contains("already initialized")));
  }

  @Test
  @DisplayName("Should fail when project directory is not empty")
  void shouldFailWhenProjectDirectoryIsNotEmpty() {
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    when(configurationPort.configurationExists(projectPath)).thenReturn(false);
    when(fileSystemPort.listFiles(projectPath)).thenReturn(List.of(
        Paths.get("/test/project/build.gradle.kts"),
        Paths.get("/test/project/src"),
        Paths.get("/test/project/README.md")));

    ValidationResult result = validator.validateForInitialization(projectPath, validConfig);

    assertFalse(result.valid());
    assertTrue(result.errors().stream()
        .anyMatch(error -> error.contains("not empty")));
  }

  @Test
  @DisplayName("Should allow Gradle files in empty project")
  void shouldAllowGradleFilesInEmptyProject() {
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    when(configurationPort.configurationExists(projectPath)).thenReturn(false);
    when(fileSystemPort.listFiles(projectPath)).thenReturn(List.of(
        Paths.get("/test/project/build.gradle.kts"),
        Paths.get("/test/project/settings.gradle.kts"),
        Paths.get("/test/project/gradlew"),
        Paths.get("/test/project/gradlew.bat"),
        Paths.get("/test/project/.gitignore"),
        Paths.get("/test/project/.git"),
        Paths.get("/test/project/gradle"),
        Paths.get("/test/project/.gradle")));

    ValidationResult result = validator.validateForInitialization(projectPath, validConfig);

    assertTrue(result.valid());
  }

  @Test
  @DisplayName("Should fail when project name is invalid")
  void shouldFailWhenProjectNameIsInvalid() {
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    when(configurationPort.configurationExists(projectPath)).thenReturn(false);
    when(fileSystemPort.listFiles(projectPath)).thenReturn(List.of());

    ProjectConfig invalidConfig = ProjectConfig.builder()
        .name("Payment_Service")
        .basePackage("com.company.payment")
        .architecture(ArchitectureType.HEXAGONAL_SINGLE)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .build();

    ValidationResult result = validator.validateForInitialization(projectPath, invalidConfig);

    assertFalse(result.valid());
    assertTrue(result.errors().stream()
        .anyMatch(error -> error.contains("Invalid project name")));
  }

  @Test
  @DisplayName("Should validate component generation when project is initialized")
  void shouldValidateComponentGenerationWhenProjectIsInitialized() {
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    when(configurationPort.configurationExists(projectPath)).thenReturn(true);

    ValidationResult result = validator.validateForComponentGeneration(projectPath);

    assertTrue(result.valid());
  }

  @Test
  @DisplayName("Should fail component generation when project is not initialized")
  void shouldFailComponentGenerationWhenProjectIsNotInitialized() {
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    when(configurationPort.configurationExists(projectPath)).thenReturn(false);

    ValidationResult result = validator.validateForComponentGeneration(projectPath);

    assertFalse(result.valid());
    assertTrue(result.errors().stream()
        .anyMatch(error -> error.contains("not initialized")));
  }
}
