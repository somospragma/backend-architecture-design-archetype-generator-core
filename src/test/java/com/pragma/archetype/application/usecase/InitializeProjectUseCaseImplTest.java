package com.pragma.archetype.application.usecase;

import com.pragma.archetype.application.generator.ProjectGenerator;
import com.pragma.archetype.application.usecase.InitializeProjectUseCaseImpl.InitializationResult;
import com.pragma.archetype.domain.model.*;
import com.pragma.archetype.domain.model.config.ProjectConfig;
import com.pragma.archetype.domain.model.project.ArchitectureType;
import com.pragma.archetype.domain.model.project.Framework;
import com.pragma.archetype.domain.model.project.Paradigm;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.service.ProjectValidator;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InitializeProjectUseCase Tests")
class InitializeProjectUseCaseImplTest {

  @Mock
  private ProjectValidator projectValidator;

  @Mock
  private ProjectGenerator projectGenerator;

  @Mock
  private ConfigurationPort configurationPort;

  private InitializeProjectUseCaseImpl useCase;
  private Path projectPath;
  private ProjectConfig config;

  @BeforeEach
  void setUp() {
    useCase = new InitializeProjectUseCaseImpl(
        projectValidator,
        projectGenerator,
        configurationPort);

    projectPath = Paths.get("/test/project");

    config = ProjectConfig.builder()
        .name("payment-service")
        .basePackage("com.company.payment")
        .architecture(ArchitectureType.HEXAGONAL_SINGLE)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .build();
  }

  @Test
  @DisplayName("Should initialize project successfully")
  void shouldInitializeProjectSuccessfully() {
    // Given
    when(projectValidator.validateForInitialization(projectPath, config))
        .thenReturn(ValidationResult.success());

    List<GeneratedFile> expectedFiles = List.of(
        GeneratedFile.create(Paths.get("/test/project/build.gradle.kts"), "content"));
    when(projectGenerator.generateProject(projectPath, config))
        .thenReturn(expectedFiles);

    // When
    InitializationResult result = useCase.execute(projectPath, config);

    // Then
    assertTrue(result.isSuccess());
    assertFalse(result.isFailure());
    assertEquals(expectedFiles, result.generatedFiles());
    assertTrue(result.errors().isEmpty());

    // Verify interactions
    verify(projectValidator).validateForInitialization(projectPath, config);
    verify(projectGenerator).generateProject(projectPath, config);
    verify(configurationPort).writeConfiguration(projectPath, config);
  }

  @Test
  @DisplayName("Should fail when validation fails")
  void shouldFailWhenValidationFails() {
    // Given
    List<String> validationErrors = List.of("Project is not empty", "Invalid package name");
    when(projectValidator.validateForInitialization(projectPath, config))
        .thenReturn(ValidationResult.failure(validationErrors));

    // When
    InitializationResult result = useCase.execute(projectPath, config);

    // Then
    assertTrue(result.isFailure());
    assertFalse(result.isSuccess());
    assertEquals(validationErrors, result.errors());
    assertTrue(result.generatedFiles().isEmpty());

    // Verify generator was not called
    verify(projectValidator).validateForInitialization(projectPath, config);
    verify(projectGenerator, never()).generateProject(any(), any());
    verify(configurationPort, never()).writeConfiguration(any(), any());
  }

  @Test
  @DisplayName("Should handle generation errors gracefully")
  void shouldHandleGenerationErrorsGracefully() {
    // Given
    when(projectValidator.validateForInitialization(projectPath, config))
        .thenReturn(ValidationResult.success());

    when(projectGenerator.generateProject(projectPath, config))
        .thenThrow(new RuntimeException("Template not found"));

    // When
    InitializationResult result = useCase.execute(projectPath, config);

    // Then
    assertTrue(result.isFailure());
    assertFalse(result.errors().isEmpty());
    assertTrue(result.errors().get(0).contains("Failed to initialize project"));

    // Verify configuration was not saved
    verify(configurationPort, never()).writeConfiguration(any(), any());
  }

  @Test
  @DisplayName("Should save configuration after successful generation")
  void shouldSaveConfigurationAfterSuccessfulGeneration() {
    // Given
    when(projectValidator.validateForInitialization(projectPath, config))
        .thenReturn(ValidationResult.success());

    when(projectGenerator.generateProject(projectPath, config))
        .thenReturn(List.of());

    // When
    useCase.execute(projectPath, config);

    // Then
    verify(configurationPort).writeConfiguration(projectPath, config);
  }

  @Test
  @DisplayName("InitializationResult should have correct success state")
  void initializationResultShouldHaveCorrectSuccessState() {
    // Given
    List<GeneratedFile> files = List.of(
        GeneratedFile.create(Paths.get("/test/file.txt"), "content"));

    // When
    InitializationResult successResult = InitializationResult.success(files);

    // Then
    assertTrue(successResult.isSuccess());
    assertFalse(successResult.isFailure());
    assertEquals(files, successResult.generatedFiles());
    assertTrue(successResult.errors().isEmpty());
  }

  @Test
  @DisplayName("InitializationResult should have correct failure state")
  void initializationResultShouldHaveCorrectFailureState() {
    // Given
    List<String> errors = List.of("Error 1", "Error 2");

    // When
    InitializationResult failureResult = InitializationResult.failure(errors);

    // Then
    assertTrue(failureResult.isFailure());
    assertFalse(failureResult.isSuccess());
    assertEquals(errors, failureResult.errors());
    assertTrue(failureResult.generatedFiles().isEmpty());
  }
}
