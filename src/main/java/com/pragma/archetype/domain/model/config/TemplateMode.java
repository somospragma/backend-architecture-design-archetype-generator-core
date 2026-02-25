package com.pragma.archetype.domain.model.config;

/**
 * Mode for template loading.
 */
public enum TemplateMode {
  /**
   * Production mode: Load templates from GitHub repository with caching.
   */
  PRODUCTION,

  /**
   * Developer mode: Load templates from local path or custom repository/branch.
   */
  DEVELOPER
}
