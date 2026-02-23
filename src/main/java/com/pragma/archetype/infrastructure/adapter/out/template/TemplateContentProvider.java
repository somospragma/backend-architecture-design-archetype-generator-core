package com.pragma.archetype.infrastructure.adapter.out.template;

import com.pragma.archetype.domain.port.out.TemplateRepository.TemplateNotFoundException;

/**
 * Provides template content from various sources (local filesystem, remote
 * repository, etc.).
 * This interface abstracts the source of template content, allowing the loader
 * to work
 * independently of where templates are stored.
 */
public interface TemplateContentProvider {

  /**
   * Gets the content of a template file.
   *
   * @param templatePath The path to the template file (relative to template root)
   * @return The template content as a string
   * @throws TemplateNotFoundException if the template cannot be found or read
   */
  String getTemplateContent(String templatePath);

  /**
   * Checks if a template exists at the specified path.
   *
   * @param templatePath The path to check
   * @return true if the template exists, false otherwise
   */
  boolean templateExists(String templatePath);
}
