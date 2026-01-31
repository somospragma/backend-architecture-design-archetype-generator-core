package com.pragma.archetype.infrastructure.adapter.out.template;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import com.pragma.archetype.domain.port.out.TemplateRepository;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Adapter for template processing using Freemarker.
 * Can load templates from local filesystem or remote repository.
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

  /**
   * Creates a repository with templates from local filesystem.
   *
   * @param templatesBasePath base path where templates are located
   */
  public FreemarkerTemplateRepository(Path templatesBasePath) {
    this.templatesBasePath = templatesBasePath;
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
    Path fullPath = templatesBasePath.resolve(templatePath);
    return Files.exists(fullPath);
  }

  @Override
  public String getTemplateContent(String templatePath) {
    try {
      Path fullPath = templatesBasePath.resolve(templatePath);
      return Files.readString(fullPath);
    } catch (IOException e) {
      throw new TemplateProcessingException(
          "Failed to read template: " + templatePath,
          e);
    }
  }

  /**
   * Gets a Freemarker template.
   */
  private Template getTemplate(String templatePath) throws IOException {
    // For embedded templates, we need to handle the path differently
    if (isEmbeddedTemplate(templatePath)) {
      return loadEmbeddedTemplate(templatePath);
    }

    // For file-based templates
    return freemarkerConfig.getTemplate(templatePath);
  }

  /**
   * Checks if template is embedded in the JAR.
   */
  private boolean isEmbeddedTemplate(String templatePath) {
    // Check if template exists in filesystem first
    Path fullPath = templatesBasePath.resolve(templatePath);
    return !Files.exists(fullPath);
  }

  /**
   * Loads template from classpath (embedded in JAR).
   */
  private Template loadEmbeddedTemplate(String templatePath) throws IOException {
    // Try to load from classpath
    var resource = getClass().getClassLoader().getResourceAsStream("templates/" + templatePath);

    if (resource == null) {
      throw new IOException("Template not found: " + templatePath);
    }

    String content = new String(resource.readAllBytes());
    return new Template(templatePath, content, freemarkerConfig);
  }

  /**
   * Creates and configures Freemarker.
   */
  private Configuration createFreemarkerConfiguration() {
    Configuration config = new Configuration(Configuration.VERSION_2_3_32);

    try {
      // Set template directory
      if (Files.exists(templatesBasePath)) {
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
