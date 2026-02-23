package com.pragma.archetype.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ParadigmTest {

  @Test
  void shouldGetValueForReactive() {
    assertEquals("reactive", Paradigm.REACTIVE.getValue());
  }

  @Test
  void shouldGetValueForImperative() {
    assertEquals("imperative", Paradigm.IMPERATIVE.getValue());
  }

  @Test
  void shouldParseFromValueCaseInsensitive() {
    assertEquals(Paradigm.REACTIVE, Paradigm.fromValue("reactive"));
    assertEquals(Paradigm.REACTIVE, Paradigm.fromValue("REACTIVE"));
    assertEquals(Paradigm.REACTIVE, Paradigm.fromValue("Reactive"));
    assertEquals(Paradigm.IMPERATIVE, Paradigm.fromValue("imperative"));
    assertEquals(Paradigm.IMPERATIVE, Paradigm.fromValue("IMPERATIVE"));
  }

  @Test
  void shouldThrowExceptionForInvalidValue() {
    assertThrows(IllegalArgumentException.class, () -> Paradigm.fromValue("invalid"));
    assertThrows(IllegalArgumentException.class, () -> Paradigm.fromValue("functional"));
  }
}
