package com.pragma.archetype.infrastructure.adapter.in.gradle;

import java.nio.file.Path;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Internal;

import com.pragma.archetype.domain.model.config.ProjectConfig;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.infrastructure.adapter.out.config.YamlConfigurationAdapter;

/**
 * Base class for all Clean Architecture Generator tasks.
 * Provides common functionality like getting the current plugin version.
 */
public abstract class BaseCleanArchTask extends DefaultTask {

  /**
   * Gets the current version of the plugin that is executing.
   * This reads the version from the plugin's own metadata, not from the user's
   * project.
   * 
   * @return The plugin version (e.g., "1.3.5-SNAPSHOT")
   */
  @Internal
  protected String getPluginVersion() {
    try {
      // First try: read from plugin's properties file
      try (var stream = getClass().getClassLoader().getResourceAsStream("plugin.properties")) {
        if (stream != null) {
          var props = new java.util.Properties();
          props.load(stream);
          String version = props.getProperty("version");
          if (version != null && !version.isBlank()) {
            getLogger().debug("Plugin version from plugin.properties: {}", version);
            return version;
          }
        }
      }

      // Second try: get version from plugin's implementation version
      var pluginManager = getProject().getPluginManager();
      var plugin = pluginManager.findPlugin("co.com.pragma.archetype-generator");

      if (plugin != null) {
        String version = plugin.getClass().getPackage().getImplementationVersion();
        if (version != null && !version.isBlank()) {
          getLogger().debug("Plugin version from implementation: {}", version);
          return version;
        }
      }

      // Last resort: use a default version
      getLogger().warn("Could not determine plugin version, using default: 1.3.5-SNAPSHOT");
      return "1.3.5-SNAPSHOT";

    } catch (Exception e) {
      getLogger().warn("Error determining plugin version: {}", e.getMessage());
      return "1.3.5-SNAPSHOT";
    }
  }

  /**
   * Updates the plugin version in .cleanarch.yml if it's different from the
   * current plugin version.
   * This ensures that the project always uses the correct version of the plugin.
   */
  protected void updatePluginVersionIfNeeded() {
    try {
      Path projectPath = getProject().getProjectDir().toPath();
      ConfigurationPort configPort = new YamlConfigurationAdapter();

      // Check if .cleanarch.yml exists
      if (!configPort.configurationExists(projectPath)) {
        return; // No configuration file, nothing to update
      }

      // Read current configuration
      var currentConfig = configPort.readConfiguration(projectPath);
      if (currentConfig.isEmpty()) {
        return;
      }

      ProjectConfig config = currentConfig.get();
      String currentPluginVersion = getPluginVersion();

      // Check if version needs update
      if (!config.pluginVersion().equals(currentPluginVersion)) {
        getLogger().lifecycle("Updating plugin version in .cleanarch.yml: {} -> {}",
            config.pluginVersion(), currentPluginVersion);

        // Create updated configuration
        ProjectConfig updatedConfig = config.toBuilder()
            .pluginVersion(currentPluginVersion)
            .build();

        // Write updated configuration
        configPort.writeConfiguration(projectPath, updatedConfig);
      }

    } catch (Exception e) {
      // Don't fail the task if version update fails
      getLogger().debug("Could not update plugin version in .cleanarch.yml: {}", e.getMessage());
    }
  }
}
