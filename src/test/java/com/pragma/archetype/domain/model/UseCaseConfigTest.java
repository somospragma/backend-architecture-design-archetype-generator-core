package com.pragma.archetype.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.pragma.archetype.domain.model.usecase.UseCaseConfig;

class UseCaseConfigTest {

  @Test
  void shouldBuildUseCaseConfigWithAllFields() {
    // Given
    UseCaseConfig.UseCaseMethod method = new UseCaseConfig.UseCaseMethod(
        "execute",
        "Mono<User>",
        List.of(new UseCaseConfig.MethodParameter("id", "String")));

    // When
    UseCaseConfig config = UseCaseConfig.builder()
        .name("GetUserUseCase")
        .packageName("com.test.application.usecase")
        .methods(List.of(method))
        .generatePort(true)
        .generateImpl(true)
        .build();

    // Then
    assertEquals("GetUserUseCase", config.name());
    assertEquals("com.test.application.usecase", config.packageName());
    assertEquals(1, config.methods().size());
    assertTrue(config.generatePort());
    assertTrue(config.generateImpl());
  }

  @Test
  void shouldBuildUseCaseConfigWithDefaultSettings() {
    // When
    UseCaseConfig config = UseCaseConfig.builder()
        .name("CreateUserUseCase")
        .packageName("com.test.application.usecase")
        .methods(List.of())
        .build();

    // Then
    assertTrue(config.generatePort());
    assertTrue(config.generateImpl());
  }

  @Test
  void shouldBuildUseCaseConfigWithoutPort() {
    // When
    UseCaseConfig config = UseCaseConfig.builder()
        .name("UpdateUserUseCase")
        .packageName("com.test.application.usecase")
        .methods(List.of())
        .generatePort(false)
        .build();

    // Then
    assertFalse(config.generatePort());
    assertTrue(config.generateImpl());
  }

  @Test
  void shouldBuildUseCaseConfigWithoutImpl() {
    // When
    UseCaseConfig config = UseCaseConfig.builder()
        .name("DeleteUserUseCase")
        .packageName("com.test.application.usecase")
        .methods(List.of())
        .generateImpl(false)
        .build();

    // Then
    assertTrue(config.generatePort());
    assertFalse(config.generateImpl());
  }

  @Test
  void shouldCreateUseCaseMethodWithParameters() {
    // Given
    List<UseCaseConfig.MethodParameter> params = List.of(
        new UseCaseConfig.MethodParameter("id", "String"),
        new UseCaseConfig.MethodParameter("status", "Status"));

    // When
    UseCaseConfig.UseCaseMethod method = new UseCaseConfig.UseCaseMethod(
        "updateStatus",
        "Mono<Void>",
        params);

    // Then
    assertEquals("updateStatus", method.name());
    assertEquals("Mono<Void>", method.returnType());
    assertEquals(2, method.parameters().size());
  }

  @Test
  void shouldCreateMethodParameter() {
    // When
    UseCaseConfig.MethodParameter param = new UseCaseConfig.MethodParameter("email", "String");

    // Then
    assertEquals("email", param.name());
    assertEquals("String", param.type());
  }

  @Test
  void shouldSupportMultipleMethods() {
    // Given
    List<UseCaseConfig.UseCaseMethod> methods = List.of(
        new UseCaseConfig.UseCaseMethod("findById", "Mono<User>", List.of()),
        new UseCaseConfig.UseCaseMethod("findAll", "Flux<User>", List.of()),
        new UseCaseConfig.UseCaseMethod("save", "Mono<User>", List.of()));

    // When
    UseCaseConfig config = UseCaseConfig.builder()
        .name("UserUseCase")
        .packageName("com.test.application.usecase")
        .methods(methods)
        .build();

    // Then
    assertEquals(3, config.methods().size());
    assertEquals("findById", config.methods().get(0).name());
    assertEquals("findAll", config.methods().get(1).name());
    assertEquals("save", config.methods().get(2).name());
  }
}
