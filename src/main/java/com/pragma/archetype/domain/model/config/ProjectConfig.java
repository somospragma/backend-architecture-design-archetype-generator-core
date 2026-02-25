package com.pragma.archetype.domain.model.config;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

import com.pragma.archetype.domain.model.project.ArchitectureType;
import com.pragma.archetype.domain.model.project.Framework;
import com.pragma.archetype.domain.model.project.Paradigm;

import lombok.Builder;

/**
 * Configuration for a clean architecture project.
 * Uses Java 21 record for immutability and conciseness.
 */
@Builder
public record ProjectConfig(
    String name,
    String basePackage,
    ArchitectureType architecture,
    Paradigm paradigm,
    Framework framework,
    String pluginVersion,
    LocalDateTime createdAt,
    boolean adaptersAsModules,
    Map<String, String> dependencyOverrides) {

  private static final String DEFAULT_PLUGIN_VERSION = "0.1.0-SNAPSHOT";

  /**
   * Compact constructor with validation.
   */
  public ProjectConfig {
    Objects.requireNonNull(name, "Project name cannot be null");
    Objects.requireNonNull(basePackage, "Base package cannot be null");
    Objects.requireNonNull(architecture, "Architecture type cannot be null");
    Objects.requireNonNull(paradigm, "Paradigm cannot be null");
    Objects.requireNonNull(framework, "Framework cannot be null");

    if (pluginVersion == null || pluginVersion.isBlank()) {
      pluginVersion = DEFAULT_PLUGIN_VERSION;
    }

    if (name.isBlank()) {
      throw new IllegalArgumentException("Project name cannot be blank");
    }

    if (!isValidPackageName(basePackage)) {
      throw new IllegalArgumentException("Invalid package name: " + basePackage);
    }

    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
  }

  /**
   * Validates Java package name format.
   */
  private static boolean isValidPackageName(String packageName) {
    if (packageName == null || packageName.isBlank()) {
      return false;
    }

    String[] parts = packageName.split("\\.");
    for (String part : parts) {
      if (!part.matches("[a-z][a-z0-9_]*")) {
        return false;
      }
    }

    return true;
  }
}
