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

class InitCleanArchTaskTest {

  @TempDir
  Path tempDir;

  private Project project;
  private InitCleanArchTask task;

  @BeforeEach
  void setUp() {
    project = ProjectBuilder.builder()
        .withProjectDir(tempDir.toFile())
        .build();
    task = project.getTasks().create("initCleanArch", InitCleanArchTask.class);
  }

  @Test
  void shouldSetAndGetArchitecture() {
    task.setArchitecture("hexagonal-multi");
    assertEquals("hexagonal-multi", task.getArchitecture());
  }

  @Test
  void shouldSetAndGetParadigm() {
    task.setParadigm("imperative");
    assertEquals("imperative", task.getParadigm());
  }

  @Test
  void shouldSetAndGetFramework() {
    task.setFramework("quarkus");
    assertEquals("quarkus", task.getFramework());
  }

  @Test
  void shouldSetAndGetPackageName() {
    task.setPackageName("com.test.project");
    assertEquals("com.test.project", task.getPackageName());
  }

  @Test
  void shouldHaveDefaultValues() {
    assertEquals("hexagonal-single", task.getArchitecture());
    assertEquals("reactive", task.getParadigm());
    assertEquals("spring", task.getFramework());
  }

  @Test
  void shouldFailWithoutPackageName() {
    // When/Then
    Exception exception = assertThrows(Exception.class, () -> {
      task.initializeProject();
    });
    assertTrue(exception.getMessage().contains("Package name is required") ||
        exception.getCause() != null && exception.getCause().getMessage().contains("Package name is required"));
  }

  @Test
  void shouldFailWithInvalidArchitecture() {
    // Given
    task.setPackageName("com.test.project");
    task.setArchitecture("invalid-arch");

    // When/Then
    Exception exception = assertThrows(Exception.class, () -> {
      task.initializeProject();
    });
    assertTrue(exception.getMessage().contains("Invalid architecture") ||
        exception.getCause() != null && exception.getCause().getMessage().contains("Invalid architecture"));
  }

  @Test
  void shouldFailWithInvalidParadigm() {
    // Given
    task.setPackageName("com.test.project");
    task.setParadigm("invalid-paradigm");

    // When/Then
    Exception exception = assertThrows(Exception.class, () -> {
      task.initializeProject();
    });
    assertTrue(exception.getMessage().contains("Invalid paradigm") ||
        exception.getCause() != null && exception.getCause().getMessage().contains("Invalid paradigm"));
  }

  @Test
  void shouldFailWithInvalidFramework() {
    // Given
    task.setPackageName("com.test.project");
    task.setFramework("invalid-framework");

    // When/Then
    Exception exception = assertThrows(Exception.class, () -> {
      task.initializeProject();
    });
    assertTrue(exception.getMessage().contains("Invalid framework") ||
        exception.getCause() != null && exception.getCause().getMessage().contains("Invalid framework"));
  }
}
