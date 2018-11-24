package com.dkorobtsov.logging.interceptors;

import com.dkorobtsov.logging.InterceptedResponse;
import com.dkorobtsov.logging.Level;
import com.dkorobtsov.logging.LoggerConfig;
import com.dkorobtsov.logging.TextUtils;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

abstract class AbstractOkHttpInterceptor {

    protected boolean isLoggable;
    protected LoggerConfig loggerConfig;

    public LoggerConfig loggerConfig() {
        return this.loggerConfig;
    }

    protected boolean skipLogging(){
        return !isLoggable || loggerConfig.level == Level.NONE;
    }

    InterceptedResponse interceptedResponse(Request request, Response response, long chainMs)
        throws IOException {

        // Trying to determine if body should be pretty printed or omitted as file request
        String subtype = null;
        if (Objects.requireNonNull(response.body()).contentType() != null) {
            subtype = Objects.requireNonNull(response.body().contentType()).subtype();
        }
        final boolean hasPrintableBody = TextUtils.hasPrintableBody(subtype);

        final List<String> segmentList = request.url().encodedPathSegments();
        final String header = response.headers().toString();
        final int code = response.code();
        final boolean isSuccessful = response.isSuccessful();
        final String message = response.message();
        final ResponseBody responseBody = response.body();
        final MediaType contentType = Objects.requireNonNull(responseBody).contentType();
        final String url = response.request().url().toString();
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
            .chainMs(chainMs)
            .build();
    }


}
