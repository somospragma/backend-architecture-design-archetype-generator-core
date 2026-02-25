package com.pragma.archetype.infrastructure.adapter.in.gradle;

import java.nio.file.Files;
import java.nio.file.Path;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

import com.pragma.archetype.application.generator.ProjectGenerator;
import com.pragma.archetype.application.usecase.InitializeProjectUseCaseImpl;
import com.pragma.archetype.application.usecase.InitializeProjectUseCaseImpl.InitializationResult;
import com.pragma.archetype.domain.model.ArchitectureType;
import com.pragma.archetype.domain.model.Framework;
import com.pragma.archetype.domain.model.Paradigm;
import com.pragma.archetype.domain.model.config.ProjectConfig;
import com.pragma.archetype.domain.model.config.TemplateConfig;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import com.pragma.archetype.domain.port.out.TemplateRepository;
import com.pragma.archetype.domain.service.ProjectValidator;
import com.pragma.archetype.infrastructure.adapter.out.config.YamlConfigurationAdapter;
import com.pragma.archetype.infrastructure.adapter.out.filesystem.LocalFileSystemAdapter;
import com.pragma.archetype.infrastructure.adapter.out.template.FreemarkerTemplateRepository;

/**
 * Gradle task for initializing a clean architecture project.
 * This is an input adapter that drives the use case.
 */
public class InitCleanArchTask extends DefaultTask {

  private String architecture = "hexagonal-single";
  private String paradigm = "reactive";
  private String framework = "spring";
  private String packageName;

  @Option(option = "architecture", description = "Architecture type (hexagonal-single, hexagonal-multi, onion-single, etc.)")
  public void setArchitecture(String architecture) {
    this.architecture = architecture;
  }

  @Input
  public String getArchitecture() {
    return architecture;
  }

  @Option(option = "paradigm", description = "Programming paradigm (reactive, imperative)")
  public void setParadigm(String paradigm) {
    this.paradigm = paradigm;
  }

  @Input
  public String getParadigm() {
    return paradigm;
  }

  @Option(option = "framework", description = "Framework (spring, quarkus)")
  public void setFramework(String framework) {
    this.framework = framework;
  }

  @Input
  public String getFramework() {
    return framework;
  }

  @Option(option = "packageName", description = "Base package name (e.g., com.company.service)")
  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  @Input
  public String getPackageName() {
    return packageName;
  }

  @TaskAction
  public void initializeProject() {
    getLogger().lifecycle("Initializing clean architecture project...");

    try {
      // 1. Validate inputs
      validateInputs();

      // 2. Create configuration
      ProjectConfig config = createProjectConfig();

      // 3. Setup dependencies (adapters)
      FileSystemPort fileSystemPort = new LocalFileSystemAdapter();
      ConfigurationPort configurationPort = new YamlConfigurationAdapter();
      TemplateRepository templateRepository = createTemplateRepository();

      // 4. Setup use case
      ProjectValidator validator = new ProjectValidator(fileSystemPort, configurationPort);
      ProjectGenerator generator = new ProjectGenerator(templateRepository, fileSystemPort);
      InitializeProjectUseCaseImpl useCase = new InitializeProjectUseCaseImpl(
          validator,
          generator,
          configurationPort);

      // 5. Execute use case
      Path projectPath = getProject().getProjectDir().toPath();
      InitializationResult result = useCase.execute(projectPath, config);

      // 6. Handle result
      if (result.isSuccess()) {
        getLogger().lifecycle("✓ Project initialized successfully!");
        getLogger().lifecycle("  Generated {} files", result.generatedFiles().size());
        getLogger().lifecycle("  Architecture: {}", architecture);
        getLogger().lifecycle("  Paradigm: {}", paradigm);
        getLogger().lifecycle("  Framework: {}", framework);
        getLogger().lifecycle("");
        getLogger().lifecycle("Next steps:");
        getLogger().lifecycle("  1. Review the generated structure");
        getLogger().lifecycle("  2. Run: ./gradlew build");
        getLogger().lifecycle("  3. Start coding your business logic!");
      } else {
        getLogger().error("✗ Failed to initialize project:");
        result.errors().forEach(error -> getLogger().error("  - {}", error));
        throw new RuntimeException("Project initialization failed");
      }

    } catch (Exception e) {
      getLogger().error("✗ Error initializing project: {}", e.getMessage());
      throw new RuntimeException("Project initialization failed", e);
    }
  }

  /**
   * Validates task inputs.
   */
  private void validateInputs() {
    if (packageName == null || packageName.isBlank()) {
      throw new IllegalArgumentException(
          "Package name is required. Use --packageName=com.company.service");
    }

    // Validate architecture
    try {
      ArchitectureType.valueOf(architecture.toUpperCase().replace('-', '_'));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          "Invalid architecture: " + architecture + ". " +
              "Valid values: hexagonal-single, hexagonal-multi, hexagonal-multi-granular, onion-single, onion-multi");
    }

    // Validate paradigm
    try {
      Paradigm.valueOf(paradigm.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          "Invalid paradigm: " + paradigm + ". Valid values: reactive, imperative");
    }

    // Validate framework
    try {
      Framework.valueOf(framework.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          "Invalid framework: " + framework + ". Valid values: spring, quarkus");
    }
  }

  /**
   * Creates project configuration from task inputs.
   */
  private ProjectConfig createProjectConfig() {
    String projectName = getProject().getName();
    ArchitectureType archType = ArchitectureType.valueOf(architecture.toUpperCase().replace('-', '_'));

    // Automatically enable adaptersAsModules for hexagonal-multi-granular
    boolean adaptersAsModules = (archType == ArchitectureType.HEXAGONAL_MULTI_GRANULAR);

    return ProjectConfig.builder()
        .name(projectName)
        .basePackage(packageName)
        .architecture(archType)
        .paradigm(Paradigm.valueOf(paradigm.toUpperCase()))
        .framework(Framework.valueOf(framework.toUpperCase()))
        .pluginVersion("0.1.15-SNAPSHOT")
        .adaptersAsModules(adaptersAsModules)
        .build();
  }

  /**
   * Creates template repository.
   * Reads configuration from .cleanarch.yml or uses environment variables.
   * Falls back to local development path if available.
   */
  private TemplateRepository createTemplateRepository() {
    Path projectDir = getProject().getProjectDir().toPath();
    YamlConfigurationAdapter configAdapter = new YamlConfigurationAdapter();

    // 1. Check for .cleanarch.yml configuration (highest priority)
    if (configAdapter.configurationExists(projectDir)) {
      TemplateConfig templateConfig = configAdapter.readTemplateConfiguration(projectDir);

      if (templateConfig.isLocalMode()) {
        Path localPath = Path.of(templateConfig.localPath());
        if (Files.exists(localPath)) {
          getLogger().lifecycle("✓ Using local templates from .cleanarch.yml: {}", localPath.toAbsolutePath());
          return new FreemarkerTemplateRepository(localPath);
        } else {
          getLogger().error("✗ Template localPath in .cleanarch.yml does not exist: {}", localPath);
          throw new RuntimeException("Template path not found: " + localPath);
        }
      }
    }

    // 2. Try to auto-detect local templates (for plugin development only)
    Path autoDetectPath = projectDir
        .resolve("../../backend-architecture-design-archetype-generator-templates/templates")
        .normalize();

    if (Files.exists(autoDetectPath)) {
      getLogger().lifecycle("✓ Auto-detected local templates (development mode): {}", autoDetectPath.toAbsolutePath());
      getLogger()
          .lifecycle("  Tip: Configure templates.localPath in .cleanarch.yml for explicit control");
      return new FreemarkerTemplateRepository(autoDetectPath);
    }

    // 3. Fall back to embedded templates (in JAR) - production mode
    getLogger().lifecycle("✓ Using embedded templates from plugin JAR (production mode)");
    return new FreemarkerTemplateRepository("embedded");
  }
}
