package com.pragma.archetype.domain.model.project;

/**
 * Programming paradigm for the generated project.
 */
public enum Paradigm {
  /**
   * Reactive programming with non-blocking I/O.
   * Uses Mono/Flux (Spring) or Uni/Multi (Quarkus).
   */
  REACTIVE("reactive"),

  /**
   * Imperative programming with blocking I/O.
   * Traditional synchronous approach.
   */
  IMPERATIVE("imperative");

  private final String value;

  Paradigm(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static Paradigm fromValue(String value) {
    for (Paradigm paradigm : values()) {
      if (paradigm.value.equalsIgnoreCase(value)) {
        return paradigm;
      }
    }
    throw new IllegalArgumentException("Unknown paradigm: " + value);
  }
}
