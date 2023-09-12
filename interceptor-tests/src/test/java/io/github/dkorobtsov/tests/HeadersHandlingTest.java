package io.github.dkorobtsov.tests;

import io.github.dkorobtsov.plinter.core.LoggingFormat;
import io.github.dkorobtsov.tests.utils.TestLogger;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import okhttp3.mockwebserver.MockResponse;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests to validate that headers are handled properly.
 */
@RunWith(JUnitParamsRunner.class)
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
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
            new SimpleEntry<>("Header1", "Value2")));

    assertThat(testLogger.formattedOutput())
        .containsIgnoringCase("Header1")
        .contains("Value2")
        .contains("Value1");
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
            new SimpleEntry<>("Header2", "Value1")));

    assertThat(testLogger.formattedOutput())
        .containsIgnoringCase("Header1: Value1")
        .containsIgnoringCase("Header2: Value1");
  }

}
