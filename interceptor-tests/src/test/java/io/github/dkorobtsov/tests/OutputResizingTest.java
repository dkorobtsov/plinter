package io.github.dkorobtsov.tests;

import io.github.dkorobtsov.plinter.core.LoggerConfig;
import io.github.dkorobtsov.plinter.core.LoggingFormat;
import io.github.dkorobtsov.plinter.okhttp3.OkHttp3LoggingInterceptor;
import io.github.dkorobtsov.tests.utils.TestLogger;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import okhttp3.mockwebserver.MockResponse;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.github.dkorobtsov.plinter.core.internal.Util.APPLICATION_JSON;

/**
 * General smoke test for output resizing validation.
 * <p>
 * Dynamic content resizing is tested in scope of Printer related unit tests: {@link
 * RequestsPrintingTest}.
 */
@RunWith(JUnitParamsRunner.class)
public class OutputResizingTest extends BaseTest {

  private static final String TEST_JSON =
      "{name: \"elolejipaqimacelogegejovonugiqomikakulekarixenirugudezebipuxuqohefu"
          + "yepuxadopagakipilepaciliyejomanicalihebabebirosefuvegecuvufunikiyekalu"
          + "kuziharaqocogovukuperibanikohijovatenutopelobokuxajasatahudagid\"}";

  @Test
  @Parameters(method = "interceptors")
  public void printerOutputResizing(String interceptor) {
    server.enqueue(new MockResponse().setResponseCode(200));
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);

    interceptWithConfig(interceptor, LoggerConfig.builder()
        .logger(testLogger)
        .maxLineLength(80)
        .build(), TEST_JSON, APPLICATION_JSON, String.valueOf(server.url(MOCK_SERVER_PATH)));

    Assertions.assertThat(testLogger.formattedOutput())
        .contains(
            "{\"name\": \"elolejipaqimacelogegejovonugiqomikakulekarixenirugudezebipuxuqohefuy");

    Assertions.assertThat(testLogger.formattedOutput())
        .contains(
            "epuxadopagakipilepaciliyejomanicalihebabebirosefuvegecuvufunikiyekalukuziharaq");

    Assertions.assertThat(testLogger.formattedOutput())
        .contains(
            "ocogovukuperibanikohijovatenutopelobokuxajasatahudagid\"}");
  }

  @Test(expected = IllegalArgumentException.class)
  @Parameters(method = "invalidMaxLineSizes")
  public void invalidOutputLengthHandling(String maxLineLength) {
    new OkHttp3LoggingInterceptor(
        LoggerConfig.builder()
            .maxLineLength(Integer.parseInt(maxLineLength))
            .build());
  }

  @Test
  @Parameters(method = "validMaxLineSizes")
  public void validOutputLengthHandling(String maxLineLength) {
    final OkHttp3LoggingInterceptor interceptor = new OkHttp3LoggingInterceptor(
        LoggerConfig.builder()
            .maxLineLength(Integer.parseInt(maxLineLength))
            .build());

    Assert.assertEquals(Integer.parseInt(maxLineLength),
        interceptor.loggerConfig().maxLineLength);
  }

}
