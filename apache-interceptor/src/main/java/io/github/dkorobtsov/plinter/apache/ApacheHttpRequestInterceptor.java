package io.github.dkorobtsov.plinter.apache;

import static io.github.dkorobtsov.plinter.internal.ClientPrintingExecutor.printRequest;

import io.github.dkorobtsov.plinter.AbstractInterceptor;
import io.github.dkorobtsov.plinter.LoggerConfig;
import io.github.dkorobtsov.plinter.RequestConverter;
import io.github.dkorobtsov.plinter.internal.InterceptedRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;

/**
 * Interceptor for requests sent by Apache HttpClient. Intended to be used with response interceptor
 * {@link ApacheHttpResponseInterceptor}.
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
public class ApacheHttpRequestInterceptor extends AbstractInterceptor
    implements HttpRequestInterceptor {

  private final RequestConverter<HttpRequest> requestConverter;

  public ApacheHttpRequestInterceptor(final LoggerConfig loggerConfig) {
    this.requestConverter = new ApacheRequestConverter();
    this.loggerConfig = loggerConfig;
  }

  @Override
  public void process(final HttpRequest request, final HttpContext context) {
    if (!skipLogging()) {
      final InterceptedRequest interceptedRequest = requestConverter.from(request);

      printRequest(loggerConfig, interceptedRequest);
    }
  }

}
