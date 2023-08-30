package io.github.dkorobtsov.plinter.okhttp3;

import io.github.dkorobtsov.plinter.core.AbstractInterceptor;
import io.github.dkorobtsov.plinter.core.LoggerConfig;
import io.github.dkorobtsov.plinter.core.RequestConverter;
import io.github.dkorobtsov.plinter.core.ResponseConverter;
import io.github.dkorobtsov.plinter.core.internal.ClientPrintingExecutor;
import io.github.dkorobtsov.plinter.core.internal.InterceptedRequest;
import io.github.dkorobtsov.plinter.core.internal.InterceptedResponse;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * Interceptor for OkHttp3 client requests and responses. Interceptor's behavior can be configured
 * using {@link LoggerConfig}
 *
 * Usage instructions:
 *
 * <pre>
 *
 * OkHttp3LoggingInterceptor interceptor = new OkHttp3LoggingInterceptor(LoggerConfig.builder().build());
 *
 * OkHttpClient okHttpClient = new OkHttpClient.Builder()
 * .addInterceptor(interceptor)
 * .build();
 *
 * </pre>
 */
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

    ClientPrintingExecutor.printRequest(loggerConfig, interceptedRequest);

    final Response response = chain.proceed(request);
    final InterceptedResponse interceptedResponse = responseConverter
        .from(response, interceptedRequest.url(),
            response.receivedResponseAtMillis() - response.sentRequestAtMillis());

    ClientPrintingExecutor.printResponse(loggerConfig, interceptedResponse);

    return response;
  }

}
