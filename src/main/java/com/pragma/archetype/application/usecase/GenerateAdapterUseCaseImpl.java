package com.pragma.archetype.application.usecase;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pragma.archetype.application.generator.AdapterGenerator;
import com.pragma.archetype.domain.model.AdapterConfig;
import com.pragma.archetype.domain.model.adapter.AdapterMetadata;
import com.pragma.archetype.domain.model.file.GeneratedFile;
import com.pragma.archetype.domain.model.validation.ValidationResult;
import com.pragma.archetype.domain.port.in.GenerateAdapterUseCase;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import com.pragma.archetype.domain.port.out.TemplateRepository;
import com.pragma.archetype.domain.service.AdapterValidator;
import com.pragma.archetype.domain.service.BackupService;
import com.pragma.archetype.infrastructure.adapter.out.config.YamlConfigurationAdapter;

/**
 * Implementation of the GenerateAdapterUseCase.
 * Orchestrates the adapter generation process.
 */
public class GenerateAdapterUseCaseImpl implements GenerateAdapterUseCase {

  private static final Logger logger = LoggerFactory.getLogger(GenerateAdapterUseCaseImpl.class);

  private final AdapterValidator validator;
  private final AdapterGenerator generator;
  private final ConfigurationPort configurationPort;
  private final FileSystemPort fileSystemPort;
  private final TemplateRepository templateRepository;
  private final YamlConfigurationAdapter yamlConfigurationAdapter;
  private final BackupService backupService;
  private final com.pragma.archetype.domain.service.DependencyConflictDetector conflictDetector;

  public GenerateAdapterUseCaseImpl(
      AdapterValidator validator,
      AdapterGenerator generator,
      ConfigurationPort configurationPort,
      FileSystemPort fileSystemPort,
      TemplateRepository templateRepository,
      YamlConfigurationAdapter yamlConfigurationAdapter,
      BackupService backupService) {
    this.validator = validator;
    this.generator = generator;
    this.configurationPort = configurationPort;
    this.fileSystemPort = fileSystemPort;
    this.templateRepository = templateRepository;
    this.yamlConfigurationAdapter = yamlConfigurationAdapter;
    this.backupService = backupService;
    this.conflictDetector = new com.pragma.archetype.domain.service.DependencyConflictDetector();
  }

  @Override
  public GenerationResult execute(Path projectPath, AdapterConfig config) {
    // 1. Validate configuration
    ValidationResult validationResult = validator.validate(projectPath, config);
    if (!validationResult.valid()) {
      return GenerationResult.failure(validationResult.errors());
    }

    // 2. Read project configuration to check adaptersAsModules flag
    var projectConfig = configurationPort.readConfiguration(projectPath).orElse(null);

    // 3. Validate all templates before processing (validation-before-modification)
    ValidationResult templateValidation = validateAllTemplates(config, projectConfig);
    if (!templateValidation.valid()) {
      logger.error("Template validation failed. Displaying all errors before generation:");
      templateValidation.errors().forEach(error -> logger.error("  - {}", error));
      return GenerationResult.failure(templateValidation.errors());
    }

    String backupId = null;
    List<Path> filesToBackup = new ArrayList<>();

    try {

      // 3. Identify files that will be modified
      filesToBackup = identifyFilesToBackup(projectPath, config, projectConfig);

      // 4. Create backup before any modifications
      if (!filesToBackup.isEmpty()) {
        backupId = backupService.createBackup(projectPath, filesToBackup);
        logger.info("Created backup with ID: {}", backupId);
      }

      // 5. Generate adapter files
      List<GeneratedFile> generatedFiles = new ArrayList<>(generator.generate(projectPath, config, projectConfig));

      // 6. Write files to disk
      for (GeneratedFile file : generatedFiles) {
        fileSystemPort.writeFile(file);
      }

      // 7. Merge application properties if adapter has applicationPropertiesTemplate
      mergeApplicationPropertiesIfNeeded(projectPath, config);

      // 8. Generate configuration classes if adapter has configurationClasses
      List<GeneratedFile> configFiles = generateConfigurationClassesIfNeeded(projectPath, config);
      generatedFiles.addAll(configFiles);

      // Write configuration class files to disk
      for (GeneratedFile file : configFiles) {
        fileSystemPort.writeFile(file);
      }

      // 9. Check for dependency conflicts before adding dependencies
      checkDependencyConflicts(projectPath, config, projectConfig);

      // 10. Add test dependencies if adapter has testDependencies
      addTestDependenciesIfNeeded(projectPath, config);

      // 11. Delete backup on success
      if (backupId != null) {
        backupService.deleteBackup(projectPath, backupId);
        logger.info("Deleted backup after successful generation: {}", backupId);
      }

      return GenerationResult.success(generatedFiles);

    } catch (Exception e) {
      logger.error("Adapter generation failed: {}", e.getMessage(), e);

      // 11. Restore backup on failure
      if (backupId != null) {
        try {
          backupService.restoreBackup(projectPath, backupId);
          logger.info("Successfully restored backup: {}", backupId);
          return GenerationResult.failure(List.of(
              "Failed to generate adapter: " + e.getMessage(),
              "All changes have been rolled back."));
        } catch (BackupService.BackupException restoreException) {
          logger.error("Failed to restore backup: {}", restoreException.getMessage(), restoreException);
          Path backupLocation = projectPath.resolve(".cleanarch/backups").resolve(backupId);
          return GenerationResult.failure(List.of(
              "Failed to generate adapter: " + e.getMessage(),
              "Failed to restore backup automatically.",
              "Manual recovery may be required.",
              "Backup location: " + backupLocation.toString()));
        }
      }

      return GenerationResult.failure(List.of("Failed to generate adapter: " + e.getMessage()));
    }
  }

  /**
   * Validates all templates required for adapter generation before any file
   * modifications.
   * This implements the validation-before-modification pattern (Requirements
   * 20.4,
   * 20.5).
   * 
   * @param config        adapter configuration
   * @param projectConfig project configuration
   * @return ValidationResult with all validation errors collected
   */
  private ValidationResult validateAllTemplates(AdapterConfig config,
      com.pragma.archetype.domain.model.config.ProjectConfig projectConfig) {
    List<String> errors = new ArrayList<>();
    List<String> warnings = new ArrayList<>();

    try {
      // 1. Load adapter metadata using framework-aware path
      String adapterTypeLower = config.type().name().toLowerCase();
      AdapterMetadata metadata;
      try {
        if (projectConfig != null) {
          // Use framework-aware loading
          String framework = projectConfig.framework().name().toLowerCase();
          String paradigm = projectConfig.paradigm().name().toLowerCase();
          String adapterTypeCategory = determineAdapterTypeCategory(config);

          metadata = templateRepository.loadAdapterMetadata(
              adapterTypeLower,
              framework,
              paradigm,
              adapterTypeCategory);
        } else {
          // Fallback to legacy loading if projectConfig not available
          metadata = templateRepository.loadAdapterMetadata(adapterTypeLower);
        }
      } catch (TemplateRepository.TemplateNotFoundException e) {
        errors.add(String.format("Adapter metadata not found for '%s': %s",
            config.type().name(), e.getMessage()));
        return ValidationResult.failure(errors);
      }

      // 2. Validate adapter metadata
      ValidationResult metadataValidation = metadata.validate();
      if (!metadataValidation.valid()) {
        errors.addAll(metadataValidation.errors());
      }

      // 3. Validate main adapter templates (from AdapterGenerator)
      String adapterTemplate = getAdapterTemplatePath(config.type());
      ValidationResult adapterTemplateValidation = templateRepository.validateTemplate(adapterTemplate);
      if (!adapterTemplateValidation.valid()) {
        errors.add(String.format("Main adapter template validation failed: %s",
            adapterTemplateValidation.errors().get(0)));
      }

      String dataEntityTemplate = getDataEntityTemplatePath(config.type());
      ValidationResult entityTemplateValidation = templateRepository.validateTemplate(dataEntityTemplate);
      if (!entityTemplateValidation.valid()) {
        errors.add(String.format("Data entity template validation failed: %s",
            entityTemplateValidation.errors().get(0)));
      }

      String mapperTemplate = getMapperTemplatePath();
      ValidationResult mapperTemplateValidation = templateRepository.validateTemplate(mapperTemplate);
      if (!mapperTemplateValidation.valid()) {
        errors.add(String.format("Mapper template validation failed: %s",
            mapperTemplateValidation.errors().get(0)));
      }

      // 4. Validate application properties template if present
      if (metadata.hasApplicationProperties()) {
        String propertiesTemplatePath = String.format("adapters/%s/%s",
            adapterTypeLower, metadata.applicationPropertiesTemplate());

        if (!templateRepository.templateExists(propertiesTemplatePath)) {
          errors.add(String.format(
              "Application properties template not found: %s (referenced in metadata.yml)",
              propertiesTemplatePath));
        } else {
          ValidationResult propertiesValidation = templateRepository.validateTemplate(propertiesTemplatePath);
          if (!propertiesValidation.valid()) {
            errors.add(String.format("Application properties template validation failed: %s",
                propertiesValidation.errors().get(0)));
          }
        }
      }

      // 5. Validate configuration class templates if present
      if (metadata.hasConfigurationClasses()) {
        for (AdapterMetadata.ConfigurationClass configClass : metadata.configurationClasses()) {
          String configTemplatePath = String.format("adapters/%s/%s",
              adapterTypeLower, configClass.templatePath());

          if (!templateRepository.templateExists(configTemplatePath)) {
            errors.add(String.format(
                "Configuration class template not found: %s (referenced in metadata.yml for class '%s')",
                configTemplatePath, configClass.name()));
          } else {
            ValidationResult configValidation = templateRepository.validateTemplate(configTemplatePath);
            if (!configValidation.valid()) {
              errors.add(String.format("Configuration class template '%s' validation failed: %s",
                  configClass.name(), configValidation.errors().get(0)));
            }
          }
        }
      }

      // 6. Validate module build template if adaptersAsModules is enabled
      if (projectConfig != null && projectConfig.adaptersAsModules()) {
        String moduleBuildTemplate = "frameworks/spring/reactive/adapters/driven-adapters/module-build.gradle.kts.ftl";
        if (!templateRepository.templateExists(moduleBuildTemplate)) {
          warnings.add(String.format(
              "Module build template not found: %s (required for adaptersAsModules=true)",
              moduleBuildTemplate));
        } else {
          ValidationResult moduleBuildValidation = templateRepository.validateTemplate(moduleBuildTemplate);
          if (!moduleBuildValidation.valid()) {
            errors.add(String.format("Module build template validation failed: %s",
                moduleBuildValidation.errors().get(0)));
          }
        }
      }

      // 7. Return validation result
      if (!errors.isEmpty()) {
        logger.error("Template validation found {} error(s)", errors.size());
        return ValidationResult.failure(errors);
      }

      if (!warnings.isEmpty()) {
        logger.warn("Template validation found {} warning(s)", warnings.size());
        warnings.forEach(warning -> logger.warn("  - {}", warning));
      }

      logger.info("All templates validated successfully for adapter: {}", config.name());
      return ValidationResult.success();

    } catch (Exception e) {
      logger.error("Unexpected error during template validation: {}", e.getMessage(), e);
      errors.add(String.format("Template validation failed: %s", e.getMessage()));
      return ValidationResult.failure(errors);
    }
  }

  /**
   * Gets the adapter template path for a given adapter type.
   * This mirrors the logic in AdapterGenerator.getAdapterTemplate().
   */
  private String getAdapterTemplatePath(AdapterConfig.AdapterType type) {
    return switch (type) {
      case REDIS -> "frameworks/spring/reactive/adapters/driven-adapters/redis/Adapter.java.ftl";
      case MONGODB -> "frameworks/spring/reactive/adapters/driven-adapters/mongodb/Adapter.java.ftl";
      case POSTGRESQL -> "frameworks/spring/reactive/adapters/driven-adapters/postgresql/Adapter.java.ftl";
      case REST_CLIENT -> "frameworks/spring/reactive/adapters/driven-adapters/rest-client/Adapter.java.ftl";
      case KAFKA -> "frameworks/spring/reactive/adapters/driven-adapters/kafka/Adapter.java.ftl";
    };
  }

  /**
   * Gets the data entity template path for a given adapter type.
   * This mirrors the logic in AdapterGenerator.getDataEntityTemplate().
   */
  private String getDataEntityTemplatePath(AdapterConfig.AdapterType type) {
    return switch (type) {
      case REDIS -> "frameworks/spring/reactive/adapters/driven-adapters/redis/Entity.java.ftl";
      case MONGODB -> "frameworks/spring/reactive/adapters/driven-adapters/mongodb/Entity.java.ftl";
      case POSTGRESQL -> "frameworks/spring/reactive/adapters/driven-adapters/postgresql/Entity.java.ftl";
      default -> "frameworks/spring/reactive/adapters/driven-adapters/generic/Entity.java.ftl";
    };
  }

  /**
   * Gets the mapper template path.
   * This mirrors the logic in AdapterGenerator.getMapperTemplate().
   */
  private String getMapperTemplatePath() {
    return "frameworks/spring/reactive/adapters/driven-adapters/generic/Mapper.java.ftl";
  }

  /**
   * Identifies files that will be modified during adapter generation.
   * These files need to be backed up before modification.
   *
   * @param projectPath   path to the project root
   * @param config        adapter configuration
   * @param projectConfig project configuration
   * @return list of file paths to backup (relative to project root)
   */
  private List<Path> identifyFilesToBackup(Path projectPath, AdapterConfig config,
      com.pragma.archetype.domain.model.config.ProjectConfig projectConfig) {
    List<Path> filesToBackup = new ArrayList<>();

    try {
      // 1. Application.yml will be modified if adapter has application properties
      AdapterMetadata metadata = loadAdapterMetadataWithFallback(
          config.type().name().toLowerCase(),
          projectPath,
          config);
      if (metadata.hasApplicationProperties()) {
        Path applicationYml = Path.of("src/main/resources/application.yml");
        if (fileSystemPort.exists(projectPath.resolve(applicationYml))) {
          filesToBackup.add(applicationYml);
        }
      }

      // 2. Build file will be modified if adapter has test dependencies
      if (metadata.hasTestDependencies()) {
        Path buildFile = determineBuildFilePath(projectPath, config).getFileName();
        if (buildFile != null && fileSystemPort.exists(projectPath.resolve(buildFile))) {
          // Get relative path from project root
          Path relativeBuildPath = projectPath.relativize(determineBuildFilePath(projectPath, config));
          filesToBackup.add(relativeBuildPath);
        }
      }

    } catch (Exception e) {
      logger.debug("Could not identify all files to backup: {}", e.getMessage());
      // Continue with partial backup list
    }

    return filesToBackup;
  }

  /**
   * Merges application properties from adapter template into existing
   * application.yml.
   * Only merges if the adapter has an applicationPropertiesTemplate defined.
   *
   * @param projectPath path to the project root
   * @param config      adapter configuration
   */
  private void mergeApplicationPropertiesIfNeeded(Path projectPath, AdapterConfig config) {
    try {
      // 1. Load adapter metadata to check if it has applicationPropertiesTemplate
      AdapterMetadata metadata = loadAdapterMetadataWithFallback(
          config.type().name().toLowerCase(),
          projectPath,
          config);

      if (!metadata.hasApplicationProperties()) {
        logger.debug("Adapter {} does not have application properties template, skipping merge",
            config.name());
        return;
      }

      logger.info("Processing application properties template for adapter: {}", config.name());

      // 2. Prepare template context with adapter information
      Map<String, Object> templateContext = prepareApplicationPropertiesContext(config);

      // 3. Process the application properties template
      String adapterTypeLower = config.type().name().toLowerCase();
      String propertiesTemplatePath = String.format("adapters/%s/%s", adapterTypeLower,
          metadata.applicationPropertiesTemplate());
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
   * @param config adapter configuration
   * @return template context map
   */
  private Map<String, Object> prepareApplicationPropertiesContext(AdapterConfig config) {
    Map<String, Object> context = new HashMap<>();
    context.put("adapterName", config.name());
    context.put("adapterType", config.type().name().toLowerCase());
    context.put("packageName", config.packageName());
    context.put("entityName", config.entityName());

    // Add project name if available (useful for database names, etc.)
    try {
      var projectConfig = configurationPort.readConfiguration(Path.of(".")).orElse(null);
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
   * Generates configuration classes if the adapter has configurationClasses
   * defined.
   * 
   * @param projectPath path to the project root
   * @param config      adapter configuration
   * @return list of generated configuration class files
   */
  private List<GeneratedFile> generateConfigurationClassesIfNeeded(Path projectPath, AdapterConfig config) {
    List<GeneratedFile> configFiles = new ArrayList<>();

    try {
      // 1. Load adapter metadata to check if it has configuration classes
      AdapterMetadata metadata = loadAdapterMetadataWithFallback(
          config.type().name().toLowerCase(),
          projectPath,
          config);

      if (!metadata.hasConfigurationClasses()) {
        logger.debug("Adapter {} does not have configuration classes, skipping generation",
            config.name());
        return configFiles;
      }

      logger.info("Generating configuration classes for adapter: {}", config.name());

      // 2. Read project configuration to get base package
      var projectConfig = configurationPort.readConfiguration(projectPath).orElse(null);
      if (projectConfig == null) {
        logger.warn("Could not read project configuration, skipping configuration class generation");
        return configFiles;
      }

      String basePackage = projectConfig.basePackage();

      // 3. Prepare template context
      Map<String, Object> templateContext = prepareConfigurationClassContext(config, projectConfig);

      // 4. Process each configuration class
      for (AdapterMetadata.ConfigurationClass configClass : metadata.configurationClasses()) {
        try {
          // Build template path: adapters/{adapterType}/{templatePath}
          String adapterTypeLower = config.type().name().toLowerCase();
          String configTemplatePath = String.format("adapters/%s/%s", adapterTypeLower,
              configClass.templatePath());

          // Process the template
          String processedContent = templateRepository.processTemplate(configTemplatePath, templateContext);

          // Resolve package path for the configuration class
          String fullPackage = basePackage + "." + configClass.packagePath();
          String packagePath = fullPackage.replace('.', '/');

          // Build file path: src/main/java/{basePackage}/{packagePath}/{ClassName}.java
          Path configFilePath = projectPath
              .resolve("src/main/java")
              .resolve(packagePath)
              .resolve(configClass.name() + ".java");

          // Create GeneratedFile
          GeneratedFile configFile = GeneratedFile.javaSource(configFilePath, processedContent);
          configFiles.add(configFile);

          logger.info("Generated configuration class: {} at {}", configClass.name(), configFilePath);

        } catch (TemplateRepository.TemplateNotFoundException e) {
          logger.warn("Configuration class template not found: {} for adapter {}",
              configClass.templatePath(), config.name());
        } catch (Exception e) {
          logger.error("Failed to generate configuration class {} for adapter {}: {}",
              configClass.name(), config.name(), e.getMessage(), e);
        }
      }

      logger.info("Successfully generated {} configuration class(es) for adapter: {}",
          configFiles.size(), config.name());

    } catch (TemplateRepository.TemplateNotFoundException e) {
      logger.warn("Adapter metadata not found for adapter {}: {}",
          config.name(), e.getMessage());
    } catch (Exception e) {
      logger.error("Failed to generate configuration classes for adapter {}: {}",
          config.name(), e.getMessage(), e);
      // Don't fail the entire generation if configuration class generation fails
      // The adapter code is already generated, configuration can be added manually
    }

    return configFiles;
  }

  /**
   * Prepares template context for processing configuration class templates.
   *
   * @param config        adapter configuration
   * @param projectConfig project configuration
   * @return template context map
   */
  private Map<String, Object> prepareConfigurationClassContext(AdapterConfig config,
      com.pragma.archetype.domain.model.config.ProjectConfig projectConfig) {
    Map<String, Object> context = new HashMap<>();
    context.put("adapterName", config.name());
    context.put("adapterType", config.type().name().toLowerCase());
    context.put("packageName", config.packageName());
    context.put("entityName", config.entityName());
    context.put("projectName", projectConfig.name());
    context.put("basePackage", projectConfig.basePackage());
    return context;
  }

  /**
   * Adds test dependencies to the build file if the adapter has testDependencies.
   * Test dependencies are added with testImplementation scope in Gradle.
   *
   * @param projectPath path to the project root
   * @param config      adapter configuration
   */
  private void addTestDependenciesIfNeeded(Path projectPath, AdapterConfig config) {
    try {
      // 1. Load adapter metadata to check if it has test dependencies
      AdapterMetadata metadata = loadAdapterMetadataWithFallback(
          config.type().name().toLowerCase(),
          projectPath,
          config);

      if (!metadata.hasTestDependencies()) {
        logger.debug("Adapter {} does not have test dependencies, skipping", config.name());
        return;
      }

      logger.info("Adding test dependencies for adapter: {}", config.name());

      // 2. Determine the build file path
      // For single-module projects: build.gradle.kts at project root
      // For multi-module projects: build.gradle.kts in the adapter module
      Path buildFilePath = determineBuildFilePath(projectPath, config);

      if (!fileSystemPort.exists(buildFilePath)) {
        logger.warn("Build file not found at {}, skipping test dependency addition", buildFilePath);
        return;
      }

      // 3. Read the current build file content
      String buildFileContent = fileSystemPort.readFile(buildFilePath);

      // 4. Add each test dependency
      String updatedContent = buildFileContent;
      for (AdapterMetadata.Dependency testDep : metadata.testDependencies()) {
        updatedContent = addTestDependencyToBuildFile(updatedContent, testDep);
      }

      // 5. Write the updated build file if changes were made
      if (!updatedContent.equals(buildFileContent)) {
        fileSystemPort.writeFile(GeneratedFile.create(buildFilePath, updatedContent));
        logger.info("Successfully added {} test dependencies for adapter: {}",
            metadata.testDependencies().size(), config.name());
      } else {
        logger.debug("No new test dependencies to add for adapter: {}", config.name());
      }

    } catch (TemplateRepository.TemplateNotFoundException e) {
      logger.warn("Adapter metadata not found for adapter {}: {}", config.name(), e.getMessage());
    } catch (Exception e) {
      logger.error("Failed to add test dependencies for adapter {}: {}",
          config.name(), e.getMessage(), e);
      // Don't fail the entire generation if test dependency addition fails
      // The adapter code is already generated, dependencies can be added manually
    }
  }

  /**
   * Determines the build file path based on project structure.
   *
   * @param projectPath path to the project root
   * @param config      adapter configuration
   * @return path to the build file
   */
  private Path determineBuildFilePath(Path projectPath, AdapterConfig config) {
    try {
      var projectConfig = configurationPort.readConfiguration(projectPath).orElse(null);

      // For multi-module projects with adaptersAsModules=true
      if (projectConfig != null && projectConfig.adaptersAsModules()) {
        String moduleName = config.name().toLowerCase().replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase();
        String modulePath = "infrastructure/driven-adapters/" + moduleName;
        return projectPath.resolve(modulePath).resolve("build.gradle.kts");
      }
    } catch (Exception e) {
      logger.debug("Could not read project configuration: {}", e.getMessage());
    }

    // Default: single-module project
    return projectPath.resolve("build.gradle.kts");
  }

  /**
   * Adds a test dependency to the build file content.
   * Uses testImplementation scope for Gradle.
   *
   * @param buildFileContent current build file content
   * @param dependency       test dependency to add
   * @return updated build file content
   */
  private String addTestDependencyToBuildFile(String buildFileContent, AdapterMetadata.Dependency dependency) {
    // Format: testImplementation("group:artifact:version")
    String dependencyStatement = String.format("    testImplementation(\"%s:%s:%s\")",
        dependency.group(), dependency.artifact(), dependency.version());

    // Check if dependency is already present
    if (buildFileContent.contains(dependencyStatement)) {
      logger.debug("Test dependency already present: {}", dependency.toCoordinate());
      return buildFileContent;
    }

    // Find the dependencies block and add the test dependency
    if (buildFileContent.contains("dependencies {")) {
      // Look for existing test dependencies section or add at the end of dependencies
      // block
      if (buildFileContent.contains("testImplementation(")) {
        // Add after the last testImplementation
        int lastTestImpl = buildFileContent.lastIndexOf("testImplementation(");
        int endOfLine = buildFileContent.indexOf('\n', lastTestImpl);
        if (endOfLine != -1) {
          return buildFileContent.substring(0, endOfLine + 1) +
              dependencyStatement + "\n" +
              buildFileContent.substring(endOfLine + 1);
        }
      }

      // If no testImplementation found, add before the closing brace of dependencies
      // block
      int dependenciesStart = buildFileContent.indexOf("dependencies {");
      int dependenciesEnd = findMatchingClosingBrace(buildFileContent, dependenciesStart + "dependencies {".length());

      if (dependenciesEnd != -1) {
        // Add before the closing brace
        return buildFileContent.substring(0, dependenciesEnd) +
            "\n    // Test dependencies\n" +
            dependencyStatement + "\n" +
            buildFileContent.substring(dependenciesEnd);
      }
    }

    logger.warn("Could not find dependencies block in build file, dependency not added: {}",
        dependency.toCoordinate());
    return buildFileContent;
  }

  /**
   * Checks for dependency conflicts before adding new dependencies.
   * Logs warnings for detected conflicts and suggests resolutions.
   * 
   * @param projectPath   the project root path
   * @param config        the adapter configuration
   * @param projectConfig the project configuration
   */
  private void checkDependencyConflicts(Path projectPath, AdapterConfig config,
      com.pragma.archetype.domain.model.config.ProjectConfig projectConfig) {

    if (projectConfig == null) {
      return;
    }

    try {
      // Load adapter metadata
      String adapterTypeLower = config.type().name().toLowerCase();
      AdapterMetadata metadata = loadAdapterMetadataWithFallback(
          adapterTypeLower,
          projectPath,
          config);

      // Get all dependencies (regular + test)
      List<AdapterMetadata.Dependency> allNewDependencies = new ArrayList<>();
      if (metadata.dependencies() != null) {
        allNewDependencies.addAll(metadata.dependencies());
      }
      if (metadata.testDependencies() != null) {
        allNewDependencies.addAll(metadata.testDependencies());
      }

      if (allNewDependencies.isEmpty()) {
        return;
      }

      // Apply version overrides if configured
      if (projectConfig.dependencyOverrides() != null && !projectConfig.dependencyOverrides().isEmpty()) {
        allNewDependencies = conflictDetector.applyVersionOverrides(
            allNewDependencies, projectConfig.dependencyOverrides());
        logger.info("Applied {} dependency version overrides", projectConfig.dependencyOverrides().size());
      }

      // TODO: Load existing dependencies from build.gradle
      // For now, we'll just check framework conflicts
      List<AdapterMetadata.Dependency> existingDependencies = List.of();

      // Detect version conflicts
      List<String> versionConflicts = conflictDetector.detectVersionConflicts(
          existingDependencies, allNewDependencies);

      // Detect framework conflicts
      String frameworkName = projectConfig.framework().name().toLowerCase();
      List<String> frameworkConflicts = conflictDetector.detectFrameworkConflicts(
          frameworkName, allNewDependencies);

      // Combine all conflicts
      List<String> allConflicts = new ArrayList<>();
      allConflicts.addAll(versionConflicts);
      allConflicts.addAll(frameworkConflicts);

      // Log warnings if conflicts detected
      if (!allConflicts.isEmpty()) {
        logger.warn("⚠ Dependency conflicts detected:");
        for (String conflict : allConflicts) {
          logger.warn("  - {}", conflict);
        }
        logger.warn("");

        // Log resolution suggestions
        List<String> suggestions = conflictDetector.suggestResolution(allConflicts);
        for (String suggestion : suggestions) {
          logger.warn(suggestion);
        }
      }

    } catch (Exception e) {
      logger.warn("Could not check for dependency conflicts: {}", e.getMessage());
    }
  }

  /**
   * Finds the matching closing brace for a given opening position.
   *
   * @param content  the content to search
   * @param startPos position after the opening brace
   * @return position of the matching closing brace, or -1 if not found
   */
  private int findMatchingClosingBrace(String content, int startPos) {
    int braceCount = 1;
    for (int i = startPos; i < content.length(); i++) {
      char c = content.charAt(i);
      if (c == '{') {
        braceCount++;
      } else if (c == '}') {
        braceCount--;
        if (braceCount == 0) {
          return i;
        }
      }
    }
    return -1;
  }

  /**
   * Determines the adapter type category for framework-aware template loading.
   * Maps adapter types to their category: "driven-adapters" or "entry-points".
   * 
   * @param config adapter configuration
   * @return adapter type category string
   */
  private String determineAdapterTypeCategory(AdapterConfig config) {
    // All current adapter types are driven adapters (output/infrastructure
    // adapters)
    // In the future, entry points (REST, GRAPHQL, GRPC, WEBSOCKET) will be added
    return "driven-adapters";
  }

  /**
   * Loads adapter metadata using framework-aware path if projectConfig is
   * available,
   * otherwise falls back to legacy loading.
   * 
   * @param adapterTypeLower adapter type in lowercase
   * @param projectPath      path to project (to read config if needed)
   * @param config           adapter configuration
   * @return AdapterMetadata
   */
  private AdapterMetadata loadAdapterMetadataWithFallback(
      String adapterTypeLower,
      Path projectPath,
      AdapterConfig config) {
    try {
      // Try to read project config
      var projectConfig = configurationPort.readConfiguration(projectPath).orElse(null);

      if (projectConfig != null) {
        // Use framework-aware loading
        String framework = projectConfig.framework().name().toLowerCase();
        String paradigm = projectConfig.paradigm().name().toLowerCase();
        String adapterTypeCategory = determineAdapterTypeCategory(config);

        return templateRepository.loadAdapterMetadata(
            adapterTypeLower,
            framework,
            paradigm,
            adapterTypeCategory);
      }
    } catch (Exception e) {
      logger.debug("Could not load with framework-aware path, falling back to legacy: {}", e.getMessage());
    }

    // Fallback to legacy loading
    return templateRepository.loadAdapterMetadata(adapterTypeLower);
  }
}
