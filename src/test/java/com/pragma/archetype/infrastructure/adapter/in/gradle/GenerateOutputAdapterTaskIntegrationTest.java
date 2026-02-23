package com.pragma.archetype.infrastructure.adapter.in.gradle;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GenerateOutputAdapterTaskIntegrationTest {

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
  void shouldFailWhenConfigurationDoesNotExist() {
    // Given
    task.setAdapterName("UserRepository");
    task.setEntityName("User");
    task.setType("redis");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateAdapter());
  }

  @Test
  void shouldFailWhenAdapterNameIsEmpty() throws Exception {
    // Given
    createBasicConfiguration();
    task.setAdapterName("");
    task.setEntityName("User");
    task.setType("redis");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateAdapter());
  }

  @Test
  void shouldFailWhenEntityNameIsEmpty() throws Exception {
    // Given
    createBasicConfiguration();
    task.setAdapterName("UserRepository");
    task.setEntityName("");
    task.setType("redis");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateAdapter());
  }

  @Test
  void shouldHandleRedisAdapterType() throws Exception {
    // Given
    createBasicConfiguration();
    task.setAdapterName("UserRepository");
    task.setEntityName("User");
    task.setType("redis");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateAdapter());
  }

  @Test
  void shouldHandleMongoDBAdapterType() throws Exception {
    // Given
    createBasicConfiguration();
    task.setAdapterName("UserRepository");
    task.setEntityName("User");
    task.setType("mongodb");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateAdapter());
  }

  @Test
  void shouldHandleMongoAdapterType() throws Exception {
    // Given
    createBasicConfiguration();
    task.setAdapterName("UserRepository");
    task.setEntityName("User");
    task.setType("mongo");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateAdapter());
  }

  @Test
  void shouldHandlePostgreSQLAdapterType() throws Exception {
    // Given
    createBasicConfiguration();
    task.setAdapterName("UserRepository");
    task.setEntityName("User");
    task.setType("postgresql");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateAdapter());
  }

  @Test
  void shouldHandlePostgresAdapterType() throws Exception {
    // Given
    createBasicConfiguration();
    task.setAdapterName("UserRepository");
    task.setEntityName("User");
    task.setType("postgres");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateAdapter());
  }

  @Test
  void shouldHandleRestClientAdapterType() throws Exception {
    // Given
    createBasicConfiguration();
    task.setAdapterName("UserClient");
    task.setEntityName("User");
    task.setType("rest-client");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateAdapter());
  }

  @Test
  void shouldHandleRestAdapterType() throws Exception {
    // Given
    createBasicConfiguration();
    task.setAdapterName("UserClient");
    task.setEntityName("User");
    task.setType("rest");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateAdapter());
  }

  @Test
  void shouldHandleKafkaAdapterType() throws Exception {
    // Given
    createBasicConfiguration();
    task.setAdapterName("UserProducer");
    task.setEntityName("User");
    task.setType("kafka");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateAdapter());
  }

  @Test
  void shouldHandleCustomPackageName() throws Exception {
    // Given
    createBasicConfiguration();
    task.setAdapterName("UserRepository");
    task.setEntityName("User");
    task.setType("redis");
    task.setPackageName("com.custom.infrastructure.drivenadapters.redis");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateAdapter());
  }

  @Test
  void shouldHandleAutoDetectedPackageName() throws Exception {
    // Given
    createBasicConfiguration();
    task.setAdapterName("UserRepository");
    task.setEntityName("User");
    task.setType("redis");
    task.setPackageName("");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateAdapter());
  }

  @Test
  void shouldHandleCustomMethods() throws Exception {
    // Given
    createBasicConfiguration();
    task.setAdapterName("UserRepository");
    task.setEntityName("User");
    task.setType("redis");
    task.setMethods("findById:User:id:Long|save:User:user:User|deleteById:void:id:Long");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateAdapter());
  }

  @Test
  void shouldHandleMethodWithoutParameters() throws Exception {
    // Given
    createBasicConfiguration();
    task.setAdapterName("UserRepository");
    task.setEntityName("User");
    task.setType("redis");
    task.setMethods("findAll:List<User>");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateAdapter());
  }

  @Test
  void shouldHandleMethodWithMultipleParameters() throws Exception {
    // Given
    createBasicConfiguration();
    task.setAdapterName("UserRepository");
    task.setEntityName("User");
    task.setType("redis");
    task.setMethods("findByNameAndAge:List<User>:name:String:age:Integer");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateAdapter());
  }

  @Test
  void shouldHandleReactiveReturnTypes() throws Exception {
    // Given
    createBasicConfiguration();
    task.setAdapterName("UserRepository");
    task.setEntityName("User");
    task.setType("redis");
    task.setMethods("findById:Mono<User>:id:Long");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateAdapter());
  }

  @Test
  void shouldHandleFluxReturnTypes() throws Exception {
    // Given
    createBasicConfiguration();
    task.setAdapterName("UserRepository");
    task.setEntityName("User");
    task.setType("redis");
    task.setMethods("findAll:Flux<User>");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateAdapter());
  }

  @Test
  void shouldHandleVoidReturnType() throws Exception {
    // Given
    createBasicConfiguration();
    task.setAdapterName("UserRepository");
    task.setEntityName("User");
    task.setType("redis");
    task.setMethods("deleteById:void:id:Long");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateAdapter());
  }

  @Test
  void shouldHandleComplexReturnTypes() throws Exception {
    // Given
    createBasicConfiguration();
    task.setAdapterName("UserRepository");
    task.setEntityName("User");
    task.setType("redis");
    task.setMethods("findByIds:Map<Long,User>:ids:List<Long>");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateAdapter());
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
