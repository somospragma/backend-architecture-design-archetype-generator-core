package com.pragma.archetype.infrastructure.adapter.out.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.pragma.archetype.domain.model.AdapterMetadata;

/**
 * Integration tests for AdapterMetadataLoader with
 * FreemarkerTemplateRepository.
 */
@DisplayName("AdapterMetadataLoader Integration Tests")
class AdapterMetadataLoaderIntegrationTest {

  @TempDir
  Path tempDir;

  private FreemarkerTemplateRepository repository;

  @BeforeEach
  void setUp() throws IOException {
    // Create adapter directory structure
    Path adaptersDir = tempDir.resolve("adapters");
    Path mongoAdapterDir = adaptersDir.resolve("mongodb-adapter");
    Files.createDirectories(mongoAdapterDir);

    // Create metadata.yml with all new fields
    String metadataYaml = """
        name: mongodb-adapter
        type: driven
        description: MongoDB database adapter using Spring Data Reactive
        dependencies:
          - group: org.springframework.boot
            artifact: spring-boot-starter-data-mongodb-reactive
            version: 3.2.0
        testDependencies:
          - group: de.flapdoodle.embed
            artifact: de.flapdoodle.embed.mongo
            version: 4.11.0
            scope: test
        applicationPropertiesTemplate: application-properties.yml.ftl
        configurationClasses:
          - name: MongoConfig
            packagePath: config
            templatePath: MongoConfig.java.ftl
        """;
    Files.writeString(mongoAdapterDir.resolve("metadata.yml"), metadataYaml);

    // Create referenced template files
    Files.writeString(mongoAdapterDir.resolve("application-properties.yml.ftl"),
        "spring:\n  data:\n    mongodb:\n      uri: mongodb://localhost:27017");
    Files.writeString(mongoAdapterDir.resolve("MongoConfig.java.ftl"),
        "public class MongoConfig {}");

    // Create repository
    repository = new FreemarkerTemplateRepository(tempDir);
  }

  @Test
  @DisplayName("Should load adapter metadata through FreemarkerTemplateRepository")
  void shouldLoadAdapterMetadataThroughRepository() {
    // When
    AdapterMetadata metadata = repository.loadAdapterMetadata("mongodb-adapter");

    // Then
    assertNotNull(metadata);
    assertEquals("mongodb-adapter", metadata.name());
    assertEquals("driven", metadata.type());
    assertEquals("MongoDB database adapter using Spring Data Reactive", metadata.description());

    // Verify dependencies
    assertEquals(1, metadata.dependencies().size());
    assertEquals("org.springframework.boot", metadata.dependencies().get(0).group());

    // Verify test dependencies
    assertTrue(metadata.hasTestDependencies());
    assertEquals(1, metadata.testDependencies().size());
    assertEquals("de.flapdoodle.embed", metadata.testDependencies().get(0).group());
    assertEquals("test", metadata.testDependencies().get(0).scope());

    // Verify application properties template
    assertTrue(metadata.hasApplicationProperties());
    assertEquals("application-properties.yml.ftl", metadata.applicationPropertiesTemplate());

    // Verify configuration classes
    assertTrue(metadata.hasConfigurationClasses());
    assertEquals(1, metadata.configurationClasses().size());
    assertEquals("MongoConfig", metadata.configurationClasses().get(0).name());
    assertEquals("config", metadata.configurationClasses().get(0).packagePath());
    assertEquals("MongoConfig.java.ftl", metadata.configurationClasses().get(0).templatePath());
  }

  @Test
  @DisplayName("Should validate that referenced templates exist")
  void shouldValidateThatReferencedTemplatesExist() throws IOException {
    // Given - create adapter without referenced template files
    Path adaptersDir = tempDir.resolve("adapters");
    Path invalidAdapterDir = adaptersDir.resolve("invalid-adapter");
    Files.createDirectories(invalidAdapterDir);

    String metadataYaml = """
        name: invalid-adapter
        type: driven
        description: Invalid adapter
        dependencies:
          - group: org.springframework.boot
            artifact: spring-boot-starter
            version: 3.2.0
        applicationPropertiesTemplate: missing-template.yml.ftl
        """;
    Files.writeString(invalidAdapterDir.resolve("metadata.yml"), metadataYaml);

    // When/Then
    assertThrows(Exception.class, () -> {
      repository.loadAdapterMetadata("invalid-adapter");
    });
  }
}
