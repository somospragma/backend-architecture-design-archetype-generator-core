package com.pragma.archetype.domain.model.error;

public final class StructureErrorMessages {
  // Configuration errors
  public static final String STRUCTURE_YML_NOT_FOUND = "structure.yml not found for architecture: %s";

  public static final String METADATA_YML_NOT_FOUND = "metadata.yml not found for adapter: %s";
  public static final String INVALID_YAML_SYNTAX = "Invalid YAML syntax in file %s: %s";
  // Resolution errors
  public static final String UNRESOLVED_VARIABLES = "Template path contains unresolved variables: %s";

  public static final String MODULE_NOT_FOUND = "Module '%s' not found in structure.yml";
  public static final String PACKAGE_TYPE_NOT_FOUND = "Package type '%s' not found in module '%s'";
  public static final String ENTITY_MODULE_NOT_CONFIGURED = "Entity module not configured in structure.yml";
  public static final String USE_CASE_MODULE_NOT_CONFIGURED = "Use case module not configured in structure.yml";
  public static final String TEMPLATE_PATH_RESOLUTION_FAILED = "Failed to resolve template path";
  public static final String UNKNOWN_TEMPLATE_TYPE = "Unknown template type";
  // Validation errors
  public static final String MODULE_NAME_EMPTY = "Module name cannot be empty";

  public static final String MODULE_PATH_EMPTY = "Module path cannot be empty";
  public static final String ADAPTER_NAME_EMPTY = "Adapter name cannot be empty";
  public static final String ADAPTER_DIRECTION_NULL = "Adapter direction cannot be null";

  private StructureErrorMessages() {
  }
}
