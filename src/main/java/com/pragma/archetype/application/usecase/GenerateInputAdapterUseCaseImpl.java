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
  private final com.pragma.archetype.domain.service.BuildFileMerger buildFileMerger;
  private final com.pragma.archetype.domain.service.BuildConfigurationProcessor buildConfigurationProcessor;

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
    this.buildFileMerger = new com.pragma.archetype.domain.service.BuildFileMerger();
    this.buildConfigurationProcessor = new com.pragma.archetype.domain.service.BuildConfigurationProcessor(
        templateRepository);
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

      // 6. Merge application properties if adapter has applicationPropertiesTemplate
      mergeApplicationPropertiesIfNeeded(projectPath, config);

      // 7. Add dependencies to build.gradle.kts if adapter has dependencies
      addDependenciesIfNeeded(projectPath, config);

      // 8. Add build plugins and configuration if adapter has them
      addBuildPluginsAndConfigurationIfNeeded(projectPath, config, structureMetadata);

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

  /**
   * Adds dependencies to the build file if the adapter has dependencies.
   * Dependencies are added with implementation scope in Gradle.
   *
   * @param projectPath path to the project root
   * @param config      adapter configuration
   */
  private void addDependenciesIfNeeded(Path projectPath, InputAdapterConfig config) {
    try {
      // 1. Load adapter metadata to check if it has dependencies
      AdapterMetadata metadata = adapterDiscovery
          .findAdapter(config.type(), AdapterDirection.INPUT, config.framework(), config.paradigm())
          .orElse(null);

      if (metadata == null || metadata.getDependencies().isEmpty()) {
        logger.debug("Adapter {} does not have dependencies, skipping", config.name());
        return;
      }

      logger.info("Adding dependencies for adapter: {}", config.name());

      // 2. Determine the build file path
      Path buildFilePath = projectPath.resolve("build.gradle.kts");

      if (!fileSystemPort.exists(buildFilePath)) {
        logger.warn("Build file not found at {}, skipping dependency addition", buildFilePath);
        return;
      }

      // 3. Read the current build file content
      String buildFileContent = fileSystemPort.readFile(buildFilePath);

      // 4. Add each dependency
      String updatedContent = buildFileContent;
      for (String dep : metadata.getDependencies()) {
        updatedContent = addDependencyToBuildFile(updatedContent, dep);
      }

      // 5. Write the updated build file if changes were made
      if (!updatedContent.equals(buildFileContent)) {
        fileSystemPort.writeFile(GeneratedFile.create(buildFilePath, updatedContent));
        logger.info("Successfully added {} dependencies for adapter: {}",
            metadata.getDependencies().size(), config.name());
      } else {
        logger.debug("No new dependencies to add for adapter: {}", config.name());
      }

    } catch (Exception e) {
      logger.error("Failed to add dependencies for adapter {}: {}",
          config.name(), e.getMessage(), e);
      // Don't fail the entire generation if dependency addition fails
    }
  }

  /**
   * Adds a dependency to the build file content.
   * Uses implementation scope for Gradle.
   *
   * @param buildFileContent current build file content
   * @param dependency       dependency coordinate string (group:artifact:version)
   * @return updated build file content
   */
  private String addDependencyToBuildFile(String buildFileContent, String dependency) {
    // Parse dependency coordinate
    String[] parts = dependency.split(":");
    if (parts.length < 3) {
      logger.warn("Invalid dependency format: {}. Expected group:artifact:version", dependency);
      return buildFileContent;
    }

    String group = parts[0];
    String artifact = parts[1];
    String version = parts[2];

    // Format: implementation("group:artifact:version")
    String dependencyStatement = String.format("    implementation(\"%s:%s:%s\")",
        group, artifact, version);

    // Check if dependency is already present
    if (buildFileContent.contains(dependencyStatement)) {
      logger.debug("Dependency already present: {}", dependency);
      return buildFileContent;
    }

    // Find the dependencies block and add the dependency
    if (buildFileContent.contains("dependencies {")) {
      // Look for existing implementation dependencies section or add at the beginning
      if (buildFileContent.contains("implementation(")) {
        // Add after the last implementation
        int lastImpl = buildFileContent.lastIndexOf("implementation(");
        int endOfLine = buildFileContent.indexOf('\n', lastImpl);
        if (endOfLine != -1) {
          return buildFileContent.substring(0, endOfLine + 1) +
              dependencyStatement + "\n" +
              buildFileContent.substring(endOfLine + 1);
        }
      }

      // If no implementation found, add after "dependencies {"
      int dependenciesStart = buildFileContent.indexOf("dependencies {");
      int lineEnd = buildFileContent.indexOf('\n', dependenciesStart);
      if (lineEnd != -1) {
        return buildFileContent.substring(0, lineEnd + 1) +
            dependencyStatement + "\n" +
            buildFileContent.substring(lineEnd + 1);
      }
    }

    logger.warn("Could not find dependencies block in build file, dependency not added: {}",
        dependency);
    return buildFileContent;
  }

  /**
   * Adds build plugins and configuration to build.gradle.kts if the adapter has
   * them.
   * Plugins are added to the plugins {} block, and configuration is appended to
   * the end.
   *
   * @param projectPath       path to the project root
   * @param config            adapter configuration
   * @param structureMetadata structure metadata for variable replacement
   */
  private void addBuildPluginsAndConfigurationIfNeeded(
      Path projectPath,
      InputAdapterConfig config,
      com.pragma.archetype.domain.model.structure.StructureMetadata structureMetadata) {

    try {
      // 1. Load adapter metadata to check if it has buildPlugins or
      // buildConfiguration
      AdapterMetadata metadata = adapterDiscovery
          .findAdapter(config.type(), AdapterDirection.INPUT, config.framework(), config.paradigm())
          .orElse(null);

      if (metadata == null) {
        logger.debug("Adapter metadata not found for {}, skipping build configuration", config.name());
        return;
      }

      if (!metadata.hasBuildPlugins() && !metadata.hasBuildConfiguration()) {
        logger.debug("Adapter {} does not have build plugins or configuration, skipping", config.name());
        return;
      }

      logger.info("Processing build plugins and configuration for adapter: {}", config.name());

      // 2. Determine the build file path
      Path buildFilePath = projectPath.resolve("build.gradle.kts");

      if (!fileSystemPort.exists(buildFilePath)) {
        logger.warn("Build file not found at {}, skipping build configuration", buildFilePath);
        return;
      }

      // 3. Read the current build file content
      String buildFileContent = fileSystemPort.readFile(buildFilePath);

      // 4. Add plugins if present
      if (metadata.hasBuildPlugins()) {
        buildFileContent = buildFileMerger.addPlugins(buildFileContent, metadata.getBuildPlugins());
      }

      // 5. Process and add build configuration if present
      if (metadata.hasBuildConfiguration()) {
        String processedConfig = buildConfigurationProcessor.process(
            metadata.getBuildConfiguration(),
            structureMetadata,
            config);

        buildFileContent = buildFileMerger.addBuildConfiguration(buildFileContent, processedConfig);
      }

      // 6. Write the updated build file
      fileSystemPort.writeFile(GeneratedFile.create(buildFilePath, buildFileContent));

      logger.info("Successfully added build plugins and configuration for adapter: {}", config.name());

    } catch (Exception e) {
      logger.error("Failed to add build plugins and configuration for adapter {}: {}",
          config.name(), e.getMessage(), e);
      // Don't fail the entire generation if build configuration fails
    }
  }
}
