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

  private static final String TEMPLATE_TYPE_INPUT_ADAPTER = "inputAdapter";
  private static final String VARIABLE_FRAMEWORK = "framework";
  private static final String VARIABLE_PARADIGM = "paradigm";

  private static final String VARIABLE_ADAPTER_DIR = "adapterDir";

  private static final String VARIABLE_TYPE = "type";

  private static final String VARIABLE_ARCHITECTURE = "architecture";

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
    // Get adapter metadata to determine suffix (with fallback)
    AdapterMetadata metadata = adapterDiscovery
        .findAdapter(config.type(), AdapterDirection.INPUT)
        .orElse(null);

    // Select template based on adapter type (dynamic from metadata)
    String templatePath = getControllerTemplate(config, data);

    // Process template
    String content = templateRepository.processTemplate(templatePath, data);

    // Calculate file path:
    // infrastructure/adapter/in/{type}/{ControllerName}Controller.java
    String packagePath = config.packageName().replace('.', '/');
    String fileName = config.name() + getControllerSuffix(metadata, config.type()) + ".java";
    Path filePath = projectPath
        .resolve("src/main/java")
        .resolve(packagePath)
        .resolve(fileName);

    return GeneratedFile.javaSource(filePath, content);
  }

  private String getControllerTemplate(InputAdapterConfig config, Map<String, Object> data) {
    try {
      // Load StructureMetadata from architecture
      String architecture = (String) data.get(VARIABLE_ARCHITECTURE);
      if (architecture == null) {
        // Fallback to hardcoded template path if architecture not provided
        return getFallbackControllerTemplate(config.type());
      }

      com.pragma.archetype.domain.model.structure.StructureMetadata structureMetadata = templateRepository
          .loadStructureMetadata(com.pragma.archetype.domain.model.project.ArchitectureType.fromValue(architecture));

      // Prepare variables for template path resolution
      Map<String, String> variables = new java.util.HashMap<>();
      variables.put(VARIABLE_FRAMEWORK, (String) data.get(VARIABLE_FRAMEWORK));
      variables.put(VARIABLE_PARADIGM, (String) data.get(VARIABLE_PARADIGM));
      variables.put(VARIABLE_ADAPTER_DIR, structureMetadata.getAdapterDirName("driving"));
      variables.put(VARIABLE_TYPE, config.type().toLowerCase());
      variables.put(VARIABLE_ARCHITECTURE, architecture);

      // Resolve template path using StructureMetadata
      return structureMetadata.resolveTemplatePath(TEMPLATE_TYPE_INPUT_ADAPTER, variables);

    } catch (IllegalStateException e) {
      throw new IllegalStateException(
          com.pragma.archetype.domain.model.error.StructureErrorMessages.TEMPLATE_PATH_RESOLUTION_FAILED
              + ": " + e.getMessage(),
          e);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          com.pragma.archetype.domain.model.error.StructureErrorMessages.UNKNOWN_TEMPLATE_TYPE
              + ": " + TEMPLATE_TYPE_INPUT_ADAPTER,
          e);
    }
  }

  private String getFallbackControllerTemplate(String adapterType) {
    // Fallback to hardcoded template paths for backward compatibility
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

  private String getControllerSuffix(AdapterMetadata metadata, String adapterType) {
    // Use suffix from AdapterMetadata if available
    if (metadata != null && metadata.getSuffix() != null && !metadata.getSuffix().isBlank()) {
      return metadata.getSuffix();
    }
    // Fallback based on adapter type
    return getFallbackSuffix(adapterType);
  }

  private String getFallbackSuffix(String adapterType) {
    return switch (adapterType.toLowerCase()) {
      case "rest" -> "Controller";
      case "graphql" -> "Resolver";
      case "grpc" -> "Service";
      case "websocket" -> "Handler";
      case "sqs-consumer", "sqs_consumer" -> "Consumer";
      case "scheduled" -> "ScheduledTask";
      default -> "Adapter";
    };
  }

  private Map<String, Object> prepareTemplateData(InputAdapterConfig config) {
    Map<String, Object> data = new HashMap<>();
    data.put("controllerName", config.name());
    data.put("packageName", config.packageName());
    data.put("useCaseName", config.useCaseName());
    data.put("adapterType", config.type().toLowerCase());

    // Add required variables for template path resolution
    // These should be provided by the caller or extracted from config
    // For now, using defaults - this will need to be enhanced
    data.put(VARIABLE_FRAMEWORK, "spring");
    data.put(VARIABLE_PARADIGM, "reactive");
    data.put(VARIABLE_ARCHITECTURE, "hexagonal-single");

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
