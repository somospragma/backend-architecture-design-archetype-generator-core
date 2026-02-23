package com.pragma.archetype.application.usecase;

import java.nio.file.Path;
import java.util.List;

import com.pragma.archetype.application.generator.InputAdapterGenerator;
import com.pragma.archetype.domain.model.GeneratedFile;
import com.pragma.archetype.domain.model.InputAdapterConfig;
import com.pragma.archetype.domain.model.ValidationResult;
import com.pragma.archetype.domain.port.in.GenerateInputAdapterUseCase;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import com.pragma.archetype.domain.service.InputAdapterValidator;

/**
 * Implementation of the GenerateInputAdapterUseCase.
 * Orchestrates the input adapter generation process.
 */
public class GenerateInputAdapterUseCaseImpl implements GenerateInputAdapterUseCase {

  private final InputAdapterValidator validator;
  private final InputAdapterGenerator generator;
  private final ConfigurationPort configurationPort;
  private final FileSystemPort fileSystemPort;

  public GenerateInputAdapterUseCaseImpl(
      InputAdapterValidator validator,
      InputAdapterGenerator generator,
      ConfigurationPort configurationPort,
      FileSystemPort fileSystemPort) {
    this.validator = validator;
    this.generator = generator;
    this.configurationPort = configurationPort;
    this.fileSystemPort = fileSystemPort;
  }

  @Override
  public GenerationResult execute(Path projectPath, InputAdapterConfig config) {
    // 1. Validate configuration
    ValidationResult validationResult = validator.validate(projectPath, config);
    if (!validationResult.valid()) {
      return GenerationResult.failure(validationResult.errors());
    }

    try {
      // 2. Generate input adapter files
      List<GeneratedFile> generatedFiles = generator.generate(projectPath, config);

      // 3. Write files to disk
      for (GeneratedFile file : generatedFiles) {
        fileSystemPort.writeFile(file);
      }

      return GenerationResult.success(generatedFiles);

    } catch (Exception e) {
      return GenerationResult.failure(List.of("Failed to generate input adapter: " + e.getMessage()));
    }
  }
}
