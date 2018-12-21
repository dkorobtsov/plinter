package com.dkorobtsov.logging.interceptors.okhttp;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.dkorobtsov.logging.ResponseConverter;
import com.dkorobtsov.logging.internal.InterceptedHeaders;
import com.dkorobtsov.logging.internal.InterceptedMediaType;
import com.dkorobtsov.logging.internal.InterceptedResponse;
import com.dkorobtsov.logging.internal.InterceptedResponseBody;
import com.dkorobtsov.logging.internal.ResponseDetails;
import com.dkorobtsov.logging.internal.ResponseHandler;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import okio.Buffer;
import okio.BufferedSource;

@SuppressWarnings("Duplicates")
class OkHttpResponseConverter implements ResponseConverter<Response> {

  private static final Logger logger = Logger.getLogger(OkHttpResponseConverter.class.getName());

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
          .headers(interceptedHeaders(response.headers()))
          .isSuccessful(response.isSuccessful())
          .mediaType(interceptedMediaType(response.body().contentType()))
          .message(response.message())
          .responseBody(interceptedResponseBody(response.body()))
          .build();
    }
  }

  private InterceptedHeaders interceptedHeaders(final Headers headers) {
    final InterceptedHeaders.Builder headersBuilder = new InterceptedHeaders.Builder();
    headers.names().forEach(name -> headersBuilder.add(name, headers.get(name)));
    return headersBuilder.build();
  }

  private InterceptedResponseBody interceptedResponseBody(final ResponseBody responseBody) {
    ResponseBody responseBodyCopy = null;
    try {
      // Since body is readable only once, here we applying this hack to get a copy.
      // NB: In general we are reading body only if it has "printable" content type, and those
      // files are usually not too big so we are not limiting maximum size.
      responseBodyCopy = copyBody(responseBody, Long.MAX_VALUE);
    } catch (IOException e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
    }
    if (nonNull(responseBodyCopy)) {
      final MediaType mediaType = responseBodyCopy.contentType();
      String responseBodyString = "";
      try {
        responseBodyString = new String(responseBodyCopy.bytes(), Charset.defaultCharset());
      } catch (IOException e) {
        logger.log(Level.SEVERE, e.getMessage(), e);
      }
      return InterceptedResponseBody
          .create(interceptedMediaType(mediaType), responseBodyString);
    } else {
      return null;
    }
  }

  private InterceptedMediaType interceptedMediaType(MediaType mediaType) {
    return mediaType == null ? InterceptedMediaType.parse("")
        : InterceptedMediaType.parse(mediaType.toString());
  }

  /**
   * Peeks up to {@code byteCount} bytes from the response body and returns them as a new response
   * body. If fewer than {@code byteCount} bytes are in the response body, the full response body is
   * returned. If more than {@code byteCount} bytes are in the response body, the returned value
   * will be truncated to {@code byteCount} bytes.
   *
   * <p>It is an error to call this method after the body has been consumed.
   *
   * <p><strong>Warning:</strong> this method loads the requested bytes into memory. Most
   * applications should set a modest limit on {@code byteCount}, such as 1 MiB.
   *
   * --------------------------------------------------------------------------------------
   *
   * NB: Method copied with some small modifications from OkHttp3 client's Response#peekBody
   * (removed deprecated method).
   *
   * See <a href="https://github.com/square/okhttp">OkHttp3</a>
   */
  private ResponseBody copyBody(final ResponseBody responseBody, final long byteCount)
      throws IOException {

    final BufferedSource source = responseBody.source();
    source.request(byteCount);
    final Buffer copy = source.getBuffer().clone();

    // There may be more than byteCount bytes in source.buffer(). If there is, return a prefix.
    final Buffer result;
    if (copy.size() > byteCount) {
      result = new Buffer();
      result.write(copy, byteCount);
      copy.clear();
    } else {
      result = copy;
    }

    return ResponseBody.create(responseBody.contentType(), result.size(), result);
  }

}
