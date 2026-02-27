package com.pragma.archetype.application.usecase;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

      // 4. Merge application properties if adapter has applicationPropertiesTemplate
      mergeApplicationPropertiesIfNeeded(projectPath, config);

      return GenerationResult.success(generatedFiles);

    } catch (Exception e) {
      return GenerationResult.failure(List.of("Failed to generate input adapter: " + e.getMessage()));
    }
  }

  /**
   * Merges application properties from adapter template into existing
   * application.yml.
   * Only merges if the adapter has an applicationPropertiesTemplate defined.
   *
   * @param projectPath path to the project root
   * @param config      input adapter configuration
   */
  private void mergeApplicationPropertiesIfNeeded(Path projectPath, InputAdapterConfig config) {
    try {
      // 1. Load adapter metadata to check if it has applicationPropertiesTemplate
      AdapterMetadata metadata = adapterDiscovery
          .findAdapter(config.type(), AdapterDirection.INPUT, config.framework(), config.paradigm())
          .orElse(null);

      if (metadata == null || !metadata.hasApplicationProperties()) {
        logger.debug("Adapter {} does not have application properties template, skipping merge",
            config.name());
        return;
      }

      logger.info("Processing application properties template for adapter: {}", config.name());

      // 2. Prepare template context with adapter information
      Map<String, Object> templateContext = prepareApplicationPropertiesContext(config, projectPath);

      // 3. Process the application properties template
      String propertiesTemplatePath = String.format("frameworks/%s/%s/adapters/entry-points/%s/%s",
          config.framework(), config.paradigm(), config.type(), metadata.applicationPropertiesTemplate());
      String processedProperties = templateRepository.processTemplate(propertiesTemplatePath, templateContext);

      // 4. Parse the processed properties as YAML
      org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml();
      Map<String, Object> newProperties = yaml.load(processedProperties);

      if (newProperties == null || newProperties.isEmpty()) {
        logger.warn("Processed application properties template is empty for adapter: {}", config.name());
        return;
      }

      // 5. Determine application.yml path
      Path applicationYmlPath = projectPath.resolve("src/main/resources/application.yml");

      // 6. Read existing application.yml if it exists
      Map<String, Object> existingProperties = yamlConfigurationAdapter.readYaml(applicationYmlPath);

      // 7. Merge new properties using YamlConfigurationAdapter
      Map<String, Object> mergedProperties = yamlConfigurationAdapter.mergeYaml(existingProperties, newProperties);

      // 8. Write merged properties back to application.yml
      yamlConfigurationAdapter.writeYaml(applicationYmlPath, mergedProperties);

      logger.info("Successfully merged application properties for adapter: {}", config.name());

    } catch (TemplateRepository.TemplateNotFoundException e) {
      logger.warn("Application properties template not found for adapter {}: {}",
          config.name(), e.getMessage());
    } catch (Exception e) {
      logger.error("Failed to merge application properties for adapter {}: {}",
          config.name(), e.getMessage(), e);
      // Don't fail the entire generation if property merging fails
      // The adapter code is already generated, properties can be added manually
    }
  }

  /**
   * Prepares template context for processing application properties template.
   *
   * @param config      input adapter configuration
   * @param projectPath path to the project root
   * @return template context map
   */
  private Map<String, Object> prepareApplicationPropertiesContext(InputAdapterConfig config, Path projectPath) {
    Map<String, Object> context = new HashMap<>();
    context.put("adapterName", config.name());
    context.put("adapterType", config.type().toLowerCase());
    context.put("packageName", config.packageName());
    context.put("useCaseName", config.useCaseName());

    // Add project name if available (useful for application names, etc.)
    try {
      var projectConfig = configurationPort.readConfiguration(projectPath).orElse(null);
      if (projectConfig != null) {
        context.put("projectName", projectConfig.name());
        context.put("basePackage", projectConfig.basePackage());
      }
    } catch (Exception e) {
      logger.debug("Could not read project configuration for template context: {}", e.getMessage());
    }

    return context;
  }
}
