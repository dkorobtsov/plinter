package com.dkorobtsov.logging;

public enum InterceptorVersion {
    OKHTTP("okhttp"), OKHTTP3("okhttp3");

    private String name;

    public String getName() {
        return name;
    }

    InterceptorVersion(String name) {

        this.name = name;
    }
}
