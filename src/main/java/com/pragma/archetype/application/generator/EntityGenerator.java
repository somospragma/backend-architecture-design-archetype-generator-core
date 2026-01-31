package com.pragma.archetype.application.generator;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pragma.archetype.domain.model.EntityConfig;
import com.pragma.archetype.domain.model.GeneratedFile;
import com.pragma.archetype.domain.model.ProjectConfig;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import com.pragma.archetype.domain.port.out.TemplateRepository;

/**
 * Generator for domain entities.
 */
public class EntityGenerator {

  private final TemplateRepository templateRepository;
  private final FileSystemPort fileSystemPort;

  public EntityGenerator(TemplateRepository templateRepository, FileSystemPort fileSystemPort) {
    this.templateRepository = templateRepository;
    this.fileSystemPort = fileSystemPort;
  }

  /**
   * Generates a domain entity.
   *
   * @param projectPath   Path to the project root
   * @param projectConfig Project configuration
   * @param entityConfig  Entity configuration
   * @return List of generated files
   */
  public List<GeneratedFile> generateEntity(
      Path projectPath,
      ProjectConfig projectConfig,
      EntityConfig entityConfig) {

    List<GeneratedFile> files = new ArrayList<>();

    // Prepare template context
    Map<String, Object> context = prepareTemplateContext(projectConfig, entityConfig);

    // Determine entity path based on architecture
    Path entityPath = determineEntityPath(projectPath, projectConfig, entityConfig);

    // Generate entity file
    String entityContent = templateRepository.processTemplate(
        "components/entity/Entity.java.ftl",
        context);

    fileSystemPort.createDirectory(entityPath.getParent());

    files.add(GeneratedFile.javaSource(
        entityPath,
        entityContent));

    return files;
  }

  /**
   * Prepares template context with entity data.
   */
  private Map<String, Object> prepareTemplateContext(
      ProjectConfig projectConfig,
      EntityConfig entityConfig) {

    Map<String, Object> context = new HashMap<>();

    // Project info
    context.put("basePackage", projectConfig.basePackage());
    context.put("packageName", entityConfig.packageName());

    // Entity info
    context.put("entityName", entityConfig.name());
    context.put("hasId", entityConfig.hasId());
    context.put("idType", entityConfig.idType());
    context.put("fields", entityConfig.fields());

    // Helper flags
    context.put("needsUUID", entityConfig.idType().equals("UUID"));
    context.put("needsLocalDateTime", hasLocalDateTimeField(entityConfig.fields()));

    return context;
  }

  /**
   * Determines the path where the entity should be generated.
   */
  private Path determineEntityPath(
      Path projectPath,
      ProjectConfig projectConfig,
      EntityConfig entityConfig) {

    String packagePath = entityConfig.packageName().replace('.', '/');

    // For single module architectures
    if (!projectConfig.architecture().isMultiModule()) {
      return projectPath
          .resolve("src/main/java")
          .resolve(packagePath)
          .resolve(entityConfig.name() + ".java");
    }

    // For multi-module architectures, entity goes in domain module
    return projectPath
        .resolve("domain")
        .resolve("src/main/java")
        .resolve(packagePath)
        .resolve(entityConfig.name() + ".java");
  }

  /**
   * Checks if any field is LocalDateTime type.
   */
  private boolean hasLocalDateTimeField(List<EntityConfig.EntityField> fields) {
    return fields.stream()
        .anyMatch(f -> f.type().equals("LocalDateTime"));
  }
}
