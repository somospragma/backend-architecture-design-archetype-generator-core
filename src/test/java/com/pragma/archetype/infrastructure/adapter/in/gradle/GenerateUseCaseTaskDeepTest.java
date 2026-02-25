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

import com.pragma.archetype.domain.model.usecase.UseCaseConfig;

class GenerateUseCaseTaskDeepTest {

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
  void testParseMethods_singleMethod() throws Exception {
    // Given
    String methodsStr = "execute:User:name:String,email:String";

    // When
    List<UseCaseConfig.UseCaseMethod> methods = invokeParseMethods(methodsStr);

    // Then
    assertNotNull(methods);
    assertEquals(1, methods.size());
    assertEquals("execute", methods.get(0).name());
    assertEquals("User", methods.get(0).returnType());
  }

  @Test
  void testParseMethods_multipleMethods() throws Exception {
    // Given
    String methodsStr = "create:User:name:String|update:User:id:Long,name:String|delete:void:id:Long";

    // When
    List<UseCaseConfig.UseCaseMethod> methods = invokeParseMethods(methodsStr);

    // Then
    assertEquals(3, methods.size());
    assertEquals("create", methods.get(0).name());
    assertEquals("update", methods.get(1).name());
    assertEquals("delete", methods.get(2).name());
  }

  @Test
  void testParseMethods_voidReturnType() throws Exception {
    // Given
    String methodsStr = "execute:void:userId:String";

    // When
    List<UseCaseConfig.UseCaseMethod> methods = invokeParseMethods(methodsStr);

    // Then
    assertEquals(1, methods.size());
    assertEquals("void", methods.get(0).returnType());
  }

  @Test
  void testParseMethods_noParameters() throws Exception {
    // Given
    String methodsStr = "execute:List<User>";

    // When
    List<UseCaseConfig.UseCaseMethod> methods = invokeParseMethods(methodsStr);

    // Then
    assertEquals(1, methods.size());
    assertEquals("execute", methods.get(0).name());
    assertEquals("List<User>", methods.get(0).returnType());
  }

  @Test
  void testParseMethods_reactiveTypes() throws Exception {
    // Given
    String methodsStr = "execute:Mono<User>:id:String|stream:Flux<Event>:filter:String";

    // When
    List<UseCaseConfig.UseCaseMethod> methods = invokeParseMethods(methodsStr);

    // Then
    assertEquals(2, methods.size());
    assertEquals("Mono<User>", methods.get(0).returnType());
    assertEquals("Flux<Event>", methods.get(1).returnType());
  }

  @Test
  void testParseMethods_withSpaces() throws Exception {
    // Given
    String methodsStr = " execute : User : name : String , email : String ";

    // When
    List<UseCaseConfig.UseCaseMethod> methods = invokeParseMethods(methodsStr);

    // Then
    assertEquals(1, methods.size());
    assertEquals("execute", methods.get(0).name());
  }

  @Test
  void testParseMethods_emptyString() throws Exception {
    // Given
    String methodsStr = "";

    // When
    List<UseCaseConfig.UseCaseMethod> methods = invokeParseMethods(methodsStr);

    // Then
    assertTrue(methods.isEmpty());
  }

  @Test
  void testResolvePackageName_withCustomPackage() throws Exception {
    // Given
    createBasicConfiguration();
    task.setPackageName("com.custom.usecase");

    // When
    String packageName = invokeResolvePackageName();

    // Then
    assertEquals("com.custom.usecase", packageName);
  }

  @Test
  void testResolvePackageName_withEmptyPackage() throws Exception {
    // Given
    createBasicConfiguration();
    task.setPackageName("");

    // When
    String packageName = invokeResolvePackageName();

    // Then
    assertNotNull(packageName);
  }

  @Test
  void testValidateInputs_validInputs() throws Exception {
    // Given
    task.setUseCaseName("CreateUser");
    task.setMethods("execute:User:name:String");

    // When/Then
    assertDoesNotThrow(() -> invokeValidateInputs());
  }

  @Test
  void testValidateInputs_emptyUseCaseName() {
    // Given
    task.setUseCaseName("");
    task.setMethods("execute:User:name:String");

    // When/Then
    assertThrows(Exception.class, () -> invokeValidateInputs());
  }

  @Test
  void testValidateInputs_blankUseCaseName() {
    // Given
    task.setUseCaseName("   ");
    task.setMethods("execute:User:name:String");

    // When/Then
    assertThrows(Exception.class, () -> invokeValidateInputs());
  }

  @Test
  void testValidateInputs_emptyMethods() {
    // Given
    task.setUseCaseName("CreateUser");
    task.setMethods("");

    // When/Then
    assertThrows(Exception.class, () -> invokeValidateInputs());
  }

  @Test
  void testValidateInputs_blankMethods() {
    // Given
    task.setUseCaseName("CreateUser");
    task.setMethods("   ");

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
  private List<UseCaseConfig.UseCaseMethod> invokeParseMethods(String methodsStr) throws Exception {
    Method method = GenerateUseCaseTask.class.getDeclaredMethod("parseMethods", String.class);
    method.setAccessible(true);
    return (List<UseCaseConfig.UseCaseMethod>) method.invoke(task, methodsStr);
  }

  private String invokeResolvePackageName() throws Exception {
    Method method = GenerateUseCaseTask.class.getDeclaredMethod("resolvePackageName");
    method.setAccessible(true);
    return (String) method.invoke(task);
  }

  private void invokeValidateInputs() throws Exception {
    Method method = GenerateUseCaseTask.class.getDeclaredMethod("validateInputs");
    method.setAccessible(true);
    method.invoke(task);
  }

  private Object invokeCreateTemplateRepository() throws Exception {
    Method method = GenerateUseCaseTask.class.getDeclaredMethod("createTemplateRepository");
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
