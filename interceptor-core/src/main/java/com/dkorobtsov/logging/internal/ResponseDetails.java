package com.dkorobtsov.logging.internal;

public class ResponseDetails {

  public final InterceptedResponseBody responseBody;
  public final InterceptedHeaders headers;
  public final int code;
  public final boolean isSuccessful;
  public final String message;
  public final InterceptedMediaType mediaType;
  public final Protocol protocol;

  ResponseDetails(InterceptedResponseBody responseBody, InterceptedHeaders headers, int code,
      boolean isSuccessful,
      String message, InterceptedMediaType mediaType,
      Protocol protocol) {
    this.responseBody = responseBody;
    this.headers = headers;
    this.code = code;
    this.isSuccessful = isSuccessful;
    this.message = message;
    this.mediaType = mediaType;
    this.protocol = protocol;
  }

  public static ResponseDetailsBuilder builder() {
    return new ResponseDetailsBuilder();
  }

  @Override
  public String toString() {
    return "ResponseDetails{"
        + "responseBody=" + responseBody
        + ", headers=" + headers
        + ", code=" + code
        + ", isSuccessful=" + isSuccessful
        + ", message='" + message + '\''
        + ", mediaType=" + mediaType
        + ", protocol=" + protocol
        + '}';
  }

  public static class ResponseDetailsBuilder {

    private InterceptedResponseBody responseBody;
    private InterceptedHeaders headers;
    private int code;
    private boolean isSuccessful;
    private String message;
    private InterceptedMediaType mediaType;
    private Protocol protocol;

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

    public ResponseDetailsBuilder protocol(Protocol protocol) {
      this.protocol = protocol;
      return this;
    }

    public ResponseDetails build() {
      return new ResponseDetails(responseBody, headers, code, isSuccessful, message, mediaType,
          protocol);
    }

    @Override
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
