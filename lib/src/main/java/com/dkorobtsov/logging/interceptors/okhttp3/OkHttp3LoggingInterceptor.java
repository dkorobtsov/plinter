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

  private RequestConverter<Request> requestConverter;
  private ResponseConverter<Response> responseConverter;

  public OkHttp3LoggingInterceptor(LoggerConfig loggerConfig) {
    this.requestConverter = new OkHttp3RequestConverter();
    this.responseConverter = new OkHttp3ResponseConverter();
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

    final Response response = chain.proceed(request);
    InterceptedResponse interceptedResponse = responseConverter
        .from(response, interceptedRequest.url(),
            response.receivedResponseAtMillis() - response.sentRequestAtMillis());

    printResponse(loggerConfig, interceptedResponse);

    return response;
  }

}
