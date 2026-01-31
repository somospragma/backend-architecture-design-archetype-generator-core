package com.pragma.archetype.domain.port.out;

import java.util.Map;

/**
 * Port for accessing and processing templates.
 * This is an output port (drives external template system).
 */
public interface TemplateRepository {

  /**
   * Exception thrown when a template is not found.
   */
  class TemplateNotFoundException extends RuntimeException {
    public TemplateNotFoundException(String message) {
      super(message);
    }
  }

  /**
   * Exception thrown when template processing fails.
   */
  class TemplateProcessingException extends RuntimeException {
    public TemplateProcessingException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  /**
   * Loads and processes a template with the given context.
   *
   * @param templatePath path to the template file
   * @param context      context data for template processing
   * @return processed template content
   * @throws TemplateNotFoundException   if template is not found
   * @throws TemplateProcessingException if template processing fails
   */
  String processTemplate(String templatePath, Map<String, Object> context);

  /**
   * Checks if a template exists.
   *
   * @param templatePath path to the template file
   * @return true if template exists, false otherwise
   */
  boolean templateExists(String templatePath);

  /**
   * Gets the raw content of a template without processing.
   *
   * @param templatePath path to the template file
   * @return raw template content
   * @throws TemplateNotFoundException if template is not found
   */
  String getTemplateContent(String templatePath);
}
