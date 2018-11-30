package com.dkorobtsov.logging.enums;

public enum InterceptorVersion {
    OKHTTP("okhttp"),
    OKHTTP3("okhttp3"),
    APACHE_HTTPCLIENT_REQUEST("apacheHttpclientRequest");

    private String name;

    public String getName() {
        return name;
    }

    public static InterceptorVersion parse(String name) {
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

    InterceptorVersion(String name) {
        this.name = name;
    }
}
