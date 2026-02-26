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
@Builder
public class AdapterMetadata {
  /**
   * Represents a dependency (Maven/Gradle artifact).
   * 
   * @deprecated Kept for backward compatibility. Use String dependencies in new
   *             code.
   */
  @Deprecated
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
   * 
   * @deprecated Kept for backward compatibility.
   */
  @Deprecated
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
   * List of runtime dependencies required by this adapter.
   * 
   * @deprecated Use String dependencies for new code. Kept for backward
   *             compatibility.
   */
  @Deprecated
  @Builder.Default
  List<Dependency> dependencies = Collections.emptyList();

  /**
   * List of test dependencies required by this adapter.
   * 
   * @deprecated Use String dependencies for new code. Kept for backward
   *             compatibility.
   */
  @Deprecated
  @Builder.Default
  List<Dependency> testDependencies = Collections.emptyList();

  /**
   * Application properties template path.
   * 
   * @deprecated Kept for backward compatibility.
   */
  @Deprecated
  String applicationPropertiesTemplate;

  /**
   * Configuration classes to generate.
   * 
   * @deprecated Kept for backward compatibility.
   */
  @Deprecated
  @Builder.Default
  List<ConfigurationClass> configurationClasses = Collections.emptyList();

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
   * 
   * @deprecated Kept for backward compatibility.
   */
  @Deprecated
  public boolean hasApplicationProperties() {
    return applicationPropertiesTemplate != null && !applicationPropertiesTemplate.isBlank();
  }

  /**
   * Checks if this adapter has configuration classes.
   * 
   * @deprecated Kept for backward compatibility.
   */
  @Deprecated
  public boolean hasConfigurationClasses() {
    return !configurationClasses.isEmpty();
  }

  /**
   * Checks if this adapter has test dependencies.
   * 
   * @deprecated Kept for backward compatibility.
   */
  @Deprecated
  public boolean hasTestDependencies() {
    return !testDependencies.isEmpty();
  }

  /**
   * Gets all dependencies (runtime + test).
   * 
   * @deprecated Kept for backward compatibility.
   */
  @Deprecated
  public List<Dependency> getAllDependencies() {
    return java.util.stream.Stream.concat(
        dependencies.stream(),
        testDependencies.stream()).toList();
  }

  /**
   * Validates that the metadata is complete and valid.
   * 
   * @deprecated Kept for backward compatibility.
   */
  @Deprecated
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
   * Gets the list of runtime dependencies.
   * Explicit getter for backward compatibility with legacy code.
   * 
   * @return List of dependencies
   * @deprecated Use the Lombok-generated getter instead
   */
  @Deprecated
  public List<Dependency> dependencies() {
    return dependencies;
  }

  /**
   * Gets the list of test dependencies.
   * Explicit getter for backward compatibility with legacy code.
   * 
   * @return List of test dependencies
   * @deprecated Use the Lombok-generated getter instead
   */
  @Deprecated
  public List<Dependency> testDependencies() {
    return testDependencies;
  }

  /**
   * Gets the list of configuration classes.
   * Explicit getter for backward compatibility with legacy code.
   * 
   * @return List of configuration classes
   * @deprecated Use the Lombok-generated getter instead
   */
  @Deprecated
  public List<ConfigurationClass> configurationClasses() {
    return configurationClasses;
  }

  /**
   * Gets the application properties template path.
   * Explicit getter for backward compatibility with legacy code.
   * 
   * @return Application properties template path
   * @deprecated Use the Lombok-generated getter instead
   */
  @Deprecated
  public String applicationPropertiesTemplate() {
    return applicationPropertiesTemplate;
  }
}
