package com.pragma.archetype.infrastructure.adapter.in.gradle;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

import com.pragma.archetype.application.usecase.GenerateInputAdapterUseCaseImpl;
import com.pragma.archetype.domain.model.adapter.Endpoint;
import com.pragma.archetype.domain.model.adapter.EndpointParameter;
import com.pragma.archetype.domain.model.adapter.HttpMethod;
import com.pragma.archetype.domain.model.adapter.InputAdapterConfig;
import com.pragma.archetype.domain.model.adapter.ParameterType;
import com.pragma.archetype.domain.model.validation.ValidationResult;
import com.pragma.archetype.domain.port.in.GenerateInputAdapterUseCase;
import com.pragma.archetype.domain.port.in.GenerateInputAdapterUseCase.GenerationResult;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import com.pragma.archetype.domain.port.out.PathResolver;
import com.pragma.archetype.domain.port.out.TemplateRepository;
import com.pragma.archetype.domain.service.ConfigurationValidator;
import com.pragma.archetype.domain.service.InputAdapterValidator;
import com.pragma.archetype.infrastructure.adapter.out.config.YamlConfigurationAdapter;
import com.pragma.archetype.infrastructure.adapter.out.filesystem.LocalFileSystemAdapter;
import com.pragma.archetype.infrastructure.adapter.out.template.FreemarkerTemplateRepository;

/**
 * Gradle task for generating input adapters (entry points: REST controllers,
 * GraphQL resolvers, etc.).
 * 
 * Usage:
 * ./gradlew generateInputAdapter --name=User --type=rest
 * --endpoints=/users:POST:create:User:userData:BODY:CreateUserRequest
 * 
 * Optional parameters:
 * --useCase=CreateUserUseCase (for documentation/reference only)
 * --packageName=com.pragma.infrastructure.entry-points.rest (auto-inferred if
 * not provided)
 */
public class GenerateInputAdapterTask extends BaseCleanArchTask {

  private String adapterName = "";
  private String useCaseName = "";
  private String endpoints = "";
  private String packageName = "";
  private String type = "rest";

  @Option(option = "name", description = "Adapter name (e.g., User, Product)")
  public void setAdapterName(String adapterName) {
    this.adapterName = adapterName;
  }

  @Input
  public String getAdapterName() {
    return adapterName;
  }

  @Option(option = "useCase", description = "Use case name (optional, for documentation/reference)")
  public void setUseCaseName(String useCaseName) {
    this.useCaseName = useCaseName;
  }

  @Input
  public String getUseCaseName() {
    return useCaseName;
  }

  @Option(option = "endpoints", description = "Endpoints (format: /path:METHOD:useCaseMethod:ReturnType:param:PARAMTYPE:Type|...)")
  public void setEndpoints(String endpoints) {
    this.endpoints = endpoints;
  }

  @Input
  public String getEndpoints() {
    return endpoints;
  }

  @Option(option = "packageName", description = "Package name (e.g., com.company.infrastructure.entry-points.rest)")
  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  @Input
  public String getPackageName() {
    return packageName;
  }

  @Option(option = "type", description = "Adapter type: rest, graphql, grpc, websocket (default: rest)")
  public void setType(String type) {
    this.type = type;
  }

  @Input
  public String getType() {
    return type;
  }

  @TaskAction
  public void generateInputAdapter() {
    getLogger().lifecycle("Generating input adapter: {}", adapterName);

    try {
      // 0. Update plugin version in .cleanarch.yml if needed
      updatePluginVersionIfNeeded();

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

      // 2. Infer package name if not provided
      if (packageName == null || packageName.isBlank()) {
        packageName = inferPackageName(projectPath, fileSystemPort, configurationPort);
        getLogger().lifecycle("  Inferred package: {}", packageName);
      }

      // 3. Read framework and paradigm from project config
      var projectConfig = configurationPort.readConfiguration(projectPath)
          .orElseThrow(() -> new RuntimeException("Project configuration not found"));
      String framework = projectConfig.framework().getValue();
      String paradigm = projectConfig.paradigm().getValue();

      // 4. Validate inputs
      validateInputs();

      // 5. Use adapter type as String (no need to parse to enum)
      String adapterType = type;

      // 6. Parse endpoints
      List<Endpoint> endpointList = parseEndpoints(endpoints);

      // 7. Create configuration
      InputAdapterConfig config = InputAdapterConfig.builder()
          .name(adapterName)
          .useCaseName(useCaseName)
          .type(adapterType)
          .packageName(packageName)
          .endpoints(endpointList)
          .framework(framework)
          .paradigm(paradigm)
          .build();

      // 7. Setup dependencies (reuse instances from validation)
      TemplateRepository templateRepository = createTemplateRepository();
      PathResolver pathResolver = new com.pragma.archetype.domain.service.PathResolverImpl(templateRepository);
      com.pragma.archetype.application.generator.ProjectGenerator projectGenerator = new com.pragma.archetype.application.generator.ProjectGenerator(
          templateRepository, fileSystemPort);

      // 8. Setup use case
      com.pragma.archetype.domain.service.PackageValidator packageValidator = new com.pragma.archetype.domain.service.PackageValidator();
      InputAdapterValidator validator = new InputAdapterValidator(fileSystemPort, configurationPort, packageValidator);
      // Create content provider that delegates to template repository
      com.pragma.archetype.infrastructure.adapter.out.template.TemplateContentProvider contentProvider = new com.pragma.archetype.infrastructure.adapter.out.template.TemplateContentProvider() {
        @Override
        public String getTemplateContent(String templatePath) {
          return templateRepository.getTemplateContent(templatePath);
        }

        @Override
        public boolean templateExists(String templatePath) {
          return templateRepository.templateExists(templatePath);
        }
      };
      com.pragma.archetype.infrastructure.adapter.out.template.AdapterMetadataLoader metadataLoader = new com.pragma.archetype.infrastructure.adapter.out.template.AdapterMetadataLoader(
          contentProvider);
      com.pragma.archetype.infrastructure.adapter.out.template.TemplateAdapterDiscovery adapterDiscovery = new com.pragma.archetype.infrastructure.adapter.out.template.TemplateAdapterDiscovery(
          templateRepository, metadataLoader);

      // Use UnifiedAdapterGenerator instead of InputAdapterGenerator
      com.pragma.archetype.application.generator.UnifiedAdapterGenerator generator = new com.pragma.archetype.application.generator.UnifiedAdapterGenerator(
          templateRepository, pathResolver, adapterDiscovery, projectGenerator);

      // Cast configurationPort to YamlConfigurationAdapter for YAML operations
      YamlConfigurationAdapter yamlConfigurationAdapter = (YamlConfigurationAdapter) configurationPort;

      GenerateInputAdapterUseCase useCase = new GenerateInputAdapterUseCaseImpl(
          generator,
          configurationPort,
          fileSystemPort,
          adapterDiscovery,
          templateRepository,
          yamlConfigurationAdapter);

      // 9. Execute use case
      GenerationResult result = useCase.execute(projectPath, config);

      // 10. Handle result
      if (result.success()) {
        getLogger().lifecycle("✓ Input adapter generated successfully!");
        getLogger().lifecycle("  Generated {} file(s)", result.generatedFiles().size());
        result.generatedFiles().forEach(file -> getLogger().lifecycle("    - {}", file.path()));
      } else {
        getLogger().error("✗ Failed to generate input adapter:");
        result.errors().forEach(error -> getLogger().error("  - {}", error));
        throw new RuntimeException("Input adapter generation failed");
      }

    } catch (Exception e) {
      getLogger().error("✗ Error generating input adapter: {}", e.getMessage());
      throw new RuntimeException("Input adapter generation failed", e);
    }
  }

  /**
   * Infers package name from .cleanarch.yml basePackage and structure.yml
   * adapterPaths.
   * This eliminates the need for users to specify --packageName explicitly.
   */
  private String inferPackageName(Path projectPath, FileSystemPort fileSystemPort,
      ConfigurationPort configurationPort) {
    try {
      // Read project configuration to get basePackage and architecture
      var projectConfig = configurationPort.readConfiguration(projectPath)
          .orElseThrow(() -> new IllegalStateException(".cleanarch.yml not found"));
      var basePackage = projectConfig.basePackage();
      var architecture = projectConfig.architecture();

      getLogger().debug("Base package from .cleanarch.yml: {}", basePackage);
      getLogger().debug("Architecture from .cleanarch.yml: {}", architecture);

      // Load StructureMetadata to get dynamic adapter paths
      TemplateRepository templateRepository = createTemplateRepository();
      com.pragma.archetype.domain.model.structure.StructureMetadata structureMetadata = templateRepository
          .loadStructureMetadata(architecture);

      // Get adapter base path for driving adapters (input/entry-points)
      String adapterBasePath = structureMetadata.getAdapterBaseDirectory("driving");

      // Transform path to package name using structure metadata
      String adapterPackageSuffix = structureMetadata.pathToPackageName(adapterBasePath);

      // Construct full package: basePackage + adapterPackageSuffix + adapterType
      // Sanitize adapter type: remove hyphens for valid Java package names
      String sanitizedType = sanitizeAdapterType(type);
      String fullPackage = basePackage + "." + adapterPackageSuffix + "." + sanitizedType;

      getLogger().debug("Inferred package: {}", fullPackage);

      return fullPackage;

    } catch (Exception e) {
      throw new IllegalStateException(
          "Failed to infer package name. Please provide --packageName explicitly or ensure .cleanarch.yml exists with basePackage configured.",
          e);
    }
  }

  /**
   * Sanitizes adapter type for use in Java package names.
   * Removes hyphens and converts to lowercase.
   * 
   * @param adapterType the adapter type (e.g., "sqs-consumer", "rest")
   * @return sanitized type (e.g., "sqsconsumer", "rest")
   */
  private String sanitizeAdapterType(String adapterType) {
    if (adapterType == null) {
      return "";
    }
    // Remove hyphens and convert to lowercase
    return adapterType.replace("-", "").toLowerCase();
  }

  /**
   * Determines if the adapter type requires a use case.
   * Some adapter types (scheduled, messaging) don't need direct use case
   * invocation.
   */
  private boolean requiresUseCase(String adapterType) {
    // Use case is now optional for all adapter types
    // Controllers can inject multiple use cases or none initially
    return false;
  }

  /**
   * Determines if the adapter type requires endpoints.
   * Only HTTP-based adapters (REST, GraphQL) need endpoint definitions.
   */
  private boolean requiresEndpoints(String adapterType) {
    return adapterType.equalsIgnoreCase("rest");
  }

  /**
   * Validates task inputs.
   */
  private void validateInputs() {
    if (adapterName.isBlank()) {
      throw new IllegalArgumentException(
          "Adapter name is required. Use --name=User");
    }

    // Only require use case for certain adapter types
    if (requiresUseCase(type) && useCaseName.isBlank()) {
      throw new IllegalArgumentException(
          "Use case name is required for " + type + " adapters. Use --useCase=CreateUserUseCase");
    }

    // Only require endpoints for HTTP-based adapter types
    if (requiresEndpoints(type) && endpoints.isBlank()) {
      throw new IllegalArgumentException(
          "Endpoints are required for " + type
              + " adapters. Use --endpoints=/users:POST:create:User:userData:BODY:CreateUserRequest");
    }

    // packageName is now optional - it will be inferred if not provided
    if (packageName.isBlank()) {
      throw new IllegalArgumentException(
          "Package name could not be inferred. Please provide --packageName explicitly.");
    }
  }

  /**
   * Normalizes adapter type string to lowercase.
   */
  private String parseAdapterType(String typeStr) {
    // Simply normalize to lowercase - validation happens in generator
    return typeStr.toLowerCase().trim();
  }

  /**
   * Parses endpoint string into Endpoint list.
   * Format:
   * "/path:METHOD:useCaseMethod:ReturnType:param1:PARAMTYPE:Type1,param2:PARAMTYPE:Type2|/path2:..."
   * Example:
   * "/users:POST:create:User:userData:BODY:CreateUserRequest|/users/{id}:GET:findById:User:id:PATH:String"
   */
  private List<Endpoint> parseEndpoints(String endpointsStr) {
    List<Endpoint> result = new ArrayList<>();

    // Return empty list if endpoints string is null or blank
    if (endpointsStr == null || endpointsStr.isBlank()) {
      return result;
    }

    String[] endpointDefinitions = endpointsStr.split("\\|");
    for (String endpointDef : endpointDefinitions) {
      String[] parts = endpointDef.trim().split(":");
      if (parts.length < 4) {
        throw new IllegalArgumentException(
            "Invalid endpoint format: " + endpointDef
                + ". Expected format: /path:METHOD:useCaseMethod:ReturnType[:param:PARAMTYPE:Type]");
      }

      String path = parts[0].trim();
      HttpMethod method = HttpMethod.valueOf(parts[1].trim().toUpperCase());
      String useCaseMethod = parts[2].trim();
      String returnType = parts[3].trim();

      List<EndpointParameter> parameters = new ArrayList<>();

      // Parse parameters if present (groups of 3: name, paramType, type)
      for (int i = 4; i < parts.length; i += 3) {
        if (i + 2 < parts.length) {
          String paramName = parts[i].trim();
          ParameterType paramType = ParameterType.valueOf(parts[i + 1].trim().toUpperCase());
          String paramTypeStr = parts[i + 2].trim();
          parameters.add(new EndpointParameter(paramName, paramTypeStr, paramType));
        }
      }

      result.add(new Endpoint(path, method, useCaseMethod, returnType, parameters));
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
