package com.pragma.archetype.application.generator;

import java.util.List;

import com.pragma.archetype.domain.model.adapter.AdapterMethod;
import com.pragma.archetype.domain.model.adapter.Endpoint;
import com.pragma.archetype.domain.model.adapter.OutputAdapterConfig;

import lombok.RequiredArgsConstructor;

/**
 * Adapter that wraps OutputAdapterConfig to implement
 * UnifiedAdapterGenerator.AdapterConfig.
 * Allows OutputAdapterConfig to be used with the unified generator.
 */
@RequiredArgsConstructor
public class OutputAdapterConfigAdapter implements UnifiedAdapterGenerator.AdapterConfig {

  private final OutputAdapterConfig config;

  @Override
  public String getName() {
    return config.name();
  }

  @Override
  public String getType() {
    return config.type();
  }

  @Override
  public String getPackageName() {
    return config.packageName();
  }

  @Override
  public String getFramework() {
    return config.framework();
  }

  @Override
  public String getParadigm() {
    return config.paradigm();
  }

  @Override
  public String getUseCaseName() {
    return null; // Not applicable for output adapters
  }

  @Override
  public List<Endpoint> getEndpoints() {
    return null; // Not applicable for output adapters
  }

  @Override
  public String getEntityName() {
    return config.entityName();
  }

  @Override
  public List<AdapterMethod> getMethods() {
    return config.methods();
  }
}
