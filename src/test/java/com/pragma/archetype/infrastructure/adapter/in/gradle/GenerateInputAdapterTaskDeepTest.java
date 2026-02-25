package com.pragma.archetype.infrastructure.adapter.in.gradle;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.pragma.archetype.domain.model.adapter.InputAdapterConfig;

class GenerateInputAdapterTaskDeepTest {

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
  void testParseEndpoints_singleEndpoint() throws Exception {
    // Given
    String endpointsStr = "/users:POST:create:User:name:String,email:String";

    // When
    List<InputAdapterConfig.Endpoint> endpoints = invokeParseEndpoints(endpointsStr);

    // Then
    assertNotNull(endpoints);
    assertEquals(1, endpoints.size());
  }

  @Test
  void testParseEndpoints_multipleEndpoints() throws Exception {
    // Given
    String endpointsStr = "/users:POST:create:User:name:String|/users/{id}:GET:findById:User:id:Long|/users/{id}:DELETE:delete:void:id:Long";

    // When
    List<InputAdapterConfig.Endpoint> endpoints = invokeParseEndpoints(endpointsStr);

    // Then
    assertEquals(3, endpoints.size());
  }

  @Test
  void testParseEndpoints_withPathVariables() throws Exception {
    // Given
    String endpointsStr = "/users/{id}:GET:findById:User:id:Long";

    // When
    List<InputAdapterConfig.Endpoint> endpoints = invokeParseEndpoints(endpointsStr);

    // Then
    assertEquals(1, endpoints.size());
  }

  @Test
  void testParseEndpoints_withQueryParams() throws Exception {
    // Given
    String endpointsStr = "/users:GET:search:List<User>:query:String,page:Integer";

    // When
    List<InputAdapterConfig.Endpoint> endpoints = invokeParseEndpoints(endpointsStr);

    // Then
    assertEquals(1, endpoints.size());
  }

  @Test
  void testParseEndpoints_emptyString() throws Exception {
    // Given
    String endpointsStr = "";

    // When
    List<InputAdapterConfig.Endpoint> endpoints = invokeParseEndpoints(endpointsStr);

    // Then
    assertTrue(endpoints.isEmpty());
  }

  @Test
  void testParseAdapterType_rest() throws Exception {
    // Given
    task.setType("rest");

    // When
    Object adapterType = invokeParseAdapterType("rest");

    // Then
    assertNotNull(adapterType);
  }

  @Test
  void testParseAdapterType_graphql() throws Exception {
    // When
    Object adapterType = invokeParseAdapterType("graphql");

    // Then
    assertNotNull(adapterType);
  }

  @Test
  void testParseAdapterType_grpc() throws Exception {
    // When
    Object adapterType = invokeParseAdapterType("grpc");

    // Then
    assertNotNull(adapterType);
  }

  @Test
  void testValidateInputs_validInputs() throws Exception {
    // Given
    task.setAdapterName("UserController");
    task.setUseCaseName("CreateUserUseCase");
    task.setEndpoints("/users:POST:create:User:name:String");

    // When/Then
    assertDoesNotThrow(() -> invokeValidateInputs());
  }

  @Test
  void testValidateInputs_emptyAdapterName() {
    // Given
    task.setAdapterName("");
    task.setUseCaseName("CreateUserUseCase");

    // When/Then
    assertThrows(Exception.class, () -> invokeValidateInputs());
  }

  @Test
  void testValidateInputs_blankAdapterName() {
    // Given
    task.setAdapterName("   ");
    task.setUseCaseName("CreateUserUseCase");

    // When/Then
    assertThrows(Exception.class, () -> invokeValidateInputs());
  }

  @Test
  void testValidateInputs_emptyUseCaseName() {
    // Given
    task.setAdapterName("UserController");
    task.setUseCaseName("");

    // When/Then
    assertThrows(Exception.class, () -> invokeValidateInputs());
  }

  @Test
  void testValidateInputs_blankUseCaseName() {
    // Given
    task.setAdapterName("UserController");
    task.setUseCaseName("   ");

    // When/Then
    assertThrows(Exception.class, () -> invokeValidateInputs());
  }

  @Test
  void testCreateTemplateRepository() throws Exception {
    // When
    Object repository = invokeCreateTemplateRepository();

    // Then
    assertNotNull(repository);
  }

  @Test
  void testCreateTemplateRepository_withConfiguration() throws Exception {
    // Given
    createBasicConfiguration();

    // When
    Object repository = invokeCreateTemplateRepository();

    // Then
    assertNotNull(repository);
  }

  // Helper methods using reflection

  @SuppressWarnings("unchecked")
  private List<InputAdapterConfig.Endpoint> invokeParseEndpoints(String endpointsStr) throws Exception {
    Method method = GenerateInputAdapterTask.class.getDeclaredMethod("parseEndpoints", String.class);
    method.setAccessible(true);
    return (List<InputAdapterConfig.Endpoint>) method.invoke(task, endpointsStr);
  }

  private Object invokeParseAdapterType(String typeStr) throws Exception {
    Method method = GenerateInputAdapterTask.class.getDeclaredMethod("parseAdapterType", String.class);
    method.setAccessible(true);
    return method.invoke(task, typeStr);
  }

  private void invokeValidateInputs() throws Exception {
    Method method = GenerateInputAdapterTask.class.getDeclaredMethod("validateInputs");
    method.setAccessible(true);
    method.invoke(task);
  }

  private Object invokeCreateTemplateRepository() throws Exception {
    Method method = GenerateInputAdapterTask.class.getDeclaredMethod("createTemplateRepository");
    method.setAccessible(true);
    return method.invoke(task);
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
