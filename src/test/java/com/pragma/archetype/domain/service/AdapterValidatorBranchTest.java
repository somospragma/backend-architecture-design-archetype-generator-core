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

import com.pragma.archetype.domain.model.AdapterConfig;
import com.pragma.archetype.domain.model.ValidationResult;
import com.pragma.archetype.domain.model.config.ProjectConfig;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.port.out.FileSystemPort;

@ExtendWith(MockitoExtension.class)
class AdapterValidatorBranchTest {

  @Mock
  private FileSystemPort fileSystemPort;

  @Mock
  private ConfigurationPort configurationPort;

  @Mock
  private PackageValidator packageValidator;

  private AdapterValidator validator;
  private Path projectPath;

  @BeforeEach
  void setUp() {
    validator = new AdapterValidator(fileSystemPort, configurationPort, packageValidator);
    projectPath = Path.of("/test/project");
  }

  @Test
  void shouldFailWhenProjectDirectoryDoesNotExist() {
    // Given
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(false);
    AdapterConfig config = AdapterConfig.builder()
        .name("UserRepository")
        .entityName("User")
        .type(AdapterConfig.AdapterType.REDIS)
        .packageName("com.test.infrastructure.drivenadapters.redis")
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
    AdapterConfig config = AdapterConfig.builder()
        .name(null)
        .entityName("User")
        .type(AdapterConfig.AdapterType.REDIS)
        .packageName("com.test.infrastructure.drivenadapters.redis")
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
    AdapterConfig config = AdapterConfig.builder()
        .name("123Invalid")
        .entityName("User")
        .type(AdapterConfig.AdapterType.REDIS)
        .packageName("com.test.infrastructure.drivenadapters.redis")
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
    AdapterConfig config = AdapterConfig.builder()
        .name("UserRepository")
        .entityName("User")
        .type(AdapterConfig.AdapterType.REDIS)
        .packageName(null)
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
    AdapterConfig config = AdapterConfig.builder()
        .name("UserRepository")
        .entityName("User")
        .type(AdapterConfig.AdapterType.REDIS)
        .packageName("com.test.adapter.out.redis")
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
    AdapterConfig config = AdapterConfig.builder()
        .name("UserController")
        .entityName("User")
        .type(AdapterConfig.AdapterType.REDIS)
        .packageName("com.test.adapter.in.rest")
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

    AdapterConfig config = AdapterConfig.builder()
        .name("UserRepository")
        .entityName("User")
        .type(AdapterConfig.AdapterType.REDIS)
        .packageName("com.wrong.infrastructure.drivenadapters.redis")
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
    AdapterConfig config = AdapterConfig.builder()
        .name("UserRepository")
        .entityName("User")
        .type(null)
        .packageName("com.test.infrastructure.drivenadapters.redis")
        .build();

    // When
    ValidationResult result = validator.validate(projectPath, config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("Adapter type is required")));
  }

  @Test
  void shouldFailWhenEntityNameIsNull() {
    // Given
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    when(packageValidator.validatePackageName(any())).thenReturn(ValidationResult.success());
    AdapterConfig config = AdapterConfig.builder()
        .name("UserRepository")
        .entityName(null)
        .type(AdapterConfig.AdapterType.REDIS)
        .packageName("com.test.infrastructure.drivenadapters.redis")
        .build();

    // When
    ValidationResult result = validator.validate(projectPath, config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("Entity name is required")));
  }

  @Test
  void shouldFailWhenMethodNameIsNull() {
    // Given
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    when(packageValidator.validatePackageName(any())).thenReturn(ValidationResult.success());

    AdapterConfig.AdapterMethod method = new AdapterConfig.AdapterMethod(null, "User", List.of());
    AdapterConfig config = AdapterConfig.builder()
        .name("UserRepository")
        .entityName("User")
        .type(AdapterConfig.AdapterType.REDIS)
        .packageName("com.test.infrastructure.drivenadapters.redis")
        .methods(List.of(method))
        .build();

    // When
    ValidationResult result = validator.validate(projectPath, config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("Method name is required")));
  }

  @Test
  void shouldFailWhenMethodNameIsInvalidIdentifier() {
    // Given
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    when(packageValidator.validatePackageName(any())).thenReturn(ValidationResult.success());

    AdapterConfig.AdapterMethod method = new AdapterConfig.AdapterMethod("123invalid", "User", List.of());
    AdapterConfig config = AdapterConfig.builder()
        .name("UserRepository")
        .entityName("User")
        .type(AdapterConfig.AdapterType.REDIS)
        .packageName("com.test.infrastructure.drivenadapters.redis")
        .methods(List.of(method))
        .build();

    // When
    ValidationResult result = validator.validate(projectPath, config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("Invalid method name")));
  }

  @Test
  void shouldFailWhenMethodReturnTypeIsNull() {
    // Given
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    when(packageValidator.validatePackageName(any())).thenReturn(ValidationResult.success());

    AdapterConfig.AdapterMethod method = new AdapterConfig.AdapterMethod("findById", null, List.of());
    AdapterConfig config = AdapterConfig.builder()
        .name("UserRepository")
        .entityName("User")
        .type(AdapterConfig.AdapterType.REDIS)
        .packageName("com.test.infrastructure.drivenadapters.redis")
        .methods(List.of(method))
        .build();

    // When
    ValidationResult result = validator.validate(projectPath, config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("Method return type is required")));
  }

  @Test
  void shouldFailWhenParameterNameIsNull() {
    // Given
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    when(packageValidator.validatePackageName(any())).thenReturn(ValidationResult.success());

    AdapterConfig.MethodParameter param = new AdapterConfig.MethodParameter(null, "Long");
    AdapterConfig.AdapterMethod method = new AdapterConfig.AdapterMethod("findById", "User", List.of(param));
    AdapterConfig config = AdapterConfig.builder()
        .name("UserRepository")
        .entityName("User")
        .type(AdapterConfig.AdapterType.REDIS)
        .packageName("com.test.infrastructure.drivenadapters.redis")
        .methods(List.of(method))
        .build();

    // When
    ValidationResult result = validator.validate(projectPath, config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("Parameter name is required")));
  }

  @Test
  void shouldFailWhenParameterNameIsInvalidIdentifier() {
    // Given
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    when(packageValidator.validatePackageName(any())).thenReturn(ValidationResult.success());

    AdapterConfig.MethodParameter param = new AdapterConfig.MethodParameter("123invalid", "Long");
    AdapterConfig.AdapterMethod method = new AdapterConfig.AdapterMethod("findById", "User", List.of(param));
    AdapterConfig config = AdapterConfig.builder()
        .name("UserRepository")
        .entityName("User")
        .type(AdapterConfig.AdapterType.REDIS)
        .packageName("com.test.infrastructure.drivenadapters.redis")
        .methods(List.of(method))
        .build();

    // When
    ValidationResult result = validator.validate(projectPath, config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("Invalid parameter name")));
  }

  @Test
  void shouldFailWhenParameterTypeIsNull() {
    // Given
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    when(packageValidator.validatePackageName(any())).thenReturn(ValidationResult.success());

    AdapterConfig.MethodParameter param = new AdapterConfig.MethodParameter("id", null);
    AdapterConfig.AdapterMethod method = new AdapterConfig.AdapterMethod("findById", "User", List.of(param));
    AdapterConfig config = AdapterConfig.builder()
        .name("UserRepository")
        .entityName("User")
        .type(AdapterConfig.AdapterType.REDIS)
        .packageName("com.test.infrastructure.drivenadapters.redis")
        .methods(List.of(method))
        .build();

    // When
    ValidationResult result = validator.validate(projectPath, config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("Parameter type is required")));
  }

  @Test
  void shouldFailWhenAdapterAlreadyExists() {
    // Given
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    when(packageValidator.validatePackageName(any())).thenReturn(ValidationResult.success());
    when(fileSystemPort.exists(any())).thenReturn(true);

    AdapterConfig config = AdapterConfig.builder()
        .name("UserRepository")
        .entityName("User")
        .type(AdapterConfig.AdapterType.REDIS)
        .packageName("com.test.infrastructure.drivenadapters.redis")
        .build();

    // When
    ValidationResult result = validator.validate(projectPath, config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("Adapter already exists")));
  }

  @Test
  void shouldPassWhenAllValidationsSucceed() {
    // Given
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    when(packageValidator.validatePackageName(any())).thenReturn(ValidationResult.success());
    when(fileSystemPort.exists(any())).thenReturn(false);

    AdapterConfig.MethodParameter param = new AdapterConfig.MethodParameter("id", "Long");
    AdapterConfig.AdapterMethod method = new AdapterConfig.AdapterMethod("findById", "User", List.of(param));
    AdapterConfig config = AdapterConfig.builder()
        .name("UserRepository")
        .entityName("User")
        .type(AdapterConfig.AdapterType.REDIS)
        .packageName("com.test.infrastructure.drivenadapters.redis")
        .methods(List.of(method))
        .build();

    // When
    ValidationResult result = validator.validate(projectPath, config);

    // Then
    assertTrue(result.valid());
  }
}
