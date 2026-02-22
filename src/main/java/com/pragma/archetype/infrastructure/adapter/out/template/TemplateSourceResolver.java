package com.pragma.archetype.infrastructure.adapter.out.template;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.pragma.archetype.domain.model.TemplateConfig;
import com.pragma.archetype.domain.model.TemplateMode;
import com.pragma.archetype.domain.model.TemplateSource;

/**
 * Resolves the source from which templates should be loaded.
 * Determines whether to use local filesystem or remote repository based on
 * configuration.
 * 
 * Resolution order:
 * 1. Check for configured localPath in TemplateConfig
 * 2. Auto-detect ../backend-architecture-design-archetype-generator-templates
 * 3. Fall back to remote repository with branch support
 */
public class TemplateSourceResolver {

  /**
   * Exception thrown when template source resolution or validation fails.
   */
  public static class TemplateSourceException extends RuntimeException {
    public TemplateSourceException(String message) {
      super(message);
    }

    public TemplateSourceException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  private static final String AUTO_DETECT_PATH = "../backend-architecture-design-archetype-generator-templates";
  private final TemplateConfig config;
  private final Path autoDetectPath;
  private TemplateSource resolvedSource;

  private Path resolvedLocalPath;

  /**
   * Creates a resolver with the given template configuration.
   *
   * @param config template configuration
   */
  public TemplateSourceResolver(TemplateConfig config) {
    this.config = config;
    this.autoDetectPath = Paths.get(AUTO_DETECT_PATH).toAbsolutePath().normalize();
  }

  /**
   * Resolves the template source based on configuration and filesystem state.
   * 
   * @return the resolved template source
   * @throws TemplateSourceException if configured local path does not exist
   */
  public TemplateSource resolveSource() {
    if (resolvedSource != null) {
      return resolvedSource;
    }

    // 1. Check for configured localPath
    if (config.localPath() != null && !config.localPath().isEmpty()) {
      Path configuredPath = Paths.get(config.localPath()).toAbsolutePath().normalize();

      if (!Files.exists(configuredPath)) {
        throw new TemplateSourceException(
            String.format("Configured local path does not exist: %s. " +
                "Please check the 'templates.localPath' configuration in .cleanarch.yml",
                configuredPath));
      }

      if (!Files.isDirectory(configuredPath)) {
        throw new TemplateSourceException(
            String.format("Configured local path is not a directory: %s. " +
                "Please ensure 'templates.localPath' points to a directory",
                configuredPath));
      }

      resolvedSource = TemplateSource.LOCAL_CONFIGURED;
      resolvedLocalPath = configuredPath;
      return resolvedSource;
    }

    // 2. Auto-detect ../backend-architecture-design-archetype-generator-templates
    // Only auto-detect in DEVELOPER mode when no repository is configured
    if (config.mode() == TemplateMode.DEVELOPER &&
        (config.repository() == null || config.repository().isEmpty()) &&
        Files.exists(autoDetectPath) &&
        Files.isDirectory(autoDetectPath)) {
      resolvedSource = TemplateSource.LOCAL_AUTO_DETECTED;
      resolvedLocalPath = autoDetectPath;
      return resolvedSource;
    }

    // 3. Fall back to remote repository
    resolvedSource = TemplateSource.REMOTE;
    resolvedLocalPath = null;
    return resolvedSource;
  }

  /**
   * Checks if the resolved source is local mode (configured or auto-detected).
   *
   * @return true if using local filesystem, false otherwise
   */
  public boolean isLocalMode() {
    if (resolvedSource == null) {
      resolveSource();
    }
    return resolvedSource == TemplateSource.LOCAL_CONFIGURED ||
        resolvedSource == TemplateSource.LOCAL_AUTO_DETECTED;
  }

  /**
   * Checks if the resolved source is remote mode.
   *
   * @return true if using remote repository, false otherwise
   */
  public boolean isRemoteMode() {
    if (resolvedSource == null) {
      resolveSource();
    }
    return resolvedSource == TemplateSource.REMOTE;
  }

  /**
   * Gets the resolved local path if in local mode.
   *
   * @return the local path, or null if in remote mode
   */
  public Path getLocalPath() {
    if (resolvedSource == null) {
      resolveSource();
    }
    return resolvedLocalPath;
  }

  /**
   * Gets the resolved template source.
   *
   * @return the resolved source
   */
  public TemplateSource getResolvedSource() {
    if (resolvedSource == null) {
      resolveSource();
    }
    return resolvedSource;
  }

  /**
   * Validates that the resolved source is accessible.
   * For local mode, checks that the path exists and is a directory.
   * For remote mode, validates that repository URL and branch are configured.
   *
   * @throws TemplateSourceException if validation fails
   */
  public void validateSource() {
    TemplateSource source = resolveSource();

    switch (source) {
      case LOCAL_CONFIGURED, LOCAL_AUTO_DETECTED:
        // Already validated during resolution
        break;

      case REMOTE:
        if (config.repository() == null || config.repository().isEmpty()) {
          throw new TemplateSourceException(
              "Remote mode requires a repository URL. " +
                  "Please configure 'templates.repository' in .cleanarch.yml");
        }

        String branch = config.getEffectiveBranch();
        if (branch == null || branch.isEmpty()) {
          throw new TemplateSourceException(
              "Remote mode requires a branch name. " +
                  "Please configure 'templates.branch' in .cleanarch.yml");
        }
        break;
    }
  }

  /**
   * Gets a description of the resolved source for logging.
   *
   * @return human-readable description of the template source
   */
  public String getSourceDescription() {
    TemplateSource source = resolveSource();

    switch (source) {
      case LOCAL_CONFIGURED:
        return String.format("Local templates (configured): %s", resolvedLocalPath);

      case LOCAL_AUTO_DETECTED:
        return String.format("Local templates (auto-detected): %s", resolvedLocalPath);

      case REMOTE:
        return String.format("Remote templates: %s (branch: %s)",
            config.repository(), config.getEffectiveBranch());

      default:
        return "Unknown source";
    }
  }
}
