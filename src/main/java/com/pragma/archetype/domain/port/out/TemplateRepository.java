package com.pragma.archetype.domain.port.out;

import com.pragma.archetype.domain.model.ArchitectureType;
import com.pragma.archetype.domain.model.Framework;
import com.pragma.archetype.domain.model.Paradigm;

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
   * Loads and processes a template with the given data.
   *
   * @param templatePath path to the template file
   * @param data         data to be used in template processing
   * @return processed template content
   * @throws TemplateNotFoundException   if template is not found
   * @throws TemplateProcessingException if template processing fails
   */
  String processTemplate(String templatePath, Object data);

  /**
   * Checks if a template exists.
   *
   * @param templatePath path to the template file
   * @return true if template exists, false otherwise
   */
  boolean templateExists(String templatePath);

  /**
   * Gets the template path for a specific architecture and framework.
   *
   * @param architecture the architecture type
   * @param framework    the framework
   * @param paradigm     the paradigm
   * @param templateName the template name
   * @return the full template path
   */
  String getTemplatePath(
      ArchitectureType architecture,
      Framework framework,
      Paradigm paradigm,
      String templateName);
}
