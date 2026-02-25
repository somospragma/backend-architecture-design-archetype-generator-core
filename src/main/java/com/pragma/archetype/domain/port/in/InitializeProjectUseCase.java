package com.pragma.archetype.domain.port.in;

import java.nio.file.Path;

import com.pragma.archetype.application.usecase.InitializeProjectUseCaseImpl.InitializationResult;
import com.pragma.archetype.domain.model.config.ProjectConfig;

/**
 * Use case for initializing a new clean architecture project.
 * This is an input port (driven by the Gradle task).
 */
public interface InitializeProjectUseCase {

  /**
   * Initializes a new project with the given configuration.
   *
   * @param projectPath the root path where the project will be created
   * @param config      the project configuration
   * @return initialization result with generated files or errors
   */
  InitializationResult execute(Path projectPath, ProjectConfig config);
}
