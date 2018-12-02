package com.dkorobtsov.logging.interceptors.apache;

import com.dkorobtsov.logging.ClientPrintingExecutor;
import com.dkorobtsov.logging.InterceptedResponse;
import com.dkorobtsov.logging.LoggerConfig;
import com.dkorobtsov.logging.enums.Level;
import com.dkorobtsov.logging.interceptors.AbstractInterceptor;
import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.protocol.HttpContext;

public class ApacheHttpResponseInterceptor extends AbstractInterceptor implements
    HttpResponseInterceptor {

    public ApacheHttpResponseInterceptor(LoggerConfig loggerConfig) {
        this.loggerConfig = loggerConfig;
    }

    @Override
    public void process(HttpResponse response, HttpContext context) throws IOException {
        if (loggerConfig.isLoggable && loggerConfig.level != Level.NONE) {

            InterceptedResponse interceptedResponse = interceptedResponse(
                ApacheResponseDetails.responseDetails(response), null, null);

            ClientPrintingExecutor.printResponse(loggerConfig, interceptedResponse);
        }
    }

}
