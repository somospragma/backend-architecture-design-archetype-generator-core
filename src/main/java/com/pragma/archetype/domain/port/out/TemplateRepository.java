package com.pragma.archetype.domain.port.out;

import java.nio.file.Path;
import java.util.Map;

import com.pragma.archetype.domain.model.project.ArchitectureType;
import com.pragma.archetype.domain.model.structure.StructureMetadata;

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
   * Copies a binary file from the template directory to a destination.
   * Used for files like gradle-wrapper.jar that cannot be read as text.
   *
   * @param templatePath path to the template file (relative to templates root)
   * @param destination  destination path where the file should be copied
   * @throws TemplateNotFoundException if template is not found
   */
  void copyBinaryFile(String templatePath, Path destination);

  /**
   * Loads structure metadata for a given architecture type.
   *
   * @param architecture the architecture type
   * @return structure metadata for the architecture
   * @throws TemplateNotFoundException if structure.yml is not found
   */
  StructureMetadata loadStructureMetadata(ArchitectureType architecture);

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
  com.pragma.archetype.domain.model.validation.ValidationResult validateTemplate(String templatePath);

  /**
   * Extracts required variables from a template.
   * Parses the template to find all variable references.
   *
   * @param templatePath path to the template file
   * @return set of required variable names
   * @throws TemplateNotFoundException if template is not found
   */
  java.util.Set<String> extractRequiredVariables(String templatePath);

  /**
   * Processes an inline template string with the given context.
   * Used for processing configuration templates that are stored as strings
   * rather than files (e.g., buildConfiguration in adapter metadata).
   *
   * @param templateContent template content as string
   * @param context         context data for template processing
   * @return processed template content
   * @throws TemplateProcessingException if template processing fails
   */
  String processInlineTemplate(String templateContent, java.util.Map<String, Object> context);

  /**
   * Lists all template files in a directory.
   * Used for dynamic template discovery without hardcoding file names.
   *
   * @param directoryPath path to the directory (relative to templates root)
   * @return list of template file names in the directory (not full paths, just
   *         names)
   */
  java.util.List<String> listTemplatesInDirectory(String directoryPath);
}
