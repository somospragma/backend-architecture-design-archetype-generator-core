package com.pragma.archetype.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class ValidationResultBranchTest {

  @Test
  void testSuccess_noWarnings() {
    // When
    ValidationResult result = ValidationResult.success();

    // Then
    assertTrue(result.valid());
    assertTrue(result.errors().isEmpty());
    assertTrue(result.warnings().isEmpty());
    assertFalse(result.hasWarnings());
  }

  @Test
  void testSuccess_withWarnings() {
    // Given
    List<String> warnings = List.of("Warning 1", "Warning 2");

    // When
    ValidationResult result = ValidationResult.successWithWarnings(warnings);

    // Then
    assertTrue(result.valid());
    assertTrue(result.errors().isEmpty());
    assertEquals(2, result.warnings().size());
    assertTrue(result.hasWarnings());
  }

  @Test
  void testSuccess_withEmptyWarnings() {
    // Given
    List<String> warnings = new ArrayList<>();

    // When
    ValidationResult result = ValidationResult.successWithWarnings(warnings);

    // Then
    assertTrue(result.valid());
    assertFalse(result.hasWarnings());
  }

  @Test
  void testFailure_singleError() {
    // Given
    List<String> errors = List.of("Error 1");

    // When
    ValidationResult result = ValidationResult.failure(errors);

    // Then
    assertFalse(result.valid());
    assertEquals(1, result.errors().size());
    assertTrue(result.warnings().isEmpty());
  }

  @Test
  void testFailure_multipleErrors() {
    // Given
    List<String> errors = List.of("Error 1", "Error 2", "Error 3");

    // When
    ValidationResult result = ValidationResult.failure(errors);

    // Then
    assertFalse(result.valid());
    assertEquals(3, result.errors().size());
  }

  @Test
  void testFailure_withWarnings() {
    // Given
    List<String> errors = List.of("Error 1");
    List<String> warnings = List.of("Warning 1", "Warning 2");

    // When
    ValidationResult result = new ValidationResult(false, errors, warnings);

    // Then
    assertFalse(result.valid());
    assertEquals(1, result.errors().size());
    assertEquals(2, result.warnings().size());
    assertTrue(result.hasWarnings());
  }

  @Test
  void testFailure_emptyErrors() {
    // Given
    List<String> errors = new ArrayList<>();

    // When
    ValidationResult result = ValidationResult.failure(errors);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().isEmpty());
  }

  @Test
  void testHasWarnings_true() {
    // Given
    ValidationResult result = ValidationResult.successWithWarnings(List.of("Warning"));

    // When/Then
    assertTrue(result.hasWarnings());
  }

  @Test
  void testHasWarnings_false() {
    // Given
    ValidationResult result = ValidationResult.success();

    // When/Then
    assertFalse(result.hasWarnings());
  }

  @Test
  void testImmutability_errors() {
    // Given
    List<String> errors = new ArrayList<>();
    errors.add("Error 1");
    ValidationResult result = ValidationResult.failure(errors);

    // When - try to modify original list
    errors.add("Error 2");

    // Then - result should not be affected
    assertEquals(1, result.errors().size());
  }

  @Test
  void testImmutability_warnings() {
    // Given
    List<String> warnings = new ArrayList<>();
    warnings.add("Warning 1");
    ValidationResult result = ValidationResult.successWithWarnings(warnings);

    // When - try to modify original list
    warnings.add("Warning 2");

    // Then - result should not be affected
    assertEquals(1, result.warnings().size());
  }

  @Test
  void testEquality_sameContent() {
    // Given
    ValidationResult result1 = ValidationResult.successWithWarnings(List.of("Warning"));
    ValidationResult result2 = ValidationResult.successWithWarnings(List.of("Warning"));

    // When/Then
    assertEquals(result1.valid(), result2.valid());
    assertEquals(result1.warnings().size(), result2.warnings().size());
  }

  @Test
  void testEquality_differentContent() {
    // Given
    ValidationResult result1 = ValidationResult.success();
    ValidationResult result2 = ValidationResult.failure(List.of("Error"));

    // When/Then
    assertNotEquals(result1.valid(), result2.valid());
  }
}
