package com.pragma.archetype.infrastructure.adapter.in.gradle;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

import com.pragma.archetype.application.usecase.ValidateTemplateUseCaseImpl;
import com.pragma.archetype.domain.model.ValidationResult;
import com.pragma.archetype.domain.port.in.ValidateTemplateUseCase;
import com.pragma.archetype.domain.port.out.TemplateRepository;
import com.pragma.archetype.domain.service.TemplateValidator;
import com.pragma.archetype.infrastructure.adapter.out.template.FreemarkerTemplateRepository;

/**
 * Gradle task for validating templates.
 * Validates architecture templates, adapter templates, or all templates.
 * 
 * Usage:
 * ./gradlew validateTemplates # Validate all templates
 * ./gradlew validateTemplates --architecture=hexagonal-single # Validate
 * specific architecture
 * ./gradlew validateTemplates --adapter=mongodb # Validate specific adapter
 */
public class ValidateTemplatesTask extends DefaultTask {

  private String architecture = "";
  private String adapter = "";

  @Option(option = "architecture", description = "Architecture to validate (e.g., hexagonal-single, onion-single)")
  public void setArchitecture(String architecture) {
    this.architecture = architecture;
  }

  @Input
  @Optional
  public String getArchitecture() {
    return architecture;
  }

  @Option(option = "adapter", description = "Adapter to validate (e.g., mongodb, rest-controller)")
  public void setAdapter(String adapter) {
    this.adapter = adapter;
  }

  @Input
  @Optional
  public String getAdapter() {
    return adapter;
  }

  @TaskAction
  public void validateTemplates() {
    Path projectPath = Paths.get(getProject().getProjectDir().getAbsolutePath());

    try {
      // Setup dependencies
      TemplateRepository templateRepository = createTemplateRepository();
      TemplateValidator templateValidator = new TemplateValidator(templateRepository);
      ValidateTemplateUseCase useCase = new ValidateTemplateUseCaseImpl(templateValidator);

      // Execute validation based on parameters
      ValidationResult result;

      if (architecture != null && !architecture.isBlank()) {
        // Validate specific architecture
        getLogger().lifecycle("Validating architecture templates: {}", architecture);
        result = useCase.validateArchitecture(architecture);
      } else if (adapter != null && !adapter.isBlank()) {
        // Validate specific adapter
        getLogger().lifecycle("Validating adapter templates: {}", adapter);
        result = useCase.validateAdapter(adapter);
      } else {
        // Validate all templates
        getLogger().lifecycle("Validating all templates...");
        result = useCase.validateAll(projectPath);
      }

      // Display results
      if (result.valid()) {
        getLogger().lifecycle("✓ Template validation successful!");

        if (result.hasWarnings()) {
          getLogger().lifecycle("");
          getLogger().lifecycle("Warnings:");
          for (String warning : result.warnings()) {
            getLogger().lifecycle("  ⚠ {}", warning);
          }
        }

        if (architecture == null || architecture.isBlank()) {
          if (adapter == null || adapter.isBlank()) {
            getLogger().lifecycle("");
            getLogger().lifecycle("All templates are valid and ready to use.");
          }
        }
      } else {
        getLogger().lifecycle("");
        getLogger().lifecycle("✗ Template validation failed!");
        getLogger().lifecycle("");
        getLogger().lifecycle("Errors:");
        for (String error : result.errors()) {
          getLogger().lifecycle("  ✗ {}", error);
        }

        throw new org.gradle.api.GradleException("Template validation failed. See errors above.");
      }

    } catch (org.gradle.api.GradleException e) {
      throw e;
    } catch (Exception e) {
      getLogger().error("Failed to validate templates: {}", e.getMessage(), e);
      throw new org.gradle.api.GradleException("Template validation failed: " + e.getMessage(), e);
    }
  }

  /**
   * Creates a template repository instance.
   * Uses the same logic as other tasks to determine template source.
   */
  private TemplateRepository createTemplateRepository() {
    Path projectPath = Paths.get(getProject().getProjectDir().getAbsolutePath());

    // Try to read template configuration from .cleanarch.yml
    try {
      var configAdapter = new com.pragma.archetype.infrastructure.adapter.out.config.YamlConfigurationAdapter();
      var configOpt = configAdapter.readConfiguration(projectPath);

      if (configOpt.isPresent()) {
        var templateConfig = configAdapter.readTemplateConfiguration(projectPath);
        if (templateConfig != null) {
          return new FreemarkerTemplateRepository(templateConfig);
        }
      }
    } catch (Exception e) {
      getLogger().debug("Could not read template configuration: {}", e.getMessage());
    }

    // Fallback to default remote repository
    String defaultRepo = "https://github.com/somospragma/backend-architecture-design-archetype-generator-templates";
    getLogger().debug("Using default template repository: {}", defaultRepo);
    return new FreemarkerTemplateRepository(defaultRepo);
  }
}
