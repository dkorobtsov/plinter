package com.dkorobtsov.logging.utils;

public enum Interceptor {
  OKHTTP("okhttp"),
  OKHTTP3("okhttp3"),
  APACHE_HTTPCLIENT_REQUEST("apacheHttpclientRequest");

  private String name;

  Interceptor(String name) {
    this.name = name;
  }

  public static Interceptor parse(String name) {
    switch (name) {
      case "okhttp":
        return OKHTTP;
      case "okhttp3":
        return OKHTTP3;
      case "apacheHttpclientRequest":
        return APACHE_HTTPCLIENT_REQUEST;
      default:
        throw new IllegalArgumentException("Unknown interceptor version: " + name);
    }
  }

}
