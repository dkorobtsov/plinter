package com.dkorobtsov.logging;

import com.dkorobtsov.logging.converters.ToOkhttp3Converter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.http.HttpResponse;

public final class ResponseDetails {

    final List<String> segmentList;
    final String header;
    final int code;
    final boolean isSuccessful;
    final String message;
    public final MediaType contentType;
    public final String url;
    public final String originalBody;
    final long chainMs;

    ResponseDetails(List<String> segmentList, String header, int code, boolean isSuccessful,
        String message, MediaType contentType, String url,
        String originalBody, long chainMs) {
        this.segmentList = segmentList;
        this.header = header;
        this.code = code;
        this.isSuccessful = isSuccessful;
        this.message = message;
        this.contentType = contentType;
        this.url = url;
        this.originalBody = originalBody;
        this.chainMs = chainMs;
    }

    public static ResponseDetails from(Request request, Response response, long chainMs,
        boolean isFileRequest) throws IOException {
        final List<String> segmentList = request.url().encodedPathSegments();
        final String header = response.headers().toString();
        final int code = response.code();
        final boolean isSuccessful = response.isSuccessful();
        final String message = response.message();
        final ResponseBody responseBody = response.body();
        final MediaType contentType = Objects.requireNonNull(responseBody).contentType();
        final String url = response.request().url().toString();
        final String originalBody =
            isFileRequest ? null : responseBody.string();

        return ResponseDetails
            .builder()
            .segmentList(segmentList)
            .header(header)
            .code(code)
            .isSuccessful(isSuccessful)
            .message(message)
            .originalBody(originalBody)
            .contentType(contentType)
            .url(url)
            .chainMs(chainMs)
            .build();
    }

    public static ResponseDetails from(HttpResponse response, boolean isFileRequest)
        throws IOException {
        final List<String> stringifiedHeaders = Arrays.stream(response
            .getAllHeaders())
            .map(headerElement -> String
                .format("%s=%s", headerElement.getName(), headerElement.getValue()))
            .collect(Collectors.toList());
        final String header = String.join(";", stringifiedHeaders);
        final int code = response.getStatusLine().getStatusCode();
        final boolean isSuccessful = code >= 200 && code <= 300;
        final String message = HttpStatusCodes.findMessage(code);
        final ResponseBody responseBody = ToOkhttp3Converter
            .convertApacheHttpResponseBodyTo3(response);
        final MediaType contentType = responseBody.contentType();
        final String url = "";
        final String originalBody =
            isFileRequest ? null : responseBody.string();

        return ResponseDetails
            .builder()
            .segmentList(Collections.emptyList())
            .header(header)
            .code(code)
            .isSuccessful(isSuccessful)
            .message(message)
            .originalBody(originalBody)
            .contentType(contentType)
            .url(url)
            .build();
    }

    public static ResponseDetailsBuilder builder() {
        return new ResponseDetailsBuilder();
    }

    public static class ResponseDetailsBuilder {

        private List<String> segmentList;
        private String header;
        private int code;
        private boolean isSuccessful;
        private String message;
        private MediaType contentType;
        private String url;
        private String originalBody;
        private long chainMs;

        ResponseDetailsBuilder() {
        }

        ResponseDetailsBuilder segmentList(List<String> segmentList) {
            this.segmentList = segmentList;
            return this;
        }

        ResponseDetailsBuilder header(String header) {
            this.header = header;
            return this;
        }

        ResponseDetailsBuilder code(int code) {
            this.code = code;
            return this;
        }

        ResponseDetailsBuilder isSuccessful(boolean isSuccessful) {
            this.isSuccessful = isSuccessful;
            return this;
        }

        ResponseDetailsBuilder message(String message) {
            this.message = message;
            return this;
        }

        ResponseDetailsBuilder contentType(MediaType contentType) {
            this.contentType = contentType;
            return this;
        }

        public ResponseDetailsBuilder url(String url) {
            this.url = url;
            return this;
        }

        ResponseDetailsBuilder chainMs(long chainMs) {
            this.chainMs = chainMs;
            return this;
        }

        ResponseDetailsBuilder originalBody(String originalBody) {
            this.originalBody = originalBody;
            return this;
        }

        public ResponseDetails build() {
            return new ResponseDetails(segmentList, header, code, isSuccessful, message,
                contentType, url, originalBody, chainMs);
        }

    }
}
