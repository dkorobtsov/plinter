package com.dkorobtsov.logging;

import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

final class TestUtil {

  private TestUtil() {
  }

  private static final ConnectionPool connectionPool = new ConnectionPool();
  private static final Dispatcher dispatcher = new Dispatcher();

  /**
   * Returns OkHttpClient for all interceptor tests to use as a starting point.
   *
   * <p>The shared instance allows all tests to share a single connection pool, which prevents idle
   * connections from consuming unnecessary resources while connections wait to be evicted.
   */
  public static OkHttpClient defaultClientWithInterceptor(Interceptor interceptor) {
    return new OkHttpClient.Builder()
        .connectionPool(connectionPool)
        .dispatcher(dispatcher)
        .addNetworkInterceptor(interceptor)
        .build();
  }

}
