package io.github.dkorobtsov.plinter;

import static org.assertj.core.api.Assertions.assertThat;

import com.squareup.okhttp.mockwebserver.MockResponse;
import io.github.dkorobtsov.plinter.core.LoggingFormat;
import io.github.dkorobtsov.plinter.utils.TestLogger;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class HeadersHandlingTest extends BaseTest {

  @Test
  @Parameters(method = "interceptors")
  public void multipleHeaders_sameNameDifferentValues(String interceptor) {
    server.enqueue(new MockResponse().setResponseCode(200));
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);

    interceptWithConfig(interceptor, defaultLoggerConfig(testLogger),
        String.valueOf(server.url(MOCK_SERVER_PATH)),
        Arrays.asList(
            new SimpleEntry<>("Header1", "Value1"),
            new SimpleEntry<>("Header1", "Value2")),
        null, null);

    assertThat(testLogger.formattedOutput()).containsIgnoringCase("header1");
    assertThat(testLogger.formattedOutput()).contains("Value2");
    assertThat(testLogger.formattedOutput()).contains("Value1");
  }

  @Test
  @Parameters(method = "interceptors")
  public void multipleHeaders_differentNamesSameValues(String interceptor) {
    server.enqueue(new MockResponse().setResponseCode(200));
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);

    interceptWithConfig(interceptor, defaultLoggerConfig(testLogger),
        String.valueOf(server.url(MOCK_SERVER_PATH)),
        Arrays.asList(
            new SimpleEntry<>("Header1", "Value1"),
            new SimpleEntry<>("Header2", "Value1")),
        null, null);

    assertThat(testLogger.formattedOutput()).containsIgnoringCase("Header1: Value1");
    assertThat(testLogger.formattedOutput()).containsIgnoringCase("Header2: Value1");
  }

}
