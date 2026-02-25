package com.pragma.archetype.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.pragma.archetype.domain.model.entity.EntityConfig;

class EntityConfigTest {

  @Test
  void shouldBuildEntityConfigWithAllFields() {
    // Given
    EntityConfig.EntityField field1 = new EntityConfig.EntityField("name", "String", false);
    EntityConfig.EntityField field2 = new EntityConfig.EntityField("email", "String", true);

    // When
    EntityConfig config = EntityConfig.builder()
        .name("User")
        .fields(List.of(field1, field2))
        .hasId(true)
        .idType("UUID")
        .packageName("com.test.domain.model")
        .build();

    // Then
    assertEquals("User", config.name());
    assertEquals(2, config.fields().size());
    assertTrue(config.hasId());
    assertEquals("UUID", config.idType());
    assertEquals("com.test.domain.model", config.packageName());
  }

  @Test
  void shouldBuildEntityConfigWithDefaultIdSettings() {
    // When
    EntityConfig config = EntityConfig.builder()
        .name("Product")
        .fields(List.of())
        .packageName("com.test.domain.model")
        .build();

    // Then
    assertTrue(config.hasId());
    assertEquals("String", config.idType());
  }

  @Test
  void shouldBuildEntityConfigWithoutId() {
    // When
    EntityConfig config = EntityConfig.builder()
        .name("ValueObject")
        .fields(List.of())
        .hasId(false)
        .packageName("com.test.domain.model")
        .build();

    // Then
    assertFalse(config.hasId());
  }

  @Test
  void shouldCreateEntityFieldWithNullable() {
    // When
    EntityConfig.EntityField field = new EntityConfig.EntityField("description", "String", true);

    // Then
    assertEquals("description", field.name());
    assertEquals("String", field.type());
    assertTrue(field.nullable());
  }

  @Test
  void shouldCreateEntityFieldWithNonNullable() {
    // When
    EntityConfig.EntityField field = new EntityConfig.EntityField("id", "Long", false);

    // Then
    assertEquals("id", field.name());
    assertEquals("Long", field.type());
    assertFalse(field.nullable());
  }

  @Test
  void shouldSupportDifferentIdTypes() {
    String[] idTypes = { "String", "Long", "UUID", "Integer" };

    for (String idType : idTypes) {
      EntityConfig config = EntityConfig.builder()
          .name("Entity")
          .fields(List.of())
          .idType(idType)
          .packageName("com.test")
          .build();

      assertEquals(idType, config.idType());
    }
  }

  @Test
  void shouldSupportMultipleFields() {
    // Given
    List<EntityConfig.EntityField> fields = List.of(
        new EntityConfig.EntityField("name", "String", false),
        new EntityConfig.EntityField("age", "Integer", false),
        new EntityConfig.EntityField("email", "String", true),
        new EntityConfig.EntityField("createdAt", "LocalDateTime", false));

    // When
    EntityConfig config = EntityConfig.builder()
        .name("User")
        .fields(fields)
        .packageName("com.test.domain.model")
        .build();

    // Then
    assertEquals(4, config.fields().size());
    assertEquals("name", config.fields().get(0).name());
    assertEquals("age", config.fields().get(1).name());
    assertEquals("email", config.fields().get(2).name());
    assertEquals("createdAt", config.fields().get(3).name());
  }
}
