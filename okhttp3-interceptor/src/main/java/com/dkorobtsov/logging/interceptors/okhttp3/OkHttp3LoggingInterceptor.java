package com.dkorobtsov.logging.interceptors.okhttp3;

import static com.dkorobtsov.logging.internal.ClientPrintingExecutor.printRequest;
import static com.dkorobtsov.logging.internal.ClientPrintingExecutor.printResponse;

import com.dkorobtsov.logging.AbstractInterceptor;
import com.dkorobtsov.logging.LoggerConfig;
import com.dkorobtsov.logging.RequestConverter;
import com.dkorobtsov.logging.ResponseConverter;
import com.dkorobtsov.logging.internal.InterceptedRequest;
import com.dkorobtsov.logging.internal.InterceptedResponse;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttp3LoggingInterceptor extends AbstractInterceptor implements Interceptor {

  private final RequestConverter<Request> requestConverter;
  private final ResponseConverter<Response> responseConverter;

  public OkHttp3LoggingInterceptor(final LoggerConfig loggerConfig) {
    this.requestConverter = new OkHttp3RequestConverter();
    this.responseConverter = new OkHttp3ResponseConverter();
    this.loggerConfig = loggerConfig;
  }

  @Override
  @SuppressWarnings("Duplicates")
  public Response intercept(final Chain chain) throws IOException {
    final Request request = chain.request();

    if (skipLogging()) {
      return chain.proceed(request);
    }

    final InterceptedRequest interceptedRequest = requestConverter.from(request);

    printRequest(loggerConfig, interceptedRequest);

    final Response response = chain.proceed(request);
    final InterceptedResponse interceptedResponse = responseConverter
        .from(response, interceptedRequest.url(),
            response.receivedResponseAtMillis() - response.sentRequestAtMillis());

    printResponse(loggerConfig, interceptedResponse);

    return response;
  }

}
