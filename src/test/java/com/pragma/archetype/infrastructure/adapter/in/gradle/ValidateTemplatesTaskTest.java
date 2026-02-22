package com.pragma.archetype.infrastructure.adapter.in.gradle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ValidateTemplatesTaskTest {

  @TempDir
  Path tempDir;

  private Project project;
  private ValidateTemplatesTask task;

  @BeforeEach
  void setUp() {
    project = ProjectBuilder.builder()
        .withProjectDir(tempDir.toFile())
        .build();
    task = project.getTasks().create("validateTemplates", ValidateTemplatesTask.class);
  }

  @Test
  void shouldSetAndGetArchitecture() {
    task.setArchitecture("hexagonal-single");
    assertEquals("hexagonal-single", task.getArchitecture());
  }

  @Test
  void shouldSetAndGetAdapter() {
    task.setAdapter("mongodb");
    assertEquals("mongodb", task.getAdapter());
  }

  @Test
  void shouldHaveDefaultValues() {
    assertEquals("", task.getArchitecture());
    assertEquals("", task.getAdapter());
  }
}
