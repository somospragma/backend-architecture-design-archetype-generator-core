package com.pragma.archetype.domain.port.in;

import java.nio.file.Path;
import java.util.List;

import com.pragma.archetype.domain.model.GeneratedFile;
import com.pragma.archetype.domain.model.usecase.UseCaseConfig;

/**
 * Use case for generating use cases (port and implementation).
 * This is an input port in the hexagonal architecture.
 */
public interface GenerateUseCaseUseCase {

  /**
   * Result of use case generation.
   */
  record GenerationResult(
      boolean success,
      List<GeneratedFile> generatedFiles,
      List<String> errors) {
    public static GenerationResult success(List<GeneratedFile> files) {
      return new GenerationResult(true, files, List.of());
    }

    public static GenerationResult failure(List<String> errors) {
      return new GenerationResult(false, List.of(), errors);
    }

    public boolean isFailure() {
      return !success;
    }
  }

  /**
   * Generates a use case with its port interface and implementation.
   *
   * @param projectPath Path to the project root
   * @param config      Use case configuration
   * @return Generation result with generated files or errors
   */
  GenerationResult execute(Path projectPath, UseCaseConfig config);
}
