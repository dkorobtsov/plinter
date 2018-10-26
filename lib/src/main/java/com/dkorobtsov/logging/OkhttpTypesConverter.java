package com.dkorobtsov.logging;

import com.squareup.okhttp.CacheControl;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import okio.Buffer;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.squareup.okhttp.internal.http.HttpMethod.permitsRequestBody;

class OkhttpTypesConverter {

    static okhttp3.MediaType convertOkhttpMediaTypeTo3(MediaType okhttpMediaType) {
        return okhttpMediaType == null ? okhttp3.MediaType.parse("") : okhttp3.MediaType.parse(okhttpMediaType.toString());
    }
    static MediaType convertOkhttp3MediaType(okhttp3.MediaType okhttp3MediaType) {
        return okhttp3MediaType == null ? MediaType.parse("") : MediaType.parse(okhttp3MediaType.toString());
    }

    public static RequestBody convertOkhtt3pRequestBody(okhttp3.Request request) {
        final MediaType contentType = request.body() == null ? MediaType.parse("") : convertOkhttp3MediaType(request.body().contentType());
        try {
            final okhttp3.Request requestCopy = request.newBuilder().build();

            String requestBodyString = "";
            if (requestCopy.body() != null) {
                final Buffer buffer = new Buffer();
                requestCopy.body().writeTo(buffer);
                requestBodyString = buffer.readUtf8();
            }
            return RequestBody.create(contentType, requestBodyString);
        } catch (final IOException e) {
            return RequestBody.create(contentType, "[LoggingInterceptorError] : could not parse request body");
        }
    }

    private static okhttp3.RequestBody convertOkhttpRequestBodyTo3(Request request) {
        final okhttp3.MediaType contentType = request.body() == null ? okhttp3.MediaType.parse("") : convertOkhttpMediaTypeTo3(request.body().contentType());
        try {
            final Request requestCopy = request.newBuilder().build();
            String requestBodyString = "";
            if (requestCopy.body() != null) {
                final Buffer buffer = new Buffer();
                requestCopy.body().writeTo(buffer);
                requestBodyString = buffer.readUtf8();
            }
            return okhttp3.RequestBody.create(contentType, requestBodyString);
        } catch (final IOException e) {
            return okhttp3.RequestBody.create(contentType, "[LoggingInterceptorError] : could not parse request body");
        }
    }

    private static okhttp3.CacheControl convertOkhttpCacheControlTo3(CacheControl cacheControl) {
        return new okhttp3.CacheControl
            .Builder()
            .maxAge(cacheControl.maxAgeSeconds() == -1 ? 0 : cacheControl.maxAgeSeconds(), TimeUnit.SECONDS)
            .maxStale(cacheControl.maxStaleSeconds() == -1 ? 0 : cacheControl.maxStaleSeconds(), TimeUnit.SECONDS)
            .minFresh(cacheControl.minFreshSeconds() == -1 ? 9: cacheControl.minFreshSeconds(), TimeUnit.SECONDS)
            .build();
    }

    static okhttp3.Request convertOkhttpRequestTo3(Request request) {
        final okhttp3.Request.Builder builder = new okhttp3.Request
            .Builder();
        builder.url(request.url());
        final Map<String, List<String>> headersMap = request.headers().toMultimap();
        headersMap.forEach((String name, List<String> values) -> {
            builder.addHeader(name, String.join(";", values));
        });
        if (permitsRequestBody(request.method())) {
            builder.method(request.method(), convertOkhttpRequestBodyTo3(request));
        } else {
            builder.method(request.method(), null);
        }
        builder.tag(request.tag());
        builder.cacheControl(convertOkhttpCacheControlTo3(request.cacheControl()));
        return builder.build();
    }
}
