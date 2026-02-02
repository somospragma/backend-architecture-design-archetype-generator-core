package com.pragma.archetype.application.usecase;

import java.nio.file.Path;
import java.util.List;

import com.pragma.archetype.application.generator.AdapterGenerator;
import com.pragma.archetype.domain.model.AdapterConfig;
import com.pragma.archetype.domain.model.GeneratedFile;
import com.pragma.archetype.domain.model.ValidationResult;
import com.pragma.archetype.domain.port.in.GenerateAdapterUseCase;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import com.pragma.archetype.domain.service.AdapterValidator;

/**
 * Implementation of the GenerateAdapterUseCase.
 * Orchestrates the adapter generation process.
 */
public class GenerateAdapterUseCaseImpl implements GenerateAdapterUseCase {

  private final AdapterValidator validator;
  private final AdapterGenerator generator;
  private final ConfigurationPort configurationPort;
  private final FileSystemPort fileSystemPort;

  public GenerateAdapterUseCaseImpl(
      AdapterValidator validator,
      AdapterGenerator generator,
      ConfigurationPort configurationPort,
      FileSystemPort fileSystemPort) {
    this.validator = validator;
    this.generator = generator;
    this.configurationPort = configurationPort;
    this.fileSystemPort = fileSystemPort;
  }

  @Override
  public GenerationResult execute(Path projectPath, AdapterConfig config) {
    // 1. Validate configuration
    ValidationResult validationResult = validator.validate(projectPath, config);
    if (!validationResult.valid()) {
      return GenerationResult.failure(validationResult.errors());
    }

    try {
      // 2. Read project configuration to check adaptersAsModules flag
      var projectConfig = configurationPort.readConfiguration(projectPath).orElse(null);

      // 3. Generate adapter files
      List<GeneratedFile> generatedFiles = generator.generate(projectPath, config, projectConfig);

      // 4. Write files to disk
      for (GeneratedFile file : generatedFiles) {
        fileSystemPort.writeFile(file);
      }

      return GenerationResult.success(generatedFiles);

    } catch (Exception e) {
      return GenerationResult.failure(List.of("Failed to generate adapter: " + e.getMessage()));
    }
  }
}
