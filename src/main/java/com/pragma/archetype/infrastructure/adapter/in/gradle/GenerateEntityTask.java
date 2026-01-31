package com.pragma.archetype.infrastructure.adapter.in.gradle;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

import com.pragma.archetype.application.generator.EntityGenerator;
import com.pragma.archetype.application.usecase.GenerateEntityUseCaseImpl;
import com.pragma.archetype.domain.model.EntityConfig;
import com.pragma.archetype.domain.model.ProjectConfig;
import com.pragma.archetype.domain.port.in.GenerateEntityUseCase;
import com.pragma.archetype.domain.port.in.GenerateEntityUseCase.GenerationResult;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import com.pragma.archetype.domain.port.out.TemplateRepository;
import com.pragma.archetype.domain.service.EntityValidator;
import com.pragma.archetype.infrastructure.adapter.out.config.YamlConfigurationAdapter;
import com.pragma.archetype.infrastructure.adapter.out.filesystem.LocalFileSystemAdapter;
import com.pragma.archetype.infrastructure.adapter.out.template.FreemarkerTemplateRepository;

/**
 * Gradle task for generating domain entities.
 * 
 * Usage:
 * ./gradlew generateEntity --name=User
 * --fields=name:String,email:String,age:Integer
 */
public class GenerateEntityTask extends DefaultTask {

  private String entityName = "";
  private String fields = "";
  private String packageName = "";
  private boolean hasId = true;
  private String idType = "String";

  @Option(option = "name", description = "Entity name (e.g., User, Product)")
  public void setEntityName(String entityName) {
    this.entityName = entityName;
  }

  @Input
  public String getEntityName() {
    return entityName;
  }

  @Option(option = "fields", description = "Entity fields (format: name:type,email:String,age:Integer)")
  public void setFields(String fields) {
    this.fields = fields;
  }

  @Input
  public String getFields() {
    return fields;
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

  @Option(option = "hasId", description = "Whether entity has an ID field (default: true)")
  public void setHasId(boolean hasId) {
    this.hasId = hasId;
  }

  @Input
  public boolean getHasId() {
    return hasId;
  }

  @Option(option = "idType", description = "ID field type: String, Long, UUID (default: String)")
  public void setIdType(String idType) {
    this.idType = idType;
  }

  @Input
  public String getIdType() {
    return idType;
  }

  @TaskAction
  public void generateEntity() {
    getLogger().lifecycle("Generating entity: {}", entityName);

    try {
      // 1. Validate inputs
      validateInputs();

      // 2. Resolve package name (auto-detect if not provided)
      String resolvedPackageName = resolvePackageName();

      // 3. Parse fields
      List<EntityConfig.EntityField> entityFields = parseFields(fields);

      // 4. Filter out 'id' field if hasId is true (to avoid duplication)
      if (hasId) {
        entityFields = entityFields.stream()
            .filter(field -> !field.name().equalsIgnoreCase("id"))
            .toList();
      }

      // 5. Create configuration
      EntityConfig config = EntityConfig.builder()
          .name(entityName)
          .fields(entityFields)
          .hasId(hasId)
          .idType(idType)
          .packageName(resolvedPackageName)
          .build();

      // 5. Setup dependencies
      FileSystemPort fileSystemPort = new LocalFileSystemAdapter();
      ConfigurationPort configurationPort = new YamlConfigurationAdapter();
      TemplateRepository templateRepository = createTemplateRepository();

      // 6. Setup use case
      EntityValidator validator = new EntityValidator(fileSystemPort, configurationPort);
      EntityGenerator generator = new EntityGenerator(templateRepository, fileSystemPort);
      GenerateEntityUseCase useCase = new GenerateEntityUseCaseImpl(
          validator,
          generator,
          configurationPort,
          fileSystemPort);

      // 6. Execute use case
      Path projectPath = getProject().getProjectDir().toPath();
      GenerationResult result = useCase.execute(projectPath, config);

      // 7. Handle result
      if (result.success()) {
        getLogger().lifecycle("✓ Entity generated successfully!");
        getLogger().lifecycle("  Generated {} file(s)", result.generatedFiles().size());
        result.generatedFiles().forEach(file -> getLogger().lifecycle("    - {}", file.path()));
      } else {
        getLogger().error("✗ Failed to generate entity:");
        result.errors().forEach(error -> getLogger().error("  - {}", error));
        throw new RuntimeException("Entity generation failed");
      }

    } catch (Exception e) {
      getLogger().error("✗ Error generating entity: {}", e.getMessage());
      throw new RuntimeException("Entity generation failed", e);
    }
  }

  /**
   * Validates task inputs.
   */
  private void validateInputs() {
    if (entityName.isBlank()) {
      throw new IllegalArgumentException(
          "Entity name is required. Use --name=User");
    }

    if (fields.isBlank()) {
      throw new IllegalArgumentException(
          "Entity fields are required. Use --fields=name:String,email:String");
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

      // For hexagonal-single, entities go in domain.model
      return projectConfig.basePackage() + ".domain.model";
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "Could not auto-detect package name. Please provide --packageName or ensure .cleanarch.yml exists", e);
    }
  }

  /**
   * Parses field string into EntityField list.
   * Format: "name:String,email:String,age:Integer"
   */
  private List<EntityConfig.EntityField> parseFields(String fieldsStr) {
    List<EntityConfig.EntityField> result = new ArrayList<>();

    String[] fieldPairs = fieldsStr.split(",");
    for (String pair : fieldPairs) {
      String[] parts = pair.trim().split(":");
      if (parts.length != 2) {
        throw new IllegalArgumentException(
            "Invalid field format: " + pair + ". Expected format: name:type");
      }

      String fieldName = parts[0].trim();
      String fieldType = parts[1].trim();
      boolean nullable = false;

      // Check for nullable marker (e.g., "name:String?")
      if (fieldType.endsWith("?")) {
        nullable = true;
        fieldType = fieldType.substring(0, fieldType.length() - 1);
      }

      result.add(new EntityConfig.EntityField(fieldName, fieldType, nullable));
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
