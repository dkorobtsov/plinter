package com.dkorobtsov.logging.interceptors;

import static com.dkorobtsov.logging.ClientPrintingExecutor.printRequest;
import static com.dkorobtsov.logging.utils.TextUtils.hasPrintableBody;

import com.dkorobtsov.logging.utils.BodyUtils;
import com.dkorobtsov.logging.enums.Level;
import com.dkorobtsov.logging.LoggerConfig;
import com.dkorobtsov.logging.RequestDetails;
import okhttp3.Request;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;

public class ApacheHttpRequestInterceptor implements HttpRequestInterceptor {

    private final boolean isLoggable;
    private final LoggerConfig loggerConfig;

    public ApacheHttpRequestInterceptor(LoggerConfig loggerConfig) {
        this.loggerConfig = loggerConfig;
        this.isLoggable = loggerConfig.isLoggable;
    }

    @Override
    public void process(HttpRequest request, HttpContext context) {
        if (isLoggable && loggerConfig.level != Level.NONE) {
            final Request requestDetails = new RequestDetails.Builder().from(request).build();

            String subtype = BodyUtils.subtype(requestDetails);

            printRequest(loggerConfig, requestDetails, hasPrintableBody(subtype));
        }
    }

    public LoggerConfig loggerConfig() {
        return this.loggerConfig;
    }
}
