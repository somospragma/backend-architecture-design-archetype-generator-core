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

import com.pragma.archetype.domain.model.AdapterConfig;

class GenerateOutputAdapterTaskDeepTest {

  @TempDir
  Path tempDir;

  private Project project;
  private GenerateOutputAdapterTask task;

  @BeforeEach
  void setUp() {
    project = ProjectBuilder.builder()
        .withProjectDir(tempDir.toFile())
        .withName("test-project")
        .build();

    task = project.getTasks().create("generateOutputAdapter", GenerateOutputAdapterTask.class);
  }

  @Test
  void testParseMethods_singleMethod() throws Exception {
    // Given
    String methodsStr = "findById:User:id:Long";

    // When
    List<AdapterConfig.AdapterMethod> methods = invokeParseMethods(methodsStr);

    // Then
    assertNotNull(methods);
    assertEquals(1, methods.size());
  }

  @Test
  void testParseMethods_multipleMethods() throws Exception {
    // Given
    String methodsStr = "findById:User:id:Long|save:User:user:User|deleteById:void:id:Long";

    // When
    List<AdapterConfig.AdapterMethod> methods = invokeParseMethods(methodsStr);

    // Then
    assertEquals(3, methods.size());
  }

  @Test
  void testParseMethods_withoutParameters() throws Exception {
    // Given
    String methodsStr = "findAll:List<User>";

    // When
    List<AdapterConfig.AdapterMethod> methods = invokeParseMethods(methodsStr);

    // Then
    assertEquals(1, methods.size());
  }

  @Test
  void testParseMethods_withMultipleParameters() throws Exception {
    // Given
    String methodsStr = "findByNameAndAge:List<User>:name:String:age:Integer";

    // When
    List<AdapterConfig.AdapterMethod> methods = invokeParseMethods(methodsStr);

    // Then
    assertEquals(1, methods.size());
  }

  @Test
  void testParseAdapterType_redis() throws Exception {
    // When
    Object adapterType = invokeParseAdapterType("redis");

    // Then
    assertNotNull(adapterType);
    assertEquals(AdapterConfig.AdapterType.REDIS, adapterType);
  }

  @Test
  void testParseAdapterType_mongodb() throws Exception {
    // When
    Object adapterType = invokeParseAdapterType("mongodb");

    // Then
    assertEquals(AdapterConfig.AdapterType.MONGODB, adapterType);
  }

  @Test
  void testParseAdapterType_mongo() throws Exception {
    // When
    Object adapterType = invokeParseAdapterType("mongo");

    // Then
    assertEquals(AdapterConfig.AdapterType.MONGODB, adapterType);
  }

  @Test
  void testParseAdapterType_postgresql() throws Exception {
    // When
    Object adapterType = invokeParseAdapterType("postgresql");

    // Then
    assertEquals(AdapterConfig.AdapterType.POSTGRESQL, adapterType);
  }

  @Test
  void testParseAdapterType_postgres() throws Exception {
    // When
    Object adapterType = invokeParseAdapterType("postgres");

    // Then
    assertEquals(AdapterConfig.AdapterType.POSTGRESQL, adapterType);
  }

  @Test
  void testParseAdapterType_restClient() throws Exception {
    // When
    Object adapterType = invokeParseAdapterType("rest-client");

    // Then
    assertEquals(AdapterConfig.AdapterType.REST_CLIENT, adapterType);
  }

  @Test
  void testParseAdapterType_rest() throws Exception {
    // When
    Object adapterType = invokeParseAdapterType("rest");

    // Then
    assertEquals(AdapterConfig.AdapterType.REST_CLIENT, adapterType);
  }

  @Test
  void testParseAdapterType_kafka() throws Exception {
    // When
    Object adapterType = invokeParseAdapterType("kafka");

    // Then
    assertEquals(AdapterConfig.AdapterType.KAFKA, adapterType);
  }

  @Test
  void testResolvePackageName_withCustomPackage() throws Exception {
    // Given
    createBasicConfiguration();
    task.setPackageName("com.custom.adapter.out");

    // When
    String packageName = invokeResolvePackageName("redis");

    // Then
    assertEquals("com.custom.adapter.out", packageName);
  }

  @Test
  void testResolvePackageName_withEmptyPackage() throws Exception {
    // Given
    createBasicConfiguration();
    task.setPackageName("");

    // When
    String packageName = invokeResolvePackageName("redis");

    // Then
    assertNotNull(packageName);
    assertTrue(packageName.contains("infrastructure.drivenadapters.redis"));
  }

  @Test
  void testResolvePackageName_autoDetect() throws Exception {
    // Given
    createBasicConfiguration();

    // When
    String packageName = invokeResolvePackageName("mongodb");

    // Then
    assertNotNull(packageName);
    assertTrue(packageName.contains("infrastructure.drivenadapters.mongodb"));
  }

  @Test
  void testValidateInputs_validInputs() throws Exception {
    // Given
    task.setAdapterName("UserRepository");
    task.setEntityName("User");

    // When/Then
    assertDoesNotThrow(this::invokeValidateInputs);
  }

  @Test
  void testValidateInputs_emptyAdapterName() {
    // Given
    task.setAdapterName("");
    task.setEntityName("User");

    // When/Then
    assertThrows(Exception.class, this::invokeValidateInputs);
  }

  @Test
  void testValidateInputs_blankAdapterName() {
    // Given
    task.setAdapterName("   ");
    task.setEntityName("User");

    // When/Then
    assertThrows(Exception.class, this::invokeValidateInputs);
  }

  @Test
  void testValidateInputs_emptyEntityName() {
    // Given
    task.setAdapterName("UserRepository");
    task.setEntityName("");

    // When/Then
    assertThrows(Exception.class, this::invokeValidateInputs);
  }

  @Test
  void testValidateInputs_blankEntityName() {
    // Given
    task.setAdapterName("UserRepository");
    task.setEntityName("   ");

    // When/Then
    assertThrows(Exception.class, this::invokeValidateInputs);
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
  private List<AdapterConfig.AdapterMethod> invokeParseMethods(String methodsStr) throws Exception {
    Method method = GenerateOutputAdapterTask.class.getDeclaredMethod("parseMethods", String.class);
    method.setAccessible(true);
    return (List<AdapterConfig.AdapterMethod>) method.invoke(task, methodsStr);
  }

  private Object invokeParseAdapterType(String typeStr) throws Exception {
    Method method = GenerateOutputAdapterTask.class.getDeclaredMethod("parseAdapterType", String.class);
    method.setAccessible(true);
    return method.invoke(task, typeStr);
  }

  private String invokeResolvePackageName(String adapterType) throws Exception {
    Method method = GenerateOutputAdapterTask.class.getDeclaredMethod("resolvePackageName", String.class);
    method.setAccessible(true);
    return (String) method.invoke(task, adapterType);
  }

  private void invokeValidateInputs() throws Exception {
    Method method = GenerateOutputAdapterTask.class.getDeclaredMethod("validateInputs");
    method.setAccessible(true);
    method.invoke(task);
  }

  private Object invokeCreateTemplateRepository() throws Exception {
    Method method = GenerateOutputAdapterTask.class.getDeclaredMethod("createTemplateRepository");
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
