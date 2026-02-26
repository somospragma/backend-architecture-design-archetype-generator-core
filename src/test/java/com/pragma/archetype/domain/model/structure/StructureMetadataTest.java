package com.pragma.archetype.domain.model.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class StructureMetadataTest {

  @Test
  void shouldGetAdapterBaseDirectory() {
    var metadata = new StructureMetadata(
        "hexagonal-single",
        Map.of(
            "driven", "infrastructure/driven-adapters/{name}",
            "driving", "infrastructure/entry-points/{name}"),
        null,
        null,
        null,
        null,
        null);

    assertEquals("infrastructure/driven-adapters", metadata.getAdapterBaseDirectory("driven"));
    assertEquals("infrastructure/entry-points", metadata.getAdapterBaseDirectory("driving"));
  }

  @Test
  void shouldConvertPathToPackageName() {
    var metadata = new StructureMetadata(
        "hexagonal-single",
        Map.of(),
        null,
        null,
        null,
        null,
        null);

    assertEquals("infrastructure.entrypoints",
        metadata.pathToPackageName("infrastructure/entry-points"));
    assertEquals("infrastructure.drivenadapters",
        metadata.pathToPackageName("infrastructure/driven-adapters"));
    assertEquals("domain.model",
        metadata.pathToPackageName("domain/model"));
  }

  @Test
  void shouldGetAdapterDirName() {
    var metadata = new StructureMetadata(
        "hexagonal-single",
        Map.of(
            "driven", "infrastructure/driven-adapters/{name}",
            "driving", "infrastructure/entry-points/{name}"),
        null,
        null,
        null,
        null,
        null);

    assertEquals("driven-adapters", metadata.getAdapterDirName("driven"));
    assertEquals("entry-points", metadata.getAdapterDirName("driving"));
  }

  @Test
  void shouldHandleSimpleAdapterPaths() {
    var metadata = new StructureMetadata(
        "onion-single",
        Map.of(
            "driven", "adapters/out/{name}",
            "driving", "adapters/in/{name}"),
        null,
        null,
        null,
        null,
        null);

    assertEquals("out", metadata.getAdapterDirName("driven"));
    assertEquals("in", metadata.getAdapterDirName("driving"));
  }

  @Test
  void shouldCheckIfHasTemplatePaths() {
    var withTemplatePaths = new StructureMetadata(
        "hexagonal-single",
        Map.of(),
        TemplatePaths.builder().build(),
        null,
        null,
        null,
        null);

    var withoutTemplatePaths = new StructureMetadata(
        "hexagonal-single",
        Map.of(),
        null,
        null,
        null,
        null,
        null);

    assertTrue(withTemplatePaths.hasTemplatePaths());
    assertFalse(withoutTemplatePaths.hasTemplatePaths());
  }

  @Test
  void shouldGetTemplatePathsOrDefault() {
    var withTemplatePaths = new StructureMetadata(
        "hexagonal-single",
        Map.of(),
        TemplatePaths.builder()
            .inputAdapter("custom/path")
            .build(),
        null,
        null,
        null,
        null);

    var withoutTemplatePaths = new StructureMetadata(
        "hexagonal-single",
        Map.of(),
        null,
        null,
        null,
        null,
        null);

    assertEquals("custom/path", withTemplatePaths.getTemplatePathsOrDefault().getInputAdapter());
    assertNotNull(withoutTemplatePaths.getTemplatePathsOrDefault());
  }

  @Test
  void shouldGetModuleByName() {
    var domainModule = ModuleMetadata.builder()
        .name("domain")
        .path("domain")
        .build();

    var infraModule = ModuleMetadata.builder()
        .name("infrastructure")
        .path("infrastructure")
        .build();

    var metadata = new StructureMetadata(
        "hexagonal-multi",
        Map.of(),
        null,
        null,
        null,
        null,
        List.of(domainModule, infraModule));

    assertEquals("domain", metadata.getModule("domain").getName());
    assertEquals("infrastructure", metadata.getModule("infrastructure").getName());
    assertNull(metadata.getModule("nonexistent"));
  }

  @Test
  void shouldCheckIfIsMultiModule() {
    var multiModule = new StructureMetadata(
        "hexagonal-multi",
        Map.of(),
        null,
        null,
        null,
        null,
        List.of(
            ModuleMetadata.builder().name("domain").path("domain").build(),
            ModuleMetadata.builder().name("infrastructure").path("infrastructure").build()));

    var singleModule = new StructureMetadata(
        "hexagonal-single",
        Map.of(),
        null,
        null,
        null,
        null,
        null);

    assertTrue(multiModule.isMultiModule());
    assertFalse(singleModule.isMultiModule());
  }

  @Test
  void shouldGetModuleMetadata() {
    var modules = List.of(
        ModuleMetadata.builder().name("domain").path("domain").build(),
        ModuleMetadata.builder().name("application").path("application").build());

    var metadata = new StructureMetadata(
        "hexagonal-multi",
        Map.of(),
        null,
        null,
        null,
        null,
        modules);

    assertEquals(2, metadata.getModuleMetadata().size());
    assertEquals("domain", metadata.getModuleMetadata().get(0).getName());
  }

  @Test
  @SuppressWarnings("deprecation")
  void shouldGetModulesForBackwardCompatibility() {
    var modules = List.of(
        ModuleMetadata.builder().name("domain").path("domain").build(),
        ModuleMetadata.builder().name("application").path("application").build());

    var metadata = new StructureMetadata(
        "hexagonal-multi",
        Map.of(),
        null,
        null,
        null,
        null,
        modules);

    var moduleNames = metadata.getModules();
    assertEquals(2, moduleNames.size());
    assertTrue(moduleNames.contains("domain"));
    assertTrue(moduleNames.contains("application"));
  }
}
