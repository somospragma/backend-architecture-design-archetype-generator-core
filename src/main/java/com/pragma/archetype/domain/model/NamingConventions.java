package com.pragma.archetype.domain.model;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Naming conventions for components in an architecture.
 * Defines suffixes and prefixes to apply to component names.
 */
public record NamingConventions(
    Map<String, String> suffixes,
    Map<String, String> prefixes) {

  public NamingConventions {
    suffixes = suffixes != null ? Collections.unmodifiableMap(suffixes) : Map.of();
    prefixes = prefixes != null ? Collections.unmodifiableMap(prefixes) : Map.of();
  }

  /**
   * Applies naming conventions to a component name.
   *
   * @param componentType The type of component (e.g., "useCase", "port",
   *                      "adapter")
   * @param baseName      The base name without conventions
   * @return The name with conventions applied
   */
  public String applyConventions(String componentType, String baseName) {
    Objects.requireNonNull(componentType, "Component type cannot be null");
    Objects.requireNonNull(baseName, "Base name cannot be null");

    String prefix = prefixes.getOrDefault(componentType, "");
    String suffix = suffixes.getOrDefault(componentType, "");

    return prefix + baseName + suffix;
  }

  /**
   * Gets the suffix for a component type.
   *
   * @param componentType The component type
   * @return The suffix, or empty string if not defined
   */
  public String getSuffix(String componentType) {
    return suffixes.getOrDefault(componentType, "");
  }

  /**
   * Gets the prefix for a component type.
   *
   * @param componentType The component type
   * @return The prefix, or empty string if not defined
   */
  public String getPrefix(String componentType) {
    return prefixes.getOrDefault(componentType, "");
  }

  /**
   * Checks if conventions are defined for a component type.
   */
  public boolean hasConventionsFor(String componentType) {
    return suffixes.containsKey(componentType) || prefixes.containsKey(componentType);
  }
}
