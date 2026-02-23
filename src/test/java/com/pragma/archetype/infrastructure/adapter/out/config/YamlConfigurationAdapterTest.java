package com.pragma.archetype.infrastructure.adapter.out.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.pragma.archetype.domain.model.TemplateConfig;
import com.pragma.archetype.domain.model.TemplateMode;

@DisplayName("YamlConfigurationAdapter Tests")
class YamlConfigurationAdapterTest {

  private final YamlConfigurationAdapter adapter = new YamlConfigurationAdapter();

  @Test
  @DisplayName("Should read template configuration with all properties")
  void shouldReadTemplateConfigurationWithAllProperties(@TempDir Path tempDir) throws IOException {
    // Given: A .cleanarch.yml file with complete templates section
    String yamlContent = """
        project:
          name: test-project
          basePackage: com.test
          pluginVersion: 1.0.0
          createdAt: 2024-01-15T10:30:00

        architecture:
          type: hexagonal-single
          paradigm: reactive
          framework: spring
          adaptersAsModules: false

        templates:
          mode: developer
          repository: https://github.com/test/templates
          branch: feature/test-branch
          version: 1.0.0
          localPath: /path/to/local/templates
          cache: false
        """;

    Files.writeString(tempDir.resolve(".cleanarch.yml"), yamlContent);

    // When: Reading template configuration
    TemplateConfig config = adapter.readTemplateConfiguration(tempDir);

    // Then: All properties should be parsed correctly
    assertNotNull(config);
    assertEquals(TemplateMode.DEVELOPER, config.mode());
    assertEquals("https://github.com/test/templates", config.repository());
    assertEquals("feature/test-branch", config.branch());
    assertEquals("1.0.0", config.version());
    assertEquals("/path/to/local/templates", config.localPath());
    assertFalse(config.cache());
  }

  @Test
  @DisplayName("Should read template configuration with minimal properties")
  void shouldReadTemplateConfigurationWithMinimalProperties(@TempDir Path tempDir) throws IOException {
    // Given: A .cleanarch.yml file with minimal templates section
    String yamlContent = """
        project:
          name: test-project
          basePackage: com.test
          pluginVersion: 1.0.0
          createdAt: 2024-01-15T10:30:00

        architecture:
          type: hexagonal-single
          paradigm: reactive
          framework: spring
          adaptersAsModules: false

        templates:
          repository: https://github.com/test/templates
        """;

    Files.writeString(tempDir.resolve(".cleanarch.yml"), yamlContent);

    // When: Reading template configuration
    TemplateConfig config = adapter.readTemplateConfiguration(tempDir);

    // Then: Should use defaults for missing properties
    assertNotNull(config);
    assertEquals(TemplateMode.PRODUCTION, config.mode());
    assertEquals("https://github.com/test/templates", config.repository());
    assertEquals("main", config.branch());
    assertNull(config.version());
    assertNull(config.localPath());
    assertTrue(config.cache());
  }

  @Test
  @DisplayName("Should return default config when templates section is missing")
  void shouldReturnDefaultConfigWhenTemplatesSectionMissing(@TempDir Path tempDir) throws IOException {
    // Given: A .cleanarch.yml file without templates section
    String yamlContent = """
        project:
          name: test-project
          basePackage: com.test
          pluginVersion: 1.0.0
          createdAt: 2024-01-15T10:30:00

        architecture:
          type: hexagonal-single
          paradigm: reactive
          framework: spring
          adaptersAsModules: false
        """;

    Files.writeString(tempDir.resolve(".cleanarch.yml"), yamlContent);

    // When: Reading template configuration
    TemplateConfig config = adapter.readTemplateConfiguration(tempDir);

    // Then: Should return default configuration
    assertNotNull(config);
    assertEquals(TemplateMode.PRODUCTION, config.mode());
    assertEquals("https://github.com/somospragma/backend-architecture-design-archetype-generator-templates",
        config.repository());
    assertEquals("main", config.branch());
    assertNull(config.version());
    assertNull(config.localPath());
    assertTrue(config.cache());
  }

  @Test
  @DisplayName("Should return default config when config file does not exist")
  void shouldReturnDefaultConfigWhenConfigFileDoesNotExist(@TempDir Path tempDir) {
    // Given: No .cleanarch.yml file exists

    // When: Reading template configuration
    TemplateConfig config = adapter.readTemplateConfiguration(tempDir);

    // Then: Should return default configuration
    assertNotNull(config);
    assertEquals(TemplateMode.PRODUCTION, config.mode());
    assertEquals("https://github.com/somospragma/backend-architecture-design-archetype-generator-templates",
        config.repository());
    assertEquals("main", config.branch());
    assertNull(config.version());
    assertNull(config.localPath());
    assertTrue(config.cache());
  }

  @Test
  @DisplayName("Should parse localPath property correctly")
  void shouldParseLocalPathPropertyCorrectly(@TempDir Path tempDir) throws IOException {
    // Given: A .cleanarch.yml file with localPath
    String yamlContent = """
        project:
          name: test-project
          basePackage: com.test
          pluginVersion: 1.0.0
          createdAt: 2024-01-15T10:30:00

        architecture:
          type: hexagonal-single
          paradigm: reactive
          framework: spring
          adaptersAsModules: false

        templates:
          mode: developer
          localPath: ../backend-architecture-design-archetype-generator-templates
        """;

    Files.writeString(tempDir.resolve(".cleanarch.yml"), yamlContent);

    // When: Reading template configuration
    TemplateConfig config = adapter.readTemplateConfiguration(tempDir);

    // Then: localPath should be parsed correctly
    assertNotNull(config);
    assertEquals("../backend-architecture-design-archetype-generator-templates", config.localPath());
    assertTrue(config.isLocalMode());
  }

  @Test
  @DisplayName("Should parse branch property correctly")
  void shouldParseBranchPropertyCorrectly(@TempDir Path tempDir) throws IOException {
    // Given: A .cleanarch.yml file with custom branch
    String yamlContent = """
        project:
          name: test-project
          basePackage: com.test
          pluginVersion: 1.0.0
          createdAt: 2024-01-15T10:30:00

        architecture:
          type: hexagonal-single
          paradigm: reactive
          framework: spring
          adaptersAsModules: false

        templates:
          repository: https://github.com/test/templates
          branch: feature/init-templates
        """;

    Files.writeString(tempDir.resolve(".cleanarch.yml"), yamlContent);

    // When: Reading template configuration
    TemplateConfig config = adapter.readTemplateConfiguration(tempDir);

    // Then: branch should be parsed correctly
    assertNotNull(config);
    assertEquals("feature/init-templates", config.branch());
  }

  @Test
  @DisplayName("Should parse cache property correctly")
  void shouldParseCachePropertyCorrectly(@TempDir Path tempDir) throws IOException {
    // Given: A .cleanarch.yml file with cache disabled
    String yamlContent = """
        project:
          name: test-project
          basePackage: com.test
          pluginVersion: 1.0.0
          createdAt: 2024-01-15T10:30:00

        architecture:
          type: hexagonal-single
          paradigm: reactive
          framework: spring
          adaptersAsModules: false

        templates:
          repository: https://github.com/test/templates
          cache: false
        """;

    Files.writeString(tempDir.resolve(".cleanarch.yml"), yamlContent);

    // When: Reading template configuration
    TemplateConfig config = adapter.readTemplateConfiguration(tempDir);

    // Then: cache should be false
    assertNotNull(config);
    assertFalse(config.cache());
  }

  @Test
  @DisplayName("Should read YAML file and return map")
  void shouldReadYamlFileAndReturnMap(@TempDir Path tempDir) throws IOException {
    // Given: A YAML file with properties
    String yamlContent = """
        spring:
          data:
            mongodb:
              uri: mongodb://localhost:27017/testdb
              database: testdb
        """;

    Path yamlFile = tempDir.resolve("application.yml");
    Files.writeString(yamlFile, yamlContent);

    // When: Reading YAML file
    var result = adapter.readYaml(yamlFile);

    // Then: Should return map with properties
    assertNotNull(result);
    assertTrue(result.containsKey("spring"));
  }

  @Test
  @DisplayName("Should return empty map when YAML file does not exist")
  void shouldReturnEmptyMapWhenYamlFileDoesNotExist(@TempDir Path tempDir) {
    // Given: No YAML file exists
    Path yamlFile = tempDir.resolve("nonexistent.yml");

    // When: Reading YAML file
    var result = adapter.readYaml(yamlFile);

    // Then: Should return empty map
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("Should write YAML file with 2-space indentation")
  void shouldWriteYamlFileWithTwoSpaceIndentation(@TempDir Path tempDir) throws IOException {
    // Given: A map to write
    var data = new java.util.LinkedHashMap<String, Object>();
    var spring = new java.util.LinkedHashMap<String, Object>();
    var dataSection = new java.util.LinkedHashMap<String, Object>();
    var mongodb = new java.util.LinkedHashMap<String, Object>();
    mongodb.put("uri", "mongodb://localhost:27017/testdb");
    mongodb.put("database", "testdb");
    dataSection.put("mongodb", mongodb);
    spring.put("data", dataSection);
    data.put("spring", spring);

    Path yamlFile = tempDir.resolve("application.yml");

    // When: Writing YAML file
    adapter.writeYaml(yamlFile, data);

    // Then: File should exist with proper indentation
    assertTrue(Files.exists(yamlFile));
    String content = Files.readString(yamlFile);
    assertTrue(content.contains("spring:"));
    assertTrue(content.contains("  data:"));
    assertTrue(content.contains("    mongodb:"));
  }

  @Test
  @DisplayName("Should merge YAML preserving existing values")
  void shouldMergeYamlPreservingExistingValues() {
    // Given: Base YAML with existing properties
    var base = new java.util.LinkedHashMap<String, Object>();
    var spring = new java.util.LinkedHashMap<String, Object>();
    spring.put("application", new java.util.LinkedHashMap<>(java.util.Map.of("name", "existing-app")));
    base.put("spring", spring);

    // And: Overlay YAML with new properties
    var overlay = new java.util.LinkedHashMap<String, Object>();
    var overlaySpring = new java.util.LinkedHashMap<String, Object>();
    var dataSection = new java.util.LinkedHashMap<String, Object>();
    dataSection.put("mongodb",
        new java.util.LinkedHashMap<>(java.util.Map.of("uri", "mongodb://localhost:27017/testdb")));
    overlaySpring.put("data", dataSection);
    overlay.put("spring", overlaySpring);

    // When: Merging YAML
    var result = adapter.mergeYaml(base, overlay);

    // Then: Should preserve existing values and add new ones
    assertNotNull(result);
    assertTrue(result.containsKey("spring"));
    @SuppressWarnings("unchecked")
    var resultSpring = (java.util.Map<String, Object>) result.get("spring");
    assertTrue(resultSpring.containsKey("application"));
    assertTrue(resultSpring.containsKey("data"));
  }

  @Test
  @DisplayName("Should merge YAML file and add security comments for sensitive properties")
  void shouldMergeYamlFileAndAddSecurityComments(@TempDir Path tempDir) throws IOException {
    // Given: Existing YAML file
    String existingContent = """
        spring:
          application:
            name: test-app
        """;

    Path yamlFile = tempDir.resolve("application.yml");
    Files.writeString(yamlFile, existingContent);

    // And: Overlay with sensitive properties
    var overlay = new java.util.LinkedHashMap<String, Object>();
    var spring = new java.util.LinkedHashMap<String, Object>();
    var dataSection = new java.util.LinkedHashMap<String, Object>();
    var mongodb = new java.util.LinkedHashMap<String, Object>();
    mongodb.put("uri", "mongodb://localhost:27017/testdb");
    mongodb.put("password", "secret123");
    dataSection.put("mongodb", mongodb);
    spring.put("data", dataSection);
    overlay.put("spring", spring);

    // When: Merging YAML file
    adapter.mergeYamlFile(yamlFile, overlay);

    // Then: File should contain security warning
    String content = Files.readString(yamlFile);
    assertTrue(content.contains("WARNING"));
    assertTrue(content.contains("Do not store credentials"));
    assertTrue(content.contains("test-app"));
  }

  @Test
  @DisplayName("Should merge YAML without conflicts when keys don't overlap")
  void shouldMergeYamlWithoutConflictsWhenKeysDoNotOverlap() {
    // Given: Base YAML
    var base = new java.util.LinkedHashMap<String, Object>();
    base.put("server", new java.util.LinkedHashMap<>(java.util.Map.of("port", 8080)));

    // And: Overlay YAML with different keys
    var overlay = new java.util.LinkedHashMap<String, Object>();
    overlay.put("logging", new java.util.LinkedHashMap<>(java.util.Map.of("level", "INFO")));

    // When: Merging YAML
    var result = adapter.mergeYaml(base, overlay);

    // Then: Should contain both sections
    assertNotNull(result);
    assertTrue(result.containsKey("server"));
    assertTrue(result.containsKey("logging"));
  }

  @Test
  @DisplayName("Should handle null base map in merge")
  void shouldHandleNullBaseMapInMerge() {
    // Given: Null base and valid overlay
    var overlay = new java.util.LinkedHashMap<String, Object>();
    overlay.put("key", "value");

    // When: Merging YAML
    var result = adapter.mergeYaml(null, overlay);

    // Then: Should return overlay content
    assertNotNull(result);
    assertTrue(result.containsKey("key"));
  }

  @Test
  @DisplayName("Should handle null overlay map in merge")
  void shouldHandleNullOverlayMapInMerge() {
    // Given: Valid base and null overlay
    var base = new java.util.LinkedHashMap<String, Object>();
    base.put("key", "value");

    // When: Merging YAML
    var result = adapter.mergeYaml(base, null);

    // Then: Should return base content
    assertNotNull(result);
    assertTrue(result.containsKey("key"));
  }

  @Test
  @DisplayName("Should create new file when merging into non-existent file")
  void shouldCreateNewFileWhenMergingIntoNonExistentFile(@TempDir Path tempDir) throws IOException {
    // Given: Non-existent YAML file
    Path yamlFile = tempDir.resolve("new-application.yml");

    // And: Overlay with properties
    var overlay = new java.util.LinkedHashMap<String, Object>();
    overlay.put("spring", new java.util.LinkedHashMap<>(java.util.Map.of("application",
        new java.util.LinkedHashMap<>(java.util.Map.of("name", "new-app")))));

    // When: Merging YAML file
    adapter.mergeYamlFile(yamlFile, overlay);

    // Then: File should be created with content
    assertTrue(Files.exists(yamlFile));
    String content = Files.readString(yamlFile);
    assertTrue(content.contains("new-app"));
  }

  @Test
  @DisplayName("Should write YAML atomically without leaving temporary files")
  void shouldWriteYamlAtomicallyWithoutLeavingTempFiles(@TempDir Path tempDir) throws IOException {
    // Given: A YAML file path
    Path yamlFile = tempDir.resolve("test.yml");

    // And: Some data to write
    var data = new java.util.LinkedHashMap<String, Object>();
    data.put("key", "value");
    data.put("number", 42);

    // When: Writing YAML
    adapter.writeYaml(yamlFile, data);

    // Then: File should exist with correct content
    assertTrue(Files.exists(yamlFile));
    String content = Files.readString(yamlFile);
    assertTrue(content.contains("key: value"));
    assertTrue(content.contains("number: 42"));

    // And: No temporary files should remain
    Path tempFile = yamlFile.resolveSibling(yamlFile.getFileName() + ".tmp");
    assertFalse(Files.exists(tempFile), "Temporary file should not exist after atomic write");
  }

  @Test
  @DisplayName("Should merge YAML atomically without leaving temporary files")
  void shouldMergeYamlAtomicallyWithoutLeavingTempFiles(@TempDir Path tempDir) throws IOException {
    // Given: An existing YAML file
    Path yamlFile = tempDir.resolve("application.yml");
    String existingContent = """
        spring:
          application:
            name: existing-app
        """;
    Files.writeString(yamlFile, existingContent);

    // And: Overlay with new properties
    var overlay = new java.util.LinkedHashMap<String, Object>();
    overlay.put("server", new java.util.LinkedHashMap<>(java.util.Map.of("port", 8080)));

    // When: Merging YAML file
    adapter.mergeYamlFile(yamlFile, overlay);

    // Then: File should contain both old and new properties
    assertTrue(Files.exists(yamlFile));
    String content = Files.readString(yamlFile);
    assertTrue(content.contains("existing-app"));
    assertTrue(content.contains("8080"));

    // And: No temporary files should remain
    Path tempFile = yamlFile.resolveSibling(yamlFile.getFileName() + ".tmp");
    assertFalse(Files.exists(tempFile), "Temporary file should not exist after atomic merge");
  }
}
