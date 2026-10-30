package com.pragma.archetype.application.generator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pragma.archetype.application.port.out.AdapterDiscoveryPort;
import com.pragma.archetype.application.usecase.AbstractGenerateAdapterUseCase;
import com.pragma.archetype.domain.model.adapter.AdapterDirection;
import com.pragma.archetype.domain.model.adapter.AdapterMetadata;
import com.pragma.archetype.domain.model.adapter.AdapterMethod;
import com.pragma.archetype.domain.model.adapter.Endpoint;
import com.pragma.archetype.domain.model.adapter.EndpointParameter;
import com.pragma.archetype.domain.model.adapter.MethodParameter;
import com.pragma.archetype.domain.model.adapter.TemplateFile;
import com.pragma.archetype.domain.model.config.ProjectConfig;
import com.pragma.archetype.domain.model.file.FileType;
import com.pragma.archetype.domain.model.file.GeneratedFile;
import com.pragma.archetype.domain.model.structure.ModuleMetadata;
import com.pragma.archetype.domain.model.structure.StructureMetadata;
import com.pragma.archetype.domain.model.template.TemplateRepositoryPaths;
import com.pragma.archetype.domain.port.out.PathResolver;
import com.pragma.archetype.domain.port.out.TemplateRepository;

import lombok.RequiredArgsConstructor;

/**
 * Unified generator for both input and output adapters.
 * Eliminates code duplication by handling both adapter types with a single
 * implementation.
 * 
 * <p>
 * The only difference between input and output adapters is the direction (INPUT
 * vs OUTPUT),
 * which determines the path where files are generated (entry-points vs
 * driven-adapters).
 * Everything else is identical: validation, backup, generation, configuration,
 * etc.
 * 
 * <p>
 * This class uses the Template Method pattern through composition with
 * {@code AbstractGenerateAdapterUseCase}, which provides the complete
 * generation flow.
 * 
 * @see AdapterDirection
 * @see AbstractGenerateAdapterUseCase
 */
@RequiredArgsConstructor
public class UnifiedAdapterGenerator {

  /**
   * Unified adapter configuration interface.
   * Abstracts the differences between InputAdapterConfig and OutputAdapterConfig.
   */
  public interface AdapterConfig {
    String getName();

    String getType();

    String getPackageName();

    String getFramework();

    String getParadigm();

    // Input-specific (return null for output adapters)
    String getUseCaseName();

    List<Endpoint> getEndpoints();

    // Output-specific (return null for input adapters)
    String getEntityName();

    List<AdapterMethod> getMethods();
  }

  private static final String VARIABLE_FRAMEWORK = "framework";
  private static final String VARIABLE_PARADIGM = "paradigm";
  private static final String DEFAULT_SUFFIX = "Adapter";
  private static final String BASE_PACKAGE_KEY = "basePackage";
  private static final String PATH_SEPARATOR = "/";

  private final TemplateRepository templateRepository;
  private final PathResolver pathResolver;
  private final AdapterDiscoveryPort adapterDiscovery;

  private final ProjectGenerator projectGenerator;

  /**
   * Generates adapter files based on configuration and direction.
   * 
   * @param projectPath       project root path
   * @param config            adapter configuration (can be InputAdapterConfig or
   *                          OutputAdapterConfig)
   * @param direction         adapter direction (INPUT or OUTPUT)
   * @param structureMetadata architecture structure metadata
   * @param projectConfig     project configuration
   * @return list of generated files
   */
  public List<GeneratedFile> generate(
      Path projectPath,
      AdapterConfig config,
      AdapterDirection direction,
      StructureMetadata structureMetadata,
      ProjectConfig projectConfig) {

    // Validate adapter exists
    validateAdapterExists(config, direction);

    // Check if we should create adapter as a module
    if (projectConfig != null && projectConfig.adaptersAsModules()) {
      return generateAsModule(projectPath, config, direction, structureMetadata, projectConfig);
    } else {
      return generateInPlace(projectPath, config, direction, structureMetadata, projectConfig);
    }
  }

  /**
   * Generates adapter as a new Gradle module (for granular architectures).
   */
  private List<GeneratedFile> generateAsModule(
      Path projectPath,
      AdapterConfig config,
      AdapterDirection direction,
      StructureMetadata structureMetadata,
      ProjectConfig projectConfig) {

    var generatedFiles = new ArrayList<GeneratedFile>();

    // Determine module path based on direction using structure metadata
    String moduleName = config.getName().toLowerCase().replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase();
    String adapterType = direction == AdapterDirection.INPUT ? "driving" : "driven";
    String moduleBasePath = structureMetadata.getAdapterBaseDirectory(adapterType);
    if (moduleBasePath == null) {
      // Fallback to default paths if not configured in structure.yml
      moduleBasePath = direction == AdapterDirection.INPUT
          ? "infrastructure/entry-points"
          : "infrastructure/driven-adapters";
    }
    String modulePath = moduleBasePath + "/" + moduleName;

    // 1. Create module build.gradle.kts
    GeneratedFile buildFile = generateModuleBuildFile(projectPath, config, projectConfig, modulePath, direction);
    generatedFiles.add(buildFile);

    // 2. Generate main adapter file in the module
    GeneratedFile mainFile = generateMainFileInModule(projectPath, config, direction, modulePath, projectConfig,
        structureMetadata);
    generatedFiles.add(mainFile);

    // Output port the driven adapter implements, when the domain doesn't have it yet
    generateGatewayPortIfMissing(projectPath, config, direction,
        prepareTemplateData(config, direction, projectConfig), structureMetadata)
        .ifPresent(generatedFiles::add);

    // 3. Generate entity mapper if needed (for data store adapters)
    if (needsEntityMapper(config, direction)) {
      GeneratedFile mapperFile = generateMapperInModule(projectPath, config, modulePath, projectConfig,
          structureMetadata);
      generatedFiles.add(mapperFile);

      GeneratedFile entityFile = generateDataEntityInModule(projectPath, config, direction, modulePath, projectConfig,
          structureMetadata);
      generatedFiles.add(entityFile);
    }

    // 4. Generate additional resources (GraphQL schemas, gRPC protos, etc.) - only
    // for INPUT
    if (direction == AdapterDirection.INPUT) {
      String resourcesPath = structureMetadata.getResourcesPath();
      List<GeneratedFile> additionalFiles = generateAdditionalResources(
          projectPath, config, direction, resourcesPath, projectConfig);
      generatedFiles.addAll(additionalFiles);
    }

    // 5. Update settings.gradle.kts to include the new module
    String modulePathForSettings = modulePath.replace('/', ':');
    projectGenerator.addModuleToSettings(projectPath, modulePathForSettings);

    // 6. Add dependency from app-service to this adapter module (use leaf name for flat includes)
    String[] segments = modulePathForSettings.split(":");
    String leafModuleName = segments[segments.length - 1];
    projectGenerator.addDependencyToModule(projectPath, "application/app-service", ":" + leafModuleName);

    return generatedFiles;
  }

  /**
   * Generates the output port {@code I{Entity}Gateway} in the domain ports
   * package when a driven adapter is created and the port does not exist yet.
   * An existing port is never overwritten, so operations the developer added
   * are kept. Templates without domain/Gateway.java.ftl skip this step.
   */
  private java.util.Optional<GeneratedFile> generateGatewayPortIfMissing(
      Path projectPath,
      AdapterConfig config,
      AdapterDirection direction,
      Map<String, Object> data,
      StructureMetadata structureMetadata) {

    String entityName = config.getEntityName();
    if (direction != AdapterDirection.OUTPUT || entityName == null || entityName.isBlank()) {
      return java.util.Optional.empty();
    }

    String templatePath = String.format("frameworks/%s/%s/domain/Gateway.java.ftl",
        config.getFramework(), config.getParadigm());
    if (!templateRepository.templateExists(templatePath)) {
      return java.util.Optional.empty();
    }

    String portsPackage = (String) data.get("portsPackage");
    if (portsPackage == null) {
      return java.util.Optional.empty();
    }
    Path gatewayPath = projectPath
        .resolve(resolveUseCaseModulePath(structureMetadata))
        .resolve(structureMetadata.getSourceDirectory())
        .resolve(portsPackage.replace('.', '/'))
        .resolve("I" + entityName + "Gateway.java")
        .normalize();
    if (Files.exists(gatewayPath)) {
      return java.util.Optional.empty();
    }

    return java.util.Optional.of(
        GeneratedFile.javaSource(gatewayPath, templateRepository.processTemplate(templatePath, data)));
  }

  /**
   * Module that holds the domain ports (same module as the use cases), or the
   * project root for single-module architectures.
   */
  private String resolveUseCaseModulePath(StructureMetadata structureMetadata) {
    if (!structureMetadata.generatesMultiModuleProject()) {
      return ".";
    }
    try {
      ModuleMetadata useCaseModule = structureMetadata.getUseCaseModule();
      if (useCaseModule != null && useCaseModule.getPath() != null) {
        return useCaseModule.getPath();
      }
    } catch (IllegalStateException e) {
      // useCaseModule not configured, fall back to the project root
    }
    return ".";
  }

  /**
   * Generates adapter files in existing structure (traditional approach).
   */
  private List<GeneratedFile> generateInPlace(
      Path projectPath,
      AdapterConfig config,
      AdapterDirection direction,
      StructureMetadata structureMetadata,
      ProjectConfig projectConfig) {

    var generatedFiles = new ArrayList<GeneratedFile>();

    // Prepare template data
    Map<String, Object> data = prepareTemplateData(config, direction, projectConfig);

    // Generate all adapter files (main + secondary templates)
    List<GeneratedFile> adapterFiles = generateAllTemplateFiles(
        projectPath, config, direction, data, projectConfig, structureMetadata);
    generatedFiles.addAll(adapterFiles);

    // Output port the driven adapter implements, when the domain doesn't have it yet
    generateGatewayPortIfMissing(projectPath, config, direction, data, structureMetadata)
        .ifPresent(generatedFiles::add);

    // Generate entity mapper if needed
    if (needsEntityMapper(config, direction)) {
      GeneratedFile mapperFile = generateMapper(projectPath, config, direction, data, projectConfig, structureMetadata);
      generatedFiles.add(mapperFile);

      GeneratedFile entityFile = generateDataEntity(projectPath, config, direction, data, projectConfig,
          structureMetadata);
      generatedFiles.add(entityFile);
    }

    // Generate additional resources (only for INPUT adapters)
    if (direction == AdapterDirection.INPUT) {
      String resourcesPath = structureMetadata.getResourcesPath();
      List<GeneratedFile> additionalFiles = generateAdditionalResources(
          projectPath, config, direction, resourcesPath, projectConfig);
      generatedFiles.addAll(additionalFiles);
    }

    return generatedFiles;
  }

  /**
   * Generates files for all discovered templates (main + secondary).
   * This supports multi-template adapters like reactive REST (Router + Handler).
   */
  private List<GeneratedFile> generateAllTemplateFiles(
      Path projectPath,
      AdapterConfig config,
      AdapterDirection direction,
      Map<String, Object> data,
      ProjectConfig projectConfig,
      StructureMetadata structureMetadata) {

    var files = new ArrayList<GeneratedFile>();

    // Get metadata
    AdapterMetadata metadata = getAdapterMetadata(config, direction);

    // Get the adapter suffix from metadata
    String suffix = getAdapterSuffix(metadata);

    // Iterate over all discovered templates (main + secondary)
    for (TemplateFile templateFile : metadata.getDiscoveredTemplates()) {
      // Skip non-main/non-secondary templates (e.g., test, config, entity)
      String templateType = templateFile.getType();
      if (!"main".equals(templateType) && !"secondary".equals(templateType)) {
        continue;
      }

      // Build template path
      String adapterDir = resolveAdapterDirectory(direction);
      String templatePath = String.format(TemplateRepositoryPaths.Patterns.ADAPTER_TEMPLATE_DIR + "/%s",
          config.getFramework(), config.getParadigm(), adapterDir, config.getType(), templateFile.getName());

      // Process template
      String content = templateRepository.processTemplate(templatePath, data);

      // Determine file name based on template destination or convention
      String fileName;
      if (templateFile.getDestination() != null && !templateFile.getDestination().isBlank()) {
        String dest = templateFile.getDestination();
        // If destination contains {name} placeholder, replace it
        if (dest.contains("{name}")) {
          fileName = dest.replace("{name}", config.getName());
        } else {
          // Prefix ALL templates (main and secondary) with the adapter name
          String baseName = dest.replace(".java", "");
          fileName = config.getName() + baseName + ".java";
        }
      } else if ("main".equals(templateType)) {
        fileName = config.getName() + suffix + ".java";
      } else {
        // For secondary templates without destination, prefix with adapter name
        String baseName = templateFile.getName().replace(".ftl", "").replace(".java", "");
        fileName = config.getName() + baseName + ".java";
      }

      // Resolve file path
      Path filePath = resolveAdapterFilePath(
          projectPath, config, direction, projectConfig, fileName, structureMetadata);

      files.add(GeneratedFile.javaSource(filePath, content));
    }

    // Fallback: if no templates were processed (backward compatibility), generate main file only
    if (files.isEmpty()) {
      GeneratedFile mainFile = generateMainFile(projectPath, config, direction, data, projectConfig, structureMetadata);
      files.add(mainFile);
    }

    return files;
  }

  /**
   * Generates the main adapter file (Controller, Repository, etc.).
   */
  private GeneratedFile generateMainFile(
      Path projectPath,
      AdapterConfig config,
      AdapterDirection direction,
      Map<String, Object> data,
      ProjectConfig projectConfig,
      StructureMetadata structureMetadata) {

    // Get metadata
    AdapterMetadata metadata = getAdapterMetadata(config, direction);

    // Get template path
    String templatePath = getMainTemplatePath(config, direction, metadata);

    // Process template
    String content = templateRepository.processTemplate(templatePath, data);

    // Get suffix from metadata
    String suffix = getAdapterSuffix(metadata);

    // Resolve file path
    Path filePath = resolveAdapterFilePath(
        projectPath, config, direction, projectConfig, config.getName() + suffix + ".java", structureMetadata);

    return GeneratedFile.javaSource(filePath, content);
  }

  /**
   * Generates mapper file for entity mapping.
   */
  private GeneratedFile generateMapper(
      Path projectPath,
      AdapterConfig config,
      AdapterDirection direction,
      Map<String, Object> data,
      ProjectConfig projectConfig,
      StructureMetadata structureMetadata) {

    // Get mapper template
    String templatePath = getMapperTemplatePath(config, direction);

    // Add mapper-specific package to data
    var mapperData = new HashMap<>(data);
    String adapterPackage = (String) data.get("packageName");
    mapperData.put("packageName", adapterPackage + ".mapper");
    mapperData.put("entityPackage", adapterPackage + ".entity");

    // Process template
    String content = templateRepository.processTemplate(templatePath, mapperData);

    // Resolve file path with mapper subdirectory
    String fileName = config.getEntityName() + "EntityMapper.java";
    Path filePath = resolveAdapterFilePath(projectPath, config, direction, projectConfig, "mapper/" + fileName,
        structureMetadata);

    return GeneratedFile.javaSource(filePath, content);
  }

  /**
   * Generates data entity file.
   */
  private GeneratedFile generateDataEntity(
      Path projectPath,
      AdapterConfig config,
      AdapterDirection direction,
      Map<String, Object> data,
      ProjectConfig projectConfig,
      StructureMetadata structureMetadata) {

    // Get metadata
    AdapterMetadata metadata = getAdapterMetadata(config, direction);

    // Get entity template
    String templatePath = getDataEntityTemplatePath(config, direction, metadata);

    // Add entity-specific package to data
    var entityData = new HashMap<>(data);
    String adapterPackage = (String) data.get("packageName");
    entityData.put("packageName", adapterPackage + ".entity");

    // Process template
    String content = templateRepository.processTemplate(templatePath, entityData);

    // Resolve file path with entity subdirectory
    String fileName = config.getEntityName() + "Entity.java";
    Path filePath = resolveAdapterFilePath(projectPath, config, direction, projectConfig, "entity/" + fileName,
        structureMetadata);

    return GeneratedFile.javaSource(filePath, content);
  }

  // ========== MODULE GENERATION METHODS ==========

  /**
   * Generates additional resource files (GraphQL schemas, gRPC protos, etc.).
   * Only applicable for INPUT adapters.
   */
  private List<GeneratedFile> generateAdditionalResources(
      Path projectPath,
      AdapterConfig config,
      AdapterDirection direction,
      String resourcesPath,
      ProjectConfig projectConfig) {

    var files = new ArrayList<GeneratedFile>();

    try {
      // Get adapter metadata
      AdapterMetadata metadata = getAdapterMetadata(config, direction);

      if (!metadata.hasAdditionalResources()) {
        return files;
      }

      // Prepare template data
      Map<String, Object> data = prepareTemplateData(config, direction, projectConfig);

      // Build base path for templates
      String adapterDir = resolveAdapterDirectory(direction);
      String basePath = String.format("frameworks/%s/%s/adapters/%s/%s",
          config.getFramework(), config.getParadigm(), adapterDir, config.getType());

      // Process each additional resource
      for (var resource : metadata.getAdditionalResources()) {
        try {
          // Build template path
          String templatePath = basePath + "/" + resource.templatePath();

          // Resolve destination path using resourcesPath from architecture
          String destination = resource.resolveDestination(config.getName(), resourcesPath);
          Path destinationPath = projectPath.resolve(destination);

          // Process resource
          String content;
          if (resource.shouldProcessAsTemplate()) {
            content = templateRepository.processTemplate(templatePath, data);
          } else {
            content = templateRepository.getTemplateContent(templatePath);
          }

          files.add(GeneratedFile.create(destinationPath, content, false));

        } catch (Exception e) {
          System.err.println("Warning: Failed to generate additional resource: "
              + resource.name() + " - " + e.getMessage());
        }
      }

    } catch (Exception e) {
      System.err.println("Warning: Failed to load additional resources: " + e.getMessage());
    }

    return files;
  }

  private GeneratedFile generateModuleBuildFile(
      Path projectPath,
      AdapterConfig config,
      ProjectConfig projectConfig,
      String modulePath,
      AdapterDirection direction) {

    var data = new HashMap<String, Object>();
    data.put("adapterType", config.getType().toLowerCase());
    data.put(BASE_PACKAGE_KEY, projectConfig.basePackage());
    data.put("direction", direction.name().toLowerCase());

    // Select template based on architecture
    String templatePath = "architectures/hexagonal-multi-granular/modules/adapter-build.gradle.kts.ftl";
    String content = templateRepository.processTemplate(templatePath, data);

    Path filePath = projectPath.resolve(modulePath).resolve("build.gradle.kts");
    return new GeneratedFile(filePath, content, FileType.GRADLE_BUILD, false);
  }

  private GeneratedFile generateMainFileInModule(
      Path projectPath,
      AdapterConfig config,
      AdapterDirection direction,
      String modulePath,
      ProjectConfig projectConfig,
      StructureMetadata structureMetadata) {

    Map<String, Object> data = prepareTemplateData(config, direction, projectConfig);
    AdapterMetadata metadata = getAdapterMetadata(config, direction);
    String templatePath = getMainTemplatePath(config, direction, metadata);
    String content = templateRepository.processTemplate(templatePath, data);

    String suffix = getAdapterSuffix(metadata);
    String packagePath = config.getPackageName().replace('.', '/');
    Path filePath = projectPath
        .resolve(modulePath)
        .resolve(structureMetadata.getSourceDirectory())
        .resolve(packagePath)
        .resolve(config.getName() + suffix + ".java");

    return GeneratedFile.javaSource(filePath, content);
  }

  private GeneratedFile generateMapperInModule(
      Path projectPath,
      AdapterConfig config,
      String modulePath,
      ProjectConfig projectConfig,
      StructureMetadata structureMetadata) {

    Map<String, Object> data = prepareTemplateData(config, AdapterDirection.OUTPUT, projectConfig);
    
    // Add mapper-specific packages
    String adapterPackage = config.getPackageName();
    data.put("packageName", adapterPackage + ".mapper");
    data.put("entityPackage", adapterPackage + ".entity");
    
    String templatePath = getMapperTemplatePath(config, AdapterDirection.OUTPUT);
    String content = templateRepository.processTemplate(templatePath, data);

    String mapperPackage = adapterPackage + ".mapper";
    String packagePath = mapperPackage.replace('.', '/');
    Path filePath = projectPath
        .resolve(modulePath)
        .resolve(structureMetadata.getSourceDirectory())
        .resolve(packagePath)
        .resolve(config.getEntityName() + "EntityMapper.java");

    return GeneratedFile.javaSource(filePath, content);
  }

  // ========== HELPER METHODS ==========

  private GeneratedFile generateDataEntityInModule(
      Path projectPath,
      AdapterConfig config,
      AdapterDirection direction,
      String modulePath,
      ProjectConfig projectConfig,
      StructureMetadata structureMetadata) {

    Map<String, Object> data = prepareTemplateData(config, direction, projectConfig);
    
    // Add entity-specific package
    String adapterPackage = config.getPackageName();
    data.put("packageName", adapterPackage + ".entity");
    
    AdapterMetadata metadata = getAdapterMetadata(config, direction);
    String templatePath = getDataEntityTemplatePath(config, direction, metadata);
    String content = templateRepository.processTemplate(templatePath, data);

    String entityPackage = adapterPackage + ".entity";
    String packagePath = entityPackage.replace('.', '/');
    Path filePath = projectPath
        .resolve(modulePath)
        .resolve(structureMetadata.getSourceDirectory())
        .resolve(packagePath)
        .resolve(config.getEntityName() + "Entity.java");

    return GeneratedFile.javaSource(filePath, content);
  }

  /**
   * Validates that the adapter type exists for the given direction.
   */
  private void validateAdapterExists(AdapterConfig config, AdapterDirection direction) {
    if (!adapterDiscovery.adapterExists(config.getType(), direction, config.getFramework(), config.getParadigm())) {
      List<String> availableAdapters = adapterDiscovery.listAdapterNames(
          direction, config.getFramework(), config.getParadigm());
      throw new IllegalArgumentException(
          String.format("Unknown %s adapter type: %s for %s/%s. Available adapters: %s",
              direction.name().toLowerCase(),
              config.getType(),
              config.getFramework(),
              config.getParadigm(),
              String.join(", ", availableAdapters)));
    }
  }

  /**
   * Gets adapter metadata from discovery service.
   */
  private AdapterMetadata getAdapterMetadata(AdapterConfig config, AdapterDirection direction) {
    return adapterDiscovery
        .findAdapter(config.getType(), direction, config.getFramework(), config.getParadigm())
        .orElseThrow(() -> {
          List<String> available = adapterDiscovery.listAdapterNames(
              direction, config.getFramework(), config.getParadigm());
          return new IllegalArgumentException(
              String.format("Unknown %s adapter type: %s for %s/%s. Available adapters: %s",
                  direction.name().toLowerCase(),
                  config.getType(),
                  config.getFramework(),
                  config.getParadigm(),
                  String.join(", ", available)));
        });
  }

  /**
   * Resolves the adapter directory based on direction for template paths.
   * This returns the directory name in the template repository structure,
   * NOT the directory in the generated project.
   */
  private String resolveAdapterDirectory(AdapterDirection direction) {
    return TemplateRepositoryPaths.AdapterDirectories.getDirectoryName(
        direction == AdapterDirection.INPUT);
  }

  /**
   * Gets the main template path for the adapter.
   */
  private String getMainTemplatePath(AdapterConfig config, AdapterDirection direction, AdapterMetadata metadata) {
    var mainTemplate = metadata.getDiscoveredTemplates().stream()
        .filter(t -> "main".equals(t.getType()))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(
            "No main template found for adapter: " + config.getType()));

    String adapterDir = resolveAdapterDirectory(direction);
    return String.format(TemplateRepositoryPaths.Patterns.ADAPTER_TEMPLATE_DIR + "/%s",
        config.getFramework(), config.getParadigm(), adapterDir, config.getType(), mainTemplate.getName());
  }

  /**
   * Gets the mapper template path.
   * First tries the adapter-specific Mapper.java.ftl, then falls back to the generic one.
   */
  private String getMapperTemplatePath(AdapterConfig config, AdapterDirection direction) {
    String adapterDir = resolveAdapterDirectory(direction);

    // Try adapter-specific Mapper.java.ftl first
    String adapterSpecificPath = String.format(TemplateRepositoryPaths.Patterns.ADAPTER_TEMPLATE_DIR + "/%s",
        config.getFramework(), config.getParadigm(), adapterDir, config.getType(), "Mapper.java.ftl");

    if (templateRepository.templateExists(adapterSpecificPath)) {
      return adapterSpecificPath;
    }

    // Fall back to generic Mapper.java.ftl
    return String.format(TemplateRepositoryPaths.Patterns.GENERIC_ADAPTER_TEMPLATE,
        config.getFramework(), config.getParadigm(), adapterDir, "Mapper.java.ftl");
  }

  /**
   * Gets the data entity template path.
   * First tries the adapter-specific Entity.java.ftl, then falls back to the generic one.
   */
  private String getDataEntityTemplatePath(AdapterConfig config, AdapterDirection direction, AdapterMetadata metadata) {
    String adapterDir = resolveAdapterDirectory(direction);

    // Try adapter-specific Entity.java.ftl first
    String adapterSpecificPath = String.format(TemplateRepositoryPaths.Patterns.ADAPTER_TEMPLATE_DIR + "/%s",
        config.getFramework(), config.getParadigm(), adapterDir, config.getType(), "Entity.java.ftl");

    if (templateRepository.templateExists(adapterSpecificPath)) {
      return adapterSpecificPath;
    }

    // Fall back to generic Entity.java.ftl
    String genericPath = String.format(TemplateRepositoryPaths.Patterns.GENERIC_ADAPTER_TEMPLATE,
        config.getFramework(), config.getParadigm(), adapterDir, "Entity.java.ftl");

    if (templateRepository.templateExists(genericPath)) {
      return genericPath;
    }

    throw new IllegalStateException(
        "No entity template found for adapter: " + config.getType());
  }

  /**
   * Gets the adapter suffix from metadata.
   */
  private String getAdapterSuffix(AdapterMetadata metadata) {
    if (metadata.getSuffix() != null && !metadata.getSuffix().isBlank()) {
      return metadata.getSuffix();
    }
    return DEFAULT_SUFFIX;
  }

  /**
   * Checks if adapter needs entity mapper.
   */
  private boolean needsEntityMapper(AdapterConfig config, AdapterDirection direction) {
    return adapterDiscovery
        .findAdapter(config.getType(), direction, config.getFramework(), config.getParadigm())
        .map(AdapterMetadata::isNeedsMapper)
        .orElse(false);
  }

  /**
   * Resolves the full file path for an adapter file.
   * For multi-module architectures, uses the module path from structure.yml
   * to prefix the filesystem path (e.g., "infrastructure/src/main/java/...").
   */
  private Path resolveAdapterFilePath(
      Path projectPath,
      AdapterConfig config,
      AdapterDirection direction,
      ProjectConfig projectConfig,
      String fileName,
      StructureMetadata structureMetadata) {

    String sourceDir = structureMetadata.getSourceDirectory();

    if (projectConfig != null) {
      // Use PathResolver for dynamic path resolution
      var context = new HashMap<String, String>();
      context.put(BASE_PACKAGE_KEY, projectConfig.basePackage());

      String adapterType = direction == AdapterDirection.INPUT ? "driving" : "driven";
      // Directory name = user-provided name + technology type
      // e.g., --name=Account --type=postgresql → "account-postgresql"
      String adapterDirName = config.getName().toLowerCase() + "-" + config.getType().toLowerCase();
      Path adapterBasePath = pathResolver.resolveAdapterPath(
          projectConfig.architecture(),
          adapterType,
          adapterDirName,
          context);

      // The adapter path from structure.yml (e.g., "infrastructure/driven-adapters/account-postgresql")
      String adapterPathStr = adapterBasePath.toString();

      // For multi-module architectures, check if the adapter path starts with a module path
      // If so, use the module path as a filesystem prefix and strip it from the package path
      String modulePath = ".";
      String packageRelativePath = adapterPathStr;

      com.pragma.archetype.domain.model.structure.ModuleMetadata owningModule = 
          structureMetadata.findModuleForPath(adapterPathStr);
      if (owningModule != null) {
        modulePath = owningModule.getPath();
        packageRelativePath = structureMetadata.stripModulePrefix(adapterPathStr, modulePath);
      }

      // Convert adapter path to Java-compatible directory (remove hyphens)
      String javaCompatiblePath = structureMetadata.pathToPackageName(packageRelativePath)
          .replace('.', PATH_SEPARATOR.charAt(0));

      String basePackagePath = projectConfig.basePackage().replace('.', PATH_SEPARATOR.charAt(0));
      return projectPath
          .resolve(modulePath)
          .resolve(sourceDir)
          .resolve(basePackagePath)
          .resolve(javaCompatiblePath)
          .resolve(fileName);
    } else {
      // Fallback to package-based path
      String packagePath = config.getPackageName().replace('.', PATH_SEPARATOR.charAt(0));
      return projectPath
          .resolve(sourceDir)
          .resolve(packagePath)
          .resolve(fileName);
    }
  }

  /**
   * Prepares template data with complete context.
   */
  private Map<String, Object> prepareTemplateData(
      AdapterConfig config,
      AdapterDirection direction,
      ProjectConfig projectConfig) {

    var data = new HashMap<String, Object>();

    // Common variables
    data.put("adapterName", config.getName());
    data.put("packageName", config.getPackageName());
    data.put("adapterType", config.getType().toLowerCase());
    data.put(VARIABLE_FRAMEWORK, config.getFramework());
    data.put(VARIABLE_PARADIGM, config.getParadigm());

    // Direction-specific variables
    if (direction == AdapterDirection.INPUT) {
      data.put("controllerName", config.getName()); // Alias for REST templates
      data.put("useCaseName", config.getUseCaseName());
      data.put("endpoints", convertEndpointsToMaps(config.getEndpoints()));
    } else {
      data.put("entityName", config.getEntityName());
      data.put("methods", convertMethodsToMaps(config.getMethods()));
    }

    // Project-level variables
    if (projectConfig != null) {
      data.put(BASE_PACKAGE_KEY, projectConfig.basePackage());
      data.put("projectName", projectConfig.name());
      if (projectConfig.architecture() != null) {
        data.put("architecture", projectConfig.architecture().name().toLowerCase());
      }

      // Package path variables for templates that reference config and domain packages
      data.put("configPackagePath",
          projectConfig.basePackage().replace('.', '/') + "/infrastructure/config");
      
      // Use structure.yml module metadata for package resolution if available
      try {
        StructureMetadata sm = templateRepository.loadStructureMetadata(projectConfig.architecture());
        ModuleMetadata entityMod = sm.getEntityModule();
        ModuleMetadata useCaseMod = sm.getUseCaseModule();
        if (entityMod != null) {
          String modelPath = entityMod.getPackage("model");
          String modelSuffix = modelPath.replace("/", ".").replace("-", "");
          data.put("modelPackage", modelSuffix.isBlank() ? projectConfig.basePackage() : projectConfig.basePackage() + "." + modelSuffix);
        } else {
          data.put("modelPackage", projectConfig.basePackage() + ".domain.model");
        }
        if (useCaseMod != null) {
          String usecasesPath = useCaseMod.getPackage("usecases");
          String portsPath = useCaseMod.getPackage("ports");
          String ucSuffix = usecasesPath.replace("/", ".").replace("-", "");
          String pSuffix = portsPath.replace("/", ".").replace("-", "");
          data.put("useCasesPackage", ucSuffix.isBlank() ? projectConfig.basePackage() : projectConfig.basePackage() + "." + ucSuffix);
          data.put("portsPackage", pSuffix.isBlank() ? projectConfig.basePackage() : projectConfig.basePackage() + "." + pSuffix);
        } else {
          data.put("useCasesPackage", projectConfig.basePackage() + ".domain.usecases");
          data.put("portsPackage", projectConfig.basePackage() + ".domain.ports");
        }
      } catch (Exception e) {
        // Fallback to hardcoded defaults (single-module style)
        System.err.println("WARNING: Failed to resolve packages from structure.yml: " + e.getMessage());
        data.put("modelPackage", projectConfig.basePackage() + ".domain.model");
        data.put("useCasesPackage", projectConfig.basePackage() + ".domain.usecases");
        data.put("portsPackage", projectConfig.basePackage() + ".domain.ports");
      }
    }

    return data;
  }

  /**
   * Converts endpoints to Maps for FreeMarker.
   */
  private List<Map<String, Object>> convertEndpointsToMaps(List<Endpoint> endpoints) {
    if (endpoints == null) {
      return new ArrayList<>();
    }

    var endpointMaps = new ArrayList<Map<String, Object>>();
    for (Endpoint endpoint : endpoints) {
      var endpointMap = new HashMap<String, Object>();
      endpointMap.put("path", endpoint.path());
      endpointMap.put("method", endpoint.method().name());
      endpointMap.put("useCaseMethod", endpoint.useCaseMethod());
      endpointMap.put("returnType", endpoint.returnType());

      var paramMaps = new ArrayList<Map<String, Object>>();
      if (endpoint.parameters() != null) {
        for (EndpointParameter param : endpoint.parameters()) {
          var paramMap = new HashMap<String, Object>();
          paramMap.put("name", param.name());
          paramMap.put("type", param.type());
          paramMap.put("paramType", param.paramType().name());
          paramMaps.add(paramMap);
        }
      }
      endpointMap.put("parameters", paramMaps);
      endpointMaps.add(endpointMap);
    }
    return endpointMaps;
  }

  /**
   * Converts methods to Maps for FreeMarker.
   */
  private List<Map<String, Object>> convertMethodsToMaps(List<AdapterMethod> methods) {
    if (methods == null) {
      return new ArrayList<>();
    }

    var methodMaps = new ArrayList<Map<String, Object>>();
    for (AdapterMethod method : methods) {
      var methodMap = new HashMap<String, Object>();
      methodMap.put("name", method.name());
      methodMap.put("returnType", method.returnType());

      var paramMaps = new ArrayList<Map<String, Object>>();
      if (method.parameters() != null) {
        for (MethodParameter param : method.parameters()) {
          var paramMap = new HashMap<String, Object>();
          paramMap.put("name", param.name());
          paramMap.put("type", param.type());
          paramMaps.add(paramMap);
        }
      }
      methodMap.put("parameters", paramMaps);
      methodMaps.add(methodMap);
    }
    return methodMaps;
  }
}
