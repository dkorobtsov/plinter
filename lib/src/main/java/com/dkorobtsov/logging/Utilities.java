package com.dkorobtsov.logging;

import static com.dkorobtsov.logging.internal.Util.delimiterOffset;

import com.dkorobtsov.logging.internal.InterceptedRequest;
import com.dkorobtsov.logging.internal.InterceptedRequestBody;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Utilities {

    private Utilities() {

    }

    public static String slashSegments(List<String> segments) {
        StringBuilder segmentString = new StringBuilder();
        for (String segment : segments) {
            segmentString.append("/").append(segment);
        }
        return segmentString.toString();
    }

    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    public static boolean hasPrintableBody(final String subtype) {
        return (subtype != null && (subtype.contains("json")
            || subtype.contains("xml")
            || subtype.contains("plain")
            || subtype.contains("html")));
    }

    public static String subtype(InterceptedRequest request) {
        final InterceptedRequestBody requestBody = request.body();

        String requestSubtype = null;
        if (requestBody != null && requestBody.contentType() != null) {
            requestSubtype = Objects.requireNonNull(requestBody.contentType()).subtype();
        }
        return requestSubtype;
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
