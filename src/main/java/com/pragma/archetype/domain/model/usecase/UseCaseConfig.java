package com.pragma.archetype.domain.model.usecase;

import java.util.List;

import lombok.Builder;

/**
 * Configuration for generating a use case.
 * Represents the input needed to generate a complete use case with its port and
 * implementation.
 */
@Builder
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
}
