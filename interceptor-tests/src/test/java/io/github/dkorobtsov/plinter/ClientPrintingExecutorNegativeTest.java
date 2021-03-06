package io.github.dkorobtsov.plinter;

import static io.github.dkorobtsov.plinter.core.internal.Util.APPLICATION_JSON;
import static io.github.dkorobtsov.plinter.core.internal.Util.APPLICATION_ZIP;
import static io.github.dkorobtsov.plinter.core.internal.Util.CONTENT_TYPE;

import io.github.dkorobtsov.plinter.core.LoggerConfig;
import io.github.dkorobtsov.plinter.core.LoggingFormat;
import io.github.dkorobtsov.plinter.core.internal.ClientPrintingExecutor;
import io.github.dkorobtsov.plinter.core.internal.InterceptedRequest;
import io.github.dkorobtsov.plinter.core.internal.InterceptedResponse;
import io.github.dkorobtsov.plinter.utils.TestLogger;
import io.github.dkorobtsov.plinter.utils.TestUtil;
import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * Tests validating that printing thread interruption will not cause any unexpected exceptions.
 */
public class ClientPrintingExecutorNegativeTest extends BaseTest {

  @Test
  public void testInterruptingPrintingJsonRequestDoesntCrashProcess() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    final InterceptedRequest request = new InterceptedRequest.Builder()
        .get()
        .addHeader(CONTENT_TYPE, APPLICATION_JSON)
        .url("http://google.com")
        .build();

    final LoggerConfig loggerConfig = LoggerConfig
        .builder()
        .logger(testLogger)
        .executor(loggingExecutor())
        .build();

    assertThatThreadInterruptionDoesNotCauseCrashes(request, loggerConfig);
  }

  @Test
  public void testInterruptingPrintingFileRequestDoesntCrashProcess() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    final InterceptedRequest request = new InterceptedRequest.Builder()
        .get()
        .addHeader(CONTENT_TYPE, APPLICATION_ZIP)
        .url("http://google.com")
        .build();

    final LoggerConfig loggerConfig = LoggerConfig
        .builder()
        .logger(testLogger)
        .executor(loggingExecutor())
        .build();

    assertThatThreadInterruptionDoesNotCauseCrashes(request, loggerConfig);
  }

  @Test
  public void testInterruptingPrintingResponseDoesntCrashProcess() {
    final TestLogger testLogger = new TestLogger(LoggingFormat.JUL_MESSAGE_ONLY);
    final InterceptedResponse response = InterceptedResponse.builder().build();
    final LoggerConfig loggerConfig = LoggerConfig
        .builder()
        .logger(testLogger)
        .executor(loggingExecutor())
        .build();

    assertThatThreadInterruptionDoesNotCauseCrashes(response, loggerConfig);
  }

  private void assertThatThreadInterruptionDoesNotCauseCrashes(InterceptedRequest request,
                                                               LoggerConfig loggerConfig) {
    ClientPrintingExecutor.printRequest(loggerConfig, request);
    final Thread thread = TestUtil.loggingExecutorThread();
    Assertions.assertThat(thread.isInterrupted()).isFalse();

    thread.interrupt();
    Assertions.assertThat(thread.isInterrupted()).isTrue();

    ClientPrintingExecutor.printRequest(loggerConfig, request);
  }

  private void assertThatThreadInterruptionDoesNotCauseCrashes(InterceptedResponse response,
                                                               LoggerConfig loggerConfig) {
    ClientPrintingExecutor.printResponse(loggerConfig, response);
    final Thread thread = TestUtil.loggingExecutorThread();
    Assertions.assertThat(thread.isInterrupted()).isFalse();

    thread.interrupt();
    Assertions.assertThat(thread.isInterrupted()).isTrue();

    ClientPrintingExecutor.printResponse(loggerConfig, response);
  }

}
