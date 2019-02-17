package io.github.dkorobtsov.plinter.okhttp;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import io.github.dkorobtsov.plinter.core.AbstractInterceptor;
import io.github.dkorobtsov.plinter.core.LoggerConfig;
import io.github.dkorobtsov.plinter.core.RequestConverter;
import io.github.dkorobtsov.plinter.core.ResponseConverter;
import io.github.dkorobtsov.plinter.core.internal.ClientPrintingExecutor;
import io.github.dkorobtsov.plinter.core.internal.InterceptedRequest;
import io.github.dkorobtsov.plinter.core.internal.InterceptedResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Interceptor for OkHttp client requests and responses. Interceptor's behavior can be configured
 * using {@link LoggerConfig}
 *
 * Usage instructions:
 *
 * <pre>
 *
 * OkHttpLoggingInterceptor interceptor = new OkHttpLoggingInterceptor(LoggerConfig.builder().build());
 *
 * OkHttpClient okHttpClient = new OkHttpClient.Builder()
 * .addInterceptor(interceptor)
 * .build();
 *
 * </pre>
 */
public class OkHttpLoggingInterceptor extends AbstractInterceptor implements Interceptor {

  private final RequestConverter<Request> requestConverter;
  private final ResponseConverter<Response> responseConverter;

  public OkHttpLoggingInterceptor(final LoggerConfig loggerConfig) {
    this.requestConverter = new OkHttpRequestConverter();
    this.responseConverter = new OkHttpResponseConverter();
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

    final long startTime = System.nanoTime();
    final Response response = chain.proceed(request);
    final long executionTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

    final InterceptedResponse interceptedResponse = responseConverter
        .from(response, interceptedRequest.url(), executionTime);

    ClientPrintingExecutor.printResponse(loggerConfig, interceptedResponse);

    return response;
  }

}
