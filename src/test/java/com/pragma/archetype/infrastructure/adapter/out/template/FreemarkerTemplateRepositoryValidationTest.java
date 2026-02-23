package com.pragma.archetype.infrastructure.adapter.out.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.pragma.archetype.domain.model.ValidationResult;

@DisplayName("FreemarkerTemplateRepository Validation Tests")
class FreemarkerTemplateRepositoryValidationTest {

  private FreemarkerTemplateRepository repository;
  private Path templatesDir;

  @BeforeEach
  void setUp(@TempDir Path tempDir) {
    templatesDir = tempDir;
    repository = new FreemarkerTemplateRepository(templatesDir);
  }

  @Test
  @DisplayName("validateTemplate should return success for valid template")
  void validateTemplate_shouldReturnSuccessForValidTemplate() throws IOException {
    // Given: A valid FreeMarker template
    Path templateFile = templatesDir.resolve("valid.ftl");
    Files.writeString(templateFile, "Hello ${name}!");

    // When: Validating the template
    ValidationResult result = repository.validateTemplate("valid.ftl");

    // Then: Should be valid
    assertTrue(result.valid());
    assertTrue(result.errors().isEmpty());
  }

  @Test
  @DisplayName("validateTemplate should return failure for non-existent template")
  void validateTemplate_shouldReturnFailureForNonExistentTemplate() {
    // When: Validating a non-existent template
    ValidationResult result = repository.validateTemplate("nonexistent.ftl");

    // Then: Should be invalid
    assertFalse(result.valid());
    assertTrue(result.getFirstError().contains("Template not found"));
  }

  @Test
  @DisplayName("validateTemplate should return failure for template with syntax error")
  void validateTemplate_shouldReturnFailureForTemplateWithSyntaxError() throws IOException {
    // Given: A template with FreeMarker syntax error (unclosed directive)
    Path templateFile = templatesDir.resolve("invalid.ftl");
    Files.writeString(templateFile, "<#if condition>Hello");

    // When: Validating the template
    ValidationResult result = repository.validateTemplate("invalid.ftl");

    // Then: Should be invalid with syntax error
    assertFalse(result.valid());
    assertTrue(result.getFirstError().contains("syntax error") ||
        result.getFirstError().contains("FreeMarker"));
  }

  @Test
  @DisplayName("extractRequiredVariables should extract simple variables")
  void extractRequiredVariables_shouldExtractSimpleVariables() throws IOException {
    // Given: A template with simple variables
    Path templateFile = templatesDir.resolve("simple.ftl");
    Files.writeString(templateFile, "Hello ${name}, your age is ${age}!");

    // When: Extracting variables
    Set<String> variables = repository.extractRequiredVariables("simple.ftl");

    // Then: Should extract both variables
    assertNotNull(variables);
    assertEquals(2, variables.size());
    assertTrue(variables.contains("name"));
    assertTrue(variables.contains("age"));
  }

  @Test
  @DisplayName("extractRequiredVariables should extract root variable from property access")
  void extractRequiredVariables_shouldExtractRootVariableFromPropertyAccess() throws IOException {
    // Given: A template with property access
    Path templateFile = templatesDir.resolve("property.ftl");
    Files.writeString(templateFile, "User: ${user.name}, Email: ${user.email}");

    // When: Extracting variables
    Set<String> variables = repository.extractRequiredVariables("property.ftl");

    // Then: Should extract only root variable
    assertNotNull(variables);
    assertEquals(1, variables.size());
    assertTrue(variables.contains("user"));
  }

  @Test
  @DisplayName("extractRequiredVariables should extract variables from directives")
  void extractRequiredVariables_shouldExtractVariablesFromDirectives() throws IOException {
    // Given: A template with FreeMarker directives
    Path templateFile = templatesDir.resolve("directives.ftl");
    Files.writeString(templateFile,
        "<#if condition>Yes</#if>\n" +
            "<#list items as item>${item}</#list>");

    // When: Extracting variables
    Set<String> variables = repository.extractRequiredVariables("directives.ftl");

    // Then: Should extract variables from directives
    assertNotNull(variables);
    assertTrue(variables.contains("condition"));
    assertTrue(variables.contains("items"));
  }

  @Test
  @DisplayName("extractRequiredVariables should handle templates with no variables")
  void extractRequiredVariables_shouldHandleTemplatesWithNoVariables() throws IOException {
    // Given: A template with no variables
    Path templateFile = templatesDir.resolve("static.ftl");
    Files.writeString(templateFile, "This is a static template with no variables.");

    // When: Extracting variables
    Set<String> variables = repository.extractRequiredVariables("static.ftl");

    // Then: Should return empty set
    assertNotNull(variables);
    assertTrue(variables.isEmpty());
  }

  @Test
  @DisplayName("validateTemplateVariables should return success when all variables provided")
  void validateTemplateVariables_shouldReturnSuccessWhenAllVariablesProvided() throws IOException {
    // Given: A template with variables
    Path templateFile = templatesDir.resolve("complete.ftl");
    Files.writeString(templateFile, "Hello ${name}, you are ${age} years old!");

    // And: All required variables are provided
    Set<String> providedVariables = Set.of("name", "age");

    // When: Validating template variables
    ValidationResult result = repository.validateTemplateVariables("complete.ftl", providedVariables);

    // Then: Should be valid
    assertTrue(result.valid());
    assertTrue(result.errors().isEmpty());
  }

  @Test
  @DisplayName("validateTemplateVariables should return failure when variables missing")
  void validateTemplateVariables_shouldReturnFailureWhenVariablesMissing() throws IOException {
    // Given: A template with variables
    Path templateFile = templatesDir.resolve("incomplete.ftl");
    Files.writeString(templateFile, "Hello ${name}, you are ${age} years old!");

    // And: Only some variables are provided
    Set<String> providedVariables = Set.of("name");

    // When: Validating template variables
    ValidationResult result = repository.validateTemplateVariables("incomplete.ftl", providedVariables);

    // Then: Should be invalid
    assertFalse(result.valid());
    assertTrue(result.getFirstError().contains("undefined variables"));
    assertTrue(result.getFirstError().contains("age"));
  }

  @Test
  @DisplayName("validateTemplateVariables should return failure for invalid template syntax")
  void validateTemplateVariables_shouldReturnFailureForInvalidTemplateSyntax() throws IOException {
    // Given: A template with syntax error
    Path templateFile = templatesDir.resolve("broken.ftl");
    Files.writeString(templateFile, "<#if condition>Hello");

    // And: Variables are provided
    Set<String> providedVariables = Set.of("condition");

    // When: Validating template variables
    ValidationResult result = repository.validateTemplateVariables("broken.ftl", providedVariables);

    // Then: Should be invalid due to syntax error
    assertFalse(result.valid());
  }

  @Test
  @DisplayName("templateExists should return true for existing template")
  void templateExists_shouldReturnTrueForExistingTemplate() throws IOException {
    // Given: An existing template
    Path templateFile = templatesDir.resolve("exists.ftl");
    Files.writeString(templateFile, "Template content");

    // When: Checking if template exists
    boolean exists = repository.templateExists("exists.ftl");

    // Then: Should return true
    assertTrue(exists);
  }

  @Test
  @DisplayName("templateExists should return false for non-existent template")
  void templateExists_shouldReturnFalseForNonExistentTemplate() {
    // When: Checking if non-existent template exists
    boolean exists = repository.templateExists("nonexistent.ftl");

    // Then: Should return false
    assertFalse(exists);
  }
}
