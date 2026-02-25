package com.pragma.archetype.infrastructure.adapter.in.gradle;

import java.nio.file.Paths;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import com.pragma.archetype.domain.model.config.TemplateConfig;
import com.pragma.archetype.infrastructure.adapter.out.config.YamlConfigurationAdapter;
import com.pragma.archetype.infrastructure.adapter.out.template.TemplateCache;

/**
 * Gradle task to update (re-download) templates from repository.
 * Usage: ./gradlew updateTemplates
 */
public class UpdateTemplatesTask extends DefaultTask {

  public UpdateTemplatesTask() {
    setGroup("Clean Architecture");
    setDescription("Updates templates by clearing cache and forcing re-download");
  }

  @TaskAction
  public void updateTemplates() {
    getLogger().lifecycle("Updating templates...");

    // Read template configuration
    YamlConfigurationAdapter configAdapter = new YamlConfigurationAdapter();
    TemplateConfig templateConfig = configAdapter.readTemplateConfiguration(
        Paths.get(getProject().getProjectDir().getAbsolutePath()));

    getLogger().lifecycle("Template configuration:");
    getLogger().lifecycle("  Mode: {}", templateConfig.mode());
    getLogger().lifecycle("  Repository: {}", templateConfig.repository());
    getLogger().lifecycle("  Branch: {}", templateConfig.getEffectiveBranch());

    if (templateConfig.isLocalMode()) {
      getLogger().lifecycle("  Local path: {}", templateConfig.localPath());
      getLogger().lifecycle("");
      getLogger().lifecycle("✓ Using local templates - no update needed");
      getLogger().lifecycle("  Templates are loaded from local filesystem");
      return;
    }

    // Clear cache to force re-download
    TemplateCache cache = new TemplateCache();
    long sizeBefore = cache.getCacheSize();
    cache.clear();

    getLogger().lifecycle("");
    getLogger().lifecycle("✓ Template cache cleared");
    getLogger().lifecycle("  Freed: {}", formatBytes(sizeBefore));
    getLogger().lifecycle("");
    getLogger().lifecycle("Templates will be re-downloaded on next use from:");
    getLogger().lifecycle("  {}/{}", templateConfig.repository(), templateConfig.getEffectiveBranch());
  }

  /**
   * Formats bytes to human-readable format.
   */
  private String formatBytes(long bytes) {
    if (bytes < 1024) {
      return bytes + " B";
    } else if (bytes < 1024 * 1024) {
      return String.format("%.2f KB", bytes / 1024.0);
    } else if (bytes < 1024 * 1024 * 1024) {
      return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    } else {
      return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
  }
}
