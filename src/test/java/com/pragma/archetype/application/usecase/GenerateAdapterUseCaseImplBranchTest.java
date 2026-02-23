package com.pragma.archetype.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pragma.archetype.application.generator.AdapterGenerator;
import com.pragma.archetype.domain.model.AdapterConfig;
import com.pragma.archetype.domain.model.ArchitectureType;
import com.pragma.archetype.domain.model.Framework;
import com.pragma.archetype.domain.model.Paradigm;
import com.pragma.archetype.domain.model.ProjectConfig;
import com.pragma.archetype.domain.model.ValidationResult;
import com.pragma.archetype.domain.port.in.GenerateInputAdapterUseCase.GenerationResult;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import com.pragma.archetype.domain.service.AdapterValidator;

@ExtendWith(MockitoExtension.class)
class GenerateAdapterUseCaseImplBranchTest {

  @Mock
  private AdapterGenerator adapterGenerator;
  @Mock
  private AdapterValidator adapterValidator;
  @Mock
  private ConfigurationPort configurationPort;
  @Mock
  private FileSystemPort fileSystemPort;

  private GenerateAdapterUseCaseImpl useCase;
  private Path projectPath;
  private AdapterConfig adapterConfig;
  private ProjectConfig projectConfig;

  @BeforeEach
  void setUp() {
    useCase = new GenerateAdapterUseCaseImpl(
        adapterGenerator, adapterValidator, configurationPort, fileSystemPort);
    projectPath = Paths.get("/test/project");

    projectConfig = ProjectConfig.builder()
        .name("test-project")
        .basePackage("com.test")
        .architecture(ArchitectureType.HEXAGONAL_SINGLE)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .build();

    adapterConfig = AdapterConfig.builder()
        .name("TestAdapter")
        .entity("TestEntity")
        .type("mongodb")
        .packageName("com.test.adapter")
        .build();
  }

  @Test
  void shouldHandleValidationFailure() {
    ValidationResult validationResult = ValidationResult.failure("Validation failed", "Invalid adapter type");
    when(configurationPort.readConfiguration(projectPath)).thenReturn(Optional.of(projectConfig));
    when(adapterValidator.validate(adapterConfig, projectConfig)).thenReturn(validationResult);

    GenerationResult result = useCase.execute(projectPath, adapterConfig);

    assertFalse(result.success());
    assertTrue(result.errors().contains("Validation failed"));
    verify(adapterGenerator, never()).generate(any(), any(), any());
  }

  @Test
  void shouldHandleConfigurationNotFound() {
    when(configurationPort.readConfiguration(projectPath)).thenReturn(Optional.empty());

    GenerationResult result = useCase.execute(projectPath, adapterConfig);

    assertFalse(result.success());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("Configuration not found")));
    verify(adapterValidator, never()).validate(any(), any());
  }

  @Test
  void shouldHandleGenerationFailure() {
    ValidationResult validationResult = ValidationResult.success();
    GenerationResult generationResult = GenerationResult.failure("Generation failed");

    when(configurationPort.readConfiguration(projectPath)).thenReturn(Optional.of(projectConfig));
    when(adapterValidator.validate(adapterConfig, projectConfig)).thenReturn(validationResult);
    when(adapterGenerator.generate(projectPath, adapterConfig, projectConfig))
        .thenReturn(generationResult);

    GenerationResult result = useCase.execute(projectPath, adapterConfig);

    assertFalse(result.success());
    assertTrue(result.errors().contains("Generation failed"));
  }

  @Test
  void shouldHandleBackupCreationFailure() {
    ValidationResult validationResult = ValidationResult.success();
    when(configurationPort.readConfiguration(projectPath)).thenReturn(Optional.of(projectConfig));
    when(adapterValidator.validate(adapterConfig, projectConfig)).thenReturn(validationResult);
    when(fileSystemPort.exists(any())).thenReturn(true);
    when(fileSystemPort.createBackup(any())).thenReturn(false);

    GenerationResult result = useCase.execute(projectPath, adapterConfig);

    assertFalse(result.success());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("backup")));
  }

  @Test
  void shouldHandleRollbackOnGenerationFailure() {
    ValidationResult validationResult = ValidationResult.success();
    GenerationResult generationResult = GenerationResult.failure("Generation error");

    when(configurationPort.readConfiguration(projectPath)).thenReturn(Optional.of(projectConfig));
    when(adapterValidator.validate(adapterConfig, projectConfig)).thenReturn(validationResult);
    when(fileSystemPort.exists(any())).thenReturn(true);
    when(fileSystemPort.createBackup(any())).thenReturn(true);
    when(adapterGenerator.generate(projectPath, adapterConfig, projectConfig))
        .thenReturn(generationResult);

    GenerationResult result = useCase.execute(projectPath, adapterConfig);

    assertFalse(result.success());
    verify(fileSystemPort).restoreBackup(any());
  }

  @Test
  void shouldHandleSuccessfulGenerationWithBackup() {
    ValidationResult validationResult = ValidationResult.success();
    GenerationResult generationResult = GenerationResult.success();

    when(configurationPort.readConfiguration(projectPath)).thenReturn(Optional.of(projectConfig));
    when(adapterValidator.validate(adapterConfig, projectConfig)).thenReturn(validationResult);
    when(fileSystemPort.exists(any())).thenReturn(true);
    when(fileSystemPort.createBackup(any())).thenReturn(true);
    when(adapterGenerator.generate(projectPath, adapterConfig, projectConfig))
        .thenReturn(generationResult);

    GenerationResult result = useCase.execute(projectPath, adapterConfig);

    assertTrue(result.success());
    verify(fileSystemPort).deleteBackup(any());
  }

  @Test
  void shouldHandleSuccessfulGenerationWithoutBackup() {
    ValidationResult validationResult = ValidationResult.success();
    GenerationResult generationResult = GenerationResult.success();

    when(configurationPort.readConfiguration(projectPath)).thenReturn(Optional.of(projectConfig));
    when(adapterValidator.validate(adapterConfig, projectConfig)).thenReturn(validationResult);
    when(fileSystemPort.exists(any())).thenReturn(false);
    when(adapterGenerator.generate(projectPath, adapterConfig, projectConfig))
        .thenReturn(generationResult);

    GenerationResult result = useCase.execute(projectPath, adapterConfig);

    assertTrue(result.success());
    verify(fileSystemPort, never()).createBackup(any());
    verify(fileSystemPort, never()).deleteBackup(any());
  }

  @Test
  void shouldHandleNullProjectPath() {
    assertThrows(NullPointerException.class, () -> useCase.execute(null, adapterConfig));
  }

  @Test
  void shouldHandleNullAdapterConfig() {
    assertThrows(NullPointerException.class, () -> useCase.execute(projectPath, null));
  }

  @Test
  void shouldHandleValidationWithWarnings() {
    ValidationResult validationResult = ValidationResult.builder()
        .valid(true)
        .warning("Deprecated adapter type")
        .warning("Consider using newer version")
        .build();
    GenerationResult generationResult = GenerationResult.success();

    when(configurationPort.readConfiguration(projectPath)).thenReturn(Optional.of(projectConfig));
    when(adapterValidator.validate(adapterConfig, projectConfig)).thenReturn(validationResult);
    when(fileSystemPort.exists(any())).thenReturn(false);
    when(adapterGenerator.generate(projectPath, adapterConfig, projectConfig))
        .thenReturn(generationResult);

    GenerationResult result = useCase.execute(projectPath, adapterConfig);

    assertTrue(result.success());
    assertEquals(2, validationResult.warnings().size());
  }

  @Test
  void shouldHandleMultipleValidationErrors() {
    ValidationResult validationResult = ValidationResult.builder()
        .valid(false)
        .error("Invalid adapter name")
        .error("Invalid package name")
        .error("Invalid entity name")
        .build();

    when(configurationPort.readConfiguration(projectPath)).thenReturn(Optional.of(projectConfig));
    when(adapterValidator.validate(adapterConfig, projectConfig)).thenReturn(validationResult);

    GenerationResult result = useCase.execute(projectPath, adapterConfig);

    assertFalse(result.success());
    assertTrue(result.errors().size() >= 3);
  }

  @Test
  void shouldHandleBackupDeletionFailure() {
    ValidationResult validationResult = ValidationResult.success();
    GenerationResult generationResult = GenerationResult.success();

    when(configurationPort.readConfiguration(projectPath)).thenReturn(Optional.of(projectConfig));
    when(adapterValidator.validate(adapterConfig, projectConfig)).thenReturn(validationResult);
    when(fileSystemPort.exists(any())).thenReturn(true);
    when(fileSystemPort.createBackup(any())).thenReturn(true);
    when(adapterGenerator.generate(projectPath, adapterConfig, projectConfig))
        .thenReturn(generationResult);
    when(fileSystemPort.deleteBackup(any())).thenReturn(false);

    GenerationResult result = useCase.execute(projectPath, adapterConfig);

    assertTrue(result.success());
    verify(fileSystemPort).deleteBackup(any());
  }

  @Test
  void shouldHandleRollbackFailure() {
    ValidationResult validationResult = ValidationResult.success();
    GenerationResult generationResult = GenerationResult.failure("Generation error");

    when(configurationPort.readConfiguration(projectPath)).thenReturn(Optional.of(projectConfig));
    when(adapterValidator.validate(adapterConfig, projectConfig)).thenReturn(validationResult);
    when(fileSystemPort.exists(any())).thenReturn(true);
    when(fileSystemPort.createBackup(any())).thenReturn(true);
    when(adapterGenerator.generate(projectPath, adapterConfig, projectConfig))
        .thenReturn(generationResult);
    when(fileSystemPort.restoreBackup(any())).thenReturn(false);

    GenerationResult result = useCase.execute(projectPath, adapterConfig);

    assertFalse(result.success());
    verify(fileSystemPort).restoreBackup(any());
  }

  @Test
  void shouldHandleDifferentArchitectureTypes() {
    for (ArchitectureType architecture : ArchitectureType.values()) {
      ProjectConfig config = ProjectConfig.builder()
          .name("test")
          .basePackage("com.test")
          .architecture(architecture)
          .paradigm(Paradigm.REACTIVE)
          .framework(Framework.SPRING)
          .build();

      ValidationResult validationResult = ValidationResult.success();
      GenerationResult generationResult = GenerationResult.success();

      when(configurationPort.readConfiguration(projectPath)).thenReturn(Optional.of(config));
      when(adapterValidator.validate(adapterConfig, config)).thenReturn(validationResult);
      when(fileSystemPort.exists(any())).thenReturn(false);
      when(adapterGenerator.generate(projectPath, adapterConfig, config))
          .thenReturn(generationResult);

      GenerationResult result = useCase.execute(projectPath, adapterConfig);

      assertTrue(result.success(), "Failed for architecture: " + architecture);
    }
  }

  @Test
  void shouldHandleDifferentParadigms() {
    for (Paradigm paradigm : Paradigm.values()) {
      ProjectConfig config = ProjectConfig.builder()
          .name("test")
          .basePackage("com.test")
          .architecture(ArchitectureType.HEXAGONAL_SINGLE)
          .paradigm(paradigm)
          .framework(Framework.SPRING)
          .build();

      ValidationResult validationResult = ValidationResult.success();
      GenerationResult generationResult = GenerationResult.success();

      when(configurationPort.readConfiguration(projectPath)).thenReturn(Optional.of(config));
      when(adapterValidator.validate(adapterConfig, config)).thenReturn(validationResult);
      when(fileSystemPort.exists(any())).thenReturn(false);
      when(adapterGenerator.generate(projectPath, adapterConfig, config))
          .thenReturn(generationResult);

      GenerationResult result = useCase.execute(projectPath, adapterConfig);

      assertTrue(result.success(), "Failed for paradigm: " + paradigm);
    }
  }
}
