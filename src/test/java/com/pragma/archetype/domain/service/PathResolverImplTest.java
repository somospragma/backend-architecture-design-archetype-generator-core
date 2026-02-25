package com.pragma.archetype.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pragma.archetype.domain.model.project.ArchitectureType;
import com.pragma.archetype.domain.model.structure.LayerDependencies;
import com.pragma.archetype.domain.model.structure.StructureMetadata;
import com.pragma.archetype.domain.model.validation.ValidationResult;
import com.pragma.archetype.domain.port.out.PathResolver;
import com.pragma.archetype.domain.port.out.TemplateRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("PathResolverImpl Tests")
class PathResolverImplTest {

  @Mock
  private TemplateRepository templateRepository;

  private PathResolver pathResolver;

  @BeforeEach
  void setUp() {
    pathResolver = new PathResolverImpl(templateRepository);
  }

  @Test
  @DisplayName("Should resolve adapter path for Hexagonal architecture")
  void shouldResolveAdapterPathForHexagonalArchitecture() {
    // Given
    ArchitectureType architecture = ArchitectureType.HEXAGONAL_SINGLE;
    String adapterType = "driven";
    String name = "mongodb";
    Map<String, String> context = Map.of("basePackage", "com.example.service");

    StructureMetadata metadata = new StructureMetadata(
        "hexagonal-single",
        Map.of(
            "driven", "src/main/java/{basePackage}/infrastructure/adapter/out/{name}",
            "driving", "src/main/java/{basePackage}/infrastructure/adapter/in/{name}"),
        null,
        null,
        null, null);

    when(templateRepository.loadStructureMetadata(architecture)).thenReturn(metadata);

    // When
    Path result = pathResolver.resolveAdapterPath(architecture, adapterType, name, context);

    // Then
    assertEquals(
        Paths.get("src/main/java/com.example.service/infrastructure/adapter/out/mongodb"),
        result);
    verify(templateRepository).loadStructureMetadata(architecture);
  }

  @Test
  @DisplayName("Should resolve adapter path for Onion architecture")
  void shouldResolveAdapterPathForOnionArchitecture() {
    // Given
    ArchitectureType architecture = ArchitectureType.ONION_SINGLE;
    String adapterType = "driving";
    String name = "rest-controller";
    Map<String, String> context = Map.of("basePackage", "com.example.api");

    StructureMetadata metadata = new StructureMetadata(
        "onion-single",
        Map.of(
            "driven", "infrastructure/adapter/out/{name}",
            "driving", "infrastructure/adapter/in/{name}"),
        null,
        null,
        null, null);

    when(templateRepository.loadStructureMetadata(architecture)).thenReturn(metadata);

    // When
    Path result = pathResolver.resolveAdapterPath(architecture, adapterType, name, context);

    // Then
    assertEquals(
        Paths.get("infrastructure/adapter/in/rest-controller"),
        result);
  }

  @Test
  @DisplayName("Should substitute multiple placeholders in path template")
  void shouldSubstituteMultiplePlaceholdersInPathTemplate() {
    // Given
    String template = "src/main/java/{basePackage}/{module}/adapter/{type}/{name}";
    Map<String, String> context = Map.of(
        "basePackage", "com.example",
        "module", "infrastructure",
        "type", "driven",
        "name", "mongodb");

    // When
    String result = pathResolver.substitutePlaceholders(template, context);

    // Then
    assertEquals("src/main/java/com.example/infrastructure/adapter/driven/mongodb", result);
  }

  @Test
  @DisplayName("Should handle template with no placeholders")
  void shouldHandleTemplateWithNoPlaceholders() {
    // Given
    String template = "src/main/java/infrastructure/adapter";
    Map<String, String> context = Map.of("name", "test");

    // When
    String result = pathResolver.substitutePlaceholders(template, context);

    // Then
    assertEquals("src/main/java/infrastructure/adapter", result);
  }

  @Test
  @DisplayName("Should handle missing placeholder values gracefully")
  void shouldHandleMissingPlaceholderValuesGracefully() {
    // Given
    String template = "src/{basePackage}/{missing}/{name}";
    Map<String, String> context = Map.of(
        "basePackage", "com.example",
        "name", "test");

    // When
    String result = pathResolver.substitutePlaceholders(template, context);

    // Then
    assertEquals("src/com.example/{missing}/test", result);
  }

  @Test
  @DisplayName("Should throw exception when adapter type not defined in structure")
  void shouldThrowExceptionWhenAdapterTypeNotDefined() {
    // Given
    ArchitectureType architecture = ArchitectureType.HEXAGONAL_SINGLE;
    String adapterType = "unknown";
    String name = "test";
    Map<String, String> context = Map.of();

    StructureMetadata metadata = new StructureMetadata(
        "hexagonal-single",
        Map.of("driven", "path/to/{name}"),
        null,
        null,
        null, null);

    when(templateRepository.loadStructureMetadata(architecture)).thenReturn(metadata);

    // When & Then
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> pathResolver.resolveAdapterPath(architecture, adapterType, name, context));

    assertTrue(exception.getMessage().contains("No adapter path defined for type"));
    assertTrue(exception.getMessage().contains("unknown"));
  }

  @Test
  @DisplayName("Should validate path successfully when no layer dependencies defined")
  void shouldValidatePathSuccessfullyWhenNoLayerDependenciesDefined() {
    // Given
    ArchitectureType architecture = ArchitectureType.HEXAGONAL_SINGLE;
    Path path = Paths.get("src/main/java/infrastructure/adapter/out/mongodb");

    StructureMetadata metadata = new StructureMetadata(
        "hexagonal-single",
        Map.of("driven", "path/{name}"),
        null,
        null,
        null, null);

    when(templateRepository.loadStructureMetadata(architecture)).thenReturn(metadata);

    // When
    ValidationResult result = pathResolver.validatePath(path, architecture);

    // Then
    assertTrue(result.valid());
    assertTrue(result.errors().isEmpty());
  }

  @Test
  @DisplayName("Should validate path with layer dependencies")
  void shouldValidatePathWithLayerDependencies() {
    // Given
    ArchitectureType architecture = ArchitectureType.ONION_SINGLE;
    Path path = Paths.get("infrastructure/adapter/out/mongodb");

    LayerDependencies layerDeps = new LayerDependencies(
        Map.of(
            "domain", List.of(),
            "application", List.of("domain"),
            "infrastructure", List.of("domain", "application")));

    StructureMetadata metadata = new StructureMetadata(
        "onion-single",
        Map.of("driven", "infrastructure/adapter/out/{name}"),
        null,
        layerDeps,
        null, null);

    when(templateRepository.loadStructureMetadata(architecture)).thenReturn(metadata);

    // When
    ValidationResult result = pathResolver.validatePath(path, architecture);

    // Then
    assertTrue(result.valid());
  }

  @Test
  @DisplayName("Should fail validation when layer not found in path")
  void shouldFailValidationWhenLayerNotFoundInPath() {
    // Given
    ArchitectureType architecture = ArchitectureType.HEXAGONAL_SINGLE;
    Path path = Paths.get("src/main/java/unknown/path");

    LayerDependencies layerDeps = new LayerDependencies(
        Map.of("domain", List.of()));

    StructureMetadata metadata = new StructureMetadata(
        "hexagonal-single",
        Map.of("driven", "path/{name}"),
        null,
        layerDeps,
        null, null);

    when(templateRepository.loadStructureMetadata(architecture)).thenReturn(metadata);

    // When
    ValidationResult result = pathResolver.validatePath(path, architecture);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream()
        .anyMatch(error -> error.contains("Could not determine layer")));
  }

  @Test
  @DisplayName("Should fail validation when layer not defined in architecture")
  void shouldFailValidationWhenLayerNotDefinedInArchitecture() {
    // Given
    ArchitectureType architecture = ArchitectureType.HEXAGONAL_SINGLE;
    Path path = Paths.get("src/main/java/infrastructure/adapter");

    LayerDependencies layerDeps = new LayerDependencies(
        Map.of("domain", List.of()));

    StructureMetadata metadata = new StructureMetadata(
        "hexagonal-single",
        Map.of("driven", "path/{name}"),
        null,
        layerDeps,
        null, null);

    when(templateRepository.loadStructureMetadata(architecture)).thenReturn(metadata);

    // When
    ValidationResult result = pathResolver.validatePath(path, architecture);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream()
        .anyMatch(error -> error.contains("not defined in architecture")));
  }

  @Test
  @DisplayName("Should throw exception when architecture is null")
  void shouldThrowExceptionWhenArchitectureIsNull() {
    // When & Then
    assertThrows(
        NullPointerException.class,
        () -> pathResolver.resolveAdapterPath(null, "driven", "test", Map.of()));
  }

  @Test
  @DisplayName("Should throw exception when adapter type is null")
  void shouldThrowExceptionWhenAdapterTypeIsNull() {
    // When & Then
    assertThrows(
        NullPointerException.class,
        () -> pathResolver.resolveAdapterPath(
            ArchitectureType.HEXAGONAL_SINGLE, null, "test", Map.of()));
  }

  @Test
  @DisplayName("Should throw exception when name is null")
  void shouldThrowExceptionWhenNameIsNull() {
    // When & Then
    assertThrows(
        NullPointerException.class,
        () -> pathResolver.resolveAdapterPath(
            ArchitectureType.HEXAGONAL_SINGLE, "driven", null, Map.of()));
  }

  @Test
  @DisplayName("Should throw exception when context is null")
  void shouldThrowExceptionWhenContextIsNull() {
    // When & Then
    assertThrows(
        NullPointerException.class,
        () -> pathResolver.resolveAdapterPath(
            ArchitectureType.HEXAGONAL_SINGLE, "driven", "test", null));
  }

  @Test
  @DisplayName("Should resolve path with module placeholder for multi-module architecture")
  void shouldResolvePathWithModulePlaceholderForMultiModuleArchitecture() {
    // Given
    ArchitectureType architecture = ArchitectureType.HEXAGONAL_MULTI;
    String adapterType = "driven";
    String name = "mongodb";
    Map<String, String> context = Map.of(
        "basePackage", "com.example",
        "module", "infrastructure");

    StructureMetadata metadata = new StructureMetadata(
        "hexagonal-multi",
        Map.of("driven", "{module}/src/main/java/{basePackage}/adapter/{name}"),
        null,
        null,
        null, null);

    when(templateRepository.loadStructureMetadata(architecture)).thenReturn(metadata);

    // When
    Path result = pathResolver.resolveAdapterPath(architecture, adapterType, name, context);

    // Then
    assertEquals(
        Paths.get("infrastructure/src/main/java/com.example/adapter/mongodb"),
        result);
  }

  @Test
  @DisplayName("Should extract domain layer from path")
  void shouldExtractDomainLayerFromPath() {
    // Given
    ArchitectureType architecture = ArchitectureType.HEXAGONAL_SINGLE;
    Path path = Paths.get("src/main/java/domain/model");

    LayerDependencies layerDeps = new LayerDependencies(
        Map.of("domain", List.of()));

    StructureMetadata metadata = new StructureMetadata(
        "hexagonal-single",
        Map.of("driven", "path/{name}"),
        null,
        layerDeps,
        null, null);

    when(templateRepository.loadStructureMetadata(architecture)).thenReturn(metadata);

    // When
    ValidationResult result = pathResolver.validatePath(path, architecture);

    // Then
    assertTrue(result.valid());
  }

  @Test
  @DisplayName("Should extract application layer from path")
  void shouldExtractApplicationLayerFromPath() {
    // Given
    ArchitectureType architecture = ArchitectureType.HEXAGONAL_SINGLE;
    Path path = Paths.get("src/main/java/application/usecase");

    LayerDependencies layerDeps = new LayerDependencies(
        Map.of("application", List.of("domain")));

    StructureMetadata metadata = new StructureMetadata(
        "hexagonal-single",
        Map.of("driven", "path/{name}"),
        null,
        layerDeps,
        null, null);

    when(templateRepository.loadStructureMetadata(architecture)).thenReturn(metadata);

    // When
    ValidationResult result = pathResolver.validatePath(path, architecture);

    // Then
    assertTrue(result.valid());
  }

  @Test
  @DisplayName("Should extract core layer from path")
  void shouldExtractCoreLayerFromPath() {
    // Given
    ArchitectureType architecture = ArchitectureType.ONION_SINGLE;
    Path path = Paths.get("core/domain/model");

    LayerDependencies layerDeps = new LayerDependencies(
        Map.of("core", List.of()));

    StructureMetadata metadata = new StructureMetadata(
        "onion-single",
        Map.of("driven", "path/{name}"),
        null,
        layerDeps,
        null, null);

    when(templateRepository.loadStructureMetadata(architecture)).thenReturn(metadata);

    // When
    ValidationResult result = pathResolver.validatePath(path, architecture);

    // Then
    assertTrue(result.valid());
  }
}
