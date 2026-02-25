package com.pragma.archetype.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.pragma.archetype.domain.model.adapter.AdapterConfig;
import com.pragma.archetype.domain.model.adapter.AdapterMethod;
import com.pragma.archetype.domain.model.adapter.AdapterType;
import com.pragma.archetype.domain.model.adapter.MethodParameter;

class AdapterConfigTest {

  @Test
  void shouldBuildAdapterConfigWithAllFields() {
    // Given
    AdapterMethod method = new AdapterMethod(
        "findById",
        "Mono<User>",
        List.of(new MethodParameter("id", "String")));

    // When
    AdapterConfig config = AdapterConfig.builder()
        .name("UserRepository")
        .packageName("com.test.infrastructure.redis")
        .type(AdapterType.REDIS)
        .entityName("User")
        .methods(List.of(method))
        .build();

    // Then
    assertEquals("UserRepository", config.name());
    assertEquals("com.test.infrastructure.redis", config.packageName());
    assertEquals(AdapterType.REDIS, config.type());
    assertEquals("User", config.entityName());
    assertEquals(1, config.methods().size());
    assertEquals("findById", config.methods().get(0).name());
  }

  @Test
  void shouldBuildAdapterConfigWithEmptyMethods() {
    // When
    AdapterConfig config = AdapterConfig.builder()
        .name("UserRepository")
        .packageName("com.test.infrastructure.redis")
        .type(AdapterType.REDIS)
        .entityName("User")
        .methods(List.of())
        .build();

    // Then
    assertNotNull(config.methods());
    assertTrue(config.methods().isEmpty());
  }

  @Test
  void shouldCreateAdapterMethodWithParameters() {
    // Given
    List<MethodParameter> params = List.of(
        new MethodParameter("id", "String"),
        new MethodParameter("status", "Status"));

    // When
    AdapterMethod method = new AdapterMethod(
        "findByIdAndStatus",
        "Mono<User>",
        params);

    // Then
    assertEquals("findByIdAndStatus", method.name());
    assertEquals("Mono<User>", method.returnType());
    assertEquals(2, method.parameters().size());
    assertEquals("id", method.parameters().get(0).name());
    assertEquals("String", method.parameters().get(0).type());
  }

  @Test
  void shouldCreateMethodParameter() {
    // When
    MethodParameter param = new MethodParameter("email", "String");

    // Then
    assertEquals("email", param.name());
    assertEquals("String", param.type());
  }

  @Test
  void shouldSupportAllAdapterTypes() {
    assertNotNull(AdapterType.REDIS);
    assertNotNull(AdapterType.MONGODB);
    assertNotNull(AdapterType.POSTGRESQL);
    assertNotNull(AdapterType.REST_CLIENT);
    assertNotNull(AdapterType.KAFKA);
  }

  @Test
  void shouldBuildConfigForEachAdapterType() {
    for (AdapterType type : AdapterType.values()) {
      AdapterConfig config = AdapterConfig.builder()
          .name("TestAdapter")
          .packageName("com.test")
          .type(type)
          .entityName("Entity")
          .methods(List.of())
          .build();

      assertEquals(type, config.type());
    }
  }
}
