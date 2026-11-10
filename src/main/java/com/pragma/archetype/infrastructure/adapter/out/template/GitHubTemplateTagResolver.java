package com.pragma.archetype.infrastructure.adapter.out.template;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.archetype.domain.model.template.TemplatesContract;
import com.pragma.archetype.domain.port.out.HttpClientPort;

/**
 * Resolves the latest release tag {@code v<major>.x.y} of a templates
 * repository on GitHub. The result is kept for a few minutes so the tasks of
 * one build share a single API call, while a long-lived Gradle daemon still
 * sees a new release.
 */
public class GitHubTemplateTagResolver {

  static final Duration TTL = Duration.ofMinutes(10);

  private static final Pattern GITHUB_REPOSITORY = Pattern.compile("github\\.com[/:]([^/]+)/([^/]+?)(?:\\.git)?/?$");
  private static final Map<String, Resolved> RESOLVED = new ConcurrentHashMap<>();

  private final HttpClientPort httpClient;
  private final ObjectMapper mapper = new ObjectMapper();
  private final Clock clock;

  public GitHubTemplateTagResolver(HttpClientPort httpClient) {
    this(httpClient, Clock.systemUTC());
  }

  GitHubTemplateTagResolver(HttpClientPort httpClient, Clock clock) {
    this.httpClient = httpClient;
    this.clock = clock;
  }

  /**
   * Latest release tag of a major.
   *
   * @param repositoryUrl templates repository, such as
   *                      "https://github.com/somospragma/templates"
   * @param major         plugin major version
   * @throws TemplateTagException when the tags cannot be read or none exists
   */
  public String latestTag(String repositoryUrl, int major) {
    String key = repositoryUrl + "#" + major;
    Resolved cached = RESOLVED.get(key);
    Instant now = clock.instant();
    if (cached != null && now.isBefore(cached.expiresAt())) {
      return cached.tag();
    }
    String tag = fetchLatestTag(repositoryUrl, major);
    RESOLVED.put(key, new Resolved(tag, now.plus(TTL)));
    return tag;
  }

  /** Forgets resolved tags (tests and updateTemplates). */
  public static void clear() {
    RESOLVED.clear();
  }

  private String fetchLatestTag(String repositoryUrl, int major) {
    Matcher repository = GITHUB_REPOSITORY.matcher(repositoryUrl);
    if (!repository.find()) {
      throw new TemplateTagException(String.format(
          "Cannot find the latest templates release of %s: only GitHub repositories are supported. %s",
          repositoryUrl, pinAdvice(major)));
    }
    String url = String.format("https://api.github.com/repos/%s/%s/git/matching-refs/tags/v%d.",
        repository.group(1), repository.group(2), major);

    List<String> tags = new ArrayList<>();
    try {
      for (JsonNode ref : mapper.readTree(httpClient.downloadContent(url))) {
        tags.add(ref.path("ref").asText().replaceFirst("^refs/tags/", ""));
      }
    } catch (HttpClientPort.HttpDownloadException | java.io.IOException e) {
      throw new TemplateTagException(String.format(
          "Cannot read the templates releases of %s (%s). %s", repositoryUrl, e.getMessage(), pinAdvice(major)), e);
    }

    return TemplatesContract.latestTag(major, tags)
        .orElseThrow(() -> new TemplateTagException(String.format(
            "%s has no templates release v%d.x.y for this plugin yet. %s", repositoryUrl, major, pinAdvice(major))));
  }

  private static String pinAdvice(int major) {
    return String.format("Pin one in .cleanarch.yml with templates.version (for example v%d.0.0) "
        + "or templates.branch.", major);
  }

  private record Resolved(String tag, Instant expiresAt) {
  }

  /** The latest templates tag could not be resolved. */
  public static class TemplateTagException extends RuntimeException {
    public TemplateTagException(String message) {
      super(message);
    }

    public TemplateTagException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
