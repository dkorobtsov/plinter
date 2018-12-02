package com.dkorobtsov.logging.interceptors.okhttp;

import static com.dkorobtsov.logging.internal.ClientPrintingExecutor.printRequest;
import static com.dkorobtsov.logging.internal.ClientPrintingExecutor.printResponse;

import com.dkorobtsov.logging.AbstractInterceptor;
import com.dkorobtsov.logging.internal.InterceptedResponse;
import com.dkorobtsov.logging.LoggerConfig;
import com.dkorobtsov.logging.RequestConverter;
import com.dkorobtsov.logging.ResponseConverter;
import com.dkorobtsov.logging.internal.InterceptedRequest;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import java.io.IOException;
import java.net.URL;
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
        final InterceptedRequest interceptedRequest = requestConverter.convertFrom(request);

        if (skipLogging()) {
            return chain.proceed(request);
        }

        printRequest(loggerConfig, interceptedRequest);

        final long startTime = System.nanoTime();
        final Response response = chain.proceed(request);
        final long ms = TimeUnit.NANOSECONDS
            .toMillis(System.nanoTime() - startTime);

        final URL url = interceptedRequest.url();
        InterceptedResponse interceptedResponse = responseConverter.convertFrom(response, url, ms);

        printResponse(loggerConfig, interceptedResponse);

        final ResponseBody body;
        if (interceptedResponse.hasPrintableBody) {
            final MediaType mediaType = MediaType.parse(interceptedResponse.contentType.toString());
            body = ResponseBody.create(mediaType, interceptedResponse.originalBody);
        } else {
            return response;
        }

        return response.newBuilder()
            .body(body)
            .build();
    }

}
