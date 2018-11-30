package com.dkorobtsov.logging;

import static com.dkorobtsov.logging.converters.ToOkHttp3Converter.convertApacheHttpRequestBodyTo3;
import static com.dkorobtsov.logging.converters.ToOkHttp3Converter.convertOkHttpCacheControlTo3;
import static com.dkorobtsov.logging.converters.ToOkHttp3Converter.convertOkHttpRequestBodyTo3;
import static com.squareup.okhttp.internal.http.HttpMethod.permitsRequestBody;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import okhttp3.Request;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpRequestWrapper;

public class RequestDetails {

    private RequestDetails() {
    }

    public static class Builder {

        private int numberOfClientsInitialized = 0;
        private com.squareup.okhttp.Request okHttpRequest;
        private HttpRequest apacheHttpRequest;

        private static String buildUrlFromApacheHttpRequest(HttpRequest request) {
            final HttpHost target = ((HttpRequestWrapper) request).getTarget();
            final String portString = target.getPort() == -1 ? "" : ":" + target.getPort();
            return String
                .format("%s://%s%s%s", target.getSchemeName(), target.getHostName(), portString,
                    ((HttpRequestWrapper) request).getURI());
        }

        public Builder from(com.squareup.okhttp.Request okHttpRequest) {
            this.okHttpRequest = okHttpRequest;
            this.numberOfClientsInitialized++;
            return this;
        }

        public Builder from(HttpRequest apacheHttpRequest) {
            this.apacheHttpRequest = apacheHttpRequest;
            this.numberOfClientsInitialized++;
            return this;
        }

        public Request build() {
            if (numberOfClientsInitialized == 0 || numberOfClientsInitialized > 1) {
                throw new IllegalArgumentException(
                    "You can only initialize one client in the builder. " +
                        "No more then 1 'interceptedResponse' method per builder invocation allowed");
            } else if (okHttpRequest != null) {
                return buildFromOkHttp();
            } else {
                return buildFromApacheHttpRequest();
            }
        }

        private Request buildFromOkHttp() {
            final okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
            builder.url(this.okHttpRequest.url());
            final Map<String, List<String>> headersMap = this.okHttpRequest.headers().toMultimap();
            headersMap.forEach((String name, List<String> values)
                -> builder.addHeader(name, String.join(";", values)));

            if (permitsRequestBody(this.okHttpRequest.method())) {
                builder
                    .method(this.okHttpRequest.method(),
                        convertOkHttpRequestBodyTo3(this.okHttpRequest));
            } else {
                builder.method(this.okHttpRequest.method(), null);
            }
            builder.tag(this.okHttpRequest.tag());
            builder.cacheControl(convertOkHttpCacheControlTo3(this.okHttpRequest.cacheControl()));
            return builder.build();
        }

        private Request buildFromApacheHttpRequest() {
            final okhttp3.Request.Builder builder = new okhttp3.Request
                .Builder();
            builder.url(buildUrlFromApacheHttpRequest(this.apacheHttpRequest));
            final Header[] headersMap = this.apacheHttpRequest.getAllHeaders();
            Arrays.stream(headersMap)
                .forEach(header -> builder.addHeader(header.getName(), header.getValue()));
            final String method = this.apacheHttpRequest.getRequestLine().getMethod();
            if (permitsRequestBody(method)) {
                builder.method(method, convertApacheHttpRequestBodyTo3(this.apacheHttpRequest));
            } else {
                builder.method(method, null);
            }
            return builder.build();
        }

    }

}

