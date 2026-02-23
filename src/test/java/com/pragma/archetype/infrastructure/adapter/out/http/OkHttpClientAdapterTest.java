package com.pragma.archetype.infrastructure.adapter.out.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.pragma.archetype.domain.port.out.HttpClientPort.HttpDownloadException;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@DisplayName("OkHttpClientAdapter Tests")
class OkHttpClientAdapterTest {

  private MockWebServer mockWebServer;
  private OkHttpClientAdapter adapter;
  private String baseUrl;

  @BeforeEach
  void setUp() throws IOException {
    mockWebServer = new MockWebServer();
    mockWebServer.start();
    baseUrl = mockWebServer.url("/").toString();
    adapter = new OkHttpClientAdapter();
  }

  @AfterEach
  void tearDown() throws IOException {
    mockWebServer.shutdown();
  }

  @Test
  @DisplayName("Should download content successfully")
  void shouldDownloadContentSuccessfully() {
    mockWebServer.enqueue(new MockResponse()
        .setBody("template content")
        .setResponseCode(200));

    String result = adapter.downloadContent(baseUrl + "template.ftl");

    assertEquals("template content", result);
  }

  @Test
  @DisplayName("Should download large content")
  void shouldDownloadLargeContent() {
    String largeContent = "x".repeat(100000);
    mockWebServer.enqueue(new MockResponse()
        .setBody(largeContent)
        .setResponseCode(200));

    String result = adapter.downloadContent(baseUrl + "large.txt");

    assertEquals(largeContent, result);
  }

  @Test
  @DisplayName("Should download empty content")
  void shouldDownloadEmptyContent() {
    mockWebServer.enqueue(new MockResponse()
        .setBody("")
        .setResponseCode(200));

    String result = adapter.downloadContent(baseUrl + "empty.txt");

    assertEquals("", result);
  }

  @Test
  @DisplayName("Should follow redirects")
  void shouldFollowRedirects() {
    mockWebServer.enqueue(new MockResponse()
        .setResponseCode(302)
        .setHeader("Location", baseUrl + "redirected"));
    mockWebServer.enqueue(new MockResponse()
        .setBody("redirected content")
        .setResponseCode(200));

    String result = adapter.downloadContent(baseUrl + "original");

    assertEquals("redirected content", result);
  }

  @Test
  @DisplayName("Should throw exception on 404")
  void shouldThrowExceptionOn404() {
    mockWebServer.enqueue(new MockResponse()
        .setResponseCode(404));

    HttpDownloadException exception = assertThrows(
        HttpDownloadException.class,
        () -> adapter.downloadContent(baseUrl + "notfound.txt"));

    assertTrue(exception.getMessage().contains("404"));
  }

  @Test
  @DisplayName("Should throw exception on 500")
  void shouldThrowExceptionOn500() {
    mockWebServer.enqueue(new MockResponse()
        .setResponseCode(500));

    HttpDownloadException exception = assertThrows(
        HttpDownloadException.class,
        () -> adapter.downloadContent(baseUrl + "error.txt"));

    assertTrue(exception.getMessage().contains("500"));
  }

  @Test
  @DisplayName("Should throw exception on 403")
  void shouldThrowExceptionOn403() {
    mockWebServer.enqueue(new MockResponse()
        .setResponseCode(403));

    HttpDownloadException exception = assertThrows(
        HttpDownloadException.class,
        () -> adapter.downloadContent(baseUrl + "forbidden.txt"));

    assertTrue(exception.getMessage().contains("403"));
  }

  @Test
  @DisplayName("Should return true for accessible URL")
  void shouldReturnTrueForAccessibleUrl() {
    mockWebServer.enqueue(new MockResponse()
        .setResponseCode(200));

    boolean result = adapter.isAccessible(baseUrl + "accessible.txt");

    assertTrue(result);
  }

  @Test
  @DisplayName("Should return false for 404")
  void shouldReturnFalseFor404() {
    mockWebServer.enqueue(new MockResponse()
        .setResponseCode(404));

    boolean result = adapter.isAccessible(baseUrl + "notfound.txt");

    assertFalse(result);
  }

  @Test
  @DisplayName("Should return false for 500")
  void shouldReturnFalseFor500() {
    mockWebServer.enqueue(new MockResponse()
        .setResponseCode(500));

    boolean result = adapter.isAccessible(baseUrl + "error.txt");

    assertFalse(result);
  }

  @Test
  @DisplayName("Should return false for connection errors")
  void shouldReturnFalseForConnectionErrors() {
    String invalidUrl = "http://invalid-host-12345.com/file.txt";

    boolean result = adapter.isAccessible(invalidUrl);

    assertFalse(result);
  }

  @Test
  @DisplayName("Should handle multiple consecutive requests")
  void shouldHandleMultipleConsecutiveRequests() {
    mockWebServer.enqueue(new MockResponse().setBody("content1").setResponseCode(200));
    mockWebServer.enqueue(new MockResponse().setBody("content2").setResponseCode(200));
    mockWebServer.enqueue(new MockResponse().setBody("content3").setResponseCode(200));

    String result1 = adapter.downloadContent(baseUrl + "file1.txt");
    String result2 = adapter.downloadContent(baseUrl + "file2.txt");
    String result3 = adapter.downloadContent(baseUrl + "file3.txt");

    assertEquals("content1", result1);
    assertEquals("content2", result2);
    assertEquals("content3", result3);
  }
}
