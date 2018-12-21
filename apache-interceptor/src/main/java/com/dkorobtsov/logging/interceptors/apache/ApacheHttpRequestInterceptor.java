package com.dkorobtsov.logging.interceptors.apache;

import static com.dkorobtsov.logging.internal.ClientPrintingExecutor.printRequest;

import com.dkorobtsov.logging.AbstractInterceptor;
import com.dkorobtsov.logging.LoggerConfig;
import com.dkorobtsov.logging.RequestConverter;
import com.dkorobtsov.logging.internal.InterceptedRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;

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
