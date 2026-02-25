package com.pragma.archetype.application.generator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class ProjectGeneratorMultiModuleTest {

  @Mock
  private TemplateRepository templateRepository;

  @Mock
  private FileSystemPort fileSystemPort;

  private ProjectGenerator generator;
  private Path projectPath;

  @BeforeEach
  void setUp() {
    generator = new ProjectGenerator(templateRepository, fileSystemPort);
    projectPath = Paths.get("/test/project");

    when(templateRepository.processTemplate(anyString(), anyMap()))
        .thenReturn("template content");
    when(fileSystemPort.readFile(any(Path.class)))
        .thenReturn("existing content");
  }

  @Test
  void shouldGenerateHexagonalMultiModuleProject() {
    // Given
    ProjectConfig config = ProjectConfig.builder()
        .name("payment-service")
        .basePackage("com.company.payment")
        .architecture(ArchitectureType.HEXAGONAL_MULTI)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .pluginVersion("1.0.0")
        .build();

    // When
    List<GeneratedFile> files = generator.generateProject(projectPath, config);

    // Then
    assertNotNull(files);
    assertFalse(files.isEmpty());
    verify(fileSystemPort).writeFiles(anyList());
    verify(fileSystemPort, atLeastOnce()).createDirectory(any(Path.class));
  }

  @Test
  void shouldGenerateOnionMultiModuleProject() {
    // Given
    ProjectConfig config = ProjectConfig.builder()
        .name("payment-service")
        .basePackage("com.company.payment")
        .architecture(ArchitectureType.ONION_MULTI)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .pluginVersion("1.0.0")
        .build();

    // When
    List<GeneratedFile> files = generator.generateProject(projectPath, config);

    // Then
    assertNotNull(files);
    assertFalse(files.isEmpty());
    verify(fileSystemPort).writeFiles(anyList());
  }

  @Test
  void shouldGenerateHexagonalGranularProject() {
    // Given
    ProjectConfig config = ProjectConfig.builder()
        .name("payment-service")
        .basePackage("com.company.payment")
        .architecture(ArchitectureType.HEXAGONAL_MULTI_GRANULAR)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .pluginVersion("1.0.0")
        .build();

    // When
    List<GeneratedFile> files = generator.generateProject(projectPath, config);

    // Then
    assertNotNull(files);
    assertFalse(files.isEmpty());
    verify(fileSystemPort).writeFiles(anyList());
  }

  @Test
  void shouldGenerateImperativeProject() {
    // Given
    ProjectConfig config = ProjectConfig.builder()
        .name("payment-service")
        .basePackage("com.company.payment")
        .architecture(ArchitectureType.HEXAGONAL_SINGLE)
        .paradigm(Paradigm.IMPERATIVE)
        .framework(Framework.SPRING)
        .pluginVersion("1.0.0")
        .build();

    // When
    List<GeneratedFile> files = generator.generateProject(projectPath, config);

    // Then
    assertNotNull(files);
    assertFalse(files.isEmpty());
    verify(fileSystemPort).writeFiles(anyList());
  }

  @Test
  void shouldGenerateQuarkusProject() {
    // Given
    ProjectConfig config = ProjectConfig.builder()
        .name("payment-service")
        .basePackage("com.company.payment")
        .architecture(ArchitectureType.HEXAGONAL_SINGLE)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.QUARKUS)
        .pluginVersion("1.0.0")
        .build();

    // When
    List<GeneratedFile> files = generator.generateProject(projectPath, config);

    // Then
    assertNotNull(files);
    assertFalse(files.isEmpty());
  }

  @Test
  void shouldHandleProjectWithHyphenatedName() {
    // Given
    ProjectConfig config = ProjectConfig.builder()
        .name("payment-processing-service")
        .basePackage("com.company.payment")
        .architecture(ArchitectureType.HEXAGONAL_SINGLE)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .pluginVersion("1.0.0")
        .build();

    // When
    List<GeneratedFile> files = generator.generateProject(projectPath, config);

    // Then
    assertNotNull(files);
    assertFalse(files.isEmpty());
  }

  @Test
  void shouldHandleProjectWithUnderscores() {
    // Given
    ProjectConfig config = ProjectConfig.builder()
        .name("payment_service")
        .basePackage("com.company.payment")
        .architecture(ArchitectureType.HEXAGONAL_SINGLE)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .pluginVersion("1.0.0")
        .build();

    // When
    List<GeneratedFile> files = generator.generateProject(projectPath, config);

    // Then
    assertNotNull(files);
    assertFalse(files.isEmpty());
  }

  @Test
  void shouldHandleDeepPackageStructure() {
    // Given
    ProjectConfig config = ProjectConfig.builder()
        .name("payment-service")
        .basePackage("com.company.division.team.payment")
        .architecture(ArchitectureType.HEXAGONAL_SINGLE)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .pluginVersion("1.0.0")
        .build();

    // When
    List<GeneratedFile> files = generator.generateProject(projectPath, config);

    // Then
    assertNotNull(files);
    assertFalse(files.isEmpty());
  }

  @Test
  void shouldGenerateProjectWithAdaptersAsModules() {
    // Given
    ProjectConfig config = ProjectConfig.builder()
        .name("payment-service")
        .basePackage("com.company.payment")
        .architecture(ArchitectureType.HEXAGONAL_MULTI)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .adaptersAsModules(true)
        .pluginVersion("1.0.0")
        .build();

    // When
    List<GeneratedFile> files = generator.generateProject(projectPath, config);

    // Then
    assertNotNull(files);
    assertFalse(files.isEmpty());
  }
}
