package com.pragma.archetype.domain.model.config;

/**
 * Represents the source from which templates are loaded.
 */
public enum TemplateSource {
  /**
   * Templates loaded from configured local path in .cleanarch.yml.
   */
  LOCAL_CONFIGURED,

  /**
   * Templates loaded from auto-detected local path
   * (../backend-architecture-design-archetype-generator-templates).
   */
  LOCAL_AUTO_DETECTED,

  /**
   * Templates loaded from remote Git repository.
   */
  REMOTE
}
