package com.pragma.archetype.infrastructure.adapter.out.template;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.pragma.archetype.domain.model.project.ArchitectureType;
import com.pragma.archetype.domain.port.out.TemplateRepository.TemplateNotFoundException;

/**
 * Loads {@code architectures/index.yml}, the list of architectures the template
 * repository provides. Templates older than the index fall back to the
 * architectures the plugin ships knowledge of.
 */
public class ArchitectureIndexLoader {

  static final String INDEX_PATH = "architectures/index.yml";

  private static final Logger logger = LoggerFactory.getLogger(ArchitectureIndexLoader.class);

  private final TemplateContentProvider contentProvider;
  private final Yaml yaml;
  private List<ArchitectureType> cached;

  public ArchitectureIndexLoader(TemplateContentProvider contentProvider) {
    this.contentProvider = contentProvider;
    this.yaml = new Yaml();
  }

  /**
   * Loads the available architectures, caching the result for this loader.
   *
   * @throws TemplateNotFoundException if index.yml exists but is invalid
   */
  public List<ArchitectureType> load() {
    if (cached == null) {
      cached = read();
    }
    return cached;
  }

  @SuppressWarnings("unchecked")
  private List<ArchitectureType> read() {
    if (!contentProvider.templateExists(INDEX_PATH)) {
      logger.debug("No {} in template repository, using built-in architectures", INDEX_PATH);
      return List.of(ArchitectureType.values());
    }

    try {
      Map<String, Object> data = yaml.load(contentProvider.getTemplateContent(INDEX_PATH));
      if (data == null || !(data.get("architectures") instanceof List<?> entries) || entries.isEmpty()) {
        throw new IllegalArgumentException("'architectures' must be a non-empty list");
      }

      List<ArchitectureType> architectures = new ArrayList<>();
      for (Object entry : entries) {
        if (!(entry instanceof Map<?, ?> map) || !(map.get("name") instanceof String name)) {
          throw new IllegalArgumentException("Each architecture needs a 'name'");
        }
        architectures.add(ArchitectureType.fromValue(name));
      }
      return List.copyOf(architectures);
    } catch (Exception e) {
      throw new TemplateNotFoundException(
          "Invalid " + INDEX_PATH + " in template repository: " + e.getMessage());
    }
  }
}
