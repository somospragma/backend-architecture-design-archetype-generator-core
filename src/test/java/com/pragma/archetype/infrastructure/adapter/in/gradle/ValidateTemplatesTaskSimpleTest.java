package com.pragma.archetype.infrastructure.adapter.in.gradle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ValidateTemplatesTaskSimpleTest {

  private Project project;
  private ValidateTemplatesTask task;

  @BeforeEach
  void setUp() {
    project = ProjectBuilder.builder().build();
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
    // Verify default values
    assertEquals("", task.getArchitecture());
    assertEquals("", task.getAdapter());
  }
}
