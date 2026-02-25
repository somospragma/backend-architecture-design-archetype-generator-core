package com.pragma.archetype.domain.service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.pragma.archetype.domain.model.ValidationResult;
import com.pragma.archetype.domain.model.config.ProjectConfig;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.port.out.FileSystemPort;

/**
 * Domain service responsible for validating project state before
 * initialization.
 * Ensures the project is in a valid state to be initialized with clean
 * architecture.
 */
public class ProjectValidator {

  private final FileSystemPort fileSystemPort;
  private final ConfigurationPort configurationPort;

  public ProjectValidator(FileSystemPort fileSystemPort, ConfigurationPort configurationPort) {
    this.fileSystemPort = fileSystemPort;
    this.configurationPort = configurationPort;
  }

  /**
   * Validates that the project can be initialized with clean architecture.
   * 
   * Validation rules:
   * 1. Project directory must exist
   * 2. Project must not already be initialized (no .cleanarch.yml)
   * 3. Project should be mostly empty (only build files allowed)
   * 4. Configuration parameters must be valid
   * 
   * @param projectPath the root directory of the project
   * @param config      the configuration to validate
   * @return ValidationResult with success status and any error messages
   */
  public ValidationResult validateForInitialization(Path projectPath, ProjectConfig config) {
    List<String> errors = new ArrayList<>();

    // 1. Check if project directory exists
    if (!fileSystemPort.directoryExists(projectPath)) {
      errors.add("Project directory does not exist: " + projectPath);
      return ValidationResult.failure(errors);
    }

    // 2. Check if project is already initialized
    if (configurationPort.configurationExists(projectPath)) {
      errors.add("Project is already initialized. Found .cleanarch.yml file.");
      errors.add("If you want to reinitialize, delete the .cleanarch.yml file first.");
      return ValidationResult.failure(errors);
    }

    // 3. Check if project is mostly empty
    ValidationResult emptyCheck = validateProjectIsEmpty(projectPath);
    if (!emptyCheck.valid()) {
      errors.addAll(emptyCheck.errors());
    }

    // 4. Validate configuration
    ValidationResult configCheck = validateConfiguration(config);
    if (!configCheck.valid()) {
      errors.addAll(configCheck.errors());
    }

    if (errors.isEmpty()) {
      return ValidationResult.success();
    }

    return ValidationResult.failure(errors);
  }

  /**
   * Validates that a component can be generated in the project.
   * 
   * @param projectPath the root directory of the project
   * @return ValidationResult with success status and any error messages
   */
  public ValidationResult validateForComponentGeneration(Path projectPath) {
    List<String> errors = new ArrayList<>();

    // 1. Check if project directory exists
    if (!fileSystemPort.directoryExists(projectPath)) {
      errors.add("Project directory does not exist: " + projectPath);
      return ValidationResult.failure(errors);
    }

    // 2. Check if project is initialized
    if (!configurationPort.configurationExists(projectPath)) {
      errors.add("Project is not initialized with clean architecture.");
      errors.add("Run 'initCleanArch' first to initialize the project.");
      return ValidationResult.failure(errors);
    }

    return ValidationResult.success();
  }

  /**
   * Validates that the project directory is mostly empty.
   * Allows only build files (build.gradle.kts, settings.gradle.kts, gradlew,
   * etc.)
   * 
   * @param projectPath the root directory of the project
   * @return ValidationResult with success status and any error messages
   */
  private ValidationResult validateProjectIsEmpty(Path projectPath) {
    List<String> errors = new ArrayList<>();

    List<Path> files = fileSystemPort.listFiles(projectPath);

    // Allowed files in an "empty" project
    List<String> allowedFiles = List.of(
        "build.gradle.kts",
        "build.gradle",
        "settings.gradle.kts",
        "settings.gradle",
        "gradlew",
        "gradlew.bat",
        ".gitignore",
        ".git",
        "gradle",
        ".gradle");

    List<Path> unexpectedFiles = files.stream()
        .filter(file -> {
          String fileName = file.getFileName().toString();
          return !allowedFiles.contains(fileName);
        })
        .toList();

    if (!unexpectedFiles.isEmpty()) {
      errors.add("Project directory is not empty. Found unexpected files/directories:");
      unexpectedFiles.forEach(file -> errors.add("  - " + file.getFileName()));
      errors.add("Clean architecture initialization requires an empty project.");
      errors.add("Please remove these files or initialize in a clean directory.");
    }

    return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
  }

  /**
   * Validates the project configuration parameters.
   * 
   * @param config the configuration to validate
   * @return ValidationResult with success status and any error messages
   */
  private ValidationResult validateConfiguration(ProjectConfig config) {
    List<String> errors = new ArrayList<>();

    // Validate package name
    if (config.basePackage() == null || config.basePackage().isBlank()) {
      errors.add("Base package cannot be empty");
    } else if (!isValidPackageName(config.basePackage())) {
      errors.add("Invalid package name: " + config.basePackage());
      errors.add("Package name must follow Java naming conventions (e.g., com.company.service)");
    }

    // Validate project name
    if (config.name() == null || config.name().isBlank()) {
      errors.add("Project name cannot be empty");
    } else if (!isValidProjectName(config.name())) {
      errors.add("Invalid project name: " + config.name());
      errors.add("Project name must contain only lowercase letters, numbers, and hyphens");
    }

    // Validate architecture type
    if (config.architecture() == null) {
      errors.add("Architecture type cannot be null");
    }

    // Validate paradigm
    if (config.paradigm() == null) {
      errors.add("Paradigm cannot be null");
    }

    // Validate framework
    if (config.framework() == null) {
      errors.add("Framework cannot be null");
    }

    return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
  }

  /**
   * Validates a Java package name.
   * 
   * @param packageName the package name to validate
   * @return true if valid, false otherwise
   */
  private boolean isValidPackageName(String packageName) {
    if (packageName == null || packageName.isBlank()) {
      return false;
    }

    // Package name pattern: lowercase letters, numbers, dots
    // Each segment must start with a letter
    String[] segments = packageName.split("\\.");

    if (segments.length < 2) {
      return false; // At least two segments required (e.g., com.company)
    }

    for (String segment : segments) {
      if (segment.isEmpty()) {
        return false;
      }

      // Must start with a letter
      if (!Character.isLetter(segment.charAt(0))) {
        return false;
      }

      // Can only contain letters, numbers, and underscores
      if (!segment.matches("[a-z][a-z0-9_]*")) {
        return false;
      }
    }

    return true;
  }

  /**
   * Validates a project name.
   * 
   * @param projectName the project name to validate
   * @return true if valid, false otherwise
   */
  private boolean isValidProjectName(String projectName) {
    if (projectName == null || projectName.isBlank()) {
      return false;
    }

    // Project name: lowercase letters, numbers, hyphens
    // Must start with a letter
    return projectName.matches("[a-z][a-z0-9-]*");
  }
}
