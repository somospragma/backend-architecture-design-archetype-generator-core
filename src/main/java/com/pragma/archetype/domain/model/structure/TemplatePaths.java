package com.pragma.archetype.domain.model.structure;

import java.util.Map;

import lombok.Builder;
import lombok.Value;

/**
 * Configuration for template path resolution with variable substitution.
 * Loaded from structure.yml templatePaths section.
 * Enables dynamic template location without hardcoding paths in Java code.
 */
@Value
@Builder
public class TemplatePaths {
  private static final String DEFAULT_INPUT_ADAPTER_PATH = "frameworks/{framework}/{paradigm}/adapters/{adapterDir}/{type}";
  private static final String DEFAULT_OUTPUT_ADAPTER_PATH = "frameworks/{framework}/{paradigm}/adapters/{adapterDir}/{type}";
  private static final String DEFAULT_ENTITY_PATH = "architectures/{architecture}/domain/entity";
  private static final String DEFAULT_USE_CASE_PATH = "architectures/{architecture}/application/usecase";
  private static final String DEFAULT_CONFIG_PATH = "architectures/{architecture}/project";

  /**
   * Template path pattern for input adapters.
   * Variables: {framework}, {paradigm}, {architecture}, {type}, {adapterDir}
   */
  @Builder.Default
  String inputAdapter = DEFAULT_INPUT_ADAPTER_PATH;

  /**
   * Template path pattern for output adapters.
   * Variables: {framework}, {paradigm}, {architecture}, {type}, {adapterDir}
   */
  @Builder.Default
  String outputAdapter = DEFAULT_OUTPUT_ADAPTER_PATH;

  /**
   * Template path pattern for entities.
   * Variables: {architecture}
   */
  @Builder.Default
  String entity = DEFAULT_ENTITY_PATH;

  /**
   * Template path pattern for use cases.
   * Variables: {architecture}
   */
  @Builder.Default
  String useCase = DEFAULT_USE_CASE_PATH;

  /**
   * Template path pattern for configuration files.
   * Variables: {architecture}
   */
  @Builder.Default
  String config = DEFAULT_CONFIG_PATH;

  /**
   * Resolves input adapter template path with variable substitution.
   *
   * @param framework    Framework name (e.g., "spring", "quarkus")
   * @param paradigm     Paradigm name (e.g., "imperative", "reactive")
   * @param architecture Architecture name (e.g., "hexagonal-single")
   * @param type         Adapter type (e.g., "rest", "graphql")
   * @param adapterDir   Adapter directory name (e.g., "entry-points", "inputs")
   * @return Resolved template path
   */
  public String resolveInputAdapterPath(
      String framework,
      String paradigm,
      String architecture,
      String type,
      String adapterDir) {
    return inputAdapter
        .replace("{framework}", framework.toLowerCase())
        .replace("{paradigm}", paradigm.toLowerCase())
        .replace("{architecture}", architecture.toLowerCase())
        .replace("{type}", type.toLowerCase())
        .replace("{adapterDir}", adapterDir);
  }

  /**
   * Resolves output adapter template path with variable substitution.
   *
   * @param framework    Framework name (e.g., "spring", "quarkus")
   * @param paradigm     Paradigm name (e.g., "imperative", "reactive")
   * @param architecture Architecture name (e.g., "hexagonal-single")
   * @param type         Adapter type (e.g., "postgresql", "redis")
   * @param adapterDir   Adapter directory name (e.g., "driven-adapters",
   *                     "outputs")
   * @return Resolved template path
   */
  public String resolveOutputAdapterPath(
      String framework,
      String paradigm,
      String architecture,
      String type,
      String adapterDir) {
    return outputAdapter
        .replace("{framework}", framework.toLowerCase())
        .replace("{paradigm}", paradigm.toLowerCase())
        .replace("{architecture}", architecture.toLowerCase())
        .replace("{type}", type.toLowerCase())
        .replace("{adapterDir}", adapterDir);
  }

  /**
   * Resolves entity template path with variable substitution.
   *
   * @param architecture Architecture name (e.g., "hexagonal-single")
   * @return Resolved template path
   */
  public String resolveEntityPath(String architecture) {
    return entity.replace("{architecture}", architecture.toLowerCase());
  }

  /**
   * Resolves use case template path with variable substitution.
   *
   * @param architecture Architecture name (e.g., "hexagonal-single")
   * @return Resolved template path
   */
  public String resolveUseCasePath(String architecture) {
    return useCase.replace("{architecture}", architecture.toLowerCase());
  }

  /**
   * Resolves config template path with variable substitution.
   *
   * @param architecture Architecture name (e.g., "hexagonal-single")
   * @return Resolved template path
   */
  public String resolveConfigPath(String architecture) {
    return config.replace("{architecture}", architecture.toLowerCase());
  }

  /**
   * Resolves a template path with generic variable substitution.
   *
   * @param templateType The type of template to resolve
   * @param variables    Map of variables to substitute
   * @return Resolved template path
   * @throws IllegalArgumentException if templateType is unknown
   * @throws IllegalStateException    if variables are unresolved
   */
  public String resolve(String templateType, Map<String, String> variables) {
    var pattern = getPatternForType(templateType);
    if (pattern == null) {
      throw new IllegalArgumentException("Unknown template type: " + templateType);
    }

    var result = pattern;
    for (var entry : variables.entrySet()) {
      result = result.replace("{" + entry.getKey() + "}", entry.getValue());
    }

    // Check for unresolved variables
    if (result.contains("{")) {
      throw new IllegalStateException(
          String.format(
              com.pragma.archetype.domain.model.error.StructureErrorMessages.UNRESOLVED_VARIABLES,
              result));
    }

    return result;
  }

  /**
   * Gets the pattern for a given template type.
   *
   * @param templateType The template type
   * @return The pattern or null if unknown
   */
  private String getPatternForType(String templateType) {
    return switch (templateType.toLowerCase()) {
      case "inputadapter", "input_adapter" -> inputAdapter;
      case "outputadapter", "output_adapter" -> outputAdapter;
      case "entity" -> entity;
      case "usecase", "use_case" -> useCase;
      case "config" -> config;
      default -> null;
    };
  }
}
