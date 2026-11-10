package com.pragma.archetype.domain.model.template;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Compatibility contract between the plugin and the template repository.
 *
 * <ul>
 * <li>Each plugin major version reads the newest templates release of that
 * major: plugin 1.x reads the latest {@code v1.x.y} tag, plugin 2.x the latest
 * {@code v2.x.y}. Fixes and new architectures reach users with a new templates
 * tag, without a plugin release, and a breaking template change goes to the
 * next major without affecting older plugins.</li>
 * <li>{@code architectures/index.yml} declares the {@code schemaVersion} of the
 * templates: the shape of structure.yml, metadata.yml and the variables passed
 * to FreeMarker. The plugin refuses templates of a schema it does not
 * understand instead of generating broken code.</li>
 * </ul>
 */
public final class TemplatesContract {

  /** Template schema this plugin understands. */
  public static final int SCHEMA_VERSION = 1;

  /** Default branch of the templates, read when the plugin version has no major (local builds). */
  public static final String DEFAULT_BRANCH = "main";

  // At most 9 digits per number, so every part fits in an int
  private static final Pattern RELEASE_TAG = Pattern.compile("v(\\d{1,9})\\.(\\d{1,9})\\.(\\d{1,9})");

  private TemplatesContract() {
  }

  /**
   * Major version of a plugin version, or empty when it has none: local builds
   * and 0.x versions read {@link #DEFAULT_BRANCH}.
   *
   * @param pluginVersion plugin version such as "1.4.0" or "2.0.0-SNAPSHOT"
   */
  public static OptionalInt majorOf(String pluginVersion) {
    if (pluginVersion == null) {
      return OptionalInt.empty();
    }
    String major = pluginVersion.split("\\.", 2)[0];
    if (!major.matches("\\d{1,9}") || Integer.parseInt(major) < 1) {
      return OptionalInt.empty();
    }
    return OptionalInt.of(Integer.parseInt(major));
  }

  /**
   * Newest release tag {@code v<major>.<minor>.<patch>} of a major among the
   * given tag names. Other majors and any other tag name are ignored.
   *
   * @param major plugin major version
   * @param tags  tag names, such as "v1.2.0"
   */
  public static Optional<String> latestTag(int major, Collection<String> tags) {
    return tags.stream()
        .map(RELEASE_TAG::matcher)
        .filter(Matcher::matches)
        .filter(m -> Integer.parseInt(m.group(1)) == major)
        .max(Comparator.<Matcher>comparingInt(m -> Integer.parseInt(m.group(2)))
            .thenComparingInt(m -> Integer.parseInt(m.group(3))))
        .map(Matcher::group);
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
        : "Point the plugin to newer templates (remove templates.version and templates.branch "
            + "from .cleanarch.yml to use the latest release).";
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
