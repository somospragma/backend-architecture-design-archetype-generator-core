package com.pragma.archetype.domain.model.structure;

import lombok.Builder;
import lombok.Value;

/**
 * Represents a static file to be copied from template to project.
 * Loaded from structure.yml staticFiles section.
 * 
 * Static files can be processed as FreeMarker templates or copied directly
 * (for binary files like JARs or executable scripts).
 * This allows architectures to define additional files (Dockerfile, tests,
 * wrapper, etc.) without hardcoding them in the generator code.
 */
@Value
@Builder
public class StaticFile {
  /**
   * Source path relative to architecture template directory, or to the
   * templates root when it starts with "/" (files shared by several
   * architectures). Supports {framework} and {paradigm} placeholders.
   * Example: "project/Dockerfile.ftl" or
   * "/frameworks/{framework}/{paradigm}/tests/architecture/ParadigmRulesTest.java.ftl"
   */
  String source;

  /**
   * Destination path relative to project root.
   * Supports {basePackage} placeholder which will be replaced with the actual
   * package path.
   * Example: "Dockerfile" or
   * "src/test/java/{basePackage}/architecture/ArchitectureTest.java"
   */
  String destination;

  /**
   * Optional description of what this file does.
   */
  String description;

  /**
   * Whether to process this file as a FreeMarker template.
   * Default: true
   * Set to false for binary files (JARs, executables) or files that should be
   * copied as-is.
   */
  @Builder.Default
  boolean processAsTemplate = true;

  /**
   * Whether this file is a binary file (JAR, executable, etc.).
   * Binary files are copied directly from the filesystem instead of being read as
   * text.
   * Default: false
   */
  @Builder.Default
  boolean binary = false;

  /**
   * Whether to set executable permissions on this file (Unix/Linux/Mac).
   * Useful for shell scripts like gradlew.
   * Default: false
   */
  @Builder.Default
  boolean executable = false;

  /**
   * Resolves the destination path by replacing placeholders.
   * 
   * @param basePackage The base package name (e.g., "com.example.app")
   * @return The resolved destination path
   */
  public String resolveDestination(String basePackage) {
    if (destination == null) {
      return null;
    }

    // Replace {basePackage} with actual package path
    String packagePath = basePackage.replace('.', '/');
    String resolved = destination.replace("{basePackage}", packagePath);

    // Replace {configPackagePath} with infrastructure/config package path
    String configPackagePath = "src/main/java/" + packagePath + "/infrastructure/config";
    resolved = resolved.replace("{configPackagePath}", configPackagePath);

    return resolved;
  }

  /**
   * Template path of the source inside the templates repository.
   *
   * @param architecture the architecture whose directory relative sources live in
   * @param framework    framework of the project (e.g. "spring")
   * @param paradigm     paradigm of the project (e.g. "reactive")
   */
  public String resolveSource(String architecture, String framework, String paradigm) {
    String resolved = source
        .replace("{framework}", framework)
        .replace("{paradigm}", paradigm);
    return resolved.startsWith("/")
        ? resolved.substring(1)
        : "architectures/" + architecture + "/" + resolved;
  }
}
