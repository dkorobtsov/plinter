package io.github.dkorobtsov.plinter.core.internal;

import java.net.URL;
import java.util.Collections;
import java.util.List;

/**
 * Utility class intended to reduce code Response converters code duplication.
 */
@SuppressWarnings({
  "MissingJavadocMethod",
  "MissingJavadocType",
  "PMD"
})
public final class ResponseHandler {

  private ResponseHandler() {
  }

  public static InterceptedResponse interceptedResponse(ResponseDetails response,
                                                        URL requestUrl,
                                                        Long chainMs) {

    final int code = response.code;
    final String message = response.message;
    final boolean isSuccessful = response.isSuccessful;

    try (InterceptedResponseBody responseBody = response.responseBody) {
      final InterceptedMediaType contentType = responseBody != null
        ? responseBody.contentType()
        : null;
      final List<String> segmentList = requestUrl != null
        ? Util.encodedPathSegments(requestUrl)
        : Collections.emptyList();
      final String url = requestUrl != null
        ? requestUrl.toString()
        : "";
      return InterceptedResponse.builder()
        .url(url)
        .code(code)
        .message(message)
        .segmentList(segmentList)
        .contentType(contentType)
        .isSuccessful(isSuccessful)
        .headers(response.headers)
        .responseBody(responseBody)
        .chainMs(chainMs != null ? chainMs : 0)
        .build();
    }
  }
}
