package com.pragma.archetype.domain.model.adapter;

import java.util.List;

/**
 * Configuration for generating an input adapter (REST, GraphQL, etc.).
 * Represents the input needed to generate a complete input adapter.
 */
public record InputAdapterConfig(
    String name,
    String packageName,
    InputAdapterType type,
    String useCaseName,
    List<Endpoint> endpoints) {

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String name;
    private String packageName;
    private InputAdapterType type;
    private String useCaseName;
    private List<Endpoint> endpoints;

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder packageName(String packageName) {
      this.packageName = packageName;
      return this;
    }

    public Builder type(InputAdapterType type) {
      this.type = type;
      return this;
    }

    public Builder useCaseName(String useCaseName) {
      this.useCaseName = useCaseName;
      return this;
    }

    public Builder endpoints(List<Endpoint> endpoints) {
      this.endpoints = endpoints;
      return this;
    }

    public InputAdapterConfig build() {
      return new InputAdapterConfig(
          name,
          packageName,
          type,
          useCaseName,
          endpoints);
    }
  }
}
