package com.pragma.archetype.domain.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pragma.archetype.domain.model.adapter.Endpoint;
import com.pragma.archetype.domain.model.adapter.EndpointParameter;
import com.pragma.archetype.domain.model.adapter.HttpMethod;
import com.pragma.archetype.domain.model.adapter.InputAdapterConfig;
import com.pragma.archetype.domain.model.adapter.InputAdapterType;
import com.pragma.archetype.domain.model.adapter.ParameterType;
import com.pragma.archetype.domain.model.config.ProjectConfig;
import com.pragma.archetype.domain.model.validation.ValidationResult;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.port.out.FileSystemPort;

@ExtendWith(MockitoExtension.class)
class InputAdapterValidatorBranchTest {

  @Mock
  private FileSystemPort fileSystemPort;

  @Mock
  private ConfigurationPort configurationPort;

  @Mock
  private PackageValidator packageValidator;

  private InputAdapterValidator validator;
  private Path projectPath;

  @BeforeEach
  void setUp() {
    validator = new InputAdapterValidator(fileSystemPort, configurationPort, packageValidator);
    projectPath = Path.of("/test/project");
  }

  @Test
  void shouldFailWhenProjectDirectoryDoesNotExist() {
    // Given
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(false);
    InputAdapterConfig config = InputAdapterConfig.builder()
        .name("UserController")
        .useCaseName("CreateUserUseCase")
        .type(InputAdapterType.REST)
        .packageName("com.test.infrastructure.entrypoints.rest")
        .endpoints(List.of())
        .build();

    // When
    ValidationResult result = validator.validate(projectPath, config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("Project directory does not exist")));
  }

  @Test
  void shouldFailWhenAdapterNameIsNull() {
    // Given
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    InputAdapterConfig config = InputAdapterConfig.builder()
        .name(null)
        .useCaseName("CreateUserUseCase")
        .type(InputAdapterType.REST)
        .packageName("com.test.infrastructure.entrypoints.rest")
        .endpoints(List.of())
        .build();

    // When
    ValidationResult result = validator.validate(projectPath, config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("Adapter name is required")));
  }

  @Test
  void shouldFailWhenAdapterNameIsInvalidIdentifier() {
    // Given
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    InputAdapterConfig config = InputAdapterConfig.builder()
        .name("123Invalid")
        .useCaseName("CreateUserUseCase")
        .type(InputAdapterType.REST)
        .packageName("com.test.infrastructure.entrypoints.rest")
        .endpoints(List.of())
        .build();

    // When
    ValidationResult result = validator.validate(projectPath, config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("must be a valid Java identifier")));
  }

  @Test
  void shouldFailWhenPackageNameIsNull() {
    // Given
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    InputAdapterConfig config = InputAdapterConfig.builder()
        .name("UserController")
        .useCaseName("CreateUserUseCase")
        .type(InputAdapterType.REST)
        .packageName(null)
        .endpoints(List.of())
        .build();

    // When
    ValidationResult result = validator.validate(projectPath, config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("Package name is required")));
  }

  @Test
  void shouldFailWhenPackageNameContainsAdapterOut() {
    // Given
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    when(packageValidator.validatePackageName(any())).thenReturn(ValidationResult.success());
    InputAdapterConfig config = InputAdapterConfig.builder()
        .name("UserController")
        .useCaseName("CreateUserUseCase")
        .type(InputAdapterType.REST)
        .packageName("com.test.adapter.out.rest")
        .endpoints(List.of())
        .build();

    // When
    ValidationResult result = validator.validate(projectPath, config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("use 'driven-adapters' instead of 'adapter.out'")));
  }

  @Test
  void shouldFailWhenPackageNameContainsAdapterIn() {
    // Given
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    when(packageValidator.validatePackageName(any())).thenReturn(ValidationResult.success());
    InputAdapterConfig config = InputAdapterConfig.builder()
        .name("UserController")
        .useCaseName("CreateUserUseCase")
        .type(InputAdapterType.REST)
        .packageName("com.test.adapter.in.rest")
        .endpoints(List.of())
        .build();

    // When
    ValidationResult result = validator.validate(projectPath, config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("use 'entry-points' instead of 'adapter.in'")));
  }

  @Test
  void shouldValidateBasePackageConsistencyWhenProjectConfigExists() {
    // Given
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    when(packageValidator.validatePackageName(any())).thenReturn(ValidationResult.success());

    ProjectConfig projectConfig = ProjectConfig.builder()
        .name("test-project")
        .basePackage("com.test")
        .build();
    when(configurationPort.readConfiguration(projectPath)).thenReturn(Optional.of(projectConfig));
    when(packageValidator.validateBasePackageConsistency(any(), any()))
        .thenReturn(ValidationResult.failure(List.of("Package does not match base package")));

    InputAdapterConfig config = InputAdapterConfig.builder()
        .name("UserController")
        .useCaseName("CreateUserUseCase")
        .type(InputAdapterType.REST)
        .packageName("com.wrong.infrastructure.entrypoints.rest")
        .endpoints(List.of())
        .build();

    // When
    ValidationResult result = validator.validate(projectPath, config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("Package does not match base package")));
  }

  @Test
  void shouldFailWhenAdapterTypeIsNull() {
    // Given
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    when(packageValidator.validatePackageName(any())).thenReturn(ValidationResult.success());
    InputAdapterConfig config = InputAdapterConfig.builder()
        .name("UserController")
        .useCaseName("CreateUserUseCase")
        .type(null)
        .packageName("com.test.infrastructure.entrypoints.rest")
        .endpoints(List.of())
        .build();

    // When
    ValidationResult result = validator.validate(projectPath, config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("Adapter type is required")));
  }

  @Test
  void shouldFailWhenUseCaseNameIsNull() {
    // Given
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    when(packageValidator.validatePackageName(any())).thenReturn(ValidationResult.success());
    InputAdapterConfig config = InputAdapterConfig.builder()
        .name("UserController")
        .useCaseName(null)
        .type(InputAdapterType.REST)
        .packageName("com.test.infrastructure.entrypoints.rest")
        .endpoints(List.of())
        .build();

    // When
    ValidationResult result = validator.validate(projectPath, config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("Use case name is required")));
  }

  @Test
  void shouldFailWhenEndpointsAreNull() {
    // Given
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    when(packageValidator.validatePackageName(any())).thenReturn(ValidationResult.success());
    InputAdapterConfig config = InputAdapterConfig.builder()
        .name("UserController")
        .useCaseName("CreateUserUseCase")
        .type(InputAdapterType.REST)
        .packageName("com.test.infrastructure.entrypoints.rest")
        .endpoints(null)
        .build();

    // When
    ValidationResult result = validator.validate(projectPath, config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("At least one endpoint is required")));
  }

  @Test
  void shouldFailWhenEndpointPathIsNull() {
    // Given
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    when(packageValidator.validatePackageName(any())).thenReturn(ValidationResult.success());

    Endpoint endpoint = new Endpoint(
        null,
        HttpMethod.POST,
        "create",
        "User",
        List.of());
    InputAdapterConfig config = InputAdapterConfig.builder()
        .name("UserController")
        .useCaseName("CreateUserUseCase")
        .type(InputAdapterType.REST)
        .packageName("com.test.infrastructure.entrypoints.rest")
        .endpoints(List.of(endpoint))
        .build();

    // When
    ValidationResult result = validator.validate(projectPath, config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("Endpoint path is required")));
  }

  @Test
  void shouldFailWhenEndpointPathDoesNotStartWithSlash() {
    // Given
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    when(packageValidator.validatePackageName(any())).thenReturn(ValidationResult.success());

    Endpoint endpoint = new Endpoint(
        "users",
        HttpMethod.POST,
        "create",
        "User",
        List.of());
    InputAdapterConfig config = InputAdapterConfig.builder()
        .name("UserController")
        .useCaseName("CreateUserUseCase")
        .type(InputAdapterType.REST)
        .packageName("com.test.infrastructure.entrypoints.rest")
        .endpoints(List.of(endpoint))
        .build();

    // When
    ValidationResult result = validator.validate(projectPath, config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("Endpoint path must start with '/'")));
  }

  @Test
  void shouldFailWhenHttpMethodIsNull() {
    // Given
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    when(packageValidator.validatePackageName(any())).thenReturn(ValidationResult.success());

    Endpoint endpoint = new Endpoint(
        "/users",
        null,
        "create",
        "User",
        List.of());
    InputAdapterConfig config = InputAdapterConfig.builder()
        .name("UserController")
        .useCaseName("CreateUserUseCase")
        .type(InputAdapterType.REST)
        .packageName("com.test.infrastructure.entrypoints.rest")
        .endpoints(List.of(endpoint))
        .build();

    // When
    ValidationResult result = validator.validate(projectPath, config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("HTTP method is required")));
  }

  @Test
  void shouldFailWhenUseCaseMethodIsNull() {
    // Given
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    when(packageValidator.validatePackageName(any())).thenReturn(ValidationResult.success());

    Endpoint endpoint = new Endpoint(
        "/users",
        HttpMethod.POST,
        null,
        "User",
        List.of());
    InputAdapterConfig config = InputAdapterConfig.builder()
        .name("UserController")
        .useCaseName("CreateUserUseCase")
        .type(InputAdapterType.REST)
        .packageName("com.test.infrastructure.entrypoints.rest")
        .endpoints(List.of(endpoint))
        .build();

    // When
    ValidationResult result = validator.validate(projectPath, config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("Use case method is required")));
  }

  @Test
  void shouldFailWhenReturnTypeIsNull() {
    // Given
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    when(packageValidator.validatePackageName(any())).thenReturn(ValidationResult.success());

    Endpoint endpoint = new Endpoint(
        "/users",
        HttpMethod.POST,
        "create",
        null,
        List.of());
    InputAdapterConfig config = InputAdapterConfig.builder()
        .name("UserController")
        .useCaseName("CreateUserUseCase")
        .type(InputAdapterType.REST)
        .packageName("com.test.infrastructure.entrypoints.rest")
        .endpoints(List.of(endpoint))
        .build();

    // When
    ValidationResult result = validator.validate(projectPath, config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("Return type is required")));
  }

  @Test
  void shouldFailWhenParameterNameIsNull() {
    // Given
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    when(packageValidator.validatePackageName(any())).thenReturn(ValidationResult.success());

    EndpointParameter param = new EndpointParameter(
        null,
        "User",
        ParameterType.BODY);
    Endpoint endpoint = new Endpoint(
        "/users",
        HttpMethod.POST,
        "create",
        "User",
        List.of(param));
    InputAdapterConfig config = InputAdapterConfig.builder()
        .name("UserController")
        .useCaseName("CreateUserUseCase")
        .type(InputAdapterType.REST)
        .packageName("com.test.infrastructure.entrypoints.rest")
        .endpoints(List.of(endpoint))
        .build();

    // When
    ValidationResult result = validator.validate(projectPath, config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("Parameter name is required")));
  }

  @Test
  void shouldFailWhenParameterTypeIsNull() {
    // Given
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    when(packageValidator.validatePackageName(any())).thenReturn(ValidationResult.success());

    EndpointParameter param = new EndpointParameter(
        "user",
        null,
        ParameterType.BODY);
    Endpoint endpoint = new Endpoint(
        "/users",
        HttpMethod.POST,
        "create",
        "User",
        List.of(param));
    InputAdapterConfig config = InputAdapterConfig.builder()
        .name("UserController")
        .useCaseName("CreateUserUseCase")
        .type(InputAdapterType.REST)
        .packageName("com.test.infrastructure.entrypoints.rest")
        .endpoints(List.of(endpoint))
        .build();

    // When
    ValidationResult result = validator.validate(projectPath, config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("Parameter type is required")));
  }

  @Test
  void shouldFailWhenParameterTypeEnumIsNull() {
    // Given
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    when(packageValidator.validatePackageName(any())).thenReturn(ValidationResult.success());

    EndpointParameter param = new EndpointParameter(
        "user",
        "User",
        null);
    Endpoint endpoint = new Endpoint(
        "/users",
        HttpMethod.POST,
        "create",
        "User",
        List.of(param));
    InputAdapterConfig config = InputAdapterConfig.builder()
        .name("UserController")
        .useCaseName("CreateUserUseCase")
        .type(InputAdapterType.REST)
        .packageName("com.test.infrastructure.entrypoints.rest")
        .endpoints(List.of(endpoint))
        .build();

    // When
    ValidationResult result = validator.validate(projectPath, config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("Parameter type (PATH/BODY/QUERY) is required")));
  }
}
