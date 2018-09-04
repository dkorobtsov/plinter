package com.dkorobtsov.logging;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
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
    return () -> Printer.printJsonRequest(builder.logger, builder.level, request);
  }

  private static Runnable createFileRequestRunnable(final LoggingInterceptor.Builder builder,
      final Request request) {
    return () -> Printer.printFileRequest(builder.logger, builder.level, request);
  }

  private static Runnable createPrintJsonResponseRunnable(final LoggingInterceptor.Builder builder,
      ResponseDetails
          responseDetails) {
    return () -> Printer.printJsonResponse(builder.logger, builder.level, responseDetails);
  }

  private static Runnable createFileResponseRunnable(final LoggingInterceptor.Builder builder,
      ResponseDetails responseDetails) {
    return () -> Printer
        .printFileResponse(builder.logger, builder.level, responseDetails);
  }

  @Override
  public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();
    HashMap<String, String> headerMap = builder.getHeaders();
    if (headerMap.size() > 0) {
      Request.Builder requestBuilder = request.newBuilder();
      headerMap.forEach(requestBuilder::addHeader);
      request = requestBuilder.build();
    }

    HashMap<String, String> queryMap = builder.getHttpUrl();
    if (queryMap.size() > 0) {
      HttpUrl.Builder httpUrlBuilder = request.url().newBuilder(request.url().toString());
      queryMap.forEach(Objects.requireNonNull(httpUrlBuilder)::addQueryParameter);
      request = request.newBuilder().url(httpUrlBuilder.build()).build();
    }

    if (!isDebug || builder.getLevel() == Level.NONE) {
      return chain.proceed(request);
    }

    final RequestBody requestBody = request.body();

    String requestSubtype = null;
    if (requestBody != null && requestBody.contentType() != null) {
      requestSubtype = Objects.requireNonNull(requestBody.contentType()).subtype();
    }

    if (isNotFileRequest(requestSubtype)) {
      printJsonRequest(request);
    } else {
      printFileRequest(request);
    }

    final long st = System.nanoTime();
    final Response response = chain.proceed(request);
    final long chainMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - st);

    String subtype = null;
    final ResponseBody body;

    if (Objects.requireNonNull(response.body()).contentType() != null) {
      subtype = Objects.requireNonNull(response.body().contentType()).subtype();
    }

    ResponseDetails responseDetails = gatherResponseDetails(request, response, chainMs,
        !isNotFileRequest(subtype));

    if (isNotFileRequest(subtype)) {
      printJsonResponse(responseDetails);
      body = ResponseBody.create(responseDetails.contentType, responseDetails.bodyString);
    } else {
      printFileResponse(responseDetails);
      return response;
    }

    return response.newBuilder()
        .body(body)
        .build();
  }

  private ResponseDetails gatherResponseDetails(Request request, Response response, long chainMs,
      boolean isFileRequest)
      throws IOException {
    final List<String> segmentList = request.url().encodedPathSegments();
    final String header = response.headers().toString();
    final int code = response.code();
    final boolean isSuccessful = response.isSuccessful();
    final String message = response.message();
    final ResponseBody responseBody = response.body();
    final MediaType contentType = Objects.requireNonNull(responseBody).contentType();
    final String url = response.request().url().toString();
    final String bodyString = isFileRequest ? null : Printer.formattedBody(responseBody.string());

    return ResponseDetails
        .builder()
        .segmentList(segmentList)
        .header(header)
        .code(code)
        .isSuccessful(isSuccessful)
        .message(message)
        .bodyString(bodyString)
        .contentType(contentType)
        .url(url)
        .chainMs(chainMs)
        .build();
  }

  private boolean isNotFileRequest(final String subtype) {
    return subtype != null && (subtype.contains("json")
        || subtype.contains("xml")
        || subtype.contains("plain")
        || subtype.contains("html"));
  }

  private void printFileResponse(ResponseDetails responseDetails) {
    if (builder.executor != null) {
      builder.executor.execute(createFileResponseRunnable(builder, responseDetails));
    } else {
      Printer.printFileResponse(builder.getLogger(), builder.getLevel(), responseDetails);
    }
  }

  private void printJsonResponse(ResponseDetails responseDetails) {
    if (builder.executor != null) {
      builder.executor.execute(createPrintJsonResponseRunnable(builder, responseDetails));
    } else {
      Printer.printJsonResponse(builder.logger, builder.level, responseDetails);
    }
  }

  private void printFileRequest(Request request) {
    if (builder.executor != null) {
      builder.executor.execute(createFileRequestRunnable(builder, request));
    } else {
      Printer.printFileRequest(builder.logger, builder.level, request);
    }
  }

  private void printJsonRequest(Request request) {
    if (builder.executor != null) {
      builder.executor.execute(createPrintJsonRequestRunnable(builder, request));
    } else {
      Printer.printJsonRequest(builder.logger, builder.level, request);
    }
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
