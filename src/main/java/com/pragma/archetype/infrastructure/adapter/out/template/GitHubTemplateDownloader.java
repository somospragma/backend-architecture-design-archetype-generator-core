package com.pragma.archetype.infrastructure.adapter.out.template;

import com.pragma.archetype.domain.model.TemplateConfig;
import com.pragma.archetype.domain.port.out.HttpClientPort;

/**
 * Downloads templates from GitHub repository.
 * Supports GitHub, GitLab, and Bitbucket.
 */
public class GitHubTemplateDownloader {

  private final HttpClientPort httpClient;
  private final TemplateCache cache;

  public GitHubTemplateDownloader(HttpClientPort httpClient, TemplateCache cache) {
    this.httpClient = httpClient;
    this.cache = cache;
  }

  /**
   * Downloads a template file from the configured repository.
   *
   * @param config       template configuration
   * @param templatePath relative path to template (e.g.,
   *                     "templates/frameworks/spring/...")
   * @return template content
   * @throws HttpClientPort.HttpDownloadException if download fails or branch
   *                                              doesn't exist
   */
  public String downloadTemplate(TemplateConfig config, String templatePath) {
    // Build cache key
    String cacheKey = buildCacheKey(config, templatePath);

    // Check cache first if enabled
    if (config.cache()) {
      String cached = cache.get(cacheKey);
      if (cached != null) {
        return cached;
      }
    }

    // Download from remote
    String url = buildRawUrl(config.repository(), config.getEffectiveBranch(), templatePath);
    try {
      String content = httpClient.downloadContent(url);

      // Cache if enabled
      if (config.cache()) {
        cache.put(cacheKey, content);
      }

      return content;
    } catch (HttpClientPort.HttpDownloadException e) {
      // Enhance error message with branch information
      String branch = config.getEffectiveBranch();
      String enhancedMessage = String.format(
          "Failed to download template '%s' from branch '%s' in repository '%s'. " +
              "Please verify that the branch exists and the template path is correct. Error: %s",
          templatePath, branch, config.repository(), e.getMessage());
      throw new HttpClientPort.HttpDownloadException(enhancedMessage, e);
    }
  }

  /**
   * Checks if a template exists in the repository.
   *
   * @param config       template configuration
   * @param templatePath relative path to template
   * @return true if template exists, false otherwise
   */
  public boolean templateExists(TemplateConfig config, String templatePath) {
    // Check cache first if enabled
    if (config.cache()) {
      String cacheKey = buildCacheKey(config, templatePath);
      if (cache.exists(cacheKey)) {
        return true;
      }
    }

    // Check remote
    String url = buildRawUrl(config.repository(), config.getEffectiveBranch(), templatePath);
    return httpClient.isAccessible(url);
  }

  /**
   * Validates that the remote repository and branch are accessible.
   *
   * @param config template configuration
   * @return true if repository and branch are accessible, false otherwise
   */
  public boolean validateRemoteRepository(TemplateConfig config) {
    if (config.isLocalMode()) {
      return true; // Local mode doesn't need remote validation
    }

    // Try to access a common file that should exist in any branch (README.md or
    // similar)
    // If we can't access the repository at all, this will fail
    String testUrl = buildRawUrl(config.repository(), config.getEffectiveBranch(), "README.md");
    return httpClient.isAccessible(testUrl);
  }

  /**
   * Gets the cache instance.
   */
  public TemplateCache getCache() {
    return cache;
  }

  /**
   * Builds the raw content URL for different Git hosting services.
   *
   * @param repository repository URL
   * @param branch     branch or tag name
   * @param path       file path
   * @return raw content URL
   */
  private String buildRawUrl(String repository, String branch, String path) {
    // Normalize path (remove leading slash if present)
    String normalizedPath = path.startsWith("/") ? path.substring(1) : path;

    if (repository.contains("github.com")) {
      // GitHub: https://raw.githubusercontent.com/owner/repo/branch/path
      String repoPath = repository
          .replace("https://github.com/", "")
          .replace("http://github.com/", "")
          .replace(".git", "");
      return String.format("https://raw.githubusercontent.com/%s/%s/%s",
          repoPath, branch, normalizedPath);

    } else if (repository.contains("gitlab.com")) {
      // GitLab: https://gitlab.com/owner/repo/-/raw/branch/path
      String repoPath = repository
          .replace("https://gitlab.com/", "")
          .replace("http://gitlab.com/", "")
          .replace(".git", "");
      return String.format("https://gitlab.com/%s/-/raw/%s/%s",
          repoPath, branch, normalizedPath);

    } else if (repository.contains("bitbucket.org")) {
      // Bitbucket: https://bitbucket.org/owner/repo/raw/branch/path
      String repoPath = repository
          .replace("https://bitbucket.org/", "")
          .replace("http://bitbucket.org/", "")
          .replace(".git", "");
      return String.format("https://bitbucket.org/%s/raw/%s/%s",
          repoPath, branch, normalizedPath);

    } else {
      // Generic: assume GitHub-like structure
      return String.format("%s/raw/%s/%s", repository, branch, normalizedPath);
    }
  }

  /**
   * Builds a cache key for a template.
   *
   * @param config       template configuration
   * @param templatePath template path
   * @return cache key
   */
  private String buildCacheKey(TemplateConfig config, String templatePath) {
    String branch = config.getEffectiveBranch();
    return String.format("%s/%s", branch, templatePath);
  }
}
