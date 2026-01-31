package com.pragma.archetype.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Result of a validation operation.
 * Uses Java 21 features for clean implementation.
 */
public record ValidationResult(
    boolean valid,
    List<String> errors) {

  public ValidationResult {
    Objects.requireNonNull(errors, "Errors list cannot be null");
    errors = Collections.unmodifiableList(new ArrayList<>(errors));
  }

  /**
   * Creates a successful validation result.
   */
  public static ValidationResult success() {
    return new ValidationResult(true, List.of());
  }

  /**
   * Creates a failed validation result with a single error.
   */
  public static ValidationResult failure(String error) {
    return new ValidationResult(false, List.of(error));
  }

  /**
   * Creates a failed validation result with multiple errors.
   */
  public static ValidationResult failure(List<String> errors) {
    return new ValidationResult(false, errors);
  }

  /**
   * Checks if validation failed.
   */
  public boolean isInvalid() {
    return !valid;
  }

  /**
   * Gets the first error message, or empty string if valid.
   */
  public String getFirstError() {
    return errors.isEmpty() ? "" : errors.get(0);
  }

  /**
   * Gets all error messages as a single string.
   */
  public String getAllErrors() {
    return String.join("\n", errors);
  }
}
