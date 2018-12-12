package com.dkorobtsov.logging;

import com.dkorobtsov.logging.internal.InterceptedRequest;

public interface RequestConverter<T> {

  InterceptedRequest convertFrom(T request);

}
