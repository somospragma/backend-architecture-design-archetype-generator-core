package com.pragma.archetype.application.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pragma.archetype.domain.model.GeneratedFile;
import com.pragma.archetype.domain.model.config.ProjectConfig;
import com.pragma.archetype.domain.model.entity.EntityConfig;
import com.pragma.archetype.domain.model.project.ArchitectureType;
import com.pragma.archetype.domain.model.project.Framework;
import com.pragma.archetype.domain.model.project.Paradigm;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import com.pragma.archetype.domain.port.out.TemplateRepository;

@ExtendWith(MockitoExtension.class)
class EntityGeneratorTest {

  @Mock
  private TemplateRepository templateRepository;

  @Mock
  private FileSystemPort fileSystemPort;

  private EntityGenerator generator;

  @BeforeEach
  void setUp() {
    generator = new EntityGenerator(templateRepository, fileSystemPort);
  }

  @Test
  void shouldGenerateEntityForSingleModuleArchitecture() {
    // Given
    ProjectConfig projectConfig = ProjectConfig.builder()
        .name("test-project")
        .basePackage("com.test")
        .architecture(ArchitectureType.HEXAGONAL_SINGLE)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .pluginVersion("1.0.0")
        .build();

    EntityConfig entityConfig = EntityConfig.builder()
        .name("User")
        .packageName("com.test.domain.model")
        .hasId(true)
        .idType("String")
        .fields(List.of(
            new EntityConfig.EntityField("name", "String", false),
            new EntityConfig.EntityField("email", "String", false)))
        .build();

    when(templateRepository.processTemplate(anyString(), anyMap()))
        .thenReturn("public class User {}");

    // When
    List<GeneratedFile> files = generator.generateEntity(
        Path.of("/test"),
        projectConfig,
        entityConfig);

    // Then
    assertNotNull(files);
    assertEquals(1, files.size());
    verify(templateRepository).processTemplate(anyString(), anyMap());
    verify(fileSystemPort).createDirectory(any());
  }

  @Test
  void shouldGenerateEntityForMultiModuleArchitecture() {
    // Given
    ProjectConfig projectConfig = ProjectConfig.builder()
        .name("test-project")
        .basePackage("com.test")
        .architecture(ArchitectureType.HEXAGONAL_MULTI)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .pluginVersion("1.0.0")
        .build();

    EntityConfig entityConfig = EntityConfig.builder()
        .name("Product")
        .packageName("com.test.domain.model")
        .hasId(true)
        .idType("UUID")
        .fields(List.of(
            new EntityConfig.EntityField("name", "String", false),
            new EntityConfig.EntityField("price", "BigDecimal", false)))
        .build();

    when(templateRepository.processTemplate(anyString(), anyMap()))
        .thenReturn("public class Product {}");

    // When
    List<GeneratedFile> files = generator.generateEntity(
        Path.of("/test"),
        projectConfig,
        entityConfig);

    // Then
    assertNotNull(files);
    assertEquals(1, files.size());
    assertTrue(files.get(0).path().toString().contains("domain"));
  }

  @Test
  void shouldHandleEntityWithLocalDateTimeField() {
    // Given
    ProjectConfig projectConfig = ProjectConfig.builder()
        .name("test-project")
        .basePackage("com.test")
        .architecture(ArchitectureType.HEXAGONAL_SINGLE)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .pluginVersion("1.0.0")
        .build();

    EntityConfig entityConfig = EntityConfig.builder()
        .name("Event")
        .packageName("com.test.domain.model")
        .hasId(true)
        .idType("String")
        .fields(List.of(
            new EntityConfig.EntityField("name", "String", false),
            new EntityConfig.EntityField("createdAt", "LocalDateTime", false)))
        .build();

    when(templateRepository.processTemplate(anyString(), anyMap()))
        .thenReturn("public class Event {}");

    // When
    List<GeneratedFile> files = generator.generateEntity(
        Path.of("/test"),
        projectConfig,
        entityConfig);

    // Then
    assertNotNull(files);
    assertEquals(1, files.size());
  }

  @Test
  void shouldHandleEntityWithLocalDateField() {
    // Given
    ProjectConfig projectConfig = ProjectConfig.builder()
        .name("test-project")
        .basePackage("com.test")
        .architecture(ArchitectureType.HEXAGONAL_SINGLE)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .pluginVersion("1.0.0")
        .build();

    EntityConfig entityConfig = EntityConfig.builder()
        .name("Booking")
        .packageName("com.test.domain.model")
        .hasId(true)
        .idType("String")
        .fields(List.of(
            new EntityConfig.EntityField("date", "LocalDate", false)))
        .build();

    when(templateRepository.processTemplate(anyString(), anyMap()))
        .thenReturn("public class Booking {}");

    // When
    List<GeneratedFile> files = generator.generateEntity(
        Path.of("/test"),
        projectConfig,
        entityConfig);

    // Then
    assertNotNull(files);
    assertEquals(1, files.size());
  }
}
