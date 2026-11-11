package com.pragma.archetype.domain.port.in;


import com.pragma.archetype.domain.model.validation.ValidationResult;

/**
 * Use case for validating templates.
 * Validates architecture templates, adapter templates, or all templates.
 */
public interface ValidateTemplateUseCase {

  /**
   * Validates all templates (architectures and adapters).
   * @return ValidationResult with success status and any error messages
   */
  ValidationResult validateAll();

  /**
   * Validates templates for a specific architecture.
   * 
   * @param architectureName the architecture name (e.g., "hexagonal-single",
   *                         "onion-single")
   * @return ValidationResult with success status and any error messages
   */
  ValidationResult validateArchitecture(String architectureName);

  /**
   * Validates templates for a specific adapter.
   * 
   * @param adapterName the adapter name (e.g., "mongodb", "rest-controller")
   * @return ValidationResult with success status and any error messages
   */
  ValidationResult validateAdapter(String adapterName);
}
