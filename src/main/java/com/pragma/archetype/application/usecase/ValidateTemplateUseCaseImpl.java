package com.pragma.archetype.application.usecase;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pragma.archetype.domain.model.project.ArchitectureType;
import com.pragma.archetype.domain.model.validation.ValidationResult;
import com.pragma.archetype.domain.port.in.ValidateTemplateUseCase;
import com.pragma.archetype.domain.service.TemplateValidator;

/**
 * Implementation of ValidateTemplateUseCase.
 * Validates templates for architectures and adapters.
 */
public class ValidateTemplateUseCaseImpl implements ValidateTemplateUseCase {

  private static final Logger logger = LoggerFactory.getLogger(ValidateTemplateUseCaseImpl.class);
  private final TemplateValidator templateValidator;

  public ValidateTemplateUseCaseImpl(TemplateValidator templateValidator) {
    this.templateValidator = templateValidator;
  }

  @Override
  public ValidationResult validateAll(Path projectPath) {
    logger.info("Validating all templates...");

    List<String> allErrors = new ArrayList<>();
    List<String> allWarnings = new ArrayList<>();
    int successCount = 0;

    // Validate every architecture the template repository declares
    for (ArchitectureType architecture : templateValidator.availableArchitectures()) {
      ValidationResult result = templateValidator.validateArchitectureTemplates(architecture);
      if (result.valid()) {
        successCount++;
        logger.info("✓ Architecture '{}' templates are valid", architecture.getValue());
        if (result.hasWarnings()) {
          allWarnings.addAll(result.warnings());
        }
      } else {
        logger.error("✗ Architecture '{}' templates have errors", architecture.getValue());
        allErrors.addAll(result.errors());
      }
    }

    // TODO: Validate all adapter types
    // This would require discovering all available adapters from the template
    // repository
    // For now, we'll just validate architectures

    if (allErrors.isEmpty()) {
      logger.info("✓ All templates validated successfully ({} architectures)", successCount);
      return allWarnings.isEmpty()
          ? ValidationResult.success()
          : ValidationResult.successWithWarnings(allWarnings);
    } else {
      logger.error("✗ Template validation failed with {} errors", allErrors.size());
      return ValidationResult.failure(allErrors);
    }
  }

  @Override
  public ValidationResult validateArchitecture(String architectureName) {
    logger.info("Validating architecture templates: {}", architectureName);

    try {
      // Only architectures declared by the template repository are valid
      ArchitectureType architecture = ArchitectureType.fromValue(architectureName);
      if (!templateValidator.availableArchitectures().contains(architecture)) {
        throw new IllegalArgumentException("Unknown architecture type: " + architectureName);
      }

      ValidationResult result = templateValidator.validateArchitectureTemplates(architecture);

      if (result.valid()) {
        logger.info("✓ Architecture '{}' templates are valid", architectureName);
        if (result.hasWarnings()) {
          logger.warn("Warnings:");
          result.warnings().forEach(warning -> logger.warn("  - {}", warning));
        }
      } else {
        logger.error("✗ Architecture '{}' templates have errors:", architectureName);
        result.errors().forEach(error -> logger.error("  - {}", error));
      }

      return result;

    } catch (IllegalArgumentException e) {
      String error = "Unknown architecture type: " + architectureName;
      logger.error(error);
      logger.info("Valid architecture types: {}", templateValidator.availableArchitectures().stream()
          .map(ArchitectureType::getValue).collect(java.util.stream.Collectors.joining(", ")));
      return ValidationResult.failure(error);
    }
  }

  @Override
  public ValidationResult validateAdapter(String adapterName) {
    logger.info("Validating adapter templates: {}", adapterName);

    ValidationResult result = templateValidator.validateAdapterTemplates(adapterName);

    if (result.valid()) {
      logger.info("✓ Adapter '{}' templates are valid", adapterName);
      if (result.hasWarnings()) {
        logger.warn("Warnings:");
        result.warnings().forEach(warning -> logger.warn("  - {}", warning));
      }
    } else {
      logger.error("✗ Adapter '{}' templates have errors:", adapterName);
      result.errors().forEach(error -> logger.error("  - {}", error));
    }

    return result;
  }
}
