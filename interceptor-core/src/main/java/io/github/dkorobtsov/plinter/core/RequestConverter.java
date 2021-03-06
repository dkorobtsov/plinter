package io.github.dkorobtsov.plinter.core;

import io.github.dkorobtsov.plinter.core.internal.InterceptedRequest;

/**
 * Base interface for helper classes converting client specific HTTP requests to internal {@link
 * InterceptedRequest}.
 *
 * @param <T> type of Http client specific request
 */
public interface RequestConverter<T> {

  InterceptedRequest from(T request);

}
