package io.github.dkorobtsov.plinter;

import static io.github.dkorobtsov.plinter.core.internal.Util.CONTENT_TYPE;
import static io.github.dkorobtsov.plinter.utils.TestUtil.PRINTING_THREAD_PREFIX;
import static java.util.Objects.nonNull;
import static org.junit.Assert.fail;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import io.github.dkorobtsov.plinter.apache.ApacheHttpRequestInterceptor;
import io.github.dkorobtsov.plinter.apache.ApacheHttpResponseInterceptor;
import io.github.dkorobtsov.plinter.core.LogWriter;
import io.github.dkorobtsov.plinter.core.LoggerConfig;
import io.github.dkorobtsov.plinter.core.LoggerConfig.LoggerConfigBuilder;
import io.github.dkorobtsov.plinter.core.LoggingFormat;
import io.github.dkorobtsov.plinter.okhttp.OkHttpLoggingInterceptor;
import io.github.dkorobtsov.plinter.okhttp3.OkHttp3LoggingInterceptor;
import io.github.dkorobtsov.plinter.utils.Interceptor;
import io.github.dkorobtsov.plinter.utils.TestLogger;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.junit.Before;
import org.junit.Rule;

/**
 * Starting point for all tests. Contains all general methods for use in child tests. Check specific
 * method javadocs for details.
 */
@SuppressWarnings({"ClassDataAbstractionCoupling", "ClassFanOutComplexity", "PMD.ExcessiveImports"})
public abstract class BaseTest {

  static final String MOCK_SERVER_PATH = "/";

  private static final org.apache.logging.log4j.Logger logger
      = org.apache.logging.log4j.LogManager.getLogger(BaseTest.class.getName());

  private static final ConnectionPool CONNECTION_POOL = new ConnectionPool();
  private static final Dispatcher DISPATCHER = new Dispatcher();
  private static final int KEEP_ALIVE_DURATION_MS = 60 * 1000;
  private static final int MAX_IDLE_CONNECTIONS = 10;

  @Rule
  public MockWebServer server = new MockWebServer();

  @Before
  public void cleanAnyExistingJavaUtilityLoggingConfigurations() {
    LogManager.getLogManager().reset();
    final Logger globalLogger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    globalLogger.setLevel(Level.OFF);
  }

  /**
   * Method intended for request validation. Intercepts http request with provided body details sent
   * with default client implementation and gets mocked server response with status 200.
   *
   * @param interceptor Interceptor version (like OkHttp3, Apache etc.) Check {@link Interceptor}
   * for valid values.
   * @param withExecutor if value is true, logger will print traffic in separate thread
   * @param body request body content as String, can be null
   * @param mediaType request body media type, can be null
   * @param preserveTrailingSpaces if true, logger output will have trailing spaces - exactly the
   * way it's displayed in console, if false, trailing spaces will be trimmed, leaving only string
   * contents for validation
   * @return logger output for validation
   *
   * NB. If content and media type params are not provided request won't have body.
   */
  List<String> interceptedRequest(String interceptor, boolean withExecutor,
      String body, String mediaType, boolean preserveTrailingSpaces) {

    server.enqueue(new MockResponse().setResponseCode(200));

    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    final LoggerConfig loggerConfig = defaultLoggerConfig(testLogger, withExecutor, null);

    interceptWithConfig(interceptor, loggerConfig, body, mediaType,
        String.valueOf(server.url(MOCK_SERVER_PATH)));

    return testLogger.loggerOutput(preserveTrailingSpaces);
  }

  /**
   * Method intended for response validation. Sends http request with empty body using default
   * client implementation intercepting mocked server response with provided body and media type.
   *
   * @param interceptor Interceptor version (like OkHttp3, Apache etc.) Check {@link Interceptor}
   * for valid values.
   * @param withExecutor if value is true, logger will print traffic in separate thread
   * @param body response body content as String, can be null
   * @param mediaType response body media type, can be null
   * @param preserveTrailingSpaces if true, logger output will have trailing spaces - exactly the
   * way it's displayed in console, if false, trailing spaces will be trimmed, leaving only string
   * contents for validation
   * @return logger output for validation
   *
   * NB. If content and media type params are not provided response won't have body.
   */
  List<String> interceptedResponse(String interceptor, boolean withExecutor,
      String body, String mediaType, boolean preserveTrailingSpaces) {

    server.enqueue(new MockResponse()
        .setResponseCode(200)
        .setHeader(CONTENT_TYPE, mediaType)
        .setBody(body));

    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    final LoggerConfig loggerConfig = defaultLoggerConfig(testLogger, withExecutor, null);

    interceptWithConfig(interceptor, loggerConfig);

    return testLogger.loggerOutput(preserveTrailingSpaces);
  }


  public LoggerConfig defaultLoggerConfig(final LogWriter logWriter) {
    return defaultLoggerConfig(logWriter, false, null);
  }

  /**
   * Default Logger configuration for use in tests.
   *
   * @param logWriter LogWriter instance.
   * @param withExecutor If specified, intercepted traffic will be printed in separate thread.
   * @param lineLength if specified, will line length will be modified. If null, default value will
   * be used.
   */
  LoggerConfig defaultLoggerConfig(final LogWriter logWriter,
      final boolean withExecutor, final Integer lineLength) {

    final LoggerConfigBuilder builder = LoggerConfig.builder()
        .withThreadInfo(true)
        .logger(logWriter);

    if (nonNull(lineLength)) {
      builder.maxLineLength(lineLength);
    }

    if (withExecutor) {
      builder.executor(Executors.newSingleThreadExecutor(new ThreadFactory() {
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
          return new Thread(r, PRINTING_THREAD_PREFIX + "-" + threadNumber.getAndIncrement());
        }

      }));
    }

    return builder.build();
  }

  /**
   * Method executes empty http request using default client implementation. Given test logger was
   * provided to logger configuration, after this method execution we can validate test logger
   * output.
   */
  public void interceptWithConfig(String interceptor, LoggerConfig loggerConfig) {
    interceptWithConfig(interceptor, loggerConfig, null, null,
        String.valueOf(server.url(MOCK_SERVER_PATH)));
  }

  /**
   * Method executes http request with provided body details using default client implementation.
   * Given test logger was provided to logger configuration, after this method execution we can
   * validate test logger output.
   *
   * @param interceptor Interceptor version (like OkHttp3, Apache etc.) Check {@link Interceptor}
   * for valid values.
   * @param loggerConfig LoggerConfiguration that will be used to print intercepted traffic
   * @param body body content as String, can be null
   * @param mediaType body media type, can be null
   *
   * NB. Note that if this method executed directly, server response should be mocked otherwise
   * connection will time out.
   *
   * NB. If content and media type params are not provided request won't have body.
   */
  @SuppressWarnings("PMD.UseObjectForClearerAPI")
  public void interceptWithConfig(String interceptor, LoggerConfig loggerConfig,
      String body, String mediaType, String url) {

    switch (Interceptor.fromString(interceptor)) {
      case OKHTTP:
        logger.info("OkHttp Interceptor: {}",
            loggerConfig.toString());

        executeOkHttpRequest(
            defaultOkHttpClient(new OkHttpLoggingInterceptor(loggerConfig)),
            okHttpRequest(body, mediaType, url));
        break;

      case OKHTTP3:
        logger.info("OkHttp3 Interceptor: {}",
            loggerConfig.toString());

        executeOkHttp3Request(
            defaultOkHttp3Client(new OkHttp3LoggingInterceptor(loggerConfig)),
            okHttp3Request(body, mediaType, url));
        break;

      case APACHE_HTTPCLIENT_REQUEST:
        logger.info("Apache Interceptors: {}",
            loggerConfig.toString());

        executeApacheRequest(defaultApacheClient(
            new ApacheHttpRequestInterceptor(loggerConfig),
            new ApacheHttpResponseInterceptor(loggerConfig)),
            apacheHttpRequest(body, mediaType, url));
        break;

      default:
        fail("Unknown interceptor version: " + interceptor);
        break;
    }
  }

  Response executeOkHttp3Request(OkHttpClient client, Request request) {
    try {
      return client.newCall(request).execute();
    } catch (IOException e) {
      logger.error(e);
    }
    return null;
  }

  com.squareup.okhttp.Response executeOkHttpRequest(com.squareup.okhttp.OkHttpClient client,
      com.squareup.okhttp.Request request) {
    try {
      return client.newCall(request).execute();
    } catch (IOException e) {
      logger.error(e);
    }
    return null;
  }

  HttpResponse executeApacheRequest(HttpClient client, HttpUriRequest request) {
    try {
      return client.execute(request);
    } catch (IOException e) {
      logger.error(e);
    }
    return null;
  }

  /**
   * Returns OkHttp3 client for use in tests.
   */
  OkHttpClient defaultOkHttp3Client(okhttp3.Interceptor interceptor) {
    return new OkHttpClient.Builder()
        .connectionPool(CONNECTION_POOL)
        .dispatcher(DISPATCHER)
        .addNetworkInterceptor(interceptor)
        .build();
  }

  /**
   * Returns Apache client for use in tests.
   */
  HttpClient defaultApacheClient(ApacheHttpRequestInterceptor requestInterceptor,
      ApacheHttpResponseInterceptor responseInterceptor) {

    return HttpClientBuilder
        .create()
        .addInterceptorFirst(requestInterceptor)
        .addInterceptorFirst(responseInterceptor)
        .setMaxConnTotal(MAX_IDLE_CONNECTIONS)
        .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
        .build();
  }

  /**
   * Returns OkHttp client for use in tests.
   */
  com.squareup.okhttp.OkHttpClient defaultOkHttpClient(
      com.squareup.okhttp.Interceptor interceptor) {

    final com.squareup.okhttp.OkHttpClient okHttpClient = new com.squareup.okhttp.OkHttpClient()
        .setConnectionPool(
            new com.squareup.okhttp.ConnectionPool(MAX_IDLE_CONNECTIONS,
                KEEP_ALIVE_DURATION_MS))
        .setDispatcher(new com.squareup.okhttp.Dispatcher());
    okHttpClient.interceptors().add(interceptor);
    return okHttpClient;
  }

  /**
   * Returns default OkHttp3 request for use in tests.
   *
   * @param content Request body content as String. Can be null.
   * @param mediaType Request body media type. Can be null.
   *
   * To add body to request both content and media type should be non null, otherwise request will
   * be empty.
   */
  Request okHttp3Request(String content, String mediaType, String url) {
    final Request.Builder requestBuilder = new Request.Builder()
        .url(url);

    if (nonNull(content) && nonNull(mediaType)) {
      requestBuilder.put(RequestBody.create(
          MediaType.parse(mediaType), content));
    } else {
      // If not specified, OkHttp3 will automatically add: Accept-encoding: gzip
      requestBuilder.addHeader("Accept-Encoding", "identity");
    }
    return requestBuilder.build();
  }

  /**
   * Returns default Apache HTTP request for use in tests. If body is not provided, GET request will
   * be sent, otherwise default request is PUT.
   *
   * @param content Request body content as String. Can be null.
   * @param mediaType Request body media type. Can be null.
   *
   * To add body to request both content and media type should be non null, otherwise request will
   * be empty.
   */
  private HttpUriRequest apacheHttpRequest(String content, String mediaType, String url) {
    final HttpUriRequest request;
    if (nonNull(content) && nonNull(mediaType)) {
      request = new HttpPut(url);
      final ContentType contentType = ContentType.create(mediaType);

      final HttpEntity entity = new StringEntity(content, contentType);

      ((HttpPut) request).setEntity(entity);
      request.setHeader(new BasicHeader(CONTENT_TYPE, mediaType));
    } else {
      request = new HttpGet(url);
      // If not specified, Apache Client will automatically add: Accept-encoding: gzip
      request.addHeader("Accept-Encoding", "identity");
    }
    return request;
  }

  /**
   * Returns default OkHttp request for use in tests.
   *
   * @param content Request body content as String. Can be null.
   * @param mediaType Request body media type. Can be null.
   *
   * To add body to request both content and media type should be non null, otherwise request will
   * be empty.
   */
  private com.squareup.okhttp.Request okHttpRequest(String content, String mediaType, String url) {
    final com.squareup.okhttp.Request.Builder requestBuilder
        = new com.squareup.okhttp.Request.Builder()
        .url(url);

    if (nonNull(content) && nonNull(mediaType)) {
      requestBuilder.put(com.squareup.okhttp.RequestBody.create(
          com.squareup.okhttp.MediaType.parse(mediaType), content));
    }
    return requestBuilder.build();
  }

  /**
   * Returns list of parameters for data driven tests.
   *
   * Format: "Interceptor name 1", "Interceptor name 2" etc
   *
   * For valid interceptor names please check: {@link Interceptor}
   *
   * NB: In IDE current method shown as unused, but it's refereed in @Parameters annotation in child
   * classes.
   */
  String[] interceptors() {
    return new String[]{
        "okhttp", "okhttp3", "apacheHttpclientRequest",
    };
  }

  /**
   * Returns list of parameters for data driven tests.
   *
   * Format: "Interceptor name, should use manually provided executor?"
   *
   * For valid interceptor names please check: {@link Interceptor}
   *
   * NB: In IDE current method shown as unused, but it's refereed in @Parameters annotation in child
   * classes.
   */
  String[] interceptorsWithExecutors() {
    return new String[]{
        "okhttp, true",
        "okhttp, false",
        "okhttp3, true",
        "okhttp3, false",
        "apacheHttpclientRequest, true",
        "apacheHttpclientRequest, false",
    };
  }

  /**
   * Returns list of valid max line lengths. {@link LoggerConfig#maxLineLength}
   *
   * NB: In IDE current method shown as unused, but it's refereed in @Parameters annotation in child
   * classes.
   */
  String[] validMaxLineSizes() {
    return new String[]{
        "80", "110", "180",
    };
  }

  /**
   * Returns list of invalid max line lengths. {@link LoggerConfig#maxLineLength}
   *
   * NB: In IDE current method shown as unused, but it's refereed in @Parameters annotation in child
   * classes.
   */
  String[] invalidMaxLineSizes() {
    return new String[]{
        "79", "181", "-1",
    };
  }

}
