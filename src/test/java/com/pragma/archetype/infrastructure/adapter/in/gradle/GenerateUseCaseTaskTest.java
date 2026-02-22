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

class GenerateUseCaseTaskTest {

  @TempDir
  Path tempDir;

  private Project project;
  private GenerateUseCaseTask task;

  @BeforeEach
  void setUp() {
    project = ProjectBuilder.builder()
        .withProjectDir(tempDir.toFile())
        .build();
    task = project.getTasks().create("generateUseCase", GenerateUseCaseTask.class);
  }

  @Test
  void shouldSetAndGetUseCaseName() {
    task.setUseCaseName("CreateUser");
    assertEquals("CreateUser", task.getUseCaseName());
  }

  @Test
  void shouldSetAndGetMethods() {
    task.setMethods("execute:User:userId:String");
    assertEquals("execute:User:userId:String", task.getMethods());
  }

  @Test
  void shouldSetAndGetPackageName() {
    task.setPackageName("com.test.domain.port.in");
    assertEquals("com.test.domain.port.in", task.getPackageName());
  }

  @Test
  void shouldSetAndGetGeneratePort() {
    task.setGeneratePort(false);
    assertFalse(task.getGeneratePort());
  }

  @Test
  void shouldSetAndGetGenerateImpl() {
    task.setGenerateImpl(false);
    assertFalse(task.getGenerateImpl());
  }

  @Test
  void shouldHaveDefaultValues() {
    assertEquals("", task.getUseCaseName());
    assertEquals("", task.getMethods());
    assertTrue(task.getGeneratePort());
    assertTrue(task.getGenerateImpl());
  }

  @Test
  void shouldFailWithoutUseCaseName() {
    // Given
    task.setMethods("execute:User");

    // When/Then
    Exception exception = assertThrows(Exception.class, () -> {
      task.generateUseCase();
    });
    assertTrue(exception.getMessage().contains("Use case name is required") ||
        exception.getCause() != null && exception.getCause().getMessage().contains("Use case name is required"));
  }

  @Test
  void shouldFailWithoutMethods() {
    // Given
    task.setUseCaseName("CreateUser");

    // When/Then
    Exception exception = assertThrows(Exception.class, () -> {
      task.generateUseCase();
    });
    assertTrue(exception.getMessage().contains("Methods are required") ||
        exception.getCause() != null && exception.getCause().getMessage().contains("Methods are required"));
  }
}
