package com.pragma.archetype.application.generator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
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

import com.pragma.archetype.domain.model.AdapterConfig;
import com.pragma.archetype.domain.model.ArchitectureType;
import com.pragma.archetype.domain.model.Framework;
import com.pragma.archetype.domain.model.Paradigm;
import com.pragma.archetype.domain.model.ProjectConfig;
import com.pragma.archetype.domain.port.in.GenerateInputAdapterUseCase.GenerationResult;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import com.pragma.archetype.domain.port.out.TemplatePort;

@ExtendWith(MockitoExtension.class)
class AdapterGeneratorBranchTest {

  @Mock
  private FileSystemPort fileSystemPort;
  @Mock
  private TemplatePort templatePort;

  private AdapterGenerator generator;
  private Path projectPath;
  private AdapterConfig adapterConfig;
  private ProjectConfig projectConfig;

  @BeforeEach
  void setUp() {
    generator = new AdapterGenerator(fileSystemPort, templatePort);
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
  void shouldHandleTemplateNotFound() {
    when(templatePort.loadTemplate(anyString(), any())).thenReturn(Optional.empty());

    GenerationResult result = generator.generate(projectPath, adapterConfig, projectConfig);

    assertFalse(result.success());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("Template not found")));
  }

  @Test
  void shouldHandleFileWriteFailure() {
    when(templatePort.loadTemplate(anyString(), any())).thenReturn(Optional.of("template content"));
    when(fileSystemPort.writeFile(any(), anyString())).thenReturn(false);

    GenerationResult result = generator.generate(projectPath, adapterConfig, projectConfig);

    assertFalse(result.success());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("Failed to write")));
  }

  @Test
  void shouldHandleDirectoryCreationFailure() {
    when(templatePort.loadTemplate(anyString(), any())).thenReturn(Optional.of("template content"));
    when(fileSystemPort.createDirectories(any())).thenReturn(false);

    GenerationResult result = generator.generate(projectPath, adapterConfig, projectConfig);

    assertFalse(result.success());
  }

  @Test
  void shouldGenerateAdapterForHexagonalSingle() {
    when(templatePort.loadTemplate(anyString(), any())).thenReturn(Optional.of("template content"));
    when(fileSystemPort.createDirectories(any())).thenReturn(true);
    when(fileSystemPort.writeFile(any(), anyString())).thenReturn(true);

    GenerationResult result = generator.generate(projectPath, adapterConfig, projectConfig);

    assertTrue(result.success());
    verify(fileSystemPort, atLeastOnce()).writeFile(any(), anyString());
  }

  @Test
  void shouldGenerateAdapterForHexagonalMulti() {
    ProjectConfig multiConfig = projectConfig.toBuilder().architecture(ArchitectureType.HEXAGONAL_MULTI).build();

    when(templatePort.loadTemplate(anyString(), any())).thenReturn(Optional.of("template content"));
    when(fileSystemPort.createDirectories(any())).thenReturn(true);
    when(fileSystemPort.writeFile(any(), anyString())).thenReturn(true);

    GenerationResult result = generator.generate(projectPath, adapterConfig, multiConfig);

    assertTrue(result.success());
  }

  @Test
  void shouldGenerateAdapterForHexagonalMultiGranular() {
    ProjectConfig granularConfig = projectConfig.toBuilder().architecture(ArchitectureType.HEXAGONAL_MULTI_GRANULAR)
        .build();

    when(templatePort.loadTemplate(anyString(), any())).thenReturn(Optional.of("template content"));
    when(fileSystemPort.createDirectories(any())).thenReturn(true);
    when(fileSystemPort.writeFile(any(), anyString())).thenReturn(true);

    GenerationResult result = generator.generate(projectPath, adapterConfig, granularConfig);

    assertTrue(result.success());
  }

  @Test
  void shouldGenerateAdapterForOnionSingle() {
    ProjectConfig onionConfig = projectConfig.toBuilder().architecture(ArchitectureType.ONION_SINGLE).build();

    when(templatePort.loadTemplate(anyString(), any())).thenReturn(Optional.of("template content"));
    when(fileSystemPort.createDirectories(any())).thenReturn(true);
    when(fileSystemPort.writeFile(any(), anyString())).thenReturn(true);

    GenerationResult result = generator.generate(projectPath, adapterConfig, onionConfig);

    assertTrue(result.success());
  }

  @Test
  void shouldHandleReactiveParadigm() {
    when(templatePort.loadTemplate(anyString(), any())).thenReturn(Optional.of("template content"));
    when(fileSystemPort.createDirectories(any())).thenReturn(true);
    when(fileSystemPort.writeFile(any(), anyString())).thenReturn(true);

    GenerationResult result = generator.generate(projectPath, adapterConfig, projectConfig);

    assertTrue(result.success());
  }

  @Test
  void shouldHandleImperativeParadigm() {
    ProjectConfig imperativeConfig = projectConfig.toBuilder().paradigm(Paradigm.IMPERATIVE).build();

    when(templatePort.loadTemplate(anyString(), any())).thenReturn(Optional.of("template content"));
    when(fileSystemPort.createDirectories(any())).thenReturn(true);
    when(fileSystemPort.writeFile(any(), anyString())).thenReturn(true);

    GenerationResult result = generator.generate(projectPath, adapterConfig, imperativeConfig);

    assertTrue(result.success());
  }

  @Test
  void shouldHandleDifferentAdapterTypes() {
    String[] adapterTypes = { "mongodb", "redis", "postgresql", "rest_client", "kafka" };

    for (String type : adapterTypes) {
      AdapterConfig config = adapterConfig.toBuilder().type(type).build();

      when(templatePort.loadTemplate(anyString(), any()))
          .thenReturn(Optional.of("template content"));
      when(fileSystemPort.createDirectories(any())).thenReturn(true);
      when(fileSystemPort.writeFile(any(), anyString())).thenReturn(true);

      GenerationResult result = generator.generate(projectPath, config, projectConfig);

      assertTrue(result.success(), "Failed for adapter type: " + type);
    }
  }

  @Test
  void shouldHandleEmptyPackageName() {
    AdapterConfig emptyPackageConfig = adapterConfig.toBuilder().packageName("").build();

    when(templatePort.loadTemplate(anyString(), any())).thenReturn(Optional.of("template content"));
    when(fileSystemPort.createDirectories(any())).thenReturn(true);
    when(fileSystemPort.writeFile(any(), anyString())).thenReturn(true);

    GenerationResult result = generator.generate(projectPath, emptyPackageConfig, projectConfig);

    assertFalse(result.success());
  }

  @Test
  void shouldHandleNullAdapterName() {
    AdapterConfig nullNameConfig = adapterConfig.toBuilder().name(null).build();

    assertThrows(
        NullPointerException.class,
        () -> generator.generate(projectPath, nullNameConfig, projectConfig));
  }

  @Test
  void shouldHandleNullEntityName() {
    AdapterConfig nullEntityConfig = adapterConfig.toBuilder().entity(null).build();

    assertThrows(
        NullPointerException.class,
        () -> generator.generate(projectPath, nullEntityConfig, projectConfig));
  }

  @Test
  void shouldHandleSpecialCharactersInNames() {
    AdapterConfig specialCharsConfig = adapterConfig.toBuilder().name("Test-Adapter").entity("Test_Entity").build();

    when(templatePort.loadTemplate(anyString(), any())).thenReturn(Optional.of("template content"));
    when(fileSystemPort.createDirectories(any())).thenReturn(true);
    when(fileSystemPort.writeFile(any(), anyString())).thenReturn(true);

    GenerationResult result = generator.generate(projectPath, specialCharsConfig, projectConfig);

    assertTrue(result.success());
  }

  @Test
  void shouldHandleLongPackageNames() {
    AdapterConfig longPackageConfig = adapterConfig
        .toBuilder()
        .packageName("com.test.very.long.package.name.for.adapter.infrastructure.driven")
        .build();

    when(templatePort.loadTemplate(anyString(), any())).thenReturn(Optional.of("template content"));
    when(fileSystemPort.createDirectories(any())).thenReturn(true);
    when(fileSystemPort.writeFile(any(), anyString())).thenReturn(true);

    GenerationResult result = generator.generate(projectPath, longPackageConfig, projectConfig);

    assertTrue(result.success());
  }

  @Test
  void shouldHandleMultipleFileGeneration() {
    when(templatePort.loadTemplate(anyString(), any())).thenReturn(Optional.of("template content"));
    when(fileSystemPort.createDirectories(any())).thenReturn(true);
    when(fileSystemPort.writeFile(any(), anyString())).thenReturn(true);

    GenerationResult result = generator.generate(projectPath, adapterConfig, projectConfig);

    assertTrue(result.success());
    verify(fileSystemPort, atLeast(3)).writeFile(any(), anyString());
  }

  @Test
  void shouldHandlePartialFileWriteFailure() {
    when(templatePort.loadTemplate(anyString(), any())).thenReturn(Optional.of("template content"));
    when(fileSystemPort.createDirectories(any())).thenReturn(true);
    when(fileSystemPort.writeFile(any(), anyString())).thenReturn(true, false);

    GenerationResult result = generator.generate(projectPath, adapterConfig, projectConfig);

    assertFalse(result.success());
  }

  @Test
  void shouldHandleTemplateProcessingError() {
    when(templatePort.loadTemplate(anyString(), any()))
        .thenThrow(new RuntimeException("Template processing error"));

    GenerationResult result = generator.generate(projectPath, adapterConfig, projectConfig);

    assertFalse(result.success());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("error")));
  }

  @Test
  void shouldGenerateMapperFile() {
    when(templatePort.loadTemplate(anyString(), any())).thenReturn(Optional.of("mapper template"));
    when(fileSystemPort.createDirectories(any())).thenReturn(true);
    when(fileSystemPort.writeFile(any(), anyString())).thenReturn(true);

    GenerationResult result = generator.generate(projectPath, adapterConfig, projectConfig);

    assertTrue(result.success());
    verify(fileSystemPort, atLeastOnce())
        .writeFile(argThat(path -> path.toString().contains("Mapper")), anyString());
  }

  @Test
  void shouldGenerateEntityFile() {
    when(templatePort.loadTemplate(anyString(), any())).thenReturn(Optional.of("entity template"));
    when(fileSystemPort.createDirectories(any())).thenReturn(true);
    when(fileSystemPort.writeFile(any(), anyString())).thenReturn(true);

    GenerationResult result = generator.generate(projectPath, adapterConfig, projectConfig);

    assertTrue(result.success());
    verify(fileSystemPort, atLeastOnce())
        .writeFile(argThat(path -> path.toString().contains("Entity")), anyString());
  }

  @Test
  void shouldGenerateConfigFile() {
    when(templatePort.loadTemplate(anyString(), any())).thenReturn(Optional.of("config template"));
    when(fileSystemPort.createDirectories(any())).thenReturn(true);
    when(fileSystemPort.writeFile(any(), anyString())).thenReturn(true);

    GenerationResult result = generator.generate(projectPath, adapterConfig, projectConfig);

    assertTrue(result.success());
  }
}
