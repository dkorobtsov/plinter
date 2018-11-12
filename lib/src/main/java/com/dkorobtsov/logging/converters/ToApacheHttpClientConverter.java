package com.dkorobtsov.logging.converters;

import java.io.IOException;
import okhttp3.RequestBody;
import okio.Buffer;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

public class ToApacheHttpClientConverter {

    private ToApacheHttpClientConverter() {
    }

    public static HttpEntity okhttp3RequestBodyToStringEntity(RequestBody requestBody,
        ContentType contentType) throws IOException {

        if (requestBody == null) {
            return new StringEntity("");
        }

        final String responseString;
        try (final Buffer buffer = new Buffer()) {
            requestBody.writeTo(buffer);
            responseString = buffer.readUtf8();
        }

        return new StringEntity(responseString, contentType);
    }
}
