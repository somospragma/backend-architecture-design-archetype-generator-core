package com.pragma.archetype.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class AdapterConfigTest {

  @Test
  void shouldBuildAdapterConfigWithAllFields() {
    // Given
    AdapterConfig.AdapterMethod method = new AdapterConfig.AdapterMethod(
        "findById",
        "Mono<User>",
        List.of(new AdapterConfig.MethodParameter("id", "String")));

    // When
    AdapterConfig config = AdapterConfig.builder()
        .name("UserRepository")
        .packageName("com.test.infrastructure.redis")
        .type(AdapterConfig.AdapterType.REDIS)
        .entityName("User")
        .methods(List.of(method))
        .build();

    // Then
    assertEquals("UserRepository", config.name());
    assertEquals("com.test.infrastructure.redis", config.packageName());
    assertEquals(AdapterConfig.AdapterType.REDIS, config.type());
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
        .type(AdapterConfig.AdapterType.REDIS)
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
    List<AdapterConfig.MethodParameter> params = List.of(
        new AdapterConfig.MethodParameter("id", "String"),
        new AdapterConfig.MethodParameter("status", "Status"));

    // When
    AdapterConfig.AdapterMethod method = new AdapterConfig.AdapterMethod(
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
    AdapterConfig.MethodParameter param = new AdapterConfig.MethodParameter("email", "String");

    // Then
    assertEquals("email", param.name());
    assertEquals("String", param.type());
  }

  @Test
  void shouldSupportAllAdapterTypes() {
    assertNotNull(AdapterConfig.AdapterType.REDIS);
    assertNotNull(AdapterConfig.AdapterType.MONGODB);
    assertNotNull(AdapterConfig.AdapterType.POSTGRESQL);
    assertNotNull(AdapterConfig.AdapterType.REST_CLIENT);
    assertNotNull(AdapterConfig.AdapterType.KAFKA);
  }

  @Test
  void shouldBuildConfigForEachAdapterType() {
    for (AdapterConfig.AdapterType type : AdapterConfig.AdapterType.values()) {
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
