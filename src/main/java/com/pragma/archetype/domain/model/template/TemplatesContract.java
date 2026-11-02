package com.pragma.archetype.domain.model.template;

/**
 * Compatibility contract between the plugin and the template repository.
 *
 * <ul>
 * <li>Each plugin major version reads its own living templates branch:
 * plugin 1.x reads {@code v1}, plugin 2.x reads {@code v2}. Fixes and new
 * architectures reach users without a plugin release, and a breaking template
 * change goes to the next branch without affecting older plugins.</li>
 * <li>{@code architectures/index.yml} declares the {@code schemaVersion} of the
 * templates: the shape of structure.yml, metadata.yml and the variables passed
 * to FreeMarker. The plugin refuses templates of a schema it does not
 * understand instead of generating broken code.</li>
 * </ul>
 */
public final class TemplatesContract {

  /** Template schema this plugin understands. */
  public static final int SCHEMA_VERSION = 1;

  /** Branch used when the plugin version cannot be parsed (local builds). */
  static final String FALLBACK_BRANCH = "main";

  private TemplatesContract() {
  }

  /**
   * Templates branch for a plugin version: {@code v<major>}, or {@code main}
   * when the version has no numeric major.
   *
   * @param pluginVersion plugin version such as "1.4.0" or "2.0.0-SNAPSHOT"
   */
  public static String branchFor(String pluginVersion) {
    if (pluginVersion == null) {
      return FALLBACK_BRANCH;
    }
    String major = pluginVersion.split("\\.", 2)[0];
    return major.matches("\\d+") ? "v" + major : FALLBACK_BRANCH;
  }

  /**
   * Fails when the templates declare a schema other than the one this plugin
   * understands.
   *
   * @param templatesSchema schemaVersion declared by architectures/index.yml
   * @param source          where the templates come from, for the message
   */
  public static void requireCompatible(int templatesSchema, String source) {
    if (templatesSchema == SCHEMA_VERSION) {
      return;
    }
    String advice = templatesSchema > SCHEMA_VERSION
        ? "Update the plugin to a version that supports schema " + templatesSchema + "."
        : "Point the plugin to newer templates (remove templates.branch from .cleanarch.yml "
            + "to use the default branch).";
    throw new IncompatibleTemplatesException(String.format(
        "Templates at %s use schema %d, but this plugin understands schema %d. %s",
        source, templatesSchema, SCHEMA_VERSION, advice));
  }

  /** Templates whose schema the plugin does not understand. */
  public static class IncompatibleTemplatesException extends RuntimeException {
    public IncompatibleTemplatesException(String message) {
      super(message);
    }
  }
}
