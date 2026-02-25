package com.pragma.archetype.infrastructure.adapter.out.template;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pragma.archetype.domain.model.StructureMetadata;
import com.pragma.archetype.domain.model.ValidationResult;
import com.pragma.archetype.domain.model.config.TemplateConfig;
import com.pragma.archetype.domain.model.project.ArchitectureType;
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
public class FreemarkerTemplateRepository implements TemplateRepository, TemplateContentProvider {

  /**
   * Exception thrown when template processing fails.
   */
  public static class TemplateProcessingException extends RuntimeException {
    public TemplateProcessingException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  private static final Logger logger = LoggerFactory.getLogger(FreemarkerTemplateRepository.class);

  private final Configuration freemarkerConfig;
  private final Path templatesBasePath;
  private final TemplateConfig templateConfig;
  private final GitHubTemplateDownloader downloader;
  private final StructureMetadataLoader structureMetadataLoader;
  private AdapterMetadataLoader adapterMetadataLoader;
  private final TemplateSourceResolver sourceResolver;

  /**
   * Creates a repository with templates from local filesystem.
   *
   * @param templatesBasePath base path where templates are located
   */
  public FreemarkerTemplateRepository(Path templatesBasePath) {
    this.templatesBasePath = templatesBasePath;
    this.templateConfig = null;
    this.downloader = null;
    this.sourceResolver = null;
    this.freemarkerConfig = createFreemarkerConfiguration();
    this.structureMetadataLoader = new StructureMetadataLoader(this);
  }

  /**
   * Creates a repository with template configuration.
   * Supports local path, remote repository, or embedded templates.
   * Uses TemplateSourceResolver to determine template source and configure
   * caching.
   *
   * @param templateConfig template configuration
   */
  public FreemarkerTemplateRepository(TemplateConfig templateConfig) {
    this.templateConfig = templateConfig;
    this.sourceResolver = new TemplateSourceResolver(templateConfig);

    // Resolve template source
    this.sourceResolver.resolveSource();
    this.sourceResolver.validateSource();

    // Log template source mode on initialization
    logger.info("Template source: {}", sourceResolver.getSourceDescription());

    // Configure based on resolved source
    if (sourceResolver.isLocalMode()) {
      // Local mode - use local path
      this.templatesBasePath = sourceResolver.getLocalPath();
      this.downloader = null;
      logger.info("Local mode active - caching disabled for hot reload");
    } else {
      // Remote mode - use downloader with caching
      this.templatesBasePath = null;
      HttpClientPort httpClient = new OkHttpClientAdapter();
      TemplateCache cache = new TemplateCache();
      this.downloader = new GitHubTemplateDownloader(httpClient, cache);
      logger.info("Remote mode active - caching enabled");
    }

    this.freemarkerConfig = createFreemarkerConfiguration();
    this.structureMetadataLoader = new StructureMetadataLoader(this);
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
    this.sourceResolver = null;
    this.freemarkerConfig = createFreemarkerConfiguration();
    this.structureMetadataLoader = new StructureMetadataLoader(this);
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

  @Override
  public StructureMetadata loadStructureMetadata(ArchitectureType architecture) {
    return structureMetadataLoader.loadStructureMetadata(architecture);
  }

  @Override
  public com.pragma.archetype.domain.model.adapter.AdapterMetadata loadAdapterMetadata(String adapterName) {
    if (adapterMetadataLoader == null) {
      adapterMetadataLoader = new AdapterMetadataLoader(this);
    }
    return adapterMetadataLoader.loadAdapterMetadata(adapterName);
  }

  @Override
  public com.pragma.archetype.domain.model.adapter.AdapterMetadata loadAdapterMetadata(
      String adapterName,
      String framework,
      String paradigm,
      String adapterType) {
    if (adapterMetadataLoader == null) {
      adapterMetadataLoader = new AdapterMetadataLoader(this);
    }
    return adapterMetadataLoader.loadAdapterMetadata(adapterName, framework, paradigm, adapterType);
  }

  /**
   * Validates a template for FreeMarker syntax errors.
   * 
   * @param templatePath path to the template file
   * @return ValidationResult indicating if template is valid
   */
  public ValidationResult validateTemplate(String templatePath) {
    try {
      // Check if template exists
      if (!templateExists(templatePath)) {
        return ValidationResult.failure("Template not found: " + templatePath);
      }

      // Get template content
      String content = getTemplateContent(templatePath);

      // Try to create a FreeMarker template to validate syntax
      new Template(templatePath, content, freemarkerConfig);

      // If we get here, syntax is valid
      return ValidationResult.success();

    } catch (freemarker.core.ParseException e) {
      return ValidationResult.failure(
          String.format("FreeMarker syntax error in '%s' at line %d: %s",
              templatePath, e.getLineNumber(), e.getMessage()));
    } catch (IOException e) {
      return ValidationResult.failure(
          String.format("Failed to load template '%s': %s", templatePath, e.getMessage()));
    } catch (Exception e) {
      return ValidationResult.failure(
          String.format("Template validation failed for '%s': %s", templatePath, e.getMessage()));
    }
  }

  /**
   * Extracts required variables from a template by parsing FreeMarker
   * expressions.
   * Identifies variables used in ${...} and #{...} expressions.
   * 
   * @param templatePath path to the template file
   * @return set of variable names used in the template
   */
  public Set<String> extractRequiredVariables(String templatePath) {
    Set<String> variables = new HashSet<>();

    try {
      // Get template content
      String content = getTemplateContent(templatePath);

      // Pattern to match FreeMarker variable expressions: ${variable} or #{variable}
      // Also matches property access: ${object.property}
      Pattern variablePattern = Pattern.compile("\\$\\{([a-zA-Z_][a-zA-Z0-9_.]*)\\}|#\\{([a-zA-Z_][a-zA-Z0-9_.]*)\\}");
      Matcher matcher = variablePattern.matcher(content);

      while (matcher.find()) {
        // Get the variable name (either from group 1 or group 2)
        String variable = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);

        // Extract the root variable name (before any dots)
        String rootVariable = variable.split("\\.")[0];
        variables.add(rootVariable);
      }

      // Pattern to match FreeMarker directives with variables: <#if variable>, <#list
      // items as item>
      Pattern directivePattern = Pattern.compile("<#(?:if|elseif|list|assign)\\s+([a-zA-Z_][a-zA-Z0-9_]*)");
      matcher = directivePattern.matcher(content);

      while (matcher.find()) {
        variables.add(matcher.group(1));
      }

      logger.debug("Extracted {} variables from template '{}': {}",
          variables.size(), templatePath, variables);

    } catch (Exception e) {
      logger.warn("Failed to extract variables from template '{}': {}",
          templatePath, e.getMessage());
    }

    return variables;
  }

  /**
   * Validates a template against a set of required variables.
   * Checks if all variables used in the template are provided in the required
   * set.
   * 
   * @param templatePath      path to the template file
   * @param providedVariables set of variables that will be provided in the
   *                          context
   * @return ValidationResult indicating if all required variables are provided
   */
  public ValidationResult validateTemplateVariables(String templatePath, Set<String> providedVariables) {
    try {
      // First validate template syntax
      ValidationResult syntaxValidation = validateTemplate(templatePath);
      if (!syntaxValidation.valid()) {
        return syntaxValidation;
      }

      // Extract variables used in template
      Set<String> requiredVariables = extractRequiredVariables(templatePath);

      // Find undefined variables
      List<String> undefinedVariables = new ArrayList<>();
      for (String variable : requiredVariables) {
        if (!providedVariables.contains(variable)) {
          undefinedVariables.add(variable);
        }
      }

      if (!undefinedVariables.isEmpty()) {
        return ValidationResult.failure(
            String.format("Template '%s' uses undefined variables: %s. Available variables: %s",
                templatePath, undefinedVariables, providedVariables));
      }

      return ValidationResult.success();

    } catch (Exception e) {
      return ValidationResult.failure(
          String.format("Failed to validate template variables for '%s': %s",
              templatePath, e.getMessage()));
    }
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
   * Disables caching in local mode for hot reload.
   * Enables caching in remote mode for performance.
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

      // Configure caching based on mode
      if (sourceResolver != null && sourceResolver.isLocalMode()) {
        // Disable caching in local mode for hot reload
        config.setTemplateUpdateDelayMilliseconds(0);
        logger.debug("Template caching disabled for local mode hot reload");
      } else {
        // Enable caching in remote mode (default behavior)
        // Templates are cached indefinitely (5000ms is default, but we can increase it)
        config.setTemplateUpdateDelayMilliseconds(Integer.MAX_VALUE);
        logger.debug("Template caching enabled for remote mode");
      }

    } catch (IOException e) {
      throw new RuntimeException("Failed to configure Freemarker", e);
    }

    return config;
  }
}
