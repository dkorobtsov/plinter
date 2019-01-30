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

    // Trying to determine if body should be pretty printed or omitted as file request
    String mediaType = null;
    if (nonNull(response.responseBody)) {
      if (nonNull(response.responseBody.contentType())) {
        mediaType = response.responseBody.contentType().subtype();
      }
    }

    final boolean hasPrintableBody = Util.hasPrintableBody(mediaType);

    final List<String> segmentList = isNull(requestUrl)
        ? Collections.emptyList() : Util.encodedPathSegments(requestUrl);

    final String header = response.headers.toString();
    final int code = response.code;
    final boolean isSuccessful = response.isSuccessful;
    final String message = response.message;
    final InterceptedResponseBody responseBody = response.responseBody;
    final InterceptedMediaType contentType = responseBody.contentType();
    final String url = isNull(requestUrl) ? "" : requestUrl.toString();

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
