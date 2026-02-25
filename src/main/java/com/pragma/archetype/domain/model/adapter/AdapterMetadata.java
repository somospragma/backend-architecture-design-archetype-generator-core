package com.pragma.archetype.domain.model.adapter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.pragma.archetype.domain.model.validation.ValidationResult;

/**
 * Metadata describing an adapter's properties and dependencies.
 * Loaded from metadata.yml files in adapter templates.
 */
public record AdapterMetadata(
    String name,
    String type,
    String description,
    List<Dependency> dependencies,
    List<Dependency> testDependencies,
    String applicationPropertiesTemplate,
    List<ConfigurationClass> configurationClasses) {

  public AdapterMetadata {
    Objects.requireNonNull(name, "Adapter name cannot be null");
    Objects.requireNonNull(type, "Adapter type cannot be null");
    dependencies = dependencies != null ? Collections.unmodifiableList(dependencies) : List.of();
    testDependencies = testDependencies != null ? Collections.unmodifiableList(testDependencies) : List.of();
    configurationClasses = configurationClasses != null ? Collections.unmodifiableList(configurationClasses)
        : List.of();
  }

  /**
   * Checks if this adapter has application properties template.
   */
  public boolean hasApplicationProperties() {
    return applicationPropertiesTemplate != null &&
        !applicationPropertiesTemplate.isBlank();
  }

  /**
   * Checks if this adapter has configuration classes.
   */
  public boolean hasConfigurationClasses() {
    return !configurationClasses.isEmpty();
  }

  /**
   * Checks if this adapter has test dependencies.
   */
  public boolean hasTestDependencies() {
    return !testDependencies.isEmpty();
  }

  /**
   * Gets all dependencies (runtime + test).
   */
  public List<Dependency> getAllDependencies() {
    return java.util.stream.Stream.concat(
        dependencies.stream(),
        testDependencies.stream()).toList();
  }

  /**
   * Validates that the metadata is complete and valid.
   */
  public ValidationResult validate() {
    if (name == null || name.isBlank()) {
      return ValidationResult.failure("Adapter name cannot be empty");
    }
    if (type == null || type.isBlank()) {
      return ValidationResult.failure("Adapter type cannot be empty");
    }
    return ValidationResult.success();
  }

  /**
   * Represents a dependency (Maven/Gradle artifact).
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
}
