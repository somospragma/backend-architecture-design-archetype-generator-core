package com.pragma.archetype.domain.model.template;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TemplatesContractTest {

  @Test
  void shouldUseTheBranchOfThePluginMajorVersion() {
    assertEquals("v1", TemplatesContract.branchFor("1.4.0"));
    assertEquals("v2", TemplatesContract.branchFor("2.0.0-SNAPSHOT"));
    assertEquals("v10", TemplatesContract.branchFor("10.1"));
  }

  @Test
  void shouldFallBackToMainWhenTheVersionHasNoNumericMajor() {
    assertEquals("develop", TemplatesContract.branchFor(null));
    assertEquals("develop", TemplatesContract.branchFor("0.0.0-unknown".replace("0.0.0", "dev")));
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
