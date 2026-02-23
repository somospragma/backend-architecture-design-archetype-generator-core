package com.pragma.archetype.domain.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pragma.archetype.domain.model.AdapterConfig;
import com.pragma.archetype.domain.model.ValidationResult;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.port.out.FileSystemPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdapterValidator Tests")
class AdapterValidatorTest {

  @Mock
  private FileSystemPort fileSystemPort;

  @Mock
  private ConfigurationPort configurationPort;

  private AdapterValidator validator;
  private Path projectPath;
  private AdapterConfig validConfig;

  @BeforeEach
  void setUp() {
    PackageValidator packageValidator = new PackageValidator();
    validator = new AdapterValidator(fileSystemPort, configurationPort, packageValidator);
    projectPath = Paths.get("/test/project");

    validConfig = AdapterConfig.builder()
        .name("MongoDB")
        .packageName("com.company.infrastructure.driven.adapters.mongodb")
        .type(AdapterConfig.AdapterType.MONGODB)
        .entityName("User")
        .methods(List.of())
        .build();
  }

  @Test
  @DisplayName("Should validate successfully when adapter does not exist")
  void shouldValidateSuccessfullyWhenAdapterDoesNotExist() {
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    Path expectedAdapterPath = projectPath
        .resolve("src/main/java/com/company/infrastructure/driven/adapters/mongodb/MongoDBAdapter.java");
    when(fileSystemPort.exists(expectedAdapterPath)).thenReturn(false);

    ValidationResult result = validator.validate(projectPath, validConfig);

    assertTrue(result.valid());
    assertTrue(result.errors().isEmpty());
    verify(fileSystemPort).exists(expectedAdapterPath);
  }

  @Test
  @DisplayName("Should fail when adapter already exists")
  void shouldFailWhenAdapterAlreadyExists() {
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    Path expectedAdapterPath = projectPath
        .resolve("src/main/java/com/company/infrastructure/driven/adapters/mongodb/MongoDBAdapter.java");
    when(fileSystemPort.exists(expectedAdapterPath)).thenReturn(true);

    ValidationResult result = validator.validate(projectPath, validConfig);

    assertFalse(result.valid());
    assertTrue(result.errors().stream()
        .anyMatch(error -> error.contains("Adapter already exists")));
    assertTrue(result.errors().stream()
        .anyMatch(error -> error.contains("MongoDBAdapter.java")));
    assertTrue(result.errors().stream()
        .anyMatch(error -> error.contains("Please use a different name or remove the existing adapter")));
    verify(fileSystemPort).exists(expectedAdapterPath);
  }

  @Test
  @DisplayName("Should fail when project directory does not exist")
  void shouldFailWhenProjectDirectoryDoesNotExist() {
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(false);

    ValidationResult result = validator.validate(projectPath, validConfig);

    assertFalse(result.valid());
    assertTrue(result.errors().stream()
        .anyMatch(error -> error.contains("does not exist")));
  }

  @Test
  @DisplayName("Should fail when adapter name is blank")
  void shouldFailWhenAdapterNameIsBlank() {
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    AdapterConfig invalidConfig = AdapterConfig.builder()
        .name("")
        .packageName("com.company.infrastructure.driven.adapters.mongodb")
        .type(AdapterConfig.AdapterType.MONGODB)
        .entityName("User")
        .methods(List.of())
        .build();

    ValidationResult result = validator.validate(projectPath, invalidConfig);

    assertFalse(result.valid());
    assertTrue(result.errors().stream()
        .anyMatch(error -> error.contains("Adapter name is required")));
  }

  @Test
  @DisplayName("Should fail when adapter name is not a valid Java identifier")
  void shouldFailWhenAdapterNameIsNotValidJavaIdentifier() {
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    AdapterConfig invalidConfig = AdapterConfig.builder()
        .name("Mongo-DB")
        .packageName("com.company.infrastructure.driven.adapters.mongodb")
        .type(AdapterConfig.AdapterType.MONGODB)
        .entityName("User")
        .methods(List.of())
        .build();

    ValidationResult result = validator.validate(projectPath, invalidConfig);

    assertFalse(result.valid());
    assertTrue(result.errors().stream()
        .anyMatch(error -> error.contains("must be a valid Java identifier")));
  }

  @Test
  @DisplayName("Should fail when package name is blank")
  void shouldFailWhenPackageNameIsBlank() {
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    AdapterConfig invalidConfig = AdapterConfig.builder()
        .name("MongoDB")
        .packageName("")
        .type(AdapterConfig.AdapterType.MONGODB)
        .entityName("User")
        .methods(List.of())
        .build();

    ValidationResult result = validator.validate(projectPath, invalidConfig);

    assertFalse(result.valid());
    assertTrue(result.errors().stream()
        .anyMatch(error -> error.contains("Package name is required")));
  }

  @Test
  @DisplayName("Should fail when package name is invalid")
  void shouldFailWhenPackageNameIsInvalid() {
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    AdapterConfig invalidConfig = AdapterConfig.builder()
        .name("MongoDB")
        .packageName("com.company.123invalid")
        .type(AdapterConfig.AdapterType.MONGODB)
        .entityName("User")
        .methods(List.of())
        .build();

    ValidationResult result = validator.validate(projectPath, invalidConfig);

    assertFalse(result.valid());
    assertTrue(result.errors().stream()
        .anyMatch(error -> error.contains("invalid characters") || error.contains("Package segment")));
  }

  @Test
  @DisplayName("Should not check for duplicate when adapter name is blank")
  void shouldNotCheckForDuplicateWhenAdapterNameIsBlank() {
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    AdapterConfig invalidConfig = AdapterConfig.builder()
        .name("")
        .packageName("com.company.infrastructure.driven.adapters.mongodb")
        .type(AdapterConfig.AdapterType.MONGODB)
        .entityName("User")
        .methods(List.of())
        .build();

    ValidationResult result = validator.validate(projectPath, invalidConfig);

    assertFalse(result.valid());
    verify(fileSystemPort, never()).exists(any(Path.class));
  }

  @Test
  @DisplayName("Should not check for duplicate when package name is blank")
  void shouldNotCheckForDuplicateWhenPackageNameIsBlank() {
    when(fileSystemPort.directoryExists(projectPath)).thenReturn(true);
    AdapterConfig invalidConfig = AdapterConfig.builder()
        .name("MongoDB")
        .packageName("")
        .type(AdapterConfig.AdapterType.MONGODB)
        .entityName("User")
        .methods(List.of())
        .build();

    ValidationResult result = validator.validate(projectPath, invalidConfig);

    assertFalse(result.valid());
    verify(fileSystemPort, never()).exists(any(Path.class));
  }
}
