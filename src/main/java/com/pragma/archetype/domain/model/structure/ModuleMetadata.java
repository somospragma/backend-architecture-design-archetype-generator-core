package com.pragma.archetype.domain.model.structure;

import java.util.Collections;
import java.util.List;

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
