package com.pragma.archetype.domain.port.out;

import java.nio.file.Path;
import java.util.Optional;

import com.pragma.archetype.domain.model.ProjectConfig;

/**
 * Output port for reading and writing project configuration.
 * This port abstracts the configuration persistence mechanism.
 */
public interface ConfigurationPort {

  /**
   * Reads the project configuration from the specified directory.
   * 
   * @param projectPath the root directory of the project
   * @return Optional containing the configuration if it exists, empty otherwise
   */
  Optional<ProjectConfig> readConfiguration(Path projectPath);

  /**
   * Writes the project configuration to the specified directory.
   * Creates a .cleanarch.yml file in the project root.
   * 
   * @param projectPath the root directory of the project
   * @param config      the configuration to write
   */
  void writeConfiguration(Path projectPath, ProjectConfig config);

  /**
   * Checks if a configuration file exists in the project.
   * 
   * @param projectPath the root directory of the project
   * @return true if configuration exists, false otherwise
   */
  boolean configurationExists(Path projectPath);

  /**
   * Deletes the configuration file from the project.
   * 
   * @param projectPath the root directory of the project
   */
  void deleteConfiguration(Path projectPath);
}
