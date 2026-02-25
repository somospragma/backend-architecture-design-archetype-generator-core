package com.pragma.archetype.infrastructure.adapter.out.template;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import com.pragma.archetype.domain.model.adapter.AdapterMetadata;
import com.pragma.archetype.domain.model.adapter.AdapterMetadata.ConfigurationClass;
import com.pragma.archetype.domain.model.adapter.AdapterMetadata.Dependency;
import com.pragma.archetype.domain.model.validation.ValidationResult;
import com.pragma.archetype.domain.port.out.TemplateRepository.TemplateNotFoundException;

/**
 * Loads adapter metadata from metadata.yml files in the template repository.
 * Parses adapter definitions including dependencies, test dependencies,
 * application properties templates, and configuration classes.
 */
public class AdapterMetadataLoader {

  private final TemplateContentProvider contentProvider;
  private final Yaml yaml;

  /**
   * Creates a new AdapterMetadataLoader.
   *
   * @param contentProvider Provider for template content
   */
  public AdapterMetadataLoader(TemplateContentProvider contentProvider) {
    this.contentProvider = contentProvider;
    this.yaml = new Yaml();
  }

  /**
   * Loads adapter metadata for the specified adapter using legacy flat structure.
   * This method is deprecated and will try both legacy and framework-aware paths.
   *
   * @param adapterName The name of the adapter to load metadata for
   * @return AdapterMetadata containing parsed adapter information
   * @throws TemplateNotFoundException if metadata.yml cannot be found or parsed
   * @deprecated Use {@link #loadAdapterMetadata(String, String, String, String)}
   *             instead
   */
  @Deprecated
  public AdapterMetadata loadAdapterMetadata(String adapterName) {
    // Try legacy path first for backward compatibility
    String legacyPath = "adapters/" + adapterName + "/metadata.yml";

    try {
      return loadMetadataFromPath(legacyPath, adapterName);
    } catch (Exception e) {
      // If legacy path fails, we can't determine framework/paradigm without more
      // context
      throw new TemplateNotFoundException(
          "Failed to load adapter metadata for: " + adapterName +
              ". Legacy path not found. Use loadAdapterMetadata(adapterName, framework, paradigm, adapterType) instead. "
              +
              "Error: " + e.getMessage());
    }
  }

  /**
   * Loads adapter metadata for the specified adapter using framework-aware
   * structure.
   * Resolves the correct path based on framework, paradigm, and adapter type.
   *
   * @param adapterName The name of the adapter to load metadata for
   * @param framework   The framework (e.g., "spring", "quarkus")
   * @param paradigm    The paradigm (e.g., "reactive", "imperative")
   * @param adapterType The adapter type (e.g., "driven-adapter", "entry-point")
   * @return AdapterMetadata containing parsed adapter information
   * @throws TemplateNotFoundException if metadata.yml cannot be found or parsed
   */
  public AdapterMetadata loadAdapterMetadata(
      String adapterName,
      String framework,
      String paradigm,
      String adapterType) {

    // Build framework-aware path:
    // frameworks/{framework}/{paradigm}/adapters/{adapterType}/{adapterName}/metadata.yml
    String metadataPath = String.format(
        "frameworks/%s/%s/adapters/%s/%s/metadata.yml",
        framework.toLowerCase(),
        paradigm.toLowerCase(),
        adapterType.toLowerCase(),
        adapterName.toLowerCase());

    try {
      return loadMetadataFromPath(metadataPath, adapterName);
    } catch (Exception e) {
      // Try legacy path as fallback
      String legacyPath = "adapters/" + adapterName + "/metadata.yml";
      try {
        return loadMetadataFromPath(legacyPath, adapterName);
      } catch (Exception legacyError) {
        throw new TemplateNotFoundException(
            "Failed to load adapter metadata for: " + adapterName +
                ". Tried framework-aware path: " + metadataPath +
                " and legacy path: " + legacyPath +
                ". Error: " + e.getMessage());
      }
    }
  }

  /**
   * Loads metadata from a specific path.
   */
  private AdapterMetadata loadMetadataFromPath(String metadataPath, String adapterName) {
    try {
      String yamlContent = contentProvider.getTemplateContent(metadataPath);
      Map<String, Object> data = yaml.load(yamlContent);

      // Validate that we have data
      if (data == null || data.isEmpty()) {
        throw new IllegalArgumentException("Metadata file is empty or invalid");
      }

      // Extract required fields
      String name = extractName(data);
      String type = extractType(data);
      String description = extractDescription(data);
      List<Dependency> dependencies = extractDependencies(data);

      // Extract new optional fields
      List<Dependency> testDependencies = extractTestDependencies(data);
      String applicationPropertiesTemplate = extractApplicationPropertiesTemplate(data);
      List<ConfigurationClass> configurationClasses = extractConfigurationClasses(data);

      // Validate referenced template files exist
      // Extract base path from metadata path (remove /metadata.yml)
      String basePath = metadataPath.substring(0, metadataPath.lastIndexOf("/metadata.yml"));
      validateReferencedTemplates(basePath, applicationPropertiesTemplate, configurationClasses);

      // Create and validate the metadata
      AdapterMetadata metadata = new AdapterMetadata(
          name,
          type,
          description,
          dependencies,
          testDependencies,
          applicationPropertiesTemplate,
          configurationClasses);

      ValidationResult validation = metadata.validate();
      if (!validation.valid()) {
        throw new IllegalArgumentException(
            "Invalid adapter metadata: " + String.join(", ", validation.errors()));
      }

      return metadata;

    } catch (Exception e) {
      throw new TemplateNotFoundException(
          "Failed to load adapter metadata from: " + metadataPath +
              ". Error: " + e.getMessage());
    }
  }

  /**
   * Extracts the adapter name from the YAML data.
   */
  private String extractName(Map<String, Object> data) {
    Object name = data.get("name");
    if (name == null) {
      throw new IllegalArgumentException("Required field 'name' is missing");
    }
    if (!(name instanceof String)) {
      throw new IllegalArgumentException("Field 'name' must be a string");
    }
    return (String) name;
  }

  /**
   * Extracts the adapter type from the YAML data.
   */
  private String extractType(Map<String, Object> data) {
    Object type = data.get("type");
    if (type == null) {
      throw new IllegalArgumentException("Required field 'type' is missing");
    }
    if (!(type instanceof String)) {
      throw new IllegalArgumentException("Field 'type' must be a string");
    }
    return (String) type;
  }

  /**
   * Extracts the adapter description from the YAML data (optional).
   */
  private String extractDescription(Map<String, Object> data) {
    Object description = data.get("description");
    if (description == null) {
      return "";
    }
    if (!(description instanceof String)) {
      throw new IllegalArgumentException("Field 'description' must be a string");
    }
    return (String) description;
  }

  /**
   * Extracts dependencies from the YAML data.
   * Supports both flat format and nested format (dependencies.gradle).
   */
  @SuppressWarnings("unchecked")
  private List<Dependency> extractDependencies(Map<String, Object> data) {
    Object depsObj = data.get("dependencies");
    if (depsObj == null) {
      return List.of();
    }

    // Check if it's nested format (dependencies.gradle)
    if (depsObj instanceof Map) {
      Map<String, Object> depsMap = (Map<String, Object>) depsObj;
      Object gradleDeps = depsMap.get("gradle");
      if (gradleDeps != null && gradleDeps instanceof List) {
        // Nested format: dependencies.gradle
        return parseDependencyList((List<Map<String, Object>>) gradleDeps, "compile", true);
      }
    }

    // Flat format: dependencies as list
    if (!(depsObj instanceof List)) {
      throw new IllegalArgumentException("Field 'dependencies' must be a list or map with 'gradle' key");
    }

    List<Map<String, Object>> depsList = (List<Map<String, Object>>) depsObj;
    return parseDependencyList(depsList, "compile", false);
  }

  /**
   * Extracts test dependencies from the YAML data (NEW).
   * Supports both flat format and nested format (testDependencies.gradle).
   */
  @SuppressWarnings("unchecked")
  private List<Dependency> extractTestDependencies(Map<String, Object> data) {
    Object testDepsObj = data.get("testDependencies");
    if (testDepsObj == null) {
      return List.of();
    }

    // Check if it's nested format (testDependencies.gradle)
    if (testDepsObj instanceof Map) {
      Map<String, Object> testDepsMap = (Map<String, Object>) testDepsObj;
      Object gradleDeps = testDepsMap.get("gradle");
      if (gradleDeps != null && gradleDeps instanceof List) {
        // Nested format: testDependencies.gradle
        return parseDependencyList((List<Map<String, Object>>) gradleDeps, "test", true);
      }
    }

    // Flat format: testDependencies as list
    if (!(testDepsObj instanceof List)) {
      throw new IllegalArgumentException("Field 'testDependencies' must be a list or map with 'gradle' key");
    }

    List<Map<String, Object>> testDepsList = (List<Map<String, Object>>) testDepsObj;
    return parseDependencyList(testDepsList, "test", false);
  }

  /**
   * Parses a list of dependencies.
   * 
   * @param depsList       list of dependency maps
   * @param defaultScope   default scope if not specified
   * @param isNestedFormat true if using nested format (groupId/artifactId), false
   *                       for flat format (group/artifact)
   * @return list of parsed dependencies
   */
  private List<Dependency> parseDependencyList(
      List<Map<String, Object>> depsList,
      String defaultScope,
      boolean isNestedFormat) {
    List<Dependency> dependencies = new ArrayList<>();

    for (Map<String, Object> depData : depsList) {
      dependencies.add(parseDependency(depData, defaultScope, isNestedFormat));
    }

    return dependencies;
  }

  /**
   * Parses a single dependency from YAML data.
   * Supports both flat format (group/artifact) and nested format
   * (groupId/artifactId).
   * 
   * @param depData        dependency data map
   * @param defaultScope   default scope if not specified
   * @param isNestedFormat true if using nested format (groupId/artifactId)
   * @return parsed Dependency
   */
  private Dependency parseDependency(Map<String, Object> depData, String defaultScope, boolean isNestedFormat) {
    // Extract group/groupId
    String group = isNestedFormat
        ? extractOptionalString(depData, "groupId")
        : extractOptionalString(depData, "group");

    // Fallback: try the other format if not found
    if (group == null) {
      group = isNestedFormat
          ? extractOptionalString(depData, "group")
          : extractOptionalString(depData, "groupId");
    }

    if (group == null) {
      throw new IllegalArgumentException("Required field 'group' or 'groupId' is missing");
    }

    // Extract artifact/artifactId
    String artifact = isNestedFormat
        ? extractOptionalString(depData, "artifactId")
        : extractOptionalString(depData, "artifact");

    // Fallback: try the other format if not found
    if (artifact == null) {
      artifact = isNestedFormat
          ? extractOptionalString(depData, "artifact")
          : extractOptionalString(depData, "artifactId");
    }

    if (artifact == null) {
      throw new IllegalArgumentException("Required field 'artifact' or 'artifactId' is missing");
    }

    String version = extractOptionalString(depData, "version");
    String scope = extractOptionalString(depData, "scope");

    // Use provided scope or default scope
    if (scope == null || scope.isBlank()) {
      scope = defaultScope;
    }

    return new Dependency(group, artifact, version, scope);
  }

  /**
   * @deprecated Use {@link #parseDependency(Map, String, boolean)} instead
   */
  @Deprecated
  private Dependency parseDependency(Map<String, Object> depData, String defaultScope) {
    return parseDependency(depData, defaultScope, false);
  }

  /**
   * Extracts application properties template path from the YAML data (NEW).
   */
  private String extractApplicationPropertiesTemplate(Map<String, Object> data) {
    Object template = data.get("applicationPropertiesTemplate");
    if (template == null) {
      return null;
    }
    if (!(template instanceof String)) {
      throw new IllegalArgumentException("Field 'applicationPropertiesTemplate' must be a string");
    }
    return (String) template;
  }

  /**
   * Extracts configuration classes from the YAML data (NEW).
   */
  @SuppressWarnings("unchecked")
  private List<ConfigurationClass> extractConfigurationClasses(Map<String, Object> data) {
    Object configClassesObj = data.get("configurationClasses");
    if (configClassesObj == null) {
      return List.of();
    }

    if (!(configClassesObj instanceof List)) {
      throw new IllegalArgumentException("Field 'configurationClasses' must be a list");
    }

    List<Map<String, Object>> configClassesList = (List<Map<String, Object>>) configClassesObj;
    List<ConfigurationClass> configurationClasses = new ArrayList<>();

    for (Map<String, Object> configData : configClassesList) {
      String name = extractRequiredString(configData, "name");
      String packagePath = extractRequiredString(configData, "packagePath");
      String templatePath = extractRequiredString(configData, "templatePath");

      configurationClasses.add(new ConfigurationClass(name, packagePath, templatePath));
    }

    return configurationClasses;
  }

  /**
   * Validates that referenced template files exist.
   */
  private void validateReferencedTemplates(
      String basePath,
      String applicationPropertiesTemplate,
      List<ConfigurationClass> configurationClasses) {

    // Validate application properties template if specified
    if (applicationPropertiesTemplate != null && !applicationPropertiesTemplate.isBlank()) {
      String fullPath = basePath + "/" + applicationPropertiesTemplate;
      if (!contentProvider.templateExists(fullPath)) {
        throw new IllegalArgumentException(
            "Referenced application properties template not found: " + fullPath);
      }
    }

    // Validate configuration class templates
    for (ConfigurationClass configClass : configurationClasses) {
      String fullPath = basePath + "/" + configClass.templatePath();
      if (!contentProvider.templateExists(fullPath)) {
        throw new IllegalArgumentException(
            "Referenced configuration class template not found: " + fullPath +
                " for class: " + configClass.name());
      }
    }
  }

  /**
   * Extracts a required string field from a map.
   */
  private String extractRequiredString(Map<String, Object> map, String key) {
    Object value = map.get(key);
    if (value == null) {
      throw new IllegalArgumentException("Required field '" + key + "' is missing");
    }
    if (!(value instanceof String)) {
      throw new IllegalArgumentException("Field '" + key + "' must be a string");
    }
    return (String) value;
  }

  /**
   * Extracts an optional string field from a map.
   */
  private String extractOptionalString(Map<String, Object> map, String key) {
    Object value = map.get(key);
    if (value == null) {
      return null;
    }
    if (!(value instanceof String)) {
      throw new IllegalArgumentException("Field '" + key + "' must be a string");
    }
    return (String) value;
  }
}
