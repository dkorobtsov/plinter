package com.dkorobtsov.logging.interceptors.apache;

import com.dkorobtsov.logging.AbstractInterceptor;
import com.dkorobtsov.logging.LoggerConfig;
import com.dkorobtsov.logging.ResponseConverter;
import com.dkorobtsov.logging.internal.ClientPrintingExecutor;
import com.dkorobtsov.logging.internal.InterceptedResponse;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.protocol.HttpContext;

public class ApacheHttpResponseInterceptor extends AbstractInterceptor
    implements HttpResponseInterceptor {

  private static final Logger logger = Logger
      .getLogger(ApacheHttpResponseInterceptor.class.getName());
  private ResponseConverter<HttpResponse> responseAdapter;

  public ApacheHttpResponseInterceptor(LoggerConfig loggerConfig) {
    this.responseAdapter = new ApacheResponseConverter();
    this.loggerConfig = loggerConfig;
  }

  @Override
  public void process(HttpResponse response, HttpContext context) {
    if (!skipLogging()) {
      final InterceptedResponse interceptedResponse = responseAdapter.convertFrom(
          response, urlFrom(context), null);

      ClientPrintingExecutor.printResponse(loggerConfig, interceptedResponse);
    }
  }

  URL urlFrom(HttpContext context) {
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
