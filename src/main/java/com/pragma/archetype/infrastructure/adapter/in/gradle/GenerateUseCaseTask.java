package com.pragma.archetype.infrastructure.adapter.in.gradle;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

import com.pragma.archetype.application.generator.UseCaseGenerator;
import com.pragma.archetype.application.usecase.GenerateUseCaseUseCaseImpl;
import com.pragma.archetype.domain.model.ProjectConfig;
import com.pragma.archetype.domain.model.UseCaseConfig;
import com.pragma.archetype.domain.port.in.GenerateUseCaseUseCase;
import com.pragma.archetype.domain.port.in.GenerateUseCaseUseCase.GenerationResult;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import com.pragma.archetype.domain.port.out.TemplateRepository;
import com.pragma.archetype.domain.service.UseCaseValidator;
import com.pragma.archetype.infrastructure.adapter.out.config.YamlConfigurationAdapter;
import com.pragma.archetype.infrastructure.adapter.out.filesystem.LocalFileSystemAdapter;
import com.pragma.archetype.infrastructure.adapter.out.template.FreemarkerTemplateRepository;

/**
 * Gradle task for generating use cases.
 * 
 * Usage:
 * ./gradlew generateUseCase --name=CreateUser
 * --methods=execute:User:userId:String,userData:UserData
 * --packageName=com.pragma.domain.port.in
 */
public class GenerateUseCaseTask extends DefaultTask {

  private String useCaseName = "";
  private String methods = "";
  private String packageName = "";
  private boolean generatePort = true;
  private boolean generateImpl = true;

  @Option(option = "name", description = "Use case name (e.g., CreateUser, GetProduct)")
  public void setUseCaseName(String useCaseName) {
    this.useCaseName = useCaseName;
  }

  @Input
  public String getUseCaseName() {
    return useCaseName;
  }

  @Option(option = "methods", description = "Methods (format: methodName:ReturnType:param1:Type1,param2:Type2)")
  public void setMethods(String methods) {
    this.methods = methods;
  }

  @Input
  public String getMethods() {
    return methods;
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

  @Option(option = "generatePort", description = "Generate port interface (default: true)")
  public void setGeneratePort(boolean generatePort) {
    this.generatePort = generatePort;
  }

  @Input
  public boolean getGeneratePort() {
    return generatePort;
  }

  @Option(option = "generateImpl", description = "Generate implementation (default: true)")
  public void setGenerateImpl(boolean generateImpl) {
    this.generateImpl = generateImpl;
  }

  @Input
  public boolean getGenerateImpl() {
    return generateImpl;
  }

  @TaskAction
  public void generateUseCase() {
    getLogger().lifecycle("Generating use case: {}", useCaseName);

    try {
      // 1. Validate inputs
      validateInputs();

      // 2. Resolve package name (auto-detect if not provided)
      String resolvedPackageName = resolvePackageName();

      // 3. Parse methods
      List<UseCaseConfig.UseCaseMethod> useCaseMethods = parseMethods(methods);

      // 4. Create configuration
      UseCaseConfig config = UseCaseConfig.builder()
          .name(useCaseName)
          .packageName(resolvedPackageName)
          .methods(useCaseMethods)
          .generatePort(generatePort)
          .generateImpl(generateImpl)
          .build();

      // 4. Setup dependencies
      FileSystemPort fileSystemPort = new LocalFileSystemAdapter();
      ConfigurationPort configurationPort = new YamlConfigurationAdapter();
      TemplateRepository templateRepository = createTemplateRepository();

      // 5. Setup use case
      UseCaseValidator validator = new UseCaseValidator(fileSystemPort, configurationPort);
      UseCaseGenerator generator = new UseCaseGenerator(templateRepository, fileSystemPort);
      GenerateUseCaseUseCase useCase = new GenerateUseCaseUseCaseImpl(
          validator,
          generator,
          configurationPort,
          fileSystemPort);

      // 6. Execute use case
      Path projectPath = getProject().getProjectDir().toPath();
      GenerationResult result = useCase.execute(projectPath, config);

      // 7. Handle result
      if (result.success()) {
        getLogger().lifecycle("✓ Use case generated successfully!");
        getLogger().lifecycle("  Generated {} file(s)", result.generatedFiles().size());
        result.generatedFiles().forEach(file -> getLogger().lifecycle("    - {}", file.path()));
      } else {
        getLogger().error("✗ Failed to generate use case:");
        result.errors().forEach(error -> getLogger().error("  - {}", error));
        throw new RuntimeException("Use case generation failed");
      }

    } catch (Exception e) {
      getLogger().error("✗ Error generating use case: {}", e.getMessage());
      throw new RuntimeException("Use case generation failed", e);
    }
  }

  /**
   * Validates task inputs.
   */
  private void validateInputs() {
    if (useCaseName.isBlank()) {
      throw new IllegalArgumentException(
          "Use case name is required. Use --name=CreateUser");
    }

    if (methods.isBlank()) {
      throw new IllegalArgumentException(
          "Methods are required. Use --methods=execute:User:userId:String");
    }
  }

  /**
   * Resolves package name from .cleanarch.yml if not provided.
   */
  private String resolvePackageName() {
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

      // For hexagonal-single, use cases go in domain.port.in
      return projectConfig.basePackage() + ".domain.port.in";
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "Could not auto-detect package name. Please provide --packageName or ensure .cleanarch.yml exists", e);
    }
  }

  /**
   * Parses method string into UseCaseMethod list.
   * Format: "methodName:ReturnType:param1:Type1,param2:Type2|method2:ReturnType2"
   * Example: "execute:User:userId:String,userData:UserData"
   */
  private List<UseCaseConfig.UseCaseMethod> parseMethods(String methodsStr) {
    List<UseCaseConfig.UseCaseMethod> result = new ArrayList<>();

    String[] methodDefinitions = methodsStr.split("\\|");
    for (String methodDef : methodDefinitions) {
      String[] parts = methodDef.trim().split(":");
      if (parts.length < 2) {
        throw new IllegalArgumentException(
            "Invalid method format: " + methodDef
                + ". Expected format: methodName:ReturnType[:param1:Type1,param2:Type2]");
      }

      String methodName = parts[0].trim();
      String returnType = parts[1].trim();
      List<UseCaseConfig.MethodParameter> parameters = new ArrayList<>();

      // Parse parameters if present
      if (parts.length > 2) {
        for (int i = 2; i < parts.length; i += 2) {
          if (i + 1 < parts.length) {
            String paramName = parts[i].trim();
            String paramType = parts[i + 1].trim();
            parameters.add(new UseCaseConfig.MethodParameter(paramName, paramType));
          }
        }
      }

      result.add(new UseCaseConfig.UseCaseMethod(methodName, returnType, parameters));
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
