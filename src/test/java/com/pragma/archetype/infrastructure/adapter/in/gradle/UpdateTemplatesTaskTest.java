package com.pragma.archetype.infrastructure.adapter.in.gradle;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UpdateTemplatesTaskTest {

  @TempDir
  Path tempDir;

  private Project project;
  private UpdateTemplatesTask task;

  @BeforeEach
  void setUp() {
    project = ProjectBuilder.builder()
        .withProjectDir(tempDir.toFile())
        .build();
    task = project.getTasks().create("updateTemplates", UpdateTemplatesTask.class);
  }

  @Test
  void shouldHaveCorrectGroupAndDescription() {
    assertEquals("Clean Architecture", task.getGroup());
    assertEquals("Updates templates by clearing cache and forcing re-download", task.getDescription());
  }

  @Test
  void shouldExecuteWithoutConfiguration() {
    // When/Then: Should not throw exception even without .cleanarch.yml
    assertDoesNotThrow(() -> task.updateTemplates());
  }
}
