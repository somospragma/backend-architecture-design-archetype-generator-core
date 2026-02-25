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

class GenerateInputAdapterTaskTest {

  @TempDir
  Path tempDir;

  private Project project;
  private GenerateInputAdapterTask task;

  @BeforeEach
  void setUp() throws Exception {
    project = ProjectBuilder.builder()
        .withProjectDir(tempDir.toFile())
        .build();
    task = project.getTasks().create("generateInputAdapter", GenerateInputAdapterTask.class);

    // Create a valid .cleanarch.yml configuration for tests that need it
    createBasicConfiguration();
  }

  @Test
  void shouldSetAndGetAdapterName() {
    task.setAdapterName("UserController");
    assertEquals("UserController", task.getAdapterName());
  }

  @Test
  void shouldSetAndGetUseCaseName() {
    task.setUseCaseName("CreateUserUseCase");
    assertEquals("CreateUserUseCase", task.getUseCaseName());
  }

  @Test
  void shouldSetAndGetEndpoints() {
    task.setEndpoints("/users:POST:create:User");
    assertEquals("/users:POST:create:User", task.getEndpoints());
  }

  @Test
  void shouldSetAndGetPackageName() {
    task.setPackageName("com.test.infrastructure.rest");
    assertEquals("com.test.infrastructure.rest", task.getPackageName());
  }

  @Test
  void shouldSetAndGetType() {
    task.setType("graphql");
    assertEquals("graphql", task.getType());
  }

  @Test
  void shouldHaveDefaultValues() {
    assertEquals("", task.getAdapterName());
    assertEquals("", task.getUseCaseName());
    assertEquals("", task.getEndpoints());
    assertEquals("rest", task.getType());
  }

  @Test
  void shouldFailWithoutAdapterName() {
    // Given
    task.setUseCaseName("CreateUserUseCase");
    task.setEndpoints("/users:POST:create:User");
    task.setPackageName("com.test");

    // When/Then
    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      task.generateInputAdapter();
    });
    assertTrue(exception.getCause() != null && exception.getCause().getMessage().contains("Adapter name is required"),
        "Expected cause to contain 'Adapter name is required' but was: " +
            (exception.getCause() != null ? exception.getCause().getMessage() : "null"));
  }

  @Test
  void shouldFailWithoutUseCaseName() {
    // Given
    task.setAdapterName("UserController");
    task.setEndpoints("/users:POST:create:User");
    task.setPackageName("com.test");

    // When/Then
    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      task.generateInputAdapter();
    });
    assertTrue(exception.getCause() != null && exception.getCause().getMessage().contains("Use case name is required"),
        "Expected cause to contain 'Use case name is required' but was: " +
            (exception.getCause() != null ? exception.getCause().getMessage() : "null"));
  }

  @Test
  void shouldFailWithoutEndpoints() {
    // Given
    task.setAdapterName("UserController");
    task.setUseCaseName("CreateUserUseCase");
    task.setPackageName("com.test");

    // When/Then
    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      task.generateInputAdapter();
    });
    assertTrue(exception.getCause() != null && exception.getCause().getMessage().contains("Endpoints are required"),
        "Expected cause to contain 'Endpoints are required' but was: " +
            (exception.getCause() != null ? exception.getCause().getMessage() : "null"));
  }

  @Test
  void shouldFailWithoutPackageName() {
    // Given
    task.setAdapterName("UserController");
    task.setUseCaseName("CreateUserUseCase");
    task.setEndpoints("/users:POST:create:User");

    // When/Then
    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      task.generateInputAdapter();
    });
    assertTrue(exception.getCause() != null && exception.getCause().getMessage().contains("Package name is required"),
        "Expected cause to contain 'Package name is required' but was: " +
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
