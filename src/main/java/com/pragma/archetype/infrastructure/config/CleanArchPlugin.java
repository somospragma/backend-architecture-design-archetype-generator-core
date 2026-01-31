package com.pragma.archetype.infrastructure.config;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import com.pragma.archetype.infrastructure.adapter.in.gradle.GenerateEntityTask;
import com.pragma.archetype.infrastructure.adapter.in.gradle.InitCleanArchTask;

/**
 * Gradle plugin for Clean Architecture Generator.
 * Registers tasks and configures the plugin.
 */
public class CleanArchPlugin implements Plugin<Project> {

  @Override
  public void apply(Project project) {
    // Register initCleanArch task
    project.getTasks().register("initCleanArch", InitCleanArchTask.class, task -> {
      task.setGroup("clean architecture");
      task.setDescription("Initialize a clean architecture project");
    });

    project.getTasks().register("initCleanArch2", InitCleanArchTask.class, task -> {
      task.setGroup("clean architecture");
      task.setDescription("Initialize a clean architecture project");
    });

    project.getTasks().register("initCleanArch3", InitCleanArchTask.class, task -> {
      task.setGroup("clean architecture");
      task.setDescription("Initialize a clean architecture project");
    });

    // Register generateEntity task
    project.getTasks().register("taskEntityTest", GenerateEntityTask.class, task -> {
      task.setGroup("clean architecture");
      task.setDescription("Generate a domain entity");
    });

    // Future tasks will be registered here:
    // - generateUseCase
    // - generateOutputAdapter
    // - generateInputAdapter
    // - listComponents
  }
}
