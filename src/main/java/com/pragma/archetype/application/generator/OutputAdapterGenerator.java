package com.pragma.archetype.application.generator;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pragma.archetype.application.port.out.AdapterDiscoveryPort;
import com.pragma.archetype.domain.model.adapter.AdapterDirection;
import com.pragma.archetype.domain.model.adapter.AdapterMethod;
import com.pragma.archetype.domain.model.adapter.MethodParameter;
import com.pragma.archetype.domain.model.adapter.OutputAdapterConfig;
import com.pragma.archetype.domain.model.config.ProjectConfig;
import com.pragma.archetype.domain.model.file.FileType;
import com.pragma.archetype.domain.model.file.GeneratedFile;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import com.pragma.archetype.domain.port.out.PathResolver;
import com.pragma.archetype.domain.port.out.TemplateRepository;

/**
 * Generates output adapter files (Redis, MongoDB, etc.).
 * Application service that orchestrates file generation.
 * 
 * Uses dynamic adapter discovery instead of hardcoded enums.
 */
public class OutputAdapterGenerator {

  private static final String TEMPLATE_TYPE_OUTPUT_ADAPTER = "outputAdapter";
  private static final String TEMPLATE_TYPE_MAPPER = "mapper";
  private static final String TEMPLATE_TYPE_DATA_ENTITY = "dataEntity";
  private static final String VARIABLE_FRAMEWORK = "framework";
  private static final String VARIABLE_PARADIGM = "paradigm";

  private static final String VARIABLE_ADAPTER_DIR = "adapterDir";

  private static final String VARIABLE_TYPE = "type";

  private static final String VARIABLE_ARCHITECTURE = "architecture";

  private final TemplateRepository templateRepository;

  private final FileSystemPort fileSystemPort;

  private final PathResolver pathResolver;

  private final ProjectGenerator projectGenerator;

  private final AdapterDiscoveryPort adapterDiscovery;

  public OutputAdapterGenerator(
      TemplateRepository templateRepository,
      FileSystemPort fileSystemPort,
      PathResolver pathResolver,
      AdapterDiscoveryPort adapterDiscovery) {
    this.templateRepository = templateRepository;
    this.fileSystemPort = fileSystemPort;
    this.pathResolver = pathResolver;
    this.projectGenerator = new ProjectGenerator(templateRepository, fileSystemPort);
    this.adapterDiscovery = adapterDiscovery;
  }

  /**
   * Generates adapter files based on configuration.
   * If ProjectConfig has adaptersAsModules=true, creates a new Gradle module.
   */
  public List<GeneratedFile> generate(Path projectPath, OutputAdapterConfig config, ProjectConfig projectConfig) {
    List<GeneratedFile> generatedFiles = new ArrayList<>();

    // Get framework and paradigm from config
    String framework = config.framework();
    String paradigm = config.paradigm();

    // Validate adapter type exists (with framework/paradigm)
    String adapterType = config.type();
    if (!adapterDiscovery.adapterExists(adapterType, AdapterDirection.OUTPUT, framework, paradigm)) {
      List<String> availableAdapters = adapterDiscovery.listAdapterNames(AdapterDirection.OUTPUT, framework, paradigm);
      throw new IllegalArgumentException(
          "Unknown output adapter type: " + adapterType +
              " for " + framework + "/" + paradigm +
              ". Available adapters: " + String.join(", ", availableAdapters));
    }

    // Check if we should create adapter as a module
    if (projectConfig != null && projectConfig.adaptersAsModules()) {
      generatedFiles.addAll(generateAdapterAsModule(projectPath, config, projectConfig));
    } else {
      // Traditional approach: generate files in existing structure
      generatedFiles.addAll(generateAdapterInPlace(projectPath, config, projectConfig));
    }

    return generatedFiles;
  }

  /**
   * Generates adapter as a new Gradle module (for granular architectures).
   */
  private List<GeneratedFile> generateAdapterAsModule(Path projectPath, OutputAdapterConfig config,
      ProjectConfig projectConfig) {
    List<GeneratedFile> generatedFiles = new ArrayList<>();

    // Determine module path based on adapter type
    String moduleName = config.name().toLowerCase().replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase();
    String modulePath = "infrastructure/driven-adapters/" + moduleName;

    // 1. Create module build.gradle.kts
    GeneratedFile buildFile = generateModuleBuildFile(projectPath, config, projectConfig, modulePath);
    generatedFiles.add(buildFile);

    // 2. Generate adapter implementation in the module
    GeneratedFile adapterFile = generateAdapterInModule(projectPath, config, modulePath, projectConfig);
    generatedFiles.add(adapterFile);

    // 3. Generate entity mapper if needed (for data store adapters)
    String framework = config.framework();
    String paradigm = config.paradigm();
    if (needsEntityMapper(config.type(), framework, paradigm)) {
      GeneratedFile mapperFile = generateMapperInModule(projectPath, config, modulePath, projectConfig);
      generatedFiles.add(mapperFile);

      GeneratedFile entityFile = generateDataEntityInModule(projectPath, config, modulePath, projectConfig);
      generatedFiles.add(entityFile);
    }

    // 4. Update settings.gradle.kts to include the new module
    String modulePathForSettings = modulePath.replace('/', ':');
    projectGenerator.addModuleToSettings(projectPath, modulePathForSettings);

    // 5. Add dependency from app-service to this adapter module
    projectGenerator.addDependencyToModule(projectPath, "application/app-service", ":" + modulePathForSettings);

    return generatedFiles;
  }

  /**
   * Generates adapter files in existing structure (traditional approach).
   */
  private List<GeneratedFile> generateAdapterInPlace(Path projectPath, OutputAdapterConfig config,
      ProjectConfig projectConfig) {
    List<GeneratedFile> generatedFiles = new ArrayList<>();

    // Prepare template data with complete context
    Map<String, Object> data = prepareTemplateData(config, projectConfig);

    // Generate adapter implementation
    GeneratedFile adapterFile = generateAdapter(projectPath, config, data, projectConfig);
    generatedFiles.add(adapterFile);

    // Generate entity mapper if needed (for data store adapters)
    String framework = config.framework();
    String paradigm = config.paradigm();
    if (needsEntityMapper(config.type(), framework, paradigm)) {
      GeneratedFile mapperFile = generateMapper(projectPath, config, data, projectConfig);
      generatedFiles.add(mapperFile);

      GeneratedFile entityFile = generateDataEntity(projectPath, config, data, projectConfig);
      generatedFiles.add(entityFile);
    }

    return generatedFiles;
  }

  /**
   * Generates build.gradle.kts for the adapter module.
   */
  private GeneratedFile generateModuleBuildFile(Path projectPath, OutputAdapterConfig config,
      ProjectConfig projectConfig, String modulePath) {
    Map<String, Object> data = new HashMap<>();
    data.put("adapterType", config.type().toLowerCase());
    data.put("basePackage", projectConfig.basePackage());

    // Select template based on adapter type
    String templatePath = "architectures/hexagonal-multi-granular/modules/driven-adapter-build.gradle.kts.ftl";
    String content = templateRepository.processTemplate(templatePath, data);

    Path filePath = projectPath.resolve(modulePath).resolve("build.gradle.kts");
    return new GeneratedFile(filePath, content, FileType.GRADLE_BUILD, false);
  }

  /**
   * Generates adapter implementation in module.
   */
  private GeneratedFile generateAdapterInModule(Path projectPath, OutputAdapterConfig config, String modulePath,
      ProjectConfig projectConfig) {
    Map<String, Object> data = prepareTemplateData(config, projectConfig);
    String templatePath = getAdapterTemplate(config.type(), data);
    String content = templateRepository.processTemplate(templatePath, data);

    String packagePath = config.packageName().replace('.', '/');
    Path filePath = projectPath
        .resolve(modulePath)
        .resolve("src/main/java")
        .resolve(packagePath)
        .resolve(config.name() + "Adapter.java");

    return GeneratedFile.javaSource(filePath, content);
  }

  /**
   * Generates mapper in module.
   */
  private GeneratedFile generateMapperInModule(Path projectPath, OutputAdapterConfig config, String modulePath,
      ProjectConfig projectConfig) {
    Map<String, Object> data = prepareTemplateData(config, projectConfig);
    String content = templateRepository.processTemplate(getMapperTemplate(data), data);

    String mapperPackage = config.packageName() + ".mapper";
    String packagePath = mapperPackage.replace('.', '/');
    Path filePath = projectPath
        .resolve(modulePath)
        .resolve("src/main/java")
        .resolve(packagePath)
        .resolve(config.entityName() + "Mapper.java");

    return GeneratedFile.javaSource(filePath, content);
  }

  /**
   * Generates data entity in module.
   */
  private GeneratedFile generateDataEntityInModule(Path projectPath, OutputAdapterConfig config, String modulePath,
      ProjectConfig projectConfig) {
    Map<String, Object> data = prepareTemplateData(config, projectConfig);
    String templatePath = getDataEntityTemplate(config.type(), data);
    String content = templateRepository.processTemplate(templatePath, data);

    String entityPackage = config.packageName() + ".entity";
    String packagePath = entityPackage.replace('.', '/');
    Path filePath = projectPath
        .resolve(modulePath)
        .resolve("src/main/java")
        .resolve(packagePath)
        .resolve(config.entityName() + "Data.java");

    return GeneratedFile.javaSource(filePath, content);
  }

  private GeneratedFile generateAdapter(Path projectPath, OutputAdapterConfig config, Map<String, Object> data,
      ProjectConfig projectConfig) {
    // Select template based on adapter type
    String templatePath = getAdapterTemplate(config.type(), data);

    // Process template
    String content = templateRepository.processTemplate(templatePath, data);

    // Resolve adapter path using PathResolver if project config is available
    Path filePath;
    if (projectConfig != null) {
      // Prepare context for path resolution
      Map<String, String> context = new HashMap<>();
      context.put("basePackage", projectConfig.basePackage());

      // Determine adapter type string (driven or driving)
      String adapterType = "driven"; // Default for output adapters

      // Resolve the adapter base path (relative to basePackage)
      Path adapterBasePath = pathResolver.resolveAdapterPath(
          projectConfig.architecture(),
          adapterType,
          config.name().toLowerCase(),
          context);

      // Convert base package to path
      String basePackagePath = projectConfig.basePackage().replace('.', '/');

      // Build full file path:
      // src/main/java/{basePackage}/{resolvedPath}/{AdapterName}Adapter.java
      filePath = projectPath
          .resolve("src/main/java")
          .resolve(basePackagePath)
          .resolve(adapterBasePath)
          .resolve(config.name() + "Adapter.java");
    } else {
      // Fallback to hardcoded path if project config not available
      String packagePath = config.packageName().replace('.', '/');
      filePath = projectPath
          .resolve("src/main/java")
          .resolve(packagePath)
          .resolve(config.name() + "Adapter.java");
    }

    return GeneratedFile.javaSource(filePath, content);
  }

  private GeneratedFile generateMapper(Path projectPath, OutputAdapterConfig config, Map<String, Object> data,
      ProjectConfig projectConfig) {
    // Process mapper template with new structure
    String content = templateRepository.processTemplate(getMapperTemplate(data), data);

    // Resolve mapper path using PathResolver if project config is available
    Path filePath;
    if (projectConfig != null) {
      // Prepare context for path resolution
      Map<String, String> context = new HashMap<>();
      context.put("basePackage", projectConfig.basePackage());

      // Determine adapter type string (driven or driving)
      String adapterType = "driven"; // Default for output adapters

      // Resolve the adapter base path (relative to basePackage)
      Path adapterBasePath = pathResolver.resolveAdapterPath(
          projectConfig.architecture(),
          adapterType,
          config.name().toLowerCase(),
          context);

      // Convert base package to path
      String basePackagePath = projectConfig.basePackage().replace('.', '/');

      // Build full file path:
      // src/main/java/{basePackage}/{resolvedPath}/mapper/{Entity}Mapper.java
      filePath = projectPath
          .resolve("src/main/java")
          .resolve(basePackagePath)
          .resolve(adapterBasePath)
          .resolve("mapper")
          .resolve(config.entityName() + "Mapper.java");
    } else {
      // Fallback to hardcoded path if project config not available
      String mapperPackage = config.packageName() + ".mapper";
      String packagePath = mapperPackage.replace('.', '/');
      filePath = projectPath
          .resolve("src/main/java")
          .resolve(packagePath)
          .resolve(config.entityName() + "Mapper.java");
    }

    return GeneratedFile.javaSource(filePath, content);
  }

  private GeneratedFile generateDataEntity(Path projectPath, OutputAdapterConfig config, Map<String, Object> data,
      ProjectConfig projectConfig) {
    // Process data entity template
    String templatePath = getDataEntityTemplate(config.type(), data);
    String content = templateRepository.processTemplate(templatePath, data);

    // Resolve entity path using PathResolver if project config is available
    Path filePath;
    if (projectConfig != null) {
      // Prepare context for path resolution
      Map<String, String> context = new HashMap<>();
      context.put("basePackage", projectConfig.basePackage());

      // Determine adapter type string (driven or driving)
      String adapterType = "driven"; // Default for output adapters

      // Resolve the adapter base path (relative to basePackage)
      Path adapterBasePath = pathResolver.resolveAdapterPath(
          projectConfig.architecture(),
          adapterType,
          config.name().toLowerCase(),
          context);

      // Convert base package to path
      String basePackagePath = projectConfig.basePackage().replace('.', '/');

      // Build full file path:
      // src/main/java/{basePackage}/{resolvedPath}/entity/{Entity}Data.java
      filePath = projectPath
          .resolve("src/main/java")
          .resolve(basePackagePath)
          .resolve(adapterBasePath)
          .resolve("entity")
          .resolve(config.entityName() + "Data.java");
    } else {
      // Fallback to hardcoded path if project config not available
      String entityPackage = config.packageName() + ".entity";
      String packagePath = entityPackage.replace('.', '/');
      filePath = projectPath
          .resolve("src/main/java")
          .resolve(packagePath)
          .resolve(config.entityName() + "Data.java");
    }

    return GeneratedFile.javaSource(filePath, content);
  }

  /**
   * Checks if an adapter type needs entity mapper and data entity.
   * Uses metadata instead of hardcoded list.
   */
  private boolean needsEntityMapper(String adapterType, String framework, String paradigm) {
    // Get from metadata instead of hardcoded list
    return adapterDiscovery
        .findAdapter(adapterType, AdapterDirection.OUTPUT, framework, paradigm)
        .map(com.pragma.archetype.domain.model.adapter.AdapterMetadata::isNeedsMapper)
        .orElse(false);
  }

  private String getAdapterTemplate(String adapterType, Map<String, Object> data) {
    String framework = (String) data.get(VARIABLE_FRAMEWORK);
    String paradigm = (String) data.get(VARIABLE_PARADIGM);

    // Find adapter metadata (REQUIRED - no fallback)
    com.pragma.archetype.domain.model.adapter.AdapterMetadata metadata = adapterDiscovery
        .findAdapter(adapterType, AdapterDirection.OUTPUT, framework, paradigm)
        .orElseThrow(() -> {
          List<String> available = adapterDiscovery.listAdapterNames(
              AdapterDirection.OUTPUT, framework, paradigm);
          return new IllegalArgumentException(
              "Unknown output adapter type: " + adapterType +
                  " for " + framework + "/" + paradigm +
                  ". Available adapters: " + String.join(", ", available));
        });

    // Get main template from discovered templates
    com.pragma.archetype.domain.model.adapter.TemplateFile mainTemplate = metadata.getDiscoveredTemplates().stream()
        .filter(t -> "main".equals(t.getType()))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(
            "No main template found for adapter: " + adapterType));

    // Build template path
    String adapterDir = "driven-adapters";
    return String.format("frameworks/%s/%s/adapters/%s/%s/%s",
        framework, paradigm, adapterDir, adapterType, mainTemplate.getName());
  }

  private String getDataEntityTemplate(String adapterType, Map<String, Object> data) {
    String framework = (String) data.get(VARIABLE_FRAMEWORK);
    String paradigm = (String) data.get(VARIABLE_PARADIGM);

    // Find adapter metadata (REQUIRED - no fallback)
    com.pragma.archetype.domain.model.adapter.AdapterMetadata metadata = adapterDiscovery
        .findAdapter(adapterType, AdapterDirection.OUTPUT, framework, paradigm)
        .orElseThrow(() -> {
          List<String> available = adapterDiscovery.listAdapterNames(
              AdapterDirection.OUTPUT, framework, paradigm);
          return new IllegalArgumentException(
              "Unknown output adapter type: " + adapterType +
                  " for " + framework + "/" + paradigm +
                  ". Available adapters: " + String.join(", ", available));
        });

    // Get entity template from discovered templates
    com.pragma.archetype.domain.model.adapter.TemplateFile entityTemplate = metadata.getDiscoveredTemplates().stream()
        .filter(t -> t.getName().contains("Entity"))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(
            "No entity template found for adapter: " + adapterType));

    // Build template path
    String adapterDir = "driven-adapters";
    return String.format("frameworks/%s/%s/adapters/%s/%s/%s",
        framework, paradigm, adapterDir, adapterType, entityTemplate.getName());
  }

  private String getMapperTemplate(Map<String, Object> data) {
    String framework = (String) data.get(VARIABLE_FRAMEWORK);
    String paradigm = (String) data.get(VARIABLE_PARADIGM);
    String adapterDir = "driven-adapters";

    // Use generic mapper template
    return String.format("frameworks/%s/%s/adapters/%s/generic/Mapper.java.ftl",
        framework, paradigm, adapterDir);
  }

  /**
   * Prepares template data with complete context including project-level
   * variables.
   * This ensures all FreeMarker templates have access to required variables.
   * 
   * @param config        adapter configuration
   * @param projectConfig project configuration (can be null)
   * @return complete template context map
   */
  private Map<String, Object> prepareTemplateData(OutputAdapterConfig config, ProjectConfig projectConfig) {
    Map<String, Object> data = new HashMap<>();

    // Adapter-specific variables
    data.put("adapterName", config.name());
    data.put("packageName", config.packageName());
    data.put("entityName", config.entityName());
    data.put("adapterType", config.type().toLowerCase());

    // Add framework and paradigm from config
    data.put(VARIABLE_FRAMEWORK, config.framework());
    data.put(VARIABLE_PARADIGM, config.paradigm());

    // Project-level variables (if available)
    if (projectConfig != null) {
      data.put("basePackage", projectConfig.basePackage());
      data.put("projectName", projectConfig.name());
      data.put("architecture", projectConfig.architecture().name().toLowerCase());
    }

    // Convert methods to Maps for Freemarker
    List<Map<String, Object>> methodMaps = new ArrayList<>();
    if (config.methods() != null) {
      for (AdapterMethod method : config.methods()) {
        Map<String, Object> methodMap = new HashMap<>();
        methodMap.put("name", method.name());
        methodMap.put("returnType", method.returnType());

        // Convert parameters to Maps
        List<Map<String, Object>> paramMaps = new ArrayList<>();
        if (method.parameters() != null) {
          for (MethodParameter param : method.parameters()) {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("name", param.name());
            paramMap.put("type", param.type());
            paramMaps.add(paramMap);
          }
        }
        methodMap.put("parameters", paramMaps);
        methodMaps.add(methodMap);
      }
    }
    data.put("methods", methodMaps);

    return data;
  }
}
