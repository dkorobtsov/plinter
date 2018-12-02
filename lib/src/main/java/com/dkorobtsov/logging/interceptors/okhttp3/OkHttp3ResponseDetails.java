package com.dkorobtsov.logging.interceptors.okhttp3;

import com.dkorobtsov.logging.ResponseDetails;
import com.dkorobtsov.logging.internal.InterceptedHeaders;
import com.dkorobtsov.logging.internal.InterceptedMediaType;
import com.dkorobtsov.logging.internal.InterceptedResponseBody;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;

@SuppressWarnings("Duplicates")
class OkHttp3ResponseDetails {

    private static final Logger logger = Logger.getLogger(OkHttp3ResponseDetails.class.getName());

    OkHttp3ResponseDetails() {

    }

    static ResponseDetails responseDetails(Response response) {
        if (response == null) {
            return null;
        } else {
            return ResponseDetails.builder()
                .code(response.code())
                .headers(interceptedHeaders(response.headers()))
                .isSuccessful(response.isSuccessful())
                .mediaType(interceptedMediaType(
                    Objects.isNull(response.body())
                        ? null
                        : response.body().contentType()))
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
