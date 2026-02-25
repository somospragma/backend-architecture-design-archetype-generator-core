package com.pragma.archetype.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.pragma.archetype.domain.model.config.ProjectConfig;
import com.pragma.archetype.domain.model.project.ArchitectureType;
import com.pragma.archetype.domain.model.project.Framework;
import com.pragma.archetype.domain.model.project.Paradigm;

class ProjectConfigBranchTest {

  private static final String VALID_NAME = "test";
  private static final String VALID_PACKAGE = "com.test";
  private static final ArchitectureType VALID_ARCHITECTURE = ArchitectureType.HEXAGONAL_SINGLE;
  private static final Paradigm VALID_PARADIGM = Paradigm.REACTIVE;
  private static final Framework VALID_FRAMEWORK = Framework.SPRING;

  @Test
  void shouldCreateWithAllFields() {
    // When
    ProjectConfig config = ProjectConfig.builder()
        .name("test-project")
        .basePackage(VALID_PACKAGE)
        .architecture(VALID_ARCHITECTURE)
        .paradigm(VALID_PARADIGM)
        .framework(VALID_FRAMEWORK)
        .pluginVersion("1.0.0")
        .createdAt(LocalDateTime.now())
        .build();

    // Then
    assertNotNull(config);
    assertEquals("test-project", config.name());
    assertEquals(VALID_PACKAGE, config.basePackage());
  }

  @Test
  void shouldCreateWithMinimalFields() {
    // When
    ProjectConfig config = ProjectConfig.builder()
        .name(VALID_NAME)
        .basePackage(VALID_PACKAGE)
        .architecture(VALID_ARCHITECTURE)
        .paradigm(VALID_PARADIGM)
        .framework(VALID_FRAMEWORK)
        .build();

    // Then
    assertNotNull(config);
    assertEquals("0.1.0-SNAPSHOT", config.pluginVersion()); // Default value
    assertNotNull(config.createdAt()); // Auto-generated
  }

  @Test
  void shouldHandleNullArchitecture() {
    // When/Then
    assertThrows(NullPointerException.class, () -> ProjectConfig.builder()
        .name(VALID_NAME)
        .basePackage(VALID_PACKAGE)
        .architecture(null)
        .paradigm(VALID_PARADIGM)
        .framework(VALID_FRAMEWORK)
        .build());
  }

  @Test
  void shouldHandleNullParadigm() {
    // When/Then
    assertThrows(NullPointerException.class, () -> ProjectConfig.builder()
        .name(VALID_NAME)
        .basePackage(VALID_PACKAGE)
        .architecture(VALID_ARCHITECTURE)
        .paradigm(null)
        .framework(VALID_FRAMEWORK)
        .build());
  }

  @Test
  void shouldHandleNullFramework() {
    // When/Then
    assertThrows(NullPointerException.class, () -> ProjectConfig.builder()
        .name(VALID_NAME)
        .basePackage(VALID_PACKAGE)
        .architecture(VALID_ARCHITECTURE)
        .paradigm(VALID_PARADIGM)
        .framework(null)
        .build());
  }

  @Test
  void shouldHandleNullPluginVersion() {
    // When - null pluginVersion should use default
    ProjectConfig config = ProjectConfig.builder()
        .name(VALID_NAME)
        .basePackage(VALID_PACKAGE)
        .architecture(VALID_ARCHITECTURE)
        .paradigm(VALID_PARADIGM)
        .framework(VALID_FRAMEWORK)
        .pluginVersion(null)
        .build();

    // Then
    assertNotNull(config);
    assertEquals("0.1.0-SNAPSHOT", config.pluginVersion());
  }

  @Test
  void shouldHandleNullCreatedAt() {
    // When - null createdAt should use current time
    ProjectConfig config = ProjectConfig.builder()
        .name(VALID_NAME)
        .basePackage(VALID_PACKAGE)
        .architecture(VALID_ARCHITECTURE)
        .paradigm(VALID_PARADIGM)
        .framework(VALID_FRAMEWORK)
        .createdAt(null)
        .build();

    // Then
    assertNotNull(config);
    assertNotNull(config.createdAt());
  }

  @Test
  void shouldTestEquality() {
    // Given
    LocalDateTime now = LocalDateTime.now();
    ProjectConfig config1 = ProjectConfig.builder()
        .name(VALID_NAME)
        .basePackage(VALID_PACKAGE)
        .architecture(VALID_ARCHITECTURE)
        .paradigm(VALID_PARADIGM)
        .framework(VALID_FRAMEWORK)
        .createdAt(now)
        .build();
    ProjectConfig config2 = ProjectConfig.builder()
        .name(VALID_NAME)
        .basePackage(VALID_PACKAGE)
        .architecture(VALID_ARCHITECTURE)
        .paradigm(VALID_PARADIGM)
        .framework(VALID_FRAMEWORK)
        .createdAt(now)
        .build();

    // Then
    assertEquals(config1, config2);
    assertEquals(config1.hashCode(), config2.hashCode());
  }

  @Test
  void shouldTestInequality() {
    // Given
    ProjectConfig config1 = ProjectConfig.builder()
        .name("test1")
        .basePackage(VALID_PACKAGE)
        .architecture(VALID_ARCHITECTURE)
        .paradigm(VALID_PARADIGM)
        .framework(VALID_FRAMEWORK)
        .build();
    ProjectConfig config2 = ProjectConfig.builder()
        .name("test2")
        .basePackage(VALID_PACKAGE)
        .architecture(VALID_ARCHITECTURE)
        .paradigm(VALID_PARADIGM)
        .framework(VALID_FRAMEWORK)
        .build();

    // Then
    assertNotEquals(config1, config2);
  }

  @Test
  void shouldTestToString() {
    // Given
    ProjectConfig config = ProjectConfig.builder()
        .name(VALID_NAME)
        .basePackage(VALID_PACKAGE)
        .architecture(VALID_ARCHITECTURE)
        .paradigm(VALID_PARADIGM)
        .framework(VALID_FRAMEWORK)
        .build();

    // When
    String str = config.toString();

    // Then
    assertNotNull(str);
    assertTrue(str.contains(VALID_NAME));
    assertTrue(str.contains(VALID_PACKAGE));
  }

  @Test
  void shouldHandleAllArchitectureTypes() {
    for (ArchitectureType type : ArchitectureType.values()) {
      ProjectConfig config = ProjectConfig.builder()
          .name(VALID_NAME)
          .basePackage(VALID_PACKAGE)
          .architecture(type)
          .paradigm(VALID_PARADIGM)
          .framework(VALID_FRAMEWORK)
          .build();
      assertNotNull(config);
      assertEquals(type, config.architecture());
    }
  }

  @Test
  void shouldHandleAllParadigms() {
    for (Paradigm paradigm : Paradigm.values()) {
      ProjectConfig config = ProjectConfig.builder()
          .name(VALID_NAME)
          .basePackage(VALID_PACKAGE)
          .architecture(VALID_ARCHITECTURE)
          .paradigm(paradigm)
          .framework(VALID_FRAMEWORK)
          .build();
      assertNotNull(config);
      assertEquals(paradigm, config.paradigm());
    }
  }

  @Test
  void shouldHandleAllFrameworks() {
    for (Framework framework : Framework.values()) {
      ProjectConfig config = ProjectConfig.builder()
          .name(VALID_NAME)
          .basePackage(VALID_PACKAGE)
          .architecture(VALID_ARCHITECTURE)
          .paradigm(VALID_PARADIGM)
          .framework(framework)
          .build();
      assertNotNull(config);
      assertEquals(framework, config.framework());
    }
  }

  @Test
  void shouldHandleEmptyName() {
    // When/Then - empty name should throw exception
    assertThrows(IllegalArgumentException.class, () -> ProjectConfig.builder()
        .name("")
        .basePackage(VALID_PACKAGE)
        .architecture(VALID_ARCHITECTURE)
        .paradigm(VALID_PARADIGM)
        .framework(VALID_FRAMEWORK)
        .build());
  }

  @Test
  void shouldHandleEmptyBasePackage() {
    // When/Then - empty package should throw exception
    assertThrows(IllegalArgumentException.class, () -> ProjectConfig.builder()
        .name(VALID_NAME)
        .basePackage("")
        .architecture(VALID_ARCHITECTURE)
        .paradigm(VALID_PARADIGM)
        .framework(VALID_FRAMEWORK)
        .build());
  }

  @Test
  void shouldHandleLongName() {
    // Given
    String longName = "a".repeat(1000);

    // When
    ProjectConfig config = ProjectConfig.builder()
        .name(longName)
        .basePackage(VALID_PACKAGE)
        .architecture(VALID_ARCHITECTURE)
        .paradigm(VALID_PARADIGM)
        .framework(VALID_FRAMEWORK)
        .build();

    // Then
    assertNotNull(config);
    assertEquals(longName, config.name());
  }

  @Test
  void shouldHandleLongBasePackage() {
    // Given
    String longPackage = "com." + "test.".repeat(100) + "end";

    // When
    ProjectConfig config = ProjectConfig.builder()
        .name(VALID_NAME)
        .basePackage(longPackage)
        .architecture(VALID_ARCHITECTURE)
        .paradigm(VALID_PARADIGM)
        .framework(VALID_FRAMEWORK)
        .build();

    // Then
    assertNotNull(config);
    assertEquals(longPackage, config.basePackage());
  }

  @Test
  void shouldHandleSpecialCharactersInName() {
    // When
    ProjectConfig config = ProjectConfig.builder()
        .name("test-project_123")
        .basePackage(VALID_PACKAGE)
        .architecture(VALID_ARCHITECTURE)
        .paradigm(VALID_PARADIGM)
        .framework(VALID_FRAMEWORK)
        .build();

    // Then
    assertNotNull(config);
    assertTrue(config.name().contains("-"));
    assertTrue(config.name().contains("_"));
  }

  @Test
  void shouldHandleFutureCreatedAt() {
    // Given
    LocalDateTime future = LocalDateTime.now().plusYears(10);

    // When
    ProjectConfig config = ProjectConfig.builder()
        .name(VALID_NAME)
        .basePackage(VALID_PACKAGE)
        .architecture(VALID_ARCHITECTURE)
        .paradigm(VALID_PARADIGM)
        .framework(VALID_FRAMEWORK)
        .createdAt(future)
        .build();

    // Then
    assertNotNull(config);
    assertEquals(future, config.createdAt());
  }

  @Test
  void shouldHandlePastCreatedAt() {
    // Given
    LocalDateTime past = LocalDateTime.now().minusYears(10);

    // When
    ProjectConfig config = ProjectConfig.builder()
        .name(VALID_NAME)
        .basePackage(VALID_PACKAGE)
        .architecture(VALID_ARCHITECTURE)
        .paradigm(VALID_PARADIGM)
        .framework(VALID_FRAMEWORK)
        .createdAt(past)
        .build();

    // Then
    assertNotNull(config);
    assertEquals(past, config.createdAt());
  }
}
