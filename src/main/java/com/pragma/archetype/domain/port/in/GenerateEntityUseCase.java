package com.pragma.archetype.domain.port.in;

import java.nio.file.Path;
import java.util.List;

import com.pragma.archetype.domain.model.entity.EntityConfig;
import com.pragma.archetype.domain.model.file.GeneratedFile;

/**
 * Use case for generating domain entities.
 */
public interface GenerateEntityUseCase {

  /**
   * Result of entity generation.
   *
   * @param success        Whether generation was successful
   * @param generatedFiles List of generated files
   * @param errors         List of error messages
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
   * Generates a domain entity in the project.
   *
   * @param projectPath Path to the project root
   * @param config      Entity configuration
   * @return Result of the generation
   */
  GenerationResult execute(Path projectPath, EntityConfig config);
}
