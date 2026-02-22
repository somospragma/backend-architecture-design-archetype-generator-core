package com.pragma.archetype.domain.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.pragma.archetype.domain.model.AdapterMetadata;

/**
 * Domain service responsible for detecting dependency conflicts.
 * Identifies version conflicts and framework incompatibilities.
 */
public class DependencyConflictDetector {

  /**
   * Detects version conflicts between existing and new dependencies.
   * 
   * @param existingDependencies list of existing dependencies in the project
   * @param newDependencies      list of new dependencies to be added
   * @return list of conflict descriptions
   */
  public List<String> detectVersionConflicts(
      List<AdapterMetadata.Dependency> existingDependencies,
      List<AdapterMetadata.Dependency> newDependencies) {

    Objects.requireNonNull(existingDependencies, "Existing dependencies cannot be null");
    Objects.requireNonNull(newDependencies, "New dependencies cannot be null");

    List<String> conflicts = new ArrayList<>();

    // Build map of existing dependencies: group:artifact -> version
    Map<String, String> existingVersions = new HashMap<>();
    for (AdapterMetadata.Dependency dep : existingDependencies) {
      String key = dep.group() + ":" + dep.artifact();
      existingVersions.put(key, dep.version());
    }

    // Check for version conflicts
    for (AdapterMetadata.Dependency newDep : newDependencies) {
      String key = newDep.group() + ":" + newDep.artifact();
      String existingVersion = existingVersions.get(key);

      if (existingVersion != null && !existingVersion.equals(newDep.version())) {
        conflicts.add(String.format(
            "Version conflict for %s: existing version %s, new version %s",
            key, existingVersion, newDep.version()));
      }
    }

    return conflicts;
  }

  /**
   * Detects conflicts with framework dependencies.
   * Checks for incompatibilities between adapter dependencies and the project
   * framework.
   * 
   * @param frameworkName   the framework used in the project (e.g., "spring",
   *                        "quarkus")
   * @param newDependencies list of new dependencies to be added
   * @return list of conflict descriptions
   */
  public List<String> detectFrameworkConflicts(
      String frameworkName,
      List<AdapterMetadata.Dependency> newDependencies) {

    Objects.requireNonNull(frameworkName, "Framework name cannot be null");
    Objects.requireNonNull(newDependencies, "New dependencies cannot be null");

    List<String> conflicts = new ArrayList<>();

    // Define framework-specific conflict rules
    Map<String, List<String>> incompatibleGroups = getIncompatibleGroups(frameworkName);

    for (AdapterMetadata.Dependency dep : newDependencies) {
      List<String> incompatible = incompatibleGroups.get(dep.group());
      if (incompatible != null) {
        conflicts.add(String.format(
            "Framework conflict: %s:%s may be incompatible with %s framework. " +
                "Consider using %s alternatives.",
            dep.group(), dep.artifact(), frameworkName,
            String.join(", ", incompatible)));
      }
    }

    return conflicts;
  }

  /**
   * Suggests resolutions for detected conflicts.
   * 
   * @param conflicts list of conflict descriptions
   * @return list of resolution suggestions
   */
  public List<String> suggestResolution(List<String> conflicts) {
    Objects.requireNonNull(conflicts, "Conflicts cannot be null");

    List<String> suggestions = new ArrayList<>();

    if (conflicts.isEmpty()) {
      return suggestions;
    }

    suggestions.add("Dependency conflicts detected. Consider the following resolutions:");
    suggestions.add("");

    for (String conflict : conflicts) {
      if (conflict.contains("Version conflict")) {
        suggestions.add("• For version conflicts:");
        suggestions.add("  - Use dependency management to enforce a single version");
        suggestions.add("  - Add version override in .cleanarch.yml under 'dependencyOverrides'");
        suggestions.add("  - Example: dependencyOverrides:");
        suggestions.add("      'org.springframework.boot:spring-boot-starter': '3.2.0'");
        break;
      }
    }

    for (String conflict : conflicts) {
      if (conflict.contains("Framework conflict")) {
        suggestions.add("• For framework conflicts:");
        suggestions.add("  - Review adapter dependencies for framework compatibility");
        suggestions.add("  - Use framework-specific adapter variants when available");
        suggestions.add("  - Consult framework documentation for compatible libraries");
        break;
      }
    }

    suggestions.add("");
    suggestions.add("To override dependency versions, add to .cleanarch.yml:");
    suggestions.add("dependencyOverrides:");
    suggestions.add("  'group:artifact': 'version'");

    return suggestions;
  }

  /**
   * Checks if a dependency override is configured for a given dependency.
   * 
   * @param dependency the dependency to check
   * @param overrides  map of dependency overrides from configuration
   * @return the override version if configured, null otherwise
   */
  public String getVersionOverride(
      AdapterMetadata.Dependency dependency,
      Map<String, String> overrides) {

    Objects.requireNonNull(dependency, "Dependency cannot be null");

    if (overrides == null || overrides.isEmpty()) {
      return null;
    }

    String key = dependency.group() + ":" + dependency.artifact();
    return overrides.get(key);
  }

  /**
   * Applies version overrides to a list of dependencies.
   * 
   * @param dependencies list of dependencies
   * @param overrides    map of dependency overrides
   * @return list of dependencies with overrides applied
   */
  public List<AdapterMetadata.Dependency> applyVersionOverrides(
      List<AdapterMetadata.Dependency> dependencies,
      Map<String, String> overrides) {

    Objects.requireNonNull(dependencies, "Dependencies cannot be null");

    if (overrides == null || overrides.isEmpty()) {
      return new ArrayList<>(dependencies);
    }

    List<AdapterMetadata.Dependency> result = new ArrayList<>();

    for (AdapterMetadata.Dependency dep : dependencies) {
      String overrideVersion = getVersionOverride(dep, overrides);

      if (overrideVersion != null) {
        // Create new dependency with override version
        result.add(new AdapterMetadata.Dependency(
            dep.group(),
            dep.artifact(),
            overrideVersion,
            dep.scope()));
      } else {
        result.add(dep);
      }
    }

    return result;
  }

  /**
   * Returns a map of incompatible dependency groups for each framework.
   */
  private Map<String, List<String>> getIncompatibleGroups(String frameworkName) {
    Map<String, List<String>> incompatible = new HashMap<>();

    switch (frameworkName.toLowerCase()) {
      case "spring":
        // Jakarta EE dependencies might conflict with Spring Boot
        incompatible.put("javax.enterprise", List.of("spring-context", "spring-boot-starter"));
        incompatible.put("io.quarkus", List.of("spring-boot-starter"));
        break;

      case "quarkus":
        // Spring dependencies conflict with Quarkus
        incompatible.put("org.springframework", List.of("quarkus-arc", "quarkus-resteasy"));
        incompatible.put("org.springframework.boot", List.of("quarkus-arc", "quarkus-resteasy"));
        break;

      case "micronaut":
        // Spring and Quarkus dependencies conflict with Micronaut
        incompatible.put("org.springframework", List.of("micronaut-inject"));
        incompatible.put("io.quarkus", List.of("micronaut-inject"));
        break;

      default:
        // No specific conflicts defined for this framework
        break;
    }

    return incompatible;
  }
}
