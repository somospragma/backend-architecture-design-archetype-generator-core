package com.pragma.archetype.domain.model.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class ModuleMetadataTest {

  @Test
  void shouldCreateModuleMetadataWithBuilder() {
    var module = ModuleMetadata.builder()
        .name("infrastructure")
        .path("infrastructure")
        .packages(List.of("entry-points/rest", "driven-adapters"))
        .dependencies(List.of("org.springframework.boot:spring-boot-starter-web"))
        .moduleDependencies(List.of("domain", "application"))
        .build();

    assertEquals("infrastructure", module.getName());
    assertEquals("infrastructure", module.getPath());
    assertEquals(2, module.getPackages().size());
    assertEquals(1, module.getDependencies().size());
    assertEquals(2, module.getModuleDependencies().size());
  }

  @Test
  void shouldCheckIfHasPackages() {
    var withPackages = ModuleMetadata.builder()
        .name("domain")
        .path("domain")
        .packages(List.of("model", "port"))
        .build();

    var withoutPackages = ModuleMetadata.builder()
        .name("application")
        .path("application")
        .build();

    assertTrue(withPackages.hasPackages());
    assertFalse(withoutPackages.hasPackages());
  }

  @Test
  void shouldCheckIfHasDependencies() {
    var withDeps = ModuleMetadata.builder()
        .name("infrastructure")
        .path("infrastructure")
        .dependencies(List.of("org.springframework.boot:spring-boot-starter-web"))
        .build();

    var withoutDeps = ModuleMetadata.builder()
        .name("domain")
        .path("domain")
        .build();

    assertTrue(withDeps.hasDependencies());
    assertFalse(withoutDeps.hasDependencies());
  }

  @Test
  void shouldCheckIfHasModuleDependencies() {
    var withModuleDeps = ModuleMetadata.builder()
        .name("application")
        .path("application")
        .moduleDependencies(List.of("domain"))
        .build();

    var withoutModuleDeps = ModuleMetadata.builder()
        .name("domain")
        .path("domain")
        .build();

    assertTrue(withModuleDeps.hasModuleDependencies());
    assertFalse(withoutModuleDeps.hasModuleDependencies());
  }

  @Test
  void shouldUseDefaultEmptyListsWhenNotProvided() {
    var module = ModuleMetadata.builder()
        .name("domain")
        .path("domain")
        .build();

    assertNotNull(module.getPackages());
    assertNotNull(module.getDependencies());
    assertNotNull(module.getModuleDependencies());
    assertTrue(module.getPackages().isEmpty());
    assertTrue(module.getDependencies().isEmpty());
    assertTrue(module.getModuleDependencies().isEmpty());
  }
}
