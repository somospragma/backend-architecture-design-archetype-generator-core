package com.pragma.archetype.application.generator;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pragma.archetype.application.port.out.AdapterDiscoveryPort;
import com.pragma.archetype.domain.model.adapter.AdapterDirection;
import com.pragma.archetype.domain.model.adapter.AdapterMetadata;
import com.pragma.archetype.domain.model.adapter.Endpoint;
import com.pragma.archetype.domain.model.adapter.EndpointParameter;
import com.pragma.archetype.domain.model.adapter.InputAdapterConfig;
import com.pragma.archetype.domain.model.file.GeneratedFile;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import com.pragma.archetype.domain.port.out.TemplateRepository;

/**
 * Generates input adapter files (REST controllers, GraphQL resolvers, etc.).
 * Application service that orchestrates file generation.
 * 
 * Uses dynamic adapter discovery instead of hardcoded enums.
 */
public class InputAdapterGenerator {

  private final TemplateRepository templateRepository;
  private final FileSystemPort fileSystemPort;
  private final AdapterDiscoveryPort adapterDiscovery;

  public InputAdapterGenerator(
      TemplateRepository templateRepository,
      FileSystemPort fileSystemPort,
      AdapterDiscoveryPort adapterDiscovery) {
    this.templateRepository = templateRepository;
    this.fileSystemPort = fileSystemPort;
    this.adapterDiscovery = adapterDiscovery;
  }

  /**
   * Generates input adapter files based on configuration.
   */
  public List<GeneratedFile> generate(Path projectPath, InputAdapterConfig config) {
    List<GeneratedFile> generatedFiles = new ArrayList<>();

    // Validate adapter type exists
    String adapterType = config.type();
    if (!adapterDiscovery.adapterExists(adapterType, AdapterDirection.INPUT)) {
      List<String> availableAdapters = adapterDiscovery.listAdapterNames(AdapterDirection.INPUT);
      throw new IllegalArgumentException(
          "Unknown input adapter type: " + adapterType +
              ". Available adapters: " + String.join(", ", availableAdapters));
    }

    // Prepare template data
    Map<String, Object> data = prepareTemplateData(config);

    // Generate controller/adapter
    GeneratedFile controllerFile = generateController(projectPath, config, data);
    generatedFiles.add(controllerFile);

    return generatedFiles;
  }

  private GeneratedFile generateController(Path projectPath, InputAdapterConfig config, Map<String, Object> data) {
    // Get adapter metadata to determine template path
    AdapterMetadata metadata = adapterDiscovery
        .findAdapter(config.type(), AdapterDirection.INPUT)
        .orElseThrow(() -> new IllegalArgumentException("Adapter not found: " + config.type()));

    // Select template based on adapter type (dynamic from metadata)
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

  private String getControllerTemplate(String adapterType) {
    // Dynamic template path based on adapter type
    // New structure:
    // frameworks/spring/reactive/adapters/entry-points/{type}/Controller.java.ftl
    return switch (adapterType.toLowerCase()) {
      case "rest" -> "frameworks/spring/reactive/adapters/entry-points/rest/Controller.java.ftl";
      case "graphql" -> "frameworks/spring/reactive/adapters/entry-points/graphql/Resolver.java.ftl";
      case "grpc" -> "frameworks/spring/reactive/adapters/entry-points/grpc/Service.java.ftl";
      case "websocket" -> "frameworks/spring/reactive/adapters/entry-points/websocket/Handler.java.ftl";
      case "sqs-consumer", "sqs_consumer" ->
        "frameworks/spring/reactive/adapters/entry-points/sqs-consumer/Consumer.java.ftl";
      case "scheduled" -> "frameworks/spring/reactive/adapters/entry-points/scheduled/ScheduledTask.java.ftl";
      default -> throw new IllegalArgumentException("No template defined for adapter type: " + adapterType);
    };
  }

  private String getControllerSuffix(String adapterType) {
    return switch (adapterType.toLowerCase()) {
      case "rest" -> "Controller";
      case "graphql" -> "Resolver";
      case "grpc" -> "Service";
      case "websocket" -> "Handler";
      case "sqs-consumer", "sqs_consumer" -> "Consumer";
      case "scheduled" -> "ScheduledTask";
      default -> "Adapter"; // Generic suffix for unknown types
    };
  }

  private Map<String, Object> prepareTemplateData(InputAdapterConfig config) {
    Map<String, Object> data = new HashMap<>();
    data.put("controllerName", config.name());
    data.put("packageName", config.packageName());
    data.put("useCaseName", config.useCaseName());
    data.put("adapterType", config.type().toLowerCase());

    // Convert endpoints to Maps for Freemarker
    List<Map<String, Object>> endpointMaps = new ArrayList<>();
    for (Endpoint endpoint : config.endpoints()) {
      Map<String, Object> endpointMap = new HashMap<>();
      endpointMap.put("path", endpoint.path());
      endpointMap.put("method", endpoint.method().name());
      endpointMap.put("useCaseMethod", endpoint.useCaseMethod());
      endpointMap.put("returnType", endpoint.returnType());

      // Convert parameters to Maps
      List<Map<String, Object>> paramMaps = new ArrayList<>();
      if (endpoint.parameters() != null) {
        for (EndpointParameter param : endpoint.parameters()) {
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
