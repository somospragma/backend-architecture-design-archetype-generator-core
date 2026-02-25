package com.pragma.archetype.domain.model.structure;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.pragma.archetype.domain.model.validation.ValidationResult;

/**
 * Defines allowed dependencies between architectural layers.
 * Used to validate that generated code respects dependency rules.
 */
public record LayerDependencies(
    Map<String, List<String>> allowedDependencies) {

  public LayerDependencies {
    Objects.requireNonNull(allowedDependencies, "Allowed dependencies cannot be null");
    // Make the map and its lists immutable
    allowedDependencies = Collections.unmodifiableMap(
        allowedDependencies.entrySet().stream()
            .collect(java.util.stream.Collectors.toMap(
                Map.Entry::getKey,
                e -> Collections.unmodifiableList(e.getValue()))));
  }

  /**
   * Checks if a layer can depend on another layer.
   *
   * @param fromLayer The layer that wants to depend
   * @param toLayer   The layer to depend on
   * @return true if the dependency is allowed
   */
  public boolean canDependOn(String fromLayer, String toLayer) {
    Objects.requireNonNull(fromLayer, "From layer cannot be null");
    Objects.requireNonNull(toLayer, "To layer cannot be null");

    List<String> allowed = allowedDependencies.get(fromLayer);
    if (allowed == null) {
      return false;
    }

    return allowed.contains(toLayer);
  }

  /**
   * Gets all layers that a given layer can depend on.
   *
   * @param layer The layer to check
   * @return List of allowed dependencies, or empty list if none
   */
  public List<String> getAllowedDependenciesFor(String layer) {
    return allowedDependencies.getOrDefault(layer, List.of());
  }

  /**
   * Validates a dependency relationship.
   *
   * @param fromLayer The layer that wants to depend
   * @param toLayer   The layer to depend on
   * @return ValidationResult indicating if dependency is valid
   */
  public ValidationResult validateDependency(String fromLayer, String toLayer) {
    if (!canDependOn(fromLayer, toLayer)) {
      return ValidationResult.failure(
          String.format("Layer '%s' cannot depend on layer '%s'. Allowed dependencies: %s",
              fromLayer, toLayer, getAllowedDependenciesFor(fromLayer)));
    }
    return ValidationResult.success();
  }
}
