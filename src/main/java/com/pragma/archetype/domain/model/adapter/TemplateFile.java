package com.pragma.archetype.domain.model.adapter;

import lombok.Builder;
import lombok.Value;

/**
 * Represents a template file discovered in an adapter directory.
 * Used for automatic template discovery from adapter template directories.
 */
@Value
@Builder
public class TemplateFile {
  /**
   * Template file name (e.g., "Controller.java.ftl").
   */
  String name;

  /**
   * Template type: main, test, config, support.
   * - main: Primary template for the adapter
   * - test: Test template
   * - config: Configuration template
   * - support: Supporting files (exception handlers, etc.)
   */
  String type;

  /**
   * Destination file name pattern (e.g., "{name}Controller.java").
   * Placeholders like {name} will be replaced during generation.
   */
  String destination;
}
