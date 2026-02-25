package com.pragma.archetype.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.pragma.archetype.domain.model.config.ProjectConfig;

class ProjectConfigBranchTest {

  @Test
  void shouldCreateWithAllFields() {
    // When
    ProjectConfig config = ProjectConfig.builder()
        .name("test-project")
        .basePackage("com.test")
        .architecture(ArchitectureType.HEXAGONAL_SINGLE)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .pluginVersion("1.0.0")
        .createdAt(LocalDateTime.now())
        .build();

    // Then
    assertNotNull(config);
    assertEquals("test-project", config.name());
    assertEquals("com.test", config.basePackage());
  }

  @Test
  void shouldCreateWithMinimalFields() {
    // When
    ProjectConfig config = ProjectConfig.builder()
        .name("test")
        .basePackage("com.test")
        .build();

    // Then
    assertNotNull(config);
  }

  @Test
  void shouldHandleNullArchitecture() {
    // When
    ProjectConfig config = ProjectConfig.builder()
        .name("test")
        .basePackage("com.test")
        .architecture(null)
        .build();

    // Then
    assertNotNull(config);
  }

  @Test
  void shouldHandleNullParadigm() {
    // When
    ProjectConfig config = ProjectConfig.builder()
        .name("test")
        .basePackage("com.test")
        .paradigm(null)
        .build();

    // Then
    assertNotNull(config);
  }

  @Test
  void shouldHandleNullFramework() {
    // When
    ProjectConfig config = ProjectConfig.builder()
        .name("test")
        .basePackage("com.test")
        .framework(null)
        .build();

    // Then
    assertNotNull(config);
  }

  @Test
  void shouldHandleNullPluginVersion() {
    // When
    ProjectConfig config = ProjectConfig.builder()
        .name("test")
        .basePackage("com.test")
        .pluginVersion(null)
        .build();

    // Then
    assertNotNull(config);
  }

  @Test
  void shouldHandleNullCreatedAt() {
    // When
    ProjectConfig config = ProjectConfig.builder()
        .name("test")
        .basePackage("com.test")
        .createdAt(null)
        .build();

    // Then
    assertNotNull(config);
  }

  @Test
  void shouldTestEquality() {
    // Given
    ProjectConfig config1 = ProjectConfig.builder()
        .name("test")
        .basePackage("com.test")
        .build();
    ProjectConfig config2 = ProjectConfig.builder()
        .name("test")
        .basePackage("com.test")
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
        .basePackage("com.test")
        .build();
    ProjectConfig config2 = ProjectConfig.builder()
        .name("test2")
        .basePackage("com.test")
        .build();

    // Then
    assertNotEquals(config1, config2);
  }

  @Test
  void shouldTestToString() {
    // Given
    ProjectConfig config = ProjectConfig.builder()
        .name("test")
        .basePackage("com.test")
        .build();

    // When
    String str = config.toString();

    // Then
    assertNotNull(str);
    assertTrue(str.contains("test"));
    assertTrue(str.contains("com.test"));
  }

  @Test
  void shouldHandleAllArchitectureTypes() {
    for (ArchitectureType type : ArchitectureType.values()) {
      ProjectConfig config = ProjectConfig.builder()
          .name("test")
          .basePackage("com.test")
          .architecture(type)
          .build();
      assertNotNull(config);
      assertEquals(type, config.architecture());
    }
  }

  @Test
  void shouldHandleAllParadigms() {
    for (Paradigm paradigm : Paradigm.values()) {
      ProjectConfig config = ProjectConfig.builder()
          .name("test")
          .basePackage("com.test")
          .paradigm(paradigm)
          .build();
      assertNotNull(config);
      assertEquals(paradigm, config.paradigm());
    }
  }

  @Test
  void shouldHandleAllFrameworks() {
    for (Framework framework : Framework.values()) {
      ProjectConfig config = ProjectConfig.builder()
          .name("test")
          .basePackage("com.test")
          .framework(framework)
          .build();
      assertNotNull(config);
      assertEquals(framework, config.framework());
    }
  }

  @Test
  void shouldHandleEmptyName() {
    // When
    ProjectConfig config = ProjectConfig.builder()
        .name("")
        .basePackage("com.test")
        .build();

    // Then
    assertNotNull(config);
    assertEquals("", config.name());
  }

  @Test
  void shouldHandleEmptyBasePackage() {
    // When
    ProjectConfig config = ProjectConfig.builder()
        .name("test")
        .basePackage("")
        .build();

    // Then
    assertNotNull(config);
    assertEquals("", config.basePackage());
  }

  @Test
  void shouldHandleLongName() {
    // Given
    String longName = "a".repeat(1000);

    // When
    ProjectConfig config = ProjectConfig.builder()
        .name(longName)
        .basePackage("com.test")
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
        .name("test")
        .basePackage(longPackage)
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
        .basePackage("com.test")
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
        .name("test")
        .basePackage("com.test")
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
        .name("test")
        .basePackage("com.test")
        .createdAt(past)
        .build();

    // Then
    assertNotNull(config);
    assertEquals(past, config.createdAt());
  }
}
