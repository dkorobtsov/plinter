package com.dkorobtsov.logging;

public enum InterceptorVersion {
    OKHTTP("okhttp"),
    OKHTTP3("okhttp3"),
    APACHE_HTTPCLIENT_REQUEST("apacheHttpclientRequest");

    private String name;

    public String getName() {
        return name;
    }

    InterceptorVersion(String name) {

        this.name = name;
    }
}
