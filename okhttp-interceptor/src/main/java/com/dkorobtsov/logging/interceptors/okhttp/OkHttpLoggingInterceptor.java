package com.dkorobtsov.logging.interceptors.okhttp;

import static com.dkorobtsov.logging.internal.ClientPrintingExecutor.printRequest;
import static com.dkorobtsov.logging.internal.ClientPrintingExecutor.printResponse;

import com.dkorobtsov.logging.AbstractInterceptor;
import com.dkorobtsov.logging.LoggerConfig;
import com.dkorobtsov.logging.RequestConverter;
import com.dkorobtsov.logging.ResponseConverter;
import com.dkorobtsov.logging.internal.InterceptedRequest;
import com.dkorobtsov.logging.internal.InterceptedResponse;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class OkHttpLoggingInterceptor extends AbstractInterceptor implements Interceptor {

  private RequestConverter<Request> requestConverter;
  private ResponseConverter<Response> responseConverter;

  public OkHttpLoggingInterceptor(LoggerConfig loggerConfig) {
    this.requestConverter = new OkHttpRequestConverter();
    this.responseConverter = new OkHttpResponseConverter();
    this.loggerConfig = loggerConfig;
  }

  @Override
  @SuppressWarnings("Duplicates")
  public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();

    if (skipLogging()) {
      return chain.proceed(request);
    }

    final InterceptedRequest interceptedRequest = requestConverter.from(request);

    printRequest(loggerConfig, interceptedRequest);

    final long startTime = System.nanoTime();
    final Response response = chain.proceed(request);
    final long executionTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

    InterceptedResponse interceptedResponse = responseConverter
        .from(response, interceptedRequest.url(), executionTime);

    printResponse(loggerConfig, interceptedResponse);

    return response;
  }

}
