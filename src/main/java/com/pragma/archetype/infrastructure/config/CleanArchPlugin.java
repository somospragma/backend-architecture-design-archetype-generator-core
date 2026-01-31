package com.pragma.archetype.infrastructure.config;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

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

    // Future tasks will be registered here:
    // - generateEntity
    // - generateUseCase
    // - generateOutputAdapter
    // - generateInputAdapter
    // - listComponents
  }
}
