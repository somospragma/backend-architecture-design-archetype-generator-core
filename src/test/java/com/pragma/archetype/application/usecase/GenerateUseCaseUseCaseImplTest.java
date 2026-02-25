package com.pragma.archetype.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pragma.archetype.application.generator.UseCaseGenerator;
import com.pragma.archetype.domain.model.GeneratedFile;
import com.pragma.archetype.domain.model.ValidationResult;
import com.pragma.archetype.domain.model.usecase.UseCaseConfig;
import com.pragma.archetype.domain.port.in.GenerateUseCaseUseCase.GenerationResult;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import com.pragma.archetype.domain.service.UseCaseValidator;

@ExtendWith(MockitoExtension.class)
class GenerateUseCaseUseCaseImplTest {

  @Mock
  private UseCaseValidator validator;

  @Mock
  private UseCaseGenerator generator;

  @Mock
  private ConfigurationPort configurationPort;

  @Mock
  private FileSystemPort fileSystemPort;

  private GenerateUseCaseUseCaseImpl useCase;

  @BeforeEach
  void setUp() {
    useCase = new GenerateUseCaseUseCaseImpl(validator, generator, configurationPort, fileSystemPort);
  }

  @Test
  void shouldGenerateUseCaseSuccessfully() {
    // Given
    UseCaseConfig config = UseCaseConfig.builder()
        .name("CreateUser")
        .packageName("com.test.domain.port.in")
        .generatePort(true)
        .generateImpl(true)
        .methods(List.of(
            new UseCaseConfig.UseCaseMethod("execute", "User", List.of())))
        .build();

    GeneratedFile portFile = GeneratedFile.javaSource(
        Path.of("src/main/java/com/test/domain/port/in/CreateUserUseCase.java"),
        "public interface CreateUserUseCase {}");

    GeneratedFile implFile = GeneratedFile.javaSource(
        Path.of("src/main/java/com/test/application/usecase/CreateUserUseCaseImpl.java"),
        "public class CreateUserUseCaseImpl {}");

    when(validator.validate(any(), any())).thenReturn(ValidationResult.success());
    when(generator.generate(any(), any())).thenReturn(List.of(portFile, implFile));

    // When
    GenerationResult result = useCase.execute(Path.of("/test"), config);

    // Then
    assertTrue(result.success());
    assertEquals(2, result.generatedFiles().size());
    verify(fileSystemPort, times(2)).writeFile(any());
  }

  @Test
  void shouldFailWhenValidationFails() {
    // Given
    UseCaseConfig config = UseCaseConfig.builder()
        .name("CreateUser")
        .packageName("com.test.domain.port.in")
        .generatePort(true)
        .generateImpl(true)
        .methods(List.of())
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
    UseCaseConfig config = UseCaseConfig.builder()
        .name("CreateUser")
        .packageName("com.test.domain.port.in")
        .generatePort(true)
        .generateImpl(true)
        .methods(List.of(
            new UseCaseConfig.UseCaseMethod("execute", "User", List.of())))
        .build();

    when(validator.validate(any(), any())).thenReturn(ValidationResult.success());
    when(generator.generate(any(), any()))
        .thenThrow(new RuntimeException("Generator error"));

    // When
    GenerationResult result = useCase.execute(Path.of("/test"), config);

    // Then
    assertFalse(result.success());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("Failed to generate use case")));
  }

  @Test
  void shouldGenerateOnlyPort() {
    // Given
    UseCaseConfig config = UseCaseConfig.builder()
        .name("GetUser")
        .packageName("com.test.domain.port.in")
        .generatePort(true)
        .generateImpl(false)
        .methods(List.of(
            new UseCaseConfig.UseCaseMethod("execute", "User", List.of())))
        .build();

    GeneratedFile portFile = GeneratedFile.javaSource(
        Path.of("src/main/java/com/test/domain/port/in/GetUserUseCase.java"),
        "public interface GetUserUseCase {}");

    when(validator.validate(any(), any())).thenReturn(ValidationResult.success());
    when(generator.generate(any(), any())).thenReturn(List.of(portFile));

    // When
    GenerationResult result = useCase.execute(Path.of("/test"), config);

    // Then
    assertTrue(result.success());
    assertEquals(1, result.generatedFiles().size());
    verify(fileSystemPort, times(1)).writeFile(any());
  }
}
