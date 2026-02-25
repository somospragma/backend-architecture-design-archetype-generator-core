package com.pragma.archetype.application.generator;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pragma.archetype.domain.model.adapter.AdapterConfig;
import com.pragma.archetype.domain.model.adapter.AdapterType;
import com.pragma.archetype.domain.model.config.ProjectConfig;
import com.pragma.archetype.domain.model.file.GeneratedFile;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import com.pragma.archetype.domain.port.out.PathResolver;
import com.pragma.archetype.domain.port.out.TemplateRepository;

/**
 * Generates output adapter files (Redis, MongoDB, etc.).
 * Application service that orchestrates file generation.
 */
public class AdapterGenerator {

  private final TemplateRepository templateRepository;
  private final FileSystemPort fileSystemPort;
  private final PathResolver pathResolver;
  private final ProjectGenerator projectGenerator;

  public AdapterGenerator(TemplateRepository templateRepository, FileSystemPort fileSystemPort,
      PathResolver pathResolver) {
    this.templateRepository = templateRepository;
    this.fileSystemPort = fileSystemPort;
    this.pathResolver = pathResolver;
    this.projectGenerator = new ProjectGenerator(templateRepository, fileSystemPort);
  }

  /**
   * Generates adapter files based on configuration.
   * If ProjectConfig has adaptersAsModules=true, creates a new Gradle module.
   */
  public List<GeneratedFile> generate(Path projectPath, AdapterConfig config, ProjectConfig projectConfig) {
    List<GeneratedFile> generatedFiles = new ArrayList<>();

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
  private List<GeneratedFile> generateAdapterAsModule(Path projectPath, AdapterConfig config,
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

    // 3. Generate entity mapper if needed
    if (config.type() == AdapterType.REDIS ||
        config.type() == AdapterType.MONGODB) {
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
  private List<GeneratedFile> generateAdapterInPlace(Path projectPath, AdapterConfig config,
      ProjectConfig projectConfig) {
    List<GeneratedFile> generatedFiles = new ArrayList<>();

    // Prepare template data with complete context
    Map<String, Object> data = prepareTemplateData(config, projectConfig);

    // Generate adapter implementation
    GeneratedFile adapterFile = generateAdapter(projectPath, config, data, projectConfig);
    generatedFiles.add(adapterFile);

    // Generate entity mapper if needed
    if (config.type() == AdapterType.REDIS ||
        config.type() == AdapterType.MONGODB) {
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
  private GeneratedFile generateModuleBuildFile(Path projectPath, AdapterConfig config,
      ProjectConfig projectConfig, String modulePath) {
    Map<String, Object> data = new HashMap<>();
    data.put("adapterType", config.type().name().toLowerCase());
    data.put("basePackage", projectConfig.basePackage());

    // Select template based on adapter type
    String templatePath = "architectures/hexagonal-multi-granular/modules/driven-adapter-build.gradle.kts.ftl";
    String content = templateRepository.processTemplate(templatePath, data);

    Path filePath = projectPath.resolve(modulePath).resolve("build.gradle.kts");
    return new GeneratedFile(filePath, content, GeneratedFile.FileType.GRADLE_BUILD);
  }

  /**
   * Generates adapter implementation in module.
   */
  private GeneratedFile generateAdapterInModule(Path projectPath, AdapterConfig config, String modulePath,
      ProjectConfig projectConfig) {
    Map<String, Object> data = prepareTemplateData(config, projectConfig);
    String templatePath = getAdapterTemplate(config.type());
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
  private GeneratedFile generateMapperInModule(Path projectPath, AdapterConfig config, String modulePath,
      ProjectConfig projectConfig) {
    Map<String, Object> data = prepareTemplateData(config, projectConfig);
    String content = templateRepository.processTemplate(getMapperTemplate(), data);

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
  private GeneratedFile generateDataEntityInModule(Path projectPath, AdapterConfig config, String modulePath,
      ProjectConfig projectConfig) {
    Map<String, Object> data = prepareTemplateData(config, projectConfig);
    String templatePath = getDataEntityTemplate(config.type());
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

  private GeneratedFile generateAdapter(Path projectPath, AdapterConfig config, Map<String, Object> data,
      ProjectConfig projectConfig) {
    // Select template based on adapter type
    String templatePath = getAdapterTemplate(config.type());

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

  private GeneratedFile generateMapper(Path projectPath, AdapterConfig config, Map<String, Object> data,
      ProjectConfig projectConfig) {
    // Process mapper template with new structure
    String content = templateRepository.processTemplate(getMapperTemplate(), data);

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

  private GeneratedFile generateDataEntity(Path projectPath, AdapterConfig config, Map<String, Object> data,
      ProjectConfig projectConfig) {
    // Process data entity template
    String templatePath = getDataEntityTemplate(config.type());
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

  private String getAdapterTemplate(AdapterType type) {
    // New structure:
    // frameworks/spring/reactive/adapters/driven-adapters/{type}/Adapter.java.ftl
    return switch (type) {
      case REDIS -> "frameworks/spring/reactive/adapters/driven-adapters/redis/Adapter.java.ftl";
      case MONGODB -> "frameworks/spring/reactive/adapters/driven-adapters/mongodb/Adapter.java.ftl";
      case POSTGRESQL -> "frameworks/spring/reactive/adapters/driven-adapters/postgresql/Adapter.java.ftl";
      case REST_CLIENT -> "frameworks/spring/reactive/adapters/driven-adapters/rest-client/Adapter.java.ftl";
      case KAFKA -> "frameworks/spring/reactive/adapters/driven-adapters/kafka/Adapter.java.ftl";
    };
  }

  private String getDataEntityTemplate(AdapterType type) {
    // New structure:
    // frameworks/spring/reactive/adapters/driven-adapters/{type}/Entity.java.ftl
    return switch (type) {
      case REDIS -> "frameworks/spring/reactive/adapters/driven-adapters/redis/Entity.java.ftl";
      case MONGODB -> "frameworks/spring/reactive/adapters/driven-adapters/mongodb/Entity.java.ftl";
      case POSTGRESQL -> "frameworks/spring/reactive/adapters/driven-adapters/postgresql/Entity.java.ftl";
      default -> "frameworks/spring/reactive/adapters/driven-adapters/generic/Entity.java.ftl";
    };
  }

  private String getMapperTemplate() {
    // New structure:
    // frameworks/spring/reactive/adapters/driven-adapters/generic/Mapper.java.ftl
    return "frameworks/spring/reactive/adapters/driven-adapters/generic/Mapper.java.ftl";
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
  private Map<String, Object> prepareTemplateData(AdapterConfig config, ProjectConfig projectConfig) {
    Map<String, Object> data = new HashMap<>();

    // Adapter-specific variables
    data.put("adapterName", config.name());
    data.put("packageName", config.packageName());
    data.put("entityName", config.entityName());
    data.put("adapterType", config.type().name().toLowerCase());

    // Project-level variables (if available)
    if (projectConfig != null) {
      data.put("basePackage", projectConfig.basePackage());
      data.put("projectName", projectConfig.name());
      data.put("framework", projectConfig.framework().name().toLowerCase());
      data.put("paradigm", projectConfig.paradigm().name().toLowerCase());
      data.put("architecture", projectConfig.architecture().name().toLowerCase());
    }

    // Convert methods to Maps for Freemarker
    List<Map<String, Object>> methodMaps = new ArrayList<>();
    if (config.methods() != null) {
      for (AdapterConfig.AdapterMethod method : config.methods()) {
        Map<String, Object> methodMap = new HashMap<>();
        methodMap.put("name", method.name());
        methodMap.put("returnType", method.returnType());

        // Convert parameters to Maps
        List<Map<String, Object>> paramMaps = new ArrayList<>();
        if (method.parameters() != null) {
          for (AdapterConfig.MethodParameter param : method.parameters()) {
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
