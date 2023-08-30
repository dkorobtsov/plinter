package io.github.dkorobtsov.plinter.okhttp3;

import io.github.dkorobtsov.plinter.core.ResponseConverter;
import io.github.dkorobtsov.plinter.core.internal.InterceptedHeaders;
import io.github.dkorobtsov.plinter.core.internal.InterceptedMediaType;
import io.github.dkorobtsov.plinter.core.internal.InterceptedResponse;
import io.github.dkorobtsov.plinter.core.internal.InterceptedResponseBody;
import io.github.dkorobtsov.plinter.core.internal.Protocol;
import io.github.dkorobtsov.plinter.core.internal.ResponseDetails;
import io.github.dkorobtsov.plinter.core.internal.ResponseHandler;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * Helper class implementing conversion logic from OkHTTP3 client response to this library's
 * internal {@link InterceptedResponse}.
 */
@SuppressWarnings("Duplicates")
class OkHttp3ResponseConverter implements ResponseConverter<Response> {

  private static final Logger logger = Logger.getLogger(OkHttp3ResponseConverter.class.getName());

  @Override
  public InterceptedResponse from(final Response response, final URL requestUrl, final Long ms) {
    return ResponseHandler
        .interceptedResponse(responseDetails(response), requestUrl, ms);
  }

  private ResponseDetails responseDetails(final Response response) {
    if (isNull(response)) {
      throw new IllegalStateException("httpResponse == null");
    } else {
      return ResponseDetails.builder()
          .code(response.code())
          .protocol(Protocol.get(response.protocol().toString()))
          .headers(interceptedHeaders(response.headers()))
          .isSuccessful(response.isSuccessful())
          .mediaType(interceptedMediaType(
              isNull(response.body())
                  ? null
                  : response.body().contentType()))
          .message(response.message())
          .responseBody(interceptedResponseBody(response))
          .build();
    }
  }

  private InterceptedHeaders interceptedHeaders(final Headers headers) {
    final InterceptedHeaders.Builder headersBuilder = new InterceptedHeaders.Builder();

    headers.toMultimap()
        .forEach((name, values) -> values.forEach(it -> headersBuilder.add(name, it)));

    return headersBuilder.build();
  }

  private InterceptedResponseBody interceptedResponseBody(final Response response) {
    ResponseBody responseBodyCopy = null;
    try {
      // Since body is readable only once, here we applying this hack to get a copy.
      // NB: In general we are reading body only if it has "printable" content type, and those
      // files are usually not too big, but in any case need to be careful here
      // since copy is stored in memory.
      responseBodyCopy = response.peekBody(Long.MAX_VALUE);
    } catch (IOException e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
    }

    if (nonNull(responseBodyCopy)) {
      final MediaType mediaType = responseBodyCopy.contentType();
      try {
        return InterceptedResponseBody
            .create(interceptedMediaType(mediaType), responseBodyCopy.bytes());
      } catch (IOException e) {
        logger.log(Level.SEVERE, e.getMessage(), e);
        return InterceptedResponseBody
            .create(interceptedMediaType(mediaType), "");
      }
    } else {
      return null;
    }
  }

  private InterceptedMediaType interceptedMediaType(final MediaType mediaType) {
    return mediaType == null ? InterceptedMediaType.parse("")
        : InterceptedMediaType.parse(mediaType.toString());
  }

}
