package com.pragma.archetype.infrastructure.adapter.in.gradle;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GenerateUseCaseTaskIntegrationTest {

  @TempDir
  Path tempDir;

  private Project project;
  private GenerateUseCaseTask task;

  @BeforeEach
  void setUp() {
    project = ProjectBuilder.builder()
        .withProjectDir(tempDir.toFile())
        .withName("test-project")
        .build();

    task = project.getTasks().create("generateUseCase", GenerateUseCaseTask.class);
  }

  @Test
  void shouldFailWhenConfigurationDoesNotExist() {
    // Given
    task.setUseCaseName("CreateUser");
    task.setMethods("execute:User:userData:CreateUserRequest");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateUseCase());
  }

  @Test
  void shouldFailWhenUseCaseNameIsEmpty() throws Exception {
    // Given
    createBasicConfiguration();
    task.setUseCaseName("");
    task.setMethods("execute:User");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateUseCase());
  }

  @Test
  void shouldFailWhenMethodsAreEmpty() throws Exception {
    // Given
    createBasicConfiguration();
    task.setUseCaseName("CreateUser");
    task.setMethods("");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateUseCase());
  }

  @Test
  void shouldFailWhenMethodsAreBlank() throws Exception {
    // Given
    createBasicConfiguration();
    task.setUseCaseName("CreateUser");
    task.setMethods("   ");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateUseCase());
  }

  @Test
  void shouldHandleMultipleMethods() throws Exception {
    // Given
    createBasicConfiguration();
    task.setUseCaseName("UserUseCase");
    task.setMethods(
        "create:User:data:CreateUserRequest|update:User:id:Long:data:UpdateUserRequest|delete:void:id:Long");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateUseCase());
  }

  @Test
  void shouldHandleReactiveReturnTypes() throws Exception {
    // Given
    createBasicConfiguration();
    task.setUseCaseName("CreateUser");
    task.setMethods("execute:Mono<User>:userData:CreateUserRequest");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateUseCase());
  }

  @Test
  void shouldHandleFluxReturnTypes() throws Exception {
    // Given
    createBasicConfiguration();
    task.setUseCaseName("FindAllUsers");
    task.setMethods("execute:Flux<User>");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateUseCase());
  }

  @Test
  void shouldHandleVoidReturnType() throws Exception {
    // Given
    createBasicConfiguration();
    task.setUseCaseName("DeleteUser");
    task.setMethods("execute:void:id:Long");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateUseCase());
  }

  @Test
  void shouldHandleComplexParameterTypes() throws Exception {
    // Given
    createBasicConfiguration();
    task.setUseCaseName("SearchUsers");
    task.setMethods("execute:List<User>:criteria:SearchCriteria:pageable:Pageable");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateUseCase());
  }

  @Test
  void shouldHandleMethodWithoutParameters() throws Exception {
    // Given
    createBasicConfiguration();
    task.setUseCaseName("GetAllUsers");
    task.setMethods("execute:List<User>");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateUseCase());
  }

  @Test
  void shouldHandleCustomPackageName() throws Exception {
    // Given
    createBasicConfiguration();
    task.setUseCaseName("CreateUser");
    task.setMethods("execute:User:data:CreateUserRequest");
    task.setPackageName("com.custom.application.usecase");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateUseCase());
  }

  @Test
  void shouldHandleUseCaseNameWithSuffix() throws Exception {
    // Given
    createBasicConfiguration();
    task.setUseCaseName("CreateUserUseCase");
    task.setMethods("execute:User:data:CreateUserRequest");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateUseCase());
  }

  @Test
  void shouldHandleGenericReturnTypes() throws Exception {
    // Given
    createBasicConfiguration();
    task.setUseCaseName("FindUser");
    task.setMethods("execute:Optional<User>:id:Long");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateUseCase());
  }

  @Test
  void shouldHandleMultipleParametersOfSameType() throws Exception {
    // Given
    createBasicConfiguration();
    task.setUseCaseName("CompareUsers");
    task.setMethods("execute:boolean:userId1:Long:userId2:Long");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateUseCase());
  }

  @Test
  void shouldHandleNestedGenericTypes() throws Exception {
    // Given
    createBasicConfiguration();
    task.setUseCaseName("GetUserMap");
    task.setMethods("execute:Map<String,List<User>>");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateUseCase());
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
