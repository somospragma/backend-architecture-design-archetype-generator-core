package com.pragma.archetype.infrastructure.adapter.in.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import com.pragma.archetype.infrastructure.adapter.out.template.TemplateCache;

/**
 * Gradle task to clear the template cache.
 * Usage: ./gradlew clearTemplateCache
 */
public class ClearTemplateCacheTask extends DefaultTask {

  public ClearTemplateCacheTask() {
    setGroup("Clean Architecture");
    setDescription("Clears the local template cache");
  }

  @TaskAction
  public void clearCache() {
    getLogger().lifecycle("Clearing template cache...");

    TemplateCache cache = new TemplateCache();

    // Get cache size before clearing
    long sizeBefore = cache.getCacheSize();
    String sizeStr = formatBytes(sizeBefore);

    // Clear cache
    cache.clear();

    getLogger().lifecycle("✓ Template cache cleared successfully");
    getLogger().lifecycle("  Freed: {}", sizeStr);
    getLogger().lifecycle("  Location: {}", cache.getCacheDirectory());
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
