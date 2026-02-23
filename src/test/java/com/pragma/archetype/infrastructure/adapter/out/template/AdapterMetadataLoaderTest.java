package com.pragma.archetype.infrastructure.adapter.out.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.pragma.archetype.domain.model.AdapterMetadata;
import com.pragma.archetype.domain.model.AdapterMetadata.ConfigurationClass;
import com.pragma.archetype.domain.model.AdapterMetadata.Dependency;
import com.pragma.archetype.domain.port.out.TemplateRepository.TemplateNotFoundException;

/**
 * Unit tests for AdapterMetadataLoader.
 */
@DisplayName("AdapterMetadataLoader Tests")
class AdapterMetadataLoaderTest {

  /**
   * Mock implementation of TemplateContentProvider for testing.
   */
  private static class MockTemplateContentProvider implements TemplateContentProvider {
    private final java.util.Map<String, String> templates = new java.util.HashMap<>();

    @Override
    public String getTemplateContent(String templatePath) {
      String content = templates.get(templatePath);
      if (content == null) {
        throw new TemplateNotFoundException("Template not found: " + templatePath);
      }
      return content;
    }

    @Override
    public boolean templateExists(String templatePath) {
      return templates.containsKey(templatePath);
    }

    void addTemplate(String path, String content) {
      templates.put(path, content);
    }
  }

  private AdapterMetadataLoader loader;

  private MockTemplateContentProvider contentProvider;

  @BeforeEach
  void setUp() {
    contentProvider = new MockTemplateContentProvider();
    loader = new AdapterMetadataLoader(contentProvider);
  }

  @Test
  @DisplayName("Should load basic adapter metadata with dependencies")
  void shouldLoadBasicAdapterMetadata() {
    // Given
    String yaml = """
        name: mongodb-adapter
        type: driven
        description: MongoDB database adapter
        dependencies:
          - group: org.springframework.boot
            artifact: spring-boot-starter-data-mongodb-reactive
            version: 3.2.0
        """;
    contentProvider.addTemplate("adapters/mongodb-adapter/metadata.yml", yaml);

    // When
    AdapterMetadata metadata = loader.loadAdapterMetadata("mongodb-adapter");

    // Then
    assertNotNull(metadata);
    assertEquals("mongodb-adapter", metadata.name());
    assertEquals("driven", metadata.type());
    assertEquals("MongoDB database adapter", metadata.description());
    assertEquals(1, metadata.dependencies().size());

    Dependency dep = metadata.dependencies().get(0);
    assertEquals("org.springframework.boot", dep.group());
    assertEquals("spring-boot-starter-data-mongodb-reactive", dep.artifact());
    assertEquals("3.2.0", dep.version());
    assertEquals("compile", dep.scope());
  }

  @Test
  @DisplayName("Should load adapter metadata with test dependencies")
  void shouldLoadAdapterMetadataWithTestDependencies() {
    // Given
    String yaml = """
        name: mongodb-adapter
        type: driven
        description: MongoDB database adapter
        dependencies:
          - group: org.springframework.boot
            artifact: spring-boot-starter-data-mongodb-reactive
            version: 3.2.0
        testDependencies:
          - group: de.flapdoodle.embed
            artifact: de.flapdoodle.embed.mongo
            version: 4.11.0
            scope: test
        """;
    contentProvider.addTemplate("adapters/mongodb-adapter/metadata.yml", yaml);

    // When
    AdapterMetadata metadata = loader.loadAdapterMetadata("mongodb-adapter");

    // Then
    assertNotNull(metadata);
    assertTrue(metadata.hasTestDependencies());
    assertEquals(1, metadata.testDependencies().size());

    Dependency testDep = metadata.testDependencies().get(0);
    assertEquals("de.flapdoodle.embed", testDep.group());
    assertEquals("de.flapdoodle.embed.mongo", testDep.artifact());
    assertEquals("4.11.0", testDep.version());
    assertEquals("test", testDep.scope());
  }

  @Test
  @DisplayName("Should load adapter metadata with application properties template")
  void shouldLoadAdapterMetadataWithApplicationPropertiesTemplate() {
    // Given
    String yaml = """
        name: mongodb-adapter
        type: driven
        description: MongoDB database adapter
        dependencies:
          - group: org.springframework.boot
            artifact: spring-boot-starter-data-mongodb-reactive
            version: 3.2.0
        applicationPropertiesTemplate: application-properties.yml.ftl
        """;
    contentProvider.addTemplate("adapters/mongodb-adapter/metadata.yml", yaml);
    contentProvider.addTemplate("adapters/mongodb-adapter/application-properties.yml.ftl",
        "spring:\n  data:\n    mongodb:\n      uri: mongodb://localhost:27017");

    // When
    AdapterMetadata metadata = loader.loadAdapterMetadata("mongodb-adapter");

    // Then
    assertNotNull(metadata);
    assertTrue(metadata.hasApplicationProperties());
    assertEquals("application-properties.yml.ftl", metadata.applicationPropertiesTemplate());
  }

  @Test
  @DisplayName("Should load adapter metadata with configuration classes")
  void shouldLoadAdapterMetadataWithConfigurationClasses() {
    // Given
    String yaml = """
        name: mongodb-adapter
        type: driven
        description: MongoDB database adapter
        dependencies:
          - group: org.springframework.boot
            artifact: spring-boot-starter-data-mongodb-reactive
            version: 3.2.0
        configurationClasses:
          - name: MongoConfig
            packagePath: config
            templatePath: MongoConfig.java.ftl
        """;
    contentProvider.addTemplate("adapters/mongodb-adapter/metadata.yml", yaml);
    contentProvider.addTemplate("adapters/mongodb-adapter/MongoConfig.java.ftl", "public class MongoConfig {}");

    // When
    AdapterMetadata metadata = loader.loadAdapterMetadata("mongodb-adapter");

    // Then
    assertNotNull(metadata);
    assertTrue(metadata.hasConfigurationClasses());
    assertEquals(1, metadata.configurationClasses().size());

    ConfigurationClass configClass = metadata.configurationClasses().get(0);
    assertEquals("MongoConfig", configClass.name());
    assertEquals("config", configClass.packagePath());
    assertEquals("MongoConfig.java.ftl", configClass.templatePath());
  }

  @Test
  @DisplayName("Should load adapter metadata with all new fields")
  void shouldLoadAdapterMetadataWithAllNewFields() {
    // Given
    String yaml = """
        name: mongodb-adapter
        type: driven
        description: MongoDB database adapter
        dependencies:
          - group: org.springframework.boot
            artifact: spring-boot-starter-data-mongodb-reactive
            version: 3.2.0
        testDependencies:
          - group: de.flapdoodle.embed
            artifact: de.flapdoodle.embed.mongo
            version: 4.11.0
        applicationPropertiesTemplate: application-properties.yml.ftl
        configurationClasses:
          - name: MongoConfig
            packagePath: config
            templatePath: MongoConfig.java.ftl
          - name: MongoHealthIndicator
            packagePath: health
            templatePath: MongoHealthIndicator.java.ftl
        """;
    contentProvider.addTemplate("adapters/mongodb-adapter/metadata.yml", yaml);
    contentProvider.addTemplate("adapters/mongodb-adapter/application-properties.yml.ftl",
        "spring:\n  data:\n    mongodb:\n      uri: mongodb://localhost:27017");
    contentProvider.addTemplate("adapters/mongodb-adapter/MongoConfig.java.ftl", "public class MongoConfig {}");
    contentProvider.addTemplate("adapters/mongodb-adapter/MongoHealthIndicator.java.ftl",
        "public class MongoHealthIndicator {}");

    // When
    AdapterMetadata metadata = loader.loadAdapterMetadata("mongodb-adapter");

    // Then
    assertNotNull(metadata);
    assertEquals("mongodb-adapter", metadata.name());
    assertEquals("driven", metadata.type());
    assertEquals(1, metadata.dependencies().size());
    assertEquals(1, metadata.testDependencies().size());
    assertTrue(metadata.hasApplicationProperties());
    assertTrue(metadata.hasConfigurationClasses());
    assertEquals(2, metadata.configurationClasses().size());
  }

  @Test
  @DisplayName("Should throw exception when metadata file not found")
  void shouldThrowExceptionWhenMetadataFileNotFound() {
    // When/Then
    assertThrows(TemplateNotFoundException.class, () -> {
      loader.loadAdapterMetadata("nonexistent-adapter");
    });
  }

  @Test
  @DisplayName("Should throw exception when required field is missing")
  void shouldThrowExceptionWhenRequiredFieldMissing() {
    // Given
    String yaml = """
        type: driven
        description: MongoDB database adapter
        """;
    contentProvider.addTemplate("adapters/mongodb-adapter/metadata.yml", yaml);

    // When/Then
    assertThrows(TemplateNotFoundException.class, () -> {
      loader.loadAdapterMetadata("mongodb-adapter");
    });
  }

  @Test
  @DisplayName("Should throw exception when application properties template not found")
  void shouldThrowExceptionWhenApplicationPropertiesTemplateNotFound() {
    // Given
    String yaml = """
        name: mongodb-adapter
        type: driven
        description: MongoDB database adapter
        dependencies:
          - group: org.springframework.boot
            artifact: spring-boot-starter-data-mongodb-reactive
            version: 3.2.0
        applicationPropertiesTemplate: application-properties.yml.ftl
        """;
    contentProvider.addTemplate("adapters/mongodb-adapter/metadata.yml", yaml);
    // Note: NOT adding the application-properties.yml.ftl template

    // When/Then
    assertThrows(TemplateNotFoundException.class, () -> {
      loader.loadAdapterMetadata("mongodb-adapter");
    });
  }

  @Test
  @DisplayName("Should throw exception when configuration class template not found")
  void shouldThrowExceptionWhenConfigurationClassTemplateNotFound() {
    // Given
    String yaml = """
        name: mongodb-adapter
        type: driven
        description: MongoDB database adapter
        dependencies:
          - group: org.springframework.boot
            artifact: spring-boot-starter-data-mongodb-reactive
            version: 3.2.0
        configurationClasses:
          - name: MongoConfig
            packagePath: config
            templatePath: MongoConfig.java.ftl
        """;
    contentProvider.addTemplate("adapters/mongodb-adapter/metadata.yml", yaml);
    // Note: NOT adding the MongoConfig.java.ftl template

    // When/Then
    assertThrows(TemplateNotFoundException.class, () -> {
      loader.loadAdapterMetadata("mongodb-adapter");
    });
  }

  @Test
  @DisplayName("Should handle empty optional fields")
  void shouldHandleEmptyOptionalFields() {
    // Given
    String yaml = """
        name: simple-adapter
        type: driven
        description: Simple adapter
        dependencies:
          - group: org.springframework.boot
            artifact: spring-boot-starter
            version: 3.2.0
        """;
    contentProvider.addTemplate("adapters/simple-adapter/metadata.yml", yaml);

    // When
    AdapterMetadata metadata = loader.loadAdapterMetadata("simple-adapter");

    // Then
    assertNotNull(metadata);
    assertFalse(metadata.hasTestDependencies());
    assertFalse(metadata.hasApplicationProperties());
    assertFalse(metadata.hasConfigurationClasses());
    assertTrue(metadata.testDependencies().isEmpty());
    assertTrue(metadata.configurationClasses().isEmpty());
  }
}
