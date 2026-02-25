package com.pragma.archetype.domain.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pragma.archetype.domain.model.ValidationResult;
import com.pragma.archetype.domain.model.entity.EntityConfig;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.port.out.FileSystemPort;

@ExtendWith(MockitoExtension.class)
class EntityValidatorTest {

  @Mock
  private FileSystemPort fileSystemPort;

  @Mock
  private ConfigurationPort configurationPort;

  private EntityValidator validator;

  @BeforeEach
  void setUp() {
    validator = new EntityValidator(fileSystemPort, configurationPort);
  }

  @Test
  void shouldValidateSuccessfully() {
    // Given
    when(fileSystemPort.exists(any())).thenReturn(true);

    EntityConfig config = EntityConfig.builder()
        .name("User")
        .packageName("com.test.domain.model")
        .hasId(true)
        .idType("String")
        .fields(List.of(
            new EntityConfig.EntityField("name", "String", false),
            new EntityConfig.EntityField("email", "String", false)))
        .build();

    // When
    ValidationResult result = validator.validate(Path.of("/test"), config);

    // Then
    assertTrue(result.valid());
    assertTrue(result.errors().isEmpty());
  }

  @Test
  void shouldFailWhenProjectNotInitialized() {
    // Given
    when(fileSystemPort.exists(any())).thenReturn(false);

    EntityConfig config = EntityConfig.builder()
        .name("User")
        .packageName("com.test.domain.model")
        .hasId(true)
        .idType("String")
        .fields(List.of(
            new EntityConfig.EntityField("name", "String", false)))
        .build();

    // When
    ValidationResult result = validator.validate(Path.of("/test"), config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("not initialized")));
  }

  @Test
  void shouldFailWithInvalidEntityName() {
    // Given
    when(fileSystemPort.exists(any())).thenReturn(true);

    EntityConfig config = EntityConfig.builder()
        .name("user") // lowercase - invalid
        .packageName("com.test.domain.model")
        .hasId(true)
        .idType("String")
        .fields(List.of(
            new EntityConfig.EntityField("name", "String", false)))
        .build();

    // When
    ValidationResult result = validator.validate(Path.of("/test"), config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("valid Java class name")));
  }

  @Test
  void shouldFailWithEmptyFields() {
    // Given
    when(fileSystemPort.exists(any())).thenReturn(true);

    EntityConfig config = EntityConfig.builder()
        .name("User")
        .packageName("com.test.domain.model")
        .hasId(true)
        .idType("String")
        .fields(List.of())
        .build();

    // When
    ValidationResult result = validator.validate(Path.of("/test"), config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("at least one field")));
  }

  @Test
  void shouldFailWithInvalidFieldName() {
    // Given
    when(fileSystemPort.exists(any())).thenReturn(true);

    EntityConfig config = EntityConfig.builder()
        .name("User")
        .packageName("com.test.domain.model")
        .hasId(true)
        .idType("String")
        .fields(List.of(
            new EntityConfig.EntityField("Name", "String", false))) // uppercase - invalid
        .build();

    // When
    ValidationResult result = validator.validate(Path.of("/test"), config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("camelCase")));
  }

  @Test
  void shouldFailWithInvalidIdType() {
    // Given
    when(fileSystemPort.exists(any())).thenReturn(true);

    EntityConfig config = EntityConfig.builder()
        .name("User")
        .packageName("com.test.domain.model")
        .hasId(true)
        .idType("Integer") // invalid
        .fields(List.of(
            new EntityConfig.EntityField("name", "String", false)))
        .build();

    // When
    ValidationResult result = validator.validate(Path.of("/test"), config);

    // Then
    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(e -> e.contains("Invalid ID type")));
  }

  @Test
  void shouldValidateWithUUIDIdType() {
    // Given
    when(fileSystemPort.exists(any())).thenReturn(true);

    EntityConfig config = EntityConfig.builder()
        .name("Product")
        .packageName("com.test.domain.model")
        .hasId(true)
        .idType("UUID")
        .fields(List.of(
            new EntityConfig.EntityField("name", "String", false)))
        .build();

    // When
    ValidationResult result = validator.validate(Path.of("/test"), config);

    // Then
    assertTrue(result.valid());
  }

  @Test
  void shouldValidateWithLongIdType() {
    // Given
    when(fileSystemPort.exists(any())).thenReturn(true);

    EntityConfig config = EntityConfig.builder()
        .name("Order")
        .packageName("com.test.domain.model")
        .hasId(true)
        .idType("Long")
        .fields(List.of(
            new EntityConfig.EntityField("total", "BigDecimal", false)))
        .build();

    // When
    ValidationResult result = validator.validate(Path.of("/test"), config);

    // Then
    assertTrue(result.valid());
  }
}
