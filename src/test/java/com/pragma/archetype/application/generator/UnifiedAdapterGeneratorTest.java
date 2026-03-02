package com.pragma.archetype.application.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pragma.archetype.application.port.out.AdapterDiscoveryPort;
import com.pragma.archetype.domain.model.adapter.AdapterDirection;
import com.pragma.archetype.domain.model.adapter.AdapterMetadata;
import com.pragma.archetype.domain.model.adapter.TemplateFile;
import com.pragma.archetype.domain.model.config.ProjectConfig;
import com.pragma.archetype.domain.model.structure.StructureMetadata;
import com.pragma.archetype.domain.port.out.PathResolver;
import com.pragma.archetype.domain.port.out.TemplateRepository;

@ExtendWith(MockitoExtension.class)
class UnifiedAdapterGeneratorTest {

  @Mock
  private TemplateRepository templateRepository;

  @Mock
  private PathResolver pathResolver;

  @Mock
  private AdapterDiscoveryPort adapterDiscovery;

  @Mock
  private ProjectGenerator projectGenerator;

  @Mock
  private ProjectConfig projectConfig;

  @Mock
  private StructureMetadata structureMetadata;

  @Mock
  private AdapterMetadata adapterMetadata;

  private UnifiedAdapterGenerator generator;

  @BeforeEach
  void setUp() {
    generator = new UnifiedAdapterGenerator(
        templateRepository,
        pathResolver,
        adapterDiscovery,
        projectGenerator);

    // Setup common mocks
    when(adapterMetadata.getName()).thenReturn("rest");
    when(adapterMetadata.getDirection()).thenReturn(AdapterDirection.INPUT);
    when(adapterMetadata.getSuffix()).thenReturn("Controller");
    when(adapterMetadata.isNeedsMapper()).thenReturn(false);
    when(adapterMetadata.hasAdditionalResources()).thenReturn(false);
    when(adapterMetadata.getDiscoveredTemplates()).thenReturn(List.of(
        TemplateFile.builder()
            .name("RestController.java.ftl")
            .type("main")
            .destination("{name}Controller.java")
            .build()));

    when(structureMetadata.getResourcesPath()).thenReturn("src/main/resources");
    when(structureMetadata.getAdapterBaseDirectory(anyString())).thenReturn("infrastructure/entry-points");

    when(projectConfig.adaptersAsModules()).thenReturn(false);
    when(projectConfig.basePackage()).thenReturn("com.test");
    when(projectConfig.name()).thenReturn("test-project");

    when(templateRepository.processTemplate(anyString(), anyMap())).thenReturn("public class TestController {}");
    when(pathResolver.resolveAdapterPath(any(), anyString(), anyString(), anyMap()))
        .thenReturn(Path.of("infrastructure/adapter/in/rest"));
  }

  @Test
  void shouldGenerateInputAdapterInPlace() {
    // Given
    var config = createTestConfig("UserController", "rest", AdapterDirection.INPUT);
    when(adapterDiscovery.adapterExists(anyString(), any(), anyString(), anyString())).thenReturn(true);
    when(adapterDiscovery.findAdapter(anyString(), any(), anyString(), anyString()))
        .thenReturn(Optional.of(adapterMetadata));

    // When
    var result = generator.generate(
        Path.of("/test"),
        config,
        AdapterDirection.INPUT,
        structureMetadata,
        projectConfig);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
  }

  @Test
  void shouldGenerateOutputAdapterInPlace() {
    // Given
    var config = createTestConfig("UserRepository", "postgresql", AdapterDirection.OUTPUT);
    when(adapterMetadata.getDirection()).thenReturn(AdapterDirection.OUTPUT);
    when(adapterMetadata.getSuffix()).thenReturn("Repository");
    when(adapterDiscovery.adapterExists(anyString(), any(), anyString(), anyString())).thenReturn(true);
    when(adapterDiscovery.findAdapter(anyString(), any(), anyString(), anyString()))
        .thenReturn(Optional.of(adapterMetadata));

    // When
    var result = generator.generate(
        Path.of("/test"),
        config,
        AdapterDirection.OUTPUT,
        structureMetadata,
        projectConfig);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
  }

  @Test
  void shouldThrowExceptionWhenAdapterNotFound() {
    // Given
    var config = createTestConfig("UserController", "unknown", AdapterDirection.INPUT);
    when(adapterDiscovery.adapterExists(anyString(), any(), anyString(), anyString())).thenReturn(false);
    when(adapterDiscovery.listAdapterNames(any(), anyString(), anyString()))
        .thenReturn(List.of("rest", "graphql", "grpc"));

    // When/Then
    assertThrows(IllegalArgumentException.class, () -> {
      generator.generate(
          Path.of("/test"),
          config,
          AdapterDirection.INPUT,
          structureMetadata,
          projectConfig);
    });
  }

  @Test
  void shouldGenerateAdapterWithMapperWhenNeeded() {
    // Given
    var config = createTestConfig("UserRepository", "postgresql", AdapterDirection.OUTPUT);
    when(adapterMetadata.getDirection()).thenReturn(AdapterDirection.OUTPUT);
    when(adapterMetadata.getSuffix()).thenReturn("Repository");
    when(adapterMetadata.isNeedsMapper()).thenReturn(true);
    when(adapterMetadata.getDiscoveredTemplates()).thenReturn(List.of(
        TemplateFile.builder()
            .name("Repository.java.ftl")
            .type("main")
            .destination("{name}Repository.java")
            .build(),
        TemplateFile.builder()
            .name("UserDataEntity.java.ftl")
            .type("entity")
            .destination("{entityName}Data.java")
            .build()));
    when(adapterDiscovery.adapterExists(anyString(), any(), anyString(), anyString())).thenReturn(true);
    when(adapterDiscovery.findAdapter(anyString(), any(), anyString(), anyString()))
        .thenReturn(Optional.of(adapterMetadata));

    // When
    var result = generator.generate(
        Path.of("/test"),
        config,
        AdapterDirection.OUTPUT,
        structureMetadata,
        projectConfig);

    // Then
    assertNotNull(result);
    assertEquals(3, result.size()); // Main + Mapper + Entity
  }

  private UnifiedAdapterGenerator.AdapterConfig createTestConfig(
      String name,
      String type,
      AdapterDirection direction) {
    return new UnifiedAdapterGenerator.AdapterConfig() {
      @Override
      public String getName() {
        return name;
      }

      @Override
      public String getType() {
        return type;
      }

      @Override
      public String getPackageName() {
        return "com.test.infrastructure";
      }

      @Override
      public String getFramework() {
        return "spring";
      }

      @Override
      public String getParadigm() {
        return "imperative";
      }

      @Override
      public String getUseCaseName() {
        return direction == AdapterDirection.INPUT ? "CreateUserUseCase" : null;
      }

      @Override
      public List<com.pragma.archetype.domain.model.adapter.Endpoint> getEndpoints() {
        return List.of();
      }

      @Override
      public String getEntityName() {
        return direction == AdapterDirection.OUTPUT ? "User" : null;
      }

      @Override
      public List<com.pragma.archetype.domain.model.adapter.AdapterMethod> getMethods() {
        return List.of();
      }
    };
  }
}
