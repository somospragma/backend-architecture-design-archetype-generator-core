package com.pragma.archetype.domain.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pragma.archetype.domain.model.ArchitectureType;
import com.pragma.archetype.domain.model.Framework;
import com.pragma.archetype.domain.model.Paradigm;
import com.pragma.archetype.domain.model.ProjectConfig;
import com.pragma.archetype.domain.model.UseCaseConfig;
import com.pragma.archetype.domain.model.ValidationResult;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.port.out.FileSystemPort;

@ExtendWith(MockitoExtension.class)
class UseCaseValidatorTest {

  @Mock
  private FileSystemPort fileSystemPort;

  @Mock
  private ConfigurationPort configurationPort;

  @Mock
  private PackageValidator packageValidator;

  private UseCaseValidator validator;

  @BeforeEach
  void setUp() {
    validator = new UseCaseValidator(fileSystemPort, configurationPort, packageValidator);
  }

  @Test
  void shouldValidateSuccessfully() {
    // Given
    when(fileSystemPort.directoryExists(any())).thenReturn(true);
    when(packageValidator.validatePackageName(anyString())).thenReturn(ValidationResult.success());
    when(configurationPort.readConfiguration(any())).thenReturn(Optional.empty());

    UseCaseConfig config = UseCaseConfig.builder()
        .name("CreateUser")
        .packageName("com.test.domain.port.in")
        .generatePort(true)
        .generateImpl(true)
        .methods(List.of(
            new UseCaseConfig.UseCaseMethod("execute", "User", List.of(
                new UseCaseConfig.MethodParameter("userId", "String")))))
        .build();

    // When
    ValidationResult result = validator.validate(Path.of("/test"), config);

    // Then
    assertTrue(result.valid());
  }

  @Test
  void shouldFailWhenProjectDoesNotExist() {
    // Given
    when(fileSystemPort.directoryExists(any())).thenReturn(false);

    UseCaseConfig config = UseCaseConfig.builder()
        .name("CreateUser")
        .packageName("com.test.domain.port.in")
        .generatePort(true)
        .generateImpl(true)
        .methods(List.of(
            new UseCaseConfig.UseCaseMethod("execute", "User", List.of())))
        .build();

    // When
    ValidationResult result = validator.validate(Path.of("/test"), config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("does not exist")));
  }

  @Test
  void shouldFailWithInvalidUseCaseName() {
    // Given
    when(fileSystemPort.directoryExists(any())).thenReturn(true);
    when(packageValidator.validatePackageName(anyString())).thenReturn(ValidationResult.success());
    when(configurationPort.readConfiguration(any())).thenReturn(Optional.empty());

    UseCaseConfig config = UseCaseConfig.builder()
        .name("Create-User") // invalid
        .packageName("com.test.domain.port.in")
        .generatePort(true)
        .generateImpl(true)
        .methods(List.of(
            new UseCaseConfig.UseCaseMethod("execute", "User", List.of())))
        .build();

    // When
    ValidationResult result = validator.validate(Path.of("/test"), config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("valid Java identifier")));
  }

  @Test
  void shouldFailWithInvalidPackageName() {
    // Given
    when(fileSystemPort.directoryExists(any())).thenReturn(true);
    when(packageValidator.validatePackageName(anyString()))
        .thenReturn(ValidationResult.failure(List.of("Invalid package")));
    when(configurationPort.readConfiguration(any())).thenReturn(Optional.empty());

    UseCaseConfig config = UseCaseConfig.builder()
        .name("CreateUser")
        .packageName("Invalid-Package")
        .generatePort(true)
        .generateImpl(true)
        .methods(List.of(
            new UseCaseConfig.UseCaseMethod("execute", "User", List.of())))
        .build();

    // When
    ValidationResult result = validator.validate(Path.of("/test"), config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("Invalid package")));
  }

  @Test
  void shouldFailWithNoMethods() {
    // Given
    when(fileSystemPort.directoryExists(any())).thenReturn(true);
    when(packageValidator.validatePackageName(anyString())).thenReturn(ValidationResult.success());
    when(configurationPort.readConfiguration(any())).thenReturn(Optional.empty());

    UseCaseConfig config = UseCaseConfig.builder()
        .name("CreateUser")
        .packageName("com.test.domain.port.in")
        .generatePort(true)
        .generateImpl(true)
        .methods(List.of())
        .build();

    // When
    ValidationResult result = validator.validate(Path.of("/test"), config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("At least one method")));
  }

  @Test
  void shouldFailWhenNoGenerationOptionEnabled() {
    // Given
    when(fileSystemPort.directoryExists(any())).thenReturn(true);
    when(packageValidator.validatePackageName(anyString())).thenReturn(ValidationResult.success());
    when(configurationPort.readConfiguration(any())).thenReturn(Optional.empty());

    UseCaseConfig config = UseCaseConfig.builder()
        .name("CreateUser")
        .packageName("com.test.domain.port.in")
        .generatePort(false)
        .generateImpl(false)
        .methods(List.of(
            new UseCaseConfig.UseCaseMethod("execute", "User", List.of())))
        .build();

    // When
    ValidationResult result = validator.validate(Path.of("/test"), config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("generatePort or generateImpl")));
  }

  @Test
  void shouldValidateBasePackageConsistency() {
    // Given
    when(fileSystemPort.directoryExists(any())).thenReturn(true);
    when(packageValidator.validatePackageName(anyString())).thenReturn(ValidationResult.success());
    when(packageValidator.validateBasePackageConsistency(anyString(), anyString()))
        .thenReturn(ValidationResult.success());

    ProjectConfig projectConfig = ProjectConfig.builder()
        .name("test-project")
        .basePackage("com.test")
        .architecture(ArchitectureType.HEXAGONAL_SINGLE)
        .paradigm(Paradigm.REACTIVE)
        .framework(Framework.SPRING)
        .pluginVersion("1.0.0")
        .build();

    when(configurationPort.readConfiguration(any())).thenReturn(Optional.of(projectConfig));

    UseCaseConfig config = UseCaseConfig.builder()
        .name("CreateUser")
        .packageName("com.test.domain.port.in")
        .generatePort(true)
        .generateImpl(true)
        .methods(List.of(
            new UseCaseConfig.UseCaseMethod("execute", "User", List.of())))
        .build();

    // When
    ValidationResult result = validator.validate(Path.of("/test"), config);

    // Then
    assertTrue(result.valid());
    verify(packageValidator).validateBasePackageConsistency("com.test.domain.port.in", "com.test");
  }
}
