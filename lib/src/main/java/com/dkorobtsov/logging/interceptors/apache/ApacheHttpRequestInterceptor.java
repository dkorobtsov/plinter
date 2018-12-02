package com.dkorobtsov.logging.interceptors.apache;

import static com.dkorobtsov.logging.ClientPrintingExecutor.printRequest;
import static com.dkorobtsov.logging.Utilities.hasPrintableBody;
import static com.dkorobtsov.logging.Utilities.subtype;

import com.dkorobtsov.logging.LoggerConfig;
import com.dkorobtsov.logging.interceptors.AbstractInterceptor;
import com.dkorobtsov.logging.internal.InterceptedRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;

public class ApacheHttpRequestInterceptor
    extends AbstractInterceptor implements HttpRequestInterceptor {

    public ApacheHttpRequestInterceptor(LoggerConfig loggerConfig) {
        this.loggerConfig = loggerConfig;
    }

    @Override
    public void process(HttpRequest request, HttpContext context) {
        if (!skipLogging()) {
            final InterceptedRequest requestDetails = ApacheRequestDetails
                .interceptedRequest(request);

            String subtype = subtype(requestDetails);

            printRequest(loggerConfig, requestDetails, hasPrintableBody(subtype));
        }
    }

}
