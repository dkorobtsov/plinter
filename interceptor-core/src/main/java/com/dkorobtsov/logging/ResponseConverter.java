package com.dkorobtsov.logging;

import com.dkorobtsov.logging.internal.InterceptedResponse;
import java.net.URL;


/**
 * Base interface for helper classes converting client specific HTTP responses to internal {@link
 * InterceptedResponse}.
 *
 * @param <T> type of Http client specific response
 */
@SuppressWarnings({"JavadocType", "NonEmptyAtclauseDescription"})
public interface ResponseConverter<T> {

  InterceptedResponse from(T response, URL url, Long ms);

}
