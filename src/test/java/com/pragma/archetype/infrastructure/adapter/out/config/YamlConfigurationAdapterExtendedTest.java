package com.pragma.archetype.infrastructure.adapter.out.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.pragma.archetype.domain.model.ArchitectureType;
import com.pragma.archetype.domain.model.Framework;
import com.pragma.archetype.domain.model.Paradigm;
import com.pragma.archetype.domain.model.ProjectConfig;

class YamlConfigurationAdapterExtendedTest {

  @TempDir
  Path tempDir;

  private YamlConfigurationAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new YamlConfigurationAdapter();
  }

  @Test
  void testWriteConfiguration() throws Exception {
    // Given
    ProjectConfig config = ProjectConfig.builder()
        .name("test-project")
        .basePackage("com.test")
        .architecture(ArchitectureType.HEXAGONAL_SINGLE)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .pluginVersion("1.0.0")
        .createdAt(LocalDateTime.now())
        .adaptersAsModules(false)
        .build();

    // When
    adapter.writeConfiguration(tempDir, config);

    // Then
    Path configFile = tempDir.resolve(".cleanarch.yml");
    assertTrue(Files.exists(configFile));

    String content = Files.readString(configFile);
    assertTrue(content.contains("name: test-project"));
    assertTrue(content.contains("basePackage: com.test"));
    assertTrue(content.contains("type: hexagonal-single"));
    assertTrue(content.contains("paradigm: reactive"));
    assertTrue(content.contains("framework: spring"));
  }

  @Test
  void testWriteConfiguration_withDependencyOverrides() throws Exception {
    // Given
    Map<String, String> overrides = new LinkedHashMap<>();
    overrides.put("spring-boot", "3.2.0");
    overrides.put("lombok", "1.18.30");

    ProjectConfig config = ProjectConfig.builder()
        .name("test-project")
        .basePackage("com.test")
        .architecture(ArchitectureType.HEXAGONAL_MULTI)
        .paradigm(Paradigm.IMPERATIVE)
        .framework(Framework.QUARKUS)
        .pluginVersion("1.0.0")
        .createdAt(LocalDateTime.now())
        .adaptersAsModules(true)
        .dependencyOverrides(overrides)
        .build();

    // When
    adapter.writeConfiguration(tempDir, config);

    // Then
    Path configFile = tempDir.resolve(".cleanarch.yml");
    String content = Files.readString(configFile);
    assertTrue(content.contains("dependencyOverrides"));
    assertTrue(content.contains("spring-boot"));
    assertTrue(content.contains("3.2.0"));
  }

  @Test
  void testDeleteConfiguration() throws Exception {
    // Given - create a config file
    Path configFile = tempDir.resolve(".cleanarch.yml");
    Files.writeString(configFile, "test: value");
    assertTrue(Files.exists(configFile));

    // When
    adapter.deleteConfiguration(tempDir);

    // Then
    assertFalse(Files.exists(configFile));
  }

  @Test
  void testDeleteConfiguration_nonExistentFile() {
    // When/Then - should not throw exception
    assertDoesNotThrow(() -> adapter.deleteConfiguration(tempDir));
  }

  @Test
  void testWriteYaml() throws Exception {
    // Given
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("key1", "value1");
    data.put("key2", 123);

    Map<String, Object> nested = new LinkedHashMap<>();
    nested.put("nestedKey", "nestedValue");
    data.put("nested", nested);

    Path yamlFile = tempDir.resolve("test.yml");

    // When
    adapter.writeYaml(yamlFile, data);

    // Then
    assertTrue(Files.exists(yamlFile));
    String content = Files.readString(yamlFile);
    assertTrue(content.contains("key1: value1"));
    assertTrue(content.contains("key2: 123"));
    assertTrue(content.contains("nested:"));
    assertTrue(content.contains("nestedKey: nestedValue"));
  }

  @Test
  void testReadYaml() throws Exception {
    // Given
    String yamlContent = """
        key1: value1
        key2: 123
        nested:
          nestedKey: nestedValue
        """;
    Path yamlFile = tempDir.resolve("test.yml");
    Files.writeString(yamlFile, yamlContent);

    // When
    Map<String, Object> result = adapter.readYaml(yamlFile);

    // Then
    assertNotNull(result);
    assertEquals("value1", result.get("key1"));
    assertEquals(123, result.get("key2"));
    assertTrue(result.containsKey("nested"));
  }

  @Test
  void testReadYaml_nonExistentFile() {
    // Given
    Path nonExistent = tempDir.resolve("nonexistent.yml");

    // When
    Map<String, Object> result = adapter.readYaml(nonExistent);

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void testMergeYaml() {
    // Given
    Map<String, Object> base = new LinkedHashMap<>();
    base.put("existing", "value");
    base.put("toKeep", "original");

    Map<String, Object> overlay = new LinkedHashMap<>();
    overlay.put("new", "addition");
    overlay.put("toKeep", "modified");

    // When
    Map<String, Object> result = adapter.mergeYaml(base, overlay);

    // Then
    assertNotNull(result);
    assertEquals("value", result.get("existing"));
    assertEquals("addition", result.get("new"));
    // YamlMerger preserves existing values by default
    assertTrue(result.containsKey("toKeep"));
  }

  @Test
  void testMergeYaml_withNullBase() {
    // Given
    Map<String, Object> overlay = new LinkedHashMap<>();
    overlay.put("key", "value");

    // When
    Map<String, Object> result = adapter.mergeYaml(null, overlay);

    // Then
    assertNotNull(result);
    assertEquals("value", result.get("key"));
  }

  @Test
  void testMergeYaml_withNullOverlay() {
    // Given
    Map<String, Object> base = new LinkedHashMap<>();
    base.put("key", "value");

    // When
    Map<String, Object> result = adapter.mergeYaml(base, null);

    // Then
    assertNotNull(result);
    assertEquals("value", result.get("key"));
  }

  @Test
  void testMergeYamlFile() throws Exception {
    // Given - create existing file
    String existingContent = """
        existing: value
        toKeep: original
        """;
    Path yamlFile = tempDir.resolve("merge-test.yml");
    Files.writeString(yamlFile, existingContent);

    Map<String, Object> overlay = new LinkedHashMap<>();
    overlay.put("new", "addition");

    // When
    adapter.mergeYamlFile(yamlFile, overlay);

    // Then
    assertTrue(Files.exists(yamlFile));
    String content = Files.readString(yamlFile);
    assertTrue(content.contains("existing"));
    assertTrue(content.contains("new"));
  }

  @Test
  void testMergeYamlFile_withSensitiveProperties() throws Exception {
    // Given
    Path yamlFile = tempDir.resolve("sensitive.yml");

    Map<String, Object> overlay = new LinkedHashMap<>();
    overlay.put("database-password", "secret123");
    overlay.put("api-token", "token456");

    // When
    adapter.mergeYamlFile(yamlFile, overlay);

    // Then
    String content = Files.readString(yamlFile);
    assertTrue(content.contains("WARNING") || content.contains("credentials"));
  }

  @Test
  void testMergeYamlFile_nonExistentFile() throws Exception {
    // Given
    Path yamlFile = tempDir.resolve("new-file.yml");
    assertFalse(Files.exists(yamlFile));

    Map<String, Object> overlay = new LinkedHashMap<>();
    overlay.put("key", "value");

    // When
    adapter.mergeYamlFile(yamlFile, overlay);

    // Then
    assertTrue(Files.exists(yamlFile));
    String content = Files.readString(yamlFile);
    assertTrue(content.contains("key: value"));
  }

  @Test
  void testWriteConfiguration_atomicWrite() throws Exception {
    // Given
    ProjectConfig config = ProjectConfig.builder()
        .name("atomic-test")
        .basePackage("com.test")
        .architecture(ArchitectureType.HEXAGONAL_SINGLE)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .pluginVersion("1.0.0")
        .createdAt(LocalDateTime.now())
        .adaptersAsModules(false)
        .build();

    // When
    adapter.writeConfiguration(tempDir, config);

    // Then - verify no .tmp file remains
    Path configFile = tempDir.resolve(".cleanarch.yml");
    Path tempFile = tempDir.resolve(".cleanarch.yml.tmp");

    assertTrue(Files.exists(configFile));
    assertFalse(Files.exists(tempFile));
  }

  @Test
  void testWriteYaml_atomicWrite() throws Exception {
    // Given
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("test", "atomic");
    Path yamlFile = tempDir.resolve("atomic.yml");

    // When
    adapter.writeYaml(yamlFile, data);

    // Then - verify no .tmp file remains
    Path tempFile = tempDir.resolve("atomic.yml.tmp");
    assertTrue(Files.exists(yamlFile));
    assertFalse(Files.exists(tempFile));
  }
}
