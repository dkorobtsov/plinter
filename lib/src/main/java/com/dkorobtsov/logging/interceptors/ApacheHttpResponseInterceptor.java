package com.dkorobtsov.logging.interceptors;

import com.dkorobtsov.logging.Level;
import com.dkorobtsov.logging.LoggingInterceptor;
import com.dkorobtsov.logging.ResponseDetails;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.util.Objects;

import static com.dkorobtsov.logging.ClientPrintingExecutor.printFileResponse;
import static com.dkorobtsov.logging.ClientPrintingExecutor.printJsonResponse;
import static com.dkorobtsov.logging.TextUtils.isFileRequest;

public class ApacheHttpResponseInterceptor implements HttpResponseInterceptor {

    private final boolean isDebug;
    private final LoggingInterceptor.Builder builder;

    public ApacheHttpResponseInterceptor(LoggingInterceptor.Builder builder) {
        this.builder = builder;
        this.isDebug = builder.isDebug();
    }

    @Override
    public void process(HttpResponse response, HttpContext context) throws IOException {
        if (isDebug && builder.getLevel() != Level.NONE) {
            String subtype = null;
            if (Objects.requireNonNull(response.getEntity()).getContentType() != null) {
                subtype = Objects.requireNonNull(response.getEntity().getContentType()).getValue();
            }

            ResponseDetails responseDetails = ResponseDetails.from(response, isFileRequest(subtype));

            if (isFileRequest(subtype)) {
                printFileResponse(responseDetails, builder);
            } else {
                printJsonResponse(responseDetails, builder);
            }
        }
    }
}
