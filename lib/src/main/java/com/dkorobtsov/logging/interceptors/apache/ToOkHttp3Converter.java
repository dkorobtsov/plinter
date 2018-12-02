package com.dkorobtsov.logging.interceptors.apache;

import static com.dkorobtsov.logging.interceptors.apache.ApacheResponseDetails.readApacheHttpEntity;
import static com.dkorobtsov.logging.interceptors.apache.ApacheResponseDetails.recreateHttpEntityFromString;

import com.dkorobtsov.logging.internal.InterceptedMediaType;
import com.dkorobtsov.logging.internal.InterceptedRequestBody;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.message.BasicHeader;

public class ToOkHttp3Converter {

    private static final String APPLICATION_JSON = "application/json";

    private static final Logger logger = Logger.getLogger(ToOkHttp3Converter.class.getName());


    private ToOkHttp3Converter() {
    }

    public static InterceptedRequestBody convertApacheHttpRequestBodyTo3(HttpRequest request) {
        if (request instanceof HttpRequestWrapper) {
            final HttpRequest original = ((HttpRequestWrapper) request).getOriginal();
            if (original instanceof HttpEntityEnclosingRequestBase) {
                final HttpEntity entity = ((HttpEntityEnclosingRequestBase) original).getEntity();
                if (entity != null) {
                    String requestBodyString;
                    try {
                        requestBodyString = readApacheHttpEntity(entity);
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, e.getMessage(), e);
                        return InterceptedRequestBody
                            .create(InterceptedMediaType
                                    .parse(APPLICATION_JSON),
                                "[LoggingInterceptorError] : could not parse request body");
                    }

                    final HttpEntity newEntity = recreateHttpEntityFromString(requestBodyString,
                        entity);
                    ((HttpEntityEnclosingRequestBase) ((HttpRequestWrapper) request).getOriginal())
                        .setEntity(newEntity);

                    final Header contentTypeHeader = Arrays
                        .stream(((HttpRequestWrapper) request).getOriginal().getAllHeaders())
                        .filter(header -> header.getName().equals("Content-Type"))
                        .findFirst()
                        .orElse(new BasicHeader("Content-Type", APPLICATION_JSON));

                    return InterceptedRequestBody
                        .create(InterceptedMediaType
                                .parse(contentTypeHeader.getValue()),
                            requestBodyString);
                }
            }
        }
        return InterceptedRequestBody
            .create(InterceptedMediaType.parse(APPLICATION_JSON), "");
    }


}
