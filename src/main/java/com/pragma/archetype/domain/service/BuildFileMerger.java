package com.pragma.archetype.domain.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pragma.archetype.domain.model.adapter.AdapterMetadata.BuildPlugin;

/**
 * Service for merging build configuration into build.gradle.kts files.
 * Handles adding plugins and build configuration blocks.
 */
public class BuildFileMerger {

  private static final Logger logger = LoggerFactory.getLogger(BuildFileMerger.class);

  private static final String PLUGINS_BLOCK_START = "plugins {";
  private static final String BUILD_CONFIG_COMMENT = "\n// Configuration added by adapter\n";

  /**
   * Adds plugins to the plugins {} block in build.gradle.kts.
   * If the plugin already exists, it is skipped.
   *
   * @param buildFileContent current build file content
   * @param plugins          list of plugins to add
   * @return updated build file content
   */
  public String addPlugins(String buildFileContent, List<BuildPlugin> plugins) {
    if (plugins == null || plugins.isEmpty()) {
      return buildFileContent;
    }

    // Find plugins block
    int pluginsStart = buildFileContent.indexOf(PLUGINS_BLOCK_START);
    if (pluginsStart == -1) {
      logger.warn("No plugins block found in build.gradle.kts, cannot add plugins");
      return buildFileContent;
    }

    // Find the closing brace of the plugins block
    int blockEnd = findClosingBrace(buildFileContent, pluginsStart);
    if (blockEnd == -1) {
      logger.warn("Could not find closing brace for plugins block");
      return buildFileContent;
    }

    var result = new StringBuilder(buildFileContent);
    int insertPosition = blockEnd;

    // Add each plugin if not already present
    for (BuildPlugin plugin : plugins) {
      String declaration = plugin.toGradleDeclaration();

      // Check if plugin is already present (check by plugin id)
      String pluginIdCheck = String.format("id(\"%s\")", plugin.id());
      if (buildFileContent.contains(pluginIdCheck)) {
        logger.debug("Plugin already present: {}", plugin.id());
        continue;
      }

      // Insert the plugin declaration before the closing brace
      String pluginLine = "\n" + declaration;
      result.insert(insertPosition, pluginLine);
      insertPosition += pluginLine.length();

      logger.info("Added plugin: {}", plugin.id());
    }

    return result.toString();
  }

  /**
   * Adds build configuration to the end of build.gradle.kts.
   * Configuration is added after a comment separator.
   *
   * @param buildFileContent current build file content
   * @param configuration    configuration block to add
   * @return updated build file content
   */
  public String addBuildConfiguration(String buildFileContent, String configuration) {
    if (configuration == null || configuration.isBlank()) {
      return buildFileContent;
    }

    // Check if configuration is already present (simple check)
    if (buildFileContent.contains(configuration.trim())) {
      logger.debug("Build configuration already present, skipping");
      return buildFileContent;
    }

    // Add configuration at the end of the file
    String result = buildFileContent;

    // Ensure file ends with newline
    if (!result.endsWith("\n")) {
      result += "\n";
    }

    // Add comment and configuration
    result += BUILD_CONFIG_COMMENT + configuration;

    // Ensure final newline
    if (!result.endsWith("\n")) {
      result += "\n";
    }

    logger.info("Added build configuration block");
    return result;
  }

  /**
   * Finds the closing brace for a block starting at the given position.
   * Handles nested braces correctly.
   *
   * @param content       file content
   * @param blockStartPos position where the block starts (at the opening brace)
   * @return position of the closing brace, or -1 if not found
   */
  private int findClosingBrace(String content, int blockStartPos) {
    // Find the opening brace
    int openBracePos = content.indexOf('{', blockStartPos);
    if (openBracePos == -1) {
      return -1;
    }

    int braceCount = 1;
    int pos = openBracePos + 1;

    while (pos < content.length() && braceCount > 0) {
      char c = content.charAt(pos);

      if (c == '{') {
        braceCount++;
      } else if (c == '}') {
        braceCount--;
      }

      pos++;
    }

    // Return position just before the closing brace (where we want to insert)
    return braceCount == 0 ? pos - 1 : -1;
  }
}
