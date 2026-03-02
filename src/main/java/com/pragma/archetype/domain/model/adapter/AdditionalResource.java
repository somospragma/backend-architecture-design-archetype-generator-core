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
   * @param adapterName   The adapter name (for variable substitution)
   * @param resourcesPath The base resources path from architecture (e.g.,
   *                      "src/main/resources" or
   *                      "infrastructure/src/main/resources")
   * @return The resolved destination path relative to project root
   */
  public String resolveDestination(String adapterName, String resourcesPath) {
    // Build path: {resourcesPath}/{targetPath}/{name}
    String resolvedName = name.replace("{adapterName}", adapterName);

    if (targetPath == null || targetPath.isBlank()) {
      return resourcesPath + "/" + resolvedName;
    }

    return resourcesPath + "/" + targetPath + "/" + resolvedName;
  }

  /**
   * Resolves the full destination path for this resource using default resources
   * path.
   * 
   * @deprecated Use resolveDestination(String, String) with explicit
   *             resourcesPath
   * @param adapterName The adapter name (for variable substitution)
   * @return The resolved destination path relative to project root
   */
  @Deprecated
  public String resolveDestination(String adapterName) {
    return resolveDestination(adapterName, "src/main/resources");
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
