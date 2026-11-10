package com.pragma.archetype.domain.model.template;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Versions declared by the template repository in {@code versions.yml}.
 * The template repository is the single source of truth for every version used
 * by generated projects (Java, Gradle, libraries, plugins and quality gates),
 * so bumping a version never requires a new plugin release.
 *
 * @param schemaVersion version of the versions.yml format
 * @param java          Java toolchain version (e.g. "21")
 * @param gradle        Gradle wrapper version (e.g. "8.14.1")
 * @param libraries     library and plugin versions keyed by catalog alias
 *                      (e.g. "spring-boot" -> "4.0.3")
 * @param quality       quality gate thresholds (e.g. "jacoco-min-coverage")
 */
public record TemplateVersions(
    int schemaVersion,
    String java,
    String gradle,
    Map<String, String> libraries,
    Map<String, String> quality) {

  public TemplateVersions {
    libraries = libraries == null ? Map.of() : Map.copyOf(libraries);
    quality = quality == null ? Map.of() : Map.copyOf(quality);
  }

  /**
   * Versions used when the template repository has no versions.yml (templates
   * older than the versions contract).
   */
  public static TemplateVersions empty() {
    return new TemplateVersions(0, null, null, Map.of(), Map.of());
  }

  public boolean isEmpty() {
    return schemaVersion == 0;
  }

  /**
   * Resolves a library version by its catalog alias.
   *
   * @param alias catalog alias declared under {@code libraries}
   * @return the version, or empty if the alias is not declared
   */
  public Optional<String> library(String alias) {
    return Optional.ofNullable(libraries.get(alias));
  }

  /**
   * Builds the {@code versions} model exposed to FreeMarker templates, used as
   * {@code ${versions.java}} or {@code ${versions.libraries["spring-boot"]}}.
   */
  public Map<String, Object> toTemplateModel() {
    Map<String, Object> model = new HashMap<>();
    if (java != null) {
      model.put("java", java);
    }
    if (gradle != null) {
      model.put("gradle", gradle);
    }
    model.put("libraries", libraries);
    model.put("quality", quality);
    return model;
  }
}
