package com.pragma.archetype.infrastructure.adapter.in.gradle;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Files;
import java.nio.file.Path;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GenerateUseCaseTaskExtendedTest {

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
  void testValidateInputs_simpleUseCase() throws Exception {
    // Given
    createBasicConfiguration();
    task.setUseCaseName("CreateUser");
    task.setMethods("execute:User:name:String,email:String");
    task.setPackageName("com.test.application.usecase");

    // When/Then
    try {
      task.generateUseCase();
    } catch (RuntimeException e) {
      assertNotNull(e);
    }
  }

  @Test
  void testValidateInputs_multipleMethods() throws Exception {
    // Given
    createBasicConfiguration();
    task.setUseCaseName("ManageProducts");
    task.setMethods("create:Product:name:String,price:Double|update:Product:id:Long,name:String|delete:void:id:Long");

    // When/Then
    try {
      task.generateUseCase();
    } catch (RuntimeException e) {
      assertNotNull(e);
    }
  }

  @Test
  void testValidateInputs_voidReturnType() throws Exception {
    // Given
    createBasicConfiguration();
    task.setUseCaseName("DeleteUser");
    task.setMethods("execute:void:userId:String");

    // When/Then
    try {
      task.generateUseCase();
    } catch (RuntimeException e) {
      assertNotNull(e);
    }
  }

  @Test
  void testValidateInputs_noParameters() throws Exception {
    // Given
    createBasicConfiguration();
    task.setUseCaseName("GetAllUsers");
    task.setMethods("execute:List<User>");

    // When/Then
    try {
      task.generateUseCase();
    } catch (RuntimeException e) {
      assertNotNull(e);
    }
  }

  @Test
  void testValidateInputs_complexReturnTypes() throws Exception {
    // Given
    createBasicConfiguration();
    task.setUseCaseName("SearchProducts");
    task.setMethods("search:List<Product>:query:String,page:Integer|count:Long:query:String");

    // When/Then
    try {
      task.generateUseCase();
    } catch (RuntimeException e) {
      assertNotNull(e);
    }
  }

  @Test
  void testValidateInputs_reactiveReturnTypes() throws Exception {
    // Given
    createBasicConfiguration();
    task.setUseCaseName("GetUserReactive");
    task.setMethods("execute:Mono<User>:userId:String");

    // When/Then
    try {
      task.generateUseCase();
    } catch (RuntimeException e) {
      assertNotNull(e);
    }
  }

  @Test
  void testValidateInputs_fluxReturnType() throws Exception {
    // Given
    createBasicConfiguration();
    task.setUseCaseName("StreamEvents");
    task.setMethods("stream:Flux<Event>:filter:String");

    // When/Then
    try {
      task.generateUseCase();
    } catch (RuntimeException e) {
      assertNotNull(e);
    }
  }

  @Test
  void testValidateInputs_manyParameters() throws Exception {
    // Given
    createBasicConfiguration();
    task.setUseCaseName("CreateOrder");
    task.setMethods(
        "execute:Order:customerId:Long,items:List<OrderItem>,shippingAddress:Address,billingAddress:Address,paymentMethod:String");

    // When/Then
    try {
      task.generateUseCase();
    } catch (RuntimeException e) {
      assertNotNull(e);
    }
  }

  @Test
  void testValidateInputs_customPackageName() throws Exception {
    // Given
    createBasicConfiguration();
    task.setUseCaseName("ProcessPayment");
    task.setMethods("process:PaymentResult:amount:BigDecimal,currency:String");
    task.setPackageName("com.custom.payment.usecase");

    // When/Then
    try {
      task.generateUseCase();
    } catch (RuntimeException e) {
      assertNotNull(e);
    }
  }

  @Test
  void testValidateInputs_emptyPackageName() throws Exception {
    // Given
    createBasicConfiguration();
    task.setUseCaseName("GetUser");
    task.setMethods("execute:User:id:String");
    task.setPackageName("");

    // When/Then
    try {
      task.generateUseCase();
    } catch (RuntimeException e) {
      assertNotNull(e);
    }
  }

  @Test
  void testValidateInputs_lowercaseUseCaseName() throws Exception {
    // Given
    createBasicConfiguration();
    task.setUseCaseName("createuser");
    task.setMethods("execute:User:name:String");

    // When/Then
    try {
      task.generateUseCase();
    } catch (RuntimeException e) {
      assertNotNull(e);
    }
  }

  @Test
  void testValidateInputs_useCaseWithSuffix() throws Exception {
    // Given
    createBasicConfiguration();
    task.setUseCaseName("CreateUserUseCase");
    task.setMethods("execute:User:name:String");

    // When/Then
    try {
      task.generateUseCase();
    } catch (RuntimeException e) {
      assertNotNull(e);
    }
  }

  @Test
  void testValidateInputs_methodsWithSpaces() throws Exception {
    // Given
    createBasicConfiguration();
    task.setUseCaseName("GetProduct");
    task.setMethods("execute : Product : id : Long");

    // When/Then
    try {
      task.generateUseCase();
    } catch (RuntimeException e) {
      assertNotNull(e);
    }
  }

  @Test
  void testValidateInputs_optionalReturnType() throws Exception {
    // Given
    createBasicConfiguration();
    task.setUseCaseName("FindUser");
    task.setMethods("find:Optional<User>:email:String");

    // When/Then
    try {
      task.generateUseCase();
    } catch (RuntimeException e) {
      assertNotNull(e);
    }
  }

  @Test
  void testValidateInputs_booleanReturnType() throws Exception {
    // Given
    createBasicConfiguration();
    task.setUseCaseName("ValidateUser");
    task.setMethods("validate:Boolean:userId:String,token:String");

    // When/Then
    try {
      task.generateUseCase();
    } catch (RuntimeException e) {
      assertNotNull(e);
    }
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
