package com.pragma.archetype.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;

class InputAdapterConfigTest {

  @Test
  void shouldBuildInputAdapterConfigWithAllFields() {
    // Given
    InputAdapterConfig.Endpoint endpoint = new InputAdapterConfig.Endpoint(
        "/users/{id}",
        InputAdapterConfig.HttpMethod.GET,
        "findById",
        "Mono<UserResponse>",
        List.of(new InputAdapterConfig.EndpointParameter("id", "String", InputAdapterConfig.ParameterType.PATH)));

    // When
    InputAdapterConfig config = InputAdapterConfig.builder()
        .name("UserController")
        .packageName("com.test.infrastructure.adapter.in.rest")
        .type(InputAdapterConfig.InputAdapterType.REST)
        .useCaseName("UserUseCase")
        .endpoints(List.of(endpoint))
        .build();

    // Then
    assertEquals("UserController", config.name());
    assertEquals("com.test.infrastructure.adapter.in.rest", config.packageName());
    assertEquals(InputAdapterConfig.InputAdapterType.REST, config.type());
    assertEquals("UserUseCase", config.useCaseName());
    assertEquals(1, config.endpoints().size());
  }

  @Test
  void shouldSupportAllInputAdapterTypes() {
    assertNotNull(InputAdapterConfig.InputAdapterType.REST);
    assertNotNull(InputAdapterConfig.InputAdapterType.GRAPHQL);
    assertNotNull(InputAdapterConfig.InputAdapterType.GRPC);
    assertNotNull(InputAdapterConfig.InputAdapterType.WEBSOCKET);
  }

  @Test
  void shouldSupportAllHttpMethods() {
    assertNotNull(InputAdapterConfig.HttpMethod.GET);
    assertNotNull(InputAdapterConfig.HttpMethod.POST);
    assertNotNull(InputAdapterConfig.HttpMethod.PUT);
    assertNotNull(InputAdapterConfig.HttpMethod.DELETE);
    assertNotNull(InputAdapterConfig.HttpMethod.PATCH);
  }

  @Test
  void shouldSupportAllParameterTypes() {
    assertNotNull(InputAdapterConfig.ParameterType.PATH);
    assertNotNull(InputAdapterConfig.ParameterType.BODY);
    assertNotNull(InputAdapterConfig.ParameterType.QUERY);
  }

  @Test
  void shouldCreateEndpointWithPathParameter() {
    // When
    InputAdapterConfig.Endpoint endpoint = new InputAdapterConfig.Endpoint(
        "/users/{id}",
        InputAdapterConfig.HttpMethod.GET,
        "findById",
        "Mono<User>",
        List.of(new InputAdapterConfig.EndpointParameter("id", "String", InputAdapterConfig.ParameterType.PATH)));

    // Then
    assertEquals("/users/{id}", endpoint.path());
    assertEquals(InputAdapterConfig.HttpMethod.GET, endpoint.method());
    assertEquals("findById", endpoint.useCaseMethod());
    assertEquals(1, endpoint.parameters().size());
    assertEquals(InputAdapterConfig.ParameterType.PATH, endpoint.parameters().get(0).paramType());
  }

  @Test
  void shouldCreateEndpointWithBodyParameter() {
    // When
    InputAdapterConfig.Endpoint endpoint = new InputAdapterConfig.Endpoint(
        "/users",
        InputAdapterConfig.HttpMethod.POST,
        "create",
        "Mono<User>",
        List.of(new InputAdapterConfig.EndpointParameter("request", "CreateUserRequest",
            InputAdapterConfig.ParameterType.BODY)));

    // Then
    assertEquals(InputAdapterConfig.HttpMethod.POST, endpoint.method());
    assertEquals(InputAdapterConfig.ParameterType.BODY, endpoint.parameters().get(0).paramType());
  }

  @Test
  void shouldCreateEndpointWithQueryParameter() {
    // When
    InputAdapterConfig.Endpoint endpoint = new InputAdapterConfig.Endpoint(
        "/users",
        InputAdapterConfig.HttpMethod.GET,
        "findByStatus",
        "Flux<User>",
        List.of(new InputAdapterConfig.EndpointParameter("status", "String", InputAdapterConfig.ParameterType.QUERY)));

    // Then
    assertEquals(InputAdapterConfig.ParameterType.QUERY, endpoint.parameters().get(0).paramType());
  }

  @Test
  void shouldSupportMultipleEndpoints() {
    // Given
    List<InputAdapterConfig.Endpoint> endpoints = List.of(
        new InputAdapterConfig.Endpoint("/users", InputAdapterConfig.HttpMethod.GET, "findAll", "Flux<User>",
            List.of()),
        new InputAdapterConfig.Endpoint("/users/{id}", InputAdapterConfig.HttpMethod.GET, "findById", "Mono<User>",
            List.of()),
        new InputAdapterConfig.Endpoint("/users", InputAdapterConfig.HttpMethod.POST, "create", "Mono<User>",
            List.of()));

    // When
    InputAdapterConfig config = InputAdapterConfig.builder()
        .name("UserController")
        .packageName("com.test")
        .type(InputAdapterConfig.InputAdapterType.REST)
        .useCaseName("UserUseCase")
        .endpoints(endpoints)
        .build();

    // Then
    assertEquals(3, config.endpoints().size());
  }

  @Test
  void shouldBuildGraphQLAdapter() {
    // When
    InputAdapterConfig config = InputAdapterConfig.builder()
        .name("UserResolver")
        .packageName("com.test.infrastructure.adapter.in.graphql")
        .type(InputAdapterConfig.InputAdapterType.GRAPHQL)
        .useCaseName("UserUseCase")
        .endpoints(List.of())
        .build();

    // Then
    assertEquals(InputAdapterConfig.InputAdapterType.GRAPHQL, config.type());
  }

  @Test
  void shouldBuildGrpcAdapter() {
    // When
    InputAdapterConfig config = InputAdapterConfig.builder()
        .name("UserService")
        .packageName("com.test.infrastructure.adapter.in.grpc")
        .type(InputAdapterConfig.InputAdapterType.GRPC)
        .useCaseName("UserUseCase")
        .endpoints(List.of())
        .build();

    // Then
    assertEquals(InputAdapterConfig.InputAdapterType.GRPC, config.type());
  }

  @Test
  void shouldBuildWebSocketAdapter() {
    // When
    InputAdapterConfig config = InputAdapterConfig.builder()
        .name("UserWebSocketHandler")
        .packageName("com.test.infrastructure.adapter.in.websocket")
        .type(InputAdapterConfig.InputAdapterType.WEBSOCKET)
        .useCaseName("UserUseCase")
        .endpoints(List.of())
        .build();

    // Then
    assertEquals(InputAdapterConfig.InputAdapterType.WEBSOCKET, config.type());
  }
}
