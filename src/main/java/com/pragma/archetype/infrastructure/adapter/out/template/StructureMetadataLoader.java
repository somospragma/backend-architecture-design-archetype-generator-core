package com.pragma.archetype.infrastructure.adapter.out.template;

import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import com.pragma.archetype.domain.model.project.ArchitectureType;
import com.pragma.archetype.domain.model.structure.LayerDependencies;
import com.pragma.archetype.domain.model.structure.ModuleMetadata;
import com.pragma.archetype.domain.model.structure.NamingConventions;
import com.pragma.archetype.domain.model.structure.PackageMappings;
import com.pragma.archetype.domain.model.structure.StaticFile;
import com.pragma.archetype.domain.model.structure.StructureMetadata;
import com.pragma.archetype.domain.model.structure.TemplatePaths;
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
      TemplatePaths templatePaths = extractTemplatePaths(data);
      java.util.List<ModuleMetadata> moduleMetadata = extractModuleMetadata(data);
      PackageMappings packageMappings = extractPackageMappings(data);
      String entityModule = extractEntityModule(data);
      String useCaseModule = extractUseCaseModule(data);
      java.util.List<StaticFile> staticFiles = extractStaticFiles(data);
      String resourcesPath = extractResourcesPath(data);
      String sourceDirectory = extractSourceDirectory(data);
      String testDirectory = extractTestDirectory(data);

      // Create and validate the metadata
      StructureMetadata metadata = new StructureMetadata(
          architectureType,
          resourcesPath,
          adapterPaths,
          templatePaths,
          packageMappings,
          namingConventions,
          layerDependencies,
          packages,
          moduleMetadata,
          entityModule,
          useCaseModule,
          staticFiles,
          sourceDirectory,
          testDirectory,
          extractFolders(data),
          Boolean.TRUE.equals(data.get("adaptersAsModules")),
          data.get("multiModule") instanceof Boolean flag ? flag : null,
          data.get("useCaseInputPorts") instanceof Boolean inputPorts ? inputPorts : null);

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
   * Extracts organizing folders (optional): directories created at the project
   * root that are not modules themselves, e.g. "infrastructure/entry-points".
   */
  @SuppressWarnings("unchecked")
  private java.util.List<String> extractFolders(Map<String, Object> data) {
    Object foldersObj = data.get("folders");
    if (foldersObj == null) {
      return java.util.List.of();
    }
    if (!(foldersObj instanceof List)) {
      throw new IllegalArgumentException("Field 'folders' must be a list");
    }
    return (java.util.List<String>) foldersObj;
  }

  /**
   * Extracts template paths configuration from YAML data (optional).
   * Returns default template paths if not specified.
   */
  @SuppressWarnings("unchecked")
  private TemplatePaths extractTemplatePaths(Map<String, Object> data) {
    if (!data.containsKey("templatePaths")) {
      // Return default template paths
      return TemplatePaths.builder().build();
    }

    Map<String, Object> templatePathsData = (Map<String, Object>) data.get("templatePaths");

    return TemplatePaths.builder()
        .inputAdapter((String) templatePathsData.get("inputAdapter"))
        .outputAdapter((String) templatePathsData.get("outputAdapter"))
        .entity((String) templatePathsData.get("entity"))
        .useCase((String) templatePathsData.get("useCase"))
        .config((String) templatePathsData.get("config"))
        .build();
  }

  /**
   * Extracts module metadata from YAML data (optional).
   * Converts simple string list to ModuleMetadata objects for backward
   * compatibility.
   */
  @SuppressWarnings("unchecked")
  private java.util.List<ModuleMetadata> extractModuleMetadata(Map<String, Object> data) {
    // Check both "modules" and "moduleMetadata" keys (some architectures use one or the other)
    String key = data.containsKey("modules") ? "modules" : "moduleMetadata";
    if (!data.containsKey(key)) {
      return null;
    }

    Object modulesObj = data.get(key);
    if (!(modulesObj instanceof List)) {
      throw new IllegalArgumentException("Field 'modules' must be a list");
    }

    List<?> modulesList = (List<?>) modulesObj;
    if (modulesList.isEmpty()) {
      return java.util.Collections.emptyList();
    }

    // Check if modules are strings (old format) or maps (new format)
    Object firstModule = modulesList.get(0);

    if (firstModule instanceof String) {
      // Old format: simple string list - convert to ModuleMetadata
      return modulesList.stream()
          .map(m -> ModuleMetadata.builder()
              .name((String) m)
              .path((String) m)
              .build())
          .toList();
    } else if (firstModule instanceof Map) {
      // New format: full ModuleMetadata objects
      return modulesList.stream()
          .map(m -> {
            Map<String, Object> moduleMap = (Map<String, Object>) m;
            Map<String, String> packageTypes = extractStringMap(moduleMap, "packageTypes");
            return ModuleMetadata.builder()
                .name((String) moduleMap.get("name"))
                .path((String) moduleMap.getOrDefault("path", moduleMap.get("name")))
                .packages((List<String>) moduleMap.getOrDefault("packages", java.util.Collections.emptyList()))
                .dependencies((List<String>) moduleMap.getOrDefault("dependencies", java.util.Collections.emptyList()))
                .moduleDependencies(
                    (List<String>) moduleMap.getOrDefault("moduleDependencies", java.util.Collections.emptyList()))
                .packageTypes(packageTypes)
                .build();
          })
          .toList();
    } else {
      throw new IllegalArgumentException("Field 'modules' must contain strings or maps");
    }
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

  /**
   * Extracts package mappings from the YAML data.
   */
  @SuppressWarnings("unchecked")
  private PackageMappings extractPackageMappings(Map<String, Object> data) {
    if (!data.containsKey("packageMappings")) {
      return null;
    }

    var packageMappingsData = (Map<String, Object>) data.get("packageMappings");
    var rules = extractStringMap(packageMappingsData, "rules");
    var replacements = extractStringMap(packageMappingsData, "replacements");

    return PackageMappings.builder()
        .rules(rules)
        .replacements(replacements)
        .build();
  }

  /**
   * Extracts the resources path from structure.yml data.
   * Returns null if not specified (will use default in StructureMetadata).
   */
  private String extractResourcesPath(Map<String, Object> data) {
    return (String) data.get("resourcesPath");
  }

  /**
   * Extracts entity module name from the YAML data.
   */
  private String extractEntityModule(Map<String, Object> data) {
    return (String) data.get("entityModule");
  }

  /**
   * Extracts use case module name from the YAML data.
   */
  private String extractUseCaseModule(Map<String, Object> data) {
    return (String) data.get("useCaseModule");
  }

  /**
   * Extracts static files configuration from the YAML data.
   */
  @SuppressWarnings("unchecked")
  private java.util.List<StaticFile> extractStaticFiles(Map<String, Object> data) {
    Object staticFilesObj = data.get("staticFiles");
    if (staticFilesObj == null) {
      return null;
    }

    if (!(staticFilesObj instanceof java.util.List)) {
      return null;
    }

    java.util.List<Map<String, Object>> staticFilesList = (java.util.List<Map<String, Object>>) staticFilesObj;

    return staticFilesList.stream()
        .map(fileData -> {
          // Read processAsTemplate flag, default to true if not specified
          Boolean processAsTemplate = (Boolean) fileData.get("processAsTemplate");

          // Read binary flag, default to false if not specified
          Boolean binary = (Boolean) fileData.get("binary");

          // Read executable flag, default to false if not specified
          Boolean executable = (Boolean) fileData.get("executable");

          return StaticFile.builder()
              .source((String) fileData.get("source"))
              .destination((String) fileData.get("destination"))
              .description((String) fileData.get("description"))
              .processAsTemplate(processAsTemplate != null ? processAsTemplate : true)
              .binary(binary != null ? binary : false)
              .executable(executable != null ? executable : false)
              .build();
        })
        .collect(java.util.stream.Collectors.toList());
  }

  /**
   * Extracts source directory from structure.yml.
   * Returns null if not specified (will use default).
   */
  private String extractSourceDirectory(Map<String, Object> data) {
    return (String) data.get("sourceDirectory");
  }

  /**
   * Extracts test directory from structure.yml.
   * Returns null if not specified (will use default).
   */
  private String extractTestDirectory(Map<String, Object> data) {
    return (String) data.get("testDirectory");
  }
}
