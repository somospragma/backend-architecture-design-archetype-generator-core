package com.pragma.archetype.infrastructure.adapter.out.template;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Comparator;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Downloads one ref of a GitHub templates repository as a zip archive and
 * extracts its templates folder, so the plugin reads remote templates from
 * disk like local ones.
 *
 * <p>Raw file URLs cannot list a directory, and the plugin discovers the files
 * of each adapter by listing its folder. A tag never changes, so its archive is
 * extracted once and reused, also offline. A branch is downloaded again on
 * every build.</p>
 */
public class GitHubTemplateArchive {

  private static final Pattern GITHUB_REPOSITORY =
      Pattern.compile("github\\.com[/:]([^/]+)/([^/]+?)(?:\\.git)?/?$");
  private static final String COMPLETE_MARKER = ".complete";

  private final HttpClient httpClient;
  private final String archiveBaseUrl;
  private final Path cacheDir;

  public GitHubTemplateArchive() {
    this(HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(30))
            .build(),
        "https://codeload.github.com",
        Paths.get(System.getProperty("user.home"), ".cleanarch", "templates-cache", "archives"));
  }

  GitHubTemplateArchive(HttpClient httpClient, String archiveBaseUrl, Path cacheDir) {
    this.httpClient = httpClient;
    this.archiveBaseUrl = archiveBaseUrl;
    this.cacheDir = cacheDir;
  }

  /** Owner and name of a GitHub repository URL, or empty for other hosts. */
  public static Optional<String[]> ownerAndName(String repositoryUrl) {
    if (repositoryUrl == null) {
      return Optional.empty();
    }
    Matcher m = GITHUB_REPOSITORY.matcher(repositoryUrl.trim());
    return m.find() ? Optional.of(new String[] {m.group(1), m.group(2)}) : Optional.empty();
  }

  /**
   * Templates folder of a ref, downloading and extracting it when needed.
   *
   * @param repositoryUrl GitHub repository URL
   * @param ref           tag or branch
   * @param immutable     true for a tag: an extracted copy is reused
   * @return the extracted templates folder
   */
  public Path templatesDir(String repositoryUrl, String ref, boolean immutable) {
    String[] repo = ownerAndName(repositoryUrl).orElseThrow(() -> new IllegalArgumentException(
        "Only GitHub repositories are supported for remote templates: " + repositoryUrl));
    Path target = cacheDir.resolve(repo[0]).resolve(repo[1]).resolve(safeName(ref));
    Path templates = target.resolve(GitHubTemplateDownloader.TEMPLATES_DIR);
    if (immutable && Files.exists(target.resolve(COMPLETE_MARKER)) && Files.isDirectory(templates)) {
      return templates;
    }
    download(repo[0], repo[1], ref, target);
    return templates;
  }

  private void download(String owner, String name, String ref, Path target) {
    URI uri = URI.create(String.format("%s/%s/%s/zip/%s", archiveBaseUrl, owner, name, ref));
    HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofMinutes(2)).GET().build();
    Path staging = null;
    try {
      HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
      try (InputStream body = response.body()) {
        if (response.statusCode() != 200) {
          throw new TemplateArchiveException(String.format(
              "Cannot download templates %s of %s/%s (HTTP %d). Check that the tag or branch exists.",
              ref, owner, name, response.statusCode()));
        }
        Files.createDirectories(target.getParent());
        staging = Files.createTempDirectory(target.getParent(), ".download-");
        extractTemplates(body, staging);
      }
      if (!Files.isDirectory(staging.resolve(GitHubTemplateDownloader.TEMPLATES_DIR))) {
        throw new TemplateArchiveException(String.format(
            "%s/%s at %s has no %s folder", owner, name, ref, GitHubTemplateDownloader.TEMPLATES_DIR));
      }
      Files.writeString(staging.resolve(COMPLETE_MARKER), ref);
      deleteRecursively(target);
      Files.move(staging, target, StandardCopyOption.ATOMIC_MOVE);
      staging = null;
    } catch (IOException e) {
      throw new TemplateArchiveException(String.format(
          "Cannot download templates %s of %s/%s: %s. Without network, pin a templates.version "
              + "that was already downloaded.", ref, owner, name, e.getMessage()), e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new TemplateArchiveException("Interrupted while downloading templates " + ref, e);
    } finally {
      if (staging != null) {
        deleteRecursively(staging);
      }
    }
  }

  /** Extracts the templates folder of a GitHub archive, whose entries start with "repo-ref/". */
  private static void extractTemplates(InputStream zip, Path destination) throws IOException {
    String prefix = GitHubTemplateDownloader.TEMPLATES_DIR + "/";
    try (ZipInputStream in = new ZipInputStream(zip)) {
      ZipEntry entry;
      while ((entry = in.getNextEntry()) != null) {
        String name = entry.getName();
        int slash = name.indexOf('/');
        String relative = slash < 0 ? "" : name.substring(slash + 1);
        if (!relative.startsWith(prefix)) {
          continue;
        }
        Path out = destination.resolve(relative).normalize();
        if (!out.startsWith(destination)) {
          throw new IOException("Archive entry outside the target folder: " + name);
        }
        if (entry.isDirectory()) {
          Files.createDirectories(out);
        } else {
          Files.createDirectories(out.getParent());
          Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
        }
      }
    }
  }

  private static String safeName(String ref) {
    return ref.replaceAll("[^A-Za-z0-9._-]", "_");
  }

  private static void deleteRecursively(Path path) {
    if (!Files.exists(path)) {
      return;
    }
    try (Stream<Path> walk = Files.walk(path)) {
      walk.sorted(Comparator.reverseOrder()).forEach(p -> {
        try {
          Files.delete(p);
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      });
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /** The templates archive could not be downloaded or read. */
  public static class TemplateArchiveException extends RuntimeException {
    public TemplateArchiveException(String message) {
      super(message);
    }

    public TemplateArchiveException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
