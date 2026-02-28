package com.pragma.archetype.domain.model.adapter;

/**
 * Represents an additional resource file to generate with an adapter.
 * Similar to StaticFile but for adapter-specific resources like GraphQL
 * schemas,
 * OpenAPI specs, configuration files, etc.
 * 
 * Examples:
 * - GraphQL schema files (schema.graphqls)
 * - OpenAPI specifications (openapi.yaml)
 * - Configuration files (application-adapter.yml)
 */
public record AdditionalResource(
    String name,
    String targetPath,
    String templatePath,
    boolean processAsTemplate) {

  /**
   * Creates an additional resource that should be processed as a template.
   */
  public static AdditionalResource template(String name, String targetPath, String templatePath) {
    return new AdditionalResource(name, targetPath, templatePath, true);
  }

  /**
   * Creates an additional resource that should be copied as-is.
   */
  public static AdditionalResource staticFile(String name, String targetPath, String templatePath) {
    return new AdditionalResource(name, targetPath, templatePath, false);
  }

  /**
   * Resolves the full destination path for this resource.
   * 
   * @param adapterName The adapter name (for variable substitution)
   * @return The resolved destination path relative to project root
   */
  public String resolveDestination(String adapterName) {
    // Build path: src/main/resources/{targetPath}/{name}
    String resolvedName = name.replace("{adapterName}", adapterName);

    if (targetPath == null || targetPath.isBlank()) {
      return "src/main/resources/" + resolvedName;
    }

    return "src/main/resources/" + targetPath + "/" + resolvedName;
  }

  /**
   * Checks if this resource should be processed as a FreeMarker template.
   */
  public boolean shouldProcessAsTemplate() {
    // If explicitly set, use that value
    if (processAsTemplate) {
      return true;
    }

    // Otherwise, check if template path ends with .ftl
    return templatePath != null && templatePath.endsWith(".ftl");
  }
}
