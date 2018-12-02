package com.dkorobtsov.logging.interceptors.apache;

import com.dkorobtsov.logging.AbstractInterceptor;
import com.dkorobtsov.logging.internal.ClientPrintingExecutor;
import com.dkorobtsov.logging.internal.InterceptedResponse;
import com.dkorobtsov.logging.LoggerConfig;
import com.dkorobtsov.logging.ResponseConverter;
import com.dkorobtsov.logging.Level;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.protocol.HttpContext;

public class ApacheHttpResponseInterceptor extends AbstractInterceptor
    implements HttpResponseInterceptor {

    private ResponseConverter<HttpResponse> responseAdapter;

    public ApacheHttpResponseInterceptor(LoggerConfig loggerConfig) {
        this.responseAdapter = new ApacheResponseConverter();
        this.loggerConfig = loggerConfig;
    }

    @Override
    public void process(HttpResponse response, HttpContext context) {
        if (loggerConfig.isLoggable && loggerConfig.level != Level.NONE) {

            InterceptedResponse interceptedResponse = responseAdapter.convertFrom(
                response, null, null);

            ClientPrintingExecutor.printResponse(loggerConfig, interceptedResponse);
        }
    }

}
