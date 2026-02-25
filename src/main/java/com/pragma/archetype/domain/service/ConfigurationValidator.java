package com.pragma.archetype.domain.service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.pragma.archetype.domain.model.StructureMetadata;
import com.pragma.archetype.domain.model.ValidationResult;
import com.pragma.archetype.domain.model.adapter.AdapterMetadata;
import com.pragma.archetype.domain.model.config.ProjectConfig;
import com.pragma.archetype.domain.model.config.TemplateConfig;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.port.out.FileSystemPort;

/**
 * Domain service responsible for validating configuration files and metadata.
 * Validates .cleanarch.yml, template configuration, structure.yml, and
 * metadata.yml files.
 */
public class ConfigurationValidator {

  private final FileSystemPort fileSystemPort;
  private final ConfigurationPort configurationPort;

  public ConfigurationValidator(FileSystemPort fileSystemPort, ConfigurationPort configurationPort) {
    this.fileSystemPort = fileSystemPort;
    this.configurationPort = configurationPort;
  }

  /**
   * Validates the project configuration file (.cleanarch.yml).
   * 
   * Validation rules:
   * 1. Configuration file must exist
   * 2. Configuration must be parseable (valid YAML)
   * 3. All required fields must be present
   * 4. Field values must be valid
   * 
   * @param projectPath the root directory of the project
   * @return ValidationResult with success status and any error messages
   */
  public ValidationResult validateProjectConfig(Path projectPath) {
    List<String> errors = new ArrayList<>();

    // 1. Check if configuration file exists
    if (!configurationPort.configurationExists(projectPath)) {
      errors.add("Configuration file .cleanarch.yml not found in: " + projectPath);
      errors.add("Run 'initCleanArch' to create initial project structure.");
      errors.add("See: https://github.com/somospragma/backend-architecture-design-archetype-generator");
      return ValidationResult.failure(errors);
    }

    // 2. Try to read and parse configuration
    try {
      var configOpt = configurationPort.readConfiguration(projectPath);
      if (configOpt.isEmpty()) {
        errors.add("Failed to parse .cleanarch.yml file");
        errors.add("Check YAML syntax and ensure all required fields are present");
        return ValidationResult.failure(errors);
      }

      ProjectConfig config = configOpt.get();

      // 3. Validate required fields are present
      if (config.name() == null || config.name().isBlank()) {
        errors.add("Missing required field 'project.name' in .cleanarch.yml");
        errors.add("Example: project:\n  name: my-service");
      }

      if (config.basePackage() == null || config.basePackage().isBlank()) {
        errors.add("Missing required field 'project.basePackage' in .cleanarch.yml");
        errors.add("Example: project:\n  basePackage: com.example.myservice");
      }

      if (config.architecture() == null) {
        errors.add("Missing required field 'architecture.type' in .cleanarch.yml");
        errors.add("Example: architecture:\n  type: hexagonal-single");
      }

      if (config.paradigm() == null) {
        errors.add("Missing required field 'architecture.paradigm' in .cleanarch.yml");
        errors.add("Example: architecture:\n  paradigm: reactive");
      }

      if (config.framework() == null) {
        errors.add("Missing required field 'architecture.framework' in .cleanarch.yml");
        errors.add("Example: architecture:\n  framework: spring");
      }

      // 4. Validate field values
      if (config.basePackage() != null && !isValidPackageName(config.basePackage())) {
        errors.add("Invalid package name: " + config.basePackage());
        errors.add("Package name must follow Java naming conventions (e.g., com.company.service)");
      }

      if (config.name() != null && !isValidProjectName(config.name())) {
        errors.add("Invalid project name: " + config.name());
        errors.add("Project name must contain only lowercase letters, numbers, and hyphens");
      }

    } catch (Exception e) {
      errors.add("Failed to parse .cleanarch.yml: " + e.getMessage());
      errors.add("Check YAML syntax. Common issues:");
      errors.add("  - Missing colons after keys");
      errors.add("  - Incorrect indentation (use 2 spaces)");
      errors.add("  - Unquoted special characters");
      return ValidationResult.failure(errors);
    }

    return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
  }

  /**
   * Validates the template configuration section in .cleanarch.yml.
   * 
   * Validation rules:
   * 1. If localPath is specified, it must exist
   * 2. If remote mode, repository URL must be valid
   * 3. Branch name must be valid if specified
   * 4. Mode must be valid (production or developer)
   * 
   * @param templateConfig the template configuration to validate
   * @return ValidationResult with success status and any error messages
   */
  public ValidationResult validateTemplateConfig(TemplateConfig templateConfig) {
    List<String> errors = new ArrayList<>();

    if (templateConfig == null) {
      errors.add("Template configuration is null");
      return ValidationResult.failure(errors);
    }

    // Validate local path if specified
    if (templateConfig.localPath() != null && !templateConfig.localPath().isBlank()) {
      Path localPath = Path.of(templateConfig.localPath());
      if (!fileSystemPort.directoryExists(localPath)) {
        errors.add("Local template path does not exist: " + templateConfig.localPath());
        errors.add("Check the 'templates.localPath' configuration in .cleanarch.yml");
        errors.add("Example: templates:\n  localPath: ../backend-architecture-design-archetype-generator-templates");
      }
    }

    // Validate remote repository if in remote mode
    if (templateConfig.isRemoteMode()) {
      if (templateConfig.repository() == null || templateConfig.repository().isBlank()) {
        errors.add("Repository URL is required for remote mode");
        errors.add(
            "Example: templates:\n  repository: https://github.com/somospragma/backend-architecture-design-archetype-generator-templates");
      } else if (!isValidRepositoryUrl(templateConfig.repository())) {
        errors.add("Invalid repository URL: " + templateConfig.repository());
        errors.add("Repository URL must be a valid HTTPS GitHub URL");
      }

      // Validate branch name
      if (templateConfig.branch() != null && !isValidBranchName(templateConfig.branch())) {
        errors.add("Invalid branch name: " + templateConfig.branch());
        errors.add("Branch name must contain only alphanumeric characters, hyphens, underscores, and slashes");
      }
    }

    // Validate mode
    if (templateConfig.mode() == null) {
      errors.add("Template mode cannot be null");
      errors.add("Valid modes: PRODUCTION, DEVELOPER");
    }

    return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
  }

  /**
   * Validates structure metadata (structure.yml).
   * 
   * Validation rules:
   * 1. Architecture type must be specified
   * 2. Adapter paths must be defined
   * 3. Adapter paths must contain 'driven' or 'driving' entries
   * 4. Path templates must be valid
   * 
   * @param metadata the structure metadata to validate
   * @return ValidationResult with success status and any error messages
   */
  public ValidationResult validateStructureMetadata(StructureMetadata metadata) {
    List<String> errors = new ArrayList<>();

    if (metadata == null) {
      errors.add("Structure metadata is null");
      return ValidationResult.failure(errors);
    }

    // Validate architecture type
    if (metadata.architectureType() == null || metadata.architectureType().isBlank()) {
      errors.add("Architecture type is required in structure.yml");
      errors.add("Example: architectureType: hexagonal-single");
    }

    // Validate adapter paths
    if (metadata.adapterPaths() == null || metadata.adapterPaths().isEmpty()) {
      errors.add("Adapter paths are required in structure.yml");
      errors.add(
          "Example: adapterPaths:\n  driven: infrastructure/adapter/out/{name}\n  driving: infrastructure/adapter/in/{name}");
    } else {
      // Check for required adapter path types
      if (!metadata.adapterPaths().containsKey("driven") && !metadata.adapterPaths().containsKey("driving")) {
        errors.add("Adapter paths must contain at least 'driven' or 'driving' entries");
        errors.add("Found keys: " + metadata.adapterPaths().keySet());
      }

      // Validate path templates contain {name} placeholder
      for (var entry : metadata.adapterPaths().entrySet()) {
        String pathTemplate = entry.getValue();
        if (pathTemplate == null || pathTemplate.isBlank()) {
          errors.add("Adapter path for '" + entry.getKey() + "' cannot be empty");
        } else if (!pathTemplate.contains("{name}")) {
          errors.add("Adapter path for '" + entry.getKey() + "' must contain {name} placeholder: " + pathTemplate);
          errors.add("Example: infrastructure/adapter/out/{name}");
        }
      }
    }

    // Use the built-in validation method
    ValidationResult builtInValidation = metadata.validate();
    if (builtInValidation.isInvalid()) {
      errors.addAll(builtInValidation.errors());
    }

    return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
  }

  /**
   * Validates adapter metadata (metadata.yml).
   * 
   * Validation rules:
   * 1. Adapter name must be specified
   * 2. Adapter type must be specified
   * 3. Dependencies must be valid
   * 4. Referenced template files must exist (if file system access available)
   * 5. Configuration classes must have valid names
   * 
   * @param metadata the adapter metadata to validate
   * @return ValidationResult with success status and any error messages
   */
  public ValidationResult validateAdapterMetadata(AdapterMetadata metadata) {
    List<String> errors = new ArrayList<>();

    if (metadata == null) {
      errors.add("Adapter metadata is null");
      return ValidationResult.failure(errors);
    }

    // Validate adapter name
    if (metadata.name() == null || metadata.name().isBlank()) {
      errors.add("Adapter name is required in metadata.yml");
      errors.add("Example: name: mongodb-adapter");
    } else if (!isValidAdapterName(metadata.name())) {
      errors.add("Invalid adapter name: " + metadata.name());
      errors.add("Adapter name must contain only lowercase letters, numbers, and hyphens");
    }

    // Validate adapter type
    if (metadata.type() == null || metadata.type().isBlank()) {
      errors.add("Adapter type is required in metadata.yml");
      errors.add("Valid types: driven, driving");
      errors.add("Example: type: driven");
    } else if (!isValidAdapterType(metadata.type())) {
      errors.add("Invalid adapter type: " + metadata.type());
      errors.add("Valid types: driven, driving");
    }

    // Validate dependencies
    if (metadata.dependencies() != null) {
      for (int i = 0; i < metadata.dependencies().size(); i++) {
        var dep = metadata.dependencies().get(i);
        if (dep.group() == null || dep.group().isBlank()) {
          errors.add("Dependency " + (i + 1) + " is missing 'group' field");
        }
        if (dep.artifact() == null || dep.artifact().isBlank()) {
          errors.add("Dependency " + (i + 1) + " is missing 'artifact' field");
        }
      }
    }

    // Validate test dependencies
    if (metadata.testDependencies() != null) {
      for (int i = 0; i < metadata.testDependencies().size(); i++) {
        var dep = metadata.testDependencies().get(i);
        if (dep.group() == null || dep.group().isBlank()) {
          errors.add("Test dependency " + (i + 1) + " is missing 'group' field");
        }
        if (dep.artifact() == null || dep.artifact().isBlank()) {
          errors.add("Test dependency " + (i + 1) + " is missing 'artifact' field");
        }
      }
    }

    // Validate configuration classes
    if (metadata.configurationClasses() != null) {
      for (var configClass : metadata.configurationClasses()) {
        if (configClass.name() == null || configClass.name().isBlank()) {
          errors.add("Configuration class is missing 'name' field");
        } else if (!isValidJavaClassName(configClass.name())) {
          errors.add("Invalid configuration class name: " + configClass.name());
          errors.add("Class name must be a valid Java identifier");
        }

        if (configClass.packagePath() == null || configClass.packagePath().isBlank()) {
          errors.add("Configuration class '" + configClass.name() + "' is missing 'packagePath' field");
        }

        if (configClass.templatePath() == null || configClass.templatePath().isBlank()) {
          errors.add("Configuration class '" + configClass.name() + "' is missing 'templatePath' field");
        }
      }
    }

    // Use the built-in validation method
    ValidationResult builtInValidation = metadata.validate();
    if (builtInValidation.isInvalid()) {
      errors.addAll(builtInValidation.errors());
    }

    return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
  }

  /**
   * Validates a Java package name.
   */
  private boolean isValidPackageName(String packageName) {
    if (packageName == null || packageName.isBlank()) {
      return false;
    }

    String[] segments = packageName.split("\\.");
    if (segments.length < 2) {
      return false;
    }

    for (String segment : segments) {
      if (segment.isEmpty() || !segment.matches("[a-z][a-z0-9_]*")) {
        return false;
      }
    }

    return true;
  }

  /**
   * Validates a project name.
   */
  private boolean isValidProjectName(String projectName) {
    if (projectName == null || projectName.isBlank()) {
      return false;
    }
    return projectName.matches("[a-z][a-z0-9-]*");
  }

  /**
   * Validates a repository URL.
   */
  private boolean isValidRepositoryUrl(String url) {
    if (url == null || url.isBlank()) {
      return false;
    }
    return url.startsWith("https://github.com/") || url.startsWith("https://gitlab.com/");
  }

  /**
   * Validates a branch name.
   */
  private boolean isValidBranchName(String branch) {
    if (branch == null || branch.isBlank()) {
      return false;
    }
    return branch.matches("[a-zA-Z0-9/_-]+");
  }

  /**
   * Validates an adapter name.
   */
  private boolean isValidAdapterName(String name) {
    if (name == null || name.isBlank()) {
      return false;
    }
    return name.matches("[a-z][a-z0-9-]*");
  }

  /**
   * Validates an adapter type.
   */
  private boolean isValidAdapterType(String type) {
    if (type == null || type.isBlank()) {
      return false;
    }
    return type.equals("driven") || type.equals("driving");
  }

  /**
   * Validates a Java class name.
   */
  private boolean isValidJavaClassName(String className) {
    if (className == null || className.isBlank()) {
      return false;
    }
    return className.matches("[A-Z][a-zA-Z0-9]*");
  }
}
