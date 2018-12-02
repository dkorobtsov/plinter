package com.dkorobtsov.logging;

import com.dkorobtsov.logging.internal.InterceptedHeaders;
import com.dkorobtsov.logging.internal.InterceptedMediaType;
import com.dkorobtsov.logging.internal.InterceptedResponseBody;

public class ResponseDetails {

    public final InterceptedResponseBody responseBody;
    public final InterceptedHeaders headers;
    public final int code;
    public final boolean isSuccessful;
    public final String message;
    public final InterceptedMediaType mediaType;

    ResponseDetails(InterceptedResponseBody responseBody, InterceptedHeaders headers, int code,
        boolean isSuccessful,
        String message, InterceptedMediaType mediaType) {
        this.responseBody = responseBody;
        this.headers = headers;
        this.code = code;
        this.isSuccessful = isSuccessful;
        this.message = message;
        this.mediaType = mediaType;
    }

    public static ResponseDetailsBuilder builder() {
        return new ResponseDetailsBuilder();
    }

    public String toString() {
        return "ResponseDetails(responseBody=" + this.responseBody + ", headers=" + this.headers
            + ", code=" + this.code + ", isSuccessful=" + this.isSuccessful + ", message="
            + this.message + ", mediaType=" + this.mediaType + ", url=" + ", originalBody=" + ")";
    }

    public static class ResponseDetailsBuilder {

        private InterceptedResponseBody responseBody;
        private InterceptedHeaders headers;
        private int code;
        private boolean isSuccessful;
        private String message;
        private InterceptedMediaType mediaType;

        ResponseDetailsBuilder() {
        }

        public ResponseDetails.ResponseDetailsBuilder responseBody(
            InterceptedResponseBody responseBody) {
            this.responseBody = responseBody;
            return this;
        }

        public ResponseDetails.ResponseDetailsBuilder headers(InterceptedHeaders headers) {
            this.headers = headers;
            return this;
        }

        public ResponseDetails.ResponseDetailsBuilder code(int code) {
            this.code = code;
            return this;
        }

        public ResponseDetails.ResponseDetailsBuilder isSuccessful(boolean isSuccessful) {
            this.isSuccessful = isSuccessful;
            return this;
        }

        public ResponseDetails.ResponseDetailsBuilder message(String message) {
            this.message = message;
            return this;
        }

        public ResponseDetails.ResponseDetailsBuilder mediaType(InterceptedMediaType mediaType) {
            this.mediaType = mediaType;
            return this;
        }

        public ResponseDetails build() {
            return new ResponseDetails(responseBody, headers, code, isSuccessful, message,
                mediaType);
        }

        public String toString() {
            return "ResponseDetails.ResponseDetailsBuilder(responseBody=" + this.responseBody
                + ", headers=" + this.headers
                + ", code=" + this.code
                + ", isSuccessful=" + this.isSuccessful
                + ", message=" + this.message
                + ", mediaType=" + this.mediaType + ")";
        }
    }
}
