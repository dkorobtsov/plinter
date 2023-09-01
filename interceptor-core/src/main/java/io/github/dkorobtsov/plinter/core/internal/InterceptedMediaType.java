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

package io.github.dkorobtsov.plinter.core.internal;

import java.nio.charset.Charset;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An <a href="http://tools.ietf.org/html/rfc2045">RFC 2045</a> Media Type, appropriate to describe
 * the content type of an HTTP request or response body.
 * <p>
 * --------------------------------------------------------------------------------------
 * <p>
 * NB: Class copied with some small modifications from OkHttp3 client (removed external dependencies
 * and unused methods). Idea was to remove hard dependency on OkHttp3, so request/response handling
 * logic was made a part of this library.
 * <p>
 * See <a href="https://github.com/square/okhttp">OkHttp3</a>
 */
@SuppressWarnings({
  "PMD.AvoidLiteralsInIfCondition",
  "PMD.DataflowAnomalyAnalysis",
  "PMD.AvoidFieldNameMatchingMethodName",
}) //by design
public final class InterceptedMediaType {

  private static final String TOKEN = "([a-zA-Z0-9-!#$%&'*+.^_`{|}~]+)";
  private static final String QUOTED = "\"([^\"]*)\"";
  private static final Pattern TYPE_SUBTYPE = Pattern.compile(TOKEN + "/" + TOKEN);
  private static final Pattern PARAMETER = Pattern.compile(
    ";\\s*(?:" + TOKEN + "=(?:" + TOKEN + "|" + QUOTED + "))?");

  private final String mediaType;
  private final String subtype;
  private final String charset;

  private InterceptedMediaType(String mediaType, String subtype, String charset) {
    this.mediaType = mediaType;
    this.subtype = subtype;
    this.charset = charset;
  }

  /**
   * Returns a media type for {@code string}, or null if {@code string} is not a well-formed media
   * type.
   */
  @SuppressWarnings({"MultipleStringLiterals", "PMD.SimplifyStartsWith"})
  public static InterceptedMediaType parse(String string) {
    final Matcher typeSubtype = TYPE_SUBTYPE.matcher(string);
    if (!typeSubtype.lookingAt()) {
      return null;
    }

    String charset = null;
    final Matcher parameter = PARAMETER.matcher(string);
    for (int s = typeSubtype.end(); s < string.length(); s = parameter.end()) {
      parameter.region(s, string.length());
      if (!parameter.lookingAt()) {
        return null; // This is not a well-formed media type.
      }

      final String name = parameter.group(1);
      if (!"charset".equalsIgnoreCase(name)) {
        continue;
      }
      final String charsetParameter = getCharsetParameter(parameter);
      if (charset != null && !charsetParameter.equalsIgnoreCase(charset)) {
        return null; // Multiple different charsets!
      }
      charset = charsetParameter;
    }

    final String subtype = typeSubtype.group(2).toLowerCase(Locale.US);
    return new InterceptedMediaType(string, subtype, charset);
  }

  private static String getCharsetParameter(Matcher parameter) {
    final String charsetParameter;
    final String token = parameter.group(2);
    if (token != null) {
      // If the token is 'single-quoted' it's invalid! But we're lenient and strip the quotes.
      charsetParameter =
        token.startsWith("'") && token.endsWith("'") && token.length() > 2
          ? token.substring(1, token.length() - 1)
          : token;
    } else {
      // Value is "double-quoted". That's valid and our regex group already strips the quotes.
      charsetParameter = parameter.group(3);
    }
    return charsetParameter;
  }

  /**
   * Returns a specific media subtype, such as "plain" or "png", "mpeg", "mp4" or "xml".
   */
  public String subtype() {
    return subtype;
  }

  /**
   * Returns the charset of this media type, or null if this media type doesn't specify a charset.
   */
  public Charset charset() {
    return charset(null);
  }

  /**
   * Returns the charset of this media type, or {@code defaultValue} if either this media type
   * doesn't specify a charset, of it its charset is unsupported by the current runtime.
   */
  public Charset charset(Charset defaultValue) {
    try {
      return charset != null ? Charset.forName(charset) : defaultValue;
    } catch (IllegalArgumentException e) {
      return defaultValue; // This charset is invalid or unsupported. Give up.
    }
  }

  /**
   * Returns the encoded media type, like "text/plain; charset=utf-8", appropriate for use in a
   * Content-Type header.
   */
  @Override
  public String toString() {
    return mediaType;
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof InterceptedMediaType && ((InterceptedMediaType) other).mediaType
      .equals(mediaType);
  }

  @Override
  public int hashCode() {
    return mediaType.hashCode();
  }
}
