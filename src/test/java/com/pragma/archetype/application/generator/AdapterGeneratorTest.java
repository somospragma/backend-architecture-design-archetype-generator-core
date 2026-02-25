package com.pragma.archetype.application.generator;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pragma.archetype.domain.model.adapter.AdapterConfig;
import com.pragma.archetype.domain.model.adapter.AdapterType;
import com.pragma.archetype.domain.model.config.ProjectConfig;
import com.pragma.archetype.domain.model.file.GeneratedFile;
import com.pragma.archetype.domain.model.project.ArchitectureType;
import com.pragma.archetype.domain.model.project.Framework;
import com.pragma.archetype.domain.model.project.Paradigm;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import com.pragma.archetype.domain.port.out.PathResolver;
import com.pragma.archetype.domain.port.out.TemplateRepository;

@ExtendWith(MockitoExtension.class)
class AdapterGeneratorTest {

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

    // Configure pathResolver mock to return a valid path
    when(pathResolver.resolveAdapterPath(any(ArchitectureType.class), anyString(), anyString(), anyMap()))
        .thenReturn(Path.of("infrastructure/adapter"));
  }

  @Test
  void shouldGenerateRedisAdapter() {
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
        .name("UserRepository")
        .entityName("User")
        .type(AdapterType.REDIS)
        .packageName("com.test.infrastructure.redis")
        .methods(List.of())
        .build();

    when(templateRepository.processTemplate(anyString(), anyMap()))
        .thenReturn("public class UserRepository {}")
        .thenReturn("public class UserMapper {}")
        .thenReturn("public class UserData {}");

    // When
    List<GeneratedFile> files = generator.generate(Path.of("/test"), config, projectConfig);

    // Then
    assertNotNull(files);
    assertTrue(files.size() >= 1);
  }

  @Test
  void shouldGenerateMongoDBAdapter() {
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
        .name("ProductRepository")
        .entityName("Product")
        .type(AdapterType.MONGODB)
        .packageName("com.test.infrastructure.mongodb")
        .methods(List.of())
        .build();

    when(templateRepository.processTemplate(anyString(), anyMap()))
        .thenReturn("public class ProductRepository {}")
        .thenReturn("public class ProductMapper {}")
        .thenReturn("public class ProductData {}");

    // When
    List<GeneratedFile> files = generator.generate(Path.of("/test"), config, projectConfig);

    // Then
    assertNotNull(files);
    assertTrue(files.size() >= 1);
  }

  @Test
  void shouldGeneratePostgreSQLAdapter() {
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
        .name("OrderRepository")
        .entityName("Order")
        .type(AdapterType.POSTGRESQL)
        .packageName("com.test.infrastructure.postgresql")
        .methods(List.of())
        .build();

    when(templateRepository.processTemplate(anyString(), anyMap()))
        .thenReturn("public class OrderRepository {}")
        .thenReturn("public class OrderMapper {}")
        .thenReturn("public class OrderData {}");

    // When
    List<GeneratedFile> files = generator.generate(Path.of("/test"), config, projectConfig);

    // Then
    assertNotNull(files);
    assertTrue(files.size() >= 1);
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
        .type(AdapterType.REST_CLIENT)
        .packageName("com.test.infrastructure.restclient")
        .methods(List.of())
        .build();

    when(templateRepository.processTemplate(anyString(), anyMap()))
        .thenReturn("public class PaymentClient {}")
        .thenReturn("public class PaymentMapper {}")
        .thenReturn("public class PaymentData {}");

    // When
    List<GeneratedFile> files = generator.generate(Path.of("/test"), config, projectConfig);

    // Then
    assertNotNull(files);
    assertTrue(files.size() >= 1);
  }
}
