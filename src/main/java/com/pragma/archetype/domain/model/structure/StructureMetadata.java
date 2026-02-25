package com.pragma.archetype.domain.model.structure;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import com.pragma.archetype.domain.model.validation.ValidationResult;

/**
 * Metadata describing an architecture's structure and conventions.
 * Loaded from structure.yml files in template repository.
 */
public record StructureMetadata(
    String architectureType,
    Map<String, String> adapterPaths,
    NamingConventions namingConventions,
    LayerDependencies layerDependencies,
    java.util.List<String> packages,
    java.util.List<String> modules) {

  public StructureMetadata {
    Objects.requireNonNull(architectureType, "Architecture type cannot be null");
    Objects.requireNonNull(adapterPaths, "Adapter paths cannot be null");
    adapterPaths = Collections.unmodifiableMap(adapterPaths);
    if (packages != null) {
      packages = Collections.unmodifiableList(packages);
    }
    if (modules != null) {
      modules = Collections.unmodifiableList(modules);
    }
  }

  /**
   * Resolves the path for an adapter based on its type and name.
   *
   * @param adapterType The type of adapter (driven or driving)
   * @param name        The adapter name
   * @return The resolved path with placeholders substituted
   */
  public String resolveAdapterPath(String adapterType, String name) {
    String pathTemplate = adapterPaths.get(adapterType);
    if (pathTemplate == null) {
      throw new IllegalArgumentException(
          "No adapter path defined for type: " + adapterType);
    }
    return pathTemplate.replace("{name}", name);
  }

  /**
   * Validates that all required fields are present.
   *
   * @return ValidationResult indicating if metadata is valid
   */
  public ValidationResult validate() {
    if (adapterPaths.isEmpty()) {
      return ValidationResult.failure("Adapter paths cannot be empty");
    }
    if (!adapterPaths.containsKey("driven") && !adapterPaths.containsKey("driving")) {
      return ValidationResult.failure(
          "Adapter paths must contain at least 'driven' or 'driving' entries");
    }
    return ValidationResult.success();
  }

  /**
   * Checks if this structure has naming conventions defined.
   */
  public boolean hasNamingConventions() {
    return namingConventions != null;
  }

  /**
   * Checks if this structure has layer dependencies defined.
   */
  public boolean hasLayerDependencies() {
    return layerDependencies != null;
  }

  /**
   * Checks if this structure supports multiple modules.
   */
  public boolean isMultiModule() {
    return modules != null && !modules.isEmpty();
  }

  /**
   * Gets the list of modules for multi-module architectures.
   */
  public java.util.List<String> getModules() {
    return modules != null ? modules : Collections.emptyList();
  }
}
