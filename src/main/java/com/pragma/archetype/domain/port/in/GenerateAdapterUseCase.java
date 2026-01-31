package com.pragma.archetype.domain.port.in;

import java.nio.file.Path;
import java.util.List;

import com.pragma.archetype.domain.model.AdapterConfig;
import com.pragma.archetype.domain.model.GeneratedFile;

/**
 * Use case for generating output adapters (Redis, MongoDB, etc.).
 * This is an input port in the hexagonal architecture.
 */
public interface GenerateAdapterUseCase {

  /**
   * Result of adapter generation.
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
   * Generates an output adapter with its implementation.
   *
   * @param projectPath Path to the project root
   * @param config      Adapter configuration
   * @return Generation result with generated files or errors
   */
  GenerationResult execute(Path projectPath, AdapterConfig config);
}
