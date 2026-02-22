package com.pragma.archetype.domain.model;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * Configuration for a clean architecture project.
 * Uses Java 21 record for immutability and conciseness.
 */
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

  /**
   * Compact constructor with validation.
   */
  public ProjectConfig {
    Objects.requireNonNull(name, "Project name cannot be null");
    Objects.requireNonNull(basePackage, "Base package cannot be null");
    Objects.requireNonNull(architecture, "Architecture type cannot be null");
    Objects.requireNonNull(paradigm, "Paradigm cannot be null");
    Objects.requireNonNull(framework, "Framework cannot be null");
    Objects.requireNonNull(pluginVersion, "Plugin version cannot be null");

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

  /**
   * Builder for convenient construction.
   */
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String name;
    private String basePackage;
    private ArchitectureType architecture;
    private Paradigm paradigm;
    private Framework framework;
    private String pluginVersion = "0.1.0-SNAPSHOT";
    private LocalDateTime createdAt = LocalDateTime.now();
    private boolean adaptersAsModules = false;
    private Map<String, String> dependencyOverrides = Map.of();

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder basePackage(String basePackage) {
      this.basePackage = basePackage;
      return this;
    }

    public Builder architecture(ArchitectureType architecture) {
      this.architecture = architecture;
      return this;
    }

    public Builder paradigm(Paradigm paradigm) {
      this.paradigm = paradigm;
      return this;
    }

    public Builder framework(Framework framework) {
      this.framework = framework;
      return this;
    }

    public Builder pluginVersion(String pluginVersion) {
      this.pluginVersion = pluginVersion;
      return this;
    }

    public Builder createdAt(LocalDateTime createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    public Builder adaptersAsModules(boolean adaptersAsModules) {
      this.adaptersAsModules = adaptersAsModules;
      return this;
    }

    public Builder dependencyOverrides(Map<String, String> dependencyOverrides) {
      this.dependencyOverrides = dependencyOverrides != null ? dependencyOverrides : Map.of();
      return this;
    }

    public ProjectConfig build() {
      return new ProjectConfig(
          name,
          basePackage,
          architecture,
          paradigm,
          framework,
          pluginVersion,
          createdAt,
          adaptersAsModules,
          dependencyOverrides);
    }
  }
}
