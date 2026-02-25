package com.pragma.archetype.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.pragma.archetype.domain.model.adapter.Endpoint;
import com.pragma.archetype.domain.model.adapter.EndpointParameter;
import com.pragma.archetype.domain.model.adapter.HttpMethod;
import com.pragma.archetype.domain.model.adapter.InputAdapterConfig;
import com.pragma.archetype.domain.model.adapter.InputAdapterType;
import com.pragma.archetype.domain.model.adapter.ParameterType;

class InputAdapterConfigTest {

  @Test
  void shouldBuildInputAdapterConfigWithAllFields() {
    // Given
    Endpoint endpoint = new Endpoint(
        "/users/{id}",
        HttpMethod.GET,
        "findById",
        "Mono<UserResponse>",
        List.of(new EndpointParameter("id", "String", ParameterType.PATH)));

    // When
    InputAdapterConfig config = InputAdapterConfig.builder()
        .name("UserController")
        .packageName("com.test.infrastructure.adapter.in.rest")
        .type(InputAdapterType.REST)
        .useCaseName("UserUseCase")
        .endpoints(List.of(endpoint))
        .build();

    // Then
    assertEquals("UserController", config.name());
    assertEquals("com.test.infrastructure.adapter.in.rest", config.packageName());
    assertEquals(InputAdapterType.REST, config.type());
    assertEquals("UserUseCase", config.useCaseName());
    assertEquals(1, config.endpoints().size());
  }

  @Test
  void shouldSupportAllInputAdapterTypes() {
    assertNotNull(InputAdapterType.REST);
    assertNotNull(InputAdapterType.GRAPHQL);
    assertNotNull(InputAdapterType.GRPC);
    assertNotNull(InputAdapterType.WEBSOCKET);
  }

  @Test
  void shouldSupportAllHttpMethods() {
    assertNotNull(HttpMethod.GET);
    assertNotNull(HttpMethod.POST);
    assertNotNull(HttpMethod.PUT);
    assertNotNull(HttpMethod.DELETE);
    assertNotNull(HttpMethod.PATCH);
  }

  @Test
  void shouldSupportAllParameterTypes() {
    assertNotNull(ParameterType.PATH);
    assertNotNull(ParameterType.BODY);
    assertNotNull(ParameterType.QUERY);
  }

  @Test
  void shouldCreateEndpointWithPathParameter() {
    // When
    Endpoint endpoint = new Endpoint(
        "/users/{id}",
        HttpMethod.GET,
        "findById",
        "Mono<User>",
        List.of(new EndpointParameter("id", "String", ParameterType.PATH)));

    // Then
    assertEquals("/users/{id}", endpoint.path());
    assertEquals(HttpMethod.GET, endpoint.method());
    assertEquals("findById", endpoint.useCaseMethod());
    assertEquals(1, endpoint.parameters().size());
    assertEquals(ParameterType.PATH, endpoint.parameters().get(0).paramType());
  }

  @Test
  void shouldCreateEndpointWithBodyParameter() {
    // When
    Endpoint endpoint = new Endpoint(
        "/users",
        HttpMethod.POST,
        "create",
        "Mono<User>",
        List.of(new EndpointParameter("request", "CreateUserRequest",
            ParameterType.BODY)));

    // Then
    assertEquals(HttpMethod.POST, endpoint.method());
    assertEquals(ParameterType.BODY, endpoint.parameters().get(0).paramType());
  }

  @Test
  void shouldCreateEndpointWithQueryParameter() {
    // When
    Endpoint endpoint = new Endpoint(
        "/users",
        HttpMethod.GET,
        "findByStatus",
        "Flux<User>",
        List.of(new EndpointParameter("status", "String", ParameterType.QUERY)));

    // Then
    assertEquals(ParameterType.QUERY, endpoint.parameters().get(0).paramType());
  }

  @Test
  void shouldSupportMultipleEndpoints() {
    // Given
    List<Endpoint> endpoints = List.of(
        new Endpoint("/users", HttpMethod.GET, "findAll", "Flux<User>",
            List.of()),
        new Endpoint("/users/{id}", HttpMethod.GET, "findById", "Mono<User>",
            List.of()),
        new Endpoint("/users", HttpMethod.POST, "create", "Mono<User>",
            List.of()));

    // When
    InputAdapterConfig config = InputAdapterConfig.builder()
        .name("UserController")
        .packageName("com.test")
        .type(InputAdapterType.REST)
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
        .type(InputAdapterType.GRAPHQL)
        .useCaseName("UserUseCase")
        .endpoints(List.of())
        .build();

    // Then
    assertEquals(InputAdapterType.GRAPHQL, config.type());
  }

  @Test
  void shouldBuildGrpcAdapter() {
    // When
    InputAdapterConfig config = InputAdapterConfig.builder()
        .name("UserService")
        .packageName("com.test.infrastructure.adapter.in.grpc")
        .type(InputAdapterType.GRPC)
        .useCaseName("UserUseCase")
        .endpoints(List.of())
        .build();

    // Then
    assertEquals(InputAdapterType.GRPC, config.type());
  }

  @Test
  void shouldBuildWebSocketAdapter() {
    // When
    InputAdapterConfig config = InputAdapterConfig.builder()
        .name("UserWebSocketHandler")
        .packageName("com.test.infrastructure.adapter.in.websocket")
        .type(InputAdapterType.WEBSOCKET)
        .useCaseName("UserUseCase")
        .endpoints(List.of())
        .build();

    // Then
    assertEquals(InputAdapterType.WEBSOCKET, config.type());
  }
}
