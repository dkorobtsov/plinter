/*
 * Copyright (C) 2013 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dkorobtsov.logging.internal;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * An HTTP request. Instances of this class are immutable if their {@link #body} is null or itself
 * immutable.
 *
 * --------------------------------------------------------------------------------------
 *
 * NB: Class copied with some small modifications from OkHttp3 client (removed external dependencies
 * and unused methods). Idea was to remove hard dependency on OkHttp3, so request/response handling
 * logic was made a part of this library.
 *
 * See <a href="https://github.com/square/okhttp">OkHttp3</a>
 */
@SuppressWarnings("PMD")
public final class InterceptedRequest {

  private static final String URL_IS_NULL_ERROR = "url == null";
  final URL url;
  final String method;
  final InterceptedHeaders headers;
  final InterceptedRequestBody body;
  final Object tag;

  InterceptedRequest(Builder builder) {
    this.url = builder.url;
    this.method = builder.method;
    this.headers = builder.headers.build();
    this.body = builder.body;
    this.tag = builder.tag != null ? builder.tag : this;
  }

  public URL url() {
    return url;
  }

  public String method() {
    return method;
  }

  public String header(String name) {
    return headers.get(name);
  }

  public InterceptedHeaders headers() {
    return headers;
  }

  public List<String> headers(String name) {
    return headers.values(name);
  }

  public InterceptedRequestBody body() {
    return body;
  }

  public Builder newBuilder() {
    return new Builder(this);
  }

  @Override
  public String toString() {
    return "Request{method="
        + method
        + ", url="
        + url
        + ", tag="
        + (tag != this ? tag : null)
        + '}';
  }

  @SuppressWarnings("JavadocType")
  public static class Builder {

    private URL url;
    private String method;
    private InterceptedHeaders.Builder headers;
    private InterceptedRequestBody body;
    private Object tag;

    public Builder() {
      this.method = "GET";
      this.headers = new InterceptedHeaders.Builder();
    }

    Builder(InterceptedRequest request) {
      this.url = request.url;
      this.method = request.method;
      this.body = request.body;
      this.tag = request.tag;
      this.headers = request.headers.newBuilder();
    }

    public Builder url(URL url) {
      if (url == null) {
        throw new NullPointerException(URL_IS_NULL_ERROR);
      }
      this.url = url;
      return this;
    }

    public Builder url(String url) {
      if (url == null) {
        throw new NullPointerException(URL_IS_NULL_ERROR);
      }

      // Silently replace web socket URLs with HTTP URLs.
      if (url.regionMatches(true, 0, "ws:", 0, 3)) {
        url = "http:" + url.substring(3);
      } else if (url.regionMatches(true, 0, "wss:", 0, 4)) {
        url = "https:" + url.substring(4);
      }

      final URL parsed;
      try {
        parsed = new URL(url);
      } catch (MalformedURLException e) {
        throw new IllegalArgumentException("unexpected url: " + url, e);
      }
      return url(parsed);
    }

    /**
     * Sets the header named {@code name} to {@code value}. If this request already has any headers
     * with that name, they are all replaced.
     */
    public Builder header(String name, String value) {
      headers.set(name, value);
      return this;
    }

    /**
     * Adds a header with {@code name} and {@code value}. Prefer this method for multiply-valued
     * headers like "Cookie".
     *
     * <p>Note that for some headers including {@code Content-Length} and {@code
     * Content-Encoding}, OkHttp may replace {@code value} with a header derived from the request
     * body.
     */
    public Builder addHeader(String name, String value) {
      headers.add(name, value);
      return this;
    }

    public Builder removeHeader(String name) {
      headers.removeAll(name);
      return this;
    }

    /**
     * Removes all headers on this builder and adds {@code headers}.
     */
    public Builder headers(InterceptedHeaders headers) {
      this.headers = headers.newBuilder();
      return this;
    }

    /**
     * Sets this request's {@code Cache-Control} header, replacing any cache control headers already
     * present. If {@code cacheControl} doesn't define any directives, this clears this request's
     * cache-control headers.
     */
    public Builder cacheControl(CacheControl cacheControl) {
      final String value = cacheControl.toString();
      if (value.isEmpty()) {
        return removeHeader("Cache-Control");
      }
      return header("Cache-Control", value);
    }

    public Builder get() {
      return method("GET", null);
    }

    public Builder method(String method, InterceptedRequestBody body) {
      if (method == null) {
        throw new NullPointerException("method == null");
      }
      if (method.length() == 0) {
        throw new IllegalArgumentException("method.length() == 0");
      }
      if (body != null && !HttpMethod.permitsRequestBody(method)) {
        throw new IllegalArgumentException(
            "method " + method + " must not have a request body.");
      }
      if (body == null && HttpMethod.requiresRequestBody(method)) {
        throw new IllegalArgumentException(
            "method " + method + " must have a request body.");
      }
      this.method = method;
      this.body = body;
      return this;
    }

    /**
     * Attaches {@code tag} to the request. It can be used later to cancel the request. If the tag
     * is unspecified or null, the request is canceled by using the request itself as the tag.
     */
    public Builder tag(Object tag) {
      this.tag = tag;
      return this;
    }

    public InterceptedRequest build() {
      if (url == null) {
        throw new IllegalStateException(URL_IS_NULL_ERROR);
      }
      return new InterceptedRequest(this);
    }
  }
}
