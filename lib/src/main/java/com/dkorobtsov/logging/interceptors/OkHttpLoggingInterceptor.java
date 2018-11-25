package com.dkorobtsov.logging.interceptors;

import static com.dkorobtsov.logging.utils.TextUtils.hasPrintableBody;
import static com.dkorobtsov.logging.converters.ToOkHttp3Converter.convertOkHttpResponseTo3;
import static com.dkorobtsov.logging.converters.ToOkHttpConverter.convertOkHttp3MediaType;

import com.dkorobtsov.logging.utils.BodyUtils;
import com.dkorobtsov.logging.ClientPrintingExecutor;
import com.dkorobtsov.logging.InterceptedResponse;
import com.dkorobtsov.logging.LoggerConfig;
import com.dkorobtsov.logging.RequestDetails;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class OkHttpLoggingInterceptor extends AbstractOkHttpInterceptor implements Interceptor {

    public OkHttpLoggingInterceptor(LoggerConfig loggerConfig) {
        this.loggerConfig = loggerConfig;
        this.isLoggable = loggerConfig.isLoggable;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        final okhttp3.Request requestDetails = new RequestDetails.Builder().from(request).build();

        if (skipLogging()) {
            return chain.proceed(request);
        }

        String subtype = BodyUtils.subtype(requestDetails);

        ClientPrintingExecutor
            .printRequest(loggerConfig, requestDetails, hasPrintableBody(subtype));

        final long startTime = System.nanoTime();
        final Response response = chain.proceed(request);
        final long responseTime = TimeUnit.NANOSECONDS
            .toMillis(System.nanoTime() - startTime);

        InterceptedResponse interceptedResponse = interceptedResponse(requestDetails,
            convertOkHttpResponseTo3(response), responseTime);

        ClientPrintingExecutor.printResponse(loggerConfig, interceptedResponse);

        final ResponseBody body;
        if (interceptedResponse.hasPrintableBody) {
            final okhttp3.MediaType okHttp3MediaType = interceptedResponse.contentType;
            final MediaType mediaType = convertOkHttp3MediaType(okHttp3MediaType);
            body = ResponseBody.create(mediaType, interceptedResponse.originalBody);
        } else {
            return response;
        }

        return response.newBuilder()
            .body(body)
            .build();
    }

}
