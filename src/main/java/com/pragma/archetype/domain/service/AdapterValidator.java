package com.pragma.archetype.domain.service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.pragma.archetype.domain.model.AdapterConfig;
import com.pragma.archetype.domain.model.ValidationResult;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.port.out.FileSystemPort;

/**
 * Validates adapter generation requests.
 * Domain service that encapsulates validation logic.
 */
public class AdapterValidator {

  private final FileSystemPort fileSystemPort;
  private final ConfigurationPort configurationPort;

  public AdapterValidator(FileSystemPort fileSystemPort, ConfigurationPort configurationPort) {
    this.fileSystemPort = fileSystemPort;
    this.configurationPort = configurationPort;
  }

  /**
   * Validates adapter configuration.
   */
  public ValidationResult validate(Path projectPath, AdapterConfig config) {
    List<String> errors = new ArrayList<>();

    // Validate project exists
    if (!fileSystemPort.directoryExists(projectPath)) {
      errors.add("Project directory does not exist: " + projectPath);
      return ValidationResult.failure(errors);
    }

    // Validate adapter name
    if (config.name() == null || config.name().isBlank()) {
      errors.add("Adapter name is required");
    } else if (!isValidJavaIdentifier(config.name())) {
      errors.add("Adapter name must be a valid Java identifier: " + config.name());
    }

    // Validate package name
    if (config.packageName() == null || config.packageName().isBlank()) {
      errors.add("Package name is required");
    } else if (!isValidPackageName(config.packageName())) {
      errors.add("Invalid package name: " + config.packageName());
    } else if (config.packageName().contains(".adapter.out.")) {
      errors.add(
          "Invalid package structure: use 'driven-adapters' instead of 'adapter.out'. Example: com.company.infrastructure.driven-adapters.redis");
    } else if (config.packageName().contains(".adapter.in.")) {
      errors.add(
          "Invalid package structure: use 'entry-points' instead of 'adapter.in'. Example: com.company.infrastructure.entry-points.rest");
    }

    // Validate adapter type
    if (config.type() == null) {
      errors.add("Adapter type is required");
    }

    // Validate entity name
    if (config.entityName() == null || config.entityName().isBlank()) {
      errors.add("Entity name is required");
    }

    // Validate methods (optional - can have default CRUD methods)
    if (config.methods() != null && !config.methods().isEmpty()) {
      validateMethods(config.methods(), errors);
    }

    return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
  }

  private void validateMethods(List<AdapterConfig.AdapterMethod> methods, List<String> errors) {
    for (AdapterConfig.AdapterMethod method : methods) {
      if (method.name() == null || method.name().isBlank()) {
        errors.add("Method name is required");
      } else if (!isValidJavaIdentifier(method.name())) {
        errors.add("Invalid method name: " + method.name());
      }

      if (method.returnType() == null || method.returnType().isBlank()) {
        errors.add("Method return type is required for method: " + method.name());
      }

      if (method.parameters() != null) {
        for (AdapterConfig.MethodParameter param : method.parameters()) {
          if (param.name() == null || param.name().isBlank()) {
            errors.add("Parameter name is required in method: " + method.name());
          } else if (!isValidJavaIdentifier(param.name())) {
            errors.add("Invalid parameter name: " + param.name() + " in method: " + method.name());
          }

          if (param.type() == null || param.type().isBlank()) {
            errors.add("Parameter type is required for parameter: " + param.name());
          }
        }
      }
    }
  }

  private boolean isValidJavaIdentifier(String name) {
    if (name == null || name.isEmpty()) {
      return false;
    }
    if (!Character.isJavaIdentifierStart(name.charAt(0))) {
      return false;
    }
    for (int i = 1; i < name.length(); i++) {
      if (!Character.isJavaIdentifierPart(name.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  private boolean isValidPackageName(String packageName) {
    if (packageName == null || packageName.isEmpty()) {
      return false;
    }
    String[] parts = packageName.split("\\.");
    for (String part : parts) {
      if (!isValidJavaIdentifier(part)) {
        return false;
      }
    }
    return true;
  }
}
