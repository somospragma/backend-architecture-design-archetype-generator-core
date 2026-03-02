package com.pragma.archetype.application.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pragma.archetype.domain.model.config.ProjectConfig;
import com.pragma.archetype.domain.model.file.GeneratedFile;
import com.pragma.archetype.domain.model.project.ArchitectureType;
import com.pragma.archetype.domain.model.project.Framework;
import com.pragma.archetype.domain.model.project.Paradigm;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import com.pragma.archetype.domain.port.out.TemplateRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectGenerator Tests")
class ProjectGeneratorTest {

  @Mock
  private TemplateRepository templateRepository;

  @Mock
  private FileSystemPort fileSystemPort;

  private ProjectGenerator generator;
  private Path projectPath;
  private ProjectConfig config;

  @BeforeEach
  void setUp() {
    generator = new ProjectGenerator(templateRepository, fileSystemPort);
    projectPath = Paths.get("/test/project");

    config = ProjectConfig.builder()
        .name("payment-service")
        .basePackage("com.company.payment")
        .architecture(ArchitectureType.HEXAGONAL_SINGLE)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .build();

    // Setup default template responses
    when(templateRepository.processTemplate(anyString(), anyMap()))
        .thenReturn("template content");

    // Setup structure metadata mock
    var structureMetadata = new com.pragma.archetype.domain.model.structure.StructureMetadata(
        "hexagonal-single",
        "src/main/resources",
        java.util.Map.of(
            "driven", "infrastructure/adapter/out/{name}",
            "driving", "infrastructure/adapter/in/{name}"),
        null,
        null,
        null,
        null,
        java.util.List.of("domain/model", "domain/port", "application/usecase", "infrastructure/adapter"),
        null,
        null,
        null,
        null);

    lenient().when(templateRepository.loadStructureMetadata(any())).thenReturn(structureMetadata);
  }

  @Test
  @DisplayName("Should generate complete project structure")
  void shouldGenerateCompleteProjectStructure() {
    // When
    List<GeneratedFile> files = generator.generateProject(projectPath, config);

    // Then
    assertNotNull(files);
    assertFalse(files.isEmpty());

    // Verify files were written
    verify(fileSystemPort).writeFiles(anyList());

    // Verify directories were created
    verify(fileSystemPort, atLeastOnce()).createDirectory(any(Path.class));
  }

  @Test
  @DisplayName("Should generate base project files")
  void shouldGenerateBaseProjectFiles() {
    // When
    generator.generateProject(projectPath, config);

    // Then
    ArgumentCaptor<List<GeneratedFile>> filesCaptor = ArgumentCaptor.forClass(List.class);
    verify(fileSystemPort).writeFiles(filesCaptor.capture());

    List<GeneratedFile> files = filesCaptor.getValue();

    // Check that base files are generated
    assertTrue(files.stream().anyMatch(f -> f.path().endsWith("build.gradle.kts")));
    assertTrue(files.stream().anyMatch(f -> f.path().endsWith("settings.gradle.kts")));
    assertTrue(files.stream().anyMatch(f -> f.path().endsWith(".gitignore")));
    assertTrue(files.stream().anyMatch(f -> f.path().endsWith("README.md")));
  }

  @Test
  @DisplayName("Should generate framework-specific files")
  void shouldGenerateFrameworkSpecificFiles() {
    // When
    generator.generateProject(projectPath, config);

    // Then
    ArgumentCaptor<List<GeneratedFile>> filesCaptor = ArgumentCaptor.forClass(List.class);
    verify(fileSystemPort).writeFiles(filesCaptor.capture());

    List<GeneratedFile> files = filesCaptor.getValue();

    // Check that framework files are generated
    assertTrue(files.stream().anyMatch(f -> f.path().endsWith("application.yml")));
    assertTrue(files.stream().anyMatch(f -> f.path().toString().contains("Application.java")),
        "Should generate Application.java file (actual files: " +
            files.stream().map(f -> f.path().toString()).collect(java.util.stream.Collectors.joining(", ")) + ")");
  }

  @Test
  @DisplayName("Should create architecture-specific directories")
  void shouldCreateArchitectureSpecificDirectories() {
    // When
    generator.generateProject(projectPath, config);

    // Then
    // Verify hexagonal architecture directories were created
    verify(fileSystemPort, atLeastOnce()).createDirectory(argThat(path -> path.toString().contains("domain/model") ||
        path.toString().contains("domain/port") ||
        path.toString().contains("application/usecase") ||
        path.toString().contains("infrastructure/adapter")));
  }

  @Test
  @DisplayName("Should process templates with correct context")
  void shouldProcessTemplatesWithCorrectContext() {
    // When
    generator.generateProject(projectPath, config);

    // Then
    ArgumentCaptor<java.util.Map<String, Object>> contextCaptor = ArgumentCaptor.forClass(java.util.Map.class);
    verify(templateRepository, atLeastOnce()).processTemplate(anyString(), contextCaptor.capture());

    java.util.Map<String, Object> context = contextCaptor.getValue();

    // Verify context contains expected values
    assertEquals("payment-service", context.get("projectName"));
    assertEquals("com.company.payment", context.get("basePackage"));
    assertEquals("com/company/payment", context.get("packagePath"));
    assertTrue((Boolean) context.get("isReactive"));
    assertFalse((Boolean) context.get("isImperative"));
    assertTrue((Boolean) context.get("isSpring"));
  }

  @Test
  @DisplayName("Should generate .gitkeep files for empty directories")
  void shouldGenerateGitkeepFilesForEmptyDirectories() {
    // When
    generator.generateProject(projectPath, config);

    // Then
    ArgumentCaptor<List<GeneratedFile>> filesCaptor = ArgumentCaptor.forClass(List.class);
    verify(fileSystemPort).writeFiles(filesCaptor.capture());

    List<GeneratedFile> files = filesCaptor.getValue();

    // Check that .gitkeep files are generated
    assertTrue(files.stream().anyMatch(f -> f.path().endsWith(".gitkeep")));
  }

  @Test
  @DisplayName("Should handle Onion architecture")
  void shouldHandleOnionArchitecture() {
    // Given
    ProjectConfig onionConfig = ProjectConfig.builder()
        .name("payment-service")
        .basePackage("com.company.payment")
        .architecture(ArchitectureType.ONION_SINGLE)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .build();

    // When
    generator.generateProject(projectPath, onionConfig);

    // Then
    // Verify onion architecture directories were created
    verify(fileSystemPort, atLeastOnce()).createDirectory(argThat(path -> path.toString().contains("core/domain") ||
        path.toString().contains("core/application") ||
        path.toString().contains("infrastructure/adapter")));
  }
}
