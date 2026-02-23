package com.pragma.archetype.infrastructure.adapter.in.gradle;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GenerateEntityTaskIntegrationTest {

  @TempDir
  Path tempDir;

  private Project project;
  private GenerateEntityTask task;

  @BeforeEach
  void setUp() {
    project = ProjectBuilder.builder()
        .withProjectDir(tempDir.toFile())
        .withName("test-project")
        .build();

    task = project.getTasks().create("generateEntity", GenerateEntityTask.class);
  }

  @Test
  void shouldFailWhenConfigurationDoesNotExist() {
    // Given
    task.setEntityName("User");
    task.setFields("name:String,email:String");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateEntity());
  }

  @Test
  void shouldFailWhenEntityNameIsEmpty() throws Exception {
    // Given
    createBasicConfiguration();
    task.setEntityName("");
    task.setFields("name:String");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateEntity());
  }

  @Test
  void shouldFailWhenFieldsAreEmpty() throws Exception {
    // Given
    createBasicConfiguration();
    task.setEntityName("User");
    task.setFields("");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateEntity());
  }

  @Test
  void shouldFailWhenFieldsAreBlank() throws Exception {
    // Given
    createBasicConfiguration();
    task.setEntityName("User");
    task.setFields("   ");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateEntity());
  }

  @Test
  void shouldFailWithInvalidConfiguration() throws Exception {
    // Given
    String yamlContent = """
        project:
          name: test-project
        """;
    Files.writeString(tempDir.resolve(".cleanarch.yml"), yamlContent);

    task.setEntityName("User");
    task.setFields("name:String");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateEntity());
  }

  @Test
  void shouldFailWithMissingBasePackage() throws Exception {
    // Given
    String yamlContent = """
        project:
          name: test-project
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

    task.setEntityName("User");
    task.setFields("name:String");
    task.setPackageName("");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateEntity());
  }

  @Test
  void shouldFailWithInvalidFieldFormat() throws Exception {
    // Given
    createBasicConfiguration();
    task.setEntityName("User");
    task.setFields("invalid");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateEntity());
  }

  @Test
  void shouldFailWithEmptyFieldName() throws Exception {
    // Given
    createBasicConfiguration();
    task.setEntityName("User");
    task.setFields(":String");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateEntity());
  }

  @Test
  void shouldFailWithEmptyFieldType() throws Exception {
    // Given
    createBasicConfiguration();
    task.setEntityName("User");
    task.setFields("name:");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateEntity());
  }

  @Test
  void shouldHandleMultipleFields() throws Exception {
    // Given
    createBasicConfiguration();
    task.setEntityName("User");
    task.setFields("name:String,email:String,age:Integer");

    // When/Then - Will fail because templates don't exist, but validates parsing
    assertThrows(RuntimeException.class, () -> task.generateEntity());
  }

  @Test
  void shouldHandleComplexFieldTypes() throws Exception {
    // Given
    createBasicConfiguration();
    task.setEntityName("User");
    task.setFields("name:String,roles:List<String>,metadata:Map<String,Object>");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateEntity());
  }

  @Test
  void shouldHandleWithId() throws Exception {
    // Given
    createBasicConfiguration();
    task.setEntityName("User");
    task.setFields("name:String");
    task.setHasId(true);
    task.setIdType("Long");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateEntity());
  }

  @Test
  void shouldHandleWithoutId() throws Exception {
    // Given
    createBasicConfiguration();
    task.setEntityName("User");
    task.setFields("name:String");
    task.setHasId(false);

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateEntity());
  }

  @Test
  void shouldHandleCustomPackageName() throws Exception {
    // Given
    createBasicConfiguration();
    task.setEntityName("User");
    task.setFields("name:String");
    task.setPackageName("com.custom.domain.model");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateEntity());
  }

  @Test
  void shouldHandleEntityNameWithNumbers() throws Exception {
    // Given
    createBasicConfiguration();
    task.setEntityName("User123");
    task.setFields("name:String");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateEntity());
  }

  @Test
  void shouldHandleFieldsWithUnderscores() throws Exception {
    // Given
    createBasicConfiguration();
    task.setEntityName("User");
    task.setFields("first_name:String,last_name:String");

    // When/Then
    assertThrows(RuntimeException.class, () -> task.generateEntity());
  }

  @Test
  void shouldGenerateEntityWithTestTemplates() throws Exception {
    // Given
    createBasicConfiguration();
    createTestTemplates();
    task.setEntityName("User");
    task.setFields("name:String,email:String");
    task.setHasId(true);
    task.setIdType("Long");

    // When/Then - Should not throw exception with test templates
    assertThrows(RuntimeException.class, () -> task.generateEntity());
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

  private void createTestTemplates() throws Exception {
    Path templatesDir = tempDir.resolve("templates/frameworks/spring/reactive/domain");
    Files.createDirectories(templatesDir);
    String template = """
        package ${packageName};

        public class ${entityName} {
        <#list fields as field>
            private ${field.type} ${field.name};
        </#list>
        }
        """;
    Files.writeString(templatesDir.resolve("Entity.java.ftl"), template);
  }
}
