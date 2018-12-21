package com.dkorobtsov.logging;

import static com.dkorobtsov.logging.internal.Util.APPLICATION_JSON;
import static org.junit.Assert.assertTrue;

import com.dkorobtsov.logging.interceptors.okhttp3.OkHttp3LoggingInterceptor;
import com.dkorobtsov.logging.utils.TestLogger;
import com.squareup.okhttp.mockwebserver.MockResponse;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class OutputResizingTest extends BaseTest {

  private static final String TEST_JSON = "{name: \"John\", age: 31, city: \"New York\"}";

  @Test
  @Parameters(method = "interceptors")
  public void printerOutputCanBeResized(String interceptor) {
    server.enqueue(new MockResponse().setResponseCode(200));
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);

    interceptWithConfig(interceptor, LoggerConfig.builder()
        .logger(testLogger)
        .maxLineLength(10)
        .build(), TEST_JSON, APPLICATION_JSON);

    assertTrue("Interceptor should be able to log simple json body.",
        testLogger.formattedOutput().contains("Method: @P"));

    assertTrue("Interceptor should be able to log simple json body.",
        testLogger.formattedOutput().contains("cess : tru"));
  }

  @Test(expected = IllegalArgumentException.class)
  @Parameters({
      "9", "501"
  })
  public void invalidOutputLengthHandling(String maxLineLength) {
    new OkHttp3LoggingInterceptor(
        LoggerConfig.builder()
            .maxLineLength(Integer.parseInt(maxLineLength))
            .build());
  }

  @Test
  @Parameters({
      "10", "500"
  })
  public void validOutputLengthHandling(String maxLineLength) {
    final OkHttp3LoggingInterceptor interceptor = new OkHttp3LoggingInterceptor(
        LoggerConfig.builder()
            .maxLineLength(Integer.parseInt(maxLineLength))
            .build());

    Assert.assertEquals(Integer.parseInt(maxLineLength),
        interceptor.loggerConfig().maxLineLength);
  }

}
