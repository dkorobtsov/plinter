package io.github.dkorobtsov.plinter;

import static io.github.dkorobtsov.plinter.internal.Util.APPLICATION_JSON;

import com.squareup.okhttp.mockwebserver.MockResponse;
import io.github.dkorobtsov.plinter.okhttp3.OkHttp3LoggingInterceptor;
import io.github.dkorobtsov.plinter.utils.TestLogger;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * General smoke test for output resizing validation.
 *
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
            "{\"name\": \"elolejipaqimacelogegejovonugiqomikakulekarixenirugudezebipuxuqohefuyep");

    Assertions.assertThat(testLogger.formattedOutput())
        .contains(
            "uxadopagakipilepaciliyejomanicalihebabebirosefuvegecuvufunikiyekalukuziharaqocog");

    Assertions.assertThat(testLogger.formattedOutput())
        .contains(
            "ovukuperibanikohijovatenutopelobokuxajasatahudagid\"}");
  }

  @Test(expected = IllegalArgumentException.class)
  @Parameters(method = "invalidMaxLineSizes")
  public void invalidOutputLengthHandling(String maxLineLength) {
    new OkHttp3LoggingInterceptor(
        io.github.dkorobtsov.plinter.LoggerConfig.builder()
            .maxLineLength(Integer.parseInt(maxLineLength))
            .build());
  }

  @Test
  @Parameters(method = "validMaxLineSizes")
  public void validOutputLengthHandling(String maxLineLength) {
    final OkHttp3LoggingInterceptor interceptor = new OkHttp3LoggingInterceptor(
        io.github.dkorobtsov.plinter.LoggerConfig.builder()
            .maxLineLength(Integer.parseInt(maxLineLength))
            .build());

    Assert.assertEquals(Integer.parseInt(maxLineLength),
        interceptor.loggerConfig().maxLineLength);
  }

}
