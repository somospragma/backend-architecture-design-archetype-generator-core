package com.pragma.archetype.application.usecase;

import java.nio.file.Path;
import java.util.List;

import com.pragma.archetype.application.generator.EntityGenerator;
import com.pragma.archetype.domain.model.config.ProjectConfig;
import com.pragma.archetype.domain.model.entity.EntityConfig;
import com.pragma.archetype.domain.model.file.GeneratedFile;
import com.pragma.archetype.domain.model.validation.ValidationResult;
import com.pragma.archetype.domain.port.in.GenerateEntityUseCase;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import com.pragma.archetype.domain.service.EntityValidator;

/**
 * Implementation of GenerateEntityUseCase.
 */
public class GenerateEntityUseCaseImpl implements GenerateEntityUseCase {

  private final EntityValidator validator;
  private final EntityGenerator generator;
  private final ConfigurationPort configurationPort;
  private final FileSystemPort fileSystemPort;

  public GenerateEntityUseCaseImpl(
      EntityValidator validator,
      EntityGenerator generator,
      ConfigurationPort configurationPort,
      FileSystemPort fileSystemPort) {
    this.validator = validator;
    this.generator = generator;
    this.configurationPort = configurationPort;
    this.fileSystemPort = fileSystemPort;
  }

  @Override
  public GenerationResult execute(Path projectPath, EntityConfig config) {
    try {
      // 1. Validate
      ValidationResult validationResult = validator.validate(projectPath, config);
      if (validationResult.isInvalid()) {
        return GenerationResult.failure(validationResult.errors());
      }

      // 2. Load project configuration
      ProjectConfig projectConfig = configurationPort.readConfiguration(projectPath)
          .orElseThrow(() -> new RuntimeException(
              "Project configuration not found. Run 'initCleanArch' first."));

      // 3. Generate entity
      List<GeneratedFile> files = generator.generateEntity(projectPath, projectConfig, config);

      // 4. Write files
      fileSystemPort.writeFiles(files);

      return GenerationResult.success(files);

    } catch (Exception e) {
      return GenerationResult.failure(List.of(
          "Failed to generate entity: " + e.getMessage()));
    }
  }
}
