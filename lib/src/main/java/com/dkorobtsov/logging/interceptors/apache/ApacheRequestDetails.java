package com.dkorobtsov.logging.interceptors.apache;

import static com.dkorobtsov.logging.interceptors.apache.ToOkHttp3Converter.convertApacheHttpRequestBodyTo3;
import static com.squareup.okhttp.internal.http.HttpMethod.permitsRequestBody;

import com.dkorobtsov.logging.internal.InterceptedRequest;
import java.util.Arrays;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpRequestWrapper;

public class ApacheRequestDetails {

    private ApacheRequestDetails() {

    }

    public static InterceptedRequest interceptedRequest(HttpRequest apacheHttpRequest) {
        final InterceptedRequest.Builder builder = new InterceptedRequest.Builder();
        builder.url(buildUrlFromApacheHttpRequest(apacheHttpRequest));
        final Header[] headersMap = apacheHttpRequest.getAllHeaders();
        Arrays.stream(headersMap)
            .forEach(header -> builder.addHeader(header.getName(), header.getValue()));
        final String method = apacheHttpRequest.getRequestLine().getMethod();
        if (permitsRequestBody(method)) {
            builder.method(method, convertApacheHttpRequestBodyTo3(apacheHttpRequest));
        } else {
            builder.method(method, null);
        }
        return builder.build();
    }

    private static String buildUrlFromApacheHttpRequest(HttpRequest request) {
        final HttpHost target = ((HttpRequestWrapper) request).getTarget();
        final String portString = target.getPort() == -1 ? "" : ":" + target.getPort();
        return String
            .format("%s://%s%s%s", target.getSchemeName(), target.getHostName(), portString,
                ((HttpRequestWrapper) request).getURI());
    }

}
