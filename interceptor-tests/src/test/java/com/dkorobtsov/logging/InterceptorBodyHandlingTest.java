package com.dkorobtsov.logging;

import com.dkorobtsov.logging.interceptors.okhttp3.OkHttp3LoggingInterceptor;
import java.util.concurrent.Executors;
import okhttp3.OkHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class InterceptorBodyHandlingTest extends BaseTest {

  @Test
  public void interceptorShouldNotModifyRequestBody() {
    //todo: add test
  }

  @Test
  public void interceptorShouldNotModifyResponseBody() {
    //todo: add test
    OkHttp3LoggingInterceptor interceptor = new OkHttp3LoggingInterceptor(
        LoggerConfig.builder()
            .logger(new LogWriter() {
              final Logger log = LogManager.getLogger("HttpLogger");

              @Override
              public void log(String msg) {
                log.debug(msg);
              }
            })
            .withThreadInfo(true)
            .loggable(true)
            .maxLineLength(180)
            .level(Level.BASIC)
            .executor(Executors.newSingleThreadExecutor(r -> new Thread(r, "HttpPrinter")))
            .build());

    OkHttpClient okHttpClient = new OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .build();
  }

}
