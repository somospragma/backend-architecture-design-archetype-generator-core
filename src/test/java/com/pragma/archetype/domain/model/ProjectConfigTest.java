package com.pragma.archetype.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.Test;

class ProjectConfigTest {

  @Test
  void shouldBuildProjectConfigWithAllFields() {
    // Given
    LocalDateTime now = LocalDateTime.now();

    // When
    ProjectConfig config = ProjectConfig.builder()
        .name("payment-service")
        .basePackage("com.company.payment")
        .architecture(ArchitectureType.HEXAGONAL_SINGLE)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .pluginVersion("1.0.0")
        .createdAt(now)
        .adaptersAsModules(true)
        .dependencyOverrides(Map.of("spring-boot", "3.2.0"))
        .build();

    // Then
    assertEquals("payment-service", config.name());
    assertEquals("com.company.payment", config.basePackage());
    assertEquals(ArchitectureType.HEXAGONAL_SINGLE, config.architecture());
    assertEquals(Paradigm.REACTIVE, config.paradigm());
    assertEquals(Framework.SPRING, config.framework());
    assertEquals("1.0.0", config.pluginVersion());
    assertEquals(now, config.createdAt());
    assertTrue(config.adaptersAsModules());
    assertEquals(1, config.dependencyOverrides().size());
  }

  @Test
  void shouldBuildProjectConfigWithDefaults() {
    // When
    ProjectConfig config = ProjectConfig.builder()
        .name("test-service")
        .basePackage("com.test")
        .architecture(ArchitectureType.HEXAGONAL_SINGLE)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .build();

    // Then
    assertEquals("0.1.0-SNAPSHOT", config.pluginVersion());
    assertNotNull(config.createdAt());
    assertFalse(config.adaptersAsModules());
    assertTrue(config.dependencyOverrides().isEmpty());
  }

  @Test
  void shouldThrowExceptionForNullName() {
    assertThrows(NullPointerException.class, () -> ProjectConfig.builder()
        .name(null)
        .basePackage("com.test")
        .architecture(ArchitectureType.HEXAGONAL_SINGLE)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .build());
  }

  @Test
  void shouldThrowExceptionForBlankName() {
    assertThrows(IllegalArgumentException.class, () -> ProjectConfig.builder()
        .name("")
        .basePackage("com.test")
        .architecture(ArchitectureType.HEXAGONAL_SINGLE)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .build());
  }

  @Test
  void shouldThrowExceptionForNullBasePackage() {
    assertThrows(NullPointerException.class, () -> ProjectConfig.builder()
        .name("test-service")
        .basePackage(null)
        .architecture(ArchitectureType.HEXAGONAL_SINGLE)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .build());
  }

  @Test
  void shouldThrowExceptionForInvalidPackageName() {
    assertThrows(IllegalArgumentException.class, () -> ProjectConfig.builder()
        .name("test-service")
        .basePackage("Com.Test") // Capital letters not allowed
        .architecture(ArchitectureType.HEXAGONAL_SINGLE)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .build());
  }

  @Test
  void shouldThrowExceptionForPackageStartingWithNumber() {
    assertThrows(IllegalArgumentException.class, () -> ProjectConfig.builder()
        .name("test-service")
        .basePackage("com.1test")
        .architecture(ArchitectureType.HEXAGONAL_SINGLE)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .build());
  }

  @Test
  void shouldAcceptValidPackageNames() {
    String[] validPackages = {
        "com.test",
        "com.company.payment",
        "org.example.service",
        "com.test_service",
        "com.test123"
    };

    for (String packageName : validPackages) {
      ProjectConfig config = ProjectConfig.builder()
          .name("test-service")
          .basePackage(packageName)
          .architecture(ArchitectureType.HEXAGONAL_SINGLE)
          .paradigm(Paradigm.REACTIVE)
          .framework(Framework.SPRING)
          .build();

      assertEquals(packageName, config.basePackage());
    }
  }

  @Test
  void shouldThrowExceptionForNullArchitecture() {
    assertThrows(NullPointerException.class, () -> ProjectConfig.builder()
        .name("test-service")
        .basePackage("com.test")
        .architecture(null)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .build());
  }

  @Test
  void shouldThrowExceptionForNullParadigm() {
    assertThrows(NullPointerException.class, () -> ProjectConfig.builder()
        .name("test-service")
        .basePackage("com.test")
        .architecture(ArchitectureType.HEXAGONAL_SINGLE)
        .paradigm(null)
        .framework(Framework.SPRING)
        .build());
  }

  @Test
  void shouldThrowExceptionForNullFramework() {
    assertThrows(NullPointerException.class, () -> ProjectConfig.builder()
        .name("test-service")
        .basePackage("com.test")
        .architecture(ArchitectureType.HEXAGONAL_SINGLE)
        .paradigm(Paradigm.REACTIVE)
        .framework(null)
        .build());
  }

  @Test
  void shouldHandleNullDependencyOverrides() {
    // When
    ProjectConfig config = ProjectConfig.builder()
        .name("test-service")
        .basePackage("com.test")
        .architecture(ArchitectureType.HEXAGONAL_SINGLE)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .dependencyOverrides(null)
        .build();

    // Then
    assertNotNull(config.dependencyOverrides());
    assertTrue(config.dependencyOverrides().isEmpty());
  }

  @Test
  void shouldSupportMultipleDependencyOverrides() {
    // Given
    Map<String, String> overrides = Map.of(
        "spring-boot", "3.2.0",
        "reactor", "3.6.0",
        "lombok", "1.18.30");

    // When
    ProjectConfig config = ProjectConfig.builder()
        .name("test-service")
        .basePackage("com.test")
        .architecture(ArchitectureType.HEXAGONAL_SINGLE)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .dependencyOverrides(overrides)
        .build();

    // Then
    assertEquals(3, config.dependencyOverrides().size());
    assertEquals("3.2.0", config.dependencyOverrides().get("spring-boot"));
  }
}
