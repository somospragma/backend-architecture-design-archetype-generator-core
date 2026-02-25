package com.pragma.archetype.application.generator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pragma.archetype.domain.model.AdapterConfig;
import com.pragma.archetype.domain.model.ArchitectureType;
import com.pragma.archetype.domain.model.Framework;
import com.pragma.archetype.domain.model.GeneratedFile;
import com.pragma.archetype.domain.model.Paradigm;
import com.pragma.archetype.domain.model.config.ProjectConfig;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import com.pragma.archetype.domain.port.out.PathResolver;
import com.pragma.archetype.domain.port.out.TemplateRepository;

@ExtendWith(MockitoExtension.class)
class AdapterGeneratorExtendedTest {

  @Mock
  private TemplateRepository templateRepository;

  @Mock
  private FileSystemPort fileSystemPort;

  @Mock
  private PathResolver pathResolver;

  private AdapterGenerator generator;

  @BeforeEach
  void setUp() {
    generator = new AdapterGenerator(templateRepository, fileSystemPort, pathResolver);
    when(templateRepository.processTemplate(anyString(), anyMap()))
        .thenReturn("template content");
    when(pathResolver.resolveAdapterPath(any(), anyString(), anyString(), anyMap()))
        .thenReturn(Path.of("/test/infrastructure/adapter"));
  }

  @Test
  void shouldGenerateAdapterForMultiModuleArchitecture() {
    // Given
    ProjectConfig projectConfig = ProjectConfig.builder()
        .name("test-project")
        .basePackage("com.test")
        .architecture(ArchitectureType.HEXAGONAL_MULTI)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .pluginVersion("1.0.0")
        .build();

    AdapterConfig config = AdapterConfig.builder()
        .name("UserRepository")
        .entityName("User")
        .type(AdapterConfig.AdapterType.REDIS)
        .packageName("com.test.infrastructure.redis")
        .methods(List.of())
        .build();

    // When
    List<GeneratedFile> files = generator.generate(Path.of("/test"), config, projectConfig);

    // Then
    assertNotNull(files);
    assertFalse(files.isEmpty());
  }

  @Test
  void shouldGenerateAdapterForGranularArchitecture() {
    // Given
    ProjectConfig projectConfig = ProjectConfig.builder()
        .name("test-project")
        .basePackage("com.test")
        .architecture(ArchitectureType.HEXAGONAL_MULTI_GRANULAR)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .pluginVersion("1.0.0")
        .build();

    AdapterConfig config = AdapterConfig.builder()
        .name("UserRepository")
        .entityName("User")
        .type(AdapterConfig.AdapterType.POSTGRESQL)
        .packageName("com.test.infrastructure.postgresql")
        .methods(List.of())
        .build();

    // When
    List<GeneratedFile> files = generator.generate(Path.of("/test"), config, projectConfig);

    // Then
    assertNotNull(files);
    assertFalse(files.isEmpty());
  }

  @Test
  void shouldGenerateAdapterForOnionArchitecture() {
    // Given
    ProjectConfig projectConfig = ProjectConfig.builder()
        .name("test-project")
        .basePackage("com.test")
        .architecture(ArchitectureType.ONION_SINGLE)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .pluginVersion("1.0.0")
        .build();

    AdapterConfig config = AdapterConfig.builder()
        .name("UserRepository")
        .entityName("User")
        .type(AdapterConfig.AdapterType.MONGODB)
        .packageName("com.test.infrastructure.mongodb")
        .methods(List.of())
        .build();

    // When
    List<GeneratedFile> files = generator.generate(Path.of("/test"), config, projectConfig);

    // Then
    assertNotNull(files);
    assertFalse(files.isEmpty());
  }

  @Test
  void shouldGenerateAdapterWithImperativeParadigm() {
    // Given
    ProjectConfig projectConfig = ProjectConfig.builder()
        .name("test-project")
        .basePackage("com.test")
        .architecture(ArchitectureType.HEXAGONAL_SINGLE)
        .paradigm(Paradigm.IMPERATIVE)
        .framework(Framework.SPRING)
        .pluginVersion("1.0.0")
        .build();

    AdapterConfig config = AdapterConfig.builder()
        .name("UserRepository")
        .entityName("User")
        .type(AdapterConfig.AdapterType.POSTGRESQL)
        .packageName("com.test.infrastructure.postgresql")
        .methods(List.of())
        .build();

    // When
    List<GeneratedFile> files = generator.generate(Path.of("/test"), config, projectConfig);

    // Then
    assertNotNull(files);
    assertFalse(files.isEmpty());
  }

  @Test
  void shouldGenerateAdapterWithQuarkusFramework() {
    // Given
    ProjectConfig projectConfig = ProjectConfig.builder()
        .name("test-project")
        .basePackage("com.test")
        .architecture(ArchitectureType.HEXAGONAL_SINGLE)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.QUARKUS)
        .pluginVersion("1.0.0")
        .build();

    AdapterConfig config = AdapterConfig.builder()
        .name("UserRepository")
        .entityName("User")
        .type(AdapterConfig.AdapterType.POSTGRESQL)
        .packageName("com.test.infrastructure.postgresql")
        .methods(List.of())
        .build();

    // When
    List<GeneratedFile> files = generator.generate(Path.of("/test"), config, projectConfig);

    // Then
    assertNotNull(files);
    assertFalse(files.isEmpty());
  }

  @Test
  void shouldGenerateKafkaAdapter() {
    // Given
    ProjectConfig projectConfig = ProjectConfig.builder()
        .name("test-project")
        .basePackage("com.test")
        .architecture(ArchitectureType.HEXAGONAL_SINGLE)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .pluginVersion("1.0.0")
        .build();

    AdapterConfig config = AdapterConfig.builder()
        .name("EventPublisher")
        .entityName("Event")
        .type(AdapterConfig.AdapterType.KAFKA)
        .packageName("com.test.infrastructure.kafka")
        .methods(List.of())
        .build();

    // When
    List<GeneratedFile> files = generator.generate(Path.of("/test"), config, projectConfig);

    // Then
    assertNotNull(files);
    assertFalse(files.isEmpty());
  }

  @Test
  void shouldGenerateAdapterWithCustomMethods() {
    // Given
    ProjectConfig projectConfig = ProjectConfig.builder()
        .name("test-project")
        .basePackage("com.test")
        .architecture(ArchitectureType.HEXAGONAL_SINGLE)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .pluginVersion("1.0.0")
        .build();

    AdapterConfig.AdapterMethod method1 = new AdapterConfig.AdapterMethod(
        "findByEmail",
        "Mono<User>",
        List.of(new AdapterConfig.MethodParameter("email", "String")));

    AdapterConfig.AdapterMethod method2 = new AdapterConfig.AdapterMethod(
        "findByStatus",
        "Flux<User>",
        List.of(new AdapterConfig.MethodParameter("status", "Status")));

    AdapterConfig config = AdapterConfig.builder()
        .name("UserRepository")
        .entityName("User")
        .type(AdapterConfig.AdapterType.POSTGRESQL)
        .packageName("com.test.infrastructure.postgresql")
        .methods(List.of(method1, method2))
        .build();

    // When
    List<GeneratedFile> files = generator.generate(Path.of("/test"), config, projectConfig);

    // Then
    assertNotNull(files);
    assertFalse(files.isEmpty());
  }

  @Test
  void shouldGenerateRestClientAdapter() {
    // Given
    ProjectConfig projectConfig = ProjectConfig.builder()
        .name("test-project")
        .basePackage("com.test")
        .architecture(ArchitectureType.HEXAGONAL_SINGLE)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .pluginVersion("1.0.0")
        .build();

    AdapterConfig config = AdapterConfig.builder()
        .name("PaymentClient")
        .entityName("Payment")
        .type(AdapterConfig.AdapterType.REST_CLIENT)
        .packageName("com.test.infrastructure.restclient")
        .methods(List.of())
        .build();

    // When
    List<GeneratedFile> files = generator.generate(Path.of("/test"), config, projectConfig);

    // Then
    assertNotNull(files);
    assertFalse(files.isEmpty());
  }

  @Test
  void shouldGenerateAdapterWithDependencyOverrides() {
    // Given
    ProjectConfig projectConfig = ProjectConfig.builder()
        .name("test-project")
        .basePackage("com.test")
        .architecture(ArchitectureType.HEXAGONAL_SINGLE)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .pluginVersion("1.0.0")
        .dependencyOverrides(Map.of("redis", "7.0.0"))
        .build();

    AdapterConfig config = AdapterConfig.builder()
        .name("UserRepository")
        .entityName("User")
        .type(AdapterConfig.AdapterType.REDIS)
        .packageName("com.test.infrastructure.redis")
        .methods(List.of())
        .build();

    // When
    List<GeneratedFile> files = generator.generate(Path.of("/test"), config, projectConfig);

    // Then
    assertNotNull(files);
    assertFalse(files.isEmpty());
  }
}
