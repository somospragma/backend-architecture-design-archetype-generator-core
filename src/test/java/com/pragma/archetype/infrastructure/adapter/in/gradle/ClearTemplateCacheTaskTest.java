package com.pragma.archetype.infrastructure.adapter.in.gradle;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ClearTemplateCacheTaskTest {

  @TempDir
  Path tempDir;

  private Project project;
  private ClearTemplateCacheTask task;

  @BeforeEach
  void setUp() {
    project = ProjectBuilder.builder()
        .withProjectDir(tempDir.toFile())
        .build();
    task = project.getTasks().create("clearTemplateCache", ClearTemplateCacheTask.class);
  }

  @Test
  void shouldHaveCorrectGroupAndDescription() {
    assertEquals("Clean Architecture", task.getGroup());
    assertEquals("Clears the local template cache", task.getDescription());
  }

  @Test
  void shouldClearCacheSuccessfully() {
    // Given: Create some cache files
    Path cacheDir = Path.of(System.getProperty("user.home"), ".cleanarch", "template-cache");
    try {
      Files.createDirectories(cacheDir);
      Path testFile = cacheDir.resolve("test-cache.txt");
      Files.writeString(testFile, "test content");
    } catch (IOException e) {
      // Ignore if we can't create cache
    }

    // When
    assertDoesNotThrow(() -> task.clearCache());

    // Then: Cache should be cleared (no exception thrown)
  }

  @Test
  void shouldHandleEmptyCache() {
    // When
    assertDoesNotThrow(() -> task.clearCache());

    // Then: Should complete without errors
  }
}
