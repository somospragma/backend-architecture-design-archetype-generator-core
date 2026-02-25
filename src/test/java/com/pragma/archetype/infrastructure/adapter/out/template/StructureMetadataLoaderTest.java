package com.pragma.archetype.infrastructure.adapter.out.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.pragma.archetype.domain.model.StructureMetadata;
import com.pragma.archetype.domain.model.project.ArchitectureType;
import com.pragma.archetype.domain.port.out.TemplateRepository.TemplateNotFoundException;

/**
 * Unit tests for StructureMetadataLoader.
 */
class StructureMetadataLoaderTest {

  @Mock
  private TemplateContentProvider contentProvider;

  private StructureMetadataLoader loader;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    loader = new StructureMetadataLoader(contentProvider);
  }

  @Test
  void loadStructureMetadata_withValidYaml_shouldParseSuccessfully() {
    // Given
    ArchitectureType architecture = ArchitectureType.HEXAGONAL_SINGLE;
    String yamlContent = """
        architecture: hexagonal-single
        adapterPaths:
          driven: "infrastructure/adapter/out/{name}"
          driving: "infrastructure/adapter/in/{name}"
        namingConventions:
          suffixes:
            useCase: "UseCase"
            port: "Port"
          prefixes:
            interface: "I"
        layerDependencies:
          domain: []
          application:
            - domain
          infrastructure:
            - domain
            - application
        """;

    when(contentProvider.getTemplateContent("architectures/hexagonal-single/structure.yml"))
        .thenReturn(yamlContent);

    // When
    StructureMetadata metadata = loader.loadStructureMetadata(architecture);

    // Then
    assertNotNull(metadata);
    assertEquals("hexagonal-single", metadata.architectureType());
    assertEquals("infrastructure/adapter/out/{name}", metadata.adapterPaths().get("driven"));
    assertEquals("infrastructure/adapter/in/{name}", metadata.adapterPaths().get("driving"));
    assertTrue(metadata.hasNamingConventions());
    assertEquals("UseCase", metadata.namingConventions().getSuffix("useCase"));
    assertEquals("I", metadata.namingConventions().getPrefix("interface"));
    assertTrue(metadata.hasLayerDependencies());
    assertTrue(metadata.layerDependencies().canDependOn("application", "domain"));
    assertFalse(metadata.layerDependencies().canDependOn("domain", "application"));
  }

  @Test
  void loadStructureMetadata_withMinimalYaml_shouldParseSuccessfully() {
    // Given
    ArchitectureType architecture = ArchitectureType.ONION_SINGLE;
    String yamlContent = """
        architecture: onion-single
        adapterPaths:
          driven: "infrastructure/adapter/out/{name}"
          driving: "infrastructure/adapter/in/{name}"
        """;

    when(contentProvider.getTemplateContent("architectures/onion-single/structure.yml"))
        .thenReturn(yamlContent);

    // When
    StructureMetadata metadata = loader.loadStructureMetadata(architecture);

    // Then
    assertNotNull(metadata);
    assertEquals("onion-single", metadata.architectureType());
    assertEquals(2, metadata.adapterPaths().size());
    assertFalse(metadata.hasNamingConventions());
    assertFalse(metadata.hasLayerDependencies());
  }

  @Test
  void loadStructureMetadata_withEmptyYaml_shouldThrowException() {
    // Given
    ArchitectureType architecture = ArchitectureType.HEXAGONAL_SINGLE;
    when(contentProvider.getTemplateContent("architectures/hexagonal-single/structure.yml"))
        .thenReturn("");

    // When & Then
    assertThrows(TemplateNotFoundException.class,
        () -> loader.loadStructureMetadata(architecture));
  }

  @Test
  void loadStructureMetadata_withMissingArchitectureField_shouldThrowException() {
    // Given
    ArchitectureType architecture = ArchitectureType.HEXAGONAL_SINGLE;
    String yamlContent = """
        adapterPaths:
          driven: "infrastructure/adapter/out/{name}"
        """;

    when(contentProvider.getTemplateContent("architectures/hexagonal-single/structure.yml"))
        .thenReturn(yamlContent);

    // When & Then
    TemplateNotFoundException exception = assertThrows(TemplateNotFoundException.class,
        () -> loader.loadStructureMetadata(architecture));
    assertTrue(exception.getMessage().contains("Required field 'architecture' is missing"));
  }

  @Test
  void loadStructureMetadata_withMissingAdapterPaths_shouldThrowException() {
    // Given
    ArchitectureType architecture = ArchitectureType.HEXAGONAL_SINGLE;
    String yamlContent = """
        architecture: hexagonal-single
        """;

    when(contentProvider.getTemplateContent("architectures/hexagonal-single/structure.yml"))
        .thenReturn(yamlContent);

    // When & Then
    TemplateNotFoundException exception = assertThrows(TemplateNotFoundException.class,
        () -> loader.loadStructureMetadata(architecture));
    assertTrue(exception.getMessage().contains("Required field 'adapterPaths' is missing"));
  }

  @Test
  void loadStructureMetadata_withEmptyAdapterPaths_shouldThrowException() {
    // Given
    ArchitectureType architecture = ArchitectureType.HEXAGONAL_SINGLE;
    String yamlContent = """
        architecture: hexagonal-single
        adapterPaths: {}
        """;

    when(contentProvider.getTemplateContent("architectures/hexagonal-single/structure.yml"))
        .thenReturn(yamlContent);

    // When & Then
    TemplateNotFoundException exception = assertThrows(TemplateNotFoundException.class,
        () -> loader.loadStructureMetadata(architecture));
    assertTrue(exception.getMessage().contains("Field 'adapterPaths' cannot be empty"));
  }

  @Test
  void loadStructureMetadata_withInvalidAdapterPaths_shouldThrowException() {
    // Given
    ArchitectureType architecture = ArchitectureType.HEXAGONAL_SINGLE;
    String yamlContent = """
        architecture: hexagonal-single
        adapterPaths:
          invalid: "path"
        """;

    when(contentProvider.getTemplateContent("architectures/hexagonal-single/structure.yml"))
        .thenReturn(yamlContent);

    // When & Then
    TemplateNotFoundException exception = assertThrows(TemplateNotFoundException.class,
        () -> loader.loadStructureMetadata(architecture));
    assertTrue(exception.getMessage().contains("Invalid structure metadata"));
  }

  @Test
  void loadStructureMetadata_withInvalidNamingConventions_shouldThrowException() {
    // Given
    ArchitectureType architecture = ArchitectureType.HEXAGONAL_SINGLE;
    String yamlContent = """
        architecture: hexagonal-single
        adapterPaths:
          driven: "path/{name}"
        namingConventions: "invalid"
        """;

    when(contentProvider.getTemplateContent("architectures/hexagonal-single/structure.yml"))
        .thenReturn(yamlContent);

    // When & Then
    TemplateNotFoundException exception = assertThrows(TemplateNotFoundException.class,
        () -> loader.loadStructureMetadata(architecture));
    assertTrue(exception.getMessage().contains("Field 'namingConventions' must be a map"));
  }

  @Test
  void loadStructureMetadata_withInvalidLayerDependencies_shouldThrowException() {
    // Given
    ArchitectureType architecture = ArchitectureType.HEXAGONAL_SINGLE;
    String yamlContent = """
        architecture: hexagonal-single
        adapterPaths:
          driven: "path/{name}"
        layerDependencies: "invalid"
        """;

    when(contentProvider.getTemplateContent("architectures/hexagonal-single/structure.yml"))
        .thenReturn(yamlContent);

    // When & Then
    TemplateNotFoundException exception = assertThrows(TemplateNotFoundException.class,
        () -> loader.loadStructureMetadata(architecture));
    assertTrue(exception.getMessage().contains("Field 'layerDependencies' must be a map"));
  }

  @Test
  void loadStructureMetadata_whenContentProviderThrowsException_shouldWrapException() {
    // Given
    ArchitectureType architecture = ArchitectureType.HEXAGONAL_SINGLE;
    when(contentProvider.getTemplateContent("architectures/hexagonal-single/structure.yml"))
        .thenThrow(new RuntimeException("Network error"));

    // When & Then
    TemplateNotFoundException exception = assertThrows(TemplateNotFoundException.class,
        () -> loader.loadStructureMetadata(architecture));
    assertTrue(exception.getMessage().contains("Failed to load structure metadata"));
    assertTrue(exception.getMessage().contains("hexagonal-single"));
  }

  @Test
  void loadStructureMetadata_withOnionArchitecture_shouldUseCorrectPath() {
    // Given
    ArchitectureType architecture = ArchitectureType.ONION_SINGLE;
    String yamlContent = """
        architecture: onion-single
        adapterPaths:
          driven: "infrastructure/adapter/out/{name}"
          driving: "infrastructure/adapter/in/{name}"
        """;

    when(contentProvider.getTemplateContent("architectures/onion-single/structure.yml"))
        .thenReturn(yamlContent);

    // When
    StructureMetadata metadata = loader.loadStructureMetadata(architecture);

    // Then
    assertNotNull(metadata);
    verify(contentProvider).getTemplateContent("architectures/onion-single/structure.yml");
  }

  @Test
  void loadStructureMetadata_withNamingConventionsOnly_shouldParseSuccessfully() {
    // Given
    ArchitectureType architecture = ArchitectureType.HEXAGONAL_SINGLE;
    String yamlContent = """
        architecture: hexagonal-single
        adapterPaths:
          driven: "path/{name}"
        namingConventions:
          suffixes:
            entity: "Entity"
          prefixes: {}
        """;

    when(contentProvider.getTemplateContent("architectures/hexagonal-single/structure.yml"))
        .thenReturn(yamlContent);

    // When
    StructureMetadata metadata = loader.loadStructureMetadata(architecture);

    // Then
    assertNotNull(metadata);
    assertTrue(metadata.hasNamingConventions());
    assertEquals("Entity", metadata.namingConventions().getSuffix("entity"));
    assertEquals("", metadata.namingConventions().getPrefix("entity"));
  }

  @Test
  void loadStructureMetadata_withLayerDependenciesOnly_shouldParseSuccessfully() {
    // Given
    ArchitectureType architecture = ArchitectureType.HEXAGONAL_SINGLE;
    String yamlContent = """
        architecture: hexagonal-single
        adapterPaths:
          driven: "path/{name}"
        layerDependencies:
          domain: []
          application:
            - domain
        """;

    when(contentProvider.getTemplateContent("architectures/hexagonal-single/structure.yml"))
        .thenReturn(yamlContent);

    // When
    StructureMetadata metadata = loader.loadStructureMetadata(architecture);

    // Then
    assertNotNull(metadata);
    assertTrue(metadata.hasLayerDependencies());
    assertEquals(0, metadata.layerDependencies().getAllowedDependenciesFor("domain").size());
    assertEquals(1, metadata.layerDependencies().getAllowedDependenciesFor("application").size());
  }
}
