package com.pragma.archetype.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
  void shouldThrowExceptionForInvalidValue() {
    assertThrows(IllegalArgumentException.class, () -> ArchitectureType.fromValue("invalid"));
  }

  @Test
  void shouldIdentifyMultiModuleArchitectures() {
    assertTrue(ArchitectureType.HEXAGONAL_MULTI.isMultiModule());
    assertTrue(ArchitectureType.HEXAGONAL_MULTI_GRANULAR.isMultiModule());
    assertTrue(ArchitectureType.ONION_MULTI.isMultiModule());
  }

  @Test
  void shouldIdentifySingleModuleArchitectures() {
    assertFalse(ArchitectureType.HEXAGONAL_SINGLE.isMultiModule());
    assertFalse(ArchitectureType.ONION_SINGLE.isMultiModule());
  }
}
