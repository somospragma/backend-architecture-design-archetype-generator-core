package com.pragma.archetype.domain.model.adapter;

import java.util.List;

/**
 * Configuration for generating an output adapter.
 * Represents the input needed to generate a complete adapter with its
 * implementation.
 */
public record AdapterConfig(
    String name,
    String packageName,
    AdapterType type,
    String entityName,
    List<AdapterMethod> methods) {

  /**
   * Represents a method in the adapter.
   */
  public record AdapterMethod(
      String name,
      String returnType,
      List<MethodParameter> parameters) {
  }

  /**
   * Represents a parameter in an adapter method.
   */
  public record MethodParameter(
      String name,
      String type) {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String name;
    private String packageName;
    private AdapterType type;
    private String entityName;
    private List<AdapterMethod> methods;

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder packageName(String packageName) {
      this.packageName = packageName;
      return this;
    }

    public Builder type(AdapterType type) {
      this.type = type;
      return this;
    }

    public Builder entityName(String entityName) {
      this.entityName = entityName;
      return this;
    }

    public Builder methods(List<AdapterMethod> methods) {
      this.methods = methods;
      return this;
    }

    public AdapterConfig build() {
      return new AdapterConfig(
          name,
          packageName,
          type,
          entityName,
          methods);
    }
  }
}
