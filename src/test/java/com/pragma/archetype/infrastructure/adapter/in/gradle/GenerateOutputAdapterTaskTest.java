package com.pragma.archetype.infrastructure.adapter.in.gradle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GenerateOutputAdapterTaskTest {

  @TempDir
  Path tempDir;

  private Project project;
  private GenerateOutputAdapterTask task;

  @BeforeEach
  void setUp() throws Exception {
    project = ProjectBuilder.builder()
        .withProjectDir(tempDir.toFile())
        .build();
    task = project.getTasks().create("generateOutputAdapter", GenerateOutputAdapterTask.class);

    // Create a valid .cleanarch.yml configuration for tests that need it
    createBasicConfiguration();
  }

  @Test
  void shouldSetAndGetAdapterName() {
    task.setAdapterName("UserRepository");
    assertEquals("UserRepository", task.getAdapterName());
  }

  @Test
  void shouldSetAndGetEntityName() {
    task.setEntityName("User");
    assertEquals("User", task.getEntityName());
  }

  @Test
  void shouldSetAndGetType() {
    task.setType("mongodb");
    assertEquals("mongodb", task.getType());
  }

  @Test
  void shouldSetAndGetPackageName() {
    task.setPackageName("com.test.infrastructure.mongodb");
    assertEquals("com.test.infrastructure.mongodb", task.getPackageName());
  }

  @Test
  void shouldSetAndGetMethods() {
    task.setMethods("findById:User:id:String");
    assertEquals("findById:User:id:String", task.getMethods());
  }

  @Test
  void shouldHaveDefaultValues() {
    assertEquals("", task.getAdapterName());
    assertEquals("", task.getEntityName());
    assertEquals("redis", task.getType());
    assertEquals("", task.getMethods());
  }

  @Test
  void shouldFailWithoutAdapterName() {
    // Given
    task.setEntityName("User");

    // When/Then
    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      task.generateAdapter();
    });
    assertTrue(exception.getCause() != null && exception.getCause().getMessage().contains("Adapter name is required"),
        "Expected cause to contain 'Adapter name is required' but was: " +
            (exception.getCause() != null ? exception.getCause().getMessage() : "null"));
  }

  @Test
  void shouldFailWithoutEntityName() {
    // Given
    task.setAdapterName("UserRepository");

    // When/Then
    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      task.generateAdapter();
    });
    assertTrue(exception.getCause() != null && exception.getCause().getMessage().contains("Entity name is required"),
        "Expected cause to contain 'Entity name is required' but was: " +
            (exception.getCause() != null ? exception.getCause().getMessage() : "null"));
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
