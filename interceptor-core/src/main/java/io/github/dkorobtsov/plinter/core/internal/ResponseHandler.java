package io.github.dkorobtsov.plinter.core.internal;

import static java.util.Objects.isNull;

import java.net.URL;
import java.util.Collections;
import java.util.List;

/**
 * Utility class intended to reduce code duplication on Response converters.
 */
public final class ResponseHandler {

  private ResponseHandler() {
  }

  public static InterceptedResponse interceptedResponse(ResponseDetails response,
      URL requestUrl, Long chainMs) {

    final List<String> segmentList = isNull(requestUrl)
        ? Collections.emptyList() : Util.encodedPathSegments(requestUrl);

    final int code = response.code;
    final String message = response.message;
    final boolean isSuccessful = response.isSuccessful;
    final InterceptedResponseBody responseBody = response.responseBody;

    final String url = isNull(requestUrl)
        ? ""
        : requestUrl.toString();

    final InterceptedMediaType contentType = isNull(responseBody)
        ? null
        : responseBody.contentType();

    return InterceptedResponse
        .builder()
        .url(url)
        .code(code)
        .headers(response.headers)
        .message(message)
        .segmentList(segmentList)
        .contentType(contentType)
        .responseBody(response.responseBody)
        .isSuccessful(isSuccessful)
        .chainMs(isNull(chainMs) ? 0 : chainMs)
        .build();
  }

}
