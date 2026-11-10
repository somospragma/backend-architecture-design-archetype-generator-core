package com.pragma.archetype.infrastructure.adapter.out.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.pragma.archetype.domain.port.out.HttpClientPort;

class GitHubTemplateTagResolverTest {

  private static final String REPO = "https://github.com/somospragma/templates";
  private static final String API = "https://api.github.com/repos/somospragma/templates/git/matching-refs/tags/v1.";

  private final HttpClientPort http = mock(HttpClientPort.class);

  @AfterEach
  void forgetResolvedTags() {
    GitHubTemplateTagResolver.clear();
  }

  @Test
  void shouldResolveTheNewestReleaseTagOfTheMajor() {
    when(http.downloadContent(API)).thenReturn("""
        [{"ref": "refs/tags/v1.2.0"}, {"ref": "refs/tags/v1.10.1"}, {"ref": "refs/tags/v1.9.0"}]
        """);

    assertEquals("v1.10.1", new GitHubTemplateTagResolver(http).latestTag(REPO, 1));
  }

  @Test
  void shouldAcceptRepositoryUrlsEndingInGit() {
    when(http.downloadContent(API)).thenReturn("[{\"ref\": \"refs/tags/v1.0.0\"}]");

    assertEquals("v1.0.0", new GitHubTemplateTagResolver(http).latestTag(REPO + ".git", 1));
  }

  @Test
  void shouldReuseTheResolvedTagUntilItExpires() {
    when(http.downloadContent(API)).thenReturn("[{\"ref\": \"refs/tags/v1.0.0\"}]");
    Instant start = Instant.parse("2026-11-10T10:00:00Z");

    new GitHubTemplateTagResolver(http, Clock.fixed(start, ZoneOffset.UTC)).latestTag(REPO, 1);
    new GitHubTemplateTagResolver(http, Clock.fixed(start.plusSeconds(60), ZoneOffset.UTC)).latestTag(REPO, 1);
    verify(http, times(1)).downloadContent(anyString());

    new GitHubTemplateTagResolver(http, Clock.fixed(start.plus(GitHubTemplateTagResolver.TTL), ZoneOffset.UTC))
        .latestTag(REPO, 1);
    verify(http, times(2)).downloadContent(anyString());
  }

  @Test
  void shouldExplainHowToPinWhenThereIsNoRelease() {
    when(http.downloadContent(API)).thenReturn("[]");

    var error = assertThrows(GitHubTemplateTagResolver.TemplateTagException.class,
        () -> new GitHubTemplateTagResolver(http).latestTag(REPO, 1));

    assertTrue(error.getMessage().contains("no templates release v1.x.y"));
    assertTrue(error.getMessage().contains("templates.version"));
  }

  @Test
  void shouldExplainHowToPinWhenGitHubCannotBeReached() {
    when(http.downloadContent(API)).thenThrow(new HttpClientPort.HttpDownloadException("HTTP 403"));

    var error = assertThrows(GitHubTemplateTagResolver.TemplateTagException.class,
        () -> new GitHubTemplateTagResolver(http).latestTag(REPO, 1));

    assertTrue(error.getMessage().contains("HTTP 403"));
    assertTrue(error.getMessage().contains("templates.version"));
  }

  @Test
  void shouldRejectRepositoriesOutsideGitHub() {
    var error = assertThrows(GitHubTemplateTagResolver.TemplateTagException.class,
        () -> new GitHubTemplateTagResolver(http).latestTag("https://gitlab.com/acme/templates", 1));

    assertTrue(error.getMessage().contains("only GitHub repositories"));
  }
}
