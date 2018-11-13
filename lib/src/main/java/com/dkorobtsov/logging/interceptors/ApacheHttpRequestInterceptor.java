package com.dkorobtsov.logging.interceptors;

import static com.dkorobtsov.logging.ClientPrintingExecutor.printFileRequest;
import static com.dkorobtsov.logging.ClientPrintingExecutor.printJsonRequest;
import static com.dkorobtsov.logging.TextUtils.isFileRequest;

import com.dkorobtsov.logging.Level;
import com.dkorobtsov.logging.LoggingInterceptor;
import com.dkorobtsov.logging.RequestDetails;
import java.util.Objects;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;

public class ApacheHttpRequestInterceptor implements HttpRequestInterceptor {

    private final boolean isDebug;
    private final LoggingInterceptor.Builder builder;

    public ApacheHttpRequestInterceptor(LoggingInterceptor.Builder builder) {
        this.builder = builder;
        this.isDebug = builder.isDebug();
    }

    @Override
    public void process(HttpRequest request, HttpContext context) {
        if (isDebug && builder.getLevel() != Level.NONE) {
            final Request requestDetails = new RequestDetails.Builder().from(request).build();
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
        }
    }
}
