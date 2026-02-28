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

    // Get framework and paradigm from config
    String framework = config.framework();
    String paradigm = config.paradigm();

    // Validate adapter type exists (with framework/paradigm)
    String adapterType = config.type();
    if (!adapterDiscovery.adapterExists(adapterType, AdapterDirection.INPUT, framework, paradigm)) {
      List<String> availableAdapters = adapterDiscovery.listAdapterNames(AdapterDirection.INPUT, framework, paradigm);
      throw new IllegalArgumentException(
          "Unknown input adapter type: " + adapterType +
              " for " + framework + "/" + paradigm +
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
    // Get framework and paradigm from config
    String framework = config.framework();
    String paradigm = config.paradigm();

    // Get adapter metadata (REQUIRED - no fallback)
    AdapterMetadata metadata = adapterDiscovery
        .findAdapter(config.type(), AdapterDirection.INPUT, framework, paradigm)
        .orElseThrow(() -> {
          List<String> available = adapterDiscovery.listAdapterNames(AdapterDirection.INPUT, framework, paradigm);
          return new IllegalArgumentException(
              "Unknown input adapter type: " + config.type() +
                  " for " + framework + "/" + paradigm +
                  ". Available adapters: " + String.join(", ", available));
        });

    // Select template based on adapter type (dynamic from metadata)
    String templatePath = getControllerTemplate(config, data, metadata);

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

  private String getControllerTemplate(InputAdapterConfig config, Map<String, Object> data, AdapterMetadata metadata) {
    // Get framework and paradigm from config
    String framework = config.framework();
    String paradigm = config.paradigm();

    // Get main template from discovered templates
    com.pragma.archetype.domain.model.adapter.TemplateFile mainTemplate = metadata.getDiscoveredTemplates().stream()
        .filter(t -> "main".equals(t.getType()))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(
            "No main template found for adapter: " + config.type()));

    // Build template path
    String adapterDir = "entry-points";
    return String.format("frameworks/%s/%s/adapters/%s/%s/%s",
        framework, paradigm, adapterDir, config.type(), mainTemplate.getName());
  }

  private String getControllerSuffix(AdapterMetadata metadata, String adapterType) {
    // Use suffix from metadata (REQUIRED - no fallback)
    if (metadata.getSuffix() != null && !metadata.getSuffix().isBlank()) {
      return metadata.getSuffix();
    }
    // Generic default if metadata doesn't specify
    return "Adapter";
  }

  private Map<String, Object> prepareTemplateData(InputAdapterConfig config) {
    Map<String, Object> data = new HashMap<>();
    data.put("controllerName", config.name());
    data.put("adapterName", config.name()); // Alias for GraphQL and other templates
    data.put("packageName", config.packageName());
    data.put("useCaseName", config.useCaseName());
    data.put("adapterType", config.type().toLowerCase());

    // Add framework and paradigm from config
    data.put(VARIABLE_FRAMEWORK, config.framework());
    data.put(VARIABLE_PARADIGM, config.paradigm());

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
