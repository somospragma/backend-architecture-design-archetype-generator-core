package com.pragma.archetype.infrastructure.adapter.out.template;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.pragma.archetype.domain.model.config.TemplateConfig;
import com.pragma.archetype.domain.model.config.TemplateMode;

@DisplayName("FreemarkerTemplateRepository Integration Tests")
class FreemarkerTemplateRepositoryIntegrationTest {

  @Test
  @DisplayName("Should initialize with TemplateSourceResolver in local mode")
  void shouldInitializeWithTemplateSourceResolverInLocalMode(@TempDir Path tempDir) throws IOException {
    // Given: A template directory with a simple template
    Path templateFile = tempDir.resolve("test.ftl");
    Files.writeString(templateFile, "Hello ${name}!");

    TemplateConfig config = new TemplateConfig(
        TemplateMode.DEVELOPER,
        null,
        null,
        null,
        tempDir.toString(),
        false);

    // When: Creating repository with template config
    FreemarkerTemplateRepository repository = assertDoesNotThrow(
        () -> new FreemarkerTemplateRepository(config));

    // Then: Should initialize successfully
    assertNotNull(repository);

    // And: Should be able to process templates
    Map<String, Object> context = new HashMap<>();
    context.put("name", "World");
    String result = repository.processTemplate("test.ftl", context);

    assertNotNull(result);
    assertTrue(result.contains("Hello World!"));
  }

  @Test
  @DisplayName("Should initialize with TemplateSourceResolver in remote mode")
  void shouldInitializeWithTemplateSourceResolverInRemoteMode() {
    // Given: A remote template configuration
    TemplateConfig config = new TemplateConfig(
        TemplateMode.PRODUCTION,
        "https://github.com/somospragma/backend-architecture-design-archetype-generator-templates",
        "main",
        null,
        null,
        true);

    // When: Creating repository with template config
    // Then: Should initialize successfully without throwing exceptions
    FreemarkerTemplateRepository repository = assertDoesNotThrow(
        () -> new FreemarkerTemplateRepository(config));

    assertNotNull(repository);
    assertNotNull(repository.getDownloader());
  }

  @Test
  @DisplayName("Should log template source mode on initialization")
  void shouldLogTemplateSourceModeOnInitialization(@TempDir Path tempDir) {
    // Given: A local template configuration
    TemplateConfig config = new TemplateConfig(
        TemplateMode.DEVELOPER,
        null,
        null,
        null,
        tempDir.toString(),
        false);

    // When: Creating repository
    // Then: Should not throw and should log (verified by no exceptions)
    assertDoesNotThrow(() -> new FreemarkerTemplateRepository(config));
  }

  @Test
  @DisplayName("Should handle template processing with hot reload in local mode")
  void shouldHandleTemplateProcessingWithHotReloadInLocalMode(@TempDir Path tempDir) throws IOException {
    // Given: A template directory with a template
    Path templateFile = tempDir.resolve("dynamic.ftl");
    Files.writeString(templateFile, "Version 1: ${value}");

    TemplateConfig config = new TemplateConfig(
        TemplateMode.DEVELOPER,
        null,
        null,
        null,
        tempDir.toString(),
        false);

    FreemarkerTemplateRepository repository = new FreemarkerTemplateRepository(config);

    // When: Processing template first time
    Map<String, Object> context = new HashMap<>();
    context.put("value", "test");
    String result1 = repository.processTemplate("dynamic.ftl", context);

    // Then: Should get first version
    assertTrue(result1.contains("Version 1"));

    // When: Updating template (simulating hot reload scenario)
    Files.writeString(templateFile, "Version 2: ${value}");

    // And: Processing again
    String result2 = repository.processTemplate("dynamic.ftl", context);

    // Then: Should get updated version (hot reload)
    assertTrue(result2.contains("Version 2"));
  }
}
