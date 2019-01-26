package io.github.dkorobtsov.plinter.okhttp;

import static io.github.dkorobtsov.plinter.internal.ClientPrintingExecutor.printRequest;
import static io.github.dkorobtsov.plinter.internal.ClientPrintingExecutor.printResponse;

import io.github.dkorobtsov.plinter.AbstractInterceptor;
import io.github.dkorobtsov.plinter.LoggerConfig;
import io.github.dkorobtsov.plinter.RequestConverter;
import io.github.dkorobtsov.plinter.ResponseConverter;
import io.github.dkorobtsov.plinter.internal.InterceptedRequest;
import io.github.dkorobtsov.plinter.internal.InterceptedResponse;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
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

    printRequest(loggerConfig, interceptedRequest);

    final long startTime = System.nanoTime();
    final Response response = chain.proceed(request);
    final long executionTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

    final InterceptedResponse interceptedResponse = responseConverter
        .from(response, interceptedRequest.url(), executionTime);

    printResponse(loggerConfig, interceptedResponse);

    return response;
  }

}
