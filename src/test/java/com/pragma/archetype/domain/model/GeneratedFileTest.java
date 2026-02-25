package com.pragma.archetype.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import com.pragma.archetype.domain.model.file.FileType;
import com.pragma.archetype.domain.model.file.GeneratedFile;

class GeneratedFileTest {

  @Test
  void shouldCreateJavaSourceFile() {
    // Given
    Path path = Path.of("src/main/java/com/test/User.java");
    String content = "public class User {}";

    // When
    GeneratedFile file = GeneratedFile.javaSource(path, content);

    // Then
    assertEquals(path, file.path());
    assertEquals(content, file.content());
    assertEquals(FileType.JAVA_SOURCE, file.type());
  }

  @Test
  void shouldCreateYamlConfigFile() {
    // Given
    Path path = Path.of("application.yml");
    String content = "server:\n  port: 8080";

    // When
    GeneratedFile file = GeneratedFile.yamlConfig(path, content);

    // Then
    assertEquals(path, file.path());
    assertEquals(content, file.content());
    assertEquals(FileType.YAML_CONFIG, file.type());
  }

  @Test
  void shouldCreateGradleBuildFile() {
    // Given
    Path path = Path.of("build.gradle");
    String content = "plugins { id 'java' }";

    // When
    GeneratedFile file = GeneratedFile.gradleBuild(path, content);

    // Then
    assertEquals(path, file.path());
    assertEquals(content, file.content());
    assertEquals(FileType.GRADLE_BUILD, file.type());
  }

  @Test
  void shouldCreateMarkdownFile() {
    // Given
    Path path = Path.of("README.md");
    String content = "# Project";

    // When
    GeneratedFile file = GeneratedFile.markdown(path, content);

    // Then
    assertEquals(path, file.path());
    assertEquals(content, file.content());
    assertEquals(FileType.MARKDOWN, file.type());
  }

  @Test
  void shouldAutoDetectFileType() {
    // Given
    Path javaPath = Path.of("User.java");
    Path yamlPath = Path.of("config.yml");
    Path mdPath = Path.of("README.md");

    // When
    GeneratedFile javaFile = GeneratedFile.create(javaPath, "class User {}");
    GeneratedFile yamlFile = GeneratedFile.create(yamlPath, "key: value");
    GeneratedFile mdFile = GeneratedFile.create(mdPath, "# Title");

    // Then
    assertEquals(FileType.JAVA_SOURCE, javaFile.type());
    assertEquals(FileType.YAML_CONFIG, yamlFile.type());
    assertEquals(FileType.MARKDOWN, mdFile.type());
  }

  @Test
  void shouldHandleEmptyContent() {
    // Given
    Path path = Path.of("Empty.java");
    String content = "";

    // When
    GeneratedFile file = GeneratedFile.javaSource(path, content);

    // Then
    assertEquals("", file.content());
  }

  @Test
  void shouldHandleComplexPath() {
    // Given
    Path path = Path.of("src/main/java/com/company/project/domain/model/User.java");
    String content = "public class User {}";

    // When
    GeneratedFile file = GeneratedFile.javaSource(path, content);

    // Then
    assertTrue(file.path().toString().contains("domain"));
    assertTrue(file.path().toString().contains("model"));
  }
}
