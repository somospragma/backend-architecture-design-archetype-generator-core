package com.pragma.archetype.application.generator;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pragma.archetype.domain.model.AdapterConfig;
import com.pragma.archetype.domain.model.GeneratedFile;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import com.pragma.archetype.domain.port.out.TemplateRepository;

/**
 * Generates output adapter files (Redis, MongoDB, etc.).
 * Application service that orchestrates file generation.
 */
public class AdapterGenerator {

  private final TemplateRepository templateRepository;
  private final FileSystemPort fileSystemPort;

  public AdapterGenerator(TemplateRepository templateRepository, FileSystemPort fileSystemPort) {
    this.templateRepository = templateRepository;
    this.fileSystemPort = fileSystemPort;
  }

  /**
   * Generates adapter files based on configuration.
   */
  public List<GeneratedFile> generate(Path projectPath, AdapterConfig config) {
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
    // Process mapper template
    String content = templateRepository.processTemplate("components/adapter/EntityMapper.java.ftl", data);

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
    return switch (type) {
      case REDIS -> "components/adapter/RedisAdapter.java.ftl";
      case MONGODB -> "components/adapter/MongoAdapter.java.ftl";
      case POSTGRESQL -> "components/adapter/PostgresAdapter.java.ftl";
      case REST_CLIENT -> "components/adapter/RestClientAdapter.java.ftl";
      case KAFKA -> "components/adapter/KafkaAdapter.java.ftl";
    };
  }

  private String getDataEntityTemplate(AdapterConfig.AdapterType type) {
    return switch (type) {
      case REDIS -> "components/adapter/RedisEntity.java.ftl";
      case MONGODB -> "components/adapter/MongoEntity.java.ftl";
      case POSTGRESQL -> "components/adapter/PostgresEntity.java.ftl";
      default -> "components/adapter/DataEntity.java.ftl";
    };
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
