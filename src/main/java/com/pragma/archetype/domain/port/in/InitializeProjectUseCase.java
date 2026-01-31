package com.pragma.archetype.domain.port.in;

import java.nio.file.Path;
import java.util.List;

import com.pragma.archetype.domain.model.GeneratedFile;
import com.pragma.archetype.domain.model.ProjectConfig;

/**
 * Use case for initializing a new clean architecture project.
 * This is an input port (driven by the Gradle task).
 */
public interface InitializeProjectUseCase {

  /**
   * Exception thrown when project initialization fails.
   */
  class ProjectInitializationException extends RuntimeException {
    public ProjectInitializationException(String message) {
      super(message);
    }

    public ProjectInitializationException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  /**
   * Initializes a new project with the given configuration.
   *
   * @param projectPath the root path where the project will be created
   * @param config      the project configuration
   * @return list of generated files
   * @throws ProjectInitializationException if initialization fails
   */
  List<GeneratedFile> initialize(Path projectPath, ProjectConfig config);
}
