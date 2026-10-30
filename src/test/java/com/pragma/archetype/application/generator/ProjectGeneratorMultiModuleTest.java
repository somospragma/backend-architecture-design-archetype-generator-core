package com.pragma.archetype.application.generator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
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
import com.pragma.archetype.domain.model.template.TemplateVersions;
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
    lenient().when(templateRepository.loadVersions()).thenReturn(TemplateVersions.empty());

    when(templateRepository.processTemplate(anyString(), anyMap()))
        .thenReturn("template content");

    // Setup structure metadata mock for multi-module architectures
    var structureMetadata = new com.pragma.archetype.domain.model.structure.StructureMetadata(
        "hexagonal-multi",
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
        null,
        null,
        null);

    lenient().when(templateRepository.loadStructureMetadata(any())).thenReturn(structureMetadata);
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
  void shouldGenerateMultiModuleArchitectureDeclaredOnlyByTemplates() {
    // Given an architecture the plugin has never heard of, described by its structure.yml
    ArchitectureType cleanMulti = ArchitectureType.fromValue("clean-multi");
    var structureMetadata = new com.pragma.archetype.domain.model.structure.StructureMetadata(
        "clean-multi",
        "app/src/main/resources",
        java.util.Map.of("driven", "adapters/{name}", "driving", "entrypoints/{name}"),
        null, null, null, null, null,
        java.util.List.of(
            com.pragma.archetype.domain.model.structure.ModuleMetadata.builder().name("core").path("core").build(),
            com.pragma.archetype.domain.model.structure.ModuleMetadata.builder().name("app").path("apps/app").build()),
        null, null, null, null, null,
        java.util.List.of("apps", "adapters"),
        true,
        null);
    when(templateRepository.loadStructureMetadata(cleanMulti)).thenReturn(structureMetadata);
    when(templateRepository.templateExists(anyString()))
        .thenAnswer(inv -> ((String) inv.getArgument(0)).startsWith("architectures/clean-multi/modules/"));

    ProjectConfig config = ProjectConfig.builder()
        .name("payment-service")
        .basePackage("com.company.payment")
        .architecture(cleanMulti)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .pluginVersion("1.0.0")
        .build();

    // When
    List<GeneratedFile> files = generator.generateProject(projectPath, config);

    // Then every module and organizing folder comes from structure.yml
    List<Path> paths = files.stream().map(GeneratedFile::path).toList();
    assertTrue(paths.contains(projectPath.resolve("core/build.gradle.kts")));
    assertTrue(paths.contains(projectPath.resolve("apps/app/build.gradle.kts")));
    verify(fileSystemPort).createDirectory(projectPath.resolve("apps"));
    verify(fileSystemPort).createDirectory(projectPath.resolve("adapters"));
    verify(templateRepository).processTemplate(
        org.mockito.ArgumentMatchers.eq("architectures/clean-multi/modules/core/build.gradle.kts.ftl"), anyMap());
  }

  @Test
  void shouldGenerateApplicationClassForSingleModuleArchitectureWithModuleMetadata() {
    // Given a single-module structure.yml that still lists moduleMetadata to locate packages
    var structureMetadata = new com.pragma.archetype.domain.model.structure.StructureMetadata(
        "hexagonal-single",
        "src/main/resources",
        java.util.Map.of("driven", "infrastructure/driven-adapters/{name}"),
        null, null, null, null,
        java.util.List.of("domain/model", "infrastructure/driven-adapters"),
        java.util.List.of(
            com.pragma.archetype.domain.model.structure.ModuleMetadata.builder().name("domain").path("domain").build()),
        null, null, null, null, null,
        java.util.List.of(),
        false,
        false);
    when(templateRepository.loadStructureMetadata(ArchitectureType.HEXAGONAL_SINGLE)).thenReturn(structureMetadata);

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

    // Then the single-module layout is generated, including the Spring Boot entry point
    List<Path> paths = files.stream().map(GeneratedFile::path).toList();
    assertTrue(paths.contains(projectPath.resolve("src/main/resources/application.yml")));
    assertTrue(paths.contains(projectPath.resolve("src/main/java/com/company/payment/PaymentServiceApplication.java")));
    verify(templateRepository).processTemplate(
        org.mockito.ArgumentMatchers.eq("frameworks/spring/imperative/project/Application.java.ftl"), anyMap());
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
