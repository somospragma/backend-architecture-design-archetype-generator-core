package com.pragma.archetype.application.usecase;

import java.nio.file.Path;

import com.pragma.archetype.application.generator.InputAdapterConfigAdapter;
import com.pragma.archetype.application.generator.UnifiedAdapterGenerator;
import com.pragma.archetype.application.port.out.AdapterDiscoveryPort;
import com.pragma.archetype.domain.model.adapter.AdapterDirection;
import com.pragma.archetype.domain.model.adapter.InputAdapterConfig;
import com.pragma.archetype.domain.port.in.GenerateInputAdapterUseCase;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import com.pragma.archetype.domain.port.out.TemplateRepository;
import com.pragma.archetype.infrastructure.adapter.out.config.YamlConfigurationAdapter;

/**
 * Implementation of the GenerateInputAdapterUseCase.
 * 
 * <p>
 * This class extends {@link AbstractGenerateAdapterUseCase} and only needs to
 * specify that it generates INPUT adapters. All the generation logic is
 * inherited from the parent class.
 * 
 * <p>
 * The complete generation flow (16 steps) is handled by the parent class:
 * validation, backup, generation, configuration, and cleanup.
 * 
 * @see AbstractGenerateAdapterUseCase
 * @see AdapterDirection#INPUT
 */
public class GenerateInputAdapterUseCaseImpl extends AbstractGenerateAdapterUseCase
    implements GenerateInputAdapterUseCase {

  public GenerateInputAdapterUseCaseImpl(
      UnifiedAdapterGenerator generator,
      ConfigurationPort configurationPort,
      FileSystemPort fileSystemPort,
      AdapterDiscoveryPort adapterDiscovery,
      TemplateRepository templateRepository,
      YamlConfigurationAdapter yamlConfigurationAdapter) {
    super(generator, configurationPort, fileSystemPort, adapterDiscovery, templateRepository,
        yamlConfigurationAdapter);
  }

  @Override
  public GenerationResult execute(Path projectPath, InputAdapterConfig config) {
    // Wrap InputAdapterConfig in the unified adapter config
    var unifiedConfig = new InputAdapterConfigAdapter(config);

    // Call parent's execute and convert result
    var parentResult = super.execute(projectPath, unifiedConfig);

    // Convert from parent's GenerationResult to this interface's GenerationResult
    if (parentResult.success()) {
      return GenerationResult.success(parentResult.generatedFiles());
    } else {
      return GenerationResult.failure(parentResult.errors());
    }
  }

  @Override
  protected AdapterDirection getDirection() {
    return AdapterDirection.INPUT;
  }
}
