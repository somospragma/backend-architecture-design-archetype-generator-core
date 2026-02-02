package com.pragma.archetype.application.generator;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pragma.archetype.domain.model.AdapterConfig;
import com.pragma.archetype.domain.model.GeneratedFile;
import com.pragma.archetype.domain.model.ProjectConfig;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import com.pragma.archetype.domain.port.out.TemplateRepository;

/**
 * Generates output adapter files (Redis, MongoDB, etc.).
 * Application service that orchestrates file generation.
 */
public class AdapterGenerator {

  private final TemplateRepository templateRepository;
  private final FileSystemPort fileSystemPort;
  private final ProjectGenerator projectGenerator;

  public AdapterGenerator(TemplateRepository templateRepository, FileSystemPort fileSystemPort) {
    this.templateRepository = templateRepository;
    this.fileSystemPort = fileSystemPort;
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
      generatedFiles.addAll(generateAdapterInPlace(projectPath, config));
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
    // driven-adapters go in: infrastructure/driven-adapters/{adapter-name}
    String moduleName = config.name().toLowerCase().replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase();
    String modulePath = "infrastructure/driven-adapters/" + moduleName;

    // 1. Create module build.gradle.kts
    GeneratedFile buildFile = generateModuleBuildFile(projectPath, config, projectConfig, modulePath);
    generatedFiles.add(buildFile);

    // 2. Generate adapter implementation in the module
    GeneratedFile adapterFile = generateAdapterInModule(projectPath, config, modulePath);
    generatedFiles.add(adapterFile);

    // 3. Generate entity mapper if needed
    if (config.type() == AdapterConfig.AdapterType.REDIS ||
        config.type() == AdapterConfig.AdapterType.MONGODB) {
      GeneratedFile mapperFile = generateMapperInModule(projectPath, config, modulePath);
      generatedFiles.add(mapperFile);

      GeneratedFile entityFile = generateDataEntityInModule(projectPath, config, modulePath);
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
  private List<GeneratedFile> generateAdapterInPlace(Path projectPath, AdapterConfig config) {
    List<GeneratedFile> generatedFiles = new ArrayList<>();

    // Prepare template data
    Map<String, Object> data = prepareTemplateData(config);

    // Generate adapter implementation
    GeneratedFile adapterFile = generateAdapter(projectPath, config, data);
    generatedFiles.add(adapterFile);

    // Generate entity mapper if needed
    if (config.type() == AdapterConfig.AdapterType.REDIS ||
        config.type() == AdapterConfig.AdapterType.MONGODB) {
      GeneratedFile mapperFile = generateMapper(projectPath, config, data);
      generatedFiles.add(mapperFile);

      GeneratedFile entityFile = generateDataEntity(projectPath, config, data);
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
  private GeneratedFile generateAdapterInModule(Path projectPath, AdapterConfig config, String modulePath) {
    Map<String, Object> data = prepareTemplateData(config);
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
  private GeneratedFile generateMapperInModule(Path projectPath, AdapterConfig config, String modulePath) {
    Map<String, Object> data = prepareTemplateData(config);
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
  private GeneratedFile generateDataEntityInModule(Path projectPath, AdapterConfig config, String modulePath) {
    Map<String, Object> data = prepareTemplateData(config);
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

  private GeneratedFile generateAdapter(Path projectPath, AdapterConfig config, Map<String, Object> data) {
    // Select template based on adapter type
    String templatePath = getAdapterTemplate(config.type());

    // Process template
    String content = templateRepository.processTemplate(templatePath, data);

    // Calculate file path:
    // infrastructure/adapter/out/{type}/{AdapterName}Adapter.java
    String packagePath = config.packageName().replace('.', '/');
    Path filePath = projectPath
        .resolve("src/main/java")
        .resolve(packagePath)
        .resolve(config.name() + "Adapter.java");

    return GeneratedFile.javaSource(filePath, content);
  }

  private GeneratedFile generateMapper(Path projectPath, AdapterConfig config, Map<String, Object> data) {
    // Process mapper template with new structure
    String content = templateRepository.processTemplate(getMapperTemplate(), data);

    // Calculate file path:
    // infrastructure/adapter/out/{type}/mapper/{Entity}Mapper.java
    String mapperPackage = config.packageName() + ".mapper";
    String packagePath = mapperPackage.replace('.', '/');
    Path filePath = projectPath
        .resolve("src/main/java")
        .resolve(packagePath)
        .resolve(config.entityName() + "Mapper.java");

    return GeneratedFile.javaSource(filePath, content);
  }

  private GeneratedFile generateDataEntity(Path projectPath, AdapterConfig config, Map<String, Object> data) {
    // Process data entity template
    String templatePath = getDataEntityTemplate(config.type());
    String content = templateRepository.processTemplate(templatePath, data);

    // Calculate file path:
    // infrastructure/adapter/out/{type}/entity/{Entity}Data.java
    String entityPackage = config.packageName() + ".entity";
    String packagePath = entityPackage.replace('.', '/');
    Path filePath = projectPath
        .resolve("src/main/java")
        .resolve(packagePath)
        .resolve(config.entityName() + "Data.java");

    return GeneratedFile.javaSource(filePath, content);
  }

  private String getAdapterTemplate(AdapterConfig.AdapterType type) {
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

  private String getDataEntityTemplate(AdapterConfig.AdapterType type) {
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

  private Map<String, Object> prepareTemplateData(AdapterConfig config) {
    Map<String, Object> data = new HashMap<>();
    data.put("adapterName", config.name());
    data.put("packageName", config.packageName());
    data.put("entityName", config.entityName());
    data.put("adapterType", config.type().name().toLowerCase());

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
