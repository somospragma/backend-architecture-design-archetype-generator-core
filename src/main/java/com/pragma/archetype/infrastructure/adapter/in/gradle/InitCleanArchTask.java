package com.pragma.archetype.infrastructure.adapter.in.gradle;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

import com.pragma.archetype.application.generator.ProjectGenerator;
import com.pragma.archetype.application.usecase.InitializeProjectUseCaseImpl;
import com.pragma.archetype.application.usecase.InitializeProjectUseCaseImpl.InitializationResult;
import com.pragma.archetype.domain.model.config.ProjectConfig;
import com.pragma.archetype.domain.model.project.ArchitectureType;
import com.pragma.archetype.domain.model.project.Framework;
import com.pragma.archetype.domain.model.project.Paradigm;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import com.pragma.archetype.domain.port.out.TemplateRepository;
import com.pragma.archetype.domain.service.ProjectValidator;
import com.pragma.archetype.infrastructure.adapter.out.config.YamlConfigurationAdapter;
import com.pragma.archetype.infrastructure.adapter.out.filesystem.LocalFileSystemAdapter;

/**
 * Gradle task for initializing a clean architecture project.
 * This is an input adapter that drives the use case.
 */
public class InitCleanArchTask extends BaseCleanArchTask {

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

  @Option(option = "package", description = "Base package name (e.g., com.company.service)")
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

      // 2. Setup dependencies (adapters)
      FileSystemPort fileSystemPort = new LocalFileSystemAdapter();
      ConfigurationPort configurationPort = new YamlConfigurationAdapter();
      TemplateRepository templateRepository = createTemplateRepository();

      // 3. Create configuration (architectures come from the templates)
      ProjectConfig config = createProjectConfig(templateRepository);

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
          "Package name is required. Use --package=com.company.service");
    }

    // Validate architecture name format; availability is checked against the
    // templates in createProjectConfig
    try {
      ArchitectureType.fromValue(architecture);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          "Invalid architecture: " + architecture + ". Use a name like hexagonal-single");
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
  private ProjectConfig createProjectConfig(TemplateRepository templateRepository) {
    String projectName = getProject().getName();
    ArchitectureType archType = ArchitectureType.fromValue(architecture);

    List<ArchitectureType> available = templateRepository.listArchitectures();
    if (!available.contains(archType)) {
      throw new IllegalArgumentException(
          "Invalid architecture: " + architecture + ". Valid values: "
              + available.stream().map(ArchitectureType::getValue).collect(Collectors.joining(", ")));
    }

    // structure.yml decides whether adapters are generated as Gradle modules
    boolean adaptersAsModules = templateRepository.loadStructureMetadata(archType).adaptersAsModules();

    // Get plugin version from the base class method
    String pluginVersion = getPluginVersion();

    return ProjectConfig.builder()
        .name(projectName)
        .basePackage(packageName)
        .architecture(archType)
        .paradigm(Paradigm.valueOf(paradigm.toUpperCase()))
        .framework(Framework.valueOf(framework.toUpperCase()))
        .pluginVersion(pluginVersion)
        .adaptersAsModules(adaptersAsModules)
        .build();
  }

  /**
   * Creates template repository.
   * Delegates to TemplateRepositoryFactory for centralized resolution logic.
   */
  private TemplateRepository createTemplateRepository() {
    Path projectDir = getProject().getProjectDir().toPath();
    return TemplateRepositoryFactory.create(projectDir, getLogger());
  }
}
