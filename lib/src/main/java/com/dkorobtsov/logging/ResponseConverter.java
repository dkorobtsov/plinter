package com.dkorobtsov.logging;

import java.net.URL;

public interface ResponseConverter<T> {

    InterceptedResponse convertFrom(T response, URL url, Long ms);

}
