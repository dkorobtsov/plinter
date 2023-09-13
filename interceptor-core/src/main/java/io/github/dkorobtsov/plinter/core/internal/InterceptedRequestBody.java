/*
 * Copyright (C) 2014 Square, Inc.
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

package io.github.dkorobtsov.plinter.core.internal;

import okio.BufferedSink;
import okio.ByteString;
import okio.Okio;
import okio.Source;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * NB: Class copied with some small modifications from OkHttp3 client (removed external dependencies
 * and unused methods). Idea was to remove hard dependency on OkHttp3, so request/response handling
 * logic was made a part of this library.
 *
 * <p>See <a href="https://github.com/square/okhttp">OkHttp3</a>.
 */
@SuppressWarnings("PMD")
public abstract class InterceptedRequestBody {

  /**
   * Returns a new request body that transmits {@code content}. If {@code contentType} is non-null
   * and lacks a charset, this will use UTF-8.
   */
  public static InterceptedRequestBody create(InterceptedMediaType contentType, String content) {
    Charset charset = Util.UTF_8;
    if (contentType != null) {
      charset = contentType.charset();
      if (charset == null) {
        charset = Util.UTF_8;
        contentType = InterceptedMediaType.parse(contentType + "; charset=utf-8");
      }
    }
    final byte[] bytes = content.getBytes(charset);
    return create(contentType, bytes);
  }

  /**
   * Returns a new request body that transmits {@code content}.
   */
  public static InterceptedRequestBody create(
    final InterceptedMediaType contentType, final ByteString content) {
    return new InterceptedRequestBody() {
      @Override
      public InterceptedMediaType contentType() {
        return contentType;
      }

      @Override
      public long contentLength() throws IOException {
        return content.size();
      }

      @Override
      public void writeTo(BufferedSink sink) throws IOException {
        sink.write(content);
      }
    };
  }

  /**
   * Returns a new request body that transmits {@code content}.
   */
  public static InterceptedRequestBody create(final InterceptedMediaType contentType,
                                              final byte[] content) {
    return create(contentType, content, 0, content.length);
  }

  /**
   * Returns a new request body that transmits {@code content}.
   */
  public static InterceptedRequestBody create(final InterceptedMediaType contentType,
                                              final byte[] content,
                                              final int offset, final int byteCount) {
    if (content == null) {
      throw new NullPointerException("content == null");
    }
    Util.checkOffsetAndCount(content.length, offset, byteCount);
    return new InterceptedRequestBody() {
      @Override
      public InterceptedMediaType contentType() {
        return contentType;
      }

      @Override
      public long contentLength() {
        return byteCount;
      }

      @Override
      public void writeTo(BufferedSink sink) throws IOException {
        sink.write(content, offset, byteCount);
      }
    };
  }

  /**
   * Returns a new request body that transmits the content of {@code file}.
   */
  public static InterceptedRequestBody create(final InterceptedMediaType contentType,
                                              final File file) {
    if (file == null) {
      throw new NullPointerException("content == null");
    }

    return new InterceptedRequestBody() {
      @Override
      public InterceptedMediaType contentType() {
        return contentType;
      }

      @Override
      public long contentLength() {
        return file.length();
      }

      @Override
      public void writeTo(BufferedSink sink) throws IOException {
        Source source = null;
        try {
          source = Okio.source(file);
          sink.writeAll(source);
        } finally {
          Util.closeQuietly(source);
        }
      }
    };
  }

  /**
   * Returns the Content-Type header for this body.
   */
  @SuppressWarnings("unused")
  public abstract InterceptedMediaType contentType();

  /**
   * Returns the number of bytes that will be written to {@code sink} in a call to {@link #writeTo},
   * or -1 if that count is unknown.
   */
  @SuppressWarnings("unused")
  public long contentLength() throws IOException {
    return -1;
  }

  /**
   * Writes the content of this request to {@code sink}.
   */
  public abstract void writeTo(BufferedSink sink) throws IOException;
}
