package io.github.dkorobtsov.plinter.core.internal;

import java.util.List;

/**
 * Internal implementation of intercepted response. In order to break hard dependency on external
 * clients and have more flexibility, interceptor-core is operating with custom request/response
 * objects.
 */
public final class InterceptedResponse {

  public final InterceptedMediaType contentType;
  public final boolean hasPrintableBody;
  public final List<String> segmentList;
  public final boolean isSuccessful;
  public final byte[] originalBody;
  public final String message;
  public final String header;
  public final long chainMs;
  public final String url;
  public final int code;

  @SuppressWarnings("PMD.ExcessiveParameterList")
  InterceptedResponse(List<String> segmentList, String header, int code, boolean isSuccessful,
      String message, InterceptedMediaType contentType, String url,
      byte[] originalBody, boolean hasPrintableBody, long chainMs) {

    this.hasPrintableBody = hasPrintableBody;
    this.isSuccessful = isSuccessful;
    this.originalBody = originalBody;
    this.segmentList = segmentList;
    this.contentType = contentType;
    this.chainMs = chainMs;
    this.message = message;
    this.header = header;
    this.code = code;
    this.url = url;
  }

  @SuppressWarnings("JavadocType")
  public static ResponseDetailsBuilder builder() {
    return new ResponseDetailsBuilder();
  }

  @SuppressWarnings({"PMD", "JavadocType"})
  public static class ResponseDetailsBuilder {

    private InterceptedMediaType contentType;
    private boolean hasPrintableBody;
    private List<String> segmentList;
    private boolean isSuccessful;
    private byte[] originalBody;
    private String message;
    private String header;
    private long chainMs;
    private String url;
    private int code;

    public ResponseDetailsBuilder contentType(InterceptedMediaType contentType) {
      this.contentType = contentType;
      return this;
    }

    public ResponseDetailsBuilder hasPrintableBody(boolean hasPrintableBody) {
      this.hasPrintableBody = hasPrintableBody;
      return this;
    }

    public ResponseDetailsBuilder segmentList(List<String> segmentList) {
      this.segmentList = segmentList;
      return this;
    }

    public ResponseDetailsBuilder isSuccessful(boolean isSuccessful) {
      this.isSuccessful = isSuccessful;
      return this;
    }

    public ResponseDetailsBuilder originalBody(byte[] originalBody) {
      this.originalBody = originalBody;
      return this;
    }

    public ResponseDetailsBuilder message(String message) {
      this.message = message;
      return this;
    }

    public ResponseDetailsBuilder header(String header) {
      this.header = header;
      return this;
    }

    public ResponseDetailsBuilder chainMs(long chainMs) {
      this.chainMs = chainMs;
      return this;
    }

    public ResponseDetailsBuilder code(int code) {
      this.code = code;
      return this;
    }

    public ResponseDetailsBuilder url(String url) {
      this.url = url;
      return this;
    }

    public InterceptedResponse build() {
      return new InterceptedResponse(segmentList, header, code, isSuccessful, message,
          contentType, url, originalBody, hasPrintableBody, chainMs);
    }

  }
}
