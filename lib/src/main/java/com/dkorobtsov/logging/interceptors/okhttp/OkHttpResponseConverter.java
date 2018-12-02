package com.dkorobtsov.logging.interceptors.okhttp;

import static java.util.Objects.isNull;

import com.dkorobtsov.logging.InterceptedResponse;
import com.dkorobtsov.logging.ResponseDetails;
import com.dkorobtsov.logging.ResponseConverter;
import com.dkorobtsov.logging.ResponseHandler;
import com.dkorobtsov.logging.internal.InterceptedHeaders;
import com.dkorobtsov.logging.internal.InterceptedMediaType;
import com.dkorobtsov.logging.internal.InterceptedResponseBody;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("Duplicates")
class OkHttpResponseConverter implements ResponseConverter<Response> {

    private static final Logger logger = Logger.getLogger(OkHttpResponseConverter.class.getName());

    @Override
    public InterceptedResponse convertFrom(Response response, URL requestUrl, Long ms) {
        return ResponseHandler
            .interceptedResponse(responseDetails(response), requestUrl, ms);
    }

    private ResponseDetails responseDetails(Response response) {
        if (isNull(response)) {
            throw new IllegalStateException("httpResponse == null");
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

    private InterceptedHeaders interceptedHeaders(Headers headers) {
        final InterceptedHeaders.Builder headersBuilder = new InterceptedHeaders.Builder();
        headers.names().forEach(name -> headersBuilder.add(name, headers.get(name)));
        return headersBuilder.build();
    }

    private InterceptedResponseBody interceptedResponseBody(ResponseBody responseBody) {
        if (isNull(responseBody)) {
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

    private InterceptedMediaType interceptedMediaType(MediaType mediaType) {
        return mediaType == null ? InterceptedMediaType.parse("")
            : InterceptedMediaType.parse(mediaType.toString());
    }

}
