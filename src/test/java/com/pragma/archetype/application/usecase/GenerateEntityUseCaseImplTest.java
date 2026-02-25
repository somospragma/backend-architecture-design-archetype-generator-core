package com.pragma.archetype.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pragma.archetype.application.generator.EntityGenerator;
import com.pragma.archetype.domain.model.config.ProjectConfig;
import com.pragma.archetype.domain.model.entity.EntityConfig;
import com.pragma.archetype.domain.model.entity.EntityField;
import com.pragma.archetype.domain.model.file.GeneratedFile;
import com.pragma.archetype.domain.model.project.ArchitectureType;
import com.pragma.archetype.domain.model.project.Framework;
import com.pragma.archetype.domain.model.project.Paradigm;
import com.pragma.archetype.domain.model.validation.ValidationResult;
import com.pragma.archetype.domain.port.in.GenerateEntityUseCase.GenerationResult;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import com.pragma.archetype.domain.service.EntityValidator;

@ExtendWith(MockitoExtension.class)
class GenerateEntityUseCaseImplTest {

  @Mock
  private EntityValidator validator;

  @Mock
  private EntityGenerator generator;

  @Mock
  private ConfigurationPort configurationPort;

  @Mock
  private FileSystemPort fileSystemPort;

  private GenerateEntityUseCaseImpl useCase;

  @BeforeEach
  void setUp() {
    useCase = new GenerateEntityUseCaseImpl(validator, generator, configurationPort, fileSystemPort);
  }

  @Test
  void shouldGenerateEntitySuccessfully() {
    // Given
    EntityConfig config = EntityConfig.builder()
        .name("User")
        .packageName("com.test.domain.model")
        .hasId(true)
        .idType("String")
        .fields(List.of(
            new EntityField("name", "String", false)))
        .build();

    ProjectConfig projectConfig = ProjectConfig.builder()
        .name("test-project")
        .basePackage("com.test")
        .architecture(ArchitectureType.HEXAGONAL_SINGLE)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .pluginVersion("1.0.0")
        .build();

    GeneratedFile file = GeneratedFile.javaSource(
        Path.of("src/main/java/com/test/domain/model/User.java"),
        "public class User {}");

    when(validator.validate(any(), any())).thenReturn(ValidationResult.success());
    when(configurationPort.readConfiguration(any())).thenReturn(Optional.of(projectConfig));
    when(generator.generateEntity(any(), any(), any())).thenReturn(List.of(file));

    // When
    GenerationResult result = useCase.execute(Path.of("/test"), config);

    // Then
    assertTrue(result.success());
    assertEquals(1, result.generatedFiles().size());
    verify(fileSystemPort).writeFiles(anyList());
  }

  @Test
  void shouldFailWhenValidationFails() {
    // Given
    EntityConfig config = EntityConfig.builder()
        .name("User")
        .packageName("com.test.domain.model")
        .hasId(true)
        .idType("String")
        .fields(List.of())
        .build();

    when(validator.validate(any(), any()))
        .thenReturn(ValidationResult.failure(List.of("Validation error")));

    // When
    GenerationResult result = useCase.execute(Path.of("/test"), config);

    // Then
    assertFalse(result.success());
    assertTrue(result.errors().contains("Validation error"));
    verify(generator, never()).generateEntity(any(), any(), any());
  }

  @Test
  void shouldFailWhenProjectConfigNotFound() {
    // Given
    EntityConfig config = EntityConfig.builder()
        .name("User")
        .packageName("com.test.domain.model")
        .hasId(true)
        .idType("String")
        .fields(List.of(
            new EntityField("name", "String", false)))
        .build();

    when(validator.validate(any(), any())).thenReturn(ValidationResult.success());
    when(configurationPort.readConfiguration(any())).thenReturn(Optional.empty());

    // When
    GenerationResult result = useCase.execute(Path.of("/test"), config);

    // Then
    assertFalse(result.success());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("configuration not found")));
  }

  @Test
  void shouldHandleGeneratorException() {
    // Given
    EntityConfig config = EntityConfig.builder()
        .name("User")
        .packageName("com.test.domain.model")
        .hasId(true)
        .idType("String")
        .fields(List.of(
            new EntityField("name", "String", false)))
        .build();

    ProjectConfig projectConfig = ProjectConfig.builder()
        .name("test-project")
        .basePackage("com.test")
        .architecture(ArchitectureType.HEXAGONAL_SINGLE)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .pluginVersion("1.0.0")
        .build();

    when(validator.validate(any(), any())).thenReturn(ValidationResult.success());
    when(configurationPort.readConfiguration(any())).thenReturn(Optional.of(projectConfig));
    when(generator.generateEntity(any(), any(), any()))
        .thenThrow(new RuntimeException("Generator error"));

    // When
    GenerationResult result = useCase.execute(Path.of("/test"), config);

    // Then
    assertFalse(result.success());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("Failed to generate entity")));
  }
}
