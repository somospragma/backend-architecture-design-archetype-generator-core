package com.pragma.archetype.domain.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pragma.archetype.domain.model.structure.MergeResult;

/**
 * Service for intelligently merging YAML configurations.
 * Preserves existing values and only adds new properties.
 * Detects conflicts when the same key exists with different values.
 */
public class YamlMerger {

  private static final Logger logger = LoggerFactory.getLogger(YamlMerger.class);

  /**
   * Merges overlay map into base map, preserving existing values.
   * Only adds properties that don't already exist in the base.
   *
   * @param base    The existing configuration map
   * @param overlay The new properties to merge
   * @return MergeResult containing merged data, conflicts, and added keys
   */
  public MergeResult merge(Map<String, Object> base, Map<String, Object> overlay) {
    if (base == null) {
      throw new IllegalArgumentException("Base map cannot be null");
    }
    if (overlay == null) {
      throw new IllegalArgumentException("Overlay map cannot be null");
    }

    Map<String, Object> merged = new HashMap<>(base);
    List<String> conflicts = new ArrayList<>();
    List<String> addedKeys = new ArrayList<>();

    mergeRecursive(merged, overlay, "", conflicts, addedKeys);

    if (!conflicts.isEmpty()) {
      logger.warn("Detected {} conflicts during YAML merge", conflicts.size());
      conflicts.forEach(conflict -> logger.warn("Conflict: {}", conflict));
    }

    logger.info("YAML merge completed: {} keys added, {} conflicts detected",
        addedKeys.size(), conflicts.size());

    return new MergeResult(merged, conflicts, addedKeys);
  }

  /**
   * Performs deep merge of nested map structures.
   * Recursively merges nested maps while preserving existing values.
   *
   * @param base    The base map to merge into
   * @param overlay The overlay map to merge from
   * @return The merged map
   */
  public Map<String, Object> deepMerge(Map<String, Object> base, Map<String, Object> overlay) {
    if (base == null) {
      throw new IllegalArgumentException("Base map cannot be null");
    }
    if (overlay == null) {
      throw new IllegalArgumentException("Overlay map cannot be null");
    }

    Map<String, Object> result = new HashMap<>(base);
    List<String> conflicts = new ArrayList<>();
    List<String> addedKeys = new ArrayList<>();

    mergeRecursive(result, overlay, "", conflicts, addedKeys);

    return result;
  }

  /**
   * Checks if merging overlay into base would cause a conflict for the given key.
   * A conflict occurs when the key exists in both maps with different values.
   *
   * @param base    The base map
   * @param overlay The overlay map
   * @param key     The key to check
   * @return true if there would be a conflict, false otherwise
   */
  public boolean hasConflict(Map<String, Object> base, Map<String, Object> overlay, String key) {
    if (base == null || overlay == null || key == null) {
      return false;
    }

    if (!base.containsKey(key) || !overlay.containsKey(key)) {
      return false;
    }

    Object baseValue = base.get(key);
    Object overlayValue = overlay.get(key);

    // If both are maps, check recursively
    if (baseValue instanceof Map && overlayValue instanceof Map) {
      @SuppressWarnings("unchecked")
      Map<String, Object> baseMap = (Map<String, Object>) baseValue;
      @SuppressWarnings("unchecked")
      Map<String, Object> overlayMap = (Map<String, Object>) overlayValue;

      // Check if any nested keys would conflict
      for (String nestedKey : overlayMap.keySet()) {
        if (hasConflict(baseMap, overlayMap, nestedKey)) {
          return true;
        }
      }
      return false;
    }

    // Values are different and not both maps - this is a conflict
    return !areValuesEqual(baseValue, overlayValue);
  }

  /**
   * Recursively merges overlay into base, tracking conflicts and added keys.
   */
  private void mergeRecursive(
      Map<String, Object> base,
      Map<String, Object> overlay,
      String path,
      List<String> conflicts,
      List<String> addedKeys) {

    for (Map.Entry<String, Object> entry : overlay.entrySet()) {
      String key = entry.getKey();
      Object overlayValue = entry.getValue();
      String currentPath = path.isEmpty() ? key : path + "." + key;

      if (!base.containsKey(key)) {
        // Key doesn't exist in base - add it
        base.put(key, deepCopy(overlayValue));
        addedKeys.add(currentPath);
        logger.debug("Added new key: {}", currentPath);
      } else {
        // Key exists in base
        Object baseValue = base.get(key);

        if (baseValue instanceof Map && overlayValue instanceof Map) {
          // Both are maps - merge recursively
          @SuppressWarnings("unchecked")
          Map<String, Object> baseMap = (Map<String, Object>) baseValue;
          @SuppressWarnings("unchecked")
          Map<String, Object> overlayMap = (Map<String, Object>) overlayValue;

          mergeRecursive(baseMap, overlayMap, currentPath, conflicts, addedKeys);
        } else if (!areValuesEqual(baseValue, overlayValue)) {
          // Values are different - this is a conflict
          String conflict = String.format(
              "Property '%s' already exists with value '%s', keeping existing value (new value: '%s')",
              currentPath, baseValue, overlayValue);
          conflicts.add(conflict);
          logger.debug("Conflict detected at: {}", currentPath);
        }
        // If values are equal, no action needed
      }
    }
  }

  /**
   * Checks if two values are equal, handling null values.
   */
  private boolean areValuesEqual(Object value1, Object value2) {
    if (value1 == null && value2 == null) {
      return true;
    }
    if (value1 == null || value2 == null) {
      return false;
    }
    return value1.equals(value2);
  }

  /**
   * Creates a deep copy of a value, handling maps and lists.
   */
  @SuppressWarnings("unchecked")
  private Object deepCopy(Object value) {
    if (value == null) {
      return null;
    }

    if (value instanceof Map) {
      Map<String, Object> original = (Map<String, Object>) value;
      Map<String, Object> copy = new HashMap<>();
      for (Map.Entry<String, Object> entry : original.entrySet()) {
        copy.put(entry.getKey(), deepCopy(entry.getValue()));
      }
      return copy;
    }

    if (value instanceof List) {
      List<Object> original = (List<Object>) value;
      List<Object> copy = new ArrayList<>();
      for (Object item : original) {
        copy.add(deepCopy(item));
      }
      return copy;
    }

    // For primitive types and strings, return as-is (they're immutable)
    return value;
  }
}
