package com.pragma.archetype.infrastructure.adapter.out.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.yaml.snakeyaml.Yaml;

import com.pragma.archetype.domain.model.ArchitectureType;
import com.pragma.archetype.domain.model.Framework;
import com.pragma.archetype.domain.model.Paradigm;
import com.pragma.archetype.domain.model.ProjectConfig;
import com.pragma.archetype.domain.model.TemplateConfig;
import com.pragma.archetype.domain.model.TemplateMode;
import com.pragma.archetype.domain.port.out.ConfigurationPort;

/**
 * Adapter for reading and writing project configuration in YAML format.
 * Uses SnakeYAML library.
 */
public class YamlConfigurationAdapter implements ConfigurationPort {

  private static final String CONFIG_FILE_NAME = ".cleanarch.yml";
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

  private final Yaml yaml;

  public YamlConfigurationAdapter() {
    this.yaml = new Yaml();
  }

  @Override
  public Optional<ProjectConfig> readConfiguration(Path projectPath) {
    Path configFile = projectPath.resolve(CONFIG_FILE_NAME);

    if (!Files.exists(configFile)) {
      return Optional.empty();
    }

    try {
      String content = Files.readString(configFile);
      Map<String, Object> data = yaml.load(content);

      return Optional.of(parseConfiguration(data));

    } catch (IOException e) {
      throw new RuntimeException("Failed to read configuration file: " + configFile, e);
    }
  }

  @Override
  public void writeConfiguration(Path projectPath, ProjectConfig config) {
    Path configFile = projectPath.resolve(CONFIG_FILE_NAME);

    try {
      Map<String, Object> data = toYamlMap(config);
      String yamlContent = yaml.dump(data);

      Files.writeString(configFile, yamlContent);

    } catch (IOException e) {
      throw new RuntimeException("Failed to write configuration file: " + configFile, e);
    }
  }

  @Override
  public boolean configurationExists(Path projectPath) {
    Path configFile = projectPath.resolve(CONFIG_FILE_NAME);
    return Files.exists(configFile);
  }

  @Override
  public void deleteConfiguration(Path projectPath) {
    Path configFile = projectPath.resolve(CONFIG_FILE_NAME);

    try {
      if (Files.exists(configFile)) {
        Files.delete(configFile);
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to delete configuration file: " + configFile, e);
    }
  }

  /**
   * Reads template configuration from .cleanarch.yml
   *
   * @param projectPath path to project
   * @return template configuration or default if not specified
   */
  public TemplateConfig readTemplateConfiguration(Path projectPath) {
    Path configFile = projectPath.resolve(CONFIG_FILE_NAME);

    if (!Files.exists(configFile)) {
      return TemplateConfig.defaultConfig();
    }

    try {
      String content = Files.readString(configFile);
      Map<String, Object> data = yaml.load(content);

      // Check if templates section exists
      @SuppressWarnings("unchecked")
      Map<String, Object> templatesSection = (Map<String, Object>) data.get("templates");

      if (templatesSection == null) {
        return TemplateConfig.defaultConfig();
      }

      return parseTemplateConfig(templatesSection);

    } catch (IOException e) {
      throw new RuntimeException("Failed to read configuration file: " + configFile, e);
    }
  }

  /**
   * Parses template configuration from YAML map.
   */
  private TemplateConfig parseTemplateConfig(Map<String, Object> templatesSection) {
    String mode = (String) templatesSection.get("mode");
    String repository = (String) templatesSection.get("repository");
    String branch = (String) templatesSection.get("branch");
    String version = (String) templatesSection.get("version");
    String localPath = (String) templatesSection.get("localPath");
    Boolean cache = (Boolean) templatesSection.get("cache");

    TemplateMode templateMode = TemplateMode.PRODUCTION;
    if ("developer".equalsIgnoreCase(mode)) {
      templateMode = TemplateMode.DEVELOPER;
    }

    return new TemplateConfig(
        templateMode,
        repository != null ? repository
            : "https://github.com/somospragma/backend-architecture-design-archetype-generator-templates",
        branch != null ? branch : "main",
        version,
        localPath,
        cache != null ? cache : true);
  }

  /**
   * Parses YAML map into ProjectConfig.
   */
  private ProjectConfig parseConfiguration(Map<String, Object> data) {
    @SuppressWarnings("unchecked")
    Map<String, Object> project = (Map<String, Object>) data.get("project");

    String name = (String) project.get("name");
    String basePackage = (String) project.get("basePackage");
    String pluginVersion = (String) project.get("pluginVersion");
    String createdAtStr = (String) project.get("createdAt");

    @SuppressWarnings("unchecked")
    Map<String, Object> architecture = (Map<String, Object>) data.get("architecture");

    String architectureType = (String) architecture.get("type");
    String paradigm = (String) architecture.get("paradigm");
    String framework = (String) architecture.get("framework");

    return ProjectConfig.builder()
        .name(name)
        .basePackage(basePackage)
        .architecture(ArchitectureType.valueOf(architectureType.toUpperCase().replace('-', '_')))
        .paradigm(Paradigm.valueOf(paradigm.toUpperCase()))
        .framework(Framework.valueOf(framework.toUpperCase()))
        .pluginVersion(pluginVersion)
        .createdAt(LocalDateTime.parse(createdAtStr, DATE_FORMATTER))
        .build();
  }

  /**
   * Converts ProjectConfig to YAML map.
   */
  private Map<String, Object> toYamlMap(ProjectConfig config) {
    Map<String, Object> data = new LinkedHashMap<>();

    // Project section
    Map<String, Object> project = new LinkedHashMap<>();
    project.put("name", config.name());
    project.put("basePackage", config.basePackage());
    project.put("pluginVersion", config.pluginVersion());
    project.put("createdAt", config.createdAt().format(DATE_FORMATTER));
    data.put("project", project);

    // Architecture section
    Map<String, Object> architecture = new LinkedHashMap<>();
    architecture.put("type", config.architecture().name().toLowerCase().replace('_', '-'));
    architecture.put("paradigm", config.paradigm().name().toLowerCase());
    architecture.put("framework", config.framework().name().toLowerCase());
    data.put("architecture", architecture);

    return data;
  }
}
