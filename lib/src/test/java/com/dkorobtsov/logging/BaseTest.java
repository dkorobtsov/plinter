package com.dkorobtsov.logging;

import com.squareup.okhttp.mockwebserver.MockWebServer;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.junit.BeforeClass;
import org.junit.Rule;

public class BaseTest {

  private static final ConnectionPool connectionPool = new ConnectionPool();
  private static final Dispatcher dispatcher = new Dispatcher();

  @Rule public MockWebServer server = new MockWebServer();

  @BeforeClass
  public static void cleanAnyExistingJavaUtiltyLoggingConfigurations() {
    LogManager.getLogManager().reset();
    Logger globalLogger = Logger.getLogger(java.util.logging.Logger.GLOBAL_LOGGER_NAME);
    globalLogger.setLevel(java.util.logging.Level.OFF);
  }

  /**
   * Returns default OkHttp request for use in tests.
   */
  Request defaultRequest() {
    return new Request.Builder()
        .url(String.valueOf(server.url("/")))
        .build();
  }

  /**
   * Returns OkHttpClient for all interceptor tests to use as a starting point.
   *
   * <p>The shared instance allows all tests to share a single connection pool, which prevents idle
   * connections from consuming unnecessary resources while connections wait to be evicted.
   */
  OkHttpClient defaultClientWithInterceptor(Interceptor interceptor) {
    return new OkHttpClient.Builder()
        .connectionPool(connectionPool)
        .dispatcher(dispatcher)
        .addNetworkInterceptor(interceptor)
        .build();
  }

}
