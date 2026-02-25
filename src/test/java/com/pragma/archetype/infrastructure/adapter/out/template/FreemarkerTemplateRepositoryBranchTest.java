package com.pragma.archetype.infrastructure.adapter.out.template;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FreemarkerTemplateRepositoryBranchTest {

  @TempDir
  Path tempDir;

  @Test
  void shouldHandleEmbeddedTemplates() {
    // When
    FreemarkerTemplateRepository repository = new FreemarkerTemplateRepository("embedded");

    // Then
    assertNotNull(repository);
  }

  @Test
  void shouldHandleLocalTemplates() throws Exception {
    // Given
    Path templatesDir = tempDir.resolve("templates");
    Files.createDirectories(templatesDir);

    // When
    FreemarkerTemplateRepository repository = new FreemarkerTemplateRepository(templatesDir);

    // Then
    assertNotNull(repository);
  }

  @Test
  void shouldHandleNonExistentLocalPath() {
    // Given
    Path nonExistent = tempDir.resolve("nonexistent");

    // When - constructor doesn't throw, but operations might fail
    FreemarkerTemplateRepository repository = new FreemarkerTemplateRepository(nonExistent);

    // Then - repository is created but template operations will fail
    assertNotNull(repository);
    assertFalse(repository.templateExists("any.ftl"));
  }

  @Test
  void shouldCheckTemplateExists() throws Exception {
    // Given
    Path templatesDir = tempDir.resolve("templates");
    Files.createDirectories(templatesDir);
    Files.writeString(templatesDir.resolve("test.ftl"), "test template");

    FreemarkerTemplateRepository repository = new FreemarkerTemplateRepository(templatesDir);

    // When
    boolean exists = repository.templateExists("test.ftl");

    // Then
    assertTrue(exists);
  }

  @Test
  void shouldCheckTemplateDoesNotExist() throws Exception {
    // Given
    Path templatesDir = tempDir.resolve("templates");
    Files.createDirectories(templatesDir);

    FreemarkerTemplateRepository repository = new FreemarkerTemplateRepository(templatesDir);

    // When
    boolean exists = repository.templateExists("nonexistent.ftl");

    // Then
    assertFalse(exists);
  }

  @Test
  void shouldProcessTemplateWithEmptyModel() throws Exception {
    // Given
    Path templatesDir = tempDir.resolve("templates");
    Files.createDirectories(templatesDir);
    Files.writeString(templatesDir.resolve("simple.ftl"), "Hello World");

    FreemarkerTemplateRepository repository = new FreemarkerTemplateRepository(templatesDir);

    // When
    String result = repository.processTemplate("simple.ftl", new HashMap<>());

    // Then
    assertNotNull(result);
    assertTrue(result.contains("Hello World"));
  }

  @Test
  void shouldProcessTemplateWithModel() throws Exception {
    // Given
    Path templatesDir = tempDir.resolve("templates");
    Files.createDirectories(templatesDir);
    Files.writeString(templatesDir.resolve("greeting.ftl"), "Hello ${name}!");

    FreemarkerTemplateRepository repository = new FreemarkerTemplateRepository(templatesDir);
    Map<String, Object> model = new HashMap<>();
    model.put("name", "World");

    // When
    String result = repository.processTemplate("greeting.ftl", model);

    // Then
    assertNotNull(result);
    assertTrue(result.contains("Hello World!"));
  }

  @Test
  void shouldHandleTemplateWithComplexModel() throws Exception {
    // Given
    Path templatesDir = tempDir.resolve("templates");
    Files.createDirectories(templatesDir);
    Files.writeString(templatesDir.resolve("complex.ftl"), "${user.name} - ${user.age}");

    FreemarkerTemplateRepository repository = new FreemarkerTemplateRepository(templatesDir);
    Map<String, Object> user = new HashMap<>();
    user.put("name", "John");
    user.put("age", 30);
    Map<String, Object> model = new HashMap<>();
    model.put("user", user);

    // When
    String result = repository.processTemplate("complex.ftl", model);

    // Then
    assertNotNull(result);
    assertTrue(result.contains("John"));
    assertTrue(result.contains("30"));
  }

  @Test
  void shouldHandleTemplateWithNullModel() throws Exception {
    // Given
    Path templatesDir = tempDir.resolve("templates");
    Files.createDirectories(templatesDir);
    Files.writeString(templatesDir.resolve("simple.ftl"), "Hello World");

    FreemarkerTemplateRepository repository = new FreemarkerTemplateRepository(templatesDir);

    // When/Then
    assertDoesNotThrow(() -> repository.processTemplate("simple.ftl", null));
  }

  @Test
  void shouldFailWhenTemplateNotFound() throws Exception {
    // Given
    Path templatesDir = tempDir.resolve("templates");
    Files.createDirectories(templatesDir);

    FreemarkerTemplateRepository repository = new FreemarkerTemplateRepository(templatesDir);

    // When/Then
    assertThrows(RuntimeException.class, () -> repository.processTemplate("nonexistent.ftl", new HashMap<>()));
  }

  @Test
  void shouldHandleTemplateInSubdirectory() throws Exception {
    // Given
    Path templatesDir = tempDir.resolve("templates");
    Path subDir = templatesDir.resolve("subdir");
    Files.createDirectories(subDir);
    Files.writeString(subDir.resolve("test.ftl"), "test content");

    FreemarkerTemplateRepository repository = new FreemarkerTemplateRepository(templatesDir);

    // When
    boolean exists = repository.templateExists("subdir/test.ftl");

    // Then
    assertTrue(exists);
  }

  @Test
  void shouldProcessTemplateInSubdirectory() throws Exception {
    // Given
    Path templatesDir = tempDir.resolve("templates");
    Path subDir = templatesDir.resolve("subdir");
    Files.createDirectories(subDir);
    Files.writeString(subDir.resolve("test.ftl"), "Hello ${name}");

    FreemarkerTemplateRepository repository = new FreemarkerTemplateRepository(templatesDir);
    Map<String, Object> model = new HashMap<>();
    model.put("name", "Test");

    // When
    String result = repository.processTemplate("subdir/test.ftl", model);

    // Then
    assertNotNull(result);
    assertTrue(result.contains("Hello Test"));
  }

  @Test
  void shouldHandleTemplateWithSpecialCharacters() throws Exception {
    // Given
    Path templatesDir = tempDir.resolve("templates");
    Files.createDirectories(templatesDir);
    Files.writeString(templatesDir.resolve("special.ftl"), "Special: ${text}");

    FreemarkerTemplateRepository repository = new FreemarkerTemplateRepository(templatesDir);
    Map<String, Object> model = new HashMap<>();
    model.put("text", "!@#$%^&*()");

    // When
    String result = repository.processTemplate("special.ftl", model);

    // Then
    assertNotNull(result);
    assertTrue(result.contains("!@#$%^&*()"));
  }

  @Test
  void shouldHandleTemplateWithUnicodeCharacters() throws Exception {
    // Given
    Path templatesDir = tempDir.resolve("templates");
    Files.createDirectories(templatesDir);
    Files.writeString(templatesDir.resolve("unicode.ftl"), "Unicode: ${text}");

    FreemarkerTemplateRepository repository = new FreemarkerTemplateRepository(templatesDir);
    Map<String, Object> model = new HashMap<>();
    model.put("text", "你好世界");

    // When
    String result = repository.processTemplate("unicode.ftl", model);

    // Then
    assertNotNull(result);
    assertTrue(result.contains("你好世界"));
  }

  @Test
  void shouldHandleTemplateWithLongContent() throws Exception {
    // Given
    Path templatesDir = tempDir.resolve("templates");
    Files.createDirectories(templatesDir);
    StringBuilder longContent = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      longContent.append("Line ").append(i).append("\n");
    }
    Files.writeString(templatesDir.resolve("long.ftl"), longContent.toString());

    FreemarkerTemplateRepository repository = new FreemarkerTemplateRepository(templatesDir);

    // When
    String result = repository.processTemplate("long.ftl", new HashMap<>());

    // Then
    assertNotNull(result);
    assertTrue(result.contains("Line 999"));
  }
}
