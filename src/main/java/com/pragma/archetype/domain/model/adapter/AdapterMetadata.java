package com.pragma.archetype.domain.model.adapter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import lombok.Builder;
import lombok.Value;

/**
 * Metadata describing an adapter type loaded from metadata.yml.
 * Replaces hardcoded enums (InputAdapterType, AdapterType) with dynamic
 * discovery.
 */
@Value
@Builder(toBuilder = true)
public class AdapterMetadata {
  /**
   * Represents a dependency (Maven/Gradle artifact).
   * Used for dependencies that need scope information (test, compile, runtime).
   */
  public record Dependency(
      String group,
      String artifact,
      String version,
      String scope) {

    public Dependency {
      Objects.requireNonNull(group, "Dependency group cannot be null");
      Objects.requireNonNull(artifact, "Dependency artifact cannot be null");
    }

    /**
     * Creates a compile-scope dependency.
     */
    public static Dependency compile(String group, String artifact, String version) {
      return new Dependency(group, artifact, version, "compile");
    }

    /**
     * Creates a test-scope dependency.
     */
    public static Dependency test(String group, String artifact, String version) {
      return new Dependency(group, artifact, version, "test");
    }

    /**
     * Checks if this is a test dependency.
     */
    public boolean isTestDependency() {
      return "test".equalsIgnoreCase(scope);
    }

    /**
     * Gets the Maven coordinate string.
     */
    public String toCoordinate() {
      return String.format("%s:%s:%s", group, artifact, version);
    }
  }

  /**
   * Represents a configuration class to generate.
   * Contains metadata for generating Spring/framework configuration classes.
   */
  public record ConfigurationClass(
      String name,
      String packagePath,
      String templatePath) {

    public ConfigurationClass {
      Objects.requireNonNull(name, "Configuration class name cannot be null");
      Objects.requireNonNull(packagePath, "Package path cannot be null");
      Objects.requireNonNull(templatePath, "Template path cannot be null");
    }

    /**
     * Gets the fully qualified class name.
     */
    public String getFullyQualifiedName(String basePackage) {
      return basePackage + "." + packagePath + "." + name;
    }
  }

  /**
   * Represents a Gradle plugin required by an adapter.
   */
  public record BuildPlugin(String id, String version) {

    public BuildPlugin {
      Objects.requireNonNull(id, "Plugin id cannot be null");
      Objects.requireNonNull(version, "Plugin version cannot be null");
    }

    /**
     * Converts to Gradle plugin declaration.
     * Example: id("com.google.protobuf") version "0.9.4"
     */
    public String toGradleDeclaration() {
      return String.format("    id(\"%s\") version \"%s\"", id, version);
    }
  }

  /**
   * Adapter type identifier (e.g., "rest", "kafka-consumer", "postgresql").
   * Used in command line: --type=rest
   */
  String name;

  /**
   * Adapter direction (INPUT or OUTPUT).
   */
  AdapterDirection direction;

  /**
   * Human-readable display name (e.g., "REST API", "Kafka Consumer").
   */
  String displayName;

  /**
   * Description of what this adapter does.
   */
  String description;

  /**
   * List of required parameter names (e.g., "useCase", "endpoints").
   * Empty list means no required parameters.
   */
  @Builder.Default
  List<String> requiredParameters = Collections.emptyList();

  /**
   * List of optional parameter names (e.g., "authentication", "cors").
   */
  @Builder.Default
  List<String> optionalParameters = Collections.emptyList();

  /**
   * List of template files to process for this adapter.
   */
  @Builder.Default
  List<TemplateMapping> templates = Collections.emptyList();

  /**
   * List of discovered template files (*.ftl) in adapter directory.
   * Used for automatic template discovery.
   */
  @Builder.Default
  List<TemplateFile> discoveredTemplates = Collections.emptyList();

  /**
   * Whether this adapter requires a use case parameter.
   * Used for validation in task classes.
   */
  @Builder.Default
  boolean requiresUseCase = false;

  /**
   * Whether this adapter requires endpoints parameter.
   * Used for validation in task classes.
   */
  @Builder.Default
  boolean requiresEndpoints = false;

  /**
   * Whether this adapter needs an entity mapper.
   * Used to determine if mapper and data entity should be generated.
   */
  @Builder.Default
  boolean needsMapper = false;

  /**
   * Adapter status from index.json: stable, beta, experimental, deprecated.
   */
  String status;

  /**
   * Adapter version from index.json.
   */
  String version;

  /**
   * Class name suffix for generated classes (e.g., "Controller", "Repository").
   */
  String suffix;

  /**
   * Application properties template file path.
   */
  String applicationPropertiesTemplate;

  /**
   * List of dependencies required by this adapter (Maven/Gradle coordinates).
   * Example: ["org.springframework.boot:spring-boot-starter-web"]
   */
  @Builder.Default
  List<String> dependencies = Collections.emptyList();

  /**
   * List of runtime dependencies required by this adapter as Dependency records.
   * Includes group, artifact, version, and scope information.
   */
  @Builder.Default
  List<Dependency> runtimeDependencies = Collections.emptyList();

  /**
   * List of test dependencies required by this adapter.
   * Uses Dependency record to maintain scope information.
   */
  @Builder.Default
  List<Dependency> testDependencies = Collections.emptyList();

  /**
   * Additional resource files to generate with this adapter.
   * Examples: GraphQL schemas, OpenAPI specs, configuration files.
   */
  @Builder.Default
  List<AdditionalResource> additionalResources = Collections.emptyList();

  /**
   * Build plugins required by this adapter.
   * Example: Protobuf plugin for gRPC
   */
  @Builder.Default
  List<BuildPlugin> buildPlugins = Collections.emptyList();

  /**
   * Build configuration to add to build.gradle.kts.
   * Can use FreeMarker variables like ${resourcesPath}
   */
  String buildConfiguration;

  /**
   * Fallback application properties template path.
   * Used when applicationPropertiesTemplate is not set directly.
   */
  String legacyApplicationPropertiesTemplate;

  /**
   * Configuration classes to generate.
   * List of Spring/framework configuration classes with their template paths.
   */
  @Builder.Default
  List<ConfigurationClass> configurationClasses = Collections.emptyList();

  /**
   * Gets the class name with suffix applied.
   *
   * @param baseName The base name for the class
   * @return The class name with suffix if configured, otherwise baseName
   */
  public String getClassName(String baseName) {
    if (suffix == null || suffix.isBlank()) {
      return baseName;
    }
    return baseName + suffix;
  }

  /**
   * Checks if a parameter is required for this adapter.
   *
   * @param parameterName The parameter name to check
   * @return true if the parameter is required
   */
  public boolean isParameterRequired(String parameterName) {
    return requiredParameters.contains(parameterName);
  }

  /**
   * Checks if a parameter is optional for this adapter.
   *
   * @param parameterName The parameter name to check
   * @return true if the parameter is optional
   */
  public boolean isParameterOptional(String parameterName) {
    return optionalParameters.contains(parameterName);
  }

  /**
   * Checks if this adapter requires any parameters.
   *
   * @return true if requiredParameters is not empty
   */
  public boolean hasRequiredParameters() {
    return !requiredParameters.isEmpty();
  }

  /**
   * Gets the full identifier for this adapter (direction + name).
   *
   * @return String like "INPUT:rest" or "OUTPUT:postgresql"
   */
  public String getFullIdentifier() {
    return direction.name() + ":" + name;
  }

  /**
   * Checks if this adapter has application properties template.
   */
  public boolean hasApplicationProperties() {
    return getEffectiveApplicationPropertiesTemplate() != null
        && !getEffectiveApplicationPropertiesTemplate().isBlank();
  }

  /**
   * Checks if this adapter has configuration classes.
   * 
   * @return true if configurationClasses is not empty
   */
  public boolean hasConfigurationClasses() {
    return !configurationClasses.isEmpty();
  }

  /**
   * Checks if this adapter has test dependencies.
   * 
   * @return true if testDependencies is not empty
   */
  public boolean hasTestDependencies() {
    return !testDependencies.isEmpty();
  }

  /**
   * Checks if this adapter has additional resources to generate.
   */
  public boolean hasAdditionalResources() {
    return !additionalResources.isEmpty();
  }

  /**
   * Checks if this adapter has build plugins.
   */
  public boolean hasBuildPlugins() {
    return !buildPlugins.isEmpty();
  }

  /**
   * Checks if this adapter has build configuration.
   */
  public boolean hasBuildConfiguration() {
    return buildConfiguration != null && !buildConfiguration.isBlank();
  }

  /**
   * Validates that the metadata is complete and valid.
   *
   * @return ValidationResult with success or failure
   */
  public com.pragma.archetype.domain.model.validation.ValidationResult validate() {
    if (name == null || name.isBlank()) {
      return com.pragma.archetype.domain.model.validation.ValidationResult.failure("Adapter name cannot be empty");
    }
    if (direction == null) {
      return com.pragma.archetype.domain.model.validation.ValidationResult.failure("Adapter direction cannot be null");
    }
    return com.pragma.archetype.domain.model.validation.ValidationResult.success();
  }

  /**
   * Gets the application properties template path with fallback to legacy.
   * This is the only custom getter needed because it has special logic.
   * 
   * @return Application properties template path
   */
  public String getEffectiveApplicationPropertiesTemplate() {
    return applicationPropertiesTemplate != null ? applicationPropertiesTemplate : legacyApplicationPropertiesTemplate;
  }
}
