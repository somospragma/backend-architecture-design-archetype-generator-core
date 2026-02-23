package com.pragma.archetype.domain.model;

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

  /**
   * Type of input adapter to generate.
   */
  public enum InputAdapterType {
    REST,
    GRAPHQL,
    GRPC,
    WEBSOCKET
  }

  /**
   * HTTP method for REST endpoints.
   */
  public enum HttpMethod {
    GET,
    POST,
    PUT,
    DELETE,
    PATCH
  }

  /**
   * Represents an endpoint in the adapter.
   */
  public record Endpoint(
      String path,
      HttpMethod method,
      String useCaseMethod,
      String returnType,
      List<EndpointParameter> parameters) {
  }

  /**
   * Represents a parameter in an endpoint.
   */
  public record EndpointParameter(
      String name,
      String type,
      ParameterType paramType) {
  }

  /**
   * Type of parameter (path variable, request body, query param).
   */
  public enum ParameterType {
    PATH,
    BODY,
    QUERY
  }

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
