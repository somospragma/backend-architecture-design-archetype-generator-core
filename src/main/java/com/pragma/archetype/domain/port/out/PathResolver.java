package com.pragma.archetype.domain.port.out;

import java.nio.file.Path;
import java.util.Map;

import com.pragma.archetype.domain.model.ValidationResult;
import com.pragma.archetype.domain.model.project.ArchitectureType;

/**
 * Port for resolving paths in different architecture types.
 * Handles placeholder substitution and path validation.
 */
public interface PathResolver {

  /**
   * Resolves the path for an adapter based on architecture configuration.
   *
   * @param architecture The architecture type
   * @param adapterType  The adapter type (driven or driving)
   * @param name         The adapter name
   * @param context      Additional context for placeholder substitution
   *                     (basePackage, module, etc.)
   * @return The resolved path
   */
  Path resolveAdapterPath(
      ArchitectureType architecture,
      String adapterType,
      String name,
      Map<String, String> context);

  /**
   * Validates that a path respects layer dependency rules.
   *
   * @param path         The path to validate
   * @param architecture The architecture type
   * @return ValidationResult indicating if path is valid
   */
  ValidationResult validatePath(Path path, ArchitectureType architecture);

  /**
   * Substitutes placeholders in a path template with actual values.
   *
   * @param template The path template with placeholders
   * @param context  Map of placeholder names to values
   * @return The path with placeholders substituted
   */
  String substitutePlaceholders(String template, Map<String, String> context);
}
