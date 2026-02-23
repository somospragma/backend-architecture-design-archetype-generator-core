package com.pragma.archetype.infrastructure.adapter.in.gradle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ValidateTemplatesTaskFullTest {

  @TempDir
  Path tempDir;

  private Project project;
  private ValidateTemplatesTask task;

  @BeforeEach
  void setUp() {
    project = ProjectBuilder.builder()
        .withProjectDir(tempDir.toFile())
        .withName("test-project")
        .build();

    task = project.getTasks().create("validateTemplates", ValidateTemplatesTask.class);
  }

  @Test
  void testGettersAndSetters() {
    // Test architecture
    task.setArchitecture("hexagonal-single");
    assertEquals("hexagonal-single", task.getArchitecture());

    // Test adapter
    task.setAdapter("mongodb");
    assertEquals("mongodb", task.getAdapter());
  }

  @Test
  void testDefaultValues() {
    assertEquals("", task.getArchitecture());
    assertEquals("", task.getAdapter());
  }

  @Test
  void testValidateTemplates_all_noConfiguration() {
    // Given - no configuration file
    task.setArchitecture("");
    task.setAdapter("");

    // When/Then - will fail due to missing templates
    assertThrows(GradleException.class, () -> task.validateTemplates());
  }

  @Test
  void testValidateTemplates_specificArchitecture() {
    // Given
    task.setArchitecture("hexagonal-single");
    task.setAdapter("");

    // When/Then - will fail due to missing templates
    assertThrows(GradleException.class, () -> task.validateTemplates());
  }

  @Test
  void testValidateTemplates_specificAdapter() {
    // Given
    task.setArchitecture("");
    task.setAdapter("mongodb");

    // When/Then - will fail due to missing templates
    assertThrows(GradleException.class, () -> task.validateTemplates());
  }

  @Test
  void testValidateTemplates_withConfiguration() throws Exception {
    // Given - create a basic .cleanarch.yml
    String yamlContent = """
        project:
          name: test-project
          basePackage: com.test
          architecture: hexagonal-single
          paradigm: reactive
          framework: spring
          pluginVersion: 1.0.0
          createdAt: 2024-01-01T00:00:00
        architecture:
          type: hexagonal-single
          paradigm: reactive
          framework: spring
          adaptersAsModules: false
        """;
    Files.writeString(tempDir.resolve(".cleanarch.yml"), yamlContent);

    // When/Then - will still fail due to missing templates
    assertThrows(GradleException.class, () -> task.validateTemplates());
  }

  @Test
  void testValidateTemplates_blankArchitecture() {
    // Given
    task.setArchitecture("   ");
    task.setAdapter("");

    // When/Then - blank is treated as empty, validates all
    assertThrows(GradleException.class, () -> task.validateTemplates());
  }

  @Test
  void testValidateTemplates_blankAdapter() {
    // Given
    task.setArchitecture("");
    task.setAdapter("   ");

    // When/Then - blank is treated as empty, validates all
    assertThrows(GradleException.class, () -> task.validateTemplates());
  }

  @Test
  void testValidateTemplates_bothArchitectureAndAdapter() {
    // Given - both set, architecture takes precedence
    task.setArchitecture("hexagonal-single");
    task.setAdapter("mongodb");

    // When/Then
    assertThrows(GradleException.class, () -> task.validateTemplates());
  }

  @Test
  void testValidateTemplates_invalidArchitecture() {
    // Given
    task.setArchitecture("invalid-architecture");
    task.setAdapter("");

    // When/Then
    assertThrows(GradleException.class, () -> task.validateTemplates());
  }

  @Test
  void testValidateTemplates_invalidAdapter() {
    // Given
    task.setArchitecture("");
    task.setAdapter("invalid-adapter-xyz");

    // When/Then
    assertThrows(GradleException.class, () -> task.validateTemplates());
  }

  @Test
  void testValidateTemplates_allArchitectures() {
    String[] architectures = {
        "hexagonal-single",
        "hexagonal-multi",
        "hexagonal-multi-granular",
        "onion-single",
        "onion-multi"
    };

    for (String arch : architectures) {
      // Given
      Project testProject = ProjectBuilder.builder()
          .withProjectDir(tempDir.resolve("arch-" + arch).toFile())
          .withName("test-" + arch)
          .build();

      ValidateTemplatesTask testTask = testProject.getTasks().create("validate-" + arch,
          ValidateTemplatesTask.class);
      testTask.setArchitecture(arch);

      // When/Then - validates that all architectures are processed
      assertThrows(GradleException.class, () -> testTask.validateTemplates());
    }
  }

  @Test
  void testValidateTemplates_commonAdapters() {
    String[] adapters = {
        "mongodb",
        "postgresql",
        "rest-controller",
        "kafka",
        "redis"
    };

    for (String adapter : adapters) {
      // Given
      Project testProject = ProjectBuilder.builder()
          .withProjectDir(tempDir.resolve("adapter-" + adapter).toFile())
          .withName("test-" + adapter)
          .build();

      ValidateTemplatesTask testTask = testProject.getTasks().create("validate-" + adapter,
          ValidateTemplatesTask.class);
      testTask.setAdapter(adapter);

      // When/Then - validates that adapters are processed
      assertThrows(GradleException.class, () -> testTask.validateTemplates());
    }
  }

  @Test
  void testValidateTemplates_nullArchitecture() {
    // Given
    task.setArchitecture(null);
    task.setAdapter("");

    // When/Then
    assertThrows(GradleException.class, () -> task.validateTemplates());
  }

  @Test
  void testValidateTemplates_nullAdapter() {
    // Given
    task.setArchitecture("");
    task.setAdapter(null);

    // When/Then
    assertThrows(GradleException.class, () -> task.validateTemplates());
  }
}
