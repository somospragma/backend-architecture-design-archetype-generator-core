package com.pragma.archetype.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pragma.archetype.application.generator.InputAdapterGenerator;
import com.pragma.archetype.domain.model.GeneratedFile;
import com.pragma.archetype.domain.model.InputAdapterConfig;
import com.pragma.archetype.domain.model.ValidationResult;
import com.pragma.archetype.domain.port.in.GenerateInputAdapterUseCase.GenerationResult;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import com.pragma.archetype.domain.service.InputAdapterValidator;

@ExtendWith(MockitoExtension.class)
class GenerateInputAdapterUseCaseImplTest {

  @Mock
  private InputAdapterValidator validator;

  @Mock
  private InputAdapterGenerator generator;

  @Mock
  private ConfigurationPort configurationPort;

  @Mock
  private FileSystemPort fileSystemPort;

  private GenerateInputAdapterUseCaseImpl useCase;

  @BeforeEach
  void setUp() {
    useCase = new GenerateInputAdapterUseCaseImpl(validator, generator, configurationPort, fileSystemPort);
  }

  @Test
  void shouldGenerateInputAdapterSuccessfully() {
    // Given
    InputAdapterConfig config = InputAdapterConfig.builder()
        .name("UserController")
        .useCaseName("CreateUserUseCase")
        .type(InputAdapterConfig.InputAdapterType.REST)
        .packageName("com.test.infrastructure.rest")
        .endpoints(List.of(
            new InputAdapterConfig.Endpoint(
                "/users",
                InputAdapterConfig.HttpMethod.POST,
                "execute",
                "User",
                List.of())))
        .build();

    GeneratedFile file = GeneratedFile.javaSource(
        Path.of("src/main/java/com/test/infrastructure/rest/UserController.java"),
        "public class UserController {}");

    when(validator.validate(any(), any())).thenReturn(ValidationResult.success());
    when(generator.generate(any(), any())).thenReturn(List.of(file));

    // When
    GenerationResult result = useCase.execute(Path.of("/test"), config);

    // Then
    assertTrue(result.success());
    assertEquals(1, result.generatedFiles().size());
    verify(fileSystemPort).writeFile(any());
  }

  @Test
  void shouldFailWhenValidationFails() {
    // Given
    InputAdapterConfig config = InputAdapterConfig.builder()
        .name("UserController")
        .useCaseName("CreateUserUseCase")
        .type(InputAdapterConfig.InputAdapterType.REST)
        .packageName("com.test.infrastructure.rest")
        .endpoints(List.of())
        .build();

    when(validator.validate(any(), any()))
        .thenReturn(ValidationResult.failure(List.of("Validation error")));

    // When
    GenerationResult result = useCase.execute(Path.of("/test"), config);

    // Then
    assertFalse(result.success());
    assertTrue(result.errors().contains("Validation error"));
    verify(generator, never()).generate(any(), any());
  }

  @Test
  void shouldHandleGeneratorException() {
    // Given
    InputAdapterConfig config = InputAdapterConfig.builder()
        .name("UserController")
        .useCaseName("CreateUserUseCase")
        .type(InputAdapterConfig.InputAdapterType.REST)
        .packageName("com.test.infrastructure.rest")
        .endpoints(List.of(
            new InputAdapterConfig.Endpoint(
                "/users",
                InputAdapterConfig.HttpMethod.POST,
                "execute",
                "User",
                List.of())))
        .build();

    when(validator.validate(any(), any())).thenReturn(ValidationResult.success());
    when(generator.generate(any(), any()))
        .thenThrow(new RuntimeException("Generator error"));

    // When
    GenerationResult result = useCase.execute(Path.of("/test"), config);

    // Then
    assertFalse(result.success());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("Failed to generate input adapter")));
  }

  @Test
  void shouldGenerateGraphQLResolver() {
    // Given
    InputAdapterConfig config = InputAdapterConfig.builder()
        .name("ProductResolver")
        .useCaseName("GetProductUseCase")
        .type(InputAdapterConfig.InputAdapterType.GRAPHQL)
        .packageName("com.test.infrastructure.graphql")
        .endpoints(List.of(
            new InputAdapterConfig.Endpoint(
                "/products",
                InputAdapterConfig.HttpMethod.GET,
                "execute",
                "Product",
                List.of())))
        .build();

    GeneratedFile file = GeneratedFile.javaSource(
        Path.of("src/main/java/com/test/infrastructure/graphql/ProductResolver.java"),
        "public class ProductResolver {}");

    when(validator.validate(any(), any())).thenReturn(ValidationResult.success());
    when(generator.generate(any(), any())).thenReturn(List.of(file));

    // When
    GenerationResult result = useCase.execute(Path.of("/test"), config);

    // Then
    assertTrue(result.success());
    assertEquals(1, result.generatedFiles().size());
  }
}
