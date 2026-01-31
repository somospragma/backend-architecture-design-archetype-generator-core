package com.pragma.archetype.infrastructure.adapter.out.template;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import com.pragma.archetype.domain.model.TemplateConfig;
import com.pragma.archetype.domain.port.out.HttpClientPort;
import com.pragma.archetype.domain.port.out.TemplateRepository;
import com.pragma.archetype.infrastructure.adapter.out.http.OkHttpClientAdapter;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Adapter for template processing using Freemarker.
 * Supports loading templates from:
 * - Local filesystem (developer mode with localPath)
 * - Remote GitHub repository (production mode or developer mode with
 * repository)
 * - Embedded resources (fallback)
 */
public class FreemarkerTemplateRepository implements TemplateRepository {

  /**
   * Exception thrown when template processing fails.
   */
  public static class TemplateProcessingException extends RuntimeException {
    public TemplateProcessingException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  private final Configuration freemarkerConfig;
  private final Path templatesBasePath;
  private final TemplateConfig templateConfig;
  private final GitHubTemplateDownloader downloader;

  /**
   * Creates a repository with templates from local filesystem.
   *
   * @param templatesBasePath base path where templates are located
   */
  public FreemarkerTemplateRepository(Path templatesBasePath) {
    this.templatesBasePath = templatesBasePath;
    this.templateConfig = null;
    this.downloader = null;
    this.freemarkerConfig = createFreemarkerConfiguration();
  }

  /**
   * Creates a repository with template configuration.
   * Supports local path, remote repository, or embedded templates.
   *
   * @param templateConfig template configuration
   */
  public FreemarkerTemplateRepository(TemplateConfig templateConfig) {
    this.templateConfig = templateConfig;

    // If local mode, use local path
    if (templateConfig.isLocalMode()) {
      this.templatesBasePath = Paths.get(templateConfig.localPath());
      this.downloader = null;
    } else {
      // Remote mode - use downloader
      this.templatesBasePath = null;
      HttpClientPort httpClient = new OkHttpClientAdapter();
      TemplateCache cache = new TemplateCache();
      this.downloader = new GitHubTemplateDownloader(httpClient, cache);
    }

    this.freemarkerConfig = createFreemarkerConfiguration();
  }

  /**
   * Creates a repository with templates from a URL.
   * For Phase 1, we'll use local templates. Remote loading will be added later.
   *
   * @param templatesUrl URL to templates repository
   */
  public FreemarkerTemplateRepository(String templatesUrl) {
    // For now, assume templates are in a local directory
    // In future phases, we'll download from GitHub
    this.templatesBasePath = Paths.get("templates");
    this.templateConfig = null;
    this.downloader = null;
    this.freemarkerConfig = createFreemarkerConfiguration();
  }

  @Override
  public String processTemplate(String templatePath, Map<String, Object> context) {
    try {
      // Get template
      Template template = getTemplate(templatePath);

      // Process template with context
      StringWriter writer = new StringWriter();
      template.process(context, writer);

      return writer.toString();

    } catch (IOException e) {
      throw new TemplateProcessingException(
          "Failed to load template: " + templatePath,
          e);
    } catch (TemplateException e) {
      throw new TemplateProcessingException(
          "Failed to process template: " + templatePath,
          e);
    }
  }

  @Override
  public boolean templateExists(String templatePath) {
    // Check local filesystem first
    if (templatesBasePath != null) {
      Path fullPath = templatesBasePath.resolve(templatePath);
      if (Files.exists(fullPath)) {
        return true;
      }
    }

    // Check remote if configured
    if (downloader != null && templateConfig != null) {
      try {
        return downloader.templateExists(templateConfig, templatePath);
      } catch (Exception e) {
        // Continue to check embedded
      }
    }

    // Check embedded resources
    var resource = getClass().getClassLoader().getResourceAsStream("templates/" + templatePath);
    return resource != null;
  }

  @Override
  public String getTemplateContent(String templatePath) {
    // Try local filesystem first
    if (templatesBasePath != null) {
      try {
        Path fullPath = templatesBasePath.resolve(templatePath);
        if (Files.exists(fullPath)) {
          return Files.readString(fullPath);
        }
      } catch (IOException e) {
        // Continue to try other methods
      }
    }

    // Try remote download if configured
    if (downloader != null && templateConfig != null) {
      try {
        return downloader.downloadTemplate(templateConfig, templatePath);
      } catch (Exception e) {
        // Continue to try embedded
      }
    }

    // Try embedded resources as fallback
    try {
      var resource = getClass().getClassLoader().getResourceAsStream("templates/" + templatePath);
      if (resource != null) {
        return new String(resource.readAllBytes());
      }
    } catch (IOException e) {
      // Fall through to exception
    }

    throw new TemplateProcessingException(
        "Template not found: " + templatePath,
        null);
  }

  /**
   * Gets the downloader instance (for testing or cache management).
   */
  public GitHubTemplateDownloader getDownloader() {
    return downloader;
  }

  /**
   * Gets a Freemarker template.
   */
  private Template getTemplate(String templatePath) throws IOException {
    // Get template content from appropriate source
    String content = getTemplateContent(templatePath);

    // Create template from string content
    return new Template(templatePath, content, freemarkerConfig);
  }

  /**
   * Creates and configures Freemarker.
   */
  private Configuration createFreemarkerConfiguration() {
    Configuration config = new Configuration(Configuration.VERSION_2_3_32);

    try {
      // Set template directory if using local filesystem
      if (templatesBasePath != null && Files.exists(templatesBasePath)) {
        config.setDirectoryForTemplateLoading(templatesBasePath.toFile());
      }

      // Set default encoding
      config.setDefaultEncoding("UTF-8");

      // Set template exception handler
      config.setTemplateExceptionHandler(
          freemarker.template.TemplateExceptionHandler.RETHROW_HANDLER);

      // Don't log exceptions
      config.setLogTemplateExceptions(false);

      // Wrap unchecked exceptions
      config.setWrapUncheckedExceptions(true);

    } catch (IOException e) {
      throw new RuntimeException("Failed to configure Freemarker", e);
    }

    return config;
  }
}
