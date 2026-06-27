package com.pragma.archetype.domain.model.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.pragma.archetype.test.StructureMetadataTestHelper;

class StructureMetadataTest {

        @Test
        void shouldGetAdapterBaseDirectory() {
                var metadata = StructureMetadataTestHelper.createFull(
                                "hexagonal-single",
                                "src/main/resources",
                                Map.of(
                                                "driven", "infrastructure/driven-adapters/{name}",
                                                "driving", "infrastructure/entry-points/{name}"),
                                null, null, null, null, null, null, null, null);

                assertEquals("infrastructure/driven-adapters", metadata.getAdapterBaseDirectory("driven"));
                assertEquals("infrastructure/entry-points", metadata.getAdapterBaseDirectory("driving"));
        }

        @Test
        void shouldConvertPathToPackageName() {
                var metadata = StructureMetadataTestHelper.createMinimal(
                                "hexagonal-single",
                                Map.of("driven", "path/{name}"));

                assertEquals("infrastructure.entrypoints",
                                metadata.pathToPackageName("infrastructure/entry-points"));
                assertEquals("infrastructure.drivenadapters",
                                metadata.pathToPackageName("infrastructure/driven-adapters"));
                assertEquals("domain.model",
                                metadata.pathToPackageName("domain/model"));
        }

        @Test
        void shouldGetAdapterDirName() {
                var metadata = StructureMetadataTestHelper.createMinimal(
                                "hexagonal-single",
                                Map.of(
                                                "driven", "infrastructure/driven-adapters/{name}",
                                                "driving", "infrastructure/entry-points/{name}"));

                assertEquals("driven-adapters", metadata.getAdapterDirName("driven"));
                assertEquals("entry-points", metadata.getAdapterDirName("driving"));
        }

        @Test
        void shouldHandleSimpleAdapterPaths() {
                var metadata = StructureMetadataTestHelper.createMinimal(
                                "onion-single",
                                Map.of(
                                                "driven", "adapters/out/{name}",
                                                "driving", "adapters/in/{name}"));

                assertEquals("out", metadata.getAdapterDirName("driven"));
                assertEquals("in", metadata.getAdapterDirName("driving"));
        }

        @Test
        void shouldCheckIfHasTemplatePaths() {
                var withTemplatePaths = StructureMetadataTestHelper.withTemplatePaths(
                                "hexagonal-single",
                                Map.of("driven", "path/{name}"),
                                TemplatePaths.builder()
                                                .inputAdapter("custom/path")
                                                .build());

                var withoutTemplatePaths = StructureMetadataTestHelper.createMinimal(
                                "hexagonal-single",
                                Map.of("driven", "path/{name}"));

                assertTrue(withTemplatePaths.hasTemplatePaths());
                assertFalse(withoutTemplatePaths.hasTemplatePaths());
        }

        @Test
        void shouldGetTemplatePathsOrDefault() {
                var withTemplatePaths = StructureMetadataTestHelper.withTemplatePaths(
                                "hexagonal-single",
                                Map.of("driven", "path/{name}"),
                                TemplatePaths.builder()
                                                .inputAdapter("custom/path")
                                                .build());

                var withoutTemplatePaths = StructureMetadataTestHelper.createMinimal(
                                "hexagonal-single",
                                Map.of("driven", "path/{name}"));

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

                var metadata = StructureMetadataTestHelper.withModules(
                                "hexagonal-multi",
                                Map.of("driven", "path/{name}"),
                                List.of(domainModule, infraModule));

                assertEquals("domain", metadata.getModule("domain").getName());
                assertEquals("infrastructure", metadata.getModule("infrastructure").getName());
                assertNull(metadata.getModule("nonexistent"));
        }

        @Test
        void shouldCheckIfIsMultiModule() {
                var multiModule = StructureMetadataTestHelper.withModules(
                                "hexagonal-multi",
                                Map.of("driven", "path/{name}"),
                                List.of(
                                                ModuleMetadata.builder().name("domain").path("domain").build(),
                                                ModuleMetadata.builder().name("infrastructure").path("infrastructure")
                                                                .build()));

                var singleModule = StructureMetadataTestHelper.createMinimal(
                                "hexagonal-single",
                                Map.of("driven", "path/{name}"));

                assertTrue(multiModule.isMultiModule());
                assertFalse(singleModule.isMultiModule());
        }

        @Test
        void shouldGetModuleMetadata() {
                var modules = List.of(
                                ModuleMetadata.builder().name("domain").path("domain").build(),
                                ModuleMetadata.builder().name("application").path("application").build());

                var metadata = StructureMetadataTestHelper.withModules(
                                "hexagonal-multi",
                                Map.of("driven", "path/{name}"),
                                modules);

                assertEquals(2, metadata.getModuleMetadata().size());
                assertEquals("domain", metadata.getModuleMetadata().get(0).getName());
        }

        @Test
        void shouldGetModuleMetadataNames() {
                var modules = List.of(
                                ModuleMetadata.builder().name("domain").path("domain").build(),
                                ModuleMetadata.builder().name("application").path("application").build());

                var metadata = StructureMetadataTestHelper.withModules(
                                "hexagonal-multi",
                                Map.of("driven", "path/{name}"),
                                modules);

                var moduleMetadata = metadata.getModuleMetadata();
                assertEquals(2, moduleMetadata.size());
                assertEquals("domain", moduleMetadata.get(0).getName());
                assertEquals("application", moduleMetadata.get(1).getName());
        }
}
