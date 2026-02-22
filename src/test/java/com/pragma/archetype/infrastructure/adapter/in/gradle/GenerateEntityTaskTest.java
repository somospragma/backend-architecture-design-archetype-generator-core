package com.pragma.archetype.infrastructure.adapter.in.gradle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
  void setUp() {
    project = ProjectBuilder.builder()
        .withProjectDir(tempDir.toFile())
        .build();
    task = project.getTasks().create("generateEntity", GenerateEntityTask.class);
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
    Exception exception = assertThrows(Exception.class, () -> {
      task.generateEntity();
    });
    assertTrue(exception.getMessage().contains("Entity name is required") ||
        exception.getCause() != null && exception.getCause().getMessage().contains("Entity name is required"));
  }

  @Test
  void shouldFailWithoutFields() {
    // Given
    task.setEntityName("User");

    // When/Then
    Exception exception = assertThrows(Exception.class, () -> {
      task.generateEntity();
    });
    assertTrue(exception.getMessage().contains("Entity fields are required") ||
        exception.getCause() != null && exception.getCause().getMessage().contains("Entity fields are required"));
  }

  @Test
  void shouldFailWithoutConfiguration() {
    // Given
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
}
