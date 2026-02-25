package com.pragma.archetype.infrastructure.adapter.in.gradle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GenerateEntityTaskTest {

  @TempDir
  Path tempDir;

  private Project project;
  private GenerateEntityTask task;

  @BeforeEach
  void setUp() throws Exception {
    project = ProjectBuilder.builder()
        .withProjectDir(tempDir.toFile())
        .build();
    task = project.getTasks().create("generateEntity", GenerateEntityTask.class);

    // Create a valid .cleanarch.yml configuration for tests that need it
    createBasicConfiguration();
  }

  @Test
  void shouldSetAndGetEntityName() {
    task.setEntityName("User");
    assertEquals("User", task.getEntityName());
  }

  @Test
  void shouldSetAndGetFields() {
    task.setFields("name:String,email:String");
    assertEquals("name:String,email:String", task.getFields());
  }

  @Test
  void shouldSetAndGetPackageName() {
    task.setPackageName("com.test.domain.model");
    assertEquals("com.test.domain.model", task.getPackageName());
  }

  @Test
  void shouldSetAndGetHasId() {
    task.setHasId(false);
    assertFalse(task.getHasId());
  }

  @Test
  void shouldSetAndGetIdType() {
    task.setIdType("Long");
    assertEquals("Long", task.getIdType());
  }

  @Test
  void shouldHaveDefaultValues() {
    assertEquals("", task.getEntityName());
    assertEquals("", task.getFields());
    assertTrue(task.getHasId());
    assertEquals("String", task.getIdType());
  }

  @Test
  void shouldFailWithoutEntityName() {
    // Given
    task.setFields("name:String");

    // When/Then
    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      task.generateEntity();
    });

    // The exception message should contain "Entity generation failed" and the cause
    // should contain "Entity name is required"
    assertTrue(exception.getMessage().contains("Entity generation failed"));
    assertNotNull(exception.getCause());
    assertTrue(exception.getCause().getMessage().contains("Entity name is required"));
  }

  @Test
  void shouldFailWithoutFields() {
    // Given
    task.setEntityName("User");

    // When/Then
    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      task.generateEntity();
    });

    // The exception message should contain "Entity generation failed" and the cause
    // should contain "Entity fields are required"
    assertTrue(exception.getMessage().contains("Entity generation failed"));
    assertNotNull(exception.getCause());
    assertTrue(exception.getCause().getMessage().contains("Entity fields are required"));
  }

  @Test
  void shouldFailWithoutConfiguration() throws Exception {
    // Given
    // Delete the configuration file created in setUp
    Files.deleteIfExists(tempDir.resolve(".cleanarch.yml"));

    task.setEntityName("User");
    task.setFields("name:String");

    // When/Then
    Exception exception = assertThrows(Exception.class, () -> {
      task.generateEntity();
    });
    assertTrue(exception.getMessage().contains("Configuration validation failed") ||
        exception.getMessage().contains(".cleanarch.yml") ||
        exception.getCause() != null);
  }

  private void createBasicConfiguration() throws Exception {
    String yamlContent = """
        project:
          name: test-project
          basePackage: com.test
          architecture: hexagonal-single
          paradigm: reactive
          framework: spring
          pluginVersion: 1.0.0
          createdAt: '2024-01-01T00:00:00'
        architecture:
          type: hexagonal-single
          paradigm: reactive
          framework: spring
          adaptersAsModules: false
        """;
    Files.writeString(tempDir.resolve(".cleanarch.yml"), yamlContent);
  }
}
