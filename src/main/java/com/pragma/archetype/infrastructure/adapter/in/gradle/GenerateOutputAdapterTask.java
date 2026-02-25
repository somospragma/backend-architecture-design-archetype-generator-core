package com.pragma.archetype.infrastructure.adapter.in.gradle;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

import com.pragma.archetype.application.generator.AdapterGenerator;
import com.pragma.archetype.application.usecase.GenerateAdapterUseCaseImpl;
import com.pragma.archetype.domain.model.adapter.AdapterConfig;
import com.pragma.archetype.domain.model.adapter.AdapterMethod;
import com.pragma.archetype.domain.model.adapter.AdapterType;
import com.pragma.archetype.domain.model.adapter.MethodParameter;
import com.pragma.archetype.domain.model.config.ProjectConfig;
import com.pragma.archetype.domain.model.validation.ValidationResult;
import com.pragma.archetype.domain.port.in.GenerateAdapterUseCase;
import com.pragma.archetype.domain.port.in.GenerateAdapterUseCase.GenerationResult;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import com.pragma.archetype.domain.port.out.PathResolver;
import com.pragma.archetype.domain.port.out.TemplateRepository;
import com.pragma.archetype.domain.service.AdapterValidator;
import com.pragma.archetype.domain.service.ConfigurationValidator;
import com.pragma.archetype.infrastructure.adapter.out.config.YamlConfigurationAdapter;
import com.pragma.archetype.infrastructure.adapter.out.filesystem.LocalFileSystemAdapter;
import com.pragma.archetype.infrastructure.adapter.out.template.FreemarkerTemplateRepository;

/**
 * Gradle task for generating output adapters (driven adapters: Redis, MongoDB,
 * etc.).
 * 
 * Usage:
 * ./gradlew generateOutputAdapter --name=UserRepository
 * --entity=User --type=redis
 * --packageName=com.pragma.infrastructure.driven-adapters.redis
 */
public class GenerateOutputAdapterTask extends DefaultTask {

  private String adapterName = "";
  private String entityName = "";
  private String type = "redis";
  private String packageName = "";
  private String methods = "";

  @Option(option = "name", description = "Adapter name (e.g., UserRepository, ProductCache)")
  public void setAdapterName(String adapterName) {
    this.adapterName = adapterName;
  }

  @Input
  public String getAdapterName() {
    return adapterName;
  }

  @Option(option = "entity", description = "Entity name (e.g., User, Product)")
  public void setEntityName(String entityName) {
    this.entityName = entityName;
  }

  @Input
  public String getEntityName() {
    return entityName;
  }

  @Option(option = "type", description = "Adapter type: redis, mongodb, postgresql, rest-client, kafka (default: redis)")
  public void setType(String type) {
    this.type = type;
  }

  @Input
  public String getType() {
    return type;
  }

  @Option(option = "packageName", description = "Package name (optional, auto-detected from .cleanarch.yml)")
  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  @Input
  @Optional
  public String getPackageName() {
    return packageName;
  }

  @Option(option = "methods", description = "Custom methods (optional, format: methodName:ReturnType:param1:Type1)")
  public void setMethods(String methods) {
    this.methods = methods;
  }

  @Input
  public String getMethods() {
    return methods;
  }

  @TaskAction
  public void generateAdapter() {
    getLogger().lifecycle("Generating {} adapter: {}", type, adapterName);

    try {
      // 1. Validate project configuration exists
      Path projectPath = getProject().getProjectDir().toPath();
      FileSystemPort fileSystemPort = new LocalFileSystemAdapter();
      ConfigurationPort configurationPort = new YamlConfigurationAdapter();

      ConfigurationValidator configValidator = new ConfigurationValidator(fileSystemPort, configurationPort);
      ValidationResult configValidation = configValidator.validateProjectConfig(projectPath);

      if (configValidation.isInvalid()) {
        getLogger().error("✗ Configuration validation failed:");
        configValidation.errors().forEach(error -> getLogger().error("  {}", error));
        throw new RuntimeException("Configuration validation failed. Please fix the errors above.");
      }

      // 2. Validate inputs
      validateInputs();

      // 3. Resolve package name (auto-detect if not provided)
      String resolvedPackageName = resolvePackageName(type);

      // 4. Parse adapter type
      AdapterType adapterType = parseAdapterType(type);

      // 5. Parse methods (if provided)
      List<AdapterMethod> adapterMethods = new ArrayList<>();
      if (!methods.isBlank()) {
        adapterMethods = parseMethods(methods);
      }

      // 6. Create configuration
      AdapterConfig config = AdapterConfig.builder()
          .name(adapterName)
          .entityName(entityName)
          .type(adapterType)
          .packageName(resolvedPackageName)
          .methods(adapterMethods)
          .build();

      // 7. Setup dependencies (reuse instances from validation)
      YamlConfigurationAdapter yamlConfigurationAdapter = (YamlConfigurationAdapter) configurationPort;
      TemplateRepository templateRepository = createTemplateRepository();
      PathResolver pathResolver = new com.pragma.archetype.domain.service.PathResolverImpl(templateRepository);

      // 8. Setup use case
      com.pragma.archetype.domain.service.PackageValidator packageValidator = new com.pragma.archetype.domain.service.PackageValidator();
      AdapterValidator validator = new AdapterValidator(fileSystemPort, configurationPort, packageValidator);
      AdapterGenerator generator = new AdapterGenerator(templateRepository, fileSystemPort, pathResolver);
      com.pragma.archetype.domain.service.BackupService backupService = new com.pragma.archetype.domain.service.BackupService(
          fileSystemPort);
      GenerateAdapterUseCase useCase = new GenerateAdapterUseCaseImpl(
          validator,
          generator,
          configurationPort,
          fileSystemPort,
          templateRepository,
          yamlConfigurationAdapter,
          backupService);

      // 9. Execute use case
      GenerationResult result = useCase.execute(projectPath, config);

      // 10. Handle result
      if (result.success()) {
        getLogger().lifecycle("✓ Adapter generated successfully!");
        getLogger().lifecycle("  Generated {} file(s)", result.generatedFiles().size());
        result.generatedFiles().forEach(file -> getLogger().lifecycle("    - {}", file.path()));
      } else {
        getLogger().error("✗ Failed to generate adapter:");
        result.errors().forEach(error -> getLogger().error("  - {}", error));
        throw new RuntimeException("Adapter generation failed");
      }

    } catch (Exception e) {
      getLogger().error("✗ Error generating adapter: {}", e.getMessage());
      throw new RuntimeException("Adapter generation failed", e);
    }
  }

  /**
   * Validates task inputs.
   */
  private void validateInputs() {
    if (adapterName.isBlank()) {
      throw new IllegalArgumentException(
          "Adapter name is required. Use --name=UserRepository");
    }

    if (entityName.isBlank()) {
      throw new IllegalArgumentException(
          "Entity name is required. Use --entity=User");
    }
  }

  /**
   * Resolves package name from .cleanarch.yml if not provided.
   */
  private String resolvePackageName(String adapterType) {
    // If packageName is provided, use it
    if (packageName != null && !packageName.isBlank()) {
      return packageName;
    }

    // Otherwise, read from .cleanarch.yml
    try {
      ConfigurationPort configurationPort = new YamlConfigurationAdapter();
      Path projectPath = getProject().getProjectDir().toPath();
      ProjectConfig projectConfig = configurationPort.readConfiguration(projectPath)
          .orElseThrow(() -> new IllegalArgumentException(".cleanarch.yml not found"));

      // For hexagonal-single, driven adapters go in
      // infrastructure.drivenadapters.{type}
      return projectConfig.basePackage() + ".infrastructure.drivenadapters." + adapterType.toLowerCase();
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "Could not auto-detect package name. Please provide --packageName or ensure .cleanarch.yml exists", e);
    }
  }

  /**
   * Parses adapter type string.
   */
  private AdapterType parseAdapterType(String typeStr) {
    return switch (typeStr.toLowerCase()) {
      case "redis" -> AdapterType.REDIS;
      case "mongodb", "mongo" -> AdapterType.MONGODB;
      case "postgresql", "postgres" -> AdapterType.POSTGRESQL;
      case "rest-client", "rest" -> AdapterType.REST_CLIENT;
      case "kafka" -> AdapterType.KAFKA;
      default -> throw new IllegalArgumentException(
          "Invalid adapter type: " + typeStr + ". Valid values: redis, mongodb, postgresql, rest-client, kafka");
    };
  }

  /**
   * Parses method string into AdapterMethod list.
   * Format: "methodName:ReturnType:param1:Type1|method2:ReturnType2"
   */
  private List<AdapterMethod> parseMethods(String methodsStr) {
    List<AdapterMethod> result = new ArrayList<>();

    String[] methodDefinitions = methodsStr.split("\\|");
    for (String methodDef : methodDefinitions) {
      String[] parts = methodDef.trim().split(":");
      if (parts.length < 2) {
        throw new IllegalArgumentException(
            "Invalid method format: " + methodDef + ". Expected format: methodName:ReturnType[:param1:Type1]");
      }

      String methodName = parts[0].trim();
      String returnType = parts[1].trim();
      List<MethodParameter> parameters = new ArrayList<>();

      // Parse parameters if present
      if (parts.length > 2) {
        for (int i = 2; i < parts.length; i += 2) {
          if (i + 1 < parts.length) {
            String paramName = parts[i].trim();
            String paramType = parts[i + 1].trim();
            parameters.add(new MethodParameter(paramName, paramType));
          }
        }
      }

      result.add(new AdapterMethod(methodName, returnType, parameters));
    }

    return result;
  }

  /**
   * Creates template repository.
   */
  private TemplateRepository createTemplateRepository() {
    // Try to find templates in project directory first (for development)
    Path projectDir = getProject().getProjectDir().toPath();
    Path localTemplates = projectDir
        .resolve("../../backend-architecture-design-archetype-generator-templates/templates").normalize();

    if (java.nio.file.Files.exists(localTemplates)) {
      getLogger().info("Using local templates from: {}", localTemplates.toAbsolutePath());
      return new FreemarkerTemplateRepository(localTemplates);
    }

    // Fall back to embedded templates (in JAR)
    getLogger().info("Using embedded templates");
    return new FreemarkerTemplateRepository("embedded");
  }
}
