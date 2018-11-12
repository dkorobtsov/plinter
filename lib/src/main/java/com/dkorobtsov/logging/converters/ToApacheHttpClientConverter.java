package com.dkorobtsov.logging.converters;

import java.io.IOException;
import okhttp3.RequestBody;
import okio.Buffer;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

public class ToApacheHttpClientConverter {

    public static HttpEntity okhttp3RequestBodyToStringEntity(RequestBody requestBody,
        ContentType contentType) throws IOException {
        final Buffer buffer = new Buffer();
        if (requestBody == null) {
            return new StringEntity("");
        }
        requestBody.writeTo(buffer);
        final String responseString = buffer.readUtf8();
        return new StringEntity(responseString, contentType);
    }
}
