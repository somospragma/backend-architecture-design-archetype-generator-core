package com.pragma.archetype.infrastructure.adapter.out.template;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.pragma.archetype.domain.model.config.TemplateConfig;
import com.pragma.archetype.domain.model.config.TemplateMode;
import com.pragma.archetype.domain.model.config.TemplateSource;

@DisplayName("TemplateSourceResolver Tests")
class TemplateSourceResolverTest {

  @Test
  @DisplayName("Should resolve to LOCAL_CONFIGURED when localPath is configured and exists")
  void shouldResolveToLocalConfiguredWhenLocalPathExists(@TempDir Path tempDir) {
    // Given: A template config with a valid local path
    TemplateConfig config = new TemplateConfig(
        TemplateMode.DEVELOPER,
        null,
        null,
        null,
        tempDir.toString(),
        false);

    // When: Resolving the source
    TemplateSourceResolver resolver = new TemplateSourceResolver(config);
    TemplateSource source = resolver.resolveSource();

    // Then: Should resolve to LOCAL_CONFIGURED
    assertEquals(TemplateSource.LOCAL_CONFIGURED, source);
    assertTrue(resolver.isLocalMode());
    assertFalse(resolver.isRemoteMode());
    assertNotNull(resolver.getLocalPath());
    assertEquals(tempDir.toAbsolutePath().normalize(), resolver.getLocalPath());
  }

  @Test
  @DisplayName("Should throw exception when configured localPath does not exist")
  void shouldThrowExceptionWhenConfiguredLocalPathDoesNotExist() {
    // Given: A template config with a non-existent local path
    TemplateConfig config = new TemplateConfig(
        TemplateMode.DEVELOPER,
        null,
        null,
        null,
        "/non/existent/path",
        false);

    // When/Then: Resolving should throw exception
    TemplateSourceResolver resolver = new TemplateSourceResolver(config);
    TemplateSourceResolver.TemplateSourceException exception = assertThrows(
        TemplateSourceResolver.TemplateSourceException.class,
        resolver::resolveSource);

    assertTrue(exception.getMessage().contains("does not exist"));
    assertTrue(exception.getMessage().contains("/non/existent/path"));
  }

  @Test
  @DisplayName("Should throw exception when configured localPath is not a directory")
  void shouldThrowExceptionWhenConfiguredLocalPathIsNotDirectory(@TempDir Path tempDir) throws IOException {
    // Given: A template config with a file path instead of directory
    Path file = tempDir.resolve("file.txt");
    Files.writeString(file, "content");

    TemplateConfig config = new TemplateConfig(
        TemplateMode.DEVELOPER,
        null,
        null,
        null,
        file.toString(),
        false);

    // When/Then: Resolving should throw exception
    TemplateSourceResolver resolver = new TemplateSourceResolver(config);
    TemplateSourceResolver.TemplateSourceException exception = assertThrows(
        TemplateSourceResolver.TemplateSourceException.class,
        resolver::resolveSource);

    assertTrue(exception.getMessage().contains("not a directory"));
  }

  @Test
  @DisplayName("Should resolve to REMOTE when no localPath is configured")
  void shouldResolveToRemoteWhenNoLocalPathConfigured() {
    // Given: A template config without local path
    TemplateConfig config = new TemplateConfig(
        TemplateMode.PRODUCTION,
        "https://github.com/somospragma/templates",
        "main",
        null,
        null,
        true);

    // When: Resolving the source
    TemplateSourceResolver resolver = new TemplateSourceResolver(config);
    TemplateSource source = resolver.resolveSource();

    // Then: Should resolve to REMOTE
    assertEquals(TemplateSource.REMOTE, source);
    assertFalse(resolver.isLocalMode());
    assertTrue(resolver.isRemoteMode());
    assertNull(resolver.getLocalPath());
  }

  @Test
  @DisplayName("Should resolve to REMOTE when localPath is empty string")
  void shouldResolveToRemoteWhenLocalPathIsEmpty() {
    // Given: A template config with empty local path
    TemplateConfig config = new TemplateConfig(
        TemplateMode.DEVELOPER,
        "https://github.com/somospragma/templates",
        "feature/test",
        null,
        "",
        false);

    // When: Resolving the source
    TemplateSourceResolver resolver = new TemplateSourceResolver(config);
    TemplateSource source = resolver.resolveSource();

    // Then: Should resolve to REMOTE
    assertEquals(TemplateSource.REMOTE, source);
    assertTrue(resolver.isRemoteMode());
  }

  @Test
  @DisplayName("Should validate successfully for local configured mode")
  void shouldValidateSuccessfullyForLocalConfiguredMode(@TempDir Path tempDir) {
    // Given: A valid local configuration
    TemplateConfig config = new TemplateConfig(
        TemplateMode.DEVELOPER,
        null,
        null,
        null,
        tempDir.toString(),
        false);

    // When: Validating the source
    TemplateSourceResolver resolver = new TemplateSourceResolver(config);

    // Then: Should not throw exception
    assertDoesNotThrow(resolver::validateSource);
  }

  @Test
  @DisplayName("Should validate successfully for remote mode with repository and branch")
  void shouldValidateSuccessfullyForRemoteMode() {
    // Given: A valid remote configuration
    TemplateConfig config = new TemplateConfig(
        TemplateMode.PRODUCTION,
        "https://github.com/somospragma/templates",
        "main",
        null,
        null,
        true);

    // When: Validating the source
    TemplateSourceResolver resolver = new TemplateSourceResolver(config);

    // Then: Should not throw exception
    assertDoesNotThrow(resolver::validateSource);
  }

  @Test
  @DisplayName("Should fail validation when remote mode has no repository")
  void shouldFailValidationWhenRemoteModeHasNoRepository() {
    // Given: A remote configuration without repository
    TemplateConfig config = new TemplateConfig(
        TemplateMode.PRODUCTION,
        null,
        "main",
        null,
        null,
        true);

    // When/Then: Validation should throw exception
    TemplateSourceResolver resolver = new TemplateSourceResolver(config);
    TemplateSourceResolver.TemplateSourceException exception = assertThrows(
        TemplateSourceResolver.TemplateSourceException.class,
        resolver::validateSource);

    assertTrue(exception.getMessage().contains("repository URL"));
  }

  @Test
  @DisplayName("Should fail validation when remote mode has empty repository")
  void shouldFailValidationWhenRemoteModeHasEmptyRepository() {
    // Given: A remote configuration with empty repository
    TemplateConfig config = new TemplateConfig(
        TemplateMode.PRODUCTION,
        "",
        "main",
        null,
        null,
        true);

    // When/Then: Validation should throw exception
    TemplateSourceResolver resolver = new TemplateSourceResolver(config);
    TemplateSourceResolver.TemplateSourceException exception = assertThrows(
        TemplateSourceResolver.TemplateSourceException.class,
        resolver::validateSource);

    assertTrue(exception.getMessage().contains("repository URL"));
  }

  @Test
  @DisplayName("Should provide descriptive source description for local configured")
  void shouldProvideDescriptiveSourceDescriptionForLocalConfigured(@TempDir Path tempDir) {
    // Given: A local configured setup
    TemplateConfig config = new TemplateConfig(
        TemplateMode.DEVELOPER,
        null,
        null,
        null,
        tempDir.toString(),
        false);

    // When: Getting source description
    TemplateSourceResolver resolver = new TemplateSourceResolver(config);
    String description = resolver.getSourceDescription();

    // Then: Should contain meaningful information
    assertTrue(description.contains("Local templates"));
    assertTrue(description.contains("configured"));
    assertTrue(description.contains(tempDir.toAbsolutePath().normalize().toString()));
  }

  @Test
  @DisplayName("Should provide descriptive source description for remote")
  void shouldProvideDescriptiveSourceDescriptionForRemote() {
    // Given: A remote setup
    TemplateConfig config = new TemplateConfig(
        TemplateMode.PRODUCTION,
        "https://github.com/somospragma/templates",
        "feature/init-templates",
        null,
        null,
        true);

    // When: Getting source description
    TemplateSourceResolver resolver = new TemplateSourceResolver(config);
    String description = resolver.getSourceDescription();

    // Then: Should contain meaningful information
    assertTrue(description.contains("Remote templates"));
    assertTrue(description.contains("https://github.com/somospragma/templates"));
    assertTrue(description.contains("feature/init-templates"));
  }

  @Test
  @DisplayName("Should cache resolved source on subsequent calls")
  void shouldCacheResolvedSourceOnSubsequentCalls(@TempDir Path tempDir) {
    // Given: A valid configuration
    TemplateConfig config = new TemplateConfig(
        TemplateMode.DEVELOPER,
        null,
        null,
        null,
        tempDir.toString(),
        false);

    // When: Resolving multiple times
    TemplateSourceResolver resolver = new TemplateSourceResolver(config);
    TemplateSource source1 = resolver.resolveSource();
    TemplateSource source2 = resolver.resolveSource();
    TemplateSource source3 = resolver.getResolvedSource();

    // Then: Should return same result
    assertEquals(source1, source2);
    assertEquals(source1, source3);
    assertEquals(TemplateSource.LOCAL_CONFIGURED, source1);
  }

  @Test
  @DisplayName("Should handle relative paths correctly")
  void shouldHandleRelativePathsCorrectly(@TempDir Path tempDir) {
    // Given: A config with relative path
    Path subDir = tempDir.resolve("templates");
    try {
      Files.createDirectory(subDir);
    } catch (IOException e) {
      fail("Failed to create test directory");
    }

    // Use relative path notation
    String relativePath = tempDir.toString() + "/templates";
    TemplateConfig config = new TemplateConfig(
        TemplateMode.DEVELOPER,
        null,
        null,
        null,
        relativePath,
        false);

    // When: Resolving the source
    TemplateSourceResolver resolver = new TemplateSourceResolver(config);
    TemplateSource source = resolver.resolveSource();

    // Then: Should resolve correctly
    assertEquals(TemplateSource.LOCAL_CONFIGURED, source);
    assertNotNull(resolver.getLocalPath());
    assertTrue(Files.exists(resolver.getLocalPath()));
  }

  @Test
  @DisplayName("Should use effective branch from config")
  void shouldUseEffectiveBranchFromConfig() {
    // Given: A remote config with custom branch
    TemplateConfig config = new TemplateConfig(
        TemplateMode.PRODUCTION,
        "https://github.com/somospragma/templates",
        "feature/test-branch",
        null,
        null,
        true);

    // When: Getting source description
    TemplateSourceResolver resolver = new TemplateSourceResolver(config);
    String description = resolver.getSourceDescription();

    // Then: Should include the branch
    assertTrue(description.contains("feature/test-branch"));
  }

  @Test
  @DisplayName("Should resolve to LOCAL_AUTO_DETECTED when in developer mode with no repository and auto-detect path exists")
  void shouldResolveToLocalAutoDetectedWhenAutoDetectPathExists() {
    // Given: A developer mode config with no repository and no localPath
    // Note: This test assumes
    // ../backend-architecture-design-archetype-generator-templates exists
    TemplateConfig config = new TemplateConfig(
        TemplateMode.DEVELOPER,
        null,
        null,
        null,
        null,
        false);

    // When: Resolving the source
    TemplateSourceResolver resolver = new TemplateSourceResolver(config);

    // Check if auto-detect path actually exists
    Path autoDetectPath = Path.of("../backend-architecture-design-archetype-generator-templates")
        .toAbsolutePath().normalize();

    if (Files.exists(autoDetectPath)) {
      // Then: Should resolve to LOCAL_AUTO_DETECTED
      TemplateSource source = resolver.resolveSource();
      assertEquals(TemplateSource.LOCAL_AUTO_DETECTED, source);
      assertTrue(resolver.isLocalMode());
      assertNotNull(resolver.getLocalPath());

      String description = resolver.getSourceDescription();
      assertTrue(description.contains("auto-detected"));
    } else {
      // If auto-detect path doesn't exist, should fall back to REMOTE
      TemplateSource source = resolver.resolveSource();
      assertEquals(TemplateSource.REMOTE, source);
    }
  }

  @Test
  @DisplayName("Should not auto-detect when repository is configured even in developer mode")
  void shouldNotAutoDetectWhenRepositoryConfigured() {
    // Given: A developer mode config with repository but no localPath
    TemplateConfig config = new TemplateConfig(
        TemplateMode.DEVELOPER,
        "https://github.com/somospragma/templates",
        "main",
        null,
        null,
        false);

    // When: Resolving the source
    TemplateSourceResolver resolver = new TemplateSourceResolver(config);
    TemplateSource source = resolver.resolveSource();

    // Then: Should resolve to REMOTE, not auto-detect
    assertEquals(TemplateSource.REMOTE, source);
    assertTrue(resolver.isRemoteMode());
    assertNull(resolver.getLocalPath());
  }
}
