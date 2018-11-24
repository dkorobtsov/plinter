package com.dkorobtsov.logging.converters;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import java.io.IOException;
import okio.Buffer;

public class ToOkHttpConverter {

    private ToOkHttpConverter() {
    }

    public static MediaType convertOkHttp3MediaType(okhttp3.MediaType okHttp3MediaType) {
        return okHttp3MediaType == null ? MediaType.parse("")
            : MediaType.parse(okHttp3MediaType.toString());
    }

    public static RequestBody convertOkHttp3RequestBody(okhttp3.Request request) {
        final MediaType contentType = request.body() == null ? MediaType.parse("")
            : convertOkHttp3MediaType(request.body().contentType());
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
            return RequestBody
                .create(contentType, "[LoggingInterceptorError] : could not parse request body");
        }
    }

}
