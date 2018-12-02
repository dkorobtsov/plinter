package com.dkorobtsov.logging.interceptors;

import static com.dkorobtsov.logging.BodyUtil.hasPrintableBody;
import static com.dkorobtsov.logging.internal.Util.encodedPathSegments;

import com.dkorobtsov.logging.InterceptedResponse;
import com.dkorobtsov.logging.LoggerConfig;
import com.dkorobtsov.logging.ResponseDetails;
import com.dkorobtsov.logging.enums.Level;
import com.dkorobtsov.logging.internal.InterceptedMediaType;
import com.dkorobtsov.logging.internal.InterceptedResponseBody;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class AbstractInterceptor {

    protected LoggerConfig loggerConfig;

    public LoggerConfig loggerConfig() {
        return this.loggerConfig;
    }

    protected boolean skipLogging() {
        return !loggerConfig.isLoggable || loggerConfig.level == Level.NONE;
    }

    protected InterceptedResponse interceptedResponse(ResponseDetails response,
        URL requestUrl, Long chainMs) throws IOException {

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
        final String originalBody = responseBody.string();

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
