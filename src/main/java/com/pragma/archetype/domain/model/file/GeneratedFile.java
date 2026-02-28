package com.pragma.archetype.domain.model.file;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Represents a file to be generated.
 * Uses Java 21 record for immutability.
 */
public record GeneratedFile(
    Path path,
    String content,
    FileType type,
    boolean executable) {

  public GeneratedFile {
    Objects.requireNonNull(path, "Path cannot be null");
    Objects.requireNonNull(content, "Content cannot be null");
    Objects.requireNonNull(type, "File type cannot be null");
  }

  /**
   * Creates a Java source file.
   */
  public static GeneratedFile javaSource(Path path, String content) {
    return new GeneratedFile(path, content, FileType.JAVA_SOURCE, false);
  }

  /**
   * Creates a Gradle build file.
   */
  public static GeneratedFile gradleBuild(Path path, String content) {
    return new GeneratedFile(path, content, FileType.GRADLE_BUILD, false);
  }

  /**
   * Creates a YAML configuration file.
   */
  public static GeneratedFile yamlConfig(Path path, String content) {
    return new GeneratedFile(path, content, FileType.YAML_CONFIG, false);
  }

  /**
   * Creates a markdown file.
   */
  public static GeneratedFile markdown(Path path, String content) {
    return new GeneratedFile(path, content, FileType.MARKDOWN, false);
  }

  /**
   * Creates a generic file with automatic type detection.
   */
  public static GeneratedFile create(Path path, String content) {
    return create(path, content, false);
  }

  /**
   * Creates a generic file with automatic type detection and executable flag.
   */
  public static GeneratedFile create(Path path, String content, boolean executable) {
    String fileName = path.getFileName().toString().toLowerCase();

    if (fileName.endsWith(".java")) {
      return javaSource(path, content);
    } else if (fileName.endsWith(".kt") || fileName.endsWith(".kts")) {
      return new GeneratedFile(path, content, FileType.KOTLIN_SOURCE, executable);
    } else if (fileName.equals("build.gradle.kts") || fileName.equals("build.gradle")) {
      return gradleBuild(path, content);
    } else if (fileName.equals("settings.gradle.kts") || fileName.equals("settings.gradle")) {
      return new GeneratedFile(path, content, FileType.GRADLE_SETTINGS, executable);
    } else if (fileName.endsWith(".yml") || fileName.endsWith(".yaml")) {
      return yamlConfig(path, content);
    } else if (fileName.endsWith(".properties")) {
      return new GeneratedFile(path, content, FileType.PROPERTIES_CONFIG, executable);
    } else if (fileName.endsWith(".md")) {
      return markdown(path, content);
    } else if (fileName.equals(".gitignore")) {
      return new GeneratedFile(path, content, FileType.GITIGNORE, executable);
    } else {
      return new GeneratedFile(path, content, FileType.OTHER, executable);
    }
  }
}
