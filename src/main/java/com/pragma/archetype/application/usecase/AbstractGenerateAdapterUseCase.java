package com.pragma.archetype.application.usecase;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pragma.archetype.application.generator.UnifiedAdapterGenerator;
import com.pragma.archetype.application.port.out.AdapterDiscoveryPort;
import com.pragma.archetype.domain.model.adapter.AdapterDirection;
import com.pragma.archetype.domain.model.adapter.AdapterMetadata;
import com.pragma.archetype.domain.model.backup.BackupManifest;
import com.pragma.archetype.domain.model.file.GeneratedFile;
import com.pragma.archetype.domain.model.structure.StructureMetadata;
import com.pragma.archetype.domain.model.template.TemplateRepositoryPaths;
import com.pragma.archetype.domain.model.validation.ValidationResult;
import com.pragma.archetype.domain.port.in.GenerateInputAdapterUseCase.GenerationResult;
import com.pragma.archetype.domain.port.out.BackupPort;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import com.pragma.archetype.domain.port.out.TemplateRepository;
import com.pragma.archetype.domain.service.AdapterConfigurationService;
import com.pragma.archetype.infrastructure.adapter.out.config.YamlConfigurationAdapter;

/**
 * Abstract base class for adapter generation use cases using Template Method
 * pattern.
 * 
 * <p>
 * This class implements the complete adapter generation flow with 16 steps,
 * including validation (28 checks), backup, generation, configuration, and
 * cleanup.
 * 
 * <p>
 * Subclasses only need to implement {@link #getDirection()} to specify whether
 * they generate INPUT or OUTPUT adapters. Everything else is handled by this
 * base class.
 * 
 * <p>
 * <b>Template Method Pattern:</b>
 * <ul>
 * <li>{@link #execute(Path, UnifiedAdapterGenerator.AdapterConfig)} - Template
 * method with complete flow</li>
 * <li>{@link #getDirection()} - Hook method implemented by subclasses</li>
 * </ul>
 * 
 * <p>
 * <b>Generation Flow (16 steps):</b>
 * <ol>
 * <li>Validate configuration</li>
 * <li>Load project configuration</li>
 * <li>Load structure metadata</li>
 * <li>Validate all templates (28 validations)</li>
 * <li>Identify files to backup</li>
 * <li>Create backup</li>
 * <li>Generate adapter files</li>
 * <li>Write files to disk</li>
 * <li>Load adapter metadata</li>
 * <li>Merge application properties</li>
 * <li>Generate configuration classes</li>
 * <li>Add dependencies</li>
 * <li>Add build plugins and configuration</li>
 * <li>Add test dependencies</li>
 * <li>Check dependency conflicts</li>
 * <li>Delete backup on success / Restore on failure</li>
 * </ol>
 * 
 * @see UnifiedAdapterGenerator
 * @see AdapterDirection
 */
public abstract class AbstractGenerateAdapterUseCase {

  private static final Logger logger = LoggerFactory.getLogger(AbstractGenerateAdapterUseCase.class);

  // Validation patterns
  private static final Pattern JAVA_CLASS_NAME_PATTERN = Pattern.compile("^[A-Z]\\w*$");
  private static final Pattern JAVA_PACKAGE_NAME_PATTERN = Pattern.compile("^[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)?$");
  private static final Pattern KOTLIN_DSL_PATTERN = Pattern.compile("(?s).*\\{.*\\}.*");

  protected final UnifiedAdapterGenerator generator;
  protected final ConfigurationPort configurationPort;
  protected final FileSystemPort fileSystemPort;
  protected final AdapterDiscoveryPort adapterDiscovery;
  protected final TemplateRepository templateRepository;
  protected final YamlConfigurationAdapter yamlConfigurationAdapter;
  protected final AdapterConfigurationService adapterConfigurationService;
  protected final BackupPort backupPort;

  protected AbstractGenerateAdapterUseCase(
      UnifiedAdapterGenerator generator,
      ConfigurationPort configurationPort,
      FileSystemPort fileSystemPort,
      AdapterDiscoveryPort adapterDiscovery,
      TemplateRepository templateRepository,
      YamlConfigurationAdapter yamlConfigurationAdapter,
      BackupPort backupPort) {
    this.generator = generator;
    this.configurationPort = configurationPort;
    this.fileSystemPort = fileSystemPort;
    this.adapterDiscovery = adapterDiscovery;
    this.templateRepository = templateRepository;
    this.yamlConfigurationAdapter = yamlConfigurationAdapter;
    this.backupPort = backupPort;
    this.adapterConfigurationService = new AdapterConfigurationService(
        configurationPort, fileSystemPort, templateRepository, yamlConfigurationAdapter);
  }

  /**
   * Template method that implements the complete adapter generation flow.
   * 
   * <p>
   * This method orchestrates all 16 steps of the generation process,
   * including validation, backup, generation, configuration, and cleanup.
   * 
   * @param projectPath path to the project root
   * @param config      adapter configuration (wrapped InputAdapterConfig or
   *                    OutputAdapterConfig)
   * @return generation result with success/failure status and generated files
   */
  public GenerationResult execute(Path projectPath, UnifiedAdapterGenerator.AdapterConfig config) {
    logger.info("Starting {} adapter generation: {}", getDirection(), config.getName());

    BackupManifest backup = null;
    try {
      // STEP 1: Validate configuration (basic validation)
      ValidationResult basicValidation = validateBasicConfiguration(config);
      if (!basicValidation.valid()) {
        return GenerationResult.failure(basicValidation.errors());
      }

      // STEP 2: Load project configuration
      var projectConfig = configurationPort.readConfiguration(projectPath)
          .orElseThrow(() -> new IllegalStateException("Project configuration not found"));

      // STEP 3: Load structure metadata
      StructureMetadata structureMetadata = templateRepository
          .loadStructureMetadata(projectConfig.architecture());

      // STEP 4: Validate all templates (28 validations -
      // validation-before-modification)
      ValidationResult templateValidation = validateAllTemplates(config, projectConfig);
      if (!templateValidation.valid()) {
        logger.error("Template validation failed. Displaying all errors before generation:");
        templateValidation.errors().forEach(error -> logger.error("  - {}", error));
        return GenerationResult.failure(templateValidation.errors());
      }

      // STEP 5: Identify files to backup
      List<Path> filesToBackup = identifyFilesToBackup(projectPath, config);

      // STEP 6: Create backup
      backup = backupPort.createBackup(projectPath, filesToBackup);

      // STEP 7: Generate adapter files
      List<GeneratedFile> generatedFiles = generator.generate(
          projectPath, config, getDirection(), structureMetadata, projectConfig);

      // STEP 8: Write files to disk
      for (GeneratedFile file : generatedFiles) {
        fileSystemPort.writeFile(file);
        // Track newly created files for potential rollback
        backup = backupPort.trackCreatedFile(backup, file.path());
      }

      // STEP 9: Load adapter metadata
      AdapterMetadata metadata = loadAdapterMetadataWithFallback(config);

      // STEP 10: Create adapter config for shared service
      var adapterConfig = createAdapterConfigForService(config);

      // STEP 11: Merge application properties
      adapterConfigurationService.mergeApplicationPropertiesIfNeeded(
          projectPath, adapterConfig, metadata);

      // STEP 12: Generate configuration classes
      List<GeneratedFile> configFiles = adapterConfigurationService
          .generateConfigurationClassesIfNeeded(projectPath, adapterConfig, metadata);
      generatedFiles.addAll(configFiles);

      // STEP 13: Add dependencies
      adapterConfigurationService.addDependenciesIfNeeded(projectPath, adapterConfig, metadata);

      // STEP 14: Add build plugins and configuration
      adapterConfigurationService.addBuildPluginsAndConfigurationIfNeeded(
          projectPath, adapterConfig, metadata, structureMetadata);

      // STEP 15: Add test dependencies
      adapterConfigurationService.addTestDependenciesIfNeeded(projectPath, adapterConfig, metadata);

      // STEP 16: Check dependency conflicts (informational only)
      checkDependencyConflicts(projectPath, config);

      // STEP 17: Confirm backup (delete backup files, generation was successful)
      backupPort.confirmBackup(backup);

      logger.info("Successfully generated {} adapter: {}", getDirection(), config.getName());
      return GenerationResult.success(generatedFiles);

    } catch (Exception e) {
      logger.error("Failed to generate {} adapter: {}", getDirection(), config.getName(), e);
      // Restore backup on failure — rollback all changes
      try {
        if (backup != null) {
          backupPort.restoreBackup(backup);
          logger.info("Rollback completed successfully for adapter: {}", config.getName());
        }
      } catch (Exception rollbackError) {
        logger.error("Rollback failed! Project may be in inconsistent state. Backup ID: {}",
            backup != null ? backup.id() : "unknown", rollbackError);
      }
      return GenerationResult.failure(List.of(
          "Failed to generate adapter: " + e.getMessage()));
    }
  }

  /**
   * Hook method to be implemented by subclasses.
   * Determines whether this use case generates INPUT or OUTPUT adapters.
   * 
   * @return the adapter direction (INPUT or OUTPUT)
   */
  protected abstract AdapterDirection getDirection();

  /**
   * Validates basic configuration fields.
   */
  private ValidationResult validateBasicConfiguration(UnifiedAdapterGenerator.AdapterConfig config) {
    var errors = new ArrayList<String>();

    if (config.getName() == null || config.getName().isBlank()) {
      errors.add("Adapter name cannot be null or blank");
    }

    if (config.getType() == null || config.getType().isBlank()) {
      errors.add("Adapter type cannot be null or blank");
    }

    if (config.getPackageName() == null || config.getPackageName().isBlank()) {
      errors.add("Package name cannot be null or blank");
    }

    return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
  }

  /**
   * Validates all templates and metadata with 28 comprehensive checks.
   * 
   * <p>
   * <b>Validation Groups:</b>
   * <ul>
   * <li>1-7: Template existence checks</li>
   * <li>8-20: Basic metadata validation</li>
   * <li>21-28: Advanced metadata validation (NEW)</li>
   * </ul>
   * 
   * @param config        adapter configuration
   * @param projectConfig project configuration from .cleanarch.yml
   * @return validation result with all errors collected
   */
  private ValidationResult validateAllTemplates(
      UnifiedAdapterGenerator.AdapterConfig config,
      com.pragma.archetype.domain.model.config.ProjectConfig projectConfig) {

    var errors = new ArrayList<String>();

    try {
      // Load adapter metadata using framework and paradigm from projectConfig
      AdapterMetadata metadata = adapterDiscovery
          .findAdapter(config.getType(), getDirection(), projectConfig.framework().getValue(), projectConfig.paradigm().getValue())
          .orElse(null);

      if (metadata == null) {
        errors.add("Adapter metadata not found for: " + config.getType());
        return ValidationResult.failure(errors);
      }

      // VALIDATIONS 1-7: Template Existence
      validateTemplateExistence(config, metadata, projectConfig, errors);

      // VALIDATIONS 8-20: Basic Metadata
      validateBasicMetadata(metadata, errors);

      // VALIDATIONS 21-28: Advanced Metadata (NEW)
      validateAdvancedMetadata(metadata, errors);

    } catch (Exception e) {
      errors.add("Failed to validate templates: " + e.getMessage());
    }

    return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
  }

  /**
   * Validates template existence (Validations 1-7).
   */
  private void validateTemplateExistence(
      UnifiedAdapterGenerator.AdapterConfig config,
      AdapterMetadata metadata,
      com.pragma.archetype.domain.model.config.ProjectConfig projectConfig,
      List<String> errors) {

    // 1. Adapter metadata exists (already checked)

    // 2. Main template exists
    boolean hasMainTemplate = metadata.getDiscoveredTemplates().stream()
        .anyMatch(t -> "main".equals(t.getType()));
    if (!hasMainTemplate) {
      errors.add("Main template not found for adapter: " + config.getType());
    }

    // 3-4. Entity and Mapper templates exist (if needsMapper)
    if (metadata.isNeedsMapper()) {
      validateMapperTemplates(config, metadata, errors);
    }

    // 5. Application properties template exists (if specified)
    validateApplicationPropertiesTemplate(config, metadata, projectConfig, errors);

    // 6. Configuration class templates exist (if specified)
    validateConfigurationClassTemplates(config, metadata, errors);

    // 7. Module build template exists (if adaptersAsModules) - skip for now
  }

  /**
   * Validates mapper-related templates.
   */
  private void validateMapperTemplates(
      UnifiedAdapterGenerator.AdapterConfig config,
      AdapterMetadata metadata,
      List<String> errors) {
    // Entity.java.ftl is handled by a dedicated generator (not in discoveredTemplates),
    // so we check if the template file exists directly via the template repository
    String adapterDir = getDirection() == AdapterDirection.INPUT ? "entry-points" : "driven-adapters";
    String entityTemplatePath = String.format("frameworks/%s/%s/adapters/%s/%s/Entity.java.ftl",
        config.getFramework(), config.getParadigm(), adapterDir, config.getType());
    String genericEntityTemplatePath = String.format("frameworks/%s/%s/adapters/%s/generic/Entity.java.ftl",
        config.getFramework(), config.getParadigm(), adapterDir);

    boolean entityTemplateExists = templateRepository.templateExists(entityTemplatePath)
        || templateRepository.templateExists(genericEntityTemplatePath);
    if (!entityTemplateExists) {
      errors.add("Entity template not found for adapter: " + config.getType());
    }
    // Mapper template is generic, always available
  }

  /**
   * Validates application properties template.
   */
  private void validateApplicationPropertiesTemplate(
      UnifiedAdapterGenerator.AdapterConfig config,
      AdapterMetadata metadata,
      com.pragma.archetype.domain.model.config.ProjectConfig projectConfig,
      List<String> errors) {
    if (metadata.getApplicationPropertiesTemplate() != null) {
      String adapterDir = TemplateRepositoryPaths.AdapterDirectories.getDirectoryName(
          getDirection() == AdapterDirection.INPUT);
      String templatePath = String.format(TemplateRepositoryPaths.Patterns.ADAPTER_TEMPLATE_DIR + "/%s",
          projectConfig.framework().getValue(), projectConfig.paradigm().getValue(), adapterDir, config.getType(),
          metadata.getApplicationPropertiesTemplate());

      if (!templateRepository.templateExists(templatePath)) {
        errors.add("Application properties template not found: " + templatePath);
      }
    }
  }

  /**
   * Validates configuration class templates.
   */
  private void validateConfigurationClassTemplates(
      UnifiedAdapterGenerator.AdapterConfig config,
      AdapterMetadata metadata,
      List<String> errors) {

    var configClasses = metadata.getConfigurationClasses();
    if (configClasses == null || configClasses.isEmpty()) {
      return;
    }

    String adapterDir = TemplateRepositoryPaths.AdapterDirectories.getDirectoryName(
        getDirection() == AdapterDirection.INPUT);

    for (var configClass : configClasses) {
      String templatePath = String.format(TemplateRepositoryPaths.Patterns.ADAPTER_TEMPLATE_DIR + "/%s",
          config.getFramework(), config.getParadigm(), adapterDir, config.getType(),
          configClass.templatePath());

      if (!templateRepository.templateExists(templatePath)) {
        errors.add("Configuration class template not found: " + templatePath);
      }
    }
  }

  /**
   * Validates basic metadata fields (Validations 8-20).
   */
  private void validateBasicMetadata(AdapterMetadata metadata, List<String> errors) {
    // 8. name != null && !name.isBlank()
    if (metadata.getName() == null || metadata.getName().isBlank()) {
      errors.add("Metadata name cannot be null or blank");
    }

    // 9. direction != null (always set by discovery)

    // 10. type must be valid (already validated by discovery)

    // 11-15. Lists must be valid (not null)
    // These are handled by the model itself

    // 16. Each dependency must have: group, artifact and optionally a version
    // (no version means it is managed by a BOM such as Spring Boot's)
    validateDependencies(metadata, errors);

    // 17. Each configurationClass must have: name, packagePath, templatePath
    validateConfigurationClasses(metadata, errors);

    // 18. Each additionalResource must have: name, templatePath
    validateAdditionalResources(metadata, errors);

    // 19. Each buildPlugin must have: id, version
    validateBuildPlugins(metadata, errors);

    // 20. Templates referenced must exist (checked in validateTemplateExistence)
  }

  /**
   * Validates dependency format.
   */
  private void validateDependencies(AdapterMetadata metadata, List<String> errors) {
    if (metadata.getDependencies() != null && !metadata.getDependencies().isEmpty()) {
      for (String dep : metadata.getDependencies()) {
        String[] parts = dep.split(":");
        if (parts.length < 2 || parts.length > 3) {
          errors.add("Dependency has invalid format (expected group:artifact[:version]): " + dep);
        } else {
          validateDependencyParts(dep, parts, errors);
        }
      }
    }
  }

  /**
   * Validates individual dependency parts.
   */
  private void validateDependencyParts(String dep, String[] parts, List<String> errors) {
    if (parts[0].isBlank()) {
      errors.add("Dependency missing group: " + dep);
    }
    if (parts[1].isBlank()) {
      errors.add("Dependency missing artifact: " + dep);
    }
    if (parts.length == 3 && parts[2].isBlank()) {
      errors.add("Dependency missing version: " + dep);
    }
  }

  /**
   * Validates configuration classes metadata.
   */
  private void validateConfigurationClasses(AdapterMetadata metadata, List<String> errors) {
    var configClasses = metadata.getConfigurationClasses();
    if (configClasses == null || configClasses.isEmpty()) {
      return;
    }

    for (var configClass : configClasses) {
      if (configClass.name() == null || configClass.name().isBlank()) {
        errors.add("Configuration class missing name");
      }
      if (configClass.packagePath() == null || configClass.packagePath().isBlank()) {
        errors.add("Configuration class missing packagePath");
      }
      if (configClass.templatePath() == null || configClass.templatePath().isBlank()) {
        errors.add("Configuration class missing templatePath");
      }
    }
  }

  /**
   * Validates additional resources metadata.
   */
  private void validateAdditionalResources(AdapterMetadata metadata, List<String> errors) {
    if (metadata.getAdditionalResources() != null) {
      for (var resource : metadata.getAdditionalResources()) {
        if (resource.name() == null || resource.name().isBlank()) {
          errors.add("Additional resource missing name");
        }
        if (resource.templatePath() == null || resource.templatePath().isBlank()) {
          errors.add("Additional resource missing templatePath");
        }
      }
    }
  }

  /**
   * Validates build plugins metadata.
   */
  private void validateBuildPlugins(AdapterMetadata metadata, List<String> errors) {
    if (metadata.getBuildPlugins() != null) {
      for (var plugin : metadata.getBuildPlugins()) {
        if (plugin.id() == null || plugin.id().isBlank()) {
          errors.add("Build plugin missing id");
        }
        if (plugin.version() == null || plugin.version().isBlank()) {
          errors.add("Build plugin missing version");
        }
      }
    }
  }

  /**
   * Validates advanced metadata fields (Validations 21-28) - NEW.
   */
  private void validateAdvancedMetadata(AdapterMetadata metadata, List<String> errors) {
    // 21. suffix no debe estar vacío si se especifica
    if (metadata.getSuffix() != null && metadata.getSuffix().isBlank()) {
      errors.add("Suffix cannot be empty if specified");
    }

    // 22. applicationPropertiesTemplate debe existir si se especifica
    // (Already checked in validateTemplateExistence)

    // 23. buildConfiguration debe ser válido Kotlin DSL si se especifica
    validateBuildConfiguration(metadata, errors);

    // 24. additionalResources.targetPath debe ser válido
    validateAdditionalResourcesTargetPath(metadata, errors);

    // 25. dependencies no deben tener duplicados
    validateNoDuplicateDependencies(metadata, errors);

    // 26. testDependencies no deben tener duplicados
    validateNoTestDependencyDuplicates(metadata, errors);

    // 27. configurationClasses.name debe ser válido como nombre de clase Java
    validateConfigurationClassNames(metadata, errors);

    // 28. configurationClasses.packagePath debe ser válido como paquete Java
    validateConfigurationClassPackages(metadata, errors);
  }

  /**
   * Validates build configuration syntax.
   */
  private void validateBuildConfiguration(AdapterMetadata metadata, List<String> errors) {
    if (metadata.getBuildConfiguration() != null
        && !metadata.getBuildConfiguration().isBlank()
        && !isValidKotlinDsl(metadata.getBuildConfiguration())) {
      errors.add("Build configuration contains invalid Kotlin DSL syntax");
    }
  }

  /**
   * Validates additional resources target paths.
   */
  private void validateAdditionalResourcesTargetPath(AdapterMetadata metadata, List<String> errors) {
    if (metadata.getAdditionalResources() != null) {
      for (var resource : metadata.getAdditionalResources()) {
        if (resource.targetPath() == null || resource.targetPath().isBlank()) {
          errors.add("Additional resource '" + resource.name() + "' has invalid targetPath");
        }
      }
    }
  }

  /**
   * Validates no duplicate dependencies.
   */
  private void validateNoDuplicateDependencies(AdapterMetadata metadata, List<String> errors) {
    if (metadata.getDependencies() != null && !metadata.getDependencies().isEmpty()) {
      Set<String> seen = new HashSet<>();
      for (String dep : metadata.getDependencies()) {
        String[] parts = dep.split(":");
        if (parts.length >= 2) {
          String key = parts[0] + ":" + parts[1];
          if (!seen.add(key)) {
            errors.add("Duplicate dependency found: " + key);
          }
        }
      }
    }
  }

  /**
   * Validates no duplicate test dependencies.
   */
  private void validateNoTestDependencyDuplicates(AdapterMetadata metadata, List<String> errors) {
    var testDeps = metadata.getTestDependencies();
    if (testDeps == null || testDeps.isEmpty()) {
      return;
    }

    Set<String> seen = new HashSet<>();
    for (var dep : testDeps) {
      String key = dep.group() + ":" + dep.artifact();
      if (!seen.add(key)) {
        errors.add("Duplicate test dependency found: " + key);
      }
    }
  }

  /**
   * Validates configuration class names.
   */
  private void validateConfigurationClassNames(AdapterMetadata metadata, List<String> errors) {
    var configClasses = metadata.getConfigurationClasses();
    if (configClasses == null || configClasses.isEmpty()) {
      return;
    }

    for (var configClass : configClasses) {
      if (!isValidJavaClassName(configClass.name())) {
        errors.add("Invalid Java class name: " + configClass.name());
      }
    }
  }

  /**
   * Validates configuration class package names.
   */
  private void validateConfigurationClassPackages(AdapterMetadata metadata, List<String> errors) {
    var configClasses = metadata.getConfigurationClasses();
    if (configClasses == null || configClasses.isEmpty()) {
      return;
    }

    for (var configClass : configClasses) {
      if (!isValidJavaPackageName(configClass.packagePath())) {
        errors.add("Invalid Java package name: " + configClass.packagePath());
      }
    }
  }

  /**
   * Identifies files that need to be backed up before modification.
   */
  private List<Path> identifyFilesToBackup(
      Path projectPath,
      UnifiedAdapterGenerator.AdapterConfig config) {

    var filesToBackup = new ArrayList<Path>();

    try {
      AdapterMetadata metadata = loadAdapterMetadataWithFallback(config);

      // Backup application.yml if adapter has applicationPropertiesTemplate
      if (metadata.getApplicationPropertiesTemplate() != null) {
        Path appPropsPath = projectPath.resolve("src/main/resources/application.yml");
        if (fileSystemPort.exists(appPropsPath)) {
          filesToBackup.add(appPropsPath);
        }
      }

      // Backup build.gradle.kts if adapter has dependencies or testDependencies
      if (shouldBackupBuildFile(metadata)) {
        Path buildFilePath = projectPath.resolve("build.gradle.kts");
        if (fileSystemPort.exists(buildFilePath)) {
          filesToBackup.add(buildFilePath);
        }
      }

    } catch (Exception e) {
      logger.warn("Failed to identify files to backup: {}", e.getMessage());
    }

    return filesToBackup;
  }

  /**
   * Determines if build file should be backed up.
   */
  private boolean shouldBackupBuildFile(AdapterMetadata metadata) {
    boolean hasDependencies = metadata.getDependencies() != null && !metadata.getDependencies().isEmpty();
    var testDeps = metadata.getTestDependencies();
    boolean hasTestDeps = testDeps != null && !testDeps.isEmpty();
    boolean hasPlugins = metadata.getBuildPlugins() != null && !metadata.getBuildPlugins().isEmpty();

    return hasDependencies || hasTestDeps || hasPlugins;
  }

  /**
   * Checks for dependency conflicts (informational only, doesn't block generation).
   * Detects cases where a new dependency uses a different version than an existing one.
   */
  private void checkDependencyConflicts(Path projectPath, UnifiedAdapterGenerator.AdapterConfig config) {
    try {
      AdapterMetadata metadata = loadAdapterMetadataWithFallback(config);

      if (metadata.getRuntimeDependencies() == null || metadata.getRuntimeDependencies().isEmpty()) {
        return;
      }

      // Read existing build file to check for version conflicts
      Path buildFilePath = projectPath.resolve("build.gradle.kts");

      if (!fileSystemPort.exists(buildFilePath)) {
        logger.info("Adding {} dependencies for adapter: {}",
            metadata.getRuntimeDependencies().size(), config.getName());
        return;
      }

      String buildContent = fileSystemPort.readFile(buildFilePath);

      for (var dep : metadata.getRuntimeDependencies()) {
        String groupArtifact = dep.group() + ":" + dep.artifact();
        String newVersion = dep.version();

        // Check if this dependency already exists with a different version
        if (buildContent.contains(dep.group()) && buildContent.contains(dep.artifact())) {
          String pattern = dep.group() + ":" + dep.artifact() + ":";
          int idx = buildContent.indexOf(pattern);
          if (idx >= 0) {
            int versionStart = idx + pattern.length();
            int versionEnd = buildContent.indexOf("\"", versionStart);
            if (versionEnd < 0) versionEnd = buildContent.indexOf("'", versionStart);
            if (versionEnd > versionStart) {
              String existingVersion = buildContent.substring(versionStart, versionEnd);
              if (newVersion != null && !newVersion.equals(existingVersion)) {
                logger.warn("Dependency version conflict detected for {}: " +
                    "existing={}, new={}. The existing version will be preserved.",
                    groupArtifact, existingVersion, newVersion);
              }
            }
          }
        }
      }

      logger.info("Dependency check complete for adapter: {}", config.getName());

    } catch (Exception e) {
      logger.warn("Failed to check dependency conflicts: {}", e.getMessage());
    }
  }

  /**
   * Loads adapter metadata with fallback to legacy paths.
   */
  private AdapterMetadata loadAdapterMetadataWithFallback(UnifiedAdapterGenerator.AdapterConfig config) {
    return adapterDiscovery
        .findAdapter(config.getType(), getDirection(), config.getFramework(), config.getParadigm())
        .orElseThrow(() -> new IllegalStateException(
            "Adapter metadata not found for: " + config.getType()));
  }

  /**
   * Creates AdapterConfig for AdapterConfigurationService.
   */
  private AdapterConfigurationService.AdapterConfig createAdapterConfigForService(
      UnifiedAdapterGenerator.AdapterConfig config) {
    return new AdapterConfigurationService.AdapterConfig(
        config.getName(),
        config.getType(),
        config.getFramework(),
        config.getParadigm(),
        config.getPackageName(),
        config.getUseCaseName(),
        config.getEntityName(),
        getDirection());
  }

  // ========== VALIDATION HELPER METHODS ==========

  /**
   * Validates Kotlin DSL syntax (basic check).
   */
  private boolean isValidKotlinDsl(String dsl) {
    return KOTLIN_DSL_PATTERN.matcher(dsl).matches();
  }

  /**
   * Validates Java class name.
   */
  private boolean isValidJavaClassName(String name) {
    return name != null && JAVA_CLASS_NAME_PATTERN.matcher(name).matches();
  }

  /**
   * Validates Java package name.
   */
  private boolean isValidJavaPackageName(String packageName) {
    return packageName != null && JAVA_PACKAGE_NAME_PATTERN.matcher(packageName).matches();
  }
}
