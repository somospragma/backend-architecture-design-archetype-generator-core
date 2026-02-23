package com.pragma.archetype.domain.model;

/**
 * Configuration for template repository.
 * Supports both production mode (GitHub) and developer mode (local path).
 */
public record TemplateConfig(
    TemplateMode mode,
    String repository,
    String branch,
    String version,
    String localPath,
    boolean cache) {

  /**
   * Creates default production configuration.
   */
  public static TemplateConfig defaultConfig() {
    return new TemplateConfig(
        TemplateMode.PRODUCTION,
        "https://github.com/somospragma/backend-architecture-design-archetype-generator-templates",
        "main",
        null,
        null,
        true);
  }

  /**
   * Creates developer configuration with local path.
   */
  public static TemplateConfig developerConfig(String localPath) {
    return new TemplateConfig(
        TemplateMode.DEVELOPER,
        null,
        null,
        null,
        localPath,
        false);
  }

  /**
   * Creates developer configuration with custom repository and branch.
   */
  public static TemplateConfig developerConfig(String repository, String branch) {
    return new TemplateConfig(
        TemplateMode.DEVELOPER,
        repository,
        branch,
        null,
        null,
        false);
  }

  /**
   * Checks if using local filesystem.
   */
  public boolean isLocalMode() {
    return mode == TemplateMode.DEVELOPER && localPath != null;
  }

  /**
   * Checks if using remote repository.
   */
  public boolean isRemoteMode() {
    return mode == TemplateMode.PRODUCTION || (mode == TemplateMode.DEVELOPER && localPath == null);
  }

  /**
   * Gets the effective branch to use.
   */
  public String getEffectiveBranch() {
    return version != null ? version : (branch != null ? branch : "main");
  }
}
