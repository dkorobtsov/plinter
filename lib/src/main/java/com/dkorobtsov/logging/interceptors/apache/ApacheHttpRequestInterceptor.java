package com.dkorobtsov.logging.interceptors.apache;

import com.dkorobtsov.logging.AbstractInterceptor;
import com.dkorobtsov.logging.LoggerConfig;
import com.dkorobtsov.logging.RequestConverter;
import com.dkorobtsov.logging.internal.ClientPrintingExecutor;
import com.dkorobtsov.logging.internal.InterceptedRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;

public class ApacheHttpRequestInterceptor extends AbstractInterceptor
    implements HttpRequestInterceptor {

  private RequestConverter<HttpRequest> requestConverter;

  public ApacheHttpRequestInterceptor(LoggerConfig loggerConfig) {
    this.requestConverter = new ApacheRequestConverter();
    this.loggerConfig = loggerConfig;
  }

  @Override
  public void process(HttpRequest request, HttpContext context) {
    if (!skipLogging()) {
      final InterceptedRequest interceptedRequest = requestConverter.convertFrom(request);
      ClientPrintingExecutor.printRequest(loggerConfig, interceptedRequest);
    }
  }

}
