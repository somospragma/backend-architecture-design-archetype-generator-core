package com.pragma.archetype.domain.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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

import com.pragma.archetype.domain.model.InputAdapterConfig;
import com.pragma.archetype.domain.model.ValidationResult;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.port.out.FileSystemPort;

@ExtendWith(MockitoExtension.class)
class InputAdapterValidatorTest {

  @Mock
  private FileSystemPort fileSystemPort;

  @Mock
  private ConfigurationPort configurationPort;

  @Mock
  private PackageValidator packageValidator;

  private InputAdapterValidator validator;

  @BeforeEach
  void setUp() {
    validator = new InputAdapterValidator(fileSystemPort, configurationPort, packageValidator);
  }

  @Test
  void shouldValidateSuccessfully() {
    // Given
    when(fileSystemPort.directoryExists(any())).thenReturn(true);
    when(packageValidator.validatePackageName(anyString())).thenReturn(ValidationResult.success());
    when(configurationPort.readConfiguration(any())).thenReturn(Optional.empty());

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

    // When
    ValidationResult result = validator.validate(Path.of("/test"), config);

    // Then
    assertTrue(result.valid());
  }

  @Test
  void shouldFailWhenProjectDoesNotExist() {
    // Given
    when(fileSystemPort.directoryExists(any())).thenReturn(false);

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

    // When
    ValidationResult result = validator.validate(Path.of("/test"), config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("does not exist")));
  }

  @Test
  void shouldFailWithInvalidAdapterName() {
    // Given
    when(fileSystemPort.directoryExists(any())).thenReturn(true);
    when(packageValidator.validatePackageName(anyString())).thenReturn(ValidationResult.success());
    when(configurationPort.readConfiguration(any())).thenReturn(Optional.empty());

    InputAdapterConfig config = InputAdapterConfig.builder()
        .name("User-Controller") // invalid
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

    // When
    ValidationResult result = validator.validate(Path.of("/test"), config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("valid Java identifier")));
  }

  @Test
  void shouldFailWithInvalidPackageStructure() {
    // Given
    when(fileSystemPort.directoryExists(any())).thenReturn(true);
    when(packageValidator.validatePackageName(anyString())).thenReturn(ValidationResult.success());
    when(configurationPort.readConfiguration(any())).thenReturn(Optional.empty());

    InputAdapterConfig config = InputAdapterConfig.builder()
        .name("UserController")
        .useCaseName("CreateUserUseCase")
        .type(InputAdapterConfig.InputAdapterType.REST)
        .packageName("com.test.adapter.in.rest") // invalid structure
        .endpoints(List.of(
            new InputAdapterConfig.Endpoint(
                "/users",
                InputAdapterConfig.HttpMethod.POST,
                "execute",
                "User",
                List.of())))
        .build();

    // When
    ValidationResult result = validator.validate(Path.of("/test"), config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("entry-points")));
  }

  @Test
  void shouldFailWithNoEndpoints() {
    // Given
    when(fileSystemPort.directoryExists(any())).thenReturn(true);
    when(packageValidator.validatePackageName(anyString())).thenReturn(ValidationResult.success());
    when(configurationPort.readConfiguration(any())).thenReturn(Optional.empty());

    InputAdapterConfig config = InputAdapterConfig.builder()
        .name("UserController")
        .useCaseName("CreateUserUseCase")
        .type(InputAdapterConfig.InputAdapterType.REST)
        .packageName("com.test.infrastructure.rest")
        .endpoints(List.of())
        .build();

    // When
    ValidationResult result = validator.validate(Path.of("/test"), config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("At least one endpoint")));
  }

  @Test
  void shouldFailWithInvalidEndpointPath() {
    // Given
    when(fileSystemPort.directoryExists(any())).thenReturn(true);
    when(packageValidator.validatePackageName(anyString())).thenReturn(ValidationResult.success());
    when(configurationPort.readConfiguration(any())).thenReturn(Optional.empty());

    InputAdapterConfig config = InputAdapterConfig.builder()
        .name("UserController")
        .useCaseName("CreateUserUseCase")
        .type(InputAdapterConfig.InputAdapterType.REST)
        .packageName("com.test.infrastructure.rest")
        .endpoints(List.of(
            new InputAdapterConfig.Endpoint(
                "users", // missing leading slash
                InputAdapterConfig.HttpMethod.POST,
                "execute",
                "User",
                List.of())))
        .build();

    // When
    ValidationResult result = validator.validate(Path.of("/test"), config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("must start with '/'")));
  }
}
