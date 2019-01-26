package io.github.dkorobtsov.plinter.okhttp3;

import static io.github.dkorobtsov.plinter.internal.ClientPrintingExecutor.printRequest;
import static io.github.dkorobtsov.plinter.internal.ClientPrintingExecutor.printResponse;

import io.github.dkorobtsov.plinter.AbstractInterceptor;
import io.github.dkorobtsov.plinter.LoggerConfig;
import io.github.dkorobtsov.plinter.RequestConverter;
import io.github.dkorobtsov.plinter.ResponseConverter;
import io.github.dkorobtsov.plinter.internal.InterceptedRequest;
import io.github.dkorobtsov.plinter.internal.InterceptedResponse;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

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

    printRequest(loggerConfig, interceptedRequest);

    final Response response = chain.proceed(request);
    final InterceptedResponse interceptedResponse = responseConverter
        .from(response, interceptedRequest.url(),
            response.receivedResponseAtMillis() - response.sentRequestAtMillis());

    printResponse(loggerConfig, interceptedResponse);

    return response;
  }

}
