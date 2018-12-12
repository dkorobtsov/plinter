package com.dkorobtsov.logging.interceptors.apache;

import static com.dkorobtsov.logging.interceptors.apache.ApacheEntityUtil.readApacheHttpEntity;
import static com.dkorobtsov.logging.interceptors.apache.ApacheEntityUtil.recreateHttpEntityFromString;
import static com.dkorobtsov.logging.internal.Util.APPLICATION_JSON;
import static java.util.Objects.isNull;

import com.dkorobtsov.logging.ResponseConverter;
import com.dkorobtsov.logging.internal.HttpStatusCode;
import com.dkorobtsov.logging.internal.InterceptedHeaders;
import com.dkorobtsov.logging.internal.InterceptedMediaType;
import com.dkorobtsov.logging.internal.InterceptedResponse;
import com.dkorobtsov.logging.internal.InterceptedResponseBody;
import com.dkorobtsov.logging.internal.ResponseDetails;
import com.dkorobtsov.logging.internal.ResponseHandler;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

public class ApacheResponseConverter implements ResponseConverter<HttpResponse> {

  private static final Logger logger = Logger.getLogger(ApacheResponseConverter.class.getName());

  @Override
  public InterceptedResponse convertFrom(HttpResponse response, URL requestUrl, Long ms) {
    return ResponseHandler.interceptedResponse(responseDetails(response), requestUrl, ms);
  }

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
          .message(HttpStatusCode.findMessage(code))
          .responseBody(responseBody)
          .build();
    }
  }

  private InterceptedHeaders interceptedHeaders(Header[] headers) {
    final InterceptedHeaders.Builder headersBuilder = new InterceptedHeaders.Builder();
    Arrays.stream(headers).forEach(it -> headersBuilder.add(it.getName(), it.getValue()));
    return headersBuilder.build();
  }

  private InterceptedResponseBody interceptedResponseBody(HttpResponse response) {
    final HttpEntity entity = response.getEntity();
    if (entity != null) {
      String requestBodyString;
      try {
        requestBodyString = readApacheHttpEntity(entity);
      } catch (IOException e) {
        logger.log(Level.SEVERE, e.getMessage(), e);
        return InterceptedResponseBody.create(InterceptedMediaType.parse(APPLICATION_JSON),
            "[LoggingInterceptorError] : could not parse response body");
      }
      final Header contentType = response.getEntity().getContentType();
      final String contentTypeValue
          = contentType == null ? ""
          : contentType.getValue();

      final HttpEntity newEntity = recreateHttpEntityFromString(requestBodyString, entity);
      response.setEntity(newEntity);

      return InterceptedResponseBody
          .create(InterceptedMediaType.parse(contentTypeValue), requestBodyString);
    }
    return InterceptedResponseBody
        .create(InterceptedMediaType.parse(APPLICATION_JSON), "");
  }


}
