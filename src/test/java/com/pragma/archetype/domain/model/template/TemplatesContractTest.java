package com.pragma.archetype.domain.model.template;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import org.junit.jupiter.api.Test;

class TemplatesContractTest {

  @Test
  void shouldReadTheMajorOfThePluginVersion() {
    assertEquals(OptionalInt.of(1), TemplatesContract.majorOf("1.4.0"));
    assertEquals(OptionalInt.of(2), TemplatesContract.majorOf("2.0.0-SNAPSHOT"));
    assertEquals(OptionalInt.of(10), TemplatesContract.majorOf("10.1"));
  }

  @Test
  void shouldHaveNoMajorForLocalBuilds() {
    assertTrue(TemplatesContract.majorOf(null).isEmpty());
    assertTrue(TemplatesContract.majorOf("0.0.0-unknown").isEmpty());
    assertTrue(TemplatesContract.majorOf("dev").isEmpty());
    assertTrue(TemplatesContract.majorOf("99999999999.0.0").isEmpty());
  }

  @Test
  void shouldPickTheNewestReleaseTagOfTheMajor() {
    var tags = List.of("v1.2.0", "v1.10.0", "v1.9.3", "v2.0.0", "v1.11.0-rc1", "phase-1-complete",
        "v1.99999999999.0");

    assertEquals(Optional.of("v1.10.0"), TemplatesContract.latestTag(1, tags));
    assertEquals(Optional.of("v2.0.0"), TemplatesContract.latestTag(2, tags));
    assertEquals(Optional.empty(), TemplatesContract.latestTag(3, tags));
    assertEquals(Optional.empty(), TemplatesContract.latestTag(1, List.of("v1.99999999999.0")));
  }

  @Test
  void shouldAcceptTemplatesOfTheSupportedSchema() {
    assertDoesNotThrow(() -> TemplatesContract.requireCompatible(TemplatesContract.SCHEMA_VERSION, "local"));
  }

  @Test
  void shouldAskToUpdateThePluginForNewerTemplates() {
    var error = assertThrows(TemplatesContract.IncompatibleTemplatesException.class,
        () -> TemplatesContract.requireCompatible(TemplatesContract.SCHEMA_VERSION + 1, "repo@v1"));

    assertTrue(error.getMessage().contains("repo@v1"));
    assertTrue(error.getMessage().contains("Update the plugin"));
  }

  @Test
  void shouldAskForNewerTemplatesWhenTheyAreOlder() {
    var error = assertThrows(TemplatesContract.IncompatibleTemplatesException.class,
        () -> TemplatesContract.requireCompatible(0, "repo@old"));

    assertTrue(error.getMessage().contains("newer templates"));
  }
}
