package com.pragma.archetype.infrastructure.adapter.in.gradle;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Files;
import java.nio.file.Path;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ClearTemplateCacheTaskExtendedTest {

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
  void shouldHaveTaskName() {
    assertNotNull(task.getName());
  }

  @Test
  void shouldHaveProject() {
    assertNotNull(task.getProject());
  }

  @Test
  void shouldExecuteWithoutCache() {
    // When/Then
    assertDoesNotThrow(() -> task.clearCache());
  }

  @Test
  void shouldExecuteWithEmptyCache() throws Exception {
    // Given
    Path cacheDir = tempDir.resolve(".gradle").resolve("template-cache");
    Files.createDirectories(cacheDir);

    // When/Then
    assertDoesNotThrow(() -> task.clearCache());
  }

  @Test
  void shouldExecuteWithCacheFiles() throws Exception {
    // Given
    Path cacheDir = tempDir.resolve(".gradle").resolve("template-cache");
    Files.createDirectories(cacheDir);
    Files.writeString(cacheDir.resolve("test.txt"), "test content");

    // When/Then
    assertDoesNotThrow(() -> task.clearCache());
  }

  @Test
  void shouldExecuteWithNestedCacheFiles() throws Exception {
    // Given
    Path cacheDir = tempDir.resolve(".gradle").resolve("template-cache");
    Path subDir = cacheDir.resolve("subdir");
    Files.createDirectories(subDir);
    Files.writeString(subDir.resolve("test.txt"), "test content");

    // When/Then
    assertDoesNotThrow(() -> task.clearCache());
  }

  @Test
  void shouldExecuteMultipleTimes() throws Exception {
    // Given
    Path cacheDir = tempDir.resolve(".gradle").resolve("template-cache");
    Files.createDirectories(cacheDir);
    Files.writeString(cacheDir.resolve("test.txt"), "test content");

    // When/Then
    assertDoesNotThrow(() -> {
      task.clearCache();
      task.clearCache();
      task.clearCache();
    });
  }
}
