package com.pragma.archetype.infrastructure.adapter.out.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.pragma.archetype.domain.model.config.TemplateConfig;
import com.pragma.archetype.domain.model.config.TemplateMode;
import com.pragma.archetype.domain.port.out.HttpClientPort;

/**
 * Tests for GitHubTemplateDownloader.
 * 
 * Coverage focus:
 * - Template downloading from different Git providers
 * - Cache integration
 * - Network error handling
 * - Branch validation
 * - URL building for GitHub, GitLab, Bitbucket
 */
@DisplayName("GitHub Template Downloader Tests")
class GitHubTemplateDownloaderTest {

  @Nested
  @DisplayName("Template Download - Success Cases")
  class DownloadSuccessTests {

    @Test
    @DisplayName("Should download template from GitHub")
    void shouldDownloadFromGitHub() {
      // Given
      TemplateConfig config = new TemplateConfig(
          TemplateMode.PRODUCTION,
          "https://github.com/owner/repo",
          "main",
          null,
          null,
          false);

      String templatePath = "templates/Application.java.ftl";
      String expectedContent = "template content";

      when(httpClient.downloadContent(anyString())).thenReturn(expectedContent);

      // When
      String content = downloader.downloadTemplate(config, templatePath);

      // Then
      assertThat(content).isEqualTo(expectedContent);
      verify(httpClient).downloadContent(contains("raw.githubusercontent.com"));
      verify(httpClient).downloadContent(contains("main"));
      verify(httpClient).downloadContent(contains(templatePath));
    }

    @Test
    @DisplayName("Should download template from GitLab")
    void shouldDownloadFromGitLab() {
      // Given
      TemplateConfig config = new TemplateConfig(
          TemplateMode.PRODUCTION,
          "https://gitlab.com/owner/repo",
          "develop",
          null,
          null,
          false);

      String templatePath = "templates/Service.java.ftl";
      String expectedContent = "service template";

      when(httpClient.downloadContent(anyString())).thenReturn(expectedContent);

      // When
      String content = downloader.downloadTemplate(config, templatePath);

      // Then
      assertThat(content).isEqualTo(expectedContent);
      verify(httpClient).downloadContent(contains("gitlab.com"));
      verify(httpClient).downloadContent(contains("/-/raw/"));
      verify(httpClient).downloadContent(contains("develop"));
    }

    @Test
    @DisplayName("Should download template from Bitbucket")
    void shouldDownloadFromBitbucket() {
      // Given
      TemplateConfig config = new TemplateConfig(
          TemplateMode.PRODUCTION,
          "https://bitbucket.org/owner/repo",
          "master",
          null,
          null,
          false);

      String templatePath = "templates/Controller.java.ftl";
      String expectedContent = "controller template";

      when(httpClient.downloadContent(anyString())).thenReturn(expectedContent);

      // When
      String content = downloader.downloadTemplate(config, templatePath);

      // Then
      assertThat(content).isEqualTo(expectedContent);
      verify(httpClient).downloadContent(contains("bitbucket.org"));
      verify(httpClient).downloadContent(contains("/raw/"));
      verify(httpClient).downloadContent(contains("master"));
    }

    @Test
    @DisplayName("Should handle template path with leading slash")
    void shouldHandleLeadingSlash() {
      // Given
      TemplateConfig config = new TemplateConfig(
          TemplateMode.PRODUCTION,
          "https://github.com/owner/repo",
          "main",
          null,
          null,
          false);

      String templatePath = "/templates/Application.java.ftl";
      when(httpClient.downloadContent(anyString())).thenReturn("content");

      // When
      downloader.downloadTemplate(config, templatePath);

      // Then
      verify(httpClient).downloadContent(contains("templates/Application.java.ftl"));
      // Verify it doesn't have double slashes
      verify(httpClient, never()).downloadContent(contains("//templates"));
    }
  }

  @Nested
  @DisplayName("Cache Integration")
  class CacheIntegrationTests {

    @Test
    @DisplayName("Should use cached template when available")
    void shouldUseCachedTemplate() {
      // Given
      TemplateConfig config = new TemplateConfig(
          TemplateMode.PRODUCTION,
          "https://github.com/owner/repo",
          "main",
          null,
          null,
          true);

      String templatePath = "templates/cached.ftl";
      String cachedContent = "cached content";

      when(cache.get(anyString())).thenReturn(cachedContent);

      // When
      String content = downloader.downloadTemplate(config, templatePath);

      // Then
      assertThat(content).isEqualTo(cachedContent);
      verify(cache).get(anyString());
      verify(httpClient, never()).downloadContent(anyString());
    }

    @Test
    @DisplayName("Should download and cache when not in cache")
    void shouldDownloadAndCache() {
      // Given
      TemplateConfig config = new TemplateConfig(
          TemplateMode.PRODUCTION,
          "https://github.com/owner/repo",
          "main",
          null,
          null,
          true);

      String templatePath = "templates/new.ftl";
      String downloadedContent = "downloaded content";

      when(cache.get(anyString())).thenReturn(null);
      when(httpClient.downloadContent(anyString())).thenReturn(downloadedContent);

      // When
      String content = downloader.downloadTemplate(config, templatePath);

      // Then
      assertThat(content).isEqualTo(downloadedContent);
      verify(cache).get(anyString());
      verify(httpClient).downloadContent(anyString());
      verify(cache).put(anyString(), eq(downloadedContent));
    }

    @Test
    @DisplayName("Should not use cache when caching is disabled")
    void shouldNotUseCacheWhenDisabled() {
      // Given
      TemplateConfig config = new TemplateConfig(
          TemplateMode.PRODUCTION,
          "https://github.com/owner/repo",
          "main",
          null,
          null,
          false);

      String templatePath = "templates/nocache.ftl";
      String content = "content";

      when(httpClient.downloadContent(anyString())).thenReturn(content);

      // When
      downloader.downloadTemplate(config, templatePath);

      // Then
      verify(cache, never()).get(anyString());
      verify(cache, never()).put(anyString(), anyString());
      verify(httpClient).downloadContent(anyString());
    }
  }

  @Nested
  @DisplayName("Network Error Handling")
  class NetworkErrorTests {

    @Test
    @DisplayName("Should throw exception with enhanced message on download failure")
    void shouldThrowEnhancedExceptionOnFailure() {
      // Given
      TemplateConfig config = new TemplateConfig(
          TemplateMode.PRODUCTION,
          "https://github.com/owner/repo",
          "non-existent-branch",
          null,
          null,
          false);

      String templatePath = "templates/missing.ftl";

      when(httpClient.downloadContent(anyString()))
          .thenThrow(new HttpClientPort.HttpDownloadException("404 Not Found"));

      // When & Then
      assertThatThrownBy(() -> downloader.downloadTemplate(config, templatePath))
          .isInstanceOf(HttpClientPort.HttpDownloadException.class)
          .hasMessageContaining("non-existent-branch")
          .hasMessageContaining(templatePath)
          .hasMessageContaining("owner/repo");
    }

    @Test
    @DisplayName("Should handle network timeout")
    void shouldHandleNetworkTimeout() {
      // Given
      TemplateConfig config = new TemplateConfig(
          TemplateMode.PRODUCTION,
          "https://github.com/owner/repo",
          "main",
          null,
          null,
          false);

      when(httpClient.downloadContent(anyString()))
          .thenThrow(new HttpClientPort.HttpDownloadException("Connection timeout"));

      // When & Then
      assertThatThrownBy(() -> downloader.downloadTemplate(config, "template.ftl"))
          .isInstanceOf(HttpClientPort.HttpDownloadException.class)
          .hasMessageContaining("timeout");
    }

    @Test
    @DisplayName("Should handle 404 Not Found")
    void shouldHandle404NotFound() {
      // Given
      TemplateConfig config = new TemplateConfig(
          TemplateMode.PRODUCTION,
          "https://github.com/owner/repo",
          "main",
          null,
          null,
          false);

      when(httpClient.downloadContent(anyString()))
          .thenThrow(new HttpClientPort.HttpDownloadException("404 Not Found"));

      // When & Then
      assertThatThrownBy(() -> downloader.downloadTemplate(config, "missing.ftl"))
          .isInstanceOf(HttpClientPort.HttpDownloadException.class);
    }

    @Test
    @DisplayName("Should handle 403 Forbidden")
    void shouldHandle403Forbidden() {
      // Given
      TemplateConfig config = new TemplateConfig(
          TemplateMode.PRODUCTION,
          "https://github.com/private/repo",
          "main",
          null,
          null,
          false);

      when(httpClient.downloadContent(anyString()))
          .thenThrow(new HttpClientPort.HttpDownloadException("403 Forbidden"));

      // When & Then
      assertThatThrownBy(() -> downloader.downloadTemplate(config, "template.ftl"))
          .isInstanceOf(HttpClientPort.HttpDownloadException.class)
          .hasMessageContaining("403");
    }
  }

  @Nested
  @DisplayName("Template Existence Checks")
  class TemplateExistenceTests {

    @Test
    @DisplayName("Should check template existence in cache first")
    void shouldCheckCacheFirst() {
      // Given
      TemplateConfig config = new TemplateConfig(
          TemplateMode.PRODUCTION,
          "https://github.com/owner/repo",
          "main",
          null,
          null,
          true);

      when(cache.exists(anyString())).thenReturn(true);

      // When
      boolean exists = downloader.templateExists(config, "template.ftl");

      // Then
      assertThat(exists).isTrue();
      verify(cache).exists(anyString());
      verify(httpClient, never()).isAccessible(anyString());
    }

    @Test
    @DisplayName("Should check remote when not in cache")
    void shouldCheckRemoteWhenNotCached() {
      // Given
      TemplateConfig config = new TemplateConfig(
          TemplateMode.PRODUCTION,
          "https://github.com/owner/repo",
          "main",
          null,
          null,
          true);

      when(cache.exists(anyString())).thenReturn(false);
      when(httpClient.isAccessible(anyString())).thenReturn(true);

      // When
      boolean exists = downloader.templateExists(config, "template.ftl");

      // Then
      assertThat(exists).isTrue();
      verify(cache).exists(anyString());
      verify(httpClient).isAccessible(anyString());
    }

    @Test
    @DisplayName("Should return false when template does not exist")
    void shouldReturnFalseWhenNotExists() {
      // Given
      TemplateConfig config = new TemplateConfig(
          TemplateMode.PRODUCTION,
          "https://github.com/owner/repo",
          "main",
          null,
          null,
          false);

      when(httpClient.isAccessible(anyString())).thenReturn(false);

      // When
      boolean exists = downloader.templateExists(config, "missing.ftl");

      // Then
      assertThat(exists).isFalse();
    }
  }

  @Nested
  @DisplayName("Repository Validation")
  class RepositoryValidationTests {

    @Test
    @DisplayName("Should validate accessible remote repository")
    void shouldValidateAccessibleRepository() {
      // Given
      TemplateConfig config = new TemplateConfig(
          TemplateMode.PRODUCTION,
          "https://github.com/owner/repo",
          "main",
          null,
          null,
          false);

      when(httpClient.isAccessible(anyString())).thenReturn(true);

      // When
      boolean valid = downloader.validateRemoteRepository(config);

      // Then
      assertThat(valid).isTrue();
      verify(httpClient).isAccessible(contains("README.md"));
    }

    @Test
    @DisplayName("Should return false for inaccessible repository")
    void shouldReturnFalseForInaccessibleRepository() {
      // Given
      TemplateConfig config = new TemplateConfig(
          TemplateMode.PRODUCTION,
          "https://github.com/owner/nonexistent",
          "main",
          null,
          null,
          false);

      when(httpClient.isAccessible(anyString())).thenReturn(false);

      // When
      boolean valid = downloader.validateRemoteRepository(config);

      // Then
      assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("Should return true for local mode without validation")
    void shouldReturnTrueForLocalMode() {
      // Given
      TemplateConfig config = TemplateConfig.developerConfig("/path/to/templates");

      // When
      boolean valid = downloader.validateRemoteRepository(config);

      // Then
      assertThat(valid).isTrue();
      verify(httpClient, never()).isAccessible(anyString());
    }
  }

  @Nested
  @DisplayName("URL Building")
  class UrlBuildingTests {

    @Test
    @DisplayName("Should build correct GitHub URL")
    void shouldBuildGitHubUrl() {
      // Given
      TemplateConfig config = new TemplateConfig(
          TemplateMode.PRODUCTION,
          "https://github.com/owner/repo.git",
          "main",
          null,
          null,
          false);

      when(httpClient.downloadContent(anyString())).thenReturn("content");

      // When
      downloader.downloadTemplate(config, "path/to/file.ftl");

      // Then
      verify(httpClient).downloadContent(
          "https://raw.githubusercontent.com/owner/repo/main/path/to/file.ftl");
    }

    @Test
    @DisplayName("Should build correct GitLab URL")
    void shouldBuildGitLabUrl() {
      // Given
      TemplateConfig config = new TemplateConfig(
          TemplateMode.PRODUCTION,
          "https://gitlab.com/owner/repo.git",
          "develop",
          null,
          null,
          false);

      when(httpClient.downloadContent(anyString())).thenReturn("content");

      // When
      downloader.downloadTemplate(config, "path/to/file.ftl");

      // Then
      verify(httpClient).downloadContent(
          "https://gitlab.com/owner/repo/-/raw/develop/path/to/file.ftl");
    }

    @Test
    @DisplayName("Should build correct Bitbucket URL")
    void shouldBuildBitbucketUrl() {
      // Given
      TemplateConfig config = new TemplateConfig(
          TemplateMode.PRODUCTION,
          "https://bitbucket.org/owner/repo.git",
          "master",
          null,
          null,
          false);

      when(httpClient.downloadContent(anyString())).thenReturn("content");

      // When
      downloader.downloadTemplate(config, "path/to/file.ftl");

      // Then
      verify(httpClient).downloadContent(
          "https://bitbucket.org/owner/repo/raw/master/path/to/file.ftl");
    }
  }

  @Nested
  @DisplayName("Utility Methods")
  class UtilityTests {

    @Test
    @DisplayName("Should get cache instance")
    void shouldGetCacheInstance() {
      // When
      TemplateCache retrievedCache = downloader.getCache();

      // Then
      assertThat(retrievedCache).isSameAs(cache);
    }
  }

  @Mock
  private HttpClientPort httpClient;

  @Mock
  private TemplateCache cache;

  private GitHubTemplateDownloader downloader;

  private AutoCloseable mocks;

  @BeforeEach
  void setUp() {
    mocks = MockitoAnnotations.openMocks(this);
    downloader = new GitHubTemplateDownloader(httpClient, cache);
  }

  @AfterEach
  void tearDown() throws Exception {
    mocks.close();
  }
}
