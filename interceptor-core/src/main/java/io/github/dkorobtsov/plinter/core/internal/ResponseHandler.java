package io.github.dkorobtsov.plinter.core.internal;

import java.net.URL;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.isNull;

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
    final InterceptedResponseBody responseBody = response.responseBody;

    try (responseBody) {
      final InterceptedMediaType contentType = isNull(responseBody)
        ? null
        : responseBody.contentType();
      final List<String> segmentList = isNull(requestUrl)
        ? Collections.emptyList()
        : Util.encodedPathSegments(requestUrl);
      final String url = isNull(requestUrl)
        ? ""
        : requestUrl.toString();
      return InterceptedResponse.builder()
        .url(url)
        .code(code)
        .message(message)
        .segmentList(segmentList)
        .contentType(contentType)
        .isSuccessful(isSuccessful)
        .headers(response.headers)
        .responseBody(responseBody)
        .chainMs(isNull(chainMs) ? 0 : chainMs)
        .build();
    }
  }
}
