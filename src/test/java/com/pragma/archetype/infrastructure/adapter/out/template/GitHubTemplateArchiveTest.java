package com.pragma.archetype.infrastructure.adapter.out.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okio.Buffer;

class GitHubTemplateArchiveTest {

  private static final String REPO = "https://github.com/acme/templates-repo";

  @TempDir
  Path cacheDir;

  private MockWebServer server;
  private GitHubTemplateArchive archive;

  @BeforeEach
  void setUp() throws IOException {
    server = new MockWebServer();
    server.start();
    String base = server.url("/").toString().replaceAll("/$", "");
    archive = new GitHubTemplateArchive(HttpClient.newHttpClient(), base, cacheDir);
  }

  @AfterEach
  void tearDown() throws IOException {
    server.shutdown();
  }

  @Test
  void extractsOnlyTheTemplatesFolder() throws Exception {
    server.enqueue(zipResponse(Map.of(
        "templates-repo-1.0.1/README.md", "readme",
        "templates-repo-1.0.1/templates/architectures/index.yml", "schemaVersion: 1",
        "templates-repo-1.0.1/templates/frameworks/spring/reactive/adapters/x/Adapter.java.ftl", "class X {}")));

    Path templates = archive.templatesDir(REPO, "v1.0.1", true);

    assertEquals("schemaVersion: 1", Files.readString(templates.resolve("architectures/index.yml")));
    assertTrue(Files.exists(templates.resolve("frameworks/spring/reactive/adapters/x/Adapter.java.ftl")));
    assertFalse(Files.exists(templates.getParent().resolve("README.md")));
    assertEquals("/acme/templates-repo/zip/v1.0.1", server.takeRequest().getPath());
  }

  @Test
  void reusesAnExtractedTag() throws Exception {
    server.enqueue(zipResponse(Map.of("r-v1/templates/architectures/index.yml", "schemaVersion: 1")));

    Path first = archive.templatesDir(REPO, "v1.0.0", true);
    Path second = archive.templatesDir(REPO, "v1.0.0", true);

    assertEquals(first, second);
    assertEquals(1, server.getRequestCount());
  }

  @Test
  void downloadsABranchAgain() throws Exception {
    server.enqueue(zipResponse(Map.of("r-main/templates/a.ftl", "old")));
    server.enqueue(zipResponse(Map.of("r-main/templates/a.ftl", "new")));

    archive.templatesDir(REPO, "main", false);
    Path templates = archive.templatesDir(REPO, "main", false);

    assertEquals("new", Files.readString(templates.resolve("a.ftl")));
    assertEquals(2, server.getRequestCount());
  }

  @Test
  void failsWhenTheRefDoesNotExist() {
    server.enqueue(new MockResponse().setResponseCode(404));

    var e = assertThrows(GitHubTemplateArchive.TemplateArchiveException.class,
        () -> archive.templatesDir(REPO, "v9.9.9", true));

    assertTrue(e.getMessage().contains("HTTP 404"));
  }

  @Test
  void failsWhenTheArchiveHasNoTemplatesFolder() throws Exception {
    server.enqueue(zipResponse(Map.of("r-main/README.md", "readme")));

    assertThrows(GitHubTemplateArchive.TemplateArchiveException.class,
        () -> archive.templatesDir(REPO, "main", false));
  }

  @Test
  void rejectsEntriesOutsideTheTarget() throws Exception {
    server.enqueue(zipResponse(Map.of("r-main/templates/../../evil.txt", "x")));

    assertThrows(GitHubTemplateArchive.TemplateArchiveException.class,
        () -> archive.templatesDir(REPO, "main", false));
    assertFalse(Files.exists(cacheDir.resolve("acme/evil.txt")));
  }

  @Test
  void parsesGitHubRepositories() {
    assertEquals("acme", GitHubTemplateArchive.ownerAndName(REPO + ".git").orElseThrow()[0]);
    assertEquals("templates-repo", GitHubTemplateArchive.ownerAndName(REPO + "/").orElseThrow()[1]);
    assertTrue(GitHubTemplateArchive.ownerAndName("https://gitlab.com/acme/repo").isEmpty());
    assertTrue(GitHubTemplateArchive.ownerAndName(null).isEmpty());
    assertThrows(IllegalArgumentException.class,
        () -> archive.templatesDir("https://gitlab.com/acme/repo", "main", false));
  }

  private static MockResponse zipResponse(Map<String, String> files) throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
      for (var file : files.entrySet()) {
        zip.putNextEntry(new ZipEntry(file.getKey()));
        zip.write(file.getValue().getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
      }
    }
    return new MockResponse().setResponseCode(200).setBody(new Buffer().write(bytes.toByteArray()));
  }
}
