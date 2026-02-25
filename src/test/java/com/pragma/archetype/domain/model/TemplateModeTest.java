package com.pragma.archetype.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.pragma.archetype.domain.model.config.TemplateMode;

class TemplateModeTest {

  @Test
  void shouldHaveProductionMode() {
    assertNotNull(TemplateMode.PRODUCTION);
    assertEquals("PRODUCTION", TemplateMode.PRODUCTION.name());
  }

  @Test
  void shouldHaveDeveloperMode() {
    assertNotNull(TemplateMode.DEVELOPER);
    assertEquals("DEVELOPER", TemplateMode.DEVELOPER.name());
  }

  @Test
  void shouldParseFromString() {
    assertEquals(TemplateMode.PRODUCTION, TemplateMode.valueOf("PRODUCTION"));
    assertEquals(TemplateMode.DEVELOPER, TemplateMode.valueOf("DEVELOPER"));
  }

  @Test
  void shouldThrowExceptionForInvalidValue() {
    assertThrows(IllegalArgumentException.class, () -> TemplateMode.valueOf("INVALID"));
  }
}
