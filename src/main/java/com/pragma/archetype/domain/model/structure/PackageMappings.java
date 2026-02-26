package com.pragma.archetype.domain.model.structure;

import java.util.Collections;
import java.util.Map;

import lombok.Builder;
import lombok.Value;

/**
 * Configuration for directory-to-package name transformations.
 * Loaded from structure.yml packageMappings section.
 */
@Value
@Builder
public class PackageMappings {
  private static final String REMOVE_HYPHENS_RULE = "removeHyphens";
  private static final String SLASH_TO_DOT_RULE = "slashToDot";

  /**
   * Transformation rules to apply.
   * Key: rule name, Value: rule configuration
   */
  @Builder.Default
  Map<String, String> rules = Collections.emptyMap();

  /**
   * Custom replacements to apply.
   * Key: pattern to find, Value: replacement string
   */
  @Builder.Default
  Map<String, String> replacements = Collections.emptyMap();

  /**
   * Transforms a directory path to a Java package name.
   * Applies rules and replacements in order.
   */
  public String transform(String path) {
    if (path == null || path.isBlank()) {
      return path;
    }

    var result = path;

    // Apply standard rules
    if (rules.containsKey(REMOVE_HYPHENS_RULE)) {
      result = result.replace("-", "");
    }
    if (rules.containsKey(SLASH_TO_DOT_RULE)) {
      result = result.replace("/", ".");
    }

    // Apply custom replacements
    for (var entry : replacements.entrySet()) {
      result = result.replace(entry.getKey(), entry.getValue());
    }

    return result;
  }
}
