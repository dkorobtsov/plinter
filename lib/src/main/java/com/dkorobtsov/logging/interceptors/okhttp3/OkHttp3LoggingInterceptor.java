package com.dkorobtsov.logging.interceptors.okhttp3;

import static com.dkorobtsov.logging.ClientPrintingExecutor.printRequest;
import static com.dkorobtsov.logging.ClientPrintingExecutor.printResponse;
import static com.dkorobtsov.logging.Utilities.hasPrintableBody;
import static com.dkorobtsov.logging.Utilities.subtype;
import static com.dkorobtsov.logging.interceptors.okhttp3.OkHttp3ResponseDetails.getOkHttpResponseDetails;

import com.dkorobtsov.logging.InterceptedResponse;
import com.dkorobtsov.logging.LoggerConfig;
import com.dkorobtsov.logging.interceptors.ResponseInterceptor;
import com.dkorobtsov.logging.internal.InterceptedRequest;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class OkHttp3LoggingInterceptor extends ResponseInterceptor implements Interceptor {

    public OkHttp3LoggingInterceptor(LoggerConfig loggerConfig) {
        this.loggerConfig = loggerConfig;
    }

    @Override
    @SuppressWarnings("Duplicates")
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        final InterceptedRequest interceptedRequest = OkHttp3RequestDetails
            .interceptedRequest(request);

        if (skipLogging()) {
            return chain.proceed(request);
        }

        String subtype = subtype(interceptedRequest);

        printRequest(loggerConfig, interceptedRequest, hasPrintableBody(subtype));

        final long startTime = System.nanoTime();
        final Response response = chain.proceed(request);
        final long ms = TimeUnit.NANOSECONDS
            .toMillis(System.nanoTime() - startTime);

        InterceptedResponse interceptedResponse = interceptedResponse(
            getOkHttpResponseDetails(response), interceptedRequest.url(), ms);

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
