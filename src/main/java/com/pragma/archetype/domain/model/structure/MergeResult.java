package com.pragma.archetype.domain.model.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Result of a YAML merge operation.
 * Contains the merged data, conflicts detected, and keys that were added.
 */
public record MergeResult(
    Map<String, Object> merged,
    List<String> conflicts,
    List<String> addedKeys) {

  public MergeResult {
    Objects.requireNonNull(merged, "Merged map cannot be null");
    Objects.requireNonNull(conflicts, "Conflicts list cannot be null");
    Objects.requireNonNull(addedKeys, "Added keys list cannot be null");
    conflicts = Collections.unmodifiableList(new ArrayList<>(conflicts));
    addedKeys = Collections.unmodifiableList(new ArrayList<>(addedKeys));
  }

  /**
   * Creates a merge result with no conflicts.
   */
  public static MergeResult success(Map<String, Object> merged, List<String> addedKeys) {
    return new MergeResult(merged, List.of(), addedKeys);
  }

  /**
   * Creates a merge result with conflicts.
   */
  public static MergeResult withConflicts(Map<String, Object> merged, List<String> conflicts, List<String> addedKeys) {
    return new MergeResult(merged, conflicts, addedKeys);
  }

  /**
   * Checks if there were any conflicts during the merge.
   */
  public boolean hasConflicts() {
    return !conflicts.isEmpty();
  }

  /**
   * Gets the number of keys that were added.
   */
  public int addedKeysCount() {
    return addedKeys.size();
  }

  /**
   * Gets all conflicts as a single string.
   */
  public String getAllConflicts() {
    return String.join("\n", conflicts);
  }
}
