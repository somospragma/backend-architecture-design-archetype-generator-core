package com.pragma.archetype.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.pragma.archetype.domain.model.validation.ValidationResult;

class ValidationResultTest {

  @Test
  void shouldCreateSuccessResult() {
    // When
    ValidationResult result = ValidationResult.success();

    // Then
    assertTrue(result.valid());
    assertFalse(result.isInvalid());
    assertTrue(result.errors().isEmpty());
    assertFalse(result.hasWarnings());
  }

  @Test
  void shouldCreateFailureResult() {
    // Given
    List<String> errors = List.of("Error 1", "Error 2");

    // When
    ValidationResult result = ValidationResult.failure(errors);

    // Then
    assertFalse(result.valid());
    assertTrue(result.isInvalid());
    assertEquals(2, result.errors().size());
    assertTrue(result.errors().contains("Error 1"));
  }

  @Test
  void shouldCreateFailureResultWithSingleError() {
    // When
    ValidationResult result = ValidationResult.failure("Single error");

    // Then
    assertFalse(result.valid());
    assertEquals(1, result.errors().size());
    assertEquals("Single error", result.errors().get(0));
  }

  @Test
  void shouldCreateSuccessWithWarnings() {
    // Given
    List<String> warnings = List.of("Warning 1", "Warning 2");

    // When
    ValidationResult result = ValidationResult.successWithWarnings(warnings);

    // Then
    assertTrue(result.valid());
    assertTrue(result.hasWarnings());
    assertEquals(2, result.warnings().size());
    assertTrue(result.warnings().contains("Warning 1"));
  }

  @Test
  void shouldHandleEmptyErrors() {
    // When
    ValidationResult result = ValidationResult.failure(List.of());

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().isEmpty());
  }

  @Test
  void shouldHandleEmptyWarnings() {
    // When
    ValidationResult result = ValidationResult.successWithWarnings(List.of());

    // Then
    assertTrue(result.valid());
    assertFalse(result.hasWarnings());
  }
}
