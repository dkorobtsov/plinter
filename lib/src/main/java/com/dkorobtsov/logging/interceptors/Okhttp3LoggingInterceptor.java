package com.dkorobtsov.logging.interceptors;

import static com.dkorobtsov.logging.ClientPrintingExecutor.printFileRequest;
import static com.dkorobtsov.logging.ClientPrintingExecutor.printFileResponse;
import static com.dkorobtsov.logging.ClientPrintingExecutor.printJsonRequest;
import static com.dkorobtsov.logging.ClientPrintingExecutor.printJsonResponse;
import static com.dkorobtsov.logging.TextUtils.isFileRequest;

import com.dkorobtsov.logging.Level;
import com.dkorobtsov.logging.LoggingInterceptor;
import com.dkorobtsov.logging.ResponseDetails;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class Okhttp3LoggingInterceptor implements Interceptor {

    private final boolean isDebug;
    private final LoggingInterceptor.Builder builder;

    public Okhttp3LoggingInterceptor(LoggingInterceptor.Builder builder) {
        this.builder = builder;
        this.isDebug = builder.isDebug();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        if (!isDebug || builder.getLevel() == Level.NONE) {
            return chain.proceed(request);
        }

        final RequestBody requestBody = request.body();

        String requestSubtype = null;
        if (requestBody != null && requestBody.contentType() != null) {
            requestSubtype = Objects.requireNonNull(requestBody.contentType()).subtype();
        }

        if (isFileRequest(requestSubtype)) {
            printFileRequest(request, builder);
        } else {
            printJsonRequest(request, builder);
        }

        final long startTime = System.nanoTime();
        final Response response = chain.proceed(request);
        final long responseTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

        String subtype = null;
        final ResponseBody body;

        if (Objects.requireNonNull(response.body()).contentType() != null) {
            subtype = Objects.requireNonNull(response.body().contentType()).subtype();
        }

        ResponseDetails responseDetails = ResponseDetails
            .from(request, response, responseTime, isFileRequest(subtype));

        if (isFileRequest(subtype)) {
            printFileResponse(responseDetails, builder);
            return response;
        } else {
            printJsonResponse(responseDetails, builder);
            body = ResponseBody.create(responseDetails.contentType, responseDetails.originalBody);
        }

        return response.newBuilder()
            .body(body)
            .build();
    }

}
