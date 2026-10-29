package com.pragma.archetype.infrastructure.adapter.out.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.pragma.archetype.domain.model.template.TemplateVersions;
import com.pragma.archetype.domain.port.out.TemplateRepository.TemplateNotFoundException;

@DisplayName("TemplateVersionsLoader Tests")
class TemplateVersionsLoaderTest {

  private static class MapContentProvider implements TemplateContentProvider {
    private final Map<String, String> templates = new HashMap<>();
    private int reads;

    @Override
    public String getTemplateContent(String templatePath) {
      reads++;
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
  }

  private MapContentProvider contentProvider;
  private TemplateVersionsLoader loader;

  @BeforeEach
  void setUp() {
    contentProvider = new MapContentProvider();
    loader = new TemplateVersionsLoader(contentProvider);
  }

  @Test
  @DisplayName("Should load java, gradle, libraries and quality from versions.yml")
  void shouldLoadVersions() {
    contentProvider.templates.put("versions.yml", """
        schemaVersion: 1
        java: 21
        gradle: "8.14.1"
        libraries:
          spring-boot: "4.0.3"
          grpc: 1.58.0
        quality:
          jacoco-min-coverage: 0.85
          pit-mutation-threshold: 20
        """);

    TemplateVersions versions = loader.load();

    assertEquals(1, versions.schemaVersion());
    assertEquals("21", versions.java());
    assertEquals("8.14.1", versions.gradle());
    assertEquals("4.0.3", versions.library("spring-boot").orElseThrow());
    assertEquals("1.58.0", versions.library("grpc").orElseThrow());
    assertEquals("0.85", versions.quality().get("jacoco-min-coverage"));
    assertEquals("20", versions.quality().get("pit-mutation-threshold"));
  }

  @Test
  @DisplayName("Should return empty versions when templates predate versions.yml")
  void shouldReturnEmptyWhenFileIsMissing() {
    TemplateVersions versions = loader.load();

    assertTrue(versions.isEmpty());
    assertTrue(versions.library("spring-boot").isEmpty());
  }

  @Test
  @DisplayName("Should reject versions.yml without a valid schemaVersion")
  void shouldRejectMissingSchemaVersion() {
    contentProvider.templates.put("versions.yml", "java: 21\n");

    TemplateNotFoundException error = assertThrows(TemplateNotFoundException.class, loader::load);
    assertTrue(error.getMessage().contains("schemaVersion"));
  }

  @Test
  @DisplayName("Should reject libraries that are not a map")
  void shouldRejectInvalidLibraries() {
    contentProvider.templates.put("versions.yml", """
        schemaVersion: 1
        libraries:
          - spring-boot
        """);

    TemplateNotFoundException error = assertThrows(TemplateNotFoundException.class, loader::load);
    assertTrue(error.getMessage().contains("libraries"));
  }

  @Test
  @DisplayName("Should read versions.yml only once")
  void shouldCacheVersions() {
    contentProvider.templates.put("versions.yml", "schemaVersion: 1\n");

    TemplateVersions first = loader.load();
    TemplateVersions second = loader.load();

    assertSame(first, second);
    assertEquals(1, contentProvider.reads);
  }
}
