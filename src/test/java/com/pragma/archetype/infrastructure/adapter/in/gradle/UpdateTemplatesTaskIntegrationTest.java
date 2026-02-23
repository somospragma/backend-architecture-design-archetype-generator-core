package com.pragma.archetype.infrastructure.adapter.in.gradle;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UpdateTemplatesTaskIntegrationTest {

  @TempDir
  Path tempDir;

  private Project project;
  private UpdateTemplatesTask task;

  @BeforeEach
  void setUp() {
    project = ProjectBuilder.builder()
        .withProjectDir(tempDir.toFile())
        .withName("test-project")
        .build();

    task = project.getTasks().create("updateTemplates", UpdateTemplatesTask.class);
  }

  @Test
  void testTaskExecution_withoutConfiguration() throws Exception {
    // Given - no .cleanarch.yml file

    // When/Then - should fail gracefully
    Exception exception = assertThrows(Exception.class, () -> task.updateTemplates());
    assertNotNull(exception);
  }

  @Test
  void testTaskExecution_withConfiguration() throws Exception {
    // Given - create a basic .cleanarch.yml
    String yamlContent = """
        project:
          name: test-project
          basePackage: com.test
          architecture: hexagonal-single
          paradigm: reactive
          framework: spring
          pluginVersion: 1.0.0
        templates:
          mode: remote
          repository: https://github.com/test/templates
          branch: main
        """;
    Files.writeString(tempDir.resolve(".cleanarch.yml"), yamlContent);

    // When/Then - should execute without throwing
    assertDoesNotThrow(() -> task.updateTemplates());
  }
}
