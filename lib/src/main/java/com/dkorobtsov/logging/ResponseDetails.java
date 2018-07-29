package com.dkorobtsov.logging;

import java.util.List;
import okhttp3.MediaType;

final class ResponseDetails {

  final List<String> segmentList;
  final String header;
  final int code;
  final boolean isSuccessful;
  final String message;
  final MediaType contentType;
  final String url;
  final String bodyString;
  final long chainMs;

  ResponseDetails(List<String> segmentList, String header, int code, boolean isSuccessful,
      String message, MediaType contentType, String url,
      String bodyString, long chainMs) {
    this.segmentList = segmentList;
    this.header = header;
    this.code = code;
    this.isSuccessful = isSuccessful;
    this.message = message;
    this.contentType = contentType;
    this.url = url;
    this.bodyString = bodyString;
    this.chainMs = chainMs;
  }

  static ResponseDetailsBuilder builder() {
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
    private String bodyString;
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

    ResponseDetailsBuilder url(String url) {
      this.url = url;
      return this;
    }

    ResponseDetailsBuilder chainMs(long chainMs) {
      this.chainMs = chainMs;
      return this;
    }

    ResponseDetailsBuilder bodyString(String bodyString) {
      this.bodyString = bodyString;
      return this;
    }

    ResponseDetails build() {
      return new ResponseDetails(segmentList, header, code, isSuccessful, message,
          contentType, url, bodyString, chainMs);
    }

  }
}
