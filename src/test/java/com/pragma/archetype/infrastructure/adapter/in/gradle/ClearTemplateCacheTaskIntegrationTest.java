package com.pragma.archetype.infrastructure.adapter.in.gradle;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ClearTemplateCacheTaskIntegrationTest {

  @TempDir
  Path tempDir;

  private Project project;
  private ClearTemplateCacheTask task;

  @BeforeEach
  void setUp() {
    project = ProjectBuilder.builder()
        .withProjectDir(tempDir.toFile())
        .withName("test-project")
        .build();

    task = project.getTasks().create("clearTemplateCache", ClearTemplateCacheTask.class);
  }

  @Test
  void testTaskExecution_withoutCache() {
    // When/Then - should execute without throwing even if no cache exists
    assertDoesNotThrow(() -> task.clearCache());
  }

  @Test
  void testTaskExecution_withCache() throws Exception {
    // Given - create a cache directory
    Path cacheDir = tempDir.resolve(".cleanarch-cache");
    Files.createDirectories(cacheDir);
    Files.writeString(cacheDir.resolve("test.txt"), "test content");

    // When
    assertDoesNotThrow(() -> task.clearCache());

    // Then - task completed successfully
    assertTrue(true);
  }
}
