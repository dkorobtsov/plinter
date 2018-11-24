package com.dkorobtsov.logging.interceptors;

import com.dkorobtsov.logging.ClientPrintingExecutor;
import com.dkorobtsov.logging.HttpStatusCodes;
import com.dkorobtsov.logging.InterceptedResponse;
import com.dkorobtsov.logging.Level;
import com.dkorobtsov.logging.LoggerConfig;
import com.dkorobtsov.logging.TextUtils;
import com.dkorobtsov.logging.converters.ToOkHttp3Converter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.protocol.HttpContext;

public class ApacheHttpResponseInterceptor implements HttpResponseInterceptor {

    private final boolean isLoggable;
    private final LoggerConfig loggerConfig;

    public ApacheHttpResponseInterceptor(LoggerConfig loggerConfig) {
        this.loggerConfig = loggerConfig;
        this.isLoggable = loggerConfig.isLoggable;
    }

    @Override
    public void process(HttpResponse response, HttpContext context) throws IOException {
        if (isLoggable && loggerConfig.level != Level.NONE) {
            InterceptedResponse interceptedResponse = interceptedResponse(response);
            ClientPrintingExecutor.printResponse(loggerConfig, interceptedResponse);
        }
    }

    InterceptedResponse interceptedResponse(HttpResponse httpResponse) throws IOException {
        // Trying to determine if body should be pretty printed or omitted as file request
        String subtype = null;
        if (Objects.requireNonNull(httpResponse.getEntity()).getContentType() != null) {
            subtype = Objects.requireNonNull(httpResponse.getEntity().getContentType()).getValue();
        }
        final boolean hasPrintableBody = TextUtils.hasPrintableBody(subtype);

        final List<String> stringifiedHeaders = Arrays.stream(httpResponse
            .getAllHeaders())
            .map(headerElement -> String
                .format("%s=%s", headerElement.getName(), headerElement.getValue()))
            .collect(Collectors.toList());

        final String header = String.join(";", stringifiedHeaders);
        final int code = httpResponse.getStatusLine().getStatusCode();
        final boolean isSuccessful = code >= 200 && code <= 300;
        final String message = HttpStatusCodes.findMessage(code);
        final ResponseBody responseBody = ToOkHttp3Converter
            .convertApacheHttpResponseBodyTo3(httpResponse);
        final MediaType contentType = responseBody.contentType();
        final String url = "";
        final String originalBody = responseBody.string();

        return InterceptedResponse
            .builder()
            .segmentList(Collections.emptyList())
            .header(header)
            .code(code)
            .isSuccessful(isSuccessful)
            .message(message)
            .originalBody(originalBody)
            .hasPrintableBody(hasPrintableBody)
            .contentType(contentType)
            .url(url)
            .build();
    }

    public LoggerConfig loggerConfig() {
        return this.loggerConfig;
    }

}
