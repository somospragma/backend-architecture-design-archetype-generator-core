package com.pragma.archetype.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class FrameworkTest {

  @Test
  void shouldGetValueForSpring() {
    assertEquals("spring", Framework.SPRING.getValue());
  }

  @Test
  void shouldGetValueForQuarkus() {
    assertEquals("quarkus", Framework.QUARKUS.getValue());
  }

  @Test
  void shouldParseFromValueCaseInsensitive() {
    assertEquals(Framework.SPRING, Framework.fromValue("spring"));
    assertEquals(Framework.SPRING, Framework.fromValue("SPRING"));
    assertEquals(Framework.SPRING, Framework.fromValue("Spring"));
    assertEquals(Framework.QUARKUS, Framework.fromValue("quarkus"));
    assertEquals(Framework.QUARKUS, Framework.fromValue("QUARKUS"));
  }

  @Test
  void shouldThrowExceptionForInvalidValue() {
    assertThrows(IllegalArgumentException.class, () -> Framework.fromValue("invalid"));
    assertThrows(IllegalArgumentException.class, () -> Framework.fromValue("micronaut"));
  }
}
