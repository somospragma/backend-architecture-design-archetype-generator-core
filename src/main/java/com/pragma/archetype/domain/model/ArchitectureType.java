package com.pragma.archetype.domain.model;

/**
 * Supported architecture types for project generation.
 * Each type defines a different structural organization of the codebase.
 */
public enum ArchitectureType {
  /**
   * Hexagonal architecture with single module.
   * All code in one module with clear package separation.
   */
  HEXAGONAL_SINGLE("hexagonal-single"),

  /**
   * Hexagonal architecture with 3 modules: domain, application, infrastructure.
   */
  HEXAGONAL_MULTI("hexagonal-multi"),

  /**
   * Hexagonal architecture with granular modules.
   * Each component (model, ports, usecase, adapters) is a separate module.
   */
  HEXAGONAL_MULTI_GRANULAR("hexagonal-multi-granular"),

  /**
   * Onion architecture with single module.
   */
  ONION_SINGLE("onion-single"),

  /**
   * Onion architecture with multiple modules.
   */
  ONION_MULTI("onion-multi");

  private final String value;

  ArchitectureType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static ArchitectureType fromValue(String value) {
    for (ArchitectureType type : values()) {
      if (type.value.equalsIgnoreCase(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown architecture type: " + value);
  }

  public boolean isMultiModule() {
    return this == HEXAGONAL_MULTI ||
        this == HEXAGONAL_MULTI_GRANULAR ||
        this == ONION_MULTI;
  }
}
