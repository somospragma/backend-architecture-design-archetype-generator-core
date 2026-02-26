package com.pragma.archetype.domain.model.structure;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Value;

/**
 * Metadata describing a module in multi-module architectures.
 * Replaces simple String module names with rich metadata including packages.
 */
@Value
@Builder
public class ModuleMetadata {
  /**
   * Module name (e.g., "domain", "application", "infrastructure").
   */
  String name;

  /**
   * Module directory path relative to project root (e.g., "domain",
   * "infrastructure").
   */
  String path;

  /**
   * List of package paths to create within this module.
   * Example: ["model", "port/in", "port/out"]
   */
  @Builder.Default
  List<String> packages = Collections.emptyList();

  /**
   * List of Gradle dependencies for this module.
   * Format: "group:artifact:version" or "group:artifact"
   */
  @Builder.Default
  List<String> dependencies = Collections.emptyList();

  /**
   * List of other modules this module depends on.
   * Example: ["domain"] for application module
   */
  @Builder.Default
  List<String> moduleDependencies = Collections.emptyList();

  /**
   * Map of package types to their paths within this module.
   * Example: {"model": "model", "portIn": "port/in", "portOut": "port/out"}
   */
  @Builder.Default
  Map<String, String> packageTypes = Collections.emptyMap();

  /**
   * Gets the package path for a specific package type.
   *
   * @param packageType The package type to retrieve
   * @return The package path
   * @throws IllegalArgumentException if packageType is not found
   */
  public String getPackage(String packageType) {
    if (packageTypes.containsKey(packageType)) {
      return packageTypes.get(packageType);
    }
    throw new IllegalArgumentException(
        String.format(
            com.pragma.archetype.domain.model.error.StructureErrorMessages.PACKAGE_TYPE_NOT_FOUND,
            packageType,
            name));
  }

  /**
   * Validates that this module metadata is complete and valid.
   *
   * @return ValidationResult indicating if metadata is valid
   */
  public com.pragma.archetype.domain.model.validation.ValidationResult validate() {
    if (name == null || name.isBlank()) {
      return com.pragma.archetype.domain.model.validation.ValidationResult.failure(
          com.pragma.archetype.domain.model.error.StructureErrorMessages.MODULE_NAME_EMPTY);
    }
    if (path == null || path.isBlank()) {
      return com.pragma.archetype.domain.model.validation.ValidationResult.failure(
          com.pragma.archetype.domain.model.error.StructureErrorMessages.MODULE_PATH_EMPTY);
    }
    return com.pragma.archetype.domain.model.validation.ValidationResult.success();
  }

  /**
   * Checks if this module has package definitions.
   *
   * @return true if packages list is not empty
   */
  public boolean hasPackages() {
    return !packages.isEmpty();
  }

  /**
   * Checks if this module has dependencies.
   *
   * @return true if dependencies list is not empty
   */
  public boolean hasDependencies() {
    return !dependencies.isEmpty();
  }

  /**
   * Checks if this module depends on other modules.
   *
   * @return true if moduleDependencies list is not empty
   */
  public boolean hasModuleDependencies() {
    return !moduleDependencies.isEmpty();
  }
}
