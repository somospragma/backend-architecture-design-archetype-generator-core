package com.pragma.archetype.domain.service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.pragma.archetype.domain.model.adapter.AdapterConfig;
import com.pragma.archetype.domain.model.adapter.AdapterMethod;
import com.pragma.archetype.domain.model.adapter.MethodParameter;
import com.pragma.archetype.domain.model.validation.ValidationResult;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.port.out.FileSystemPort;

/**
 * Validates adapter generation requests.
 * Domain service that encapsulates validation logic.
 */
public class AdapterValidator {

  private final FileSystemPort fileSystemPort;
  private final ConfigurationPort configurationPort;
  private final PackageValidator packageValidator;

  public AdapterValidator(FileSystemPort fileSystemPort, ConfigurationPort configurationPort,
      PackageValidator packageValidator) {
    this.fileSystemPort = fileSystemPort;
    this.configurationPort = configurationPort;
    this.packageValidator = packageValidator;
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
    } else {
      // Use PackageValidator for comprehensive package validation
      ValidationResult packageValidation = packageValidator.validatePackageName(config.packageName());
      if (!packageValidation.valid()) {
        errors.addAll(packageValidation.errors());
      }

      // Additional adapter-specific package validation
      if (config.packageName().contains(".adapter.out.")) {
        errors.add(
            "Invalid package structure: use 'driven-adapters' instead of 'adapter.out'. Example: com.company.infrastructure.driven-adapters.redis");
      } else if (config.packageName().contains(".adapter.in.")) {
        errors.add(
            "Invalid package structure: use 'entry-points' instead of 'adapter.in'. Example: com.company.infrastructure.entry-points.rest");
      }

      // Validate base package consistency if project config is available
      var projectConfigOpt = configurationPort.readConfiguration(projectPath);
      if (projectConfigOpt.isPresent()) {
        String basePackage = projectConfigOpt.get().basePackage();
        if (basePackage != null && !basePackage.isBlank()) {
          ValidationResult basePackageValidation = packageValidator.validateBasePackageConsistency(config.packageName(),
              basePackage);
          if (!basePackageValidation.valid()) {
            errors.addAll(basePackageValidation.errors());
          }
        }
      }
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

    // Check for duplicate adapter (Requirement 18.4)
    if (config.name() != null && !config.name().isBlank() &&
        config.packageName() != null && !config.packageName().isBlank()) {
      Path adapterFilePath = resolveAdapterFilePath(projectPath, config);
      if (fileSystemPort.exists(adapterFilePath)) {
        errors.add("Adapter already exists: " + config.name() + "Adapter.java at " + adapterFilePath +
            ". Please use a different name or remove the existing adapter before generating a new one.");
      }
    }

    return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
  }

  /**
   * Resolves the file path where the adapter would be generated.
   * This is used to check if an adapter already exists.
   * 
   * @param projectPath the project root path
   * @param config      the adapter configuration
   * @return the resolved adapter file path
   */
  private Path resolveAdapterFilePath(Path projectPath, AdapterConfig config) {
    // Convert package name to file path
    String packagePath = config.packageName().replace('.', '/');

    // Build the adapter file path:
    // src/main/java/{packagePath}/{AdapterName}Adapter.java
    return projectPath
        .resolve("src/main/java")
        .resolve(packagePath)
        .resolve(config.name() + "Adapter.java");
  }

  private void validateMethods(List<AdapterMethod> methods, List<String> errors) {
    for (AdapterMethod method : methods) {
      if (method.name() == null || method.name().isBlank()) {
        errors.add("Method name is required");
      } else if (!isValidJavaIdentifier(method.name())) {
        errors.add("Invalid method name: " + method.name());
      }

      if (method.returnType() == null || method.returnType().isBlank()) {
        errors.add("Method return type is required for method: " + method.name());
      }

      if (method.parameters() != null) {
        for (MethodParameter param : method.parameters()) {
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
