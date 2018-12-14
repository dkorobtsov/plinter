package com.dkorobtsov.logging.internal;

import static com.dkorobtsov.logging.internal.Util.encodedPathSegments;
import static com.dkorobtsov.logging.internal.Util.hasPrintableBody;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class intended to reduce code duplication on Response converters.
 */
public class ResponseHandler {

  private static final Logger logger = Logger.getLogger(ResponseHandler.class.getName());

  private ResponseHandler() {
  }

  public static InterceptedResponse interceptedResponse(ResponseDetails response,
      URL requestUrl, Long chainMs) {

    // Trying to determine if body should be pretty printed or omitted as file request
    String mediaType = null;
    if (Objects.requireNonNull(response.responseBody).contentType() != null) {
      mediaType = Objects.requireNonNull(response.responseBody.contentType()).subtype();
    }
    final boolean hasPrintableBody = hasPrintableBody(mediaType);

    final List<String> segmentList = Objects.isNull(requestUrl)
        ? Collections.emptyList() : encodedPathSegments(requestUrl);

    final String header = response.headers.toString();
    final int code = response.code;
    final boolean isSuccessful = response.isSuccessful;
    final String message = response.message;
    final InterceptedResponseBody responseBody = response.responseBody;
    final InterceptedMediaType contentType = Objects.requireNonNull(responseBody).contentType();
    final String url = Objects.isNull(requestUrl) ? "" : requestUrl.toString();

    final byte[] originalBody = originalBodyFrom(responseBody);

    return InterceptedResponse
        .builder()
        .segmentList(segmentList)
        .header(header)
        .code(code)
        .isSuccessful(isSuccessful)
        .message(message)
        .originalBody(originalBody)
        .contentType(contentType)
        .hasPrintableBody(hasPrintableBody)
        .url(url)
        .chainMs(Objects.isNull(chainMs) ? 0 : chainMs)
        .build();
  }

  private static byte[] originalBodyFrom(InterceptedResponseBody responseBody) {
    byte[] originalBody;
    try {
      originalBody = responseBody.bytes();
    } catch (IOException e) {
      originalBody = "".getBytes();
      logger.log(Level.SEVERE, e.getMessage(), e);
    }
    return originalBody;
  }

}
