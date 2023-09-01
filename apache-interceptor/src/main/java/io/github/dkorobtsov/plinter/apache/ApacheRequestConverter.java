package io.github.dkorobtsov.plinter.apache;

import io.github.dkorobtsov.plinter.core.RequestConverter;
import io.github.dkorobtsov.plinter.core.internal.HttpMethod;
import io.github.dkorobtsov.plinter.core.internal.InterceptedMediaType;
import io.github.dkorobtsov.plinter.core.internal.InterceptedRequest;
import io.github.dkorobtsov.plinter.core.internal.InterceptedRequestBody;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.message.BasicHeader;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.github.dkorobtsov.plinter.core.internal.Util.CONTENT_TYPE;
import static io.github.dkorobtsov.plinter.core.internal.Util.TEXT_PLAIN;
import static java.util.Objects.nonNull;

/**
 * Helper class implementing conversion logic from Apache HTTP client request to this library's
 * internal {@link InterceptedRequest}.
 */
public class ApacheRequestConverter implements RequestConverter<HttpRequest> {

  private static final Logger logger = Logger.getLogger(ApacheRequestConverter.class.getName());

  @Override
  public InterceptedRequest from(final HttpRequest apacheHttpRequest) {
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

  @SuppressWarnings({"PMD", "CPD-START"}) // Ignore duplicate code here
  private InterceptedRequestBody interceptedRequestBody(final HttpRequest request) {

    if (request instanceof HttpRequestWrapper) {

      final HttpRequest original = ((HttpRequestWrapper) request).getOriginal();
      if (original instanceof HttpEntityEnclosingRequestBase) {

        final HttpEntity entity = ((HttpEntityEnclosingRequestBase) original).getEntity();
        if (nonNull(entity)) {

          final byte[] byteArray;
          try {
            byteArray = ApacheEntityUtil.getEntityBytes(entity);
          } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            return InterceptedRequestBody
                .create(InterceptedMediaType
                        .parse(TEXT_PLAIN),
                    "[LoggingInterceptorError] : could not parse body");
          }

          final HttpEntity newEntity = ApacheEntityUtil
              .recreateHttpEntityFromByteArray(byteArray, entity);

          ((HttpEntityEnclosingRequestBase) ((HttpRequestWrapper) request).getOriginal())
              .setEntity(newEntity);

          final Header contentTypeHeader = Arrays
              .stream(((HttpRequestWrapper) request).getOriginal().getAllHeaders())
              .filter(header -> header.getName().equals(CONTENT_TYPE))
              .findFirst()
              .orElse(new BasicHeader(CONTENT_TYPE, TEXT_PLAIN));

          return InterceptedRequestBody
              .create(InterceptedMediaType
                  .parse(contentTypeHeader.getValue()), byteArray);
        }
      }
    }
    return InterceptedRequestBody
        .create(InterceptedMediaType.parse(TEXT_PLAIN), "");
  }

  private String interceptedUrl(final HttpRequest request) {
    final HttpHost target = ((HttpRequestWrapper) request).getTarget();
    final String portString = target.getPort() == -1 ? "" : ":" + target.getPort();
    final URI uri = ((HttpRequestWrapper) request).getURI();
    return String.format("%s://%s%s%s",
        target.getSchemeName(), target.getHostName(), portString, uri);
  }

}
