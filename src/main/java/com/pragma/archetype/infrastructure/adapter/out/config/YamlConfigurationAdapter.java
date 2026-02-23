package com.pragma.archetype.infrastructure.adapter.out.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import com.pragma.archetype.domain.model.ArchitectureType;
import com.pragma.archetype.domain.model.Framework;
import com.pragma.archetype.domain.model.MergeResult;
import com.pragma.archetype.domain.model.Paradigm;
import com.pragma.archetype.domain.model.ProjectConfig;
import com.pragma.archetype.domain.model.TemplateConfig;
import com.pragma.archetype.domain.model.TemplateMode;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.service.YamlMerger;

/**
 * Adapter for reading and writing project configuration in YAML format.
 * Uses SnakeYAML library.
 * Supports intelligent YAML merging with comment preservation and security
 * warnings.
 */
public class YamlConfigurationAdapter implements ConfigurationPort {

  private static final Logger logger = LoggerFactory.getLogger(YamlConfigurationAdapter.class);
  private static final String CONFIG_FILE_NAME = ".cleanarch.yml";
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

  // Security warning comment for sensitive properties
  private static final String SECURITY_WARNING_COMMENT = "# WARNING: Do not store credentials in source control\n" +
      "# Use environment variables or secret management in production\n";

  // Patterns to identify sensitive property keys
  private static final Set<Pattern> SENSITIVE_PROPERTY_PATTERNS = Set.of(
      Pattern.compile(".*password.*", Pattern.CASE_INSENSITIVE),
      Pattern.compile(".*secret.*", Pattern.CASE_INSENSITIVE),
      Pattern.compile(".*credential.*", Pattern.CASE_INSENSITIVE),
      Pattern.compile(".*token.*", Pattern.CASE_INSENSITIVE),
      Pattern.compile(".*key.*", Pattern.CASE_INSENSITIVE),
      Pattern.compile(".*uri.*", Pattern.CASE_INSENSITIVE),
      Pattern.compile(".*url.*", Pattern.CASE_INSENSITIVE));

  private final Yaml yaml;
  private final Yaml formattedYaml;
  private final YamlMerger yamlMerger;

  public YamlConfigurationAdapter() {
    this.yaml = new Yaml();
    this.formattedYaml = createFormattedYaml();
    this.yamlMerger = new YamlMerger();
  }

  /**
   * Constructor with YamlMerger dependency injection for testing.
   */
  public YamlConfigurationAdapter(YamlMerger yamlMerger) {
    this.yaml = new Yaml();
    this.formattedYaml = createFormattedYaml();
    this.yamlMerger = yamlMerger;
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

      // Atomic write: write to temporary file first, then rename
      Path tempFile = configFile.resolveSibling(configFile.getFileName() + ".tmp");
      Files.writeString(tempFile, yamlContent);
      Files.move(tempFile, configFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING,
          java.nio.file.StandardCopyOption.ATOMIC_MOVE);

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
  @Override
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
   * Reads YAML file and returns it as a map.
   *
   * @param filePath path to YAML file
   * @return map representation of YAML content
   */
  public Map<String, Object> readYaml(Path filePath) {
    if (!Files.exists(filePath)) {
      logger.debug("YAML file does not exist: {}", filePath);
      return new LinkedHashMap<>();
    }

    try {
      String content = Files.readString(filePath);
      Map<String, Object> data = yaml.load(content);
      return data != null ? data : new LinkedHashMap<>();
    } catch (IOException e) {
      throw new RuntimeException("Failed to read YAML file: " + filePath, e);
    }
  }

  /**
   * Writes a map to a YAML file with 2-space indentation.
   *
   * @param filePath path to YAML file
   * @param data     map to write
   */
  public void writeYaml(Path filePath, Map<String, Object> data) {
    try {
      String yamlContent = formattedYaml.dump(data);

      // Atomic write: write to temporary file first, then rename
      Path tempFile = filePath.resolveSibling(filePath.getFileName() + ".tmp");
      Files.writeString(tempFile, yamlContent);
      Files.move(tempFile, filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING,
          java.nio.file.StandardCopyOption.ATOMIC_MOVE);

      logger.debug("Wrote YAML file atomically: {}", filePath);
    } catch (IOException e) {
      throw new RuntimeException("Failed to write YAML file: " + filePath, e);
    }
  }

  /**
   * Merges overlay YAML into base YAML, preserving existing values.
   * Uses YamlMerger for intelligent merging with conflict detection.
   *
   * @param base    existing YAML map
   * @param overlay new properties to merge
   * @return merged YAML map
   */
  public Map<String, Object> mergeYaml(Map<String, Object> base, Map<String, Object> overlay) {
    if (base == null) {
      base = new LinkedHashMap<>();
    }
    if (overlay == null) {
      overlay = new LinkedHashMap<>();
    }

    MergeResult result = yamlMerger.merge(base, overlay);

    // Log conflicts for user awareness
    if (!result.conflicts().isEmpty()) {
      logger.warn("YAML merge detected {} conflicts:", result.conflicts().size());
      result.conflicts().forEach(conflict -> logger.warn("  - {}", conflict));
    }

    logger.info("YAML merge completed: {} new keys added", result.addedKeys().size());

    return result.merged();
  }

  /**
   * Merges overlay YAML into an existing YAML file, preserving comments and
   * structure.
   * Adds security warnings for sensitive properties.
   *
   * @param filePath path to existing YAML file
   * @param overlay  new properties to merge
   */
  public void mergeYamlFile(Path filePath, Map<String, Object> overlay) {
    // Read existing content with comments
    String existingContent = "";
    Map<String, Object> existingData = new LinkedHashMap<>();

    if (Files.exists(filePath)) {
      try {
        existingContent = Files.readString(filePath);
        existingData = yaml.load(existingContent);
        if (existingData == null) {
          existingData = new LinkedHashMap<>();
        }
      } catch (IOException e) {
        throw new RuntimeException("Failed to read YAML file: " + filePath, e);
      }
    }

    // Merge the data
    Map<String, Object> merged = mergeYaml(existingData, overlay);

    // Generate new YAML content with proper formatting
    String newContent = formattedYaml.dump(merged);

    // Add security warnings for sensitive properties
    newContent = addSecurityComments(newContent, overlay);

    // Preserve existing comments where possible
    newContent = preserveComments(existingContent, newContent);

    // Write the merged content atomically
    try {
      // Atomic write: write to temporary file first, then rename
      Path tempFile = filePath.resolveSibling(filePath.getFileName() + ".tmp");
      Files.writeString(tempFile, newContent);
      Files.move(tempFile, filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING,
          java.nio.file.StandardCopyOption.ATOMIC_MOVE);

      logger.info("Merged YAML file atomically: {}", filePath);
    } catch (IOException e) {
      throw new RuntimeException("Failed to write merged YAML file: " + filePath, e);
    }
  }

  /**
   * Creates a Yaml instance configured for 2-space indentation and proper
   * formatting.
   */
  private Yaml createFormattedYaml() {
    DumperOptions options = new DumperOptions();
    options.setIndent(2);
    options.setPrettyFlow(true);
    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    return new Yaml(options);
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
    Boolean adaptersAsModules = (Boolean) architecture.get("adaptersAsModules");

    // Parse dependency overrides if present
    @SuppressWarnings("unchecked")
    Map<String, String> dependencyOverrides = (Map<String, String>) data.get("dependencyOverrides");
    if (dependencyOverrides == null) {
      dependencyOverrides = Map.of();
    }

    return ProjectConfig.builder()
        .name(name)
        .basePackage(basePackage)
        .architecture(ArchitectureType.valueOf(architectureType.toUpperCase().replace('-', '_')))
        .paradigm(Paradigm.valueOf(paradigm.toUpperCase()))
        .framework(Framework.valueOf(framework.toUpperCase()))
        .pluginVersion(pluginVersion)
        .createdAt(LocalDateTime.parse(createdAtStr, DATE_FORMATTER))
        .adaptersAsModules(adaptersAsModules != null && adaptersAsModules)
        .dependencyOverrides(dependencyOverrides)
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
    architecture.put("adaptersAsModules", config.adaptersAsModules());
    data.put("architecture", architecture);

    // Dependency overrides section (if present)
    if (config.dependencyOverrides() != null && !config.dependencyOverrides().isEmpty()) {
      data.put("dependencyOverrides", config.dependencyOverrides());
    }

    return data;
  }

  /**
   * Adds security warning comments above sensitive property sections.
   *
   * @param yamlContent the YAML content
   * @param overlay     the overlay map to check for sensitive properties
   * @return YAML content with security comments added
   */
  private String addSecurityComments(String yamlContent, Map<String, Object> overlay) {
    if (!containsSensitiveProperties(overlay)) {
      return yamlContent;
    }

    // Add security warning at the top if not already present
    if (!yamlContent.contains("WARNING") && !yamlContent.contains("Do not store credentials")) {
      yamlContent = SECURITY_WARNING_COMMENT + yamlContent;
    }

    return yamlContent;
  }

  /**
   * Checks if the map contains any sensitive properties.
   *
   * @param map the map to check
   * @return true if sensitive properties are found
   */
  private boolean containsSensitiveProperties(Map<String, Object> map) {
    for (String key : map.keySet()) {
      if (isSensitiveProperty(key)) {
        return true;
      }

      Object value = map.get(key);
      if (value instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> nestedMap = (Map<String, Object>) value;
        if (containsSensitiveProperties(nestedMap)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Checks if a property key is sensitive based on patterns.
   *
   * @param key the property key
   * @return true if the key matches sensitive patterns
   */
  private boolean isSensitiveProperty(String key) {
    if (key == null) {
      return false;
    }

    for (Pattern pattern : SENSITIVE_PROPERTY_PATTERNS) {
      if (pattern.matcher(key).matches()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Preserves comments from existing YAML content in the new content.
   * This is a best-effort approach as SnakeYAML doesn't preserve comments
   * natively.
   *
   * @param existingContent the original YAML content with comments
   * @param newContent      the new YAML content without comments
   * @return new content with preserved comments where possible
   */
  private String preserveComments(String existingContent, String newContent) {
    if (existingContent == null || existingContent.isEmpty()) {
      return newContent;
    }

    // Extract comments from existing content
    List<String> existingLines = existingContent.lines().toList();
    List<String> newLines = newContent.lines().toList();

    StringBuilder result = new StringBuilder();
    int existingIndex = 0;

    for (String newLine : newLines) {
      // Look for matching lines in existing content
      while (existingIndex < existingLines.size()) {
        String existingLine = existingLines.get(existingIndex);

        // If we find a comment in the existing content, preserve it
        if (existingLine.trim().startsWith("#")) {
          result.append(existingLine).append("\n");
          existingIndex++;
          continue;
        }

        // If lines match (ignoring whitespace differences), move to next
        if (linesMatchIgnoringWhitespace(existingLine, newLine)) {
          result.append(newLine).append("\n");
          existingIndex++;
          break;
        }

        // If existing line doesn't match, skip it (it was removed or changed)
        existingIndex++;
        break;
      }

      // If we didn't find a match, just add the new line
      if (existingIndex >= existingLines.size() ||
          !linesMatchIgnoringWhitespace(existingLines.get(existingIndex - 1), newLine)) {
        result.append(newLine).append("\n");
      }
    }

    return result.toString();
  }

  /**
   * Checks if two lines match, ignoring whitespace differences.
   *
   * @param line1 first line
   * @param line2 second line
   * @return true if lines match
   */
  private boolean linesMatchIgnoringWhitespace(String line1, String line2) {
    if (line1 == null || line2 == null) {
      return false;
    }

    String normalized1 = line1.trim().replaceAll("\\s+", " ");
    String normalized2 = line2.trim().replaceAll("\\s+", " ");

    return normalized1.equals(normalized2);
  }
}
