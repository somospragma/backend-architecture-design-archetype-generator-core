package com.pragma.archetype.application.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
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

    // Setup common mocks (lenient to avoid UnnecessaryStubbingException in tests that don't use all)
    lenient().when(adapterMetadata.getName()).thenReturn("rest");
    lenient().when(adapterMetadata.getDirection()).thenReturn(AdapterDirection.INPUT);
    lenient().when(adapterMetadata.getSuffix()).thenReturn("Controller");
    lenient().when(adapterMetadata.isNeedsMapper()).thenReturn(false);
    lenient().when(adapterMetadata.hasAdditionalResources()).thenReturn(false);
    lenient().when(adapterMetadata.getDiscoveredTemplates()).thenReturn(List.of(
        TemplateFile.builder()
            .name("RestController.java.ftl")
            .type("main")
            .destination("{name}Controller.java")
            .build()));

    lenient().when(structureMetadata.getResourcesPath()).thenReturn("src/main/resources");
    lenient().when(structureMetadata.getSourceDirectory()).thenReturn("src/main/java");
    lenient().when(structureMetadata.modulePathForPackageType(anyString())).thenReturn(".");
    lenient().when(structureMetadata.generatesUseCaseInputPorts()).thenReturn(true);
    lenient().when(structureMetadata.packageForType(anyString(), anyString()))
        .thenAnswer(inv -> inv.getArgument(0) + ".domain." + inv.getArgument(1));
    lenient().when(structureMetadata.adapterResourcesPath(anyString(), anyString())).thenReturn("src/main/resources");
    lenient().when(structureMetadata.getAdapterBaseDirectory(anyString())).thenReturn("infrastructure/entry-points");
    lenient().when(structureMetadata.findModuleForPath(anyString())).thenReturn(null);
    lenient().when(structureMetadata.pathToPackageName(anyString())).thenAnswer(
        invocation -> invocation.getArgument(0, String.class).replace("-", "").replace("/", "."));
    lenient().when(structureMetadata.stripModulePrefix(anyString(), anyString())).thenAnswer(
        invocation -> invocation.getArgument(0, String.class));

    lenient().when(projectConfig.adaptersAsModules()).thenReturn(false);
    lenient().when(projectConfig.basePackage()).thenReturn("com.test");
    lenient().when(projectConfig.name()).thenReturn("test-project");
    lenient().when(projectConfig.architecture()).thenReturn(com.pragma.archetype.domain.model.project.ArchitectureType.HEXAGONAL_SINGLE);

    lenient().when(templateRepository.processTemplate(anyString(), anyMap())).thenReturn("public class TestController {}");
    lenient().when(templateRepository.loadStructureMetadata(any())).thenReturn(structureMetadata);
    lenient().when(templateRepository.templateExists(anyString())).thenReturn(true);
    lenient().when(pathResolver.resolveAdapterPath(any(), anyString(), anyString(), anyMap()))
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
    lenient().when(adapterMetadata.getDirection()).thenReturn(AdapterDirection.OUTPUT);
    lenient().when(adapterMetadata.getSuffix()).thenReturn("Adapter");
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
    assertEquals(2, result.size()); // Adapter + IUserGateway output port
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
    lenient().when(adapterMetadata.getDirection()).thenReturn(AdapterDirection.OUTPUT);
    lenient().when(adapterMetadata.getSuffix()).thenReturn("Adapter");
    lenient().when(adapterMetadata.isNeedsMapper()).thenReturn(true);
    lenient().when(adapterMetadata.getDiscoveredTemplates()).thenReturn(List.of(
        TemplateFile.builder()
            .name("Repository.java.ftl")
            .type("main")
            .destination("{name}Adapter.java")
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
    assertEquals(4, result.size()); // Main + Mapper + Entity + IUserGateway output port
  }

  @Test
  void shouldGenerateGatewayPortForDrivenAdapterWhenMissing(@TempDir Path projectPath) {
    // Given
    var config = createTestConfig("UserRepository", "postgresql", AdapterDirection.OUTPUT);
    stubOutputAdapter();
    when(templateRepository.processTemplate(org.mockito.ArgumentMatchers.eq(GATEWAY_TEMPLATE), anyMap()))
        .thenReturn("public interface IUserGateway {}");

    // When
    var result = generator.generate(projectPath, config, AdapterDirection.OUTPUT, structureMetadata, projectConfig);

    // Then
    Path gateway = projectPath.resolve("src/main/java/com/test/domain/ports/IUserGateway.java");
    assertTrue(result.stream().anyMatch(file -> file.path().equals(gateway)));
  }

  @Test
  void shouldKeepExistingGatewayPort(@TempDir Path projectPath) throws java.io.IOException {
    // Given a port the developer already filled with operations
    Path gateway = projectPath.resolve("src/main/java/com/test/domain/ports/IUserGateway.java");
    java.nio.file.Files.createDirectories(gateway.getParent());
    java.nio.file.Files.writeString(gateway, "public interface IUserGateway { User save(User user); }");
    var config = createTestConfig("UserRepository", "postgresql", AdapterDirection.OUTPUT);
    stubOutputAdapter();

    // When
    var result = generator.generate(projectPath, config, AdapterDirection.OUTPUT, structureMetadata, projectConfig);

    // Then it is neither regenerated nor overwritten
    assertFalse(result.stream().anyMatch(file -> file.path().equals(gateway)));
    verify(templateRepository, never()).processTemplate(org.mockito.ArgumentMatchers.eq(GATEWAY_TEMPLATE), anyMap());
  }

  @Test
  void shouldSkipGatewayPortWhenTemplatesDoNotProvideIt(@TempDir Path projectPath) {
    // Given templates older than domain/Gateway.java.ftl
    var config = createTestConfig("UserRepository", "postgresql", AdapterDirection.OUTPUT);
    stubOutputAdapter();
    when(templateRepository.templateExists(GATEWAY_TEMPLATE)).thenReturn(false);

    // When
    var result = generator.generate(projectPath, config, AdapterDirection.OUTPUT, structureMetadata, projectConfig);

    // Then
    assertFalse(result.stream().anyMatch(file -> file.path().getFileName().toString().equals("IUserGateway.java")));
  }

  private static final String GATEWAY_TEMPLATE = "frameworks/spring/imperative/domain/Gateway.java.ftl";

  private void stubOutputAdapter() {
    lenient().when(adapterMetadata.getDirection()).thenReturn(AdapterDirection.OUTPUT);
    lenient().when(adapterMetadata.getSuffix()).thenReturn("Adapter");
    when(adapterDiscovery.adapterExists(anyString(), any(), anyString(), anyString())).thenReturn(true);
    when(adapterDiscovery.findAdapter(anyString(), any(), anyString(), anyString()))
        .thenReturn(Optional.of(adapterMetadata));
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
