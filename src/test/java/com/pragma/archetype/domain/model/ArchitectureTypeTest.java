package com.pragma.archetype.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.pragma.archetype.domain.model.project.ArchitectureType;

class ArchitectureTypeTest {

  @Test
  void shouldGetValueForHexagonalSingle() {
    assertEquals("hexagonal-single", ArchitectureType.HEXAGONAL_SINGLE.getValue());
  }

  @Test
  void shouldGetValueForHexagonalMulti() {
    assertEquals("hexagonal-multi", ArchitectureType.HEXAGONAL_MULTI.getValue());
  }

  @Test
  void shouldGetValueForHexagonalMultiGranular() {
    assertEquals("hexagonal-multi-granular", ArchitectureType.HEXAGONAL_MULTI_GRANULAR.getValue());
  }

  @Test
  void shouldGetValueForOnionSingle() {
    assertEquals("onion-single", ArchitectureType.ONION_SINGLE.getValue());
  }

  @Test
  void shouldGetValueForOnionMulti() {
    assertEquals("onion-multi", ArchitectureType.ONION_MULTI.getValue());
  }

  @Test
  void shouldParseFromValueCaseInsensitive() {
    assertEquals(ArchitectureType.HEXAGONAL_SINGLE, ArchitectureType.fromValue("hexagonal-single"));
    assertEquals(ArchitectureType.HEXAGONAL_SINGLE, ArchitectureType.fromValue("HEXAGONAL-SINGLE"));
    assertEquals(ArchitectureType.HEXAGONAL_SINGLE, ArchitectureType.fromValue("Hexagonal-Single"));
  }

  @Test
  void shouldRejectNamesThatAreNotDirectoryNames() {
    assertThrows(IllegalArgumentException.class, () -> ArchitectureType.fromValue(null));
    assertThrows(IllegalArgumentException.class, () -> ArchitectureType.fromValue(" "));
    assertThrows(IllegalArgumentException.class, () -> ArchitectureType.fromValue("hexagonal single"));
    assertThrows(IllegalArgumentException.class, () -> ArchitectureType.fromValue("../hexagonal"));
  }

  @Test
  void shouldAcceptArchitecturesDeclaredOnlyByTemplates() {
    ArchitectureType clean = ArchitectureType.fromValue("clean-multi");

    assertEquals("clean-multi", clean.getValue());
    assertEquals("CLEAN_MULTI", clean.name());
    assertEquals(clean, ArchitectureType.fromValue("CLEAN_MULTI"));
  }

  @Test
  void shouldReturnSharedConstantsForBuiltInNames() {
    assertSame(ArchitectureType.HEXAGONAL_MULTI, ArchitectureType.fromValue("hexagonal-multi"));
    assertSame(ArchitectureType.HEXAGONAL_MULTI, ArchitectureType.valueOf("HEXAGONAL_MULTI"));
  }

  @Test
  void shouldKeepEnumStyleNameForBuiltIns() {
    assertEquals("HEXAGONAL_MULTI_GRANULAR", ArchitectureType.HEXAGONAL_MULTI_GRANULAR.name());
    assertEquals(5, ArchitectureType.values().length);
  }
}
