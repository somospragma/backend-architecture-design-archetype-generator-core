package com.pragma.archetype.application.generator;

import java.util.List;

import com.pragma.archetype.domain.model.adapter.AdapterMethod;
import com.pragma.archetype.domain.model.adapter.Endpoint;
import com.pragma.archetype.domain.model.adapter.InputAdapterConfig;

import lombok.RequiredArgsConstructor;

/**
 * Adapter that wraps InputAdapterConfig to implement
 * UnifiedAdapterGenerator.AdapterConfig.
 * Allows InputAdapterConfig to be used with the unified generator.
 */
@RequiredArgsConstructor
public class InputAdapterConfigAdapter implements UnifiedAdapterGenerator.AdapterConfig {

  private final InputAdapterConfig config;

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
    return config.useCaseName();
  }

  @Override
  public List<Endpoint> getEndpoints() {
    return config.endpoints();
  }

  @Override
  public String getEntityName() {
    return null; // Not applicable for input adapters
  }

  @Override
  public List<AdapterMethod> getMethods() {
    return null; // Not applicable for input adapters
  }
}
