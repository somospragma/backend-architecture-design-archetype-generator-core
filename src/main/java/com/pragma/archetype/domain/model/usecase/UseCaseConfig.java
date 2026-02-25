package com.pragma.archetype.domain.model.usecase;

import java.util.List;

/**
 * Configuration for generating a use case.
 * Represents the input needed to generate a complete use case with its port and
 * implementation.
 */
public record UseCaseConfig(
    String name,
    String packageName,
    List<UseCaseMethod> methods,
    boolean generatePort,
    boolean generateImpl) {

  /**
   * Represents a method in the use case.
   */
  public record UseCaseMethod(
      String name,
      String returnType,
      List<MethodParameter> parameters) {
  }

  /**
   * Represents a parameter in a use case method.
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
    private List<UseCaseMethod> methods;
    private boolean generatePort = true;
    private boolean generateImpl = true;

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder packageName(String packageName) {
      this.packageName = packageName;
      return this;
    }

    public Builder methods(List<UseCaseMethod> methods) {
      this.methods = methods;
      return this;
    }

    public Builder generatePort(boolean generatePort) {
      this.generatePort = generatePort;
      return this;
    }

    public Builder generateImpl(boolean generateImpl) {
      this.generateImpl = generateImpl;
      return this;
    }

    public UseCaseConfig build() {
      return new UseCaseConfig(
          name,
          packageName,
          methods,
          generatePort,
          generateImpl);
    }
  }
}
