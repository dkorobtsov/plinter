package com.dkorobtsov.logging.interceptors.okhttp;

import static com.dkorobtsov.logging.ClientPrintingExecutor.printRequest;
import static com.dkorobtsov.logging.ClientPrintingExecutor.printResponse;
import static com.dkorobtsov.logging.interceptors.okhttp.OkHttpRequestDetails.interceptedRequest;
import static com.dkorobtsov.logging.interceptors.okhttp.OkHttpResponseDetails.responseDetails;

import com.dkorobtsov.logging.InterceptedResponse;
import com.dkorobtsov.logging.LoggerConfig;
import com.dkorobtsov.logging.interceptors.AbstractInterceptor;
import com.dkorobtsov.logging.internal.InterceptedRequest;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class OkHttpLoggingInterceptor
    extends AbstractInterceptor implements Interceptor {

    public OkHttpLoggingInterceptor(LoggerConfig loggerConfig) {
        this.loggerConfig = loggerConfig;
    }

    @Override
    @SuppressWarnings("Duplicates")
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        final InterceptedRequest interceptedRequest = interceptedRequest(request);

        if (skipLogging()) {
            return chain.proceed(request);
        }

        printRequest(loggerConfig, interceptedRequest);

        final long startTime = System.nanoTime();
        final Response response = chain.proceed(request);
        final long ms = TimeUnit.NANOSECONDS
            .toMillis(System.nanoTime() - startTime);

        InterceptedResponse interceptedResponse = interceptedResponse(
            responseDetails(response), interceptedRequest.url(), ms);

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
