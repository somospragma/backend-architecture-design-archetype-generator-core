package com.pragma.archetype.application.usecase;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pragma.archetype.application.generator.OutputAdapterGenerator;
import com.pragma.archetype.domain.model.adapter.AdapterMetadata;
import com.pragma.archetype.domain.model.adapter.OutputAdapterConfig;
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
  private final OutputAdapterGenerator generator;
  private final ConfigurationPort configurationPort;
  private final FileSystemPort fileSystemPort;
  private final TemplateRepository templateRepository;
  private final YamlConfigurationAdapter yamlConfigurationAdapter;
  private final BackupService backupService;
  private final com.pragma.archetype.domain.service.DependencyConflictDetector conflictDetector;
  private final com.pragma.archetype.domain.service.AdapterConfigurationService adapterConfigurationService;

  public GenerateAdapterUseCaseImpl(
      AdapterValidator validator,
      OutputAdapterGenerator generator,
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
    this.adapterConfigurationService = new com.pragma.archetype.domain.service.AdapterConfigurationService(
        configurationPort, fileSystemPort, templateRepository, yamlConfigurationAdapter);
  }

  @Override
  public GenerationResult execute(Path projectPath, OutputAdapterConfig config) {
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

      // 7. Load adapter metadata for configuration tasks
      AdapterMetadata metadata = loadAdapterMetadataWithFallback(
          config.type().toLowerCase(),
          projectPath,
          config);

      // 8. Create adapter config for shared service
      var adapterConfig = new com.pragma.archetype.domain.service.AdapterConfigurationService.AdapterConfig(
          config.name(),
          config.type(),
          projectConfig.framework().name(),
          projectConfig.paradigm().name(),
          config.packageName(),
          null, // useCaseName not applicable for output adapters
          config.entityName(),
          com.pragma.archetype.domain.model.adapter.AdapterDirection.OUTPUT);

      // 9. Merge application properties if adapter has applicationPropertiesTemplate
      adapterConfigurationService.mergeApplicationPropertiesIfNeeded(projectPath, adapterConfig, metadata);

      // 10. Generate configuration classes if adapter has configurationClasses
      List<GeneratedFile> configFiles = adapterConfigurationService.generateConfigurationClassesIfNeeded(
          projectPath, adapterConfig, metadata);
      generatedFiles.addAll(configFiles);

      // 11. Add dependencies if adapter has dependencies
      adapterConfigurationService.addDependenciesIfNeeded(projectPath, adapterConfig, metadata);

      // 12. Add build plugins and configuration if adapter has them
      com.pragma.archetype.domain.model.structure.StructureMetadata structureMetadata = templateRepository
          .loadStructureMetadata(projectConfig.architecture());
      adapterConfigurationService.addBuildPluginsAndConfigurationIfNeeded(
          projectPath, adapterConfig, metadata, structureMetadata);

      // 13. Add test dependencies if adapter has testDependencies
      adapterConfigurationService.addTestDependenciesIfNeeded(projectPath, adapterConfig, metadata);

      // 14. Check for dependency conflicts (informational only)
      checkDependencyConflicts(projectPath, config, projectConfig);

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
  private ValidationResult validateAllTemplates(OutputAdapterConfig config,
      com.pragma.archetype.domain.model.config.ProjectConfig projectConfig) {
    List<String> errors = new ArrayList<>();
    List<String> warnings = new ArrayList<>();

    try {
      // 1. Load adapter metadata using framework-aware path
      String adapterTypeLower = config.type().toLowerCase();
      AdapterMetadata metadata;
      try {
        if (projectConfig != null) {
          // Use framework-aware loading
          String framework = projectConfig.framework().name().toLowerCase();
          String paradigm = projectConfig.paradigm().name().toLowerCase();
          String adapterTypeCategory = "driven-adapters"; // Output adapters are always driven-adapters

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
            config.type(), e.getMessage()));
        return ValidationResult.failure(errors);
      }

      // 2. Validate adapter metadata
      ValidationResult metadataValidation = metadata.validate();
      if (!metadataValidation.valid()) {
        errors.addAll(metadataValidation.errors());
      }

      // 3. Validate main adapter templates (from AdapterGenerator)
      String adapterTemplate = getAdapterTemplatePath(config.type(), projectConfig);
      ValidationResult adapterTemplateValidation = templateRepository.validateTemplate(adapterTemplate);
      if (!adapterTemplateValidation.valid()) {
        errors.add(String.format("Main adapter template validation failed: %s",
            adapterTemplateValidation.errors().get(0)));
      }

      String dataEntityTemplate = getDataEntityTemplatePath(config.type(), projectConfig);
      if (templateRepository.templateExists(dataEntityTemplate)) {
        ValidationResult entityTemplateValidation = templateRepository.validateTemplate(dataEntityTemplate);
        if (!entityTemplateValidation.valid()) {
          errors.add(String.format("Data entity template validation failed: %s",
              entityTemplateValidation.errors().get(0)));
        }
      }
      // Entity template is optional - not all adapters need it

      String mapperTemplate = getMapperTemplatePath(projectConfig);
      if (templateRepository.templateExists(mapperTemplate)) {
        ValidationResult mapperTemplateValidation = templateRepository.validateTemplate(mapperTemplate);
        if (!mapperTemplateValidation.valid()) {
          errors.add(String.format("Mapper template validation failed: %s",
              mapperTemplateValidation.errors().get(0)));
        }
      }
      // Mapper template is optional - not all adapters need it

      // 4. Validate application properties template if present
      if (metadata.hasApplicationProperties()) {
        String framework = projectConfig != null ? projectConfig.framework().name().toLowerCase() : "spring";
        String paradigm = projectConfig != null ? projectConfig.paradigm().name().toLowerCase() : "reactive";
        String adapterTypeCategory = "driven-adapters"; // Output adapters are always driven-adapters

        String propertiesTemplatePath = String.format("frameworks/%s/%s/adapters/%s/%s/%s",
            framework, paradigm, adapterTypeCategory, adapterTypeLower, metadata.applicationPropertiesTemplate());

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
        String framework = projectConfig != null ? projectConfig.framework().name().toLowerCase() : "spring";
        String paradigm = projectConfig != null ? projectConfig.paradigm().name().toLowerCase() : "reactive";
        String adapterTypeCategory = "driven-adapters"; // Output adapters are always driven-adapters

        for (AdapterMetadata.ConfigurationClass configClass : metadata.configurationClasses()) {
          String configTemplatePath = String.format("frameworks/%s/%s/adapters/%s/%s/%s",
              framework, paradigm, adapterTypeCategory, adapterTypeLower, configClass.templatePath());

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
        String paradigm = projectConfig.paradigm().name().toLowerCase();
        String moduleBuildTemplate = String
            .format("frameworks/spring/%s/adapters/driven-adapters/module-build.gradle.kts.ftl", paradigm);
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
   * 
   * @param type          adapter type
   * @param projectConfig project configuration (may be null)
   * @return template path
   * @deprecated This method uses hardcoded paths. Template validation should be
   *             done in the generator.
   */
  @Deprecated
  private String getAdapterTemplatePath(String type,
      com.pragma.archetype.domain.model.config.ProjectConfig projectConfig) {
    // Return generic path - actual validation happens in generator with dynamic
    // discovery
    String typeLower = type.toLowerCase().replace("_", "-");
    String paradigm = projectConfig != null ? projectConfig.paradigm().name().toLowerCase() : "reactive";
    return String.format("frameworks/spring/%s/adapters/driven-adapters/%s/Adapter.java.ftl", paradigm, typeLower);
  }

  /**
   * Gets the data entity template path for a given adapter type.
   * 
   * @param type          adapter type
   * @param projectConfig project configuration (may be null)
   * @return template path
   * @deprecated This method uses hardcoded paths. Template validation should be
   *             done in the generator.
   */
  @Deprecated
  private String getDataEntityTemplatePath(String type,
      com.pragma.archetype.domain.model.config.ProjectConfig projectConfig) {
    // Return generic path - actual validation happens in generator with dynamic
    // discovery
    String typeLower = type.toLowerCase().replace("_", "-");
    String paradigm = projectConfig != null ? projectConfig.paradigm().name().toLowerCase() : "reactive";
    return String.format("frameworks/spring/%s/adapters/driven-adapters/%s/Entity.java.ftl", paradigm, typeLower);
  }

  /**
   * Gets the mapper template path.
   * This mirrors the logic in AdapterGenerator.getMapperTemplate().
   * 
   * @param projectConfig project configuration (may be null)
   * @return template path
   */
  private String getMapperTemplatePath(com.pragma.archetype.domain.model.config.ProjectConfig projectConfig) {
    String paradigm = projectConfig != null ? projectConfig.paradigm().name().toLowerCase() : "reactive";
    return String.format("frameworks/spring/%s/adapters/driven-adapters/generic/Mapper.java.ftl", paradigm);
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
  private List<Path> identifyFilesToBackup(Path projectPath, OutputAdapterConfig config,
      com.pragma.archetype.domain.model.config.ProjectConfig projectConfig) {
    List<Path> filesToBackup = new ArrayList<>();

    try {
      // 1. Application.yml will be modified if adapter has application properties
      AdapterMetadata metadata = loadAdapterMetadataWithFallback(
          config.type().toLowerCase(),
          projectPath,
          config);
      if (metadata.hasApplicationProperties()) {
        Path applicationYml = Path.of("src/main/resources/application.yml");
        if (fileSystemPort.exists(projectPath.resolve(applicationYml))) {
          filesToBackup.add(applicationYml);
        }
      }

      // 2. Build file will be modified if adapter has dependencies or test
      // dependencies
      if (!metadata.getDependencies().isEmpty() || metadata.hasTestDependencies()) {
        Path buildFile = Path.of("build.gradle.kts");
        if (fileSystemPort.exists(projectPath.resolve(buildFile))) {
          filesToBackup.add(buildFile);
        }
      }

    } catch (Exception e) {
      logger.debug("Could not identify all files to backup: {}", e.getMessage());
      // Continue with partial backup list
    }

    return filesToBackup;
  }

  /**
   * Checks for dependency conflicts before adding new dependencies.
   * Logs warnings for detected conflicts and suggests resolutions.
   * 
   * @param projectPath   the project root path
   * @param config        the adapter configuration
   * @param projectConfig the project configuration
   */
  private void checkDependencyConflicts(Path projectPath, OutputAdapterConfig config,
      com.pragma.archetype.domain.model.config.ProjectConfig projectConfig) {

    if (projectConfig == null) {
      return;
    }

    try {
      // Load adapter metadata
      String adapterTypeLower = config.type().toLowerCase();
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
      OutputAdapterConfig config) {
    try {
      // Try to read project config
      var projectConfig = configurationPort.readConfiguration(projectPath).orElse(null);

      if (projectConfig != null) {
        // Use framework-aware loading
        String framework = projectConfig.framework().name().toLowerCase();
        String paradigm = projectConfig.paradigm().name().toLowerCase();
        String adapterTypeCategory = "driven-adapters"; // Output adapters are always driven-adapters

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
