package com.pragma.archetype.domain.port.out;

/**
 * Port for HTTP client operations.
 */
public interface HttpClientPort {

  /**
   * Exception thrown when HTTP download fails.
   */
  class HttpDownloadException extends RuntimeException {
    public HttpDownloadException(String message) {
      super(message);
    }

    public HttpDownloadException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  /**
   * Downloads content from a URL.
   *
   * @param url URL to download from
   * @return content as string
   * @throws HttpDownloadException if download fails
   */
  String downloadContent(String url);

  /**
   * Checks if a URL is accessible.
   *
   * @param url URL to check
   * @return true if accessible, false otherwise
   */
  boolean isAccessible(String url);
}
