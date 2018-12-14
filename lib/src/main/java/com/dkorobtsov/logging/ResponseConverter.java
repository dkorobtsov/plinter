package com.dkorobtsov.logging;

import com.dkorobtsov.logging.internal.InterceptedResponse;
import java.net.URL;

public interface ResponseConverter<T> {

  InterceptedResponse from(T response, URL url, Long ms);

}
