package com.pragma.archetype.application.usecase;

import java.nio.file.Path;

import com.pragma.archetype.application.generator.OutputAdapterConfigAdapter;
import com.pragma.archetype.application.generator.UnifiedAdapterGenerator;
import com.pragma.archetype.application.port.out.AdapterDiscoveryPort;
import com.pragma.archetype.domain.model.adapter.AdapterDirection;
import com.pragma.archetype.domain.model.adapter.OutputAdapterConfig;
import com.pragma.archetype.domain.port.in.GenerateAdapterUseCase;
import com.pragma.archetype.domain.port.out.BackupPort;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import com.pragma.archetype.domain.port.out.TemplateRepository;
import com.pragma.archetype.infrastructure.adapter.out.config.YamlConfigurationAdapter;

/**
 * Implementation of the GenerateAdapterUseCase (for OUTPUT adapters).
 * 
 * <p>
 * This class extends {@link AbstractGenerateAdapterUseCase} and only needs to
 * specify that it generates OUTPUT adapters. All the generation logic is
 * inherited from the parent class.
 * 
 * <p>
 * The complete generation flow (16 steps) is handled by the parent class:
 * validation, backup, generation, configuration, and cleanup.
 * 
 * @see AbstractGenerateAdapterUseCase
 * @see AdapterDirection#OUTPUT
 */
public class GenerateAdapterUseCaseImpl extends AbstractGenerateAdapterUseCase
    implements GenerateAdapterUseCase {

  public GenerateAdapterUseCaseImpl(
      UnifiedAdapterGenerator generator,
      ConfigurationPort configurationPort,
      FileSystemPort fileSystemPort,
      AdapterDiscoveryPort adapterDiscovery,
      TemplateRepository templateRepository,
      YamlConfigurationAdapter yamlConfigurationAdapter,
      BackupPort backupPort) {
    super(generator, configurationPort, fileSystemPort, adapterDiscovery, templateRepository,
        yamlConfigurationAdapter, backupPort);
  }

  @Override
  public GenerationResult execute(Path projectPath, OutputAdapterConfig config) {
    // Wrap OutputAdapterConfig in the unified adapter config
    var unifiedConfig = new OutputAdapterConfigAdapter(config);

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
    return AdapterDirection.OUTPUT;
  }
}
