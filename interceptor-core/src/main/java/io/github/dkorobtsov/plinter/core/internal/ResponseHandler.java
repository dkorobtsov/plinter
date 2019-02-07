package io.github.dkorobtsov.plinter.core.internal;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class intended to reduce code duplication on Response converters.
 */
public final class ResponseHandler {

  private static final Logger logger = Logger.getLogger(ResponseHandler.class.getName());

  private ResponseHandler() {
  }

  public static InterceptedResponse interceptedResponse(ResponseDetails response,
      URL requestUrl, Long chainMs) {

    // Trying to determine if body should be pretty printed or omitted as file content
    String mediaType = null;
    if (nonNull(response.responseBody) && nonNull(response.responseBody.contentType())) {
      mediaType = response.responseBody.contentType().subtype();
    }

    final boolean hasPrintableBody = Util.hasPrintableBody(mediaType);

    final List<String> segmentList = isNull(requestUrl)
        ? Collections.emptyList() : Util.encodedPathSegments(requestUrl);

    final int code = response.code;
    final String message = response.message;
    final String header = response.headers.toString();
    final boolean isSuccessful = response.isSuccessful;
    final InterceptedResponseBody responseBody = response.responseBody;

    final String url = isNull(requestUrl)
        ? ""
        : requestUrl.toString();

    final InterceptedMediaType contentType = isNull(responseBody)
        ? null
        : responseBody.contentType();

    final byte[] originalBody = isNull(responseBody)
        ? null
        : originalBodyFrom(responseBody);

    return InterceptedResponse
        .builder()
        .url(url)
        .code(code)
        .header(header)
        .message(message)
        .segmentList(segmentList)
        .contentType(contentType)
        .originalBody(originalBody)
        .isSuccessful(isSuccessful)
        .hasPrintableBody(hasPrintableBody)
        .chainMs(isNull(chainMs) ? 0 : chainMs)
        .build();
  }

  @SuppressFBWarnings(value = "DM_DEFAULT_ENCODING", justification = "By Design.")
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
