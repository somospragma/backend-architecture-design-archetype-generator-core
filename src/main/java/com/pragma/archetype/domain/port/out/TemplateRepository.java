package com.pragma.archetype.domain.port.out;

import java.util.Map;

import com.pragma.archetype.domain.model.ArchitectureType;
import com.pragma.archetype.domain.model.StructureMetadata;

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

  /**
   * Loads structure metadata for a given architecture type.
   *
   * @param architecture the architecture type
   * @return structure metadata for the architecture
   * @throws TemplateNotFoundException if structure.yml is not found
   */
  StructureMetadata loadStructureMetadata(ArchitectureType architecture);

  /**
   * Loads adapter metadata for a given adapter name (legacy method).
   * This method uses the legacy flat structure:
   * adapters/{adapterName}/metadata.yml
   *
   * @param adapterName the name of the adapter
   * @return adapter metadata for the adapter
   * @throws TemplateNotFoundException if metadata.yml is not found
   * @deprecated Use {@link #loadAdapterMetadata(String, String, String, String)}
   *             instead
   */
  @Deprecated
  com.pragma.archetype.domain.model.adapter.AdapterMetadata loadAdapterMetadata(String adapterName);

  /**
   * Loads adapter metadata for a given adapter using framework-aware structure.
   * Path:
   * frameworks/{framework}/{paradigm}/adapters/{adapterType}/{adapterName}/metadata.yml
   *
   * @param adapterName the name of the adapter
   * @param framework   the framework (e.g., "spring", "quarkus")
   * @param paradigm    the paradigm (e.g., "reactive", "imperative")
   * @param adapterType the adapter type (e.g., "driven-adapters", "entry-points")
   * @return adapter metadata for the adapter
   * @throws TemplateNotFoundException if metadata.yml is not found
   */
  com.pragma.archetype.domain.model.adapter.AdapterMetadata loadAdapterMetadata(
      String adapterName,
      String framework,
      String paradigm,
      String adapterType);

  /**
   * Validates a template for syntax errors and existence.
   * This is used for validation-before-modification pattern.
   *
   * @param templatePath path to the template file
   * @return ValidationResult indicating if the template is valid
   */
  com.pragma.archetype.domain.model.ValidationResult validateTemplate(String templatePath);

  /**
   * Extracts required variables from a template.
   * Parses the template to find all variable references.
   *
   * @param templatePath path to the template file
   * @return set of required variable names
   * @throws TemplateNotFoundException if template is not found
   */
  java.util.Set<String> extractRequiredVariables(String templatePath);
}
