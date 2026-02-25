package com.pragma.archetype.application.usecase;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pragma.archetype.domain.model.project.ArchitectureType;
import com.pragma.archetype.domain.model.validation.ValidationResult;
import com.pragma.archetype.domain.service.TemplateValidator;

@ExtendWith(MockitoExtension.class)
class ValidateTemplateUseCaseImplTest {

  @Mock
  private TemplateValidator templateValidator;

  private ValidateTemplateUseCaseImpl useCase;

  @BeforeEach
  void setUp() {
    useCase = new ValidateTemplateUseCaseImpl(templateValidator);
  }

  @Test
  void shouldValidateAllTemplatesSuccessfully() {
    // Given
    when(templateValidator.validateArchitectureTemplates(any()))
        .thenReturn(ValidationResult.success());

    // When
    ValidationResult result = useCase.validateAll(Path.of("/test"));

    // Then
    assertTrue(result.valid());
    verify(templateValidator, atLeast(1)).validateArchitectureTemplates(any());
  }

  @Test
  void shouldFailWhenAnyArchitectureHasErrors() {
    // Given
    when(templateValidator.validateArchitectureTemplates(ArchitectureType.HEXAGONAL_SINGLE))
        .thenReturn(ValidationResult.success());
    when(templateValidator.validateArchitectureTemplates(ArchitectureType.HEXAGONAL_MULTI))
        .thenReturn(ValidationResult.failure(List.of("Template error")));
    when(templateValidator.validateArchitectureTemplates(any()))
        .thenReturn(ValidationResult.success());

    // When
    ValidationResult result = useCase.validateAll(Path.of("/test"));

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("Template error")));
  }

  @Test
  void shouldValidateSpecificArchitecture() {
    // Given
    when(templateValidator.validateArchitectureTemplates(ArchitectureType.HEXAGONAL_SINGLE))
        .thenReturn(ValidationResult.success());

    // When
    ValidationResult result = useCase.validateArchitecture("hexagonal-single");

    // Then
    assertTrue(result.valid());
    verify(templateValidator).validateArchitectureTemplates(ArchitectureType.HEXAGONAL_SINGLE);
  }

  @Test
  void shouldFailWithInvalidArchitectureName() {
    // When
    ValidationResult result = useCase.validateArchitecture("invalid-architecture");

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("Unknown architecture type")));
  }

  @Test
  void shouldValidateAdapter() {
    // Given
    when(templateValidator.validateAdapterTemplates("mongodb"))
        .thenReturn(ValidationResult.success());

    // When
    ValidationResult result = useCase.validateAdapter("mongodb");

    // Then
    assertTrue(result.valid());
    verify(templateValidator).validateAdapterTemplates("mongodb");
  }

  @Test
  void shouldFailWhenAdapterHasErrors() {
    // Given
    when(templateValidator.validateAdapterTemplates("redis"))
        .thenReturn(ValidationResult.failure(List.of("Adapter template error")));

    // When
    ValidationResult result = useCase.validateAdapter("redis");

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("Adapter template error")));
  }

  @Test
  void shouldHandleWarnings() {
    // Given
    when(templateValidator.validateArchitectureTemplates(any()))
        .thenReturn(ValidationResult.successWithWarnings(List.of("Warning message")));

    // When
    ValidationResult result = useCase.validateAll(Path.of("/test"));

    // Then
    assertTrue(result.valid());
    assertTrue(result.hasWarnings());
    assertTrue(result.warnings().stream().anyMatch(w -> w.contains("Warning message")));
  }
}
