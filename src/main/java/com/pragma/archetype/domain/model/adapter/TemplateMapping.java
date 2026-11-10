package com.pragma.archetype.domain.model.adapter;

import lombok.Builder;
import lombok.Value;

/**
 * Represents a mapping between a template file and its output configuration.
 * Used by AdapterMetadata to define which templates to process and where to
 * place generated files.
 */
@Value
@Builder
public class TemplateMapping {
  private static final String DEFAULT_OUTPUT_DIR = "src/main/java";

  /**
   * Source template file name (e.g., "Controller.java.ftl").
   */
  String source;

  /**
   * Target file name pattern with placeholders (e.g.,
   * "{adapterName}Controller.java").
   * Placeholders: {adapterName}, {entityName}, {useCaseName}
   */
  String target;

  /**
   * Output directory relative to module root (e.g., "src/main/java",
   * "src/test/java").
   * Defaults to "src/main/java" if not specified.
   */
  @Builder.Default
  String outputDir = DEFAULT_OUTPUT_DIR;

  /**
   * Resolves the target file name by replacing placeholders.
   *
   * @param adapterName The adapter name
   * @return The resolved target file name
   */
  public String resolveTarget(String adapterName) {
    return target.replace("{adapterName}", adapterName);
  }

  /**
   * Resolves the target file name with all placeholders.
   *
   * @param adapterName The adapter name
   * @param entityName  The entity name (optional)
   * @param useCaseName The use case name (optional)
   * @return The resolved target file name
   */
  public String resolveTarget(String adapterName, String entityName, String useCaseName) {
    var resolved = target.replace("{adapterName}", adapterName);
    if (entityName != null) {
      resolved = resolved.replace("{entityName}", entityName);
    }
    if (useCaseName != null) {
      resolved = resolved.replace("{useCaseName}", useCaseName);
    }
    return resolved;
  }
}
