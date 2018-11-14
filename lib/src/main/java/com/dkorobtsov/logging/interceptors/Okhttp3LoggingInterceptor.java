package com.dkorobtsov.logging.interceptors;

import static com.dkorobtsov.logging.ClientPrintingExecutor.printFileRequest;
import static com.dkorobtsov.logging.ClientPrintingExecutor.printFileResponse;
import static com.dkorobtsov.logging.ClientPrintingExecutor.printJsonRequest;
import static com.dkorobtsov.logging.ClientPrintingExecutor.printJsonResponse;
import static com.dkorobtsov.logging.TextUtils.isFileRequest;

import com.dkorobtsov.logging.Level;
import com.dkorobtsov.logging.LoggerConfig;
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
    private final LoggerConfig loggerConfig;

    public Okhttp3LoggingInterceptor(LoggerConfig loggerConfig) {
        this.loggerConfig = loggerConfig;
        this.isDebug = loggerConfig.isDebug;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        if (!isDebug || loggerConfig.level == Level.NONE) {
            return chain.proceed(request);
        }

        final RequestBody requestBody = request.body();

        String requestSubtype = null;
        if (requestBody != null && requestBody.contentType() != null) {
            requestSubtype = Objects.requireNonNull(requestBody.contentType()).subtype();
        }

        if (isFileRequest(requestSubtype)) {
            printFileRequest(request, loggerConfig);
        } else {
            printJsonRequest(request, loggerConfig);
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
            printFileResponse(responseDetails, loggerConfig);
            return response;
        } else {
            printJsonResponse(responseDetails, loggerConfig);
            body = ResponseBody.create(responseDetails.contentType, responseDetails.originalBody);
        }

        return response.newBuilder()
            .body(body)
            .build();
    }

    public LoggerConfig loggerConfig() {
        return this.loggerConfig;
    }
}
