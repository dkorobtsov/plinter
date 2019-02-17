package io.github.dkorobtsov.plinter.apache;

import io.github.dkorobtsov.plinter.core.AbstractInterceptor;
import io.github.dkorobtsov.plinter.core.LoggerConfig;
import io.github.dkorobtsov.plinter.core.ResponseConverter;
import io.github.dkorobtsov.plinter.core.internal.ClientPrintingExecutor;
import io.github.dkorobtsov.plinter.core.internal.InterceptedResponse;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.protocol.HttpContext;

/**
 * Interceptor for responses sent by Apache HttpClient. Intended to be used with request interceptor
 * {@link ApacheHttpRequestInterceptor}.
 *
 * Interceptor's behavior can be configured using {@link LoggerConfig}
 *
 * Usage instructions:
 *
 * <pre>
 *
 *  ApacheRequestInterceptor requestInterceptor = new ApacheRequestInterceptor(LoggerConfig.builder().build());
 *  ApacheResponseInterceptor responseInterceptor = new ApacheResponseInterceptor(LoggerConfig.builder().build());
 *
 *  CloseableHttpClient client = HttpClientBuilder
 *      .create()
 *      .addInterceptorFirst(requestInterceptor)
 *      .addInterceptorFirst(responseInterceptor)
 *      .setMaxConnTotal(MAX_IDLE_CONNECTIONS)
 *      .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
 *      .build();
 *
 * </pre>
 */
public class ApacheHttpResponseInterceptor extends AbstractInterceptor
    implements HttpResponseInterceptor {

  private static final Logger logger = Logger
      .getLogger(ApacheHttpResponseInterceptor.class.getName());

  private final ResponseConverter<HttpResponse> responseConverter;

  public ApacheHttpResponseInterceptor(LoggerConfig loggerConfig) {
    this.responseConverter = new ApacheResponseConverter();
    this.loggerConfig = loggerConfig;
  }

  @Override
  public void process(final HttpResponse response, final HttpContext context) {
    if (!skipLogging()) {
      final InterceptedResponse interceptedResponse = responseConverter.from(
          response, urlFrom(context), null);

      ClientPrintingExecutor.printResponse(loggerConfig, interceptedResponse);
    }
  }

  URL urlFrom(final HttpContext context) {
    final HttpRequestWrapper request
        = (HttpRequestWrapper) context.getAttribute("http.request");

    try {
      return new URL(request.getOriginal().getRequestLine().getUri());
    } catch (MalformedURLException e) {
      logger.log(java.util.logging.Level.SEVERE, e.getMessage(), e);
      return null;
    }
  }

}
