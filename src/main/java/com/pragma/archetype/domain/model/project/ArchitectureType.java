package com.pragma.archetype.domain.model.project;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Architecture type for project generation, identified by the name of its
 * directory under {@code architectures/} in the template repository.
 *
 * <p>
 * This is an open set: the template repository declares which architectures
 * exist (architectures/index.yml), so adding one never requires a plugin
 * release. The constants below are the architectures the plugin ships
 * knowledge of and are used as the fallback when the templates have no index.
 */
public final class ArchitectureType {

  private static final Pattern VALID_NAME = Pattern.compile("[a-z0-9]+(-[a-z0-9]+)*");

  /**
   * Hexagonal architecture with single module.
   * All code in one module with clear package separation.
   */
  public static final ArchitectureType HEXAGONAL_SINGLE = new ArchitectureType("hexagonal-single");

  /**
   * Hexagonal architecture with 3 modules: domain, application, infrastructure.
   */
  public static final ArchitectureType HEXAGONAL_MULTI = new ArchitectureType("hexagonal-multi");

  /**
   * Hexagonal architecture with granular modules.
   * Each component (model, ports, usecase, adapters) is a separate module.
   */
  public static final ArchitectureType HEXAGONAL_MULTI_GRANULAR = new ArchitectureType("hexagonal-multi-granular");

  /**
   * Onion architecture with single module.
   */
  public static final ArchitectureType ONION_SINGLE = new ArchitectureType("onion-single");

  /**
   * Onion architecture with multiple modules.
   */
  public static final ArchitectureType ONION_MULTI = new ArchitectureType("onion-multi");

  private static final ArchitectureType[] BUILT_IN = {
      HEXAGONAL_SINGLE, HEXAGONAL_MULTI, HEXAGONAL_MULTI_GRANULAR, ONION_SINGLE, ONION_MULTI
  };

  private final String value;

  private ArchitectureType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  /**
   * Creates an architecture type from its template directory name.
   * Accepts both {@code hexagonal-multi} and the legacy enum form
   * {@code HEXAGONAL_MULTI}. Built-in names return the shared constant.
   *
   * @throws IllegalArgumentException if the name is not a valid directory name
   */
  public static ArchitectureType fromValue(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Unknown architecture type: " + value);
    }
    String normalized = value.trim().toLowerCase().replace('_', '-');
    if (!VALID_NAME.matcher(normalized).matches()) {
      throw new IllegalArgumentException("Unknown architecture type: " + value);
    }
    for (ArchitectureType type : BUILT_IN) {
      if (type.value.equals(normalized)) {
        return type;
      }
    }
    return new ArchitectureType(normalized);
  }

  /**
   * Alias of {@link #fromValue(String)} kept for callers written against the
   * former enum.
   */
  public static ArchitectureType valueOf(String name) {
    return fromValue(name);
  }

  /**
   * Architectures the plugin ships knowledge of. The authoritative list is the
   * template repository's architectures/index.yml.
   */
  public static ArchitectureType[] values() {
    return BUILT_IN.clone();
  }

  /**
   * Legacy enum-style name, e.g. {@code HEXAGONAL_MULTI}.
   */
  public String name() {
    return value.toUpperCase().replace('-', '_');
  }

  @Override
  public boolean equals(Object other) {
    return this == other || (other instanceof ArchitectureType type && value.equals(type.value));
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return name();
  }
}
