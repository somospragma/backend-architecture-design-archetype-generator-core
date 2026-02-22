package com.pragma.archetype.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.pragma.archetype.infrastructure.adapter.in.gradle.ClearTemplateCacheTask;
import com.pragma.archetype.infrastructure.adapter.in.gradle.GenerateEntityTask;
import com.pragma.archetype.infrastructure.adapter.in.gradle.GenerateInputAdapterTask;
import com.pragma.archetype.infrastructure.adapter.in.gradle.GenerateOutputAdapterTask;
import com.pragma.archetype.infrastructure.adapter.in.gradle.GenerateUseCaseTask;
import com.pragma.archetype.infrastructure.adapter.in.gradle.InitCleanArchTask;
import com.pragma.archetype.infrastructure.adapter.in.gradle.UpdateTemplatesTask;
import com.pragma.archetype.infrastructure.adapter.in.gradle.ValidateTemplatesTask;

class CleanArchPluginTest {

  private Project project;
  private CleanArchPlugin plugin;

  @BeforeEach
  void setUp() {
    project = ProjectBuilder.builder().build();
    plugin = new CleanArchPlugin();
  }

  @Test
  void apply_shouldRegisterAllTasks() {
    // When
    plugin.apply(project);

    // Then
    assertNotNull(project.getTasks().findByName("initCleanArch"));
    assertNotNull(project.getTasks().findByName("generateEntity"));
    assertNotNull(project.getTasks().findByName("generateUseCase"));
    assertNotNull(project.getTasks().findByName("generateOutputAdapter"));
    assertNotNull(project.getTasks().findByName("generateInputAdapter"));
    assertNotNull(project.getTasks().findByName("updateTemplates"));
    assertNotNull(project.getTasks().findByName("clearTemplateCache"));
    assertNotNull(project.getTasks().findByName("validateTemplates"));
  }

  @Test
  void apply_shouldRegisterInitCleanArchTaskWithCorrectType() {
    // When
    plugin.apply(project);

    // Then
    Task task = project.getTasks().findByName("initCleanArch");
    assertInstanceOf(InitCleanArchTask.class, task);
    assertEquals("clean architecture", task.getGroup());
    assertEquals("Initialize a clean architecture project", task.getDescription());
  }

  @Test
  void apply_shouldRegisterGenerateEntityTaskWithCorrectType() {
    // When
    plugin.apply(project);

    // Then
    Task task = project.getTasks().findByName("generateEntity");
    assertInstanceOf(GenerateEntityTask.class, task);
    assertEquals("clean architecture", task.getGroup());
    assertEquals("Generate a domain entity", task.getDescription());
  }

  @Test
  void apply_shouldRegisterGenerateUseCaseTaskWithCorrectType() {
    // When
    plugin.apply(project);

    // Then
    Task task = project.getTasks().findByName("generateUseCase");
    assertInstanceOf(GenerateUseCaseTask.class, task);
    assertEquals("clean architecture", task.getGroup());
    assertEquals("Generate a use case (port and implementation)", task.getDescription());
  }

  @Test
  void apply_shouldRegisterGenerateOutputAdapterTaskWithCorrectType() {
    // When
    plugin.apply(project);

    // Then
    Task task = project.getTasks().findByName("generateOutputAdapter");
    assertInstanceOf(GenerateOutputAdapterTask.class, task);
    assertEquals("clean architecture", task.getGroup());
    assertEquals("Generate an output adapter (Redis, MongoDB, etc.)", task.getDescription());
  }

  @Test
  void apply_shouldRegisterGenerateInputAdapterTaskWithCorrectType() {
    // When
    plugin.apply(project);

    // Then
    Task task = project.getTasks().findByName("generateInputAdapter");
    assertInstanceOf(GenerateInputAdapterTask.class, task);
    assertEquals("clean architecture", task.getGroup());
    assertEquals("Generate an input adapter (REST controller, GraphQL resolver, etc.)", task.getDescription());
  }

  @Test
  void apply_shouldRegisterUpdateTemplatesTaskWithCorrectType() {
    // When
    plugin.apply(project);

    // Then
    Task task = project.getTasks().findByName("updateTemplates");
    assertInstanceOf(UpdateTemplatesTask.class, task);
    assertEquals("clean architecture", task.getGroup());
    assertEquals("Update templates by clearing cache and forcing re-download", task.getDescription());
  }

  @Test
  void apply_shouldRegisterClearTemplateCacheTaskWithCorrectType() {
    // When
    plugin.apply(project);

    // Then
    Task task = project.getTasks().findByName("clearTemplateCache");
    assertInstanceOf(ClearTemplateCacheTask.class, task);
    assertEquals("clean architecture", task.getGroup());
    assertEquals("Clear the local template cache", task.getDescription());
  }

  @Test
  void apply_shouldRegisterValidateTemplatesTaskWithCorrectType() {
    // When
    plugin.apply(project);

    // Then
    Task task = project.getTasks().findByName("validateTemplates");
    assertInstanceOf(ValidateTemplatesTask.class, task);
    assertEquals("clean architecture", task.getGroup());
    assertEquals("Validate architecture and adapter templates", task.getDescription());
  }
}
