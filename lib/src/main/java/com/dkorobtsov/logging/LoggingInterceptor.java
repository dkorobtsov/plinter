package com.dkorobtsov.logging;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @author ihsan on 09/02/2017.
 */
public class LoggingInterceptor implements Interceptor {

  private final boolean isDebug;
  private final Builder builder;

  private LoggingInterceptor(Builder builder) {
    this.builder = builder;
    this.isDebug = builder.isDebug;
  }

  private static Runnable createPrintJsonRequestRunnable(final LoggingInterceptor.Builder builder,
      final Request request) {
    return () -> Printer.printJsonRequest(builder, request);
  }

  private static Runnable createFileRequestRunnable(final LoggingInterceptor.Builder builder,
      final Request request) {
    return () -> Printer.printFileRequest(builder, request);
  }

  private static Runnable createPrintJsonResponseRunnable(final LoggingInterceptor.Builder builder,
      final long chainMs, final boolean isSuccessful,
      final int code, final String headers, final String bodyString, final List<String> segments,
      final String message, final String responseUrl) {
    return () -> Printer.printJsonResponse(builder, chainMs, isSuccessful,
        code, headers, bodyString, segments, message, responseUrl);
  }

  private static Runnable createFileResponseRunnable(final LoggingInterceptor.Builder builder,
      final long chainMs, final boolean isSuccessful,
      final int code, final String headers, final List<String> segments, final String message) {
    return () -> Printer.printFileResponse(builder, chainMs, isSuccessful,
        code, headers, segments, message);
  }

  @Override
  public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();
    HashMap<String, String> headerMap = builder.getHeaders();
    if (headerMap.size() > 0) {
      Request.Builder requestBuilder = request.newBuilder();
      for (String key : headerMap.keySet()) {
        String value = headerMap.get(key);
        requestBuilder.addHeader(key, value);
      }
      request = requestBuilder.build();
    }

    HashMap<String, String> queryMap = builder.getHttpUrl();
    if (queryMap.size() > 0) {
      HttpUrl.Builder httpUrlBuilder = request.url().newBuilder(request.url().toString());
      for (String key : queryMap.keySet()) {
        String value = queryMap.get(key);
        httpUrlBuilder.addQueryParameter(key, value);
      }
      request = request.newBuilder().url(httpUrlBuilder.build()).build();
    }

    if (!isDebug || builder.getLevel() == Level.NONE) {
      return chain.proceed(request);
    }

    final RequestBody requestBody = request.body();

    String rSubtype = null;
    if (requestBody != null && requestBody.contentType() != null) {
      rSubtype = requestBody.contentType().subtype();
    }

    Executor executor = builder.executor;

    if (isNotFileRequest(rSubtype)) {
      if (executor != null) {
        executor.execute(createPrintJsonRequestRunnable(builder, request));
      } else {
        Printer.printJsonRequest(builder, request);
      }
    } else {
      if (executor != null) {
        executor.execute(createFileRequestRunnable(builder, request));
      } else {
        Printer.printFileRequest(builder, request);
      }
    }

    final long st = System.nanoTime();
    final Response response = chain.proceed(request);
    final long chainMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - st);

    final List<String> segmentList = request.url().encodedPathSegments();
    final String header = response.headers().toString();
    final int code = response.code();
    final boolean isSuccessful = response.isSuccessful();
    final String message = response.message();
    final ResponseBody responseBody = response.body();
    final MediaType contentType = responseBody.contentType();

    String subtype = null;
    final ResponseBody body;

    if (contentType != null) {
      subtype = contentType.subtype();
    }

    if (isNotFileRequest(subtype)) {
      final String bodyString = Printer.getJsonString(responseBody.string());
      final String url = response.request().url().toString();

      if (executor != null) {
        executor.execute(
            createPrintJsonResponseRunnable(builder, chainMs, isSuccessful, code, header,
                bodyString,
                segmentList, message, url));
      } else {
        Printer.printJsonResponse(builder, chainMs, isSuccessful, code, header, bodyString,
            segmentList, message, url);
      }
      body = ResponseBody.create(contentType, bodyString);
    } else {
      if (executor != null) {
        executor.execute(
            createFileResponseRunnable(builder, chainMs, isSuccessful, code, header, segmentList,
                message));
      } else {
        Printer
            .printFileResponse(builder, chainMs, isSuccessful, code, header, segmentList, message);
      }
      return response;
    }

    return response.newBuilder().
        body(body).
        build();
  }

  private boolean isNotFileRequest(final String subtype) {
    return subtype != null && (subtype.contains("json")
        || subtype.contains("xml")
        || subtype.contains("plain")
        || subtype.contains("html"));
  }

  @SuppressWarnings({"unused", "SameParameterValue"})
  public static class Builder {

    private final HashMap<String, String> headers;
    private final HashMap<String, String> queries;
    private boolean isDebug = true;
    private Level level = Level.BASIC;
    private LogWriter logger;
    private LogFormatter formatter;
    private Executor executor;

    public Builder() {
      headers = new HashMap<>();
      queries = new HashMap<>();
      formatter = LogFormatter.JUL_MESSAGE_ONLY;
    }

    /**
     * @param logger manual logging interface
     * @return Builder
     * @see LogWriter
     */
    public Builder logger(LogWriter logger) {
      this.logger = logger;
      return this;
    }

    LogWriter getLogger() {
      if (logger == null) {
        logger = new DefaultLogger(formatter);
      }
      return logger;
    }

    /**
     * @param format set Java Utility Logger format
     * (will be ignored in case custom logger is used)
     * @return Builder
     */
    public Builder format(LogFormatter format) {
      this.formatter = format;
      return this;
    }

    LogFormatter getFormatter() {
      return formatter;
    }

    /**
     * @param isDebug set can sending log output
     * @return Builder
     */
    public Builder loggable(boolean isDebug) {
      this.isDebug = isDebug;
      return this;
    }

    /**
     * @param level set log level
     * @return Builder
     * @see Level
     */
    public Builder level(Level level) {
      this.level = level;
      return this;
    }

    Level getLevel() {
      return level;
    }

    /**
     * @param name Filed
     * @param value Value
     * @return Builder
     * Add a field with the specified value
     */
    public Builder addHeader(String name, String value) {
      headers.put(name, value);
      return this;
    }

    HashMap<String, String> getHeaders() {
      return headers;
    }

    /**
     * @param name Filed
     * @param value Value
     * @return Builder
     * Add a field with the specified value
     */
    public Builder addQueryParam(String name, String value) {
      queries.put(name, value);
      return this;
    }

    HashMap<String, String> getHttpUrl() {
      return queries;
    }

    /**
     * @param executor manual executor for printing
     * @return Builder
     * @see LogWriter
     */
    public Builder executor(Executor executor) {
      this.executor = executor;
      return this;
    }

    Executor getExecutor() {
      return executor;
    }

    public LoggingInterceptor build() {
      return new LoggingInterceptor(this);
    }
  }

}
