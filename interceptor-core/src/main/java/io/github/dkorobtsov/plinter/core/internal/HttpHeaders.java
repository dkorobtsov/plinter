/*
 * Copyright (C) 2012 The Android Open Source Project
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

/**
 * Headers and utilities for internal use by OkHttp.
 *
 * --------------------------------------------------------------------------------------
 *
 * NB: Class copied with some small modifications  from OkHttp3 client (removed external
 * dependencies and unused methods). Idea was to remove hard dependency on OkHttp3, so
 * request/response handling logic was made a part of this library.
 *
 * <p>See <a href="https://github.com/square/okhttp">OkHttp3</a>.
 */
@SuppressWarnings("PMD")
final class HttpHeaders {

  private HttpHeaders() {
  }

  /**
   * Returns the next index in {@code input} at or after {@code pos} that contains a character from
   * {@code characters}. Returns the input length if none of the requested characters can be found.
   */
  static int skipUntil(String input, int pos, String characters) {
    for (; pos < input.length(); pos++) {
      if (characters.indexOf(input.charAt(pos)) != -1) {
        break;
      }
    }
    return pos;
  }

  /**
   * Returns the next non-whitespace character in {@code input} that is white space. Result is
   * undefined if input contains newline characters.
   */
  @SuppressWarnings("ParameterAssignment")
  static int skipWhitespace(String input, int pos) {
    for (; pos < input.length(); pos++) {
      final char c = input.charAt(pos);
      if (c != ' ' && c != '\t') {
        break;
      }
    }
    return pos;
  }

  /**
   * Returns {@code value} as a positive integer, or 0 if it is negative, or {@code defaultValue} if
   * it cannot be parsed.
   */
  static int parseSeconds(String value, int defaultValue) {
    try {
      final long seconds = Long.parseLong(value);
      if (seconds > Integer.MAX_VALUE) {
        return Integer.MAX_VALUE;
      } else if (seconds < 0) {
        return 0;
      } else {
        return (int) seconds;
      }
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }
}
