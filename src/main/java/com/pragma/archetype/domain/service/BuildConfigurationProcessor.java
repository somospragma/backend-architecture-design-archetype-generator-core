package com.pragma.archetype.domain.service;

import java.util.HashMap;
import java.util.Map;

import com.pragma.archetype.domain.model.structure.StructureMetadata;
import com.pragma.archetype.domain.port.out.TemplateRepository;

/**
 * Processes build configuration templates with FreeMarker variables.
 * Used to replace variables like ${resourcesPath} in adapter build
 * configuration.
 */
public class BuildConfigurationProcessor {

  private final TemplateRepository templateRepository;

  public BuildConfigurationProcessor(TemplateRepository templateRepository) {
    this.templateRepository = templateRepository;
  }

  /**
   * Processes buildConfiguration as FreeMarker template with variables.
   * 
   * @param buildConfiguration The build configuration template string
   * @param structureMetadata  The structure metadata (for resourcesPath, etc.)
   * @param adapterName        Name of the adapter
   * @param packageName        Package name of the adapter
   * @param adapterType        Type of the adapter
   * @return Processed configuration with variables replaced
   */
  public String process(
      String buildConfiguration,
      StructureMetadata structureMetadata,
      String adapterName,
      String packageName,
      String adapterType) {

    if (buildConfiguration == null || buildConfiguration.isBlank()) {
      return "";
    }

    // Prepare variables for the template
    Map<String, Object> variables = new HashMap<>();
    variables.put("resourcesPath", structureMetadata.getResourcesPath());
    variables.put("adapterName", adapterName);
    variables.put("packageName", packageName);
    variables.put("adapterType", adapterType);

    // Process as inline template
    return templateRepository.processInlineTemplate(buildConfiguration, variables);
  }
}
