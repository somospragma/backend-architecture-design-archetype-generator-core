package com.pragma.archetype.application.usecase;

import java.nio.file.Path;
import java.util.List;

import com.pragma.archetype.application.generator.ProjectGenerator;
import com.pragma.archetype.domain.model.GeneratedFile;
import com.pragma.archetype.domain.model.ProjectConfig;
import com.pragma.archetype.domain.model.ValidationResult;
import com.pragma.archetype.domain.port.in.InitializeProjectUseCase;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.service.ProjectValidator;

/**
 * Implementation of the Initialize Project use case.
 * Orchestrates the project initialization process following clean architecture
 * principles.
 */
public class InitializeProjectUseCaseImpl implements InitializeProjectUseCase {

  /**
   * Result of project initialization.
   */
  public record InitializationResult(
      boolean success,
      List<GeneratedFile> generatedFiles,
      List<String> errors) {

    public static InitializationResult success(List<GeneratedFile> files) {
      return new InitializationResult(true, files, List.of());
    }

    public static InitializationResult failure(List<String> errors) {
      return new InitializationResult(false, List.of(), errors);
    }

    public boolean isSuccess() {
      return success;
    }

    public boolean isFailure() {
      return !success;
    }
  }

  private final ProjectValidator projectValidator;
  private final ProjectGenerator projectGenerator;

  private final ConfigurationPort configurationPort;

  public InitializeProjectUseCaseImpl(
      ProjectValidator projectValidator,
      ProjectGenerator projectGenerator,
      ConfigurationPort configurationPort) {
    this.projectValidator = projectValidator;
    this.projectGenerator = projectGenerator;
    this.configurationPort = configurationPort;
  }

  /**
   * Initializes a clean architecture project.
   *
   * @param projectPath the root directory of the project
   * @param config      the project configuration
   * @return result containing generated files or validation errors
   */
  @Override
  public InitializationResult execute(Path projectPath, ProjectConfig config) {
    // 1. Validate project can be initialized
    ValidationResult validation = projectValidator.validateForInitialization(projectPath, config);
    if (!validation.valid()) {
      return InitializationResult.failure(validation.errors());
    }

    try {
      // 2. Generate project structure
      List<GeneratedFile> generatedFiles = projectGenerator.generateProject(projectPath, config);

      // 3. Save configuration
      configurationPort.writeConfiguration(projectPath, config);

      // 4. Return success result
      return InitializationResult.success(generatedFiles);

    } catch (Exception e) {
      return InitializationResult.failure(
          List.of("Failed to initialize project: " + e.getMessage()));
    }
  }
}
