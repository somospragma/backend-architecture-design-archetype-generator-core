package com.pragma.archetype.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.pragma.archetype.domain.model.structure.MergeResult;

class YamlMergerTest {

  private YamlMerger merger;

  @BeforeEach
  void setUp() {
    merger = new YamlMerger();
  }

  @Test
  void shouldMergeSimpleMaps() {
    // Given
    Map<String, Object> base = new HashMap<>();
    base.put("key1", "value1");

    Map<String, Object> overlay = new HashMap<>();
    overlay.put("key2", "value2");

    // When
    MergeResult result = merger.merge(base, overlay);

    // Then
    assertEquals(2, result.merged().size());
    assertEquals("value1", result.merged().get("key1"));
    assertEquals("value2", result.merged().get("key2"));
    assertTrue(result.conflicts().isEmpty());
    assertEquals(1, result.addedKeys().size());
  }

  @Test
  void shouldDetectConflict() {
    // Given
    Map<String, Object> base = new HashMap<>();
    base.put("key1", "value1");

    Map<String, Object> overlay = new HashMap<>();
    overlay.put("key1", "value2");

    // When
    MergeResult result = merger.merge(base, overlay);

    // Then
    assertEquals("value1", result.merged().get("key1")); // keeps existing value
    assertFalse(result.conflicts().isEmpty());
    assertTrue(result.conflicts().get(0).contains("already exists"));
  }

  @Test
  void shouldMergeNestedMaps() {
    // Given
    Map<String, Object> base = new HashMap<>();
    Map<String, Object> baseNested = new HashMap<>();
    baseNested.put("nested1", "value1");
    base.put("parent", baseNested);

    Map<String, Object> overlay = new HashMap<>();
    Map<String, Object> overlayNested = new HashMap<>();
    overlayNested.put("nested2", "value2");
    overlay.put("parent", overlayNested);

    // When
    MergeResult result = merger.merge(base, overlay);

    // Then
    @SuppressWarnings("unchecked")
    Map<String, Object> mergedParent = (Map<String, Object>) result.merged().get("parent");
    assertEquals(2, mergedParent.size());
    assertEquals("value1", mergedParent.get("nested1"));
    assertEquals("value2", mergedParent.get("nested2"));
  }

  @Test
  void shouldDeepMerge() {
    // Given
    Map<String, Object> base = new HashMap<>();
    base.put("key1", "value1");

    Map<String, Object> overlay = new HashMap<>();
    overlay.put("key2", "value2");

    // When
    Map<String, Object> result = merger.deepMerge(base, overlay);

    // Then
    assertEquals(2, result.size());
    assertEquals("value1", result.get("key1"));
    assertEquals("value2", result.get("key2"));
  }

  @Test
  void shouldDetectConflictForKey() {
    // Given
    Map<String, Object> base = new HashMap<>();
    base.put("key1", "value1");

    Map<String, Object> overlay = new HashMap<>();
    overlay.put("key1", "value2");

    // When
    boolean hasConflict = merger.hasConflict(base, overlay, "key1");

    // Then
    assertTrue(hasConflict);
  }

  @Test
  void shouldNotDetectConflictForSameValue() {
    // Given
    Map<String, Object> base = new HashMap<>();
    base.put("key1", "value1");

    Map<String, Object> overlay = new HashMap<>();
    overlay.put("key1", "value1");

    // When
    boolean hasConflict = merger.hasConflict(base, overlay, "key1");

    // Then
    assertFalse(hasConflict);
  }

  @Test
  void shouldNotDetectConflictForNewKey() {
    // Given
    Map<String, Object> base = new HashMap<>();
    base.put("key1", "value1");

    Map<String, Object> overlay = new HashMap<>();
    overlay.put("key2", "value2");

    // When
    boolean hasConflict = merger.hasConflict(base, overlay, "key2");

    // Then
    assertFalse(hasConflict);
  }

  @Test
  void shouldHandleNullValues() {
    // Given
    Map<String, Object> base = new HashMap<>();
    base.put("key1", null);

    Map<String, Object> overlay = new HashMap<>();
    overlay.put("key1", "value1");

    // When
    MergeResult result = merger.merge(base, overlay);

    // Then
    assertNull(result.merged().get("key1")); // keeps existing null
    assertFalse(result.conflicts().isEmpty());
  }

  @Test
  void shouldThrowExceptionForNullBase() {
    // Given
    Map<String, Object> overlay = new HashMap<>();

    // When/Then
    assertThrows(IllegalArgumentException.class, () -> {
      merger.merge(null, overlay);
    });
  }

  @Test
  void shouldThrowExceptionForNullOverlay() {
    // Given
    Map<String, Object> base = new HashMap<>();

    // When/Then
    assertThrows(IllegalArgumentException.class, () -> {
      merger.merge(base, null);
    });
  }

  @Test
  void shouldMergeListValues() {
    // Given
    Map<String, Object> base = new HashMap<>();
    base.put("list1", List.of("item1"));

    Map<String, Object> overlay = new HashMap<>();
    overlay.put("list2", List.of("item2"));

    // When
    MergeResult result = merger.merge(base, overlay);

    // Then
    assertEquals(2, result.merged().size());
    assertTrue(result.merged().containsKey("list1"));
    assertTrue(result.merged().containsKey("list2"));
  }

  @Test
  void shouldHandleComplexNestedStructure() {
    // Given
    Map<String, Object> base = new HashMap<>();
    Map<String, Object> level1 = new HashMap<>();
    Map<String, Object> level2 = new HashMap<>();
    level2.put("deep", "value1");
    level1.put("level2", level2);
    base.put("level1", level1);

    Map<String, Object> overlay = new HashMap<>();
    Map<String, Object> overlayLevel1 = new HashMap<>();
    Map<String, Object> overlayLevel2 = new HashMap<>();
    overlayLevel2.put("deep2", "value2");
    overlayLevel1.put("level2", overlayLevel2);
    overlay.put("level1", overlayLevel1);

    // When
    MergeResult result = merger.merge(base, overlay);

    // Then
    @SuppressWarnings("unchecked")
    Map<String, Object> mergedLevel1 = (Map<String, Object>) result.merged().get("level1");
    @SuppressWarnings("unchecked")
    Map<String, Object> mergedLevel2 = (Map<String, Object>) mergedLevel1.get("level2");
    assertEquals(2, mergedLevel2.size());
    assertEquals("value1", mergedLevel2.get("deep"));
    assertEquals("value2", mergedLevel2.get("deep2"));
  }
}
