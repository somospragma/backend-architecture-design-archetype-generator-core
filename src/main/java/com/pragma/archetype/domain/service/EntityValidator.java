package com.pragma.archetype.domain.service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.pragma.archetype.domain.model.entity.EntityConfig;
import com.pragma.archetype.domain.model.validation.ValidationResult;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.port.out.FileSystemPort;

/**
 * Service for validating entity generation requests.
 */
public class EntityValidator {

  private final FileSystemPort fileSystemPort;
  private final ConfigurationPort configurationPort;

  public EntityValidator(FileSystemPort fileSystemPort, ConfigurationPort configurationPort) {
    this.fileSystemPort = fileSystemPort;
    this.configurationPort = configurationPort;
  }

  /**
   * Validates that an entity can be generated in the project.
   *
   * @param projectPath Path to the project
   * @param config      Entity configuration
   * @return Validation result
   */
  public ValidationResult validate(Path projectPath, EntityConfig config) {
    List<String> errors = new ArrayList<>();

    // Check project is initialized
    if (!isProjectInitialized(projectPath)) {
      errors.add("Project is not initialized. Run 'initCleanArch' first.");
      return ValidationResult.failure(errors);
    }

    // Validate entity name
    if (config.name() == null || config.name().isBlank()) {
      errors.add("Entity name is required");
    } else if (!isValidJavaClassName(config.name())) {
      errors.add("Entity name must be a valid Java class name (PascalCase, no spaces or special characters)");
    }

    // Validate fields
    if (config.fields() == null || config.fields().isEmpty()) {
      errors.add("Entity must have at least one field");
    } else {
      validateFields(config.fields(), errors);
    }

    // Validate ID type
    if (config.hasId() && !isValidIdType(config.idType())) {
      errors.add("Invalid ID type: " + config.idType() + ". Valid types: String, Long, UUID");
    }

    return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
  }

  /**
   * Checks if project is initialized (has .cleanarch.yml).
   */
  private boolean isProjectInitialized(Path projectPath) {
    Path configFile = projectPath.resolve(".cleanarch.yml");
    return fileSystemPort.exists(configFile);
  }

  /**
   * Validates Java class name format.
   */
  private boolean isValidJavaClassName(String name) {
    if (name == null || name.isEmpty()) {
      return false;
    }

    // Must start with uppercase letter
    if (!Character.isUpperCase(name.charAt(0))) {
      return false;
    }

    // Must contain only letters and numbers
    return name.matches("[A-Z][a-zA-Z0-9]*");
  }

  /**
   * Validates entity fields.
   */
  private void validateFields(List<EntityConfig.EntityField> fields, List<String> errors) {
    for (EntityConfig.EntityField field : fields) {
      if (field.name() == null || field.name().isBlank()) {
        errors.add("Field name cannot be empty");
      } else if (!isValidJavaFieldName(field.name())) {
        errors.add("Invalid field name: " + field.name() + ". Must be camelCase");
      }

      if (field.type() == null || field.type().isBlank()) {
        errors.add("Field type cannot be empty for field: " + field.name());
      }
    }
  }

  /**
   * Validates Java field name format (camelCase).
   */
  private boolean isValidJavaFieldName(String name) {
    if (name == null || name.isEmpty()) {
      return false;
    }

    // Must start with lowercase letter
    if (!Character.isLowerCase(name.charAt(0))) {
      return false;
    }

    // Must contain only letters and numbers
    return name.matches("[a-z][a-zA-Z0-9]*");
  }

  /**
   * Validates ID type.
   */
  private boolean isValidIdType(String idType) {
    return idType != null &&
        (idType.equals("String") || idType.equals("Long") || idType.equals("UUID"));
  }
}
