package com.pragma.archetype.infrastructure.adapter.in.gradle;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

import com.pragma.archetype.application.generator.InputAdapterGenerator;
import com.pragma.archetype.application.usecase.GenerateInputAdapterUseCaseImpl;
import com.pragma.archetype.domain.model.InputAdapterConfig;
import com.pragma.archetype.domain.model.InputAdapterConfig.Endpoint;
import com.pragma.archetype.domain.model.InputAdapterConfig.EndpointParameter;
import com.pragma.archetype.domain.model.InputAdapterConfig.HttpMethod;
import com.pragma.archetype.domain.model.InputAdapterConfig.InputAdapterType;
import com.pragma.archetype.domain.model.InputAdapterConfig.ParameterType;
import com.pragma.archetype.domain.model.ValidationResult;
import com.pragma.archetype.domain.port.in.GenerateInputAdapterUseCase;
import com.pragma.archetype.domain.port.in.GenerateInputAdapterUseCase.GenerationResult;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import com.pragma.archetype.domain.port.out.TemplateRepository;
import com.pragma.archetype.domain.service.ConfigurationValidator;
import com.pragma.archetype.domain.service.InputAdapterValidator;
import com.pragma.archetype.infrastructure.adapter.out.config.YamlConfigurationAdapter;
import com.pragma.archetype.infrastructure.adapter.out.filesystem.LocalFileSystemAdapter;
import com.pragma.archetype.infrastructure.adapter.out.template.FreemarkerTemplateRepository;

/**
 * Gradle task for generating input adapters (entry points: REST controllers,
 * GraphQL
 * resolvers, etc.).
 * 
 * Usage:
 * ./gradlew generateInputAdapter --name=User --useCase=CreateUserUseCase
 * --endpoints=/users:POST:create:User:userData:BODY:CreateUserRequest
 * --packageName=com.pragma.infrastructure.entry-points.rest
 * --type=rest
 */
public class GenerateInputAdapterTask extends DefaultTask {

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

  @Option(option = "useCase", description = "Use case name (e.g., CreateUserUseCase)")
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

      // 3. Parse adapter type
      InputAdapterType adapterType = parseAdapterType(type);

      // 4. Parse endpoints
      List<Endpoint> endpointList = parseEndpoints(endpoints);

      // 5. Create configuration
      InputAdapterConfig config = InputAdapterConfig.builder()
          .name(adapterName)
          .useCaseName(useCaseName)
          .type(adapterType)
          .packageName(packageName)
          .endpoints(endpointList)
          .build();

      // 6. Setup dependencies (reuse instances from validation)
      TemplateRepository templateRepository = createTemplateRepository();

      // 7. Setup use case
      com.pragma.archetype.domain.service.PackageValidator packageValidator = new com.pragma.archetype.domain.service.PackageValidator();
      InputAdapterValidator validator = new InputAdapterValidator(fileSystemPort, configurationPort, packageValidator);
      InputAdapterGenerator generator = new InputAdapterGenerator(templateRepository, fileSystemPort);
      GenerateInputAdapterUseCase useCase = new GenerateInputAdapterUseCaseImpl(
          validator,
          generator,
          configurationPort,
          fileSystemPort);

      // 8. Execute use case
      GenerationResult result = useCase.execute(projectPath, config);

      // 9. Handle result
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
   * Validates task inputs.
   */
  private void validateInputs() {
    if (adapterName.isBlank()) {
      throw new IllegalArgumentException(
          "Adapter name is required. Use --name=User");
    }

    if (useCaseName.isBlank()) {
      throw new IllegalArgumentException(
          "Use case name is required. Use --useCase=CreateUserUseCase");
    }

    if (endpoints.isBlank()) {
      throw new IllegalArgumentException(
          "Endpoints are required. Use --endpoints=/users:POST:create:User:userData:BODY:CreateUserRequest");
    }

    if (packageName.isBlank()) {
      throw new IllegalArgumentException(
          "Package name is required. Use --packageName=com.company.infrastructure.entry-points.rest");
    }
  }

  /**
   * Parses adapter type string into InputAdapterType enum.
   */
  private InputAdapterType parseAdapterType(String typeStr) {
    return switch (typeStr.toLowerCase()) {
      case "rest" -> InputAdapterType.REST;
      case "graphql" -> InputAdapterType.GRAPHQL;
      case "grpc" -> InputAdapterType.GRPC;
      case "websocket" -> InputAdapterType.WEBSOCKET;
      default -> throw new IllegalArgumentException(
          "Invalid adapter type: " + typeStr + ". Valid types: rest, graphql, grpc, websocket");
    };
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
