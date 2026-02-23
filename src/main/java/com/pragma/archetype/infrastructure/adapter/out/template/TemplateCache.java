package com.pragma.archetype.infrastructure.adapter.out.template;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Local cache for downloaded templates.
 * Stores templates in ~/.cleanarch/templates-cache/
 */
public class TemplateCache {

  private final Path cacheDir;

  public TemplateCache() {
    String userHome = System.getProperty("user.home");
    this.cacheDir = Paths.get(userHome, ".cleanarch", "templates-cache");
    ensureCacheDirectoryExists();
  }

  /**
   * Gets cached template content.
   *
   * @param cacheKey unique key for the template (e.g., "main/templates/...")
   * @return template content or null if not cached
   */
  public String get(String cacheKey) {
    Path cachedFile = getCachePath(cacheKey);

    if (!Files.exists(cachedFile)) {
      return null;
    }

    try {
      return Files.readString(cachedFile);
    } catch (IOException e) {
      // If we can't read the cache, return null and let it be re-downloaded
      return null;
    }
  }

  /**
   * Stores template content in cache.
   *
   * @param cacheKey unique key for the template
   * @param content  template content
   */
  public void put(String cacheKey, String content) {
    Path cachedFile = getCachePath(cacheKey);

    try {
      // Create parent directories if they don't exist
      Files.createDirectories(cachedFile.getParent());

      // Atomic write: write to temporary file first, then rename
      Path tempFile = cachedFile.resolveSibling(cachedFile.getFileName() + ".tmp");
      Files.writeString(tempFile, content);
      Files.move(tempFile, cachedFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING,
          java.nio.file.StandardCopyOption.ATOMIC_MOVE);

    } catch (IOException e) {
      // If we can't write to cache, just log and continue
      // The template will work, just won't be cached
      System.err.println("Warning: Failed to cache template: " + e.getMessage());
    }
  }

  /**
   * Checks if a template is cached.
   *
   * @param cacheKey unique key for the template
   * @return true if cached, false otherwise
   */
  public boolean exists(String cacheKey) {
    Path cachedFile = getCachePath(cacheKey);
    return Files.exists(cachedFile);
  }

  /**
   * Clears all cached templates.
   */
  public void clear() {
    try {
      if (Files.exists(cacheDir)) {
        Files.walk(cacheDir)
            .sorted((a, b) -> b.compareTo(a)) // Delete files before directories
            .forEach(path -> {
              try {
                Files.delete(path);
              } catch (IOException e) {
                System.err.println("Warning: Failed to delete " + path + ": " + e.getMessage());
              }
            });
      }
    } catch (IOException e) {
      System.err.println("Warning: Failed to clear cache: " + e.getMessage());
    }
  }

  /**
   * Gets the size of the cache in bytes.
   */
  public long getCacheSize() {
    if (!Files.exists(cacheDir)) {
      return 0;
    }

    try {
      return Files.walk(cacheDir)
          .filter(Files::isRegularFile)
          .mapToLong(path -> {
            try {
              return Files.size(path);
            } catch (IOException e) {
              return 0;
            }
          })
          .sum();
    } catch (IOException e) {
      return 0;
    }
  }

  /**
   * Gets the cache directory path.
   */
  public Path getCacheDirectory() {
    return cacheDir;
  }

  /**
   * Gets the full path for a cached file.
   */
  private Path getCachePath(String cacheKey) {
    // Normalize the cache key to prevent directory traversal
    String normalizedKey = cacheKey.replace("..", "").replace("\\", "/");
    return cacheDir.resolve(normalizedKey);
  }

  /**
   * Ensures the cache directory exists.
   */
  private void ensureCacheDirectoryExists() {
    try {
      if (!Files.exists(cacheDir)) {
        Files.createDirectories(cacheDir);
      }
    } catch (IOException e) {
      System.err.println("Warning: Failed to create cache directory: " + e.getMessage());
    }
  }
}
