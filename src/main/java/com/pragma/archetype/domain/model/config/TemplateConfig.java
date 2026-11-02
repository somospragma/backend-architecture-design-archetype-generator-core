package com.pragma.archetype.domain.model.config;

import com.pragma.archetype.domain.model.template.TemplatesContract;

/**
 * Configuration for template repository.
 * Supports both production mode (GitHub) and developer mode (local path).
 * Without an explicit branch or version, the plugin reads the templates branch
 * of its major version (see {@link TemplatesContract}).
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
        null,
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
   * Gets the effective branch to use: the pinned version, the configured
   * branch, or the branch of this plugin's major version.
   */
  public String getEffectiveBranch() {
    if (version != null) {
      return version;
    }
    return branch != null ? branch : TemplatesContract.branchFor(PluginVersion.current());
  }
}
