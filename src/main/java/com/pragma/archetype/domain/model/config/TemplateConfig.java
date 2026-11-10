package com.pragma.archetype.domain.model.config;

import com.pragma.archetype.domain.model.template.TemplatesContract;

/**
 * Configuration for template repository.
 * Supports both production mode (GitHub) and developer mode (local path).
 * Without an explicit version (a templates tag) or branch, the plugin reads the
 * latest release tag of its major version (see {@link TemplatesContract}),
 * which the infrastructure resolves and pins with {@link #withVersion(String)}.
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
   * True when the remote templates ref still has to be resolved to the latest
   * release tag of this plugin's major: no version or branch is configured.
   */
  public boolean needsLatestTag() {
    return isRemoteMode() && version == null && branch == null
        && TemplatesContract.majorOf(PluginVersion.current()).isPresent();
  }

  /**
   * Same configuration pinned to a templates tag.
   *
   * @param tag templates tag, such as "v1.2.0"
   */
  public TemplateConfig withVersion(String tag) {
    return new TemplateConfig(mode, repository, branch, tag, localPath, cache);
  }

  /**
   * Gets the git ref to read: the pinned version (a tag), the configured branch,
   * or the default branch for local plugin builds.
   *
   * @throws IllegalStateException when the latest tag has not been resolved yet
   */
  public String getEffectiveBranch() {
    if (version != null) {
      return version;
    }
    if (branch != null) {
      return branch;
    }
    if (needsLatestTag()) {
      throw new IllegalStateException(
          "The latest templates tag has not been resolved; use TemplateRepositoryFactory");
    }
    return TemplatesContract.DEFAULT_BRANCH;
  }

  /**
   * Human readable ref for logs, available before the latest tag is resolved.
   */
  public String describeRef() {
    if (needsLatestTag()) {
      return "latest v" + TemplatesContract.majorOf(PluginVersion.current()).getAsInt() + ".x.y tag";
    }
    return getEffectiveBranch();
  }
}
