package com.dkorobtsov.logging.interceptors.okhttp;

import com.dkorobtsov.logging.ResponseDetails;
import com.dkorobtsov.logging.internal.InterceptedHeaders;
import com.dkorobtsov.logging.internal.InterceptedMediaType;
import com.dkorobtsov.logging.internal.InterceptedResponseBody;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("Duplicates")
class OkHttpResponseDetails {

    private static final Logger logger = Logger.getLogger(OkHttpResponseDetails.class.getName());

    OkHttpResponseDetails() {

    }

    static ResponseDetails responseDetails(Response response) {
        if (response == null) {
            return null;
        } else {
            return ResponseDetails.builder()
                .code(response.code())
                .headers(interceptedHeaders(response.headers()))
                .isSuccessful(response.isSuccessful())
                .mediaType(interceptedMediaType(response.body().contentType()))
                .message(response.message())
                .responseBody(interceptedResponseBody(response.body()))
                .build();
        }
    }

    private static InterceptedHeaders interceptedHeaders(Headers headers) {
        final InterceptedHeaders.Builder headersBuilder = new InterceptedHeaders.Builder();
        headers.names().forEach(name -> headersBuilder.add(name, headers.get(name)));
        return headersBuilder.build();
    }

    private static InterceptedResponseBody interceptedResponseBody(ResponseBody responseBody) {
        if (Objects.isNull(responseBody)) {
            return null;
        } else {
            final MediaType mediaType = responseBody.contentType();
            String responseBodyString = "";
            try {
                responseBodyString = new String(responseBody.bytes(), Charset.defaultCharset());
            } catch (IOException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
            return InterceptedResponseBody
                .create(interceptedMediaType(mediaType), responseBodyString);
        }
    }

    private static InterceptedMediaType interceptedMediaType(MediaType mediaType) {
        return mediaType == null ? InterceptedMediaType.parse("")
            : InterceptedMediaType.parse(mediaType.toString());
    }

}
