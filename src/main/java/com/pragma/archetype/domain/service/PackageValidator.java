package com.pragma.archetype.domain.service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.pragma.archetype.domain.model.ValidationResult;

/**
 * Domain service responsible for validating Java package names and structure.
 * Ensures package names follow Java naming conventions and align with folder
 * structure.
 */
public class PackageValidator {

  /**
   * Validates a Java package name according to Java naming conventions.
   * 
   * Rules:
   * - Must contain at least two segments separated by dots
   * - Each segment must start with a lowercase letter
   * - Segments can contain lowercase letters, numbers, and underscores
   * - Cannot use Java reserved keywords
   * 
   * @param packageName the package name to validate
   * @return ValidationResult with success status and any error messages
   */
  public ValidationResult validatePackageName(String packageName) {
    List<String> errors = new ArrayList<>();

    if (packageName == null || packageName.isBlank()) {
      errors.add("Package name cannot be null or empty");
      return ValidationResult.failure(errors);
    }

    // Check for leading/trailing dots
    if (packageName.startsWith(".") || packageName.endsWith(".")) {
      errors.add("Package name cannot start or end with a dot: " + packageName);
    }

    // Split into segments
    String[] segments = packageName.split("\\.");

    if (segments.length < 2) {
      errors.add("Package name must contain at least two segments: " + packageName);
      errors.add("Example: com.company.service");
    }

    // Validate each segment
    for (int i = 0; i < segments.length; i++) {
      String segment = segments[i];

      if (segment.isEmpty()) {
        errors.add("Package name contains empty segment at position " + (i + 1) + ": " + packageName);
        continue;
      }

      // Check if segment starts with lowercase letter
      if (!Character.isLowerCase(segment.charAt(0))) {
        errors.add("Package segment must start with lowercase letter: '" + segment + "' in " + packageName);
      }

      // Check if segment contains only valid characters
      if (!segment.matches("[a-z][a-z0-9_]*")) {
        errors.add("Package segment contains invalid characters: '" + segment + "' in " + packageName);
        errors.add("Segments must contain only lowercase letters, numbers, and underscores");
      }

      // Check for Java reserved keywords
      if (isJavaKeyword(segment)) {
        errors.add("Package segment cannot be a Java reserved keyword: '" + segment + "' in " + packageName);
      }
    }

    return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
  }

  /**
   * Validates that package declaration matches the folder structure.
   * 
   * Rules:
   * - Package path must match folder structure
   * - Each package segment must correspond to a folder
   * 
   * @param packageName the package name (e.g., "com.example.service")
   * @param filePath    the file path relative to source root (e.g.,
   *                    "com/example/service/MyClass.java")
   * @return ValidationResult with success status and any error messages
   */
  public ValidationResult validatePackageFolderAlignment(String packageName, Path filePath) {
    Objects.requireNonNull(packageName, "Package name cannot be null");
    Objects.requireNonNull(filePath, "File path cannot be null");

    List<String> errors = new ArrayList<>();

    // Convert package name to expected path
    String expectedPath = packageName.replace('.', '/');

    // Get the actual path (without filename)
    String actualPath = filePath.getParent() != null
        ? filePath.getParent().toString().replace('\\', '/')
        : "";

    // Check if actual path ends with expected path
    if (!actualPath.endsWith(expectedPath)) {
      errors.add("Package declaration does not match folder structure");
      errors.add("  Package: " + packageName);
      errors.add("  Expected path: .../" + expectedPath);
      errors.add("  Actual path: " + actualPath);
      errors.add("Ensure the file is in the correct folder matching its package declaration");
    }

    return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
  }

  /**
   * Validates that all packages in a project start with the base package.
   * 
   * Rules:
   * - All package names must start with the base package
   * - Ensures consistent package structure across the project
   * 
   * @param packageName the package name to validate
   * @param basePackage the base package for the project
   * @return ValidationResult with success status and any error messages
   */
  public ValidationResult validateBasePackageConsistency(String packageName, String basePackage) {
    Objects.requireNonNull(packageName, "Package name cannot be null");
    Objects.requireNonNull(basePackage, "Base package cannot be null");

    List<String> errors = new ArrayList<>();

    if (!packageName.startsWith(basePackage)) {
      errors.add("Package does not start with base package");
      errors.add("  Package: " + packageName);
      errors.add("  Base package: " + basePackage);
      errors.add("All packages must start with the base package defined in .cleanarch.yml");
      errors.add("Example: If base package is 'com.example', package should be 'com.example.domain'");
    }

    // Validate that it's not just the base package (should have additional
    // segments)
    if (packageName.equals(basePackage)) {
      errors.add("Package cannot be exactly the base package");
      errors.add("  Package: " + packageName);
      errors.add("Add additional segments for layer/component organization");
      errors.add("Example: " + basePackage + ".domain or " + basePackage + ".application");
    }

    return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
  }

  /**
   * Checks if a string is a Java reserved keyword.
   */
  private boolean isJavaKeyword(String word) {
    String[] keywords = {
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
        "class", "const", "continue", "default", "do", "double", "else", "enum",
        "extends", "final", "finally", "float", "for", "goto", "if", "implements",
        "import", "instanceof", "int", "interface", "long", "native", "new", "package",
        "private", "protected", "public", "return", "short", "static", "strictfp",
        "super", "switch", "synchronized", "this", "throw", "throws", "transient",
        "try", "void", "volatile", "while", "true", "false", "null"
    };

    for (String keyword : keywords) {
      if (keyword.equals(word)) {
        return true;
      }
    }
    return false;
  }
}
