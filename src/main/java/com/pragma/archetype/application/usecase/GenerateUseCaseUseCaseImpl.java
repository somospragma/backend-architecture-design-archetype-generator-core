package com.pragma.archetype.application.usecase;

import java.nio.file.Path;
import java.util.List;

import com.pragma.archetype.application.generator.UseCaseGenerator;
import com.pragma.archetype.domain.model.GeneratedFile;
import com.pragma.archetype.domain.model.UseCaseConfig;
import com.pragma.archetype.domain.model.ValidationResult;
import com.pragma.archetype.domain.port.in.GenerateUseCaseUseCase;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import com.pragma.archetype.domain.service.UseCaseValidator;

/**
 * Implementation of the GenerateUseCaseUseCase.
 * Orchestrates the use case generation process.
 */
public class GenerateUseCaseUseCaseImpl implements GenerateUseCaseUseCase {

  private final UseCaseValidator validator;
  private final UseCaseGenerator generator;
  private final ConfigurationPort configurationPort;
  private final FileSystemPort fileSystemPort;

  public GenerateUseCaseUseCaseImpl(
      UseCaseValidator validator,
      UseCaseGenerator generator,
      ConfigurationPort configurationPort,
      FileSystemPort fileSystemPort) {
    this.validator = validator;
    this.generator = generator;
    this.configurationPort = configurationPort;
    this.fileSystemPort = fileSystemPort;
  }

  @Override
  public GenerationResult execute(Path projectPath, UseCaseConfig config) {
    // 1. Validate configuration
    ValidationResult validationResult = validator.validate(projectPath, config);
    if (!validationResult.valid()) {
      return GenerationResult.failure(validationResult.errors());
    }

    try {
      // 2. Generate use case files
      List<GeneratedFile> generatedFiles = generator.generate(projectPath, config);

      // 3. Write files to disk
      for (GeneratedFile file : generatedFiles) {
        fileSystemPort.writeFile(file);
      }

      return GenerationResult.success(generatedFiles);

    } catch (Exception e) {
      return GenerationResult.failure(List.of("Failed to generate use case: " + e.getMessage()));
    }
  }
}
