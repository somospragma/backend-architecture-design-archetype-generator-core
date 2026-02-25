package com.pragma.archetype.domain.model.file;

/**
 * Represents the type of a generated file.
 */
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
