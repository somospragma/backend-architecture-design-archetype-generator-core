package com.pragma.archetype.domain.model;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Represents a file to be generated.
 * Uses Java 21 record for immutability.
 */
public record GeneratedFile(
    Path path,
    String content,
    FileType type) {

  public GeneratedFile {
    Objects.requireNonNull(path, "Path cannot be null");
    Objects.requireNonNull(content, "Content cannot be null");
    Objects.requireNonNull(type, "File type cannot be null");
  }

  public enum FileType {
    JAVA_SOURCE,
    KOTLIN_SOURCE,
    GRADLE_BUILD,
    GRADLE_SETTINGS,
    YAML_CONFIG,
    PROPERTIES_CONFIG,
    MARKDOWN,
    GITIGNORE,
    OTHER
  }

  /**
   * Creates a Java source file.
   */
  public static GeneratedFile javaSource(Path path, String content) {
    return new GeneratedFile(path, content, FileType.JAVA_SOURCE);
  }

  /**
   * Creates a Gradle build file.
   */
  public static GeneratedFile gradleBuild(Path path, String content) {
    return new GeneratedFile(path, content, FileType.GRADLE_BUILD);
  }

  /**
   * Creates a YAML configuration file.
   */
  public static GeneratedFile yamlConfig(Path path, String content) {
    return new GeneratedFile(path, content, FileType.YAML_CONFIG);
  }

  /**
   * Creates a markdown file.
   */
  public static GeneratedFile markdown(Path path, String content) {
    return new GeneratedFile(path, content, FileType.MARKDOWN);
  }
}
