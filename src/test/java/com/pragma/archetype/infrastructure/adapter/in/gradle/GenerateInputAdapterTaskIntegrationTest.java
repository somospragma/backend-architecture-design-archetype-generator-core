package com.pragma.archetype.infrastructure.adapter.in.gradle;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GenerateInputAdapterTaskIntegrationTest {

  @TempDir
  Path tempDir;

  private Project project;
  private GenerateInputAdapterTask task;

  @BeforeEach
  void setUp() {
    project = ProjectBuilder.builder()
        .withProjectDir(tempDir.toFile())
        .withName("test-project")
        .build();

    task = project.getTasks().create("generateInputAdapter", GenerateInputAdapterTask.class);
  }

  @Test
  void shouldFailWhenConfigurationDoesNotExist() {
    // Given
    task.setAdapterName("UserController");
    task.setUseCaseName("CreateUserUseCase");
    task.setEndpoints("/users:POST:create:User:userData:BODY:CreateUserRequest");
    task.setPackageName("com.test.infrastructure.entrypoints.rest");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateInputAdapter());
  }

  @Test
  void shouldFailWhenAdapterNameIsEmpty() throws Exception {
    // Given
    createBasicConfiguration();
    task.setAdapterName("");
    task.setUseCaseName("CreateUserUseCase");
    task.setEndpoints("/users:POST:create:User");
    task.setPackageName("com.test.infrastructure.entrypoints.rest");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateInputAdapter());
  }

  @Test
  void shouldFailWhenUseCaseNameIsEmpty() throws Exception {
    // Given
    createBasicConfiguration();
    task.setAdapterName("UserController");
    task.setUseCaseName("");
    task.setEndpoints("/users:POST:create:User");
    task.setPackageName("com.test.infrastructure.entrypoints.rest");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateInputAdapter());
  }

  @Test
  void shouldFailWhenEndpointsAreEmpty() throws Exception {
    // Given
    createBasicConfiguration();
    task.setAdapterName("UserController");
    task.setUseCaseName("CreateUserUseCase");
    task.setEndpoints("");
    task.setPackageName("com.test.infrastructure.entrypoints.rest");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateInputAdapter());
  }

  @Test
  void shouldFailWhenPackageNameIsEmpty() throws Exception {
    // Given
    createBasicConfiguration();
    task.setAdapterName("UserController");
    task.setUseCaseName("CreateUserUseCase");
    task.setEndpoints("/users:POST:create:User");
    task.setPackageName("");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateInputAdapter());
  }

  @Test
  void shouldHandleMultipleEndpoints() throws Exception {
    // Given
    createBasicConfiguration();
    task.setAdapterName("UserController");
    task.setUseCaseName("UserUseCase");
    task.setEndpoints(
        "/users:POST:create:User:data:BODY:CreateUserRequest|/users/{id}:GET:findById:User:id:PATH:Long|/users/{id}:DELETE:delete:void:id:PATH:Long");
    task.setPackageName("com.test.infrastructure.entrypoints.rest");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateInputAdapter());
  }

  @Test
  void shouldHandlePathVariables() throws Exception {
    // Given
    createBasicConfiguration();
    task.setAdapterName("UserController");
    task.setUseCaseName("FindUserUseCase");
    task.setEndpoints("/users/{id}:GET:findById:User:id:PATH:Long");
    task.setPackageName("com.test.infrastructure.entrypoints.rest");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateInputAdapter());
  }

  @Test
  void shouldHandleQueryParameters() throws Exception {
    // Given
    createBasicConfiguration();
    task.setAdapterName("UserController");
    task.setUseCaseName("SearchUsersUseCase");
    task.setEndpoints("/users:GET:search:List<User>:query:QUERY:String:page:QUERY:Integer");
    task.setPackageName("com.test.infrastructure.entrypoints.rest");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateInputAdapter());
  }

  @Test
  void shouldHandleBodyParameters() throws Exception {
    // Given
    createBasicConfiguration();
    task.setAdapterName("UserController");
    task.setUseCaseName("CreateUserUseCase");
    task.setEndpoints("/users:POST:create:User:userData:BODY:CreateUserRequest");
    task.setPackageName("com.test.infrastructure.entrypoints.rest");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateInputAdapter());
  }

  @Test
  void shouldHandleMixedParameterTypes() throws Exception {
    // Given
    createBasicConfiguration();
    task.setAdapterName("UserController");
    task.setUseCaseName("UpdateUserUseCase");
    task.setEndpoints("/users/{id}:PUT:update:User:id:PATH:Long:userData:BODY:UpdateUserRequest");
    task.setPackageName("com.test.infrastructure.entrypoints.rest");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateInputAdapter());
  }

  @Test
  void shouldHandleRestAdapterType() throws Exception {
    // Given
    createBasicConfiguration();
    task.setAdapterName("UserController");
    task.setUseCaseName("CreateUserUseCase");
    task.setEndpoints("/users:POST:create:User");
    task.setPackageName("com.test.infrastructure.entrypoints.rest");
    task.setType("rest");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateInputAdapter());
  }

  @Test
  void shouldHandleGraphQLAdapterType() throws Exception {
    // Given
    createBasicConfiguration();
    task.setAdapterName("UserResolver");
    task.setUseCaseName("CreateUserUseCase");
    task.setEndpoints("/users:POST:create:User");
    task.setPackageName("com.test.infrastructure.entrypoints.graphql");
    task.setType("graphql");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateInputAdapter());
  }

  @Test
  void shouldHandleGrpcAdapterType() throws Exception {
    // Given
    createBasicConfiguration();
    task.setAdapterName("UserService");
    task.setUseCaseName("CreateUserUseCase");
    task.setEndpoints("/users:POST:create:User");
    task.setPackageName("com.test.infrastructure.entrypoints.grpc");
    task.setType("grpc");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateInputAdapter());
  }

  @Test
  void shouldHandleWebSocketAdapterType() throws Exception {
    // Given
    createBasicConfiguration();
    task.setAdapterName("UserWebSocket");
    task.setUseCaseName("CreateUserUseCase");
    task.setEndpoints("/users:POST:create:User");
    task.setPackageName("com.test.infrastructure.entrypoints.websocket");
    task.setType("websocket");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateInputAdapter());
  }

  @Test
  void shouldHandleReactiveReturnTypes() throws Exception {
    // Given
    createBasicConfiguration();
    task.setAdapterName("UserController");
    task.setUseCaseName("CreateUserUseCase");
    task.setEndpoints("/users:POST:create:Mono<User>:userData:BODY:CreateUserRequest");
    task.setPackageName("com.test.infrastructure.entrypoints.rest");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateInputAdapter());
  }

  @Test
  void shouldHandleFluxReturnTypes() throws Exception {
    // Given
    createBasicConfiguration();
    task.setAdapterName("UserController");
    task.setUseCaseName("FindAllUsersUseCase");
    task.setEndpoints("/users:GET:findAll:Flux<User>");
    task.setPackageName("com.test.infrastructure.entrypoints.rest");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateInputAdapter());
  }

  private void createBasicConfiguration() throws Exception {
    String yamlContent = """
        project:
          name: test-project
          basePackage: com.test
          architecture: hexagonal-single
          paradigm: reactive
          framework: spring
          pluginVersion: 1.0.0
          createdAt: 2024-01-01T00:00:00
        architecture:
          type: hexagonal-single
          paradigm: reactive
          framework: spring
          adaptersAsModules: false
        """;
    Files.writeString(tempDir.resolve(".cleanarch.yml"), yamlContent);
  }
}
