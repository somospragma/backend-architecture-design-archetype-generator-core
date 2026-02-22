package com.pragma.archetype.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class MergeResultTest {

  @Test
  void shouldCreateMergeResult() {
    // Given
    Map<String, Object> merged = new HashMap<>();
    merged.put("key1", "value1");
    List<String> conflicts = List.of("conflict1");
    List<String> addedKeys = List.of("key2");

    // When
    MergeResult result = new MergeResult(merged, conflicts, addedKeys);

    // Then
    assertEquals(1, result.merged().size());
    assertEquals("value1", result.merged().get("key1"));
    assertEquals(1, result.conflicts().size());
    assertEquals("conflict1", result.conflicts().get(0));
    assertEquals(1, result.addedKeys().size());
    assertEquals("key2", result.addedKeys().get(0));
  }

  @Test
  void shouldHandleEmptyConflicts() {
    // Given
    Map<String, Object> merged = new HashMap<>();
    List<String> conflicts = List.of();
    List<String> addedKeys = List.of("key1");

    // When
    MergeResult result = new MergeResult(merged, conflicts, addedKeys);

    // Then
    assertTrue(result.conflicts().isEmpty());
  }

  @Test
  void shouldHandleEmptyAddedKeys() {
    // Given
    Map<String, Object> merged = new HashMap<>();
    List<String> conflicts = List.of("conflict1");
    List<String> addedKeys = List.of();

    // When
    MergeResult result = new MergeResult(merged, conflicts, addedKeys);

    // Then
    assertTrue(result.addedKeys().isEmpty());
  }

  @Test
  void shouldHandleComplexMergedData() {
    // Given
    Map<String, Object> merged = new HashMap<>();
    Map<String, Object> nested = new HashMap<>();
    nested.put("nested1", "value1");
    merged.put("parent", nested);
    merged.put("simple", "value2");

    // When
    MergeResult result = new MergeResult(merged, List.of(), List.of());

    // Then
    assertEquals(2, result.merged().size());
    assertTrue(result.merged().containsKey("parent"));
    assertTrue(result.merged().containsKey("simple"));
  }
}
