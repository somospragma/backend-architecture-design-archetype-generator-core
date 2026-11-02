package com.pragma.archetype.domain.model.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Version of the running plugin, read from the plugin.properties file the build
 * generates.
 */
public final class PluginVersion {

  static final String UNKNOWN = "0.0.0-unknown";

  private static final String CURRENT = read();

  private PluginVersion() {
  }

  /** Version of the running plugin, or {@value #UNKNOWN} outside a plugin build. */
  public static String current() {
    return CURRENT;
  }

  private static String read() {
    try (InputStream stream = PluginVersion.class.getClassLoader().getResourceAsStream("plugin.properties")) {
      if (stream == null) {
        return UNKNOWN;
      }
      Properties properties = new Properties();
      properties.load(stream);
      String version = properties.getProperty("version");
      return version == null || version.isBlank() ? UNKNOWN : version.trim();
    } catch (IOException e) {
      return UNKNOWN;
    }
  }
}
