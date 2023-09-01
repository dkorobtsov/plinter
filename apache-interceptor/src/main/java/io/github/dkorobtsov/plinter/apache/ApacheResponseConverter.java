package io.github.dkorobtsov.plinter.apache;

import io.github.dkorobtsov.plinter.core.ResponseConverter;
import io.github.dkorobtsov.plinter.core.internal.HttpStatus;
import io.github.dkorobtsov.plinter.core.internal.InterceptedHeaders;
import io.github.dkorobtsov.plinter.core.internal.InterceptedMediaType;
import io.github.dkorobtsov.plinter.core.internal.InterceptedResponse;
import io.github.dkorobtsov.plinter.core.internal.InterceptedResponseBody;
import io.github.dkorobtsov.plinter.core.internal.ResponseDetails;
import io.github.dkorobtsov.plinter.core.internal.ResponseHandler;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.github.dkorobtsov.plinter.core.internal.Util.APPLICATION_JSON;
import static io.github.dkorobtsov.plinter.core.internal.Util.TEXT_PLAIN;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * Helper class implementing conversion logic from Apache HTTP client response to this library's
 * internal {@link InterceptedResponse}.
 */
public class ApacheResponseConverter implements ResponseConverter<HttpResponse> {

  private static final Logger logger = Logger.getLogger(ApacheResponseConverter.class.getName());

  @Override
  public InterceptedResponse from(HttpResponse response, URL requestUrl, Long ms) {
    return ResponseHandler.interceptedResponse(responseDetails(response), requestUrl, ms);
  }

  @SuppressWarnings({"PMD", "CPD-START"}) // Ignore duplicate code here
  private InterceptedResponseBody interceptedResponseBody(HttpResponse response) {
    final HttpEntity entity = response.getEntity();
    if (nonNull(entity)) {

      final byte[] byteArray;
      try {
        byteArray = ApacheEntityUtil.getEntityBytes(entity);
      } catch (IOException e) {
        logger.log(Level.SEVERE, e.getMessage(), e);
        return InterceptedResponseBody
          .create(InterceptedMediaType.parse(TEXT_PLAIN),
            "[LoggingInterceptorError] : could not parse body");
      }

      final Header contentType = response.getEntity().getContentType();
      final String contentTypeValue
        = isNull(contentType) ? ""
        : contentType.getValue();

      final HttpEntity newEntity = ApacheEntityUtil
        .recreateHttpEntityFromByteArray(byteArray.clone(), entity);

      response.setEntity(newEntity);

      return InterceptedResponseBody
        .create(InterceptedMediaType.parse(contentTypeValue), byteArray);
    }

    return InterceptedResponseBody
      .create(InterceptedMediaType.parse(APPLICATION_JSON), "");
  }

  @SuppressWarnings("PMD")
  private ResponseDetails responseDetails(HttpResponse httpResponse) {
    if (isNull(httpResponse)) {
      throw new IllegalStateException("httpResponse == null");
    } else {
      final int code = httpResponse.getStatusLine().getStatusCode();
      final InterceptedResponseBody responseBody = interceptedResponseBody(httpResponse);

      return ResponseDetails.builder()
        .code(httpResponse.getStatusLine().getStatusCode())
        .headers(interceptedHeaders(httpResponse.getAllHeaders()))
        .isSuccessful(code >= 200 && code <= 300)
        .mediaType(responseBody.contentType())
        .message(HttpStatus.fromCode(code))
        .responseBody(responseBody)
        .build();
    }
  }

  private InterceptedHeaders interceptedHeaders(Header... headers) {
    final InterceptedHeaders.Builder headersBuilder = new InterceptedHeaders.Builder();
    Arrays.stream(headers).forEach(it -> headersBuilder.add(it.getName(), it.getValue()));
    return headersBuilder.build();
  }

}
