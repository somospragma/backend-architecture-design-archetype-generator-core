package com.pragma.archetype.domain.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pragma.archetype.domain.model.adapter.AdapterMetadata;
import com.pragma.archetype.domain.model.project.ArchitectureType;
import com.pragma.archetype.domain.model.structure.StructureMetadata;
import com.pragma.archetype.domain.model.validation.ValidationResult;
import com.pragma.archetype.domain.port.out.TemplateRepository;

/**
 * Domain service responsible for validating templates.
 * Validates architecture templates, adapter templates, and their metadata.
 */
public class TemplateValidator {

  private static final Logger logger = LoggerFactory.getLogger(TemplateValidator.class);
  private final TemplateRepository templateRepository;

  public TemplateValidator(TemplateRepository templateRepository) {
    this.templateRepository = Objects.requireNonNull(templateRepository, "Template repository cannot be null");
  }

  /**
   * Validates all templates for an architecture.
   * 
   * @param architecture the architecture type to validate
   * @return ValidationResult with success status and any error messages
   */
  public ValidationResult validateArchitectureTemplates(ArchitectureType architecture) {
    Objects.requireNonNull(architecture, "Architecture cannot be null");

    List<String> errors = new ArrayList<>();
    List<String> warnings = new ArrayList<>();

    try {
      // 1. Load and validate structure metadata
      StructureMetadata metadata = templateRepository.loadStructureMetadata(architecture);
      ValidationResult metadataValidation = metadata.validate();
      if (!metadataValidation.valid()) {
        errors.add("Structure metadata validation failed for " + architecture.getValue());
        errors.addAll(metadataValidation.errors());
      }

      // 2. Validate README template exists
      String readmeTemplate = "architectures/" + architecture.getValue() + "/README.md.ftl";
      ValidationResult readmeValidation = templateRepository.validateTemplate(readmeTemplate);
      if (!readmeValidation.valid()) {
        warnings.add("README template not found or invalid: " + readmeTemplate);
      }

      // 3. Validate build file templates exist
      String buildTemplate = "architectures/" + architecture.getValue() + "/build.gradle.ftl";
      ValidationResult buildValidation = templateRepository.validateTemplate(buildTemplate);
      if (!buildValidation.valid()) {
        warnings.add("Build template not found or invalid: " + buildTemplate);
      }

    } catch (TemplateRepository.TemplateNotFoundException e) {
      errors.add("Architecture templates not found: " + e.getMessage());
    } catch (Exception e) {
      errors.add("Failed to validate architecture templates: " + e.getMessage());
    }

    return errors.isEmpty()
        ? (warnings.isEmpty() ? ValidationResult.success() : ValidationResult.successWithWarnings(warnings))
        : ValidationResult.failure(errors);
  }

  /**
   * Validates all templates for an adapter.
   * 
   * @param adapterName the adapter name to validate
   * @return ValidationResult with success status and any error messages
   */
  public ValidationResult validateAdapterTemplates(String adapterName) {
    Objects.requireNonNull(adapterName, "Adapter name cannot be null");

    List<String> errors = new ArrayList<>();
    List<String> warnings = new ArrayList<>();

    try {
      // 1. Load and validate adapter metadata
      AdapterMetadata metadata = templateRepository.loadAdapterMetadata(adapterName);
      ValidationResult metadataValidation = metadata.validate();
      if (!metadataValidation.valid()) {
        errors.add("Adapter metadata validation failed for " + adapterName);
        errors.addAll(metadataValidation.errors());
      }

      // 2. Validate main adapter template
      String adapterTemplate = "adapters/" + adapterName + "/Adapter.java.ftl";
      ValidationResult adapterValidation = templateRepository.validateTemplate(adapterTemplate);
      if (!adapterValidation.valid()) {
        errors.add("Adapter template not found or invalid: " + adapterTemplate);
      }

      // 3. Validate data entity template
      String entityTemplate = "adapters/" + adapterName + "/DataEntity.java.ftl";
      ValidationResult entityValidation = templateRepository.validateTemplate(entityTemplate);
      if (!entityValidation.valid()) {
        warnings.add("Data entity template not found or invalid: " + entityTemplate);
      }

      // 4. Validate mapper template
      String mapperTemplate = "adapters/" + adapterName + "/Mapper.java.ftl";
      ValidationResult mapperValidation = templateRepository.validateTemplate(mapperTemplate);
      if (!mapperValidation.valid()) {
        warnings.add("Mapper template not found or invalid: " + mapperTemplate);
      }

      // 5. Validate application properties template if specified
      if (metadata.hasApplicationProperties()) {
        String propsTemplate = "adapters/" + adapterName + "/" + metadata.applicationPropertiesTemplate();
        ValidationResult propsValidation = templateRepository.validateTemplate(propsTemplate);
        if (!propsValidation.valid()) {
          errors.add("Application properties template not found or invalid: " + propsTemplate);
        }
      }

      // 6. Validate configuration class templates if specified
      if (metadata.hasConfigurationClasses()) {
        for (AdapterMetadata.ConfigurationClass configClass : metadata.configurationClasses()) {
          String configTemplate = "adapters/" + adapterName + "/" + configClass.templatePath();
          ValidationResult configValidation = templateRepository.validateTemplate(configTemplate);
          if (!configValidation.valid()) {
            errors.add("Configuration class template not found or invalid: " + configTemplate);
          }
        }
      }

    } catch (TemplateRepository.TemplateNotFoundException e) {
      errors.add("Adapter templates not found: " + e.getMessage());
    } catch (Exception e) {
      errors.add("Failed to validate adapter templates: " + e.getMessage());
    }

    return errors.isEmpty()
        ? (warnings.isEmpty() ? ValidationResult.success() : ValidationResult.successWithWarnings(warnings))
        : ValidationResult.failure(errors);
  }

  /**
   * Validates template variables to check for undefined variables.
   * 
   * @param templatePath      the template path to validate
   * @param expectedVariables set of expected variable names
   * @return ValidationResult with success status and any error messages
   */
  public ValidationResult validateTemplateVariables(String templatePath, java.util.Set<String> expectedVariables) {
    Objects.requireNonNull(templatePath, "Template path cannot be null");
    Objects.requireNonNull(expectedVariables, "Expected variables cannot be null");

    List<String> errors = new ArrayList<>();

    try {
      // Extract required variables from template
      java.util.Set<String> requiredVariables = templateRepository.extractRequiredVariables(templatePath);

      // Check if all required variables are provided
      for (String required : requiredVariables) {
        if (!expectedVariables.contains(required)) {
          errors.add("Template " + templatePath + " requires undefined variable: " + required);
        }
      }

    } catch (Exception e) {
      errors.add("Failed to validate template variables: " + e.getMessage());
    }

    return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
  }
}
