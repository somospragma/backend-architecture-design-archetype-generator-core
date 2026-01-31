package com.pragma.archetype.infrastructure.config;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import com.pragma.archetype.infrastructure.adapter.in.gradle.GenerateEntityTask;
import com.pragma.archetype.infrastructure.adapter.in.gradle.GenerateInputAdapterTask;
import com.pragma.archetype.infrastructure.adapter.in.gradle.GenerateOutputAdapterTask;
import com.pragma.archetype.infrastructure.adapter.in.gradle.GenerateUseCaseTask;
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

    // Register generateEntity task
    project.getTasks().register("generateEntity", GenerateEntityTask.class, task -> {
      task.setGroup("clean architecture");
      task.setDescription("Generate a domain entity");
    });

    // Register generateUseCase task
    project.getTasks().register("generateUseCase", GenerateUseCaseTask.class, task -> {
      task.setGroup("clean architecture");
      task.setDescription("Generate a use case (port and implementation)");
    });

    // Register generateOutputAdapter task
    project.getTasks().register("generateOutputAdapter", GenerateOutputAdapterTask.class, task -> {
      task.setGroup("clean architecture");
      task.setDescription("Generate an output adapter (Redis, MongoDB, etc.)");
    });

    // Register generateInputAdapter task
    project.getTasks().register("generateInputAdapter", GenerateInputAdapterTask.class, task -> {
      task.setGroup("clean architecture");
      task.setDescription("Generate an input adapter (REST controller, GraphQL resolver, etc.)");
    });

    // Future tasks will be registered here:
    // - listComponents
  }
}
