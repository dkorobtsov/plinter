package com.dkorobtsov.logging;

import static com.dkorobtsov.logging.BodyUtil.hasPrintableBody;
import static com.dkorobtsov.logging.internal.Util.encodedPathSegments;

import com.dkorobtsov.logging.internal.InterceptedMediaType;
import com.dkorobtsov.logging.internal.InterceptedResponseBody;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ResponseHandler {

    public static InterceptedResponse interceptedResponse(ResponseDetails response,
        URL requestUrl, Long chainMs) {

        // Trying to determine if body should be pretty printed or omitted as file request
        String mediaType = null;
        if (Objects.requireNonNull(response.responseBody).contentType() != null) {
            mediaType = Objects.requireNonNull(response.responseBody.contentType()).subtype();
        }
        final boolean hasPrintableBody = hasPrintableBody(mediaType);

        final List<String> segmentList = Objects.isNull(requestUrl)
            ? Collections.emptyList() : encodedPathSegments(requestUrl);

        final String header = response.headers.toString();
        final int code = response.code;
        final boolean isSuccessful = response.isSuccessful;
        final String message = response.message;
        final InterceptedResponseBody responseBody = response.responseBody;
        final InterceptedMediaType contentType = Objects.requireNonNull(responseBody).contentType();
        final String url = Objects.isNull(requestUrl) ? "" : requestUrl.toString();

        String originalBody;
        try {
            originalBody = responseBody.string();
        } catch (IOException e) {
            originalBody = "";
            e.printStackTrace();
        }

        return InterceptedResponse
            .builder()
            .segmentList(segmentList)
            .header(header)
            .code(code)
            .isSuccessful(isSuccessful)
            .message(message)
            .originalBody(originalBody)
            .contentType(contentType)
            .hasPrintableBody(hasPrintableBody)
            .url(url)
            .chainMs(Objects.isNull(chainMs) ? 0 : chainMs)
            .build();
    }

}
