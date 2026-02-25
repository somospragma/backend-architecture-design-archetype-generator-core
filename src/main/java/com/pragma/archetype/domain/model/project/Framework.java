package com.pragma.archetype.domain.model.project;

/**
 * Supported frameworks for project generation.
 */
public enum Framework {
  /**
   * Spring Boot framework.
   */
  SPRING("spring"),

  /**
   * Quarkus framework (future support).
   */
  QUARKUS("quarkus");

  private final String value;

  Framework(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static Framework fromValue(String value) {
    for (Framework framework : values()) {
      if (framework.value.equalsIgnoreCase(value)) {
        return framework;
      }
    }
    throw new IllegalArgumentException("Unknown framework: " + value);
  }
}
