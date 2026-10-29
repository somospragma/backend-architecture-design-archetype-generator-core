package com.pragma.archetype.infrastructure.adapter.out.template;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.pragma.archetype.domain.model.template.TemplateVersions;
import com.pragma.archetype.domain.port.out.TemplateRepository.TemplateNotFoundException;

/**
 * Loads {@code versions.yml} from the root of the template repository.
 * Templates older than the versions contract have no versions.yml; in that
 * case {@link TemplateVersions#empty()} is returned so they keep working.
 */
public class TemplateVersionsLoader {

  static final String VERSIONS_PATH = "versions.yml";

  private static final Logger logger = LoggerFactory.getLogger(TemplateVersionsLoader.class);

  private final TemplateContentProvider contentProvider;
  private final Yaml yaml;
  private TemplateVersions cached;

  public TemplateVersionsLoader(TemplateContentProvider contentProvider) {
    this.contentProvider = contentProvider;
    this.yaml = new Yaml();
  }

  /**
   * Loads the template versions, caching the result for this loader.
   *
   * @return parsed versions, or {@link TemplateVersions#empty()} when the
   *         template repository has no versions.yml
   * @throws TemplateNotFoundException if versions.yml exists but is invalid
   */
  public TemplateVersions load() {
    if (cached == null) {
      cached = read();
    }
    return cached;
  }

  private TemplateVersions read() {
    if (!contentProvider.templateExists(VERSIONS_PATH)) {
      logger.debug("No {} in template repository, using empty versions", VERSIONS_PATH);
      return TemplateVersions.empty();
    }

    try {
      Map<String, Object> data = yaml.load(contentProvider.getTemplateContent(VERSIONS_PATH));
      if (data == null || data.isEmpty()) {
        throw new IllegalArgumentException("File is empty");
      }

      int schemaVersion = requireSchemaVersion(data);
      return new TemplateVersions(
          schemaVersion,
          asString(data.get("java")),
          asString(data.get("gradle")),
          asStringMap(data.get("libraries"), "libraries"),
          asStringMap(data.get("quality"), "quality"));
    } catch (TemplateNotFoundException e) {
      throw e;
    } catch (Exception e) {
      throw new TemplateNotFoundException(
          "Invalid " + VERSIONS_PATH + " in template repository: " + e.getMessage());
    }
  }

  private int requireSchemaVersion(Map<String, Object> data) {
    Object value = data.get("schemaVersion");
    if (!(value instanceof Integer schemaVersion) || schemaVersion < 1) {
      throw new IllegalArgumentException("'schemaVersion' must be a positive integer");
    }
    return schemaVersion;
  }

  private Map<String, String> asStringMap(Object value, String field) {
    if (value == null) {
      return Map.of();
    }
    if (!(value instanceof Map<?, ?> raw)) {
      throw new IllegalArgumentException("'" + field + "' must be a map");
    }
    Map<String, String> result = new LinkedHashMap<>();
    raw.forEach((key, entry) -> result.put(String.valueOf(key), asString(entry)));
    return result;
  }

  // YAML reads unquoted values like 21 or 0.85 as numbers; versions are text
  private String asString(Object value) {
    return value == null ? null : String.valueOf(value);
  }
}
