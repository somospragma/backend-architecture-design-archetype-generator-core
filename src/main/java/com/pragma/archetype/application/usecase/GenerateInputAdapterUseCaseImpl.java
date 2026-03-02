package com.pragma.archetype.application.usecase;

import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pragma.archetype.application.generator.InputAdapterGenerator;
import com.pragma.archetype.application.port.out.AdapterDiscoveryPort;
import com.pragma.archetype.domain.model.adapter.AdapterDirection;
import com.pragma.archetype.domain.model.adapter.AdapterMetadata;
import com.pragma.archetype.domain.model.adapter.InputAdapterConfig;
import com.pragma.archetype.domain.model.file.GeneratedFile;
import com.pragma.archetype.domain.model.validation.ValidationResult;
import com.pragma.archetype.domain.port.in.GenerateInputAdapterUseCase;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import com.pragma.archetype.domain.port.out.TemplateRepository;
import com.pragma.archetype.domain.service.InputAdapterValidator;
import com.pragma.archetype.infrastructure.adapter.out.config.YamlConfigurationAdapter;

/**
 * Implementation of the GenerateInputAdapterUseCase.
 * Orchestrates the input adapter generation process.
 */
public class GenerateInputAdapterUseCaseImpl implements GenerateInputAdapterUseCase {

  private static final Logger logger = LoggerFactory.getLogger(GenerateInputAdapterUseCaseImpl.class);

  private final InputAdapterValidator validator;
  private final InputAdapterGenerator generator;
  private final ConfigurationPort configurationPort;
  private final FileSystemPort fileSystemPort;
  private final AdapterDiscoveryPort adapterDiscovery;
  private final TemplateRepository templateRepository;
  private final YamlConfigurationAdapter yamlConfigurationAdapter;
  private final com.pragma.archetype.domain.service.AdapterConfigurationService adapterConfigurationService;

  public GenerateInputAdapterUseCaseImpl(
      InputAdapterValidator validator,
      InputAdapterGenerator generator,
      ConfigurationPort configurationPort,
      FileSystemPort fileSystemPort,
      AdapterDiscoveryPort adapterDiscovery,
      TemplateRepository templateRepository,
      YamlConfigurationAdapter yamlConfigurationAdapter) {
    this.validator = validator;
    this.generator = generator;
    this.configurationPort = configurationPort;
    this.fileSystemPort = fileSystemPort;
    this.adapterDiscovery = adapterDiscovery;
    this.templateRepository = templateRepository;
    this.yamlConfigurationAdapter = yamlConfigurationAdapter;
    this.adapterConfigurationService = new com.pragma.archetype.domain.service.AdapterConfigurationService(
        configurationPort, fileSystemPort, templateRepository, yamlConfigurationAdapter);
  }

  @Override
  public GenerationResult execute(Path projectPath, InputAdapterConfig config) {
    // 1. Validate configuration
    ValidationResult validationResult = validator.validate(projectPath, config);
    if (!validationResult.valid()) {
      return GenerationResult.failure(validationResult.errors());
    }

    try {
      // 2. Load project configuration to get architecture
      var projectConfig = configurationPort.readConfiguration(projectPath)
          .orElseThrow(() -> new IllegalStateException("Project configuration not found"));

      // 3. Load structure metadata for the architecture
      com.pragma.archetype.domain.model.structure.StructureMetadata structureMetadata = templateRepository
          .loadStructureMetadata(projectConfig.architecture());

      // 4. Generate input adapter files
      List<GeneratedFile> generatedFiles = generator.generate(projectPath, config, structureMetadata);

      // 5. Write files to disk
      for (GeneratedFile file : generatedFiles) {
        fileSystemPort.writeFile(file);
      }

      // 6. Load adapter metadata for configuration tasks
      AdapterMetadata metadata = adapterDiscovery
          .findAdapter(config.type(), AdapterDirection.INPUT, config.framework(), config.paradigm())
          .orElseThrow(() -> new IllegalStateException("Adapter metadata not found for: " + config.type()));

      // 7. Create adapter config for shared service
      var adapterConfig = new com.pragma.archetype.domain.service.AdapterConfigurationService.AdapterConfig(
          config.name(),
          config.type(),
          config.framework(),
          config.paradigm(),
          config.packageName(),
          config.useCaseName(),
          null, // entityName not applicable for input adapters
          AdapterDirection.INPUT);

      // 8. Merge application properties if adapter has applicationPropertiesTemplate
      adapterConfigurationService.mergeApplicationPropertiesIfNeeded(projectPath, adapterConfig, metadata);

      // 9. Add dependencies to build.gradle.kts if adapter has dependencies
      adapterConfigurationService.addDependenciesIfNeeded(projectPath, adapterConfig, metadata);

      // 10. Add build plugins and configuration if adapter has them
      adapterConfigurationService.addBuildPluginsAndConfigurationIfNeeded(
          projectPath, adapterConfig, metadata, structureMetadata);

      // 11. Generate configuration classes if adapter has configurationClasses
      List<GeneratedFile> configFiles = adapterConfigurationService.generateConfigurationClassesIfNeeded(
          projectPath, adapterConfig, metadata);
      generatedFiles.addAll(configFiles);

      // 12. Add test dependencies if adapter has testDependencies
      adapterConfigurationService.addTestDependenciesIfNeeded(projectPath, adapterConfig, metadata);

      return GenerationResult.success(generatedFiles);

    } catch (Exception e) {
      return GenerationResult.failure(List.of("Failed to generate input adapter: " + e.getMessage()));
    }
  }

}
