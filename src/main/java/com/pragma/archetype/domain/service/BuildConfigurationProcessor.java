package com.pragma.archetype.domain.service;

import java.util.HashMap;
import java.util.Map;

import com.pragma.archetype.domain.model.adapter.InputAdapterConfig;
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
   * @param config             The adapter configuration
   * @return Processed configuration with variables replaced
   */
  public String process(
      String buildConfiguration,
      StructureMetadata structureMetadata,
      InputAdapterConfig config) {

    if (buildConfiguration == null || buildConfiguration.isBlank()) {
      return "";
    }

    // Prepare variables for the template
    Map<String, Object> variables = new HashMap<>();
    variables.put("resourcesPath", structureMetadata.getResourcesPath());
    variables.put("adapterName", config.name());
    variables.put("packageName", config.packageName());
    variables.put("adapterType", config.type());

    // Process as inline template
    return templateRepository.processInlineTemplate(buildConfiguration, variables);
  }
}
