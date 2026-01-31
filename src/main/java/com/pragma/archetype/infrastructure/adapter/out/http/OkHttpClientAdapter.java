package com.pragma.archetype.infrastructure.adapter.out.http;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.pragma.archetype.domain.port.out.HttpClientPort;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * HTTP client adapter using OkHttp.
 * Used for downloading templates from remote repositories.
 */
public class OkHttpClientAdapter implements HttpClientPort {

  private final OkHttpClient client;

  public OkHttpClientAdapter() {
    this.client = new OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build();
  }

  @Override
  public String downloadContent(String url) {
    Request request = new Request.Builder()
        .url(url)
        .get()
        .build();

    try (Response response = client.newCall(request).execute()) {
      if (!response.isSuccessful()) {
        throw new HttpDownloadException(
            "Failed to download from " + url + ". HTTP " + response.code());
      }

      if (response.body() == null) {
        throw new HttpDownloadException("Empty response body from " + url);
      }

      return response.body().string();

    } catch (IOException e) {
      throw new HttpDownloadException("Failed to download from " + url, e);
    }
  }

  @Override
  public boolean isAccessible(String url) {
    Request request = new Request.Builder()
        .url(url)
        .head()
        .build();

    try (Response response = client.newCall(request).execute()) {
      return response.isSuccessful();
    } catch (IOException e) {
      return false;
    }
  }
}
