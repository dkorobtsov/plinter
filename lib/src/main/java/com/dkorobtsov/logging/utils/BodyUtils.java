package com.dkorobtsov.logging.utils;

import java.util.Objects;
import okhttp3.Request;
import okhttp3.RequestBody;

public class BodyUtils {

    private BodyUtils() {
    }

    public static String subtype(Request request) {
        final RequestBody requestBody = request.body();

        String requestSubtype = null;
        if (requestBody != null && requestBody.contentType() != null) {
            requestSubtype = Objects.requireNonNull(requestBody.contentType()).subtype();
        }
        return requestSubtype;
    }

}
