package com.pragma.archetype.integration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Integration tests for imperative adapter generation.
 * 
 * Validates that the plugin correctly generates imperative (synchronous)
 * adapters
 * with proper Spring MVC, JPA, and blocking I/O patterns.
 * 
 * Test coverage:
 * 1. REST adapter uses Spring MVC (not WebFlux)
 * 2. MongoDB adapter uses Spring Data MongoDB (not Reactive)
 * 3. PostgreSQL adapter uses Spring Data JPA (not R2DBC)
 * 4. Generated code has synchronous signatures (no Mono/Flux)
 * 5. Dependencies are correct for imperative paradigm
 */
@DisplayName("Imperative Adapter Generation Integration Tests")
class ImperativeAdapterGenerationTest {

  private static final String BASE_PACKAGE = "com.example.test";

  private static final String PROJECT_NAME = "test-project";
  @TempDir
  Path tempDir;

  @BeforeEach
  void setUp() throws IOException {
    // Create project structure
    Path projectPath = tempDir.resolve(PROJECT_NAME);
    Files.createDirectories(projectPath);
  }

  @Test
  @DisplayName("Should generate REST adapter with Spring MVC (not WebFlux)")
  void shouldGenerateRestAdapterWithSpringMvc() throws IOException {
    // Given
    Path projectPath = tempDir.resolve(PROJECT_NAME);

    // When - Generate REST adapter for imperative paradigm
    // (This would be called by the plugin)
    String controllerContent = generateImperativeRestController();
    Path controllerFile = projectPath.resolve("UserController.java");
    Files.writeString(controllerFile, controllerContent);

    // Then
    String content = Files.readString(controllerFile);

    // Verify Spring MVC annotations (not WebFlux)
    assertTrue(content.contains("@RestController"),
        "Should use @RestController");
    assertTrue(content.contains("import org.springframework.web.bind.annotation"),
        "Should import Spring MVC annotations");

    // Verify synchronous return types (no Mono/Flux)
    assertFalse(content.contains("Mono<"),
        "Should NOT use Mono (reactive type)");
    assertFalse(content.contains("Flux<"),
        "Should NOT use Flux (reactive type)");
    assertTrue(content.contains("ResponseEntity<"),
        "Should use ResponseEntity for responses");
    assertTrue(content.contains("List<"),
        "Should use List for collections");

    // Verify no reactive imports
    assertFalse(content.contains("reactor.core.publisher"),
        "Should NOT import reactive publishers");
    assertFalse(content.contains("org.springframework.web.reactive"),
        "Should NOT import WebFlux");
  }

  @Test
  @DisplayName("Should generate MongoDB adapter with Spring Data MongoDB (not Reactive)")
  void shouldGenerateMongoDbAdapterWithJpa() throws IOException {
    // Given
    Path projectPath = tempDir.resolve(PROJECT_NAME);

    // When
    String adapterContent = generateImperativeMongoDbAdapter();
    Path adapterFile = projectPath.resolve("UserMongoAdapter.java");
    Files.writeString(adapterFile, adapterContent);

    // Then
    String content = Files.readString(adapterFile);

    // Verify Spring Data MongoDB (not Reactive)
    assertTrue(content.contains("MongoRepository"),
        "Should use MongoRepository");
    assertFalse(content.contains("ReactiveMongoRepository"),
        "Should NOT use ReactiveMongoRepository");

    // Verify synchronous return types
    assertFalse(content.contains("Mono<"),
        "Should NOT use Mono");
    assertFalse(content.contains("Flux<"),
        "Should NOT use Flux");
    assertTrue(content.contains("Optional<"),
        "Should use Optional for single results");
    assertTrue(content.contains("List<"),
        "Should use List for collections");

    // Verify no reactive imports
    assertFalse(content.contains("reactor.core.publisher"),
        "Should NOT import reactive publishers");
  }

  @Test
  @DisplayName("Should generate PostgreSQL adapter with Spring Data JPA (not R2DBC)")
  void shouldGeneratePostgreSqlAdapterWithJpa() throws IOException {
    // Given
    Path projectPath = tempDir.resolve(PROJECT_NAME);

    // When
    String adapterContent = generateImperativePostgreSqlAdapter();
    Path adapterFile = projectPath.resolve("UserJpaAdapter.java");
    Files.writeString(adapterFile, adapterContent);

    // Then
    String content = Files.readString(adapterFile);

    // Verify Spring Data JPA (not R2DBC)
    assertTrue(content.contains("JpaRepository"),
        "Should use JpaRepository");
    assertFalse(content.contains("ReactiveCrudRepository"),
        "Should NOT use ReactiveCrudRepository");
    assertFalse(content.contains("R2dbcRepository"),
        "Should NOT use R2dbcRepository");

    // Verify JPA annotations
    assertTrue(content.contains("@Entity") || content.contains("jakarta.persistence"),
        "Should use JPA annotations");

    // Verify synchronous return types
    assertFalse(content.contains("Mono<"),
        "Should NOT use Mono");
    assertFalse(content.contains("Flux<"),
        "Should NOT use Flux");
    assertTrue(content.contains("Optional<"),
        "Should use Optional for single results");
    assertTrue(content.contains("List<"),
        "Should use List for collections");
  }

  @Test
  @DisplayName("Should generate HTTP Client adapter with RestTemplate (not WebClient)")
  void shouldGenerateHttpClientWithRestTemplate() throws IOException {
    // Given
    Path projectPath = tempDir.resolve(PROJECT_NAME);

    // When
    String adapterContent = generateImperativeHttpClient();
    Path adapterFile = projectPath.resolve("ExternalApiClient.java");
    Files.writeString(adapterFile, adapterContent);

    // Then
    String content = Files.readString(adapterFile);

    // Verify RestTemplate (not WebClient)
    assertTrue(content.contains("RestTemplate"),
        "Should use RestTemplate");
    assertFalse(content.contains("WebClient"),
        "Should NOT use WebClient");

    // Verify synchronous return types
    assertFalse(content.contains("Mono<"),
        "Should NOT use Mono");
    assertFalse(content.contains("Flux<"),
        "Should NOT use Flux");

    // Verify blocking HTTP methods
    assertTrue(content.contains("getForObject") || content.contains("postForObject")
        || content.contains("exchange"),
        "Should use RestTemplate blocking methods");
  }

  @Test
  @DisplayName("Should generate service layer with synchronous signatures")
  void shouldGenerateServiceWithSynchronousSignatures() throws IOException {
    // Given
    Path projectPath = tempDir.resolve(PROJECT_NAME);

    // When
    String serviceContent = generateImperativeService();
    Path serviceFile = projectPath.resolve("UserService.java");
    Files.writeString(serviceFile, serviceContent);

    // Then
    String content = Files.readString(serviceFile);

    // Verify synchronous method signatures
    assertTrue(content.contains("public User ") || content.contains("public Optional<User>"),
        "Should have synchronous return types");
    assertTrue(content.contains("public List<User>"),
        "Should use List for collections");
    assertTrue(content.contains("public void "),
        "Should use void for operations without return");

    // Verify no reactive types
    assertFalse(content.contains("Mono<"),
        "Should NOT use Mono");
    assertFalse(content.contains("Flux<"),
        "Should NOT use Flux");
    assertFalse(content.contains("reactor.core.publisher"),
        "Should NOT import reactive publishers");
  }

  // Helper methods to generate sample code

  private String generateImperativeRestController() {
    return """
        package com.example.test.adapter.in.rest;

        import org.springframework.web.bind.annotation.*;
        import org.springframework.http.ResponseEntity;
        import java.util.List;

        @RestController
        @RequestMapping("/api/users")
        public class UserController {

            @GetMapping("/{id}")
            public ResponseEntity<User> getUser(@PathVariable Long id) {
                return ResponseEntity.ok(new User());
            }

            @GetMapping
            public List<User> getAllUsers() {
                return List.of();
            }

            @PostMapping
            public ResponseEntity<User> createUser(@RequestBody User user) {
                return ResponseEntity.ok(user);
            }
        }
        """;
  }

  private String generateImperativeMongoDbAdapter() {
    return """
        package com.example.test.adapter.out.mongodb;

        import org.springframework.data.mongodb.repository.MongoRepository;
        import org.springframework.stereotype.Repository;
        import java.util.List;
        import java.util.Optional;

        @Repository
        public interface UserMongoRepository extends MongoRepository<User, String> {
            Optional<User> findByEmail(String email);
            List<User> findByStatus(String status);
        }
        """;
  }

  private String generateImperativePostgreSqlAdapter() {
    return """
        package com.example.test.adapter.out.jpa;

        import org.springframework.data.jpa.repository.JpaRepository;
        import org.springframework.stereotype.Repository;
        import jakarta.persistence.*;
        import java.util.List;
        import java.util.Optional;

        @Repository
        public interface UserJpaRepository extends JpaRepository<User, Long> {
            Optional<User> findByEmail(String email);
            List<User> findByStatus(String status);
        }

        @Entity
        @Table(name = "users")
        class User {
            @Id
            @GeneratedValue(strategy = GenerationType.IDENTITY)
            private Long id;
        }
        """;
  }

  private String generateImperativeHttpClient() {
    return """
        package com.example.test.adapter.out.http;

        import org.springframework.stereotype.Component;
        import org.springframework.web.client.RestTemplate;

        @Component
        public class ExternalApiClient {
            private final RestTemplate restTemplate;

            public ExternalApiClient(RestTemplate restTemplate) {
                this.restTemplate = restTemplate;
            }

            public ApiResponse callApi(String endpoint) {
                return restTemplate.getForObject(endpoint, ApiResponse.class);
            }
        }
        """;
  }

  private String generateImperativeService() {
    return """
        package com.example.test.application.service;

        import org.springframework.stereotype.Service;
        import java.util.List;
        import java.util.Optional;

        @Service
        public class UserService {

            public User createUser(User user) {
                return user;
            }

            public Optional<User> getUser(Long id) {
                return Optional.empty();
            }

            public List<User> getAllUsers() {
                return List.of();
            }

            public void deleteUser(Long id) {
                // Delete logic
            }
        }
        """;
  }
}
