package com.pragma.archetype.application.generator;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pragma.archetype.domain.model.GeneratedFile;
import com.pragma.archetype.domain.model.adapter.InputAdapterConfig;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import com.pragma.archetype.domain.port.out.TemplateRepository;

/**
 * Generates input adapter files (REST controllers, GraphQL resolvers, etc.).
 * Application service that orchestrates file generation.
 */
public class InputAdapterGenerator {

  private final TemplateRepository templateRepository;
  private final FileSystemPort fileSystemPort;

  public InputAdapterGenerator(TemplateRepository templateRepository, FileSystemPort fileSystemPort) {
    this.templateRepository = templateRepository;
    this.fileSystemPort = fileSystemPort;
  }

  /**
   * Generates input adapter files based on configuration.
   */
  public List<GeneratedFile> generate(Path projectPath, InputAdapterConfig config) {
    List<GeneratedFile> generatedFiles = new ArrayList<>();

    // Prepare template data
    Map<String, Object> data = prepareTemplateData(config);

    // Generate controller/adapter
    GeneratedFile controllerFile = generateController(projectPath, config, data);
    generatedFiles.add(controllerFile);

    return generatedFiles;
  }

  private GeneratedFile generateController(Path projectPath, InputAdapterConfig config, Map<String, Object> data) {
    // Select template based on adapter type
    String templatePath = getControllerTemplate(config.type());

    // Process template
    String content = templateRepository.processTemplate(templatePath, data);

    // Calculate file path:
    // infrastructure/adapter/in/{type}/{ControllerName}Controller.java
    String packagePath = config.packageName().replace('.', '/');
    String fileName = config.name() + getControllerSuffix(config.type()) + ".java";
    Path filePath = projectPath
        .resolve("src/main/java")
        .resolve(packagePath)
        .resolve(fileName);

    return GeneratedFile.javaSource(filePath, content);
  }

  private String getControllerTemplate(InputAdapterConfig.InputAdapterType type) {
    // New structure:
    // frameworks/spring/reactive/adapters/entry-points/{type}/Controller.java.ftl
    return switch (type) {
      case REST -> "frameworks/spring/reactive/adapters/entry-points/rest/Controller.java.ftl";
      case GRAPHQL -> "frameworks/spring/reactive/adapters/entry-points/graphql/Resolver.java.ftl";
      case GRPC -> "frameworks/spring/reactive/adapters/entry-points/grpc/Service.java.ftl";
      case WEBSOCKET -> "frameworks/spring/reactive/adapters/entry-points/websocket/Handler.java.ftl";
    };
  }

  private String getControllerSuffix(InputAdapterConfig.InputAdapterType type) {
    return switch (type) {
      case REST -> "Controller";
      case GRAPHQL -> "Resolver";
      case GRPC -> "Service";
      case WEBSOCKET -> "Handler";
    };
  }

  private Map<String, Object> prepareTemplateData(InputAdapterConfig config) {
    Map<String, Object> data = new HashMap<>();
    data.put("controllerName", config.name());
    data.put("packageName", config.packageName());
    data.put("useCaseName", config.useCaseName());
    data.put("adapterType", config.type().name().toLowerCase());

    // Convert endpoints to Maps for Freemarker
    List<Map<String, Object>> endpointMaps = new ArrayList<>();
    for (InputAdapterConfig.Endpoint endpoint : config.endpoints()) {
      Map<String, Object> endpointMap = new HashMap<>();
      endpointMap.put("path", endpoint.path());
      endpointMap.put("method", endpoint.method().name());
      endpointMap.put("useCaseMethod", endpoint.useCaseMethod());
      endpointMap.put("returnType", endpoint.returnType());

      // Convert parameters to Maps
      List<Map<String, Object>> paramMaps = new ArrayList<>();
      if (endpoint.parameters() != null) {
        for (InputAdapterConfig.EndpointParameter param : endpoint.parameters()) {
          Map<String, Object> paramMap = new HashMap<>();
          paramMap.put("name", param.name());
          paramMap.put("type", param.type());
          paramMap.put("paramType", param.paramType().name());
          paramMaps.add(paramMap);
        }
      }
      endpointMap.put("parameters", paramMaps);
      endpointMaps.add(endpointMap);
    }
    data.put("endpoints", endpointMaps);

    return data;
  }
}
