package com.pragma.archetype.infrastructure.adapter.out.template;

import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import com.pragma.archetype.domain.model.project.ArchitectureType;
import com.pragma.archetype.domain.model.structure.LayerDependencies;
import com.pragma.archetype.domain.model.structure.NamingConventions;
import com.pragma.archetype.domain.model.structure.StructureMetadata;
import com.pragma.archetype.domain.model.validation.ValidationResult;
import com.pragma.archetype.domain.port.out.TemplateRepository.TemplateNotFoundException;

/**
 * Loads structure metadata from structure.yml files in the template repository.
 * Parses architecture structure definitions including adapter paths, naming
 * conventions,
 * and layer dependencies.
 */
public class StructureMetadataLoader {

  private final TemplateContentProvider contentProvider;
  private final Yaml yaml;

  /**
   * Creates a new StructureMetadataLoader.
   *
   * @param contentProvider Provider for template content
   */
  public StructureMetadataLoader(TemplateContentProvider contentProvider) {
    this.contentProvider = contentProvider;
    this.yaml = new Yaml();
  }

  /**
   * Loads structure metadata for the specified architecture.
   *
   * @param architecture The architecture type to load metadata for
   * @return StructureMetadata containing parsed structure information
   * @throws TemplateNotFoundException if structure.yml cannot be found or parsed
   */
  public StructureMetadata loadStructureMetadata(ArchitectureType architecture) {
    String structurePath = "architectures/" + architecture.getValue() + "/structure.yml";

    try {
      String yamlContent = contentProvider.getTemplateContent(structurePath);
      Map<String, Object> data = yaml.load(yamlContent);

      // Validate that we have data
      if (data == null || data.isEmpty()) {
        throw new IllegalArgumentException("Structure file is empty or invalid");
      }

      // Extract and validate required fields
      String architectureType = extractArchitectureType(data);
      Map<String, String> adapterPaths = extractAdapterPaths(data);

      // Extract optional fields
      NamingConventions namingConventions = extractNamingConventions(data);
      LayerDependencies layerDependencies = extractLayerDependencies(data);
      java.util.List<String> packages = extractPackages(data);
      java.util.List<String> modules = extractModules(data);

      // Create and validate the metadata
      StructureMetadata metadata = new StructureMetadata(
          architectureType,
          adapterPaths,
          namingConventions,
          layerDependencies,
          packages,
          modules);

      ValidationResult validation = metadata.validate();
      if (!validation.valid()) {
        throw new IllegalArgumentException(
            "Invalid structure metadata: " + String.join(", ", validation.errors()));
      }

      return metadata;

    } catch (Exception e) {
      throw new TemplateNotFoundException(
          "Failed to load structure metadata for architecture: " + architecture.getValue() +
              ". Error: " + e.getMessage());
    }
  }

  /**
   * Extracts the architecture type from the YAML data.
   */
  private String extractArchitectureType(Map<String, Object> data) {
    Object architectureType = data.get("architecture");
    if (architectureType == null) {
      throw new IllegalArgumentException("Required field 'architecture' is missing");
    }
    if (!(architectureType instanceof String)) {
      throw new IllegalArgumentException("Field 'architecture' must be a string");
    }
    return (String) architectureType;
  }

  /**
   * Extracts adapter paths from the YAML data.
   */
  @SuppressWarnings("unchecked")
  private Map<String, String> extractAdapterPaths(Map<String, Object> data) {
    Object adapterPathsObj = data.get("adapterPaths");
    if (adapterPathsObj == null) {
      throw new IllegalArgumentException("Required field 'adapterPaths' is missing");
    }
    if (!(adapterPathsObj instanceof Map)) {
      throw new IllegalArgumentException("Field 'adapterPaths' must be a map");
    }

    Map<String, String> adapterPaths = (Map<String, String>) adapterPathsObj;
    if (adapterPaths.isEmpty()) {
      throw new IllegalArgumentException("Field 'adapterPaths' cannot be empty");
    }

    return adapterPaths;
  }

  /**
   * Extracts naming conventions from the YAML data (optional).
   */
  @SuppressWarnings("unchecked")
  private NamingConventions extractNamingConventions(Map<String, Object> data) {
    if (!data.containsKey("namingConventions")) {
      return null;
    }

    Object namingData = data.get("namingConventions");
    if (!(namingData instanceof Map)) {
      throw new IllegalArgumentException("Field 'namingConventions' must be a map");
    }

    Map<String, Object> namingMap = (Map<String, Object>) namingData;
    Map<String, String> suffixes = extractStringMap(namingMap, "suffixes");
    Map<String, String> prefixes = extractStringMap(namingMap, "prefixes");

    return new NamingConventions(suffixes, prefixes);
  }

  /**
   * Extracts layer dependencies from the YAML data (optional).
   */
  @SuppressWarnings("unchecked")
  private LayerDependencies extractLayerDependencies(Map<String, Object> data) {
    if (!data.containsKey("layerDependencies")) {
      return null;
    }

    Object layerDepsObj = data.get("layerDependencies");
    if (!(layerDepsObj instanceof Map)) {
      throw new IllegalArgumentException("Field 'layerDependencies' must be a map");
    }

    Map<String, List<String>> allowedDeps = (Map<String, List<String>>) layerDepsObj;
    return new LayerDependencies(allowedDeps);
  }

  /**
   * Extracts packages from the YAML data (optional).
   */
  @SuppressWarnings("unchecked")
  private java.util.List<String> extractPackages(Map<String, Object> data) {
    if (!data.containsKey("packages")) {
      return null;
    }

    Object packagesObj = data.get("packages");
    if (!(packagesObj instanceof List)) {
      throw new IllegalArgumentException("Field 'packages' must be a list");
    }

    return (java.util.List<String>) packagesObj;
  }

  /**
   * Extracts modules from the YAML data (optional).
   * Used for multi-module architectures.
   */
  @SuppressWarnings("unchecked")
  private java.util.List<String> extractModules(Map<String, Object> data) {
    if (!data.containsKey("modules")) {
      return null;
    }

    Object modulesObj = data.get("modules");
    if (!(modulesObj instanceof List)) {
      throw new IllegalArgumentException("Field 'modules' must be a list");
    }

    return (java.util.List<String>) modulesObj;
  }

  /**
   * Extracts a string map from a parent map (helper method).
   */
  @SuppressWarnings("unchecked")
  private Map<String, String> extractStringMap(Map<String, Object> parent, String key) {
    Object value = parent.get(key);
    if (value == null) {
      return Map.of();
    }
    if (!(value instanceof Map)) {
      throw new IllegalArgumentException("Field '" + key + "' must be a map");
    }
    return (Map<String, String>) value;
  }
}
