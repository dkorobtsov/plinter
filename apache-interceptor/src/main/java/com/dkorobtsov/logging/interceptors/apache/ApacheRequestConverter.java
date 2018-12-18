package com.dkorobtsov.logging.interceptors.apache;

import static com.dkorobtsov.logging.interceptors.apache.ApacheEntityUtil.readApacheHttpEntity;
import static com.dkorobtsov.logging.interceptors.apache.ApacheEntityUtil.recreateHttpEntityFromString;
import static com.dkorobtsov.logging.internal.Util.APPLICATION_JSON;
import static com.dkorobtsov.logging.internal.Util.CONTENT_TYPE;
import static com.dkorobtsov.logging.internal.Util.TEXT_PLAIN;
import static java.util.Objects.nonNull;

import com.dkorobtsov.logging.RequestConverter;
import com.dkorobtsov.logging.internal.HttpMethod;
import com.dkorobtsov.logging.internal.InterceptedMediaType;
import com.dkorobtsov.logging.internal.InterceptedRequest;
import com.dkorobtsov.logging.internal.InterceptedRequestBody;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.message.BasicHeader;

public class ApacheRequestConverter implements RequestConverter<HttpRequest> {

  private static final Logger logger = Logger.getLogger(ApacheRequestConverter.class.getName());

  @Override
  public InterceptedRequest from(HttpRequest apacheHttpRequest) {
    final InterceptedRequest.Builder builder = new InterceptedRequest.Builder();
    builder.url(interceptedUrl(apacheHttpRequest));

    final Header[] headersMap = apacheHttpRequest.getAllHeaders();
    Arrays.stream(headersMap)
        .forEach(header -> builder.addHeader(header.getName(), header.getValue()));

    final String method = apacheHttpRequest.getRequestLine().getMethod();
    if (HttpMethod.permitsRequestBody(method)) {
      builder.method(method, interceptedRequestBody(apacheHttpRequest));
    } else {
      builder.method(method, null);
    }
    return builder.build();
  }

  private InterceptedRequestBody interceptedRequestBody(HttpRequest request) {

    if (request instanceof HttpRequestWrapper) {

      final HttpRequest original = ((HttpRequestWrapper) request).getOriginal();
      if (original instanceof HttpEntityEnclosingRequestBase) {

        final HttpEntity entity = ((HttpEntityEnclosingRequestBase) original).getEntity();
        if (nonNull(entity)) {

          String requestBodyString;
          try {
            requestBodyString = readApacheHttpEntity(entity);
          } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            return InterceptedRequestBody
                .create(InterceptedMediaType
                        .parse(APPLICATION_JSON),
                    "[LoggingInterceptorError] : could not parse request body");
          }

          final HttpEntity newEntity = recreateHttpEntityFromString(requestBodyString, entity);
          ((HttpEntityEnclosingRequestBase) ((HttpRequestWrapper) request).getOriginal())
              .setEntity(newEntity);

          final Header contentTypeHeader = Arrays
              .stream(((HttpRequestWrapper) request).getOriginal().getAllHeaders())
              .filter(header -> header.getName().equals(CONTENT_TYPE))
              .findFirst()
              .orElse(new BasicHeader(CONTENT_TYPE, TEXT_PLAIN));

          return InterceptedRequestBody
              .create(InterceptedMediaType
                      .parse(contentTypeHeader.getValue()),
                  requestBodyString);
        }
      }
    }
    return InterceptedRequestBody
        .create(InterceptedMediaType.parse(TEXT_PLAIN), "");
  }

  private String interceptedUrl(HttpRequest request) {
    final HttpHost target = ((HttpRequestWrapper) request).getTarget();
    final String portString = target.getPort() == -1 ? "" : ":" + target.getPort();
    final URI uri = ((HttpRequestWrapper) request).getURI();
    return String.format("%s://%s%s%s",
        target.getSchemeName(), target.getHostName(), portString, uri);
  }

}
