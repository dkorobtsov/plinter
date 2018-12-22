package com.dkorobtsov.logging.internal;

import java.util.List;

/**
 * Internal implementation of intercepted response. In order to break hard dependency on external
 * clients and have more flexibility, interceptor-core is operating with custom request/response
 * objects.
 */
public final class InterceptedResponse {

  public final List<String> segmentList;
  public final String header;
  public final int code;
  public final boolean isSuccessful;
  public final String message;
  public final InterceptedMediaType contentType;
  public final String url;
  public final byte[] originalBody;
  public final boolean hasPrintableBody;
  public final long chainMs;

  @SuppressWarnings("PMD.ExcessiveParameterList")
  InterceptedResponse(List<String> segmentList, String header, int code, boolean isSuccessful,
      String message, InterceptedMediaType contentType, String url,
      byte[] originalBody, boolean hasPrintableBody, long chainMs) {
    this.segmentList = segmentList;
    this.header = header;
    this.code = code;
    this.isSuccessful = isSuccessful;
    this.message = message;
    this.contentType = contentType;
    this.url = url;
    this.originalBody = originalBody;
    this.hasPrintableBody = hasPrintableBody;
    this.chainMs = chainMs;
  }

  @SuppressWarnings("JavadocType")
  public static ResponseDetailsBuilder builder() {
    return new ResponseDetailsBuilder();
  }

  @SuppressWarnings({"PMD", "JavadocType"})
  public static class ResponseDetailsBuilder {

    private List<String> segmentList;
    private String header;
    private int code;
    private boolean isSuccessful;
    private String message;
    private InterceptedMediaType contentType;
    private String url;
    private byte[] originalBody;
    private boolean hasPrintableBody;
    private long chainMs;

    public ResponseDetailsBuilder segmentList(List<String> segmentList) {
      this.segmentList = segmentList;
      return this;
    }

    public ResponseDetailsBuilder header(String header) {
      this.header = header;
      return this;
    }

    public ResponseDetailsBuilder code(int code) {
      this.code = code;
      return this;
    }

    public ResponseDetailsBuilder isSuccessful(boolean isSuccessful) {
      this.isSuccessful = isSuccessful;
      return this;
    }

    public ResponseDetailsBuilder message(String message) {
      this.message = message;
      return this;
    }

    public ResponseDetailsBuilder contentType(InterceptedMediaType contentType) {
      this.contentType = contentType;
      return this;
    }

    public ResponseDetailsBuilder url(String url) {
      this.url = url;
      return this;
    }

    public ResponseDetailsBuilder chainMs(long chainMs) {
      this.chainMs = chainMs;
      return this;
    }

    public ResponseDetailsBuilder originalBody(byte[] originalBody) {
      this.originalBody = originalBody;
      return this;
    }

    public ResponseDetailsBuilder hasPrintableBody(boolean hasPrintableBody) {
      this.hasPrintableBody = hasPrintableBody;
      return this;
    }

    public InterceptedResponse build() {
      return new InterceptedResponse(segmentList, header, code, isSuccessful, message,
          contentType, url, originalBody, hasPrintableBody, chainMs);
    }

  }
}
