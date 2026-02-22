package com.pragma.archetype.infrastructure.adapter.in.gradle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
  void setUp() {
    project = ProjectBuilder.builder()
        .withProjectDir(tempDir.toFile())
        .build();
    task = project.getTasks().create("generateInputAdapter", GenerateInputAdapterTask.class);
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
    Exception exception = assertThrows(Exception.class, () -> {
      task.generateInputAdapter();
    });
    assertTrue(exception.getMessage().contains("Adapter name is required") ||
        exception.getCause() != null && exception.getCause().getMessage().contains("Adapter name is required"));
  }

  @Test
  void shouldFailWithoutUseCaseName() {
    // Given
    task.setAdapterName("UserController");
    task.setEndpoints("/users:POST:create:User");
    task.setPackageName("com.test");

    // When/Then
    Exception exception = assertThrows(Exception.class, () -> {
      task.generateInputAdapter();
    });
    assertTrue(exception.getMessage().contains("Use case name is required") ||
        exception.getCause() != null && exception.getCause().getMessage().contains("Use case name is required"));
  }

  @Test
  void shouldFailWithoutEndpoints() {
    // Given
    task.setAdapterName("UserController");
    task.setUseCaseName("CreateUserUseCase");
    task.setPackageName("com.test");

    // When/Then
    Exception exception = assertThrows(Exception.class, () -> {
      task.generateInputAdapter();
    });
    assertTrue(exception.getMessage().contains("Endpoints are required") ||
        exception.getCause() != null && exception.getCause().getMessage().contains("Endpoints are required"));
  }

  @Test
  void shouldFailWithoutPackageName() {
    // Given
    task.setAdapterName("UserController");
    task.setUseCaseName("CreateUserUseCase");
    task.setEndpoints("/users:POST:create:User");

    // When/Then
    Exception exception = assertThrows(Exception.class, () -> {
      task.generateInputAdapter();
    });
    assertTrue(exception.getMessage().contains("Package name is required") ||
        exception.getCause() != null && exception.getCause().getMessage().contains("Package name is required"));
  }
}
