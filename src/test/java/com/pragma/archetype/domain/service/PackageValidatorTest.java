package com.pragma.archetype.domain.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.pragma.archetype.domain.model.ValidationResult;

class PackageValidatorTest {

  private PackageValidator validator;

  @BeforeEach
  void setUp() {
    validator = new PackageValidator();
  }

  @Test
  void shouldValidateCorrectPackageName() {
    // When
    ValidationResult result = validator.validatePackageName("com.example.service");

    // Then
    assertTrue(result.valid());
  }

  @Test
  void shouldFailWithSingleSegment() {
    // When
    ValidationResult result = validator.validatePackageName("service");

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("at least two segments")));
  }

  @Test
  void shouldFailWithUppercaseStart() {
    // When
    ValidationResult result = validator.validatePackageName("Com.example.service");

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("lowercase letter")));
  }

  @Test
  void shouldFailWithJavaKeyword() {
    // When
    ValidationResult result = validator.validatePackageName("com.class.service");

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("reserved keyword")));
  }

  @Test
  void shouldFailWithLeadingDot() {
    // When
    ValidationResult result = validator.validatePackageName(".com.example");

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("start or end with a dot")));
  }

  @Test
  void shouldFailWithTrailingDot() {
    // When
    ValidationResult result = validator.validatePackageName("com.example.");

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("start or end with a dot")));
  }

  @Test
  void shouldValidatePackageFolderAlignment() {
    // When
    ValidationResult result = validator.validatePackageFolderAlignment(
        "com.example.service",
        Path.of("src/main/java/com/example/service/MyClass.java"));

    // Then
    assertTrue(result.valid());
  }

  @Test
  void shouldFailWhenFolderDoesNotMatchPackage() {
    // When
    ValidationResult result = validator.validatePackageFolderAlignment(
        "com.example.service",
        Path.of("src/main/java/com/wrong/path/MyClass.java"));

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("does not match folder structure")));
  }

  @Test
  void shouldValidateBasePackageConsistency() {
    // When
    ValidationResult result = validator.validateBasePackageConsistency(
        "com.example.service.impl",
        "com.example");

    // Then
    assertTrue(result.valid());
  }

  @Test
  void shouldFailWhenPackageDoesNotStartWithBase() {
    // When
    ValidationResult result = validator.validateBasePackageConsistency(
        "org.other.service",
        "com.example");

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("does not start with base package")));
  }

  @Test
  void shouldFailWhenPackageIsExactlyBasePackage() {
    // When
    ValidationResult result = validator.validateBasePackageConsistency(
        "com.example",
        "com.example");

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("cannot be exactly the base package")));
  }

  @Test
  void shouldFailWithNullPackageName() {
    // When
    ValidationResult result = validator.validatePackageName(null);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("cannot be null or empty")));
  }

  @Test
  void shouldFailWithEmptyPackageName() {
    // When
    ValidationResult result = validator.validatePackageName("");

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("cannot be null or empty")));
  }
}
