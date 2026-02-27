package com.pragma.archetype.infrastructure.adapter.out.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.pragma.archetype.domain.model.adapter.AdapterMetadata;
import com.pragma.archetype.domain.model.adapter.AdapterMetadata.ConfigurationClass;
import com.pragma.archetype.domain.model.adapter.AdapterMetadata.Dependency;
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
    assertEquals("mongodb-adapter", metadata.getName());
    assertEquals(com.pragma.archetype.domain.model.adapter.AdapterDirection.OUTPUT, metadata.getDirection());
    assertEquals("MongoDB database adapter", metadata.getDescription());
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
    assertEquals("mongodb-adapter", metadata.getName());
    assertEquals(com.pragma.archetype.domain.model.adapter.AdapterDirection.OUTPUT, metadata.getDirection());
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

  @Test
  @DisplayName("Should load adapter metadata with suffix")
  void load_withSuffix_parsesCorrectly() {
    // Given
    String yaml = """
        name: test
        type: INPUT
        displayName: Test Adapter
        description: Test adapter for unit testing
        suffix: Controller
        templates:
          - source: Controller.java.ftl
            target: {entityName}Controller.java
            outputDir: src/main/java
        """;
    String metadataPath = "frameworks/spring/imperative/adapters/entry-points/test-adapter/metadata.yml";
    contentProvider.addTemplate(metadataPath, yaml);
    contentProvider.addTemplate("frameworks/spring/imperative/adapters/entry-points/test-adapter/Controller.java.ftl",
        "template");

    // When
    AdapterMetadata metadata = loader.loadMetadata(metadataPath);

    // Then
    assertNotNull(metadata);
    assertEquals("Controller", metadata.getSuffix());
  }

  @Test
  @DisplayName("Should load adapter metadata with applicationPropertiesTemplate")
  void load_withApplicationPropertiesTemplate_parsesCorrectly() {
    // Given
    String yaml = """
        name: test
        type: INPUT
        displayName: Test Adapter
        description: Test adapter for unit testing
        applicationPropertiesTemplate: application-test.yml.ftl
        templates:
          - source: Controller.java.ftl
            target: {entityName}Controller.java
            outputDir: src/main/java
        """;
    String metadataPath = "frameworks/spring/imperative/adapters/entry-points/test-adapter/metadata.yml";
    contentProvider.addTemplate(metadataPath, yaml);
    contentProvider.addTemplate("frameworks/spring/imperative/adapters/entry-points/test-adapter/Controller.java.ftl",
        "template");
    contentProvider.addTemplate(
        "frameworks/spring/imperative/adapters/entry-points/test-adapter/application-test.yml.ftl", "props");

    // When
    AdapterMetadata metadata = loader.loadMetadata(metadataPath);

    // Then
    assertNotNull(metadata);
    assertEquals("application-test.yml.ftl", metadata.applicationPropertiesTemplate());
  }

  @Test
  @DisplayName("Should load adapter metadata with dependencies as strings")
  void load_withDependencies_parsesCorrectly() {
    // Given
    String yaml = """
        name: test
        type: INPUT
        displayName: Test Adapter
        description: Test adapter for unit testing
        dependencies:
          - org.springframework.boot:spring-boot-starter-web
        templates:
          - source: Controller.java.ftl
            target: {entityName}Controller.java
            outputDir: src/main/java
        """;
    String metadataPath = "frameworks/spring/imperative/adapters/entry-points/test-adapter/metadata.yml";
    contentProvider.addTemplate(metadataPath, yaml);
    contentProvider.addTemplate("frameworks/spring/imperative/adapters/entry-points/test-adapter/Controller.java.ftl",
        "template");

    // When
    AdapterMetadata metadata = loader.loadMetadata(metadataPath);

    // Then
    assertNotNull(metadata);
    assertNotNull(metadata.dependencies());
    assertEquals(1, metadata.dependencies().size());
    assertEquals("org.springframework.boot:spring-boot-starter-web", metadata.dependencies().get(0));
  }

  @Test
  @DisplayName("Should load adapter metadata with unified templates")
  void load_withUnifiedTemplates_parsesCorrectly() {
    // Given
    String yaml = """
        name: test
        type: INPUT
        displayName: Test Adapter
        description: Test adapter for unit testing
        templates:
          - source: Controller.java.ftl
            target: {entityName}Controller.java
            outputDir: src/main/java
        """;
    String metadataPath = "frameworks/spring/imperative/adapters/entry-points/test-adapter/metadata.yml";
    contentProvider.addTemplate(metadataPath, yaml);
    contentProvider.addTemplate("frameworks/spring/imperative/adapters/entry-points/test-adapter/Controller.java.ftl",
        "template");

    // When
    AdapterMetadata metadata = loader.loadMetadata(metadataPath);

    // Then
    assertNotNull(metadata);
    assertNotNull(metadata.getTemplates());
    assertEquals(1, metadata.getTemplates().size());
    assertEquals("Controller.java.ftl", metadata.getTemplates().get(0).getSource());
  }
}
