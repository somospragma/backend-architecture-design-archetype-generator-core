package com.pragma.archetype.domain.model.template;

/**
 * Constants for template repository structure paths.
 * These paths represent the fixed structure of the template repository,
 * NOT the structure of generated projects (which is configurable via
 * structure.yml).
 */
public final class TemplateRepositoryPaths {

  /**
   * Directory names for adapter types in the template repository.
   * These are fixed conventions for organizing templates.
   */
  public static final class AdapterDirectories {
    /**
     * Directory for input/driving adapters (REST, GraphQL, gRPC, etc.)
     */
    public static final String INPUT_ADAPTERS = "entry-points";

    /**
     * Directory for output/driven adapters (MongoDB, PostgreSQL, Redis, etc.)
     */
    public static final String OUTPUT_ADAPTERS = "driven-adapters";

    /**
     * Gets the adapter directory name for the given direction.
     *
     * @param isInput true for input adapters, false for output adapters
     * @return the directory name in the template repository
     */
    public static String getDirectoryName(boolean isInput) {
      return isInput ? INPUT_ADAPTERS : OUTPUT_ADAPTERS;
    }

    private AdapterDirectories() {
      throw new UnsupportedOperationException("Utility class");
    }
  }

  /**
   * Path patterns for template repository structure.
   */
  public static final class Patterns {
    /**
     * Pattern for adapter index.json path.
     * Format: frameworks/{framework}/{paradigm}/adapters/{adapterDir}/index.json
     */
    public static final String ADAPTER_INDEX = "frameworks/%s/%s/adapters/%s/index.json";

    /**
     * Pattern for adapter metadata.yml path.
     * Format:
     * frameworks/{framework}/{paradigm}/adapters/{adapterDir}/{adapterName}/metadata.yml
     */
    public static final String ADAPTER_METADATA = "frameworks/%s/%s/adapters/%s/%s/metadata.yml";

    /**
     * Pattern for adapter template directory.
     * Format: frameworks/{framework}/{paradigm}/adapters/{adapterDir}/{adapterName}
     */
    public static final String ADAPTER_TEMPLATE_DIR = "frameworks/%s/%s/adapters/%s/%s";

    /**
     * Pattern for generic adapter templates.
     * Format:
     * frameworks/{framework}/{paradigm}/adapters/{adapterDir}/generic/{templateName}
     */
    public static final String GENERIC_ADAPTER_TEMPLATE = "frameworks/%s/%s/adapters/%s/generic/%s";

    private Patterns() {
      throw new UnsupportedOperationException("Utility class");
    }
  }

  private TemplateRepositoryPaths() {
    throw new UnsupportedOperationException("Utility class");
  }
}
