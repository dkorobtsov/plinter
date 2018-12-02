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
package com.dkorobtsov.logging.internal;

import static java.util.Objects.isNull;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import okio.BufferedSource;
import okio.ByteString;

/**
 * Junk drawer of utility methods.
 *
 * --------------------------------------------------------------------------------------
 *
 * NB: Class copied with some small modifications  from OkHttp3 client (removed external
 * dependencies and unused methods). Idea was to remove hard dependency on OkHttp3, so
 * request/response handling logic was made a part of this library.
 *
 * <p>See <a href="https://github.com/square/okhttp">OkHttp3</a>.
 */
public final class Util {

    static final Charset UTF_8 = Charset.forName("UTF-8");
    private static final ByteString UTF_8_BOM = ByteString.decodeHex("efbbbf");
    private static final ByteString UTF_16_BE_BOM = ByteString.decodeHex("feff");
    private static final ByteString UTF_16_LE_BOM = ByteString.decodeHex("fffe");
    private static final ByteString UTF_32_BE_BOM = ByteString.decodeHex("0000ffff");
    private static final ByteString UTF_32_LE_BOM = ByteString.decodeHex("ffff0000");
    private static final Charset UTF_16_BE = Charset.forName("UTF-16BE");
    private static final Charset UTF_16_LE = Charset.forName("UTF-16LE");
    private static final Charset UTF_32_BE = Charset.forName("UTF-32BE");
    private static final Charset UTF_32_LE = Charset.forName("UTF-32LE");

    private Util() {
    }

    public static void checkOffsetAndCount(long arrayLength, long offset, long count) {
        if ((offset | count) < 0 || offset > arrayLength || arrayLength - offset < count) {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    /**
     * Closes {@code closeable}, ignoring any checked exceptions. Does nothing if {@code closeable}
     * is null.
     */
    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Returns the index of the first character in {@code input} that contains a character in {@code
     * delimiters}. Returns limit if there is no such character.
     */
    public static int delimiterOffset(String input, int pos, int limit, String delimiters) {
        for (int i = pos; i < limit; i++) {
            if (delimiters.indexOf(input.charAt(i)) != -1) {
                return i;
            }
        }
        return limit;
    }

    /**
     * Returns the index of the first character in {@code input} that is {@code delimiter}. Returns
     * limit if there is no such character.
     */
    public static int delimiterOffset(String input, int pos, int limit, char delimiter) {
        for (int i = pos; i < limit; i++) {
            if (input.charAt(i) == delimiter) {
                return i;
            }
        }
        return limit;
    }

    /**
     * Returns a {@link Locale#US} formatted {@link String}.
     */
    public static String format(String format, Object... args) {
        return String.format(Locale.US, format, args);
    }

    public static Charset bomAwareCharset(BufferedSource source, Charset charset)
        throws IOException {
        if (source.rangeEquals(0, UTF_8_BOM)) {
            source.skip(UTF_8_BOM.size());
            return UTF_8;
        }
        if (source.rangeEquals(0, UTF_16_BE_BOM)) {
            source.skip(UTF_16_BE_BOM.size());
            return UTF_16_BE;
        }
        if (source.rangeEquals(0, UTF_16_LE_BOM)) {
            source.skip(UTF_16_LE_BOM.size());
            return UTF_16_LE;
        }
        if (source.rangeEquals(0, UTF_32_BE_BOM)) {
            source.skip(UTF_32_BE_BOM.size());
            return UTF_32_BE;
        }
        if (source.rangeEquals(0, UTF_32_LE_BOM)) {
            source.skip(UTF_32_LE_BOM.size());
            return UTF_32_LE;
        }
        return charset;
    }

    /**
     * Returns a list of encoded path segments like {@code ["a", "b", "c"]} for the URL {@code
     * http://host/a/b/c}. This list is never empty though it may contain a single empty string.
     *
     * <p><table summary="">
     * <tr><th>URL</th><th>{@code encodedPathSegments()}</th></tr>
     * <tr><td>{@code http://host/}</td><td>{@code [""]}</td></tr>
     * <tr><td>{@code http://host/a/b/c}</td><td>{@code ["a", "b", "c"]}</td></tr>
     * <tr><td>{@code http://host/a/b%20c/d}</td><td>{@code ["a", "b%20c", "d"]}</td></tr>
     * </table>
     *
     * --------------------------------------------------------------------------------------
     *
     * NB: Method copied with some small modifications from OkHttp3 client's HttpUrl. In order to
     * remove hard dependency from OkHttp3 this library uses java native URL class. This method
     * copied for convenience.
     *
     * <p>See <a href="https://github.com/square/okhttp">OkHttp3</a>.
     */
    public static List<String> encodedPathSegments(URL url) {
        if (isNull(url)) {
            return Collections.emptyList();
        }
        String urlString = url.toString();
        String scheme = url.getProtocol();

        int pathStart = urlString.indexOf('/', scheme.length() + 3);
        int pathEnd = delimiterOffset(urlString, pathStart, urlString.length(), "?#");
        List<String> result = new ArrayList<>();
        for (int i = pathStart; i < pathEnd; ) {
            i++; // Skip the '/'.
            int segmentEnd = delimiterOffset(urlString, i, pathEnd, '/');
            result.add(urlString.substring(i, segmentEnd));
            i = segmentEnd;
        }
        return result;
    }

}
