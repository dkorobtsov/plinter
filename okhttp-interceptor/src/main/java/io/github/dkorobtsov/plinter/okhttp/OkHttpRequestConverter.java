package io.github.dkorobtsov.plinter.okhttp;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import io.github.dkorobtsov.plinter.core.RequestConverter;
import io.github.dkorobtsov.plinter.core.internal.CacheControl;
import io.github.dkorobtsov.plinter.core.internal.InterceptedMediaType;
import io.github.dkorobtsov.plinter.core.internal.InterceptedRequest;
import io.github.dkorobtsov.plinter.core.internal.InterceptedRequestBody;
import okio.Buffer;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.squareup.okhttp.internal.http.HttpMethod.permitsRequestBody;

/**
 * Helper class implementing conversion logic from OkHttp client request to this library's
 * internal {@link InterceptedRequest}.
 */
@SuppressWarnings("Duplicates")
class OkHttpRequestConverter implements RequestConverter<Request> {

  @Override
  public InterceptedRequest from(final Request okHttpRequest) {
    final InterceptedRequest.Builder builder = new InterceptedRequest.Builder();
    builder.url(okHttpRequest.url());
    final Map<String, List<String>> headersMap = okHttpRequest.headers().toMultimap();
    headersMap.forEach((String name, List<String> values)
        -> builder.addHeader(name, String.join(";", values)));

    if (permitsRequestBody(okHttpRequest.method())) {
      builder
          .method(okHttpRequest.method(),
              interceptedRequestBody(okHttpRequest));
    } else {
      builder.method(okHttpRequest.method(), null);
    }
    builder.tag(okHttpRequest.tag());
    builder.cacheControl(cacheControl(okHttpRequest.cacheControl()));
    return builder.build();
  }

  private InterceptedRequestBody interceptedRequestBody(final Request request) {
    final InterceptedMediaType contentType = request.body() == null
        ? InterceptedMediaType.parse("")
        : interceptedMediaType(request.body().contentType());

    try {
      final Request requestCopy = request.newBuilder().build();
      String requestBodyString = "";
      if (requestCopy.body() != null) {
        final Buffer buffer = new Buffer();
        requestCopy.body().writeTo(buffer);
        requestBodyString = buffer.readUtf8();
      }
      return InterceptedRequestBody.create(contentType, requestBodyString);

    } catch (final IOException e) {
      return InterceptedRequestBody
          .create(contentType, "[LoggingInterceptorError] : could not parse request body.");
    }
  }

  private InterceptedMediaType interceptedMediaType(final MediaType mediaType) {
    return mediaType == null ? InterceptedMediaType.parse("")
        : InterceptedMediaType.parse(mediaType.toString());
  }

  private CacheControl cacheControl(final com.squareup.okhttp.CacheControl cacheControl) {
    return new CacheControl.Builder()
        .maxAge(cacheControl.maxAgeSeconds() == -1 ? 0 : cacheControl.maxAgeSeconds(),
            TimeUnit.SECONDS)
        .maxStale(cacheControl.maxStaleSeconds() == -1 ? 0 : cacheControl.maxStaleSeconds(),
            TimeUnit.SECONDS)
        .minFresh(cacheControl.minFreshSeconds() == -1 ? 9 : cacheControl.minFreshSeconds(),
            TimeUnit.SECONDS)
        .build();
  }

}
