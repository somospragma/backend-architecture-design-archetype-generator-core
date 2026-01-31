package com.pragma.archetype.domain.service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.pragma.archetype.domain.model.UseCaseConfig;
import com.pragma.archetype.domain.model.ValidationResult;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.port.out.FileSystemPort;

/**
 * Validates use case generation requests.
 * Domain service that encapsulates validation logic.
 */
public class UseCaseValidator {

  private final FileSystemPort fileSystemPort;
  private final ConfigurationPort configurationPort;

  public UseCaseValidator(FileSystemPort fileSystemPort, ConfigurationPort configurationPort) {
    this.fileSystemPort = fileSystemPort;
    this.configurationPort = configurationPort;
  }

  /**
   * Validates use case configuration.
   */
  public ValidationResult validate(Path projectPath, UseCaseConfig config) {
    List<String> errors = new ArrayList<>();

    // Validate project exists
    if (!fileSystemPort.directoryExists(projectPath)) {
      errors.add("Project directory does not exist: " + projectPath);
      return ValidationResult.failure(errors);
    }

    // Validate use case name
    if (config.name() == null || config.name().isBlank()) {
      errors.add("Use case name is required");
    } else if (!isValidJavaIdentifier(config.name())) {
      errors.add("Use case name must be a valid Java identifier: " + config.name());
    }

    // Validate package name
    if (config.packageName() == null || config.packageName().isBlank()) {
      errors.add("Package name is required");
    } else if (!isValidPackageName(config.packageName())) {
      errors.add("Invalid package name: " + config.packageName());
    }

    // Validate methods
    if (config.methods() == null || config.methods().isEmpty()) {
      errors.add("At least one method is required");
    } else {
      validateMethods(config.methods(), errors);
    }

    // Validate at least one generation option is enabled
    if (!config.generatePort() && !config.generateImpl()) {
      errors.add("At least one of generatePort or generateImpl must be true");
    }

    return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
  }

  private void validateMethods(List<UseCaseConfig.UseCaseMethod> methods, List<String> errors) {
    for (UseCaseConfig.UseCaseMethod method : methods) {
      if (method.name() == null || method.name().isBlank()) {
        errors.add("Method name is required");
      } else if (!isValidJavaIdentifier(method.name())) {
        errors.add("Invalid method name: " + method.name());
      }

      if (method.returnType() == null || method.returnType().isBlank()) {
        errors.add("Method return type is required for method: " + method.name());
      }

      if (method.parameters() != null) {
        for (UseCaseConfig.MethodParameter param : method.parameters()) {
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
