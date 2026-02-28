package com.pragma.archetype.domain.model.structure;

import lombok.Builder;
import lombok.Value;

/**
 * Represents a static file to be copied from template to project.
 * Loaded from structure.yml staticFiles section.
 * 
 * Static files are processed as FreeMarker templates and copied to the
 * specified destination.
 * This allows architectures to define additional files (Dockerfile, tests,
 * etc.) without
 * hardcoding them in the generator code.
 */
@Value
@Builder
public class StaticFile {
  /**
   * Source path relative to architecture template directory.
   * Example: "project/Dockerfile.ftl"
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
    return destination.replace("{basePackage}", packagePath);
  }
}
