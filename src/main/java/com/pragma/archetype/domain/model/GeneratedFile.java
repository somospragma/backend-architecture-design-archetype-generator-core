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

  /**
   * Creates a generic file with automatic type detection.
   */
  public static GeneratedFile create(Path path, String content) {
    String fileName = path.getFileName().toString().toLowerCase();

    if (fileName.endsWith(".java")) {
      return javaSource(path, content);
    } else if (fileName.endsWith(".kt") || fileName.endsWith(".kts")) {
      return new GeneratedFile(path, content, FileType.KOTLIN_SOURCE);
    } else if (fileName.equals("build.gradle.kts") || fileName.equals("build.gradle")) {
      return gradleBuild(path, content);
    } else if (fileName.equals("settings.gradle.kts") || fileName.equals("settings.gradle")) {
      return new GeneratedFile(path, content, FileType.GRADLE_SETTINGS);
    } else if (fileName.endsWith(".yml") || fileName.endsWith(".yaml")) {
      return yamlConfig(path, content);
    } else if (fileName.endsWith(".properties")) {
      return new GeneratedFile(path, content, FileType.PROPERTIES_CONFIG);
    } else if (fileName.endsWith(".md")) {
      return markdown(path, content);
    } else if (fileName.equals(".gitignore")) {
      return new GeneratedFile(path, content, FileType.GITIGNORE);
    } else {
      return new GeneratedFile(path, content, FileType.OTHER);
    }
  }
}
