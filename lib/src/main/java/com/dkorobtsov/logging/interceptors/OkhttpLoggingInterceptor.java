package com.dkorobtsov.logging.interceptors;

import static com.dkorobtsov.logging.ClientPrintingExecutor.printFileRequest;
import static com.dkorobtsov.logging.ClientPrintingExecutor.printFileResponse;
import static com.dkorobtsov.logging.ClientPrintingExecutor.printJsonRequest;
import static com.dkorobtsov.logging.ClientPrintingExecutor.printJsonResponse;
import static com.dkorobtsov.logging.TextUtils.isFileRequest;
import static com.dkorobtsov.logging.converters.ToOkhttp3Converter.convertOkhttpResponseTo3;
import static com.dkorobtsov.logging.converters.ToOkhttpConverter.convertOkhttp3MediaType;

import com.dkorobtsov.logging.Level;
import com.dkorobtsov.logging.LoggingInterceptor;
import com.dkorobtsov.logging.RequestDetails;
import com.dkorobtsov.logging.ResponseDetails;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import okhttp3.RequestBody;

public class OkhttpLoggingInterceptor implements Interceptor {

    private final boolean isDebug;
    private final LoggingInterceptor.Builder builder;

    public OkhttpLoggingInterceptor(LoggingInterceptor.Builder builder) {
        this.builder = builder;
        this.isDebug = builder.isDebug();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        final okhttp3.Request requestDetails = new RequestDetails.Builder().from(request).build();

        if (!isDebug || builder.getLevel() == Level.NONE) {
            return chain.proceed(request);
        }

        final RequestBody requestBody = requestDetails.body();

        String requestSubtype = null;
        if (requestBody != null && requestBody.contentType() != null) {
            requestSubtype = Objects.requireNonNull(requestBody.contentType()).subtype();
        }

        if (isFileRequest(requestSubtype)) {
            printFileRequest(requestDetails, builder);
        } else {
            printJsonRequest(requestDetails, builder);
        }

        final long requestStartTime = System.nanoTime();
        final Response response = chain.proceed(request);
        final long responseTime = TimeUnit.NANOSECONDS
            .toMillis(System.nanoTime() - requestStartTime);

        String subtype = null;
        final ResponseBody body;

        if (Objects.requireNonNull(response.body()).contentType() != null) {
            subtype = Objects.requireNonNull(response.body().contentType()).subtype();
        }

        ResponseDetails responseDetails = ResponseDetails
            .from(requestDetails, convertOkhttpResponseTo3(response), responseTime,
                isFileRequest(subtype));

        if (isFileRequest(subtype)) {
            printFileResponse(responseDetails, builder);
            return response;
        } else {
            printJsonResponse(responseDetails, builder);
            final okhttp3.MediaType okhttp3MediaType = responseDetails.contentType;
            final MediaType mediaType = convertOkhttp3MediaType(okhttp3MediaType);
            body = ResponseBody.create(mediaType, responseDetails.bodyString);
        }

        return response.newBuilder()
            .body(body)
            .build();
    }

}
