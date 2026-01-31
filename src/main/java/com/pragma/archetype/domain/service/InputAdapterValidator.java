package com.pragma.archetype.domain.service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.pragma.archetype.domain.model.InputAdapterConfig;
import com.pragma.archetype.domain.model.ValidationResult;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.port.out.FileSystemPort;

/**
 * Validates input adapter generation requests.
 * Domain service that encapsulates validation logic.
 */
public class InputAdapterValidator {

  private final FileSystemPort fileSystemPort;
  private final ConfigurationPort configurationPort;

  public InputAdapterValidator(FileSystemPort fileSystemPort, ConfigurationPort configurationPort) {
    this.fileSystemPort = fileSystemPort;
    this.configurationPort = configurationPort;
  }

  /**
   * Validates input adapter configuration.
   */
  public ValidationResult validate(Path projectPath, InputAdapterConfig config) {
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

    // Validate use case name
    if (config.useCaseName() == null || config.useCaseName().isBlank()) {
      errors.add("Use case name is required");
    }

    // Validate endpoints
    if (config.endpoints() == null || config.endpoints().isEmpty()) {
      errors.add("At least one endpoint is required");
    } else {
      validateEndpoints(config.endpoints(), errors);
    }

    return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
  }

  private void validateEndpoints(List<InputAdapterConfig.Endpoint> endpoints, List<String> errors) {
    for (InputAdapterConfig.Endpoint endpoint : endpoints) {
      if (endpoint.path() == null || endpoint.path().isBlank()) {
        errors.add("Endpoint path is required");
      } else if (!endpoint.path().startsWith("/")) {
        errors.add("Endpoint path must start with '/': " + endpoint.path());
      }

      if (endpoint.method() == null) {
        errors.add("HTTP method is required for endpoint: " + endpoint.path());
      }

      if (endpoint.useCaseMethod() == null || endpoint.useCaseMethod().isBlank()) {
        errors.add("Use case method is required for endpoint: " + endpoint.path());
      }

      if (endpoint.returnType() == null || endpoint.returnType().isBlank()) {
        errors.add("Return type is required for endpoint: " + endpoint.path());
      }

      if (endpoint.parameters() != null) {
        for (InputAdapterConfig.EndpointParameter param : endpoint.parameters()) {
          if (param.name() == null || param.name().isBlank()) {
            errors.add("Parameter name is required in endpoint: " + endpoint.path());
          }

          if (param.type() == null || param.type().isBlank()) {
            errors.add("Parameter type is required for parameter: " + param.name());
          }

          if (param.paramType() == null) {
            errors.add("Parameter type (PATH/BODY/QUERY) is required for parameter: " + param.name());
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
